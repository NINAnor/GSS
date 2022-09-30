# Generated by Django 4.1.1 on 2022-09-30 14:34

import datetime
from django.conf import settings
import django.contrib.gis.db.models.fields
import django.contrib.gis.geos.linestring
import django.contrib.gis.geos.point
import django.core.validators
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('auth', '0012_alter_user_first_name_max_length'),
        ('data', '0001_initial'),
    ]

    operations = [
        migrations.CreateModel(
            name='Device',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('uniqueid', models.CharField(max_length=100, unique=True)),
                ('name', models.CharField(max_length=100)),
                ('active', models.BooleanField()),
            ],
        ),
        migrations.CreateModel(
            name='GpsLog',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=200)),
                ('startts', models.DateTimeField(default=datetime.datetime.now)),
                ('endts', models.DateTimeField(default=datetime.datetime.now)),
                ('uploadts', models.DateTimeField(default=datetime.datetime.now)),
                ('the_geom', django.contrib.gis.db.models.fields.LineStringField(default=django.contrib.gis.geos.linestring.LineString(), srid=4326)),
                ('width', models.FloatField(default=3)),
                ('color', models.CharField(default='#FF0000', max_length=9)),
            ],
        ),
        migrations.CreateModel(
            name='ImageData',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('data', models.BinaryField(default=())),
            ],
        ),
        migrations.CreateModel(
            name='ProjectData',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('label', models.CharField(max_length=100, unique=True)),
                ('file', models.FileField(upload_to='projectdata/%Y/%m/%d/')),
            ],
        ),
        migrations.CreateModel(
            name='TmsSource',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('label', models.CharField(max_length=100, unique=True)),
                ('urltemplate', models.URLField(max_length=500)),
                ('opacity', models.FloatField(default=1.0, validators=[django.core.validators.MinValueValidator(0.0), django.core.validators.MaxValueValidator(1.0)])),
                ('subdomains', models.CharField(max_length=100, null=True)),
                ('maxzoom', models.IntegerField(default=19.0, null=True, validators=[django.core.validators.MinValueValidator(1), django.core.validators.MaxValueValidator(21)])),
                ('attribution', models.CharField(max_length=100)),
            ],
        ),
        migrations.CreateModel(
            name='WmsSource',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('label', models.CharField(max_length=100, unique=True)),
                ('version', models.CharField(choices=[('1.1.1', '1.1.1'), ('1.3.0', '1.3.0')], max_length=10)),
                ('transparent', models.BooleanField(default=True)),
                ('imageformat', models.CharField(choices=[('image/png', 'image/png'), ('image/jpg', 'image/jpg')], max_length=10)),
                ('getcapabilities', models.URLField(max_length=500)),
                ('layername', models.CharField(max_length=100)),
                ('attribution', models.CharField(max_length=100)),
                ('opacity', models.FloatField(default=1.0, validators=[django.core.validators.MinValueValidator(0.0), django.core.validators.MaxValueValidator(1.0)])),
            ],
        ),
        migrations.CreateModel(
            name='UserDeviceAssociation',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('fromdate', models.DateTimeField(default=datetime.datetime.now)),
                ('todate', models.DateTimeField(default=datetime.datetime.now, null=True)),
                ('device', models.ForeignKey(default=-1, on_delete=django.db.models.deletion.CASCADE, to='data.device')),
                ('user', models.ForeignKey(default=-1, on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.CreateModel(
            name='Project',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=200, unique=True)),
                ('description', models.TextField(default='', null=True)),
                ('groups', models.ManyToManyField(to='auth.group')),
                ('projectdata', models.ManyToManyField(blank=True, to='data.projectdata')),
                ('tmssources', models.ManyToManyField(blank=True, to='data.tmssource')),
                ('wmssources', models.ManyToManyField(blank=True, to='data.wmssource')),
            ],
        ),
        migrations.CreateModel(
            name='Note',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('previd', models.IntegerField(blank=True, null=True)),
                ('the_geom', django.contrib.gis.db.models.fields.PointField(default=django.contrib.gis.geos.point.Point(), srid=4326)),
                ('altim', models.FloatField(default=-1)),
                ('ts', models.DateTimeField(default=datetime.datetime.now)),
                ('uploadts', models.DateTimeField(default=datetime.datetime.now)),
                ('description', models.TextField(default='')),
                ('text', models.TextField(default='')),
                ('marker', models.CharField(default='circle', max_length=50)),
                ('size', models.FloatField(default=10)),
                ('rotation', models.FloatField(blank=True, null=True)),
                ('color', models.CharField(default='#FF0000', max_length=9)),
                ('accuracy', models.FloatField(default=0)),
                ('heading', models.FloatField(default=0)),
                ('speed', models.FloatField(default=0)),
                ('speedaccuracy', models.FloatField(default=0)),
                ('form', models.JSONField(blank=True, null=True)),
                ('project', models.ForeignKey(default=-1, on_delete=django.db.models.deletion.CASCADE, to='data.project')),
                ('user', models.ForeignKey(default=-1, on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.CreateModel(
            name='Image',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('the_geom', django.contrib.gis.db.models.fields.PointField(default=django.contrib.gis.geos.point.Point(), srid=4326)),
                ('altim', models.FloatField(default=-1)),
                ('ts', models.DateTimeField(default=datetime.datetime.now)),
                ('uploadts', models.DateTimeField(default=datetime.datetime.now)),
                ('azimuth', models.FloatField(default=0)),
                ('text', models.TextField(default='')),
                ('thumbnail', models.BinaryField(default=())),
                ('imagedata', models.ForeignKey(default=-1, on_delete=django.db.models.deletion.CASCADE, to='data.imagedata')),
                ('notes', models.ForeignKey(blank=True, default=-1, null=True, on_delete=django.db.models.deletion.CASCADE, to='data.note')),
                ('project', models.ForeignKey(default=-1, on_delete=django.db.models.deletion.CASCADE, to='data.project')),
                ('user', models.ForeignKey(default=-1, on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.CreateModel(
            name='GpsLogData',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('the_geom', django.contrib.gis.db.models.fields.PointField(default=django.contrib.gis.geos.point.Point(), dim=3, srid=4326)),
                ('ts', models.DateTimeField(default=datetime.datetime.now)),
                ('gpslogid', models.ForeignKey(default=-1, on_delete=django.db.models.deletion.CASCADE, to='data.gpslog')),
            ],
        ),
        migrations.AddField(
            model_name='gpslog',
            name='project',
            field=models.ForeignKey(default=-1, on_delete=django.db.models.deletion.CASCADE, to='data.project'),
        ),
        migrations.AddField(
            model_name='gpslog',
            name='user',
            field=models.ForeignKey(default=-1, on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL),
        ),
        migrations.AddIndex(
            model_name='userdeviceassociation',
            index=models.Index(fields=['user'], name='data_userde_user_id_aea26b_idx'),
        ),
        migrations.AddIndex(
            model_name='userdeviceassociation',
            index=models.Index(fields=['device'], name='data_userde_device__71d075_idx'),
        ),
        migrations.AddIndex(
            model_name='note',
            index=models.Index(fields=['previd'], name='data_note_previd_61c531_idx'),
        ),
        migrations.AddIndex(
            model_name='note',
            index=models.Index(fields=['ts'], name='data_note_ts_392f20_idx'),
        ),
        migrations.AddIndex(
            model_name='note',
            index=models.Index(fields=['uploadts'], name='data_note_uploadt_434419_idx'),
        ),
        migrations.AddIndex(
            model_name='note',
            index=models.Index(fields=['user'], name='data_note_user_id_435937_idx'),
        ),
        migrations.AddIndex(
            model_name='note',
            index=models.Index(fields=['project'], name='data_note_project_e7699c_idx'),
        ),
        migrations.AddIndex(
            model_name='image',
            index=models.Index(fields=['ts'], name='data_image_ts_903f88_idx'),
        ),
        migrations.AddIndex(
            model_name='image',
            index=models.Index(fields=['uploadts'], name='data_image_uploadt_201137_idx'),
        ),
        migrations.AddIndex(
            model_name='image',
            index=models.Index(fields=['notes'], name='data_image_notes_i_d89959_idx'),
        ),
        migrations.AddIndex(
            model_name='image',
            index=models.Index(fields=['imagedata'], name='data_image_imageda_857106_idx'),
        ),
        migrations.AddIndex(
            model_name='image',
            index=models.Index(fields=['user'], name='data_image_user_id_72d34f_idx'),
        ),
        migrations.AddIndex(
            model_name='image',
            index=models.Index(fields=['project'], name='data_image_project_864474_idx'),
        ),
        migrations.AddIndex(
            model_name='gpslogdata',
            index=models.Index(fields=['ts'], name='data_gpslog_ts_9a8770_idx'),
        ),
        migrations.AddIndex(
            model_name='gpslogdata',
            index=models.Index(fields=['gpslogid'], name='data_gpslog_gpslogi_b5e83e_idx'),
        ),
        migrations.AddIndex(
            model_name='gpslog',
            index=models.Index(fields=['uploadts'], name='data_gpslog_uploadt_69e6c8_idx'),
        ),
        migrations.AddIndex(
            model_name='gpslog',
            index=models.Index(fields=['user'], name='data_gpslog_user_id_9e0534_idx'),
        ),
        migrations.AddIndex(
            model_name='gpslog',
            index=models.Index(fields=['project'], name='data_gpslog_project_5ef398_idx'),
        ),
    ]
