import 'dart:convert';

import 'package:after_layout/after_layout.dart';
import 'package:dart_hydrologis_utils/dart_hydrologis_utils.dart';
import 'package:flushbar/flushbar.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:flutter_server/com/hydrologis/gss/layers.dart';
import 'package:flutter_server/com/hydrologis/gss/models.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:flutter_server/com/hydrologis/gss/utils.dart';
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:latlong/latlong.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';

Marker buildSimpleNote(
    var x, var y, String name, Icon icon, double size, Color color) {
  List lengthHeight = guessTextDimensions(name, size);
  return Marker(
    width: lengthHeight[0],
    height: size + lengthHeight[1],
    point: new LatLng(y, x),
    builder: (ctx) => new Container(
      child: Column(
        children: <Widget>[
          icon,
          FittedBox(
            child: Container(
              decoration: new BoxDecoration(
                  color: color,
                  borderRadius:
                      new BorderRadius.all(const Radius.circular(5.0))),
              child: new Center(
                child: Padding(
                  padding: const EdgeInsets.all(5.0),
                  child: Text(
                    name,
                    style: TextStyle(
                        fontWeight: FontWeight.normal, color: Colors.black),
                  ),
                ),
              ),
            ),
          )
        ],
      ),
    ),
  );
}

List guessTextDimensions(String name, double size) {
  var lengthHeight = [];
  final constraints = BoxConstraints(
    maxWidth: 800.0, // maxwidth calculated
    minHeight: 0.0,
    minWidth: 0.0,
  );

  RenderParagraph renderParagraph = RenderParagraph(
    TextSpan(
      text: name,
      style: TextStyle(
        fontSize: 36,
      ),
    ),
    textDirection: TextDirection.ltr,
    maxLines: 1,
  );
  renderParagraph.layout(constraints);
  double textlen = renderParagraph.getMinIntrinsicWidth(36).ceilToDouble();
  double textHeight = renderParagraph.getMinIntrinsicHeight(36).ceilToDouble();

  textlen = textlen > size ? textlen : size;
  lengthHeight.add(textlen);
  lengthHeight.add(textHeight);
  return lengthHeight;
}

Marker buildImage(MapstateModel mapState, double screenHeight, var x, var y,
    String name, var dataId, var imageWidget) {
  return Marker(
    width: 180,
    height: 180,
    point: new LatLng(y, x),
    builder: (ctx) => new Container(
      child: GestureDetector(
        onTap: () async {
          Flushbar(
            flushbarPosition: FlushbarPosition.BOTTOM,
            flushbarStyle: FlushbarStyle.GROUNDED,
            backgroundColor: Colors.white.withAlpha(128),
//              isDismissible: true,
//              dismissDirection: FlushbarDismissDirection.HORIZONTAL,
            onTap: (e) {
              Navigator.of(mapState.currentMapContext).pop();
            },
            titleText: SmashUI.titleText(
              name,
              textAlign: TextAlign.center,
            ),
            messageText:
                NetworkImageWidget("$API_IMAGE/$dataId", screenHeight / 2.0),
          )..show(mapState.currentMapContext);
        },
        child: imageWidget,
      ),
    ),
  );
}

Marker buildFormNote(MapstateModel mapState, var x, var y, String name,
    String form, var noteId, Icon icon, double size, Color color) {
  LatLng p = LatLng(y, x);

  List lengthHeight = guessTextDimensions(name, size);

  return Marker(
    width: lengthHeight[0],
    height: size + lengthHeight[1],
    point: new LatLng(y, x),
    builder: (ctx) => new Container(
      child: GestureDetector(
        child: Column(
          children: <Widget>[
            icon,
            Container(
              decoration: new BoxDecoration(
                  color: color,
                  borderRadius:
                      new BorderRadius.all(const Radius.circular(5.0))),
              child: new Center(
                child: Padding(
                  padding: const EdgeInsets.all(5.0),
                  child: Text(
                    name,
                    style: TextStyle(
                        fontWeight: FontWeight.normal, color: Colors.black),
                  ),
                ),
              ),
            )
          ],
        ),
        onTap: () async {
          var sectionMap = jsonDecode(form);
          var sectionName = sectionMap[ATTR_SECTIONNAME];

          Flushbar(
            flushbarPosition: FlushbarPosition.BOTTOM,
            flushbarStyle: FlushbarStyle.GROUNDED,
            backgroundColor: Colors.white.withAlpha(128),
            onTap: (e) {
              Navigator.of(mapState.currentMapContext).pop();
            },
            messageText: Container(
              height: 600,
              child: Center(
                child: MasterDetailPage(
                  sectionMap,
                  SmashUI.titleText(sectionName,
                      color: SmashColors.mainBackground, bold: true),
                  sectionName,
                  p,
                  noteId,
                  null, // TODO add here save function if editing is supported on web
                  null, // TODO add get thumbnails function
                  null, // no taking pictures permitted on web
                ),
              ),
            ),
          )..show(mapState.currentMapContext);
        },
      ),
    ),
  );
}

