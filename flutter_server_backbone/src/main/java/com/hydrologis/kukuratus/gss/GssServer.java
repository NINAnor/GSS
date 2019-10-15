package com.hydrologis.kukuratus.gss;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.compat.IHMResultSet;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.hortonmachine.dbs.log.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hydrologis.kukuratus.database.DatabaseHandler;
import com.hydrologis.kukuratus.gss.database.Forms;
import com.hydrologis.kukuratus.gss.database.GpapUsers;
import com.hydrologis.kukuratus.gss.database.GpsLogs;
import com.hydrologis.kukuratus.gss.database.GpsLogsData;
import com.hydrologis.kukuratus.gss.database.GpsLogsProperties;
import com.hydrologis.kukuratus.gss.database.ImageData;
import com.hydrologis.kukuratus.gss.database.Images;
import com.hydrologis.kukuratus.gss.database.Notes;
import com.hydrologis.kukuratus.tiles.ITilesGenerator;
import com.hydrologis.kukuratus.tiles.MapsforgeTilesGenerator;
import com.hydrologis.kukuratus.utils.KukuratusLogger;
import com.hydrologis.kukuratus.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;

/**
 * Deploy:
 * 
 * - export as runnable jar (will use embedded jetty, which is anyways necessary for websockets)
 * - run with java -jar xxx.jar
 * 
 * To load static pages, the folder public, that resides in the src/main/resources folder, needs
 * to be copied to the folder into which the runnable jar is exported and used. 
 * 
 * 
 * @author hydrologis
 *
 */
public class GssServer implements Vars {
    private List<Class< ? >> tableClasses = Arrays.asList(//
            GpapUsers.class, //
            Notes.class, //
            ImageData.class, //
            Images.class, //
            GpsLogs.class, //
            GpsLogsData.class, //
            GpsLogsProperties.class, //
            Forms.class);

    private ITilesGenerator mapsforgeTilesGenerator;

