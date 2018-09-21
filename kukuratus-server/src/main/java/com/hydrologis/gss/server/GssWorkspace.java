package com.hydrologis.gss.server;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hydrologis.gss.server.utils.BaseMap;
import com.hydrologis.gss.server.utils.Overlays;
import com.hydrologis.gss.server.utils.Projects;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
import com.vaadin.ui.Notification;

public enum GssWorkspace {
    INSTANCE;

    public static final String BASEMAP = "basemaps";
    public static final String OVERLAYS = "overlays";
    public static final String PROJECTS = "projects";
    public static final String NAME = "name";

    private File basemapsFolderFile;
    private File overlayFolderFile;
    private File projectsFolderFile;

    public Optional<File> getBasemapsFolder() {
        Optional<File> workspaceFolder = KukuratusWorkspace.getInstance().getWorkspaceFolder();
        if (workspaceFolder.isPresent()) {
            File folder = workspaceFolder.get();
            File baseMapsFolder = new File(folder, BASEMAP);
            if (!baseMapsFolder.exists()) {
                if (!baseMapsFolder.mkdirs()) {
                    return Optional.empty();
                }
            }
            return Optional.of(baseMapsFolder);
        }
        return Optional.empty();
    }

    public Optional<File> getOverlaysFolder() {
        Optional<File> workspaceFolder = KukuratusWorkspace.getInstance().getWorkspaceFolder();
        if (workspaceFolder.isPresent()) {
            File folder = workspaceFolder.get();
            File overlaysFolder = new File(folder, OVERLAYS);
            if (!overlaysFolder.exists()) {
                if (!overlaysFolder.mkdirs()) {
                    return Optional.empty();
                }
            }
            return Optional.of(overlaysFolder);
        }
        return Optional.empty();
    }

    public Optional<File> getProjectsFolder() {
        Optional<File> workspaceFolder = KukuratusWorkspace.getInstance().getWorkspaceFolder();
        if (workspaceFolder.isPresent()) {
            File folder = workspaceFolder.get();
            File projectsFolder = new File(folder, PROJECTS);
            if (!projectsFolder.exists()) {
                if (projectsFolder.mkdirs()) {
                    return Optional.empty();
                }
            }
            return Optional.of(projectsFolder);
        }
        return Optional.empty();
    }

    private void checkFolders() {
        Optional<File> basemapsFolder = GssWorkspace.INSTANCE.getBasemapsFolder();
        Optional<File> overlaysFolder = GssWorkspace.INSTANCE.getOverlaysFolder();
        Optional<File> projectsFolder = GssWorkspace.INSTANCE.getProjectsFolder();
        if (!basemapsFolder.isPresent() || !overlaysFolder.isPresent() || !projectsFolder.isPresent()) {
            Notification.show("There is a problem with the data folders of your workspace. Contact your admin.",
                    Notification.Type.WARNING_MESSAGE);
            return;
        }
        basemapsFolderFile = basemapsFolder.get();
        overlayFolderFile = overlaysFolder.get();
        projectsFolderFile = projectsFolder.get();
    }

    public List<BaseMap> getBasemaps() {
        checkFolders();
        File[] baseMaps = basemapsFolderFile.listFiles(new FilenameFilter(){
            @Override
            public boolean accept( File dir, String name ) {
                return isBaseMap(name);
            }
        });

        List<BaseMap> maps = Arrays.asList(baseMaps).stream().map(file -> {
            BaseMap m = new BaseMap();
            m.setMapName(file.getName());
            return m;
        }).collect(Collectors.toList());
        return maps;
    }

    public List<Overlays> getOverlays() {
        checkFolders();
        File[] overlayMaps = overlayFolderFile.listFiles(new FilenameFilter(){
            @Override
            public boolean accept( File dir, String name ) {
                return isOverlay(name);
            }
        });

        List<Overlays> maps = Arrays.asList(overlayMaps).stream().map(file -> {
            Overlays m = new Overlays();
            m.setName(file.getName());
            return m;
        }).collect(Collectors.toList());
        return maps;
    }

    public List<Projects> getProjects() {
        checkFolders();
        File[] overlayMaps = projectsFolderFile.listFiles(new FilenameFilter(){
            @Override
            public boolean accept( File dir, String name ) {
                return isProject(name);
            }
        });

        List<Projects> maps = Arrays.asList(overlayMaps).stream().map(file -> {
            Projects m = new Projects();
            m.setName(file.getName());
            return m;
        }).collect(Collectors.toList());
        return maps;
    }

    public String getMapsListJson() {
        List<BaseMap> basemaps = getBasemaps();
        List<Overlays> overlays = getOverlays();
        List<Projects> projects = getProjects();

        JSONObject root = new JSONObject();

        JSONArray bmArray = new JSONArray();
        root.put(BASEMAP, bmArray);
        for( BaseMap bm : basemaps ) {
            JSONObject bmObj = new JSONObject();
            bmObj.put(NAME, bm.getMapName());
            bmArray.put(bmObj);
        }

        JSONArray ovArray = new JSONArray();
        root.put(OVERLAYS, ovArray);
        for( Overlays ov : overlays ) {
            JSONObject ovObj = new JSONObject();
            ovObj.put(NAME, ov.getName());
            ovArray.put(ovObj);
        }

        JSONArray pArray = new JSONArray();
        root.put(PROJECTS, pArray);
        for( Projects p : projects ) {
            JSONObject pObj = new JSONObject();
            pObj.put(NAME, p.getName());
            pArray.put(pObj);
        }

        return root.toString();
    }

    public static boolean isBaseMap( String name ) {
        return name.toLowerCase().endsWith(".map") || name.toLowerCase().endsWith(".mbtiles");
    }

    public static boolean isOverlay( String name ) {
        return name.toLowerCase().endsWith(".sqlite");
    }

    public static boolean isProject( String name ) {
        return name.toLowerCase().endsWith(".gpap");
    }

    public Optional<File> getMapFile( String fileName ) {
        if (isBaseMap(fileName)) {
            return Optional.of(new File(basemapsFolderFile, fileName));
        } else if (isOverlay(fileName)) {
            return Optional.of(new File(overlayFolderFile, fileName));
        } else if (isProject(fileName)) {
            return Optional.of(new File(projectsFolderFile, fileName));
        }
        return Optional.empty();
    }

}
