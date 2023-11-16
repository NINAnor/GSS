from curses import panel
from datetime import datetime
from enum import unique
from random import choices
from django.db import models
from django.contrib.gis.db import models as geomodels
from django.contrib.gis.geos import Point, LineString
from django.contrib.auth.models import User, Group
from django.utils.safestring import mark_safe
from base64 import b64encode
from django.contrib import admin
from django.core.validators import MaxValueValidator, MinValueValidator
from django.db.models.signals import pre_delete
from gss.utils import Utilities
from django.core.validators import RegexValidator
from django.core.exceptions import ValidationError
import json


class DbNamings():
    GROUP_COORDINATORS = "Coordinators"
    GROUP_SURVEYORS = "Surveyors"
    GROUP_WEBUSERS = "Webusers"
    GROUP_DEFAULT = "Default"
    PROJECT_DEFAULT = "Default"

    GEOM = "the_geom"
    USER = "user" 
    LASTEDIT_USER = "lastedituser" 
    PROJECT = "project" 
    TIMESTAMP = "timestamp" 
    CREATION_TIMESTAMP = "creationts" 
    LASTEDIT_TIMESTAMP = "lasteditts" 

    NOTE_ID = "id"
    NOTE_PREV = "previous"
    NOTE_ALTIM = "altim"
    NOTE_TS = "ts"
    NOTE_UPLOADTS = "uploadts"
    NOTE_DESCRIPTION = "description"
    NOTE_TEXT = "text"
    NOTE_MARKER = "marker"
    NOTE_SIZE = "size"
    NOTE_ROTATION = "rotation"
    NOTE_COLOR = "color"
    NOTE_ACCURACY = "accuracy"
    NOTE_HEADING = "heading"
    NOTE_SPEED = "speed"
    NOTE_SPEEDACCURACY = "speedaccuracy"
    NOTE_FORM = "form"
    NOTE_IMAGES = "images"

    GPSLOG_ID = "id" 
    GPSLOG_NAME = "name" 
    GPSLOG_STARTTS = "startts" 
    GPSLOG_ENDTS = "endts" 
    GPSLOG_UPLOADTIMESTAMP = "uploadts" 
    GPSLOG_COLOR = "color" 
    GPSLOG_WIDTH = "width"
    GPSLOG_DATA = "data"

    GPSLOGDATA_ID = "id" 
    GPSLOGDATA_ALTIM = "altim" 
    GPSLOGDATA_TIMESTAMP = "ts" 
    GPSLOGDATA_GPSLOGS = "gpslogid" 

    IMAGE_ID = "id" 
    IMAGE_ALTIM = "altim" 
    IMAGE_TIMESTAMP = "ts" 
    IMAGE_UPLOADTIMESTAMP = "uploadts" 
    IMAGE_AZIMUTH = "azimuth" 
    IMAGE_TEXT = "text" 
    IMAGE_THUMB = "thumbnail" 
    IMAGE_IMAGEDATA = "imagedata" 
    IMAGE_NOTE = "notes" 
    
    IMAGEDATA_ID = "id" 
    IMAGEDATA_DATA = "data" 

    DEVICE_ID = "id"
    DEVICE_NAME = "name"
    DEVICE_UNIQUE_ID = "uniqueid"
    DEVICE_CONTACT = "contact"
    DEVICE_ACTIVE = "active"

    U_D_ASS_ID = "id"
    U_D_ASS_DEVICEID = "device"
    U_D_ASS_FROMDATE = "fromdate"
    U_D_ASS_TODATE = "todate"

    PROJECT_ID = "id"
    PROJECT_NAME = "name"
    PROJECT_DESCRIPTION = "description"
    PROJECT_GROUPS = "groups"
    
    PROJECTDATA_FILE = "file"
    PROJECTDATA_LABEL = "label"
    
    FORM_NAME = "name"
    FORM_DEFINITION = "definition"
    FORM_GEOMETRYTYPE = "geometrytype"
    FORM_ENABLED = "enabled"
    
    WMSSOURCE_LABEL = "label"
    WMSSOURCE_VERSION = "version"
    WMSSOURCE_TRANSPARENT = "transparent"
    WMSSOURCE_IMAGEFORMAT = "imageformat"
    WMSSOURCE_GETCAPABILITIES = "getcapabilities"
    WMSSOURCE_LAYERNAME = "layername"
    WMSSOURCE_OPACITY = "opacity"
    WMSSOURCE_EPSG = "epsg"
    WMSSOURCE_ATTRIBUTION = "attribution"
    
    TMSSOURCE_LABEL = "label"
    TMSSOURCE_URLTEMPLATE = "urltemplate"
    TMSSOURCE_OPACITY = "opacity"
    TMSSOURCE_SUBDOMAINS = "subdomains"
    TMSSOURCE_MAXZOOM = "maxzoom"
    TMSSOURCE_ATTRIBUTION = "attribution"

    LASTUSER_TIMESTAMP = "ts" 
    LASTUSER_UPLOADTIMESTAMP = "uploadts" 

    USERCONFIG_KEY = "key"
    USERCONFIG_VALUE = "value"

    USERCONFIG_KEY_BASEMAP = "basemap";    
    USERCONFIG_KEY_MAPCENTER = "mapcenter_xyz";
    USERCONFIG_KEY_BOOKMARKS = "bookmarks";

    API_PARAM_PROJECT = "project"