openMapSelectionDialog(BuildContext context) {
  var size = 400.0;
  Dialog mapSelectionDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: size,
      width: size,
      child: BackgroundMapSelectionWidget(),
    ),
  );
  showDialog(
      context: context, builder: (BuildContext context) => mapSelectionDialog);
}

class BackgroundMapSelectionWidget extends StatefulWidget {
  BackgroundMapSelectionWidget();
  _BackgroundMapSelectionWidgetState createState() =>
      _BackgroundMapSelectionWidgetState();
}

class _BackgroundMapSelectionWidgetState
    extends State<BackgroundMapSelectionWidget> {
  int _index = 0;
  List<TileLayerOptions> _widgets = [];
  List<String> _names = [];
  MapController _mapController = new MapController();

  @override
  void initState() {
    AVAILABLE_MAPS.forEach((name, tilelayer) {
      _names.add(name);
      _widgets.add(tilelayer);
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: SmashUI.titleText(_names[_index]),
        ),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.all(8.0),
            child: Listener(
              // listen to mouse scroll
              onPointerSignal: (e) {
                if (e is PointerScrollEvent) {
                  var delta = e.scrollDelta.direction;
                  _mapController.move(_mapController.center,
                      _mapController.zoom + (delta > 0 ? -0.2 : 0.2));
                }
              },
              child: FlutterMap(
                options: new MapOptions(
                  center: new LatLng(46.47781, 11.33140),
                  zoom: 8,
                ),
                layers: [_widgets[_index]],
                mapController: _mapController,
              ),
            ),
          ),
        ),
        ButtonBar(
          alignment: MainAxisAlignment.spaceEvenly,
          children: [
            FlatButton(
              child: const Text('CANCEL'),
              onPressed: () {
                Navigator.pop(context);
              },
            ),
            FlatButton(
              child: const Text('NEXT'),
              onPressed: () {
                setState(() {
                  _index = _index + 1;
                  if (_index >= _widgets.length) {
                    _index = 0;
                  }
                });
              },
            ),
            FlatButton(
              child: const Text('OK'),
              onPressed: () {
                SmashSession.setBasemap(_names[_index]);
                var mapstateModel =
                    Provider.of<MapstateModel>(context, listen: false);
                mapstateModel.reloadMap();
                Navigator.pop(context);
              },
            ),
          ],
        )
      ],
    );
  }
}

openFilterDialog(BuildContext context) {
  var size = 600.0;
  Dialog filterDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: size,
      width: size,
      child: FilterWidget(),
    ),
  );
  showDialog(context: context, builder: (BuildContext context) => filterDialog);
}

class FilterWidget extends StatefulWidget {
  FilterWidget();
  _FilterWidgetState createState() => _FilterWidgetState();
}

