/*******************************************************************************
 * Copyright (C) 2018 HydroloGIS S.r.l. (www.hydrologis.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Antonello Andrea (http://www.hydrologis.com)
 ******************************************************************************/
package com.hydrologis.kukuratus.gss.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hortonmachine.gears.io.geopaparazzi.forms.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Point;

import com.hydrologis.kukuratus.database.DatabaseHandler;
import com.hydrologis.kukuratus.database.ISpatialTable;
import com.hydrologis.kukuratus.database.ormlite.KukuratusPointType;
import com.hydrologis.kukuratus.utils.KukuratusLogger;
import com.hydrologis.kukuratus.utils.export.KmlRepresenter;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The notes class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "notes")
public class Notes implements ISpatialTable, KmlRepresenter {
    private static final long serialVersionUID = 1L;
    public static final String ID_FIELD_NAME = "id"; //$NON-NLS-1$

    public static final String PREVIOUSID_FIELD_NAME = "previd"; //$NON-NLS-1$
    public static final String ORIGINALID_FIELD_NAME = "originalid"; //$NON-NLS-1$
    public static final String ALTIM_FIELD_NAME = "altim"; //$NON-NLS-1$
    public static final String TIMESTAMP_FIELD_NAME = "ts"; //$NON-NLS-1$

    public static final String UPLOADTIMESTAMP_FIELD_NAME = "uploadts"; //$NON-NLS-1$
    public static final String DESCRIPTION_FIELD_NAME = "description"; //$NON-NLS-1$
    public static final String TEXT_FIELD_NAME = "text"; //$NON-NLS-1$
    public static final String FORM_FIELD_NAME = "form"; //$NON-NLS-1$
    public static final String STYLE_FIELD_NAME = "style"; //$NON-NLS-1$
    public static final String GPAPUSER_FIELD_NAME = "gpapusersid"; //$NON-NLS-1$

    public static final String GPAPPROJECT_FIELD_NAME = "gpapprojectid"; //$NON-NLS-1$

