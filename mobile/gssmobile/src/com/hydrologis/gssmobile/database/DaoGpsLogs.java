/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.database;

import com.codename1.db.Cursor;
import com.codename1.db.Database;
import com.codename1.db.Row;
import com.codename1.io.Util;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hydrologis
 */
public class DaoGpsLogs {

    /**
     * gpslog table name.
     */
    public static final String TABLE_GPSLOGS = "gpslogs";
    /**
     * gpslog data table name.
     */
    public static final String TABLE_GPSLOG_DATA = "gpslogsdata";
    /**
     * gpslog properties table name.
     */
    public static final String TABLE_GPSLOG_PROPERTIES = "gpslogsproperties";

    public static List<GssGpsLog> getLogsList(Database db, boolean withLogData) throws Exception {
        List<GssGpsLog> logsList = new ArrayList<>();
        String query = "select "
                + //
                GpsLogsTableFields.COLUMN_ID.getFieldName() + ","
                + //
                GpsLogsTableFields.COLUMN_LOG_STARTTS.getFieldName() + ","
                + //
                GpsLogsTableFields.COLUMN_LOG_ENDTS.getFieldName() + ","
                + //
                GpsLogsTableFields.COLUMN_LOG_TEXT.getFieldName() 
                + //
                " from " + TABLE_GPSLOGS + " where " + GpsLogsTableFields.COLUMN_LOG_ISDIRTY.getFieldName() + "=" + 1 + " order by " + GpsLogsTableFields.COLUMN_LOG_STARTTS.getFieldName();

        Cursor cursorLog = null;
        try {
            cursorLog = db.executeQuery(query);
            while (cursorLog.next()) {
                Row row = cursorLog.getRow();
                int i = 0;
                GssGpsLog log = new GssGpsLog();
                log.id = row.getLong(i++);
                log.startts = row.getLong(i++);
                log.endts = row.getLong(i++);
                log.name = row.getString(i++);
                logsList.add(log);

                if (withLogData) {
                    String queryData = "select "
                            + //
                            GpsLogsDataTableFields.COLUMN_DATA_LAT.getFieldName() + ","
                            + //
                            GpsLogsDataTableFields.COLUMN_DATA_LON.getFieldName() + ","
                            + //
                            GpsLogsDataTableFields.COLUMN_DATA_ALTIM.getFieldName() + ","
                            + //
                            GpsLogsDataTableFields.COLUMN_DATA_TS.getFieldName()
                            + //
                            " from " + TABLE_GPSLOG_DATA + " where "
                            + //
                            GpsLogsDataTableFields.COLUMN_LOGID.getFieldName() + " = " + log.id + " order by "
                            + GpsLogsDataTableFields.COLUMN_DATA_TS.getFieldName();

                    Cursor cursorLogData = null;
                    try {
                        cursorLogData = db.executeQuery(queryData);
                        while (cursorLogData.next()) {
                            Row dataRow = cursorLogData.getRow();
                            int j = 0;

                            double lat = dataRow.getDouble(j++);
                            double lon = dataRow.getDouble(j++);
                            double altim = dataRow.getDouble(j++);
                            long ts = dataRow.getLong(j++);

                            GssGpsLogPoint gPoint = new GssGpsLogPoint();
                            gPoint.longitude = lon;
                            gPoint.latitude = lat;
                            gPoint.altimetry = altim;
                            gPoint.ts = ts;
                            log.points.add(gPoint);
                        }
                    } finally {
                        Util.cleanup(cursorLogData);
                    }

                    String queryProps = "select "
                            + //
                            GpsLogsPropertiesTableFields.COLUMN_PROPERTIES_COLOR.getFieldName() + ","
                            + //
                            GpsLogsPropertiesTableFields.COLUMN_PROPERTIES_WIDTH.getFieldName() + " from "
                            + //
                            TABLE_GPSLOG_PROPERTIES + " where " + GpsLogsPropertiesTableFields.COLUMN_LOGID.getFieldName() + "=" + log.id;

                    Cursor cursorLogProps = null;
                    try {
                        cursorLogProps = db.executeQuery(queryProps);
                        while (cursorLogProps.next()) {
                            Row propsRow = cursorLogProps.getRow();
                            int j = 0;

                            String color = propsRow.getString(j++);
                            float width = propsRow.getFloat(j++);
                            log.color = color;
                            log.width = width;
                        }
                    } finally {
                        Util.cleanup(cursorLogProps);
                    }

                }
            }
        } finally {
            Util.cleanup(cursorLog);
        }

        return logsList;
    }

    public static enum GpsLogsTableFields {
        /**
         * id of the log, Generated by the db.
         */
        COLUMN_ID("_id", Long.class),
        /**
         * the start UTC timestamp.
         */
        COLUMN_LOG_STARTTS("startts", Long.class),
        /**
         * the end UTC timestamp.
         */
        COLUMN_LOG_ENDTS("endts", Long.class),
        /**
         * The length of the track in meters, as last updated.
         */
        COLUMN_LOG_LENGTHM("lengthm", Double.class),
        /**
         * Is dirty field (0=false, 1=true)
         */
        COLUMN_LOG_ISDIRTY("isdirty", Integer.class),
        /**
         * the name of the log.
         */
        COLUMN_LOG_TEXT("text", String.class);

        private String fieldName;
        private Class fieldClass;

        GpsLogsTableFields(String fieldName, Class fieldClass) {
            this.fieldName = fieldName;
            this.fieldClass = fieldClass;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Class getFieldClass() {
            return fieldClass;
        }
    }

    public static enum GpsLogsDataTableFields {
        /**
         * id of the log, Generated by the db.
         */
        COLUMN_ID("_id", Long.class),
        /**
         * the longitude of the point.
         */
        COLUMN_DATA_LON("lon", Double.class),
        /**
         * the latitude of the point.
         */
        COLUMN_DATA_LAT("lat", Double.class),
        /**
         * the elevation of the point.
         */
        COLUMN_DATA_ALTIM("altim", Double.class),
        /**
         * the UTC timestamp
         */
        COLUMN_DATA_TS("ts", Long.class),
        /**
         * the id of the parent gps log.
         */
        COLUMN_LOGID("logid", Long.class);

        private String fieldName;
        private Class fieldClass;

        GpsLogsDataTableFields(String fieldName, Class fieldClass) {
            this.fieldName = fieldName;
            this.fieldClass = fieldClass;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Class getFieldClass() {
            return fieldClass;
        }
    }

    public static enum GpsLogsPropertiesTableFields {
        /**
         * id of the log, Generated by the db.
         */
        COLUMN_ID("_id", Long.class),
        /**
         * field for log visibility.
         */
        COLUMN_PROPERTIES_VISIBLE("visible", Integer.class),
        /**
         * the lgo stroke width.
         */
        COLUMN_PROPERTIES_WIDTH("width", Float.class),
        /**
         * the log stroke color.
         */
        COLUMN_PROPERTIES_COLOR("color", String.class),
        /**
         * the id of the parent gps log.
         */
        COLUMN_LOGID("logid", Long.class);

        private String fieldName;
        private Class fieldClass;

        GpsLogsPropertiesTableFields(String fieldName, Class fieldClass) {
            this.fieldName = fieldName;
            this.fieldClass = fieldClass;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Class getFieldClass() {
            return fieldClass;
        }
    }

}