class _FilterWidgetState extends State<FilterWidget>
    with AfterLayoutMixin<FilterWidget> {
  Map<String, bool> _projectsToActive;
  Map<String, bool> _surveyorsToActive;
  List<String> _projectNames;
  List<String> _surveyorNames;
  bool _doSurveyors = true;

  bool _dataLoaded = false;

  FilterStateModel _filterStateModel;

  @override
  Future<void> afterFirstLayout(BuildContext context) async {
    _filterStateModel = Provider.of<FilterStateModel>(context, listen: false);
    var sessionUser = SmashSession.getSessionUser();
    String responsJson =
        await ServerApi.getProjects(sessionUser[0], sessionUser[1]);

    var jsonMap = jsonDecode(responsJson);

    List<dynamic> projects = jsonMap[KEY_PROJECTS];
    List<String> filterProjects = _filterStateModel.projects;

    Map<String, bool> tmp = {};
    projects.forEach((name) {
      tmp[name] = filterProjects != null ? filterProjects.contains(name) : true;
    });

    _projectsToActive = tmp;
    _projectNames = _projectsToActive.keys.toList();

    responsJson = await ServerApi.getSurveyors(sessionUser[0], sessionUser[1]);

    jsonMap = jsonDecode(responsJson);

    List<dynamic> surveyors = jsonMap[KEY_SURVEYORS];
    List<String> filterSurveyors = _filterStateModel.surveyors;

    tmp = {};
    surveyors.forEach((name) {
      tmp[name] =
          filterSurveyors != null ? filterSurveyors.contains(name) : true;
    });

    _surveyorsToActive = tmp;
    _surveyorNames = _surveyorsToActive.keys.toList();

    setState(() {
      _dataLoaded = true;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (!_dataLoaded) {
      return SmashCircularProgress();
    } else {
      List<dynamic> names = _doSurveyors ? _surveyorNames : _projectNames;
      Map<String, bool> name2active =
          _doSurveyors ? _surveyorsToActive : _projectsToActive;
      var title = _doSurveyors ? "SURVEYORS" : "PROJECTS";

      return Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: SmashUI.titleText(title),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListView.builder(
                itemCount: names.length,
                itemBuilder: (BuildContext context, int index) {
                  var name = names[index];
                  var isActive = name2active[name];
                  return CheckboxListTile(
                      title: Text(name),
                      value: isActive,
                      onChanged: (selected) {
                        setState(() {
                          name2active[name] = selected;
                        });
                      });
                },
              ),
            ),
          ),
          ButtonBar(
            alignment: MainAxisAlignment.spaceEvenly,
            children: [
              FlatButton(
                child: const Text('CANCEL'),
                onPressed: () {
                  Navigator.pop(context);
                },
              ),
              FlatButton(
                child: const Text('RESET'),
                onPressed: () async {
                  _filterStateModel.reset();
                  var mapstateModel =
                      Provider.of<MapstateModel>(context, listen: false);
                  await mapstateModel.getData(context);
                  mapstateModel.fitbounds();
                  mapstateModel.reloadMap();
                  Navigator.pop(context);
                },
              ),
              FlatButton(
                child: Text(_doSurveyors ? 'PROJECTS' : 'SURVEYORS'),
                onPressed: () {
                  setState(() {
                    _doSurveyors = !_doSurveyors;
                  });
                },
              ),
              FlatButton(
                child: const Text('OK'),
                onPressed: () async {
                  _projectsToActive.removeWhere((key, value) => !value);
                  var activeProjects = _projectsToActive.entries
                      .map((entry) => entry.key)
                      .toList();
                  _filterStateModel.setProjectsQuiet(activeProjects);

                  _surveyorsToActive.removeWhere((key, value) => !value);
                  var activeSurveyors =
                      _surveyorsToActive.entries.map((e) => e.key).toList();
                  _filterStateModel.setSurveyors(activeSurveyors);

                  var mapstateModel =
                      Provider.of<MapstateModel>(context, listen: false);
                  await mapstateModel.getData(context);
                  mapstateModel.fitbounds();
                  mapstateModel.reloadMap();
                  Navigator.pop(context);
                },
              ),
            ],
          )
        ],
      );
    }
  }
}

class Attributes {
  Widget marker;
  int id;
  String text;
  int timeStamp;
  String user;
  String project;
  LatLng point;
}

class AttributesTableWidget extends StatefulWidget {
  ValueNotifier<bool> refreshNotifier = ValueNotifier<bool>(true);
  AttributesTableWidget({Key key}) : super(key: key);

  @override
  _AttributesTableWidgetState createState() => _AttributesTableWidgetState();

  void refresh() {
    refreshNotifier.value = !refreshNotifier.value;
  }
}

class _AttributesTableWidgetState extends State<AttributesTableWidget> {
  @override
  void initState() {
    widget.refreshNotifier.addListener(() {
      setState(() {});
    });

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    var mapstateModel = Provider.of<MapstateModel>(context, listen: false);
    var dataRows = mapstateModel.attributes
        .where((arrt) => mapstateModel.currentMapBounds.contains(arrt.point))
        .map((attr) {
      return DataRow(cells: [
        DataCell(attr.marker),
        DataCell(Text("${attr.id}")),
        DataCell(Text(attr.text??"text")),
        DataCell(Text(TimeUtilities.ISO8601_TS_FORMATTER
            .format(DateTime.fromMillisecondsSinceEpoch(attr.timeStamp)))),
        DataCell(Text(attr.user??"user")),
        DataCell(Text(attr.project??"project")),
      ]);
    }).toList();

    return SingleChildScrollView(
      child: Container(
        width: double.infinity,
        child: DataTable(
          columns: [
            DataColumn(
              label: Text("Marker"),
            ),
            DataColumn(
              label: Text("Id"),
            ),
            DataColumn(
              label: Text("Text"),
            ),
            DataColumn(
              label: Text("Timestamp"),
            ),
            DataColumn(
              label: Text("User"),
            ),
            DataColumn(
              label: Text("Project"),
            ),
          ],
          rows: dataRows,
        ),
      ),
    );
  }
}