    public void start() throws Exception {
        KukuratusWorkspace workspace = KukuratusWorkspace.getInstance();
        File dataFolder = workspace.getDataFolder();
        File dbFile = new File(dataFolder, "gss_database.mv.db"); //$NON-NLS-1$
        if (!dbFile.exists()) {
            KukuratusLogger.logInfo(this, "No database present in folder, creating one."); //$NON-NLS-1$
        }

        // TODO handel dbs
        ASpatialDb db = EDb.H2GIS.getSpatialDb();
        db.open(dbFile.getAbsolutePath());
        DatabaseHandler.init(db);
        createTables();

        /*
         * THE SERVER
         */
        port(WEBAPP_PORT);
        staticFiles.location("/public");

        // ENABLE CORS START
        options("/*", ( request, response ) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before(( request, response ) -> response.header("Access-Control-Allow-Origin", "*"));
        // ENABLE CORS END

        // ROUTES START
        activateMapsforge();

        get("/tiles/:source/:z/:x/:y", ( req, res ) -> {
            String source = req.params(":source");
            if (source.equals("mapsforge")) {
                String x = req.params(":x");
                String y = req.params(":y");
                String z = req.params(":z");
                int xTile = Integer.parseInt(x);
                int yTile = Integer.parseInt(y);
                int zoom = Integer.parseInt(z);
                try {
                    HttpServletResponse raw = res.raw();
                    raw.setContentType("image/png");
                    res.header("Content-Disposition", "attachment; filename=image.png");
                    ServletOutputStream outputStream = raw.getOutputStream();
                    mapsforgeTilesGenerator.getTile(xTile, yTile, zoom, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    return raw;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // halt();
                }

                return res;
            }
            return res;
        });

//        get("/data/:sectorid/:from/:to", ( req, res ) -> {
//            try {
//                String sectorId = req.params(":sectorid");
//                int id = Integer.parseInt(sectorId);
//                String from = req.params(":from");
//                String to = req.params(":to");
//                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
//                long fromDate = dateFormatter.parse(from + " 00:00:00").getTime();
//                long toDate = dateFormatter.parse(to + " 23:59:59").getTime();
//
//                String sql = "select  type_id,timestamp, value from measures where sectorid=" + id + " and timestamp between "
//                        + fromDate + " and " + toDate + " group by  type_id, timestamp order by  type_id, timestamp";
//                JSONObject root = new JSONObject();
//                JSONArray temperatureArray = new JSONArray();
//                JSONArray humidityArray = new JSONArray();
//                root.put(TEMPERATURE_ID, temperatureArray);
//                root.put(HUMIDITY_ID, humidityArray);
//                String json = db.execOnConnection(connection -> {
//                    try (IHMStatement stmt = connection.createStatement(); IHMResultSet rs = stmt.executeQuery(sql)) {
//                        while( rs.next() ) {
//                            int type = rs.getInt(1);
//                            long ts = rs.getLong(2);
//                            double value = rs.getDouble(3);
//
//                            if (type == 0) {
//                                JSONObject valueObj = new JSONObject();
//                                valueObj.put("ts", ts);
//                                valueObj.put("v", value);
//                                temperatureArray.put(valueObj);
//                            } else {
//                                JSONObject valueObj = new JSONObject();
//                                valueObj.put("ts", ts);
//                                valueObj.put("v", value);
//                                humidityArray.put(valueObj);
//                            }
//                        }
//                        return root.toString();
//                    }
//                });
//                return json;
//            } catch (Exception e) {
//                Logger.INSTANCE.insertError("get status", "error", e);
//            }
//            return "{ERROR}";
//        });

        get("/data", ( req, res ) -> {
            JSONObject root = new JSONObject();
            Dao<GpapUsers, ? > userDao = DatabaseHandler.instance().getDao(GpapUsers.class);

            // TODO parameterize users, from and to
            List<GpapUsers> users = userDao.queryForAll();
            Long from = null;
            Long to = null;

//            Dao<GpsLogs, ? > logsDao = DatabaseHandler.instance().getDao(GpsLogs.class);
//            Dao<GpsLogsProperties, ? > logPropDao = DatabaseHandler.instance().getDao(GpsLogsProperties.class);
//            GssDatabaseUtilities.getLogs(root, logsDao, logPropDao, users, from, to);

            Dao<Notes, ? > notesDao = DatabaseHandler.instance().getDao(Notes.class);
            GssDatabaseUtilities.getNotes(root, notesDao, users, from, to);

            GssDatabaseUtilities.getImages(root, DatabaseHandler.instance().getDb(), users, from, to);

            return root.toString();
        });

        get("/", ( req, res ) -> {
            res.redirect("index.html");
            return null;
        });
        // ROUTES END
    }

    private void activateMapsforge() {
        try {
            File dataFolder = KukuratusWorkspace.getInstance().getDataFolder();
            File[] mapfiles = dataFolder.listFiles(new FilenameFilter(){
                @Override
                public boolean accept( File dir, String name ) {
                    return name.endsWith(".map"); //$NON-NLS-1$
                }
            });

            float factor = 1f;
            int tile = 256;
            mapsforgeTilesGenerator = new MapsforgeTilesGenerator("mapsforge", mapfiles, tile, factor, null); //$NON-NLS-1$
        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

    private void createTables() throws Exception {
        DatabaseHandler dbHandler = DatabaseHandler.instance();
        for( Class< ? > tClass : tableClasses ) {
            String tableName = DatabaseHandler.getTableName(tClass);
            if (dbHandler.getDb().hasTable(tableName)) {
                KukuratusLogger.logDebug(this, "Table exists already: " + tableName); //$NON-NLS-1$
                continue;
            }
            KukuratusLogger.logDebug(this, "Creating table: " + tableName); //$NON-NLS-1$
            dbHandler.createTableIfNotExists(tClass);
            KukuratusLogger.logDebug(this, "Done creating table: " + tableName); //$NON-NLS-1$
        }
    }

    public static void main( String[] args ) throws Exception {

        args = new String[]{"/home/hydrologis/TMP/TESTGSS/"}; // TODO remove after testing

        if (args.length == 0) {
            System.err.println("The workspace folder needs to be supplied as argument.");
            System.exit(1);
        }
        String workspacePath = args[0];
        File workspaceFolder = new File(workspacePath);
        if (!workspaceFolder.exists()) {
            System.err.println("The workspace folder needs to exist.");
            System.exit(1);
        }
        KukuratusWorkspace.setWorkspaceFolderPath(workspacePath);

        GssServer lampServer = new GssServer();
        lampServer.start();
    }

}