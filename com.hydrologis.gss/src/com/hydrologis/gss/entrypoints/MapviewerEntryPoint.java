/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package com.hydrologis.gss.entrypoints;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.hydrologis.gss.GssContext;
import com.hydrologis.gss.GssDbProvider;
import com.hydrologis.gss.GssSession;
import com.hydrologis.gss.entrypoints.settings.WebUsersUIHandler;
import com.hydrologis.gss.map.GssMapBrowser;
import com.hydrologis.gss.server.database.DatabaseHandler;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.gss.utils.DevicesTableContentProvider;
import com.hydrologis.gss.utils.GssGuiUtilities;
import com.hydrologis.gss.utils.GssLoginDialog;
import com.hydrologis.gss.utils.GssMapsHandler;
import com.hydrologis.gss.utils.GssUserPermissionsHandler;
import com.hydrologis.gss.utils.ImageCache;

import eu.hydrologis.stage.libs.entrypoints.StageEntryPoint;
import eu.hydrologis.stage.libs.html.HtmlFeatureChooser;
import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.map.IMapObserver;
import eu.hydrologis.stage.libs.providers.data.SpatialDbDataProvider;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.utils.StageUtils;
import eu.hydrologis.stage.libs.utilsrap.MessageDialogUtil;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class MapviewerEntryPoint extends StageEntryPoint implements IMapObserver {

    private static final String NOTES = "Notes";
    private static final String LOGS = "Gps Logs";

    public static final String ID = "com.hydrologis.gss.entrypoints.MapviewerEntryPoint";

    private static final long serialVersionUID = 1L;

    private Display display;

    private Shell parentShell;
    private GssMapBrowser mapBrowser;
    private Composite mapComposite;

    private SashForm mainSashComposite;

    private Combo devicesCombo;

    private DatabaseHandler databaseHandler;

    private TableViewer devicesTableViewer;

    private List<GpapUsers> visibleDevices = new ArrayList<>();

    private GssDbProvider dbp;
    private List<GpapUsers> allDevices;
    private Map<String, GpapUsers> deviceNamesMap;

    @Override
    protected void createPage( final Composite parent ) {

        eu.hydrologis.stage.libs.registry.User loggedUser = GssLoginDialog.checkUserLogin(getShell());
        boolean isAdmin = RegistryHandler.INSTANCE.isAdmin(loggedUser);
        GssUserPermissionsHandler permissionsHandler = new GssUserPermissionsHandler(isAdmin);
        if (loggedUser == null || !permissionsHandler.isAllowed(redirectUrl)) {
            StageUtils.permissionDeniedPage(parent, redirectUrl);
            return;
        }

        String name = loggedUser.getName();
        String uniquename = loggedUser.getUniqueName();
        StageUtils.logAccessIp(redirectUrl, "user:" + uniquename);

        parentShell = parent.getShell();
        display = parent.getDisplay();

        dbp = GssContext.instance().getDbProvider();
        databaseHandler = dbp.getDatabaseHandler();
        try {
            allDevices = databaseHandler.getDao(GpapUsers.class).queryForAll();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        deviceNamesMap = allDevices.stream().collect(Collectors.toMap(gu -> getUserName(gu), Function.identity()));

        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(2, false));

        final Composite horizToolbarComposite = new Composite(composite, SWT.NONE);
        GridData horizToolbarGD = new GridData(SWT.FILL, SWT.FILL, false, false);
        horizToolbarGD.horizontalSpan = 2;
        horizToolbarComposite.setLayoutData(horizToolbarGD);
        GridLayout toolbarLayout = new GridLayout(7, false);
        horizToolbarComposite.setLayout(toolbarLayout);

        final Composite vertToolbarComposite = new Composite(composite, SWT.NONE);
        vertToolbarComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        GridLayout vertToolbarLayout = new GridLayout(1, false);
        vertToolbarComposite.setLayout(vertToolbarLayout);

        GssGuiUtilities.addLogo(horizToolbarComposite);
        GssGuiUtilities.addPagesLinks(vertToolbarComposite, this, isAdmin);

        ToolBar toolsToolBar = new ToolBar(horizToolbarComposite, SWT.FLAT);

        final ToolItem devicesItem = new ToolItem(toolsToolBar, SWT.CHECK);
        devicesItem.setImage(ImageCache.getInstance().getImage(display, ImageCache.GROUP));
        devicesItem.setWidth(200);
        devicesItem.setText("Toggle Devices View");
        devicesItem.setToolTipText("Toggle the devices view to add and remove data to visualize.");
        devicesItem.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                if (devicesItem.getSelection()) {
                    mainSashComposite.setMaximizedControl(null);
                } else {
                    mainSashComposite.setMaximizedControl(mapComposite);
                }
                GssSession.setDevicesVisible(devicesItem.getSelection());
            }
        });

        GssGuiUtilities.addToolBarSeparator(toolsToolBar);
        GssGuiUtilities.addVerticalFiller(vertToolbarComposite);
        GssGuiUtilities.addHorizontalFiller(horizToolbarComposite);
        GssGuiUtilities.addAdminTools(vertToolbarComposite, this, isAdmin);
        GssGuiUtilities.addLogoutButton(horizToolbarComposite);

        // SASH FOR MAP + FEATURE PROPERTIES
        mainSashComposite = new SashForm(composite, SWT.HORIZONTAL);
        GridData mainCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainSashComposite.setLayoutData(mainCompositeGD);

        Composite devicesComposite = new Composite(mainSashComposite, SWT.None);
        GridLayout attributesLayout = new GridLayout(1, true);
        attributesLayout.marginWidth = 0;
        attributesLayout.marginHeight = 0;
        devicesComposite.setLayout(attributesLayout);
        GridData attributesGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        devicesComposite.setLayoutData(attributesGD);

        // Button closeViewButton = new Button(featurePropertiesComposite, SWT.PUSH | SWT.FLAT);
        // GridData closeViewButtonGD = new GridData(SWT.END, SWT.FILL, false, false);
        // closeViewButton.setLayoutData(closeViewButtonGD);
        // Image closeViewImage = ImageCache.getInstance().getImage(display, ImageCache.CLOSE_VIEW);
        // closeViewButton.setImage(closeViewImage);
        // closeViewButton.setToolTipText("Close properties view");
        // closeViewButton.addSelectionListener(new SelectionAdapter(){
        // @Override
        // public void widgetSelected( SelectionEvent e ) {
        // mainSashComposite.setMaximizedControl(mapAndAttributesSashComposite);
        // }
        // });

        try {
            createDevicesCombo(devicesComposite);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapComposite = new Composite(mainSashComposite, SWT.None);
        GridLayout mapLayout = new GridLayout(1, true);
        mapLayout.marginWidth = 0;
        mapLayout.marginHeight = 0;
        mapComposite.setLayout(mapLayout);
        GridData mapGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        mapComposite.setLayoutData(mapGD);

        mapBrowser = new GssMapBrowser(mapComposite, SWT.BORDER);
        GridData mapBrowserGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mapBrowser.setLayoutData(mapBrowserGD);
        mapBrowser.addMapObserver(this);
        loadMapBrowser();

        mainSashComposite.setWeights(new int[]{2, 8});
        if (!GssSession.areDevicesVisible()) {
            mainSashComposite.setMaximizedControl(mapComposite);
        }

        GssGuiUtilities.addFooter(name, composite);

        String[] loadedGpapUsers = GssSession.getLoadedGpapUsers();
        if (loadedGpapUsers != null) {
            for( String loadedGpapUser : loadedGpapUsers ) {
                GpapUsers gpapUser = deviceNamesMap.get(loadedGpapUser);
                visibleDevices.add(gpapUser);
                addDeviceToMap(gpapUser);
            }
        }
        devicesTableViewer.setInput(visibleDevices);
    }

    private void loadMapBrowser() {
        if (mapBrowser != null) {
            String mapHtml = mapBrowser.getMapHtml();
            mapHtml = HtmlFeatureChooser.INSTANCE.addDebugging(mapHtml);
            mapHtml = HtmlFeatureChooser.INSTANCE.addUndefinedProgressBar(mapHtml);
            mapHtml = HtmlFeatureChooser.INSTANCE.addCursors(mapHtml);
            String replacement = "";
            try {
                replacement += GssMapsHandler.INSTANCE.addSelectedMaps(mapBrowser);
            } catch (SQLException e) {
                StageLogger.logError(this, e);
            }

            // replacement += mapBrowser.getAddTileLayer("pipes", "pipes",
            // "./getpipetile?z={z}&x={x}&y={y}", 22, 22, "© Comune Roma", true, true);
            // replacement += mapBrowser.getAddTileLayer("work", "work",
            // "./getworktile?z={z}&x={x}&y={y}", 22, 22, "© Comune Roma", true, true);
            // replacement += mapBrowser.getAddTileLayer("events", "events",
            // "./geteventstile?z={z}&x={x}&y={y}", 22, 22, "© Comune Roma", true, true);

            mapHtml = mapBrowser.addLayersInHtml(mapHtml, replacement);

            mapBrowser.setText(mapHtml);
        }
    }

    public GssMapBrowser getMapBrowser() {
        return mapBrowser;
    }

    private void createDevicesCombo( Composite parent ) throws Exception {

        Composite devicesViewerGroup = new Composite(parent, SWT.BORDER);
        GridData devicesViewerGroupGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        devicesViewerGroup.setLayoutData(devicesViewerGroupGD);
        GridLayout devicesViewerLayout = new GridLayout(3, true);
        devicesViewerLayout.marginWidth = 15;
        devicesViewerLayout.marginHeight = 15;
        devicesViewerLayout.verticalSpacing = 15;
        devicesViewerGroup.setLayout(devicesViewerLayout);
        // devicesViewerGroup.setText("Surveyors");

        devicesCombo = new Combo(devicesViewerGroup, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        GridData devicesGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        devicesGD.horizontalSpan = 3;
        devicesCombo.setLayoutData(devicesGD);

        devicesCombo.setItems(deviceNamesMap.keySet().toArray(new String[0]));
        devicesCombo.setToolTipText("Select device to add");

        Composite tableComposite = new Composite(devicesViewerGroup, SWT.NONE);
        GridData tableCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableCompositeGD.horizontalSpan = 3;
        tableComposite.setLayoutData(tableCompositeGD);

        devicesTableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        devicesTableViewer.setContentProvider(DevicesTableContentProvider.getInstance());

        TableColumn deviceColumn = createColumn(devicesTableViewer, "Device", 0);

        TableColumnLayout layout = new TableColumnLayout();
        tableComposite.setLayout(layout);
        layout.setColumnData(deviceColumn, new ColumnWeightData(100, true));

        devicesTableViewer.getTable().setHeaderVisible(false);
        devicesTableViewer.getTable().setLinesVisible(true);
        GridData devicesTableGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        devicesTableGD.horizontalSpan = 3;
        devicesTableViewer.getTable().setLayoutData(devicesTableGD);

        Button addButton = new Button(devicesViewerGroup, SWT.PUSH);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        addButton.setText("   +   ");
        addButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                String selected = devicesCombo.getText();
                GpapUsers user = deviceNamesMap.get(selected);
                if (!visibleDevices.contains(user)) {
                    visibleDevices.add(user);
                }
                devicesTableViewer.setInput(visibleDevices);

                addDeviceToMap(user);
                addDevicesToSession();

            }

        });
        Button addAllButton = new Button(devicesViewerGroup, SWT.PUSH);
        addAllButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        addAllButton.setText("   +ALL   ");
        addAllButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                String[] all = devicesCombo.getItems();
                for( String selected : all ) {
                    GpapUsers user = deviceNamesMap.get(selected);
                    if (!visibleDevices.contains(user)) {
                        visibleDevices.add(user);
                    }
                    devicesTableViewer.setInput(visibleDevices);
                    addDeviceToMap(user);
                    addDevicesToSession();
                }
            }
        });

        Button removeButton = new Button(devicesViewerGroup, SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        removeButton.setText("   -   ");
        removeButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                IStructuredSelection selection = (IStructuredSelection) devicesTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    Iterator< ? > iterator = selection.iterator();
                    List<String> namesToRemove = new ArrayList<>();
                    while( iterator.hasNext() ) {
                        Object object = iterator.next();
                        if (object instanceof GpapUsers) {
                            GpapUsers user = (GpapUsers) object;
                            if (visibleDevices.contains(user)) {
                                visibleDevices.remove(user);
                                namesToRemove.add(getUserName(user));
                            }
                        }
                    }
                    devicesTableViewer.setInput(visibleDevices);
                    removeDeviceFromMap(namesToRemove);
                    addDevicesToSession();
                }
            }
        });

        // add right click menu
        MenuManager manager = new MenuManager();
        devicesTableViewer.getControl().setMenu(manager.createContextMenu(devicesTableViewer.getControl()));
        manager.addMenuListener(new IMenuListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void menuAboutToShow( IMenuManager manager ) {
                IStructuredSelection selection = (IStructuredSelection) devicesTableViewer.getSelection();
                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof GpapUsers) {
                    GpapUsers selectedUser = (GpapUsers) firstElement;
                    manager.add(new Action("Zoom to data", null){
                        @Override
                        public void run() {
                            String notesLayer = getLayerName(selectedUser, NOTES);
                            String logsLayer = getLayerName(selectedUser, LOGS);

                            String script = mapBrowser.getZoomToLayer(notesLayer);
                            script += mapBrowser.getZoomToLayer(logsLayer);
                            mapBrowser.runScript(script);
                        }
                    });
                }

                if (devicesTableViewer.getSelection() instanceof IStructuredSelection) {

                }
            }

        });
        manager.setRemoveAllWhenShown(true);

        parent.layout();
    }

    private void addDevicesToSession() {
        String[] newLoadedGpapUsers = new String[visibleDevices.size()];
        for( int i = 0; i < newLoadedGpapUsers.length; i++ ) {
            newLoadedGpapUsers[i] = getUserName(visibleDevices.get(i));
        }
        GssSession.setLoadedGpapUsers(newLoadedGpapUsers);
    }

    private void addDeviceToMap( GpapUsers user ) {
        mapBrowser.runScript(mapBrowser.getStartUndefindedProgress("Loading device data..."));
        try {
            SpatialDbDataProvider notesProv = new SpatialDbDataProvider(dbp.getDb(), getLayerName(user, NOTES),
                    DatabaseHandler.getTableName(Notes.class), new String[]{Notes.TEXT_FIELD_NAME, Notes.ALTIM_FIELD_NAME});
            String notesName = notesProv.getName();
            String notesScript = mapBrowser.getRemoveDataLayer(notesName);
            String notesGeoJson = notesProv.asGeoJson(Notes.GPAPUSER_FIELD_NAME + "=" + user.id);
            if (notesGeoJson != null) {
                notesGeoJson = notesGeoJson.replaceAll("'", "`");
                notesScript += "addJsonMap('" + notesName + "','" + notesGeoJson + "',null);";
            }
            mapBrowser.runScript(notesScript);

            SpatialDbDataProvider logsProv = new SpatialDbDataProvider(dbp.getDb(), getLayerName(user, LOGS),
                    DatabaseHandler.getTableName(GpsLogs.class),
                    new String[]{GpsLogs.NAME_FIELD_NAME, GpsLogs.STARTTS_FIELD_NAME, GpsLogs.ENDTS_FIELD_NAME});
            String logsName = logsProv.getName();
            String logsScript = mapBrowser.getRemoveDataLayer(logsName);
            String logsGeoJson = logsProv.asGeoJson(Notes.GPAPUSER_FIELD_NAME + "=" + user.id);
            if (logsGeoJson != null) {
                // logsGeoJson = logsGeoJson.replaceAll("'", "`");
                logsScript += "addJsonMap('" + logsName + "','" + logsGeoJson + "',null);";
            }
            mapBrowser.runScript(logsScript);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mapBrowser.runScript(mapBrowser.getStopUndefindedProgress());
        }

    }

    private void removeDeviceFromMap( List<String> namesToRemove ) {
        try {
            String script = "";
            for( String name : namesToRemove ) {
                String layerName = getLayerName(name, NOTES);
                script += mapBrowser.getRemoveDataLayer(layerName);
                layerName = getLayerName(name, LOGS);
                script += mapBrowser.getRemoveDataLayer(layerName);
            }
            mapBrowser.runScript(script);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getLayerName( GpapUsers user, String postFix ) {
        return postFix + ": " + user.deviceId;
    }
    private String getLayerName( String comboString, String postFix ) {
        return postFix + ": " + comboString;
    }

    private TableColumn createColumn( TableViewer viewer, String name, int dataIndex ) {
        TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
        result.setLabelProvider(new DeviceLabelProvider());
        TableColumn column = result.getColumn();
        column.setText(name);
        column.setToolTipText(name);
        column.setWidth(100);
        column.setMoveable(true);
        // column.addSelectionListener( new SelectionAdapter() {
        // @Override
        // public void widgetSelected( SelectionEvent event ) {
        // int sortDirection = updateSortDirection( ( TableColumn )event.widget );
        // sort( viewer, COL_FIRST_NAME, sortDirection == SWT.DOWN );
        // }
        // } );
        return column;
    }

    private class DeviceLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText( Object element ) {
            if (element instanceof GpapUsers) {
                GpapUsers user = (GpapUsers) element;
                return getUserName(user);
            }
            return "???";
        }
    }

    private String getUserName( GpapUsers user ) {
        return user.name;
    }

    @Override
    public void onClick( double lon, double lat, int zoomLevel ) {
    }

    @Override
    public void layerFeatureClicked( String layerName, Object[] properties ) {
        // TODO Auto-generated method stub

    }

}