class ProjectData(models.Model):
    label = models.CharField(name=DbNamings.PROJECTDATA_LABEL, max_length=100, null=False, unique=True)
    file = models.FileField(name = DbNamings.PROJECTDATA_FILE, null = False, upload_to ='projectdata/%Y/%m/%d/')
    
    def __str__(self):
        return f"{self.label} -> {self.file}"
    
    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.PROJECTDATA_LABEL]),
        ]

class WmsSource(models.Model):
    VERSION_CHOICES = (
        ("1.1.1", "1.1.1"),
        ("1.3.0", "1.3.0"),
    )
    FORMAT_CHOICES = (
        ("image/png","image/png"),
        ("image/jpg","image/jpg"),
    )
    EPSG_CHOICES = (
        (3857, "EPSG:3857"),
        (4326, "EPSG:4326"),
    )

    label = models.CharField(name=DbNamings.WMSSOURCE_LABEL, max_length=100, null=False, unique=True)
    version = models.CharField(name=DbNamings.WMSSOURCE_VERSION, max_length=10, null=False, choices=VERSION_CHOICES)
    transparent = models.BooleanField(name=DbNamings.WMSSOURCE_TRANSPARENT, null=False, default=True)
    imageFormat = models.CharField(name=DbNamings.WMSSOURCE_IMAGEFORMAT, max_length=10, null=False, choices=FORMAT_CHOICES)
    getcapabilitiesUrl = models.URLField(name=DbNamings.WMSSOURCE_GETCAPABILITIES, max_length=500, null=False)
    layerName = models.CharField(name=DbNamings.WMSSOURCE_LAYERNAME, max_length=100, null=False)
    attribution = models.CharField(name=DbNamings.WMSSOURCE_ATTRIBUTION, max_length=100, null=False)
    opacity = models.FloatField(name=DbNamings.WMSSOURCE_OPACITY, null=False, default=1.0, validators=[MinValueValidator(0.0), MaxValueValidator(1.0)])
    epsg = models.IntegerField(name=DbNamings.WMSSOURCE_EPSG, null=False, default=3857, choices=EPSG_CHOICES)

    def __str__(self):
        return f"{self.label} -> Layer: {self.layername}"

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.WMSSOURCE_LABEL]),
        ]

class TmsSource(models.Model):
    label = models.CharField(name=DbNamings.TMSSOURCE_LABEL, max_length=100, null=False, unique=True)
    urlTemplate = models.URLField(name=DbNamings.TMSSOURCE_URLTEMPLATE, max_length=500, null=False)
    opacity = models.FloatField(name=DbNamings.TMSSOURCE_OPACITY, null=False, default=1.0, validators=[MinValueValidator(0.0), MaxValueValidator(1.0)])
    subdomains = models.CharField(name=DbNamings.TMSSOURCE_SUBDOMAINS, max_length=100, null=True, blank=True)
    maxzoom = models.IntegerField(name=DbNamings.TMSSOURCE_MAXZOOM, null=True, default=19.0, validators=[MinValueValidator(1), MaxValueValidator(21)])
    attribution = models.CharField(name=DbNamings.TMSSOURCE_ATTRIBUTION, max_length=100, null=False)
    
    def __str__(self):
        return self.label

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.TMSSOURCE_LABEL]),
        ]




