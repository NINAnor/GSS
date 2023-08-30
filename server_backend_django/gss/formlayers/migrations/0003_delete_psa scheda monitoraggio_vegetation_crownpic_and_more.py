# Generated by Django 4.1.1 on 2023-08-30 10:33

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('formlayers', '0002_psa scheda monitoraggio'),
    ]

    operations = [
        migrations.DeleteModel(
            name='PSA Scheda Monitoraggio',
        ),
        migrations.AddField(
            model_name='vegetation',
            name='crownpic',
            field=models.BinaryField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='vegetation',
            name='flowerpic',
            field=models.BinaryField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='vegetation',
            name='fruitpic',
            field=models.BinaryField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='vegetation',
            name='hasflowers',
            field=models.BooleanField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='vegetation',
            name='hasfruit',
            field=models.BooleanField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='vegetation',
            name='rootpic',
            field=models.BinaryField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='vegetation',
            name='trunkpic',
            field=models.BinaryField(blank=True, null=True),
        ),
    ]