    public static final String notesFKColumnDefinition = "bigint references notes(id) on delete cascade"; //$NON-NLS-1$

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = ORIGINALID_FIELD_NAME, canBeNull = false, index = true)
    public long originalId;

    @DatabaseField(columnName = PREVIOUSID_FIELD_NAME, canBeNull = true, index = true)
    public long previousId;

    @DatabaseField(columnName = GEOM_FIELD_NAME, canBeNull = false, persisterClass = KukuratusPointType.class)
    public Point the_geom;

    @DatabaseField(columnName = ALTIM_FIELD_NAME, canBeNull = false)
    public double altimetry;

    @DatabaseField(columnName = TIMESTAMP_FIELD_NAME, canBeNull = false, index = true)
    public long timestamp;

    @DatabaseField(columnName = UPLOADTIMESTAMP_FIELD_NAME, canBeNull = false, index = true)
    public long uploadTimestamp;

    @DatabaseField(columnName = DESCRIPTION_FIELD_NAME, canBeNull = false)
    public String description;

    @DatabaseField(columnName = TEXT_FIELD_NAME, canBeNull = false)
    public String text;

    @DatabaseField(columnName = FORM_FIELD_NAME, canBeNull = true, dataType = DataType.LONG_STRING)
    public String form;

    @DatabaseField(columnName = STYLE_FIELD_NAME, canBeNull = true)
    public String style;

    @DatabaseField(columnName = GPAPUSER_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapUsers.usersFKColumnDefinition)
    public GpapUsers gpapUser;

    @DatabaseField(columnName = GPAPPROJECT_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapProject.projectsFKColumnDefinition)
    public GpapProject gpapProject;

    private List<String> images;

    Notes() {
    }

    public Notes(long id) {
        this.id = id;
    }

    public Notes(Point the_geom, long originalId, double altimetry, long timestamp, String description, String text,
            String form, String style, GpapUsers gpapUser, GpapProject gpapProject, long previousId,
            long uploadTimestamp) {
        super();
        this.the_geom = the_geom;
        this.originalId = originalId;
        this.altimetry = altimetry;
        this.timestamp = timestamp;
        this.description = description;
        this.text = text;
        this.form = form;
        this.style = style;
        this.gpapUser = gpapUser;
        this.gpapProject = gpapProject;
        this.previousId = previousId;
        this.uploadTimestamp = uploadTimestamp;
        the_geom.setSRID(ISpatialTable.SRID);
    }

    public Notes(Point the_geom, long originalId, double altimetry, Date timestamp, String description, String text,
            String form, String style, GpapUsers gpapUser, GpapProject gpapProject, long previousId,
            long uploadTimestamp) {
        super();
        this.the_geom = the_geom;
        this.originalId = originalId;
        this.altimetry = altimetry;
        this.gpapProject = gpapProject;
        this.timestamp = timestamp.getTime();
        this.description = description;
        this.text = text;
        this.form = form;
        this.style = style;
        this.gpapUser = gpapUser;
        this.previousId = previousId;
        this.uploadTimestamp = uploadTimestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOriginalId() {
        return originalId;
    }

    public void setOriginalId(long originalId) {
        this.originalId = originalId;
    }

    public Long getPreviousId() {
        return previousId;
    }

    public void setPreviousId(Long previousId) {
        this.previousId = previousId;
    }

    public Point getThe_geom() {
        return the_geom;
    }

    public void setThe_geom(Point the_geom) {
        this.the_geom = the_geom;
    }

    public double getAltimetry() {
        return altimetry;
    }

    public void setAltimetry(double altimetry) {
        this.altimetry = altimetry;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(long uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public GpapUsers getGpapUser() {
        return gpapUser;
    }

    public void setGpapUser(GpapUsers gpapUser) {
        this.gpapUser = gpapUser;
    }

    public GpapProject getGpapProject() {
        return gpapProject;
    }

    public void setGpapProject(GpapProject gpapProject) {
        this.gpapProject = gpapProject;
    }

    public String toKmlString() throws Exception {
        DatabaseHandler dbHandler = DatabaseHandler.instance();
        Dao<Images, ?> imagesDAO = dbHandler.getDao(Images.class);

        images = new ArrayList<>();
        String name = makeXmlSafe(text);
        StringBuilder sB = new StringBuilder();
        sB.append("<Placemark>\n"); //$NON-NLS-1$
        // sB.append("<styleUrl>#red-pushpin</styleUrl>\n");
        sB.append("<styleUrl>#info-icon</styleUrl>\n"); //$NON-NLS-1$
        sB.append("<name>").append(name).append("</name>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sB.append("<description>\n"); //$NON-NLS-1$

        if (form != null && form.length() > 0) {

            sB.append("<![CDATA[\n"); //$NON-NLS-1$
            JSONObject sectionObject = new JSONObject(form);
            if (sectionObject.has(Utilities.ATTR_SECTIONNAME)) {
                String sectionName = sectionObject.getString(Utilities.ATTR_SECTIONNAME);
                sB.append("<h1>").append(sectionName).append("</h1>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            List<String> formsNames = Utilities.getFormNames4Section(sectionObject);
            for (String formName : formsNames) {
                sB.append("<h2>").append(formName).append("</h2>\n"); //$NON-NLS-1$ //$NON-NLS-2$

                sB.append(
                        "<table style=\"text-align: left; width: 100%;\" border=\"1\" cellpadding=\"5\" cellspacing=\"2\">"); //$NON-NLS-1$
                sB.append("<tbody>"); //$NON-NLS-1$

                JSONObject form4Name = Utilities.getForm4Name(formName, sectionObject);
                JSONArray formItems = Utilities.getFormItems(form4Name);
                for (int i = 0; i < formItems.length(); i++) {
                    JSONObject formItem = formItems.getJSONObject(i);
                    if (!formItem.has(Utilities.TAG_KEY)) {
                        continue;
                    }

                    String type = formItem.getString(Utilities.TAG_TYPE);
                    String key = formItem.getString(Utilities.TAG_KEY);
                    String value = formItem.getString(Utilities.TAG_VALUE);

                    String label = key;
                    if (formItem.has(Utilities.TAG_LABEL)) {
                        label = formItem.getString(Utilities.TAG_LABEL);
                    }

                    if (type.equals(Utilities.TYPE_PICTURES)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        String[] imageIdsSplit = value.split(Utilities.IMAGES_SEPARATOR);
                        for (String imageId : imageIdsSplit) {

                            Images image = imagesDAO.queryForSameId(new Images(Long.parseLong(imageId)));
                            String imgName = image.text;
                            sB.append("<tr>"); //$NON-NLS-1$
                            sB.append(
                                    "<td colspan=\"2\" style=\"text-align: left; vertical-align: top; width: 100%;\">"); //$NON-NLS-1$
                            sB.append("<img src=\"").append(imgName).append("\" width=\"300\">"); //$NON-NLS-1$ //$NON-NLS-2$
                            sB.append("</td>"); //$NON-NLS-1$
                            sB.append("</tr>"); //$NON-NLS-1$

                            images.add(imageId);
                        }
                    } else if (type.equals(Utilities.TYPE_MAP)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        sB.append("<tr>"); //$NON-NLS-1$
                        // FIXME
                        String imageId = value.trim();
                        Images image = imagesDAO.queryForSameId(new Images(Long.parseLong(imageId)));
                        String imgName = image.text;
                        sB.append("<td colspan=\"2\" style=\"text-align: left; vertical-align: top; width: 100%;\">"); //$NON-NLS-1$
                        sB.append("<img src=\"").append(imgName).append("\" width=\"300\">"); //$NON-NLS-1$ //$NON-NLS-2$
                        sB.append("</td>"); //$NON-NLS-1$
                        sB.append("</tr>"); //$NON-NLS-1$
                        images.add(imageId);
                    } else if (type.equals(Utilities.TYPE_SKETCH)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        String[] imageIdsSplit = value.split(Utilities.IMAGES_SEPARATOR);
                        for (String imageId : imageIdsSplit) {
                            Images image = imagesDAO.queryForSameId(new Images(Long.parseLong(imageId)));
                            String imgName = image.text;
                            sB.append("<tr>"); //$NON-NLS-1$
                            sB.append(
                                    "<td colspan=\"2\" style=\"text-align: left; vertical-align: top; width: 100%;\">"); //$NON-NLS-1$
                            sB.append("<img src=\"").append(imgName).append("\" width=\"300\">"); //$NON-NLS-1$ //$NON-NLS-2$
                            sB.append("</td>"); //$NON-NLS-1$
                            sB.append("</tr>"); //$NON-NLS-1$

                            images.add(imageId);
                        }
                    } else {
                        sB.append("<tr>"); //$NON-NLS-1$
                        sB.append("<td style=\"text-align: left; vertical-align: top; width: 50%;\">"); //$NON-NLS-1$
                        sB.append(label);
                        sB.append("</td>"); //$NON-NLS-1$
                        sB.append("<td style=\"text-align: left; vertical-align: top; width: 50%;\">"); //$NON-NLS-1$
                        sB.append(value);
                        sB.append("</td>"); //$NON-NLS-1$
                        sB.append("</tr>"); //$NON-NLS-1$
                    }
                }
                sB.append("</tbody>"); //$NON-NLS-1$
                sB.append("</table>"); //$NON-NLS-1$
            }
            sB.append("]]>\n"); //$NON-NLS-1$
        } else {
            String description = makeXmlSafe(this.description);
            sB.append(description);
            sB.append("\n"); //$NON-NLS-1$
            sB.append(new Date(timestamp));
        }

        sB.append("</description>\n"); //$NON-NLS-1$
        sB.append("<gx:balloonVisibility>1</gx:balloonVisibility>\n"); //$NON-NLS-1$
        sB.append("<Point>\n"); //$NON-NLS-1$
        sB.append("<coordinates>").append(the_geom.getX()).append(",").append(the_geom.getY()) //$NON-NLS-1$ //$NON-NLS-2$
                .append(",0</coordinates>\n"); //$NON-NLS-1$
        sB.append("</Point>\n"); //$NON-NLS-1$
        sB.append("</Placemark>\n"); //$NON-NLS-1$

        return sB.toString();
    }

    public boolean hasImages() {
        return images != null && images.size() > 0;
    }

    public List<String> getImageIds() {
        if (images == null) {
            try {
                images = Utilities.getImageIds(form);
            } catch (Exception e) {
                KukuratusLogger.logError(this, e);
            }
        }
        if (images == null) {
            return Collections.emptyList();
        }
        return images;
    }

}