formNameValidator = RegexValidator(
    regex=r'^[a-zA-Z0-9_]+$',
    message='Only alphanumeric characters with no spaces are allowed.',
)

def validateFormContent(data):
    # Check if the the form is valid json
    try:
        # the first level needs to be a list
        if not isinstance(data, list):
            raise ValidationError('The form must be a list.')
        # the list has to contain only one value
        if len(data) != 1:
            raise ValidationError('The form must contain only one section element.')
        # the section element has to be a dict and contain the keys sectionname and forms
        if not isinstance(data[0], dict):
            raise ValidationError('The section element must be a dict.')
        if not "sectionname" in data[0]:
            raise ValidationError('The section element must contain the key sectionname.')
        if not "forms" in data[0]:
            raise ValidationError('The section element must contain the key forms.')
        # the forms element has to be a list
        if not isinstance(data[0]["forms"], list):
            raise ValidationError('The forms element must be a list.')
        # each forms element has to contain a formitems key
        for form in data[0]["forms"]:
            if not "formitems" in form:
                raise ValidationError('Each form element must contain the key formitems.')
            # the formitems element has to be a list
            if not isinstance(form["formitems"], list):
                raise ValidationError('The formitems element must be a list.')
            # each formitem has to contain a key key
            for formitem in form["formitems"]:
                if not "key" in formitem:
                    raise ValidationError('Each formitem element must contain a key element.')
                if not "value" in formitem:
                    raise ValidationError('Each formitem element must contain a value element.')
        
    except ValueError as e:
        raise ValidationError('The value must not contain spaces or special characters.')


class Form(models.Model):
    GEOMETRYTYPES = (
        ("Point", "Point"),
        ("LineString", "LineString"),
        ("Polygon", "Polygon"),
    )
        
    name = models.CharField(name=DbNamings.FORM_NAME, max_length=200, null=False, unique=True, validators=[formNameValidator])
    definition = models.JSONField(name=DbNamings.FORM_DEFINITION, null=False, default=list, validators=[validateFormContent])
    geometrytype = models.CharField(name=DbNamings.FORM_GEOMETRYTYPE, max_length=10, null=False, choices=GEOMETRYTYPES, default=GEOMETRYTYPES[0][0]) 
    add_userinfo = models.BooleanField(name="add_userinfo", null=False, default=True)
    add_timestamp = models.BooleanField(name="add_timestamp", null=False, default=True)
    enabled = models.BooleanField(name=DbNamings.FORM_ENABLED, null=False, default=True)

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.FORM_NAME]),
            models.Index(fields=[DbNamings.FORM_ENABLED]),
        ]

    def __str__(self):
        return f"{self.name}"


class Project(models.Model):
    name = models.CharField(name=DbNamings.PROJECT_NAME, max_length=200, null=False, unique=True)
    description = models.TextField(name=DbNamings.PROJECT_DESCRIPTION,  null=True, default="")
    groups = models.ManyToManyField(Group, name=DbNamings.PROJECT_GROUPS)

    projectdata = models.ManyToManyField(ProjectData, blank=True)
    forms = models.ManyToManyField(Form, blank=True)
    wmssources = models.ManyToManyField(WmsSource, blank=True)
    tmssources = models.ManyToManyField(TmsSource, blank=True)
    # TODO  configurations

    def hasUser(self, user):
        project = Project.objects.filter(name=self.name, groups__user__username=user.username).first()
        
        if project != None:
          return True
        return False

    def __str__(self):
        return self.name

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.PROJECT_NAME]),
        ]

class UserConfiguration(models.Model):
    key = models.CharField(name=DbNamings.USERCONFIG_KEY, max_length=200, null=False)
    value = models.TextField(name=DbNamings.USERCONFIG_VALUE, null=False)
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.USER, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.PROJECT, default=-1)

    def __str__(self):
        return f"{self.key}={self.value[:20]} ...       (user: {self.user.username}, project: {self.project.name})"

    class Meta:
        unique_together = (DbNamings.USERCONFIG_KEY, DbNamings.USER, DbNamings.PROJECT)
        indexes = [
            models.Index(fields=[DbNamings.USERCONFIG_KEY]),
            models.Index(fields=[DbNamings.USER]),
            models.Index(fields=[DbNamings.PROJECT]),
        ]

