from django.core.management.base import BaseCommand, CommandError

from django.conf import settings
from django.core.management import call_command
from data.models import DbNamings, Project, Note, GpsLogData, GpsLog, Image, ImageData, Utilities
from django.contrib.auth.models import User
from django.contrib.gis.geos import Point, LineString
from datetime import datetime, timezone
import requests
from requests.auth import HTTPBasicAuth
from django.db import connection
import json
import sqlite3
from django.contrib.gis.geos import Point, LineString
from base64 import b64decode, b64encode

class Command(BaseCommand):
    help = 'Populate the database with example data as a surveyor.'

    def add_arguments(self, parser):
        parser.add_argument('-p', '--gpap', type=str, help='The optional path to the gpap sqlite to import.')
        parser.add_argument('-d', '--delete', action='store_true', help='Flag that enables clearing of the tables first.')

    def handle(self, *args, **options):
        doDelete = options['delete']
        if doDelete:
            self.stdout.write("Clearing data from tables.")
            GpsLogData.objects.all().delete()
            GpsLog.objects.all().delete()
            ImageData.objects.all().delete()
            Image.objects.all().delete()
            Note.objects.all().delete()

        gpapPath = options['gpap']
        if gpapPath:
            self.stdout.write("Importing an existing SMASH project database using the API.")
            try:
                conn = sqlite3.connect(gpapPath)
                cursor = conn.cursor()
                
                surveyorAuth = HTTPBasicAuth('surveyor', 'surveyor')
                base = "http://localhost:8000/api"

                self.insertNotes(cursor, surveyorAuth, base)
                self.insertSimpleImages(cursor, surveyorAuth, base)
                self.insertGpslogs(cursor, surveyorAuth, base)
            except sqlite3.Error as error:
                self.stderr.write('Error occured - ', error)
            finally:
                if conn:
                    conn.close()

        else:
            self.generateExampleData()

    def insertGpslogs(self, cursor, surveyorAuth, base):
        gpslogsUrl = f"{base}/gpslogs/"
        cursor.execute("""
                        SELECT g._id,g.startts,g.endts,g.text,glp.color,glp.width 
                        FROM gpslogs g left join gpslogsproperties glp on g._id=glp.logid
                    """)
        result = cursor.fetchall()
        for row in result:
            id = row[0]
            startts = row[1]
            endts = row[2]
            text = row[3]
            color = row[4]
            width = row[5]

            dt = datetime.fromtimestamp(startts/1000, timezone.utc)
            starttsStr = dt.strftime("%Y-%m-%d %H:%M:%S")
            dt = datetime.fromtimestamp(endts/1000, timezone.utc)
            endtsStr = dt.strftime("%Y-%m-%d %H:%M:%S")


            cursor.execute("""
                            SELECT g._id,g.lon,g.lat,g.altim,g.ts
                            FROM gpslogsdata g where g.logid=1
                        """)
            subResult = cursor.fetchall()

            gpslogdata = []
            coords = []
            for row in subResult:
                subid = row[0]
                lon = row[1]
                lat = row[2]
                altim = row[3]
                ts2 = row[4]
                dt2 = datetime.fromtimestamp(ts2/1000, timezone.utc)
                ts2Str = dt2.strftime("%Y-%m-%d %H:%M:%S")
                gpslogdata.append({
                            DbNamings.GEOM: f"SRID=4326;POINT ({lon} {lat} {altim})", 
                            DbNamings.GPSLOGDATA_TIMESTAMP: ts2Str,
                        })
                coords.append((lon, lat))
                    

            line = LineString(coords, srid=4326)
            newGpslog = {
                        DbNamings.GPSLOG_NAME: text,
                        DbNamings.GPSLOG_STARTTS: starttsStr,
                        DbNamings.GPSLOG_ENDTS: endtsStr,
                        DbNamings.GEOM: line.ewkt,
                        DbNamings.GPSLOG_WIDTH: width,
                        DbNamings.GPSLOG_COLOR: color,
                        DbNamings.USER: 2,
                        DbNamings.PROJECT: 1,
                    }
            newGpslog["gpslogdata"] = gpslogdata

            headers = {
                        "Content-Type":"application/json",
                        "Accept":"application/json",
                    }
            self.stdout.write(f"Uploading Gpslog '{text}' with id: {id}")
            r = requests.post(url = gpslogsUrl,headers=headers, json=json.dumps(newGpslog), auth = surveyorAuth)
            if r.status_code != 201:
                self.stderr.write("Gpslog could not be uploaded:")
                self.stderr.write(r.json())

    def insertSimpleImages(self, cursor, surveyorAuth, base):
        imagesUrl = f"{base}/images/"
        cursor.execute("""
                    SELECT i._id, i.lon,i.lat,i.altim,i.azim,i.ts,i.text,id.data 
                    FROM images i left join imagedata id on i.imagedata_id=id._id 
                    where i.note_id is null
                    """)
        result = cursor.fetchall()

        for row in result:
            id = row[0]
            lon = row[1]
            lat = row[2]
            altim = row[3]
            azim = row[4]
            ts = row[5]
            text = row[6]
            data = row[7]

            dt = datetime.fromtimestamp(ts/1000, timezone.utc)
            tsStr = dt.strftime("%Y-%m-%d %H:%M:%S")

            newImage = {
                        DbNamings.GEOM: f'SRID=4326;POINT ({lon} {lat})', 
                        DbNamings.IMAGE_ALTIM: altim, 
                        DbNamings.IMAGE_TIMESTAMP: tsStr, 
                        DbNamings.IMAGE_AZIMUTH: azim,
                        DbNamings.IMAGE_TEXT: text, 
                        DbNamings.IMAGE_IMAGEDATA: {
                            DbNamings.IMAGEDATA_DATA: b64encode(data).decode('UTF-8')
                        },
                        DbNamings.USER: 2, 
                        DbNamings.PROJECT: 1
                    }
            self.stdout.write(f"Uploading Image '{text}' with id: {id}")
            imageB64 = json.dumps(newImage)
            r = requests.post(url = imagesUrl, json=imageB64, auth = surveyorAuth)
            if r.status_code != 201:
                self.stderr.write("Image could not be uploaded:")
                self.stderr.write(r.json())
    

    def insertNotes(self, cursor, surveyorAuth, base):
        notesUrl = f"{base}/notes/"
        cursor.execute("""
                    SELECT n._id,n.lon,n.lat,n.altim,n.ts,n.description,n.text,n.form,
                    ne.marker,ne.size,ne.rotation,ne.color,ne.accuracy,ne.heading,ne.speed,ne.speedaccuracy
                    FROM notes n left join notesext ne on n._id=ne.noteid
                    """)
        result = cursor.fetchall()
                
        for row in result:
            id = row[0]
            lon = row[1]
            lat = row[2]
            altim = row[3]
            ts = row[4]
            description = row[5]
            text = row[6]
            form = row[7]
            marker = row[8]
            size = row[9]
            rotation = row[10]
            color = row[11]
            accuracy = row[12]
            heading = row[13]
            speed = row[14]
            speedaccuracy = row[15]

            if not accuracy:
                accuracy = -1
            if not heading:
                heading = -1
            if not speed:
                speed = -1
            if not speedaccuracy:
                speedaccuracy = -1

            dt = datetime.fromtimestamp(ts/1000, timezone.utc)
            tsStr = dt.strftime("%Y-%m-%d %H:%M:%S")
            uploadtsStr = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

            newNote = {
                DbNamings.GEOM: f'SRID=4326;POINT ({lon} {lat})', 
                DbNamings.NOTE_ALTIM: altim, 
                DbNamings.NOTE_TS: tsStr, 
                DbNamings.NOTE_UPLOADTS: uploadtsStr, 
                DbNamings.NOTE_DESCRIPTION: description, 
                DbNamings.NOTE_TEXT: text, 
                DbNamings.NOTE_MARKER: marker, 
                DbNamings.NOTE_SIZE: size, 
                DbNamings.NOTE_ROTATION: rotation, 
                DbNamings.NOTE_COLOR: color, 
                DbNamings.NOTE_ACCURACY: accuracy, 
                DbNamings.NOTE_HEADING: heading, 
                DbNamings.NOTE_SPEED: speed, 
                DbNamings.NOTE_SPEEDACCURACY: speedaccuracy, 
                DbNamings.NOTE_FORM: form, 
                DbNamings.USER: 2, 
                DbNamings.PROJECT: 1
            }
            if form != None:
                # find images connected to notes
                imageIds = []
                formDict = json.loads(form)
                Utilities.collectImageIds(formDict, imageIds)
                if imageIds:
                    wherePart = ",".join([str(i) for i in imageIds])
                    cursor.execute(f"""
                                SELECT i._id, i.lon,i.lat,i.altim,i.azim,i.ts,i.text,id.data 
                                FROM images i left join imagedata id on i.imagedata_id=id._id 
                                where i._id in ({wherePart})
                                """)
                    result = cursor.fetchall()

                    imagesMap = {}
                    for row in result:
                        id = row[0]
                        lon = row[1]
                        lat = row[2]
                        altim = row[3]
                        azim = row[4]
                        ts = row[5]
                        text = row[6]
                        data = row[7]

                        dt = datetime.fromtimestamp(ts/1000, timezone.utc)
                        tsStr = dt.strftime("%Y-%m-%d %H:%M:%S")

                        newImage = {
                            DbNamings.GEOM: f'SRID=4326;POINT ({lon} {lat})', 
                            DbNamings.IMAGE_ALTIM: altim, 
                            DbNamings.IMAGE_TIMESTAMP: tsStr, 
                            DbNamings.IMAGE_AZIMUTH: azim,
                            DbNamings.IMAGE_TEXT: text, 
                            DbNamings.IMAGE_IMAGEDATA: {
                                DbNamings.IMAGEDATA_DATA: b64encode(data).decode('UTF-8')
                            },
                            DbNamings.USER: 2, 
                            DbNamings.PROJECT: 1
                        }
                        imagesMap[id] = newImage
                    
                    newNote[DbNamings.NOTE_IMAGES] = imagesMap
            
            self.stdout.write(f"Uploading Note '{text}' with id: {id}")
            noteJson = json.dumps(newNote)
            r = requests.post(url = notesUrl, json = noteJson, auth = surveyorAuth)
            if r.status_code != 201:
                self.stderr.write("Note could not be uploaded:")
                self.stderr.write(r.json())
                



    def generateExampleData(self):
        self.stdout.write("Generating some serverside example data using models and the API.")
            # get default project
        defaultProject = Project.objects.filter(name=DbNamings.PROJECT_DEFAULT).first()
        if not defaultProject:
            self.stderr.write("The Default project is not available, check your data. Exiting.")

            # get surveyor user
        surveyorUser = User.objects.filter(username="surveyor").first()
        if not surveyorUser:
            self.stderr.write("The Surveyor user is not available, check your data. Exiting.")

            # insert some data using models
        if Note.objects.count() == 0:
            note = Note.objects.create(
                    the_geom=Point(11.0, 46.0),
                    altim = 322,
                    ts = "2022-09-23 10:00:00",
                    uploadts = "2022-09-23 16:50:00",
                    description = "A test note, inserted using models",
                    text = "Test Note - models",
                    marker = "circle",
                    size = 10,
                    rotation = 0,
                    color = "#FF0000",
                    accuracy = 0,
                    heading = 0,
                    speed = 0,
                    speedaccuracy = 0,
                    form = "",
                    user = surveyorUser,
                    project = defaultProject
                )
            note.save()

        if GpsLogData.objects.count() == 0:
            gpsLog = GpsLog.objects.create(
                    name = "Test GpsLog",
                    startts = "2022-09-23 10:10:00",
                    endts = "2022-09-23 10:10:09",
                    the_geom = LineString((11.1, 46.1), (11.2, 46.2), (11.4, 46.0), srid=4326),
                    width = 3,
                    color = "#FF0000",
                    user = surveyorUser,
                    project = defaultProject,
                )
            GpsLogData.objects.create(
                    the_geom=Point(11.1, 46.1, 325),
                    ts = "2022-09-23 10:10:00",
                    gpslogid=gpsLog
                )
            GpsLogData.objects.create(
                    the_geom=Point(11.2, 46.2, 356),
                    ts = "2022-09-23 10:10:05",
                    gpslogid=gpsLog
                )
            GpsLogData.objects.create(
                    the_geom=Point(11.4, 46.0, 382),
                    ts = "2022-09-23 10:10:09",
                    gpslogid=gpsLog
                )

        # insert some data using the rest api

        # get list of existing notes
        base = "http://localhost:8000/api"
        surveyorAuth = HTTPBasicAuth('surveyor', 'surveyor')

        notesUrl = f"{base}/notes/"
        r = requests.get(url = notesUrl,auth = surveyorAuth)
        notesList = r.json()
        if len(notesList) == 1:
            newNote = {'id': 1, 'previd': None, 
                            'the_geom': 'SRID=4326;POINT (11.11 46.11)', 
                            'altim': 360.0, 
                            'ts': '2022-09-23T10:00:00Z', 
                            'uploadts': '2022-09-25T16:50:00Z', 
                            'description': 'A test note, inserted via API', 
                            'text': 'Test Note - API', 
                            'marker': 'square', 
                            'size': 12.0, 
                            'rotation': 0.0, 
                            'color': '#00FF00', 
                            'accuracy': 0.0, 
                            'heading': 0.0, 
                            'speed': 0.0, 
                            'speedaccuracy': 0.0, 
                            'form': None, 
                            'user': 2, 
                            'project': 1
                        }
            r = requests.post(url = notesUrl, data = newNote, auth = surveyorAuth)
            if r.status_code != 200:
                print(r.json())

        gpslogsUrl = f"{base}/gpslogs/"
        r = requests.get(url = gpslogsUrl, auth = surveyorAuth)
        gpslogsList = r.json()
        if len(gpslogsList) == 1:
            newGpslog = {
                    "name": "Test GpsLog - API",
                    "startts": "2022-09-25T10:10:00Z",
                    "endts": "2022-09-25T10:10:09Z",
                    "the_geom": "SRID=4326;LINESTRING (11.15 46.15, 11.220552 46.08421, 11.45 46.05)",
                    "width": 5.0,
                    "color": "#00FF00",
                    "user": 2,
                    "project": 1,
                    "gpslogdata": [
                        {
                            "the_geom": "SRID=4326;POINT (11.15 46.15 425)", 
                            "ts": "2022-09-25 10:10:00",
                        },
                        {
                            "the_geom": "SRID=4326;POINT (11.2 46.2 356)", 
                            "ts": "2022-09-25 10:10:05",
                        },
                        {
                            "the_geom": "SRID=4326;POINT (11.45 46.05 482)", 
                            "ts": "2022-09-25 10:10:09",
                        }
                    ]
                }

            print(newGpslog['gpslogdata'][0]['ts'])
            headers = {
                    "Content-Type":"application/json",
                    "Accept":"application/json",
                }
            r = requests.post(url = gpslogsUrl,headers=headers, json=json.dumps(newGpslog), auth = surveyorAuth)
            if r.status_code != 200:
                print(r.json())


    
