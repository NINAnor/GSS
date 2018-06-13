package com.hydrologis.gss.server.database.objects;
import com.hydrologis.gss.server.database.ISpatialTable;
import com.hydrologis.gss.server.database.ormlite.PointTypeH2GIS;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vividsolutions.jts.geom.Point;

/**
 * The gps log data table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "gpslogsdata")
public class GpsLogsData implements ISpatialTable {
    public static final String ID_FIELD_NAME = "id";
    public static final String ALTIM_FIELD_NAME = "altim";
    public static final String TIMESTAMP_FIELD_NAME = "ts";
    public static final String GPSLOGS_FIELD_NAME = "gpslogsid";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = GEOM_FIELD_NAME, canBeNull = false, persisterClass = PointTypeH2GIS.class)
    public Point the_geom;

    @DatabaseField(columnName = ALTIM_FIELD_NAME, canBeNull = false)
    public double altimetry;

    @DatabaseField(columnName = TIMESTAMP_FIELD_NAME, canBeNull = false, index = true)
    public long timestamp;

    @DatabaseField(columnName = GPSLOGS_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpsLogs.gpslogFKColumnDefinition)
    public GpsLogs gpsLog;

    GpsLogsData() {
    }

    public GpsLogsData( long id ) {
        this.id = id;
    }

    public GpsLogsData( Point the_geom, double altimetry, long timestamp, GpsLogs gpsLog ) {
        this.the_geom = the_geom;
        this.altimetry = altimetry;
        this.timestamp = timestamp;
        this.gpsLog = gpsLog;
    }

}