class Note(models.Model):
    geometry = geomodels.PointField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point())
    altim = models.FloatField(name=DbNamings.NOTE_ALTIM, null=False, default=-1)
    timestamp = models.DateTimeField(name=DbNamings.NOTE_TS, null=False, default=datetime.now)
    uploadTimestamp = models.DateTimeField(name=DbNamings.NOTE_UPLOADTS, null=False, default=datetime.now)
    description = models.TextField(name=DbNamings.NOTE_DESCRIPTION, null=False, default="")
    text = models.TextField(name=DbNamings.NOTE_TEXT, null=False, default="")
    marker = models.CharField(name=DbNamings.NOTE_MARKER, max_length=50, null=False, default="circle")
    size = models.FloatField(name=DbNamings.NOTE_SIZE, null=False,default=10)
    rotation = models.FloatField(name=DbNamings.NOTE_ROTATION, null=True, blank=True)
    color = models.CharField(max_length=9,name=DbNamings.NOTE_COLOR, null=False, default="#FF0000")
    accuracy = models.FloatField(name=DbNamings.NOTE_ACCURACY, null=False, default=0)
    heading = models.FloatField(name=DbNamings.NOTE_HEADING, null=False, default=0)
    speed = models.FloatField(name=DbNamings.NOTE_SPEED, null=False, default=0)
    speedaccuracy = models.FloatField(name=DbNamings.NOTE_SPEEDACCURACY, null=False, default=0)
    form = models.JSONField(name=DbNamings.NOTE_FORM, null=True, blank=True)

    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.USER, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.PROJECT, default=-1)
    previous = models.ForeignKey('self', on_delete=models.DO_NOTHING, name=DbNamings.NOTE_PREV, null=True, blank=True)

    def __str__(self):
        hasForm = ""
        parentNote = ""
        name = self.text
        if self.form != None:
            hasForm = " - has form"
            labelString = []
            Utilities.collectIsLabelValue(self.form, labelString)
            if labelString:
                name = labelString[0]
        if self.previous != None:
            parentNote = f" (parent = {self.previous.id})"
        return f"{name} - {str(self.ts)[:-6]}{parentNote}{hasForm}"

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.NOTE_PREV]),
            models.Index(fields=[DbNamings.NOTE_TS]),
            models.Index(fields=[DbNamings.NOTE_UPLOADTS]),
            models.Index(fields=[DbNamings.USER]),
            models.Index(fields=[DbNamings.PROJECT]),
        ]



class GpsLog(models.Model):
    name = models.CharField(name=DbNamings.GPSLOG_NAME, max_length=200, null=False)
    startTimestamp = models.DateTimeField(name=DbNamings.GPSLOG_STARTTS, null=False, default=datetime.now)
    endTimestamp = models.DateTimeField(name=DbNamings.GPSLOG_ENDTS, null=False, default=datetime.now)
    uploadTimestamp = models.DateTimeField(name=DbNamings.GPSLOG_UPLOADTIMESTAMP, null=False, default=datetime.now)
    geometry = geomodels.LineStringField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=LineString())
    width = models.FloatField(name=DbNamings.GPSLOG_WIDTH, null=False,default=3)
    color = models.CharField(max_length=9,name=DbNamings.GPSLOG_COLOR, null=False, default="#FF0000")

    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.USER, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.PROJECT, default=-1)

    def __str__(self):
        return self.name

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.GPSLOG_UPLOADTIMESTAMP]),
            models.Index(fields=[DbNamings.USER]),
            models.Index(fields=[DbNamings.PROJECT]),
        ]

class GpsLogData(models.Model):
    geometry = geomodels.PointField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point(), dim=3)
    timestamp = models.DateTimeField(name=DbNamings.GPSLOGDATA_TIMESTAMP, null=False, default=datetime.now)

    gpsLog = models.ForeignKey(GpsLog, on_delete=models.CASCADE, null=False, name=DbNamings.GPSLOGDATA_GPSLOGS, default=-1)

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.GPSLOGDATA_TIMESTAMP]),
            models.Index(fields=[DbNamings.GPSLOGDATA_GPSLOGS]),
        ]

