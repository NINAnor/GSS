/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.server.database.objects;
import java.util.Collections;
import java.util.List;

import com.hydrologis.gss.server.GssDbProvider;
import com.hydrologis.kukuratus.libs.database.ISpatialTable;
import com.hydrologis.kukuratus.libs.database.ormlite.LineStringTypeH2GIS;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.utils.export.KmlRepresenter;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * The gps log table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "gpslogs")
public class GpsLogs implements ISpatialTable, KmlRepresenter {
    private static final long serialVersionUID = 1L;
    public static final String ID_FIELD_NAME = "id";
    public static final String NAME_FIELD_NAME = "name";
    public static final String STARTTS_FIELD_NAME = "startts";
    public static final String ENDTS_FIELD_NAME = "endts";
    public static final String GPAPUSER_FIELD_NAME = "gpapusersid";

    public static final String gpslogFKColumnDefinition = "long references gpslogs(id) on delete cascade";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
    public String name;

    @DatabaseField(columnName = STARTTS_FIELD_NAME, canBeNull = false)
    public long startTs;

    @DatabaseField(columnName = ENDTS_FIELD_NAME, canBeNull = false)
    public long endTs;

    @DatabaseField(columnName = GEOM_FIELD_NAME, canBeNull = false, persisterClass = LineStringTypeH2GIS.class)
    public LineString the_geom;

    @DatabaseField(columnName = GPAPUSER_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapUsers.usersFKColumnDefinition)
    public GpapUsers gpapUser;

    GpsLogs() {
    }

    public GpsLogs( long id ) {
        this.id = id;
    }

    public GpsLogs( String name, long startTs, long endTs, LineString the_geom, GpapUsers gpapUser ) {
        this.name = name;
        this.startTs = startTs;
        this.endTs = endTs;
        this.the_geom = the_geom;
        this.gpapUser = gpapUser;
        the_geom.setSRID(ISpatialTable.SRID);
    }

    public String toKmlString() {
        String hexColor = "#FF0000";
        float width = 3;
        try {
            Dao<GpsLogsProperties, ? > logPropDAO = GssDbProvider.INSTANCE.getDatabaseHandler().get()
                    .getDao(GpsLogsProperties.class);

            GpsLogsProperties props = logPropDAO.queryBuilder().where().eq(GpsLogsProperties.GPSLOGS_FIELD_NAME, this)
                    .queryForFirst();
            hexColor = props.color;
            if (!hexColor.startsWith("#")) {
                hexColor = "#FF0000";
            }
            width = props.width;
        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }

        String name = makeXmlSafe(this.name);
        StringBuilder sB = new StringBuilder();
        sB.append("<Placemark>\n");
        sB.append("<name>" + name + "</name>\n");
        sB.append("<visibility>1</visibility>\n");
        sB.append("<LineString>\n");
        sB.append("<tessellate>1</tessellate>\n");
        sB.append("<coordinates>\n");
        Coordinate[] coords = the_geom.getCoordinates();
        for( int i = 0; i < coords.length; i++ ) {
            double lon = coords[i].x;
            double lat = coords[i].y;
            sB.append(lon).append(",").append(lat).append(",1 \n");
        }
        sB.append("</coordinates>\n");
        sB.append("</LineString>\n");
        sB.append("<Style>\n");
        sB.append("<LineStyle>\n");

        String aabbggrr = "#FF" + new StringBuilder(hexColor.substring(1)).reverse().toString();
        sB.append("<color>").append(aabbggrr).append("</color>\n");
        sB.append("<width>").append(width).append("</width>\n");
        sB.append("</LineStyle>\n");
        sB.append("</Style>\n");
        sB.append("</Placemark>\n");

        return sB.toString();
    }

    public boolean hasImages() {
        return false;
    }

    @Override
    public List<String> getImageIds() {
        return Collections.emptyList();
    }
}