class ImageData(models.Model):
    data = models.BinaryField(name=DbNamings.IMAGEDATA_DATA, null=True, blank=True)

    def data_thumb(self):
        return mark_safe('<img src = "data: image/png; base64, {}" width="300">'.format(
            b64encode(self.data).decode('utf8')
        ))
    data_thumb.short_description = 'Thumbnail'
    data_thumb.allow_tags = True
    
    def data_image(self):
        return mark_safe('<img src = "data: image/png; base64, {}" width="800">'.format(
            b64encode(self.data).decode('utf8')
        ))
    data_image.short_description = 'Image'
    data_image.allow_tags = True

    def getAsBase64(self):
        return b64encode(self.data).decode('utf8')

    def __str__(self):
        return str(self.id)
    

class Image(models.Model):
    geometry = geomodels.PointField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point())
    altimetry = models.FloatField(name=DbNamings.IMAGE_ALTIM, null=False, default=-1)
    timestamp = models.DateTimeField(name=DbNamings.IMAGE_TIMESTAMP, null=False, default=datetime.now)
    uploadTimestamp = models.DateTimeField(name=DbNamings.IMAGE_UPLOADTIMESTAMP, null=False, default=datetime.now)
    azimuth = models.FloatField(name=DbNamings.IMAGE_AZIMUTH, null=False,default=0)
    text = models.TextField(name=DbNamings.IMAGE_TEXT, null=False, default="")
    thumbnail = models.BinaryField(name=DbNamings.IMAGE_THUMB, null=True, blank=True)

    note = models.ForeignKey(Note, on_delete=models.CASCADE, null=True, blank=True, name=DbNamings.IMAGE_NOTE, default=-1)
    imageData = models.ForeignKey(ImageData, on_delete=models.CASCADE, null=False, name=DbNamings.IMAGE_IMAGEDATA, default=-1)
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.USER, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.PROJECT, default=-1)

    def __str__(self):
        ownedByForm = ""
        if self.notes != None:
            ownedByForm = f" - owned by note {self.notes.id}"
        return f"{self.id}: {self.text}{ownedByForm}"

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.IMAGE_TIMESTAMP]),
            models.Index(fields=[DbNamings.IMAGE_UPLOADTIMESTAMP]),
            models.Index(fields=[DbNamings.IMAGE_NOTE]),
            models.Index(fields=[DbNamings.IMAGE_IMAGEDATA]),
            models.Index(fields=[DbNamings.USER]),
            models.Index(fields=[DbNamings.PROJECT]),
        ]

class Device(models.Model):
    uniqueId = models.CharField(name=DbNamings.DEVICE_UNIQUE_ID, max_length=100, null=False, unique=True)
    name = models.CharField(name=DbNamings.DEVICE_NAME, max_length=100, null=False)
    isActive = models.BooleanField(name=DbNamings.DEVICE_ACTIVE, null=False)

    def __str__(self):
        return f"{self.name} ({self.uniqueid})"

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.DEVICE_NAME]),
            models.Index(fields=[DbNamings.DEVICE_UNIQUE_ID]),
        ]

class UserDeviceAssociation(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.USER, default=-1)
    device = models.ForeignKey(Device, on_delete=models.CASCADE, null=False, name=DbNamings.U_D_ASS_DEVICEID, default=-1)
    fromDate = models.DateTimeField(name=DbNamings.U_D_ASS_FROMDATE, null=False, default=datetime.now)
    toDate = models.DateTimeField(name=DbNamings.U_D_ASS_TODATE, null=True, default=datetime.now)

    def __str__(self):
        return f"User: {self.user} <-> Device: {self.device}"

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.USER]),
            models.Index(fields=[DbNamings.U_D_ASS_DEVICEID]),
        ]

class LastUserPosition(models.Model):
    geometry = geomodels.PointField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point())
    timestamp = models.DateTimeField(name=DbNamings.LASTUSER_TIMESTAMP, null=False, default=datetime.now)
    uploadTimestamp = models.DateTimeField(name=DbNamings.LASTUSER_UPLOADTIMESTAMP, null=False, default=datetime.now)
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.USER, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.PROJECT, default=-1)

    class Meta:
        unique_together = (DbNamings.USER, DbNamings.PROJECT)
        indexes = [
            models.Index(fields=[DbNamings.LASTUSER_TIMESTAMP]),
            models.Index(fields=[DbNamings.LASTUSER_UPLOADTIMESTAMP]),
            models.Index(fields=[DbNamings.USER]),
            models.Index(fields=[DbNamings.PROJECT]),
        ]





