import 'dart:convert';

import 'package:dart_hydrologis_utils/dart_hydrologis_utils.dart' hide SIZE;
import 'package:flutter/material.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/maputils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/utils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';
import 'package:dart_jts/dart_jts.dart' as JTS;
// import 'package:flutter_map_tappable_polyline/flutter_map_tappable_polyline.dart';

class FilterStateModel extends ChangeNotifier {
  List<String> _surveyors;

  List<int> _fromToTimestamp;

  List<String> _projects;

  String _matchingText;

  List<String> get surveyors => _surveyors;

  void setSurveyors(List<String> surveyors) {
    _surveyors = surveyors;
    notifyListeners();
  }

  void setSurveyorsQuiet(List<String> surveyors) {
    _surveyors = surveyors;
  }

  List<int> get fromToTimestamp => _fromToTimestamp;

  void setFromToTimestamp(List<int> fromToTimestamp) {
    _fromToTimestamp = fromToTimestamp;
    notifyListeners();
  }

  List<String> get projects => _projects;

  void setProjects(List<String> projects) {
    _projects = projects;
    notifyListeners();
  }

  void setProjectsQuiet(List<String> projects) {
    _projects = projects;
  }

  String get matchingText => _matchingText;

  void setMatchingText(String matchingText) {
    _matchingText = matchingText;
    notifyListeners();
  }

  void reset() {
    _surveyors = null;
    _fromToTimestamp = null;
    _projects = null;
    _matchingText = null;
  }

  @override
  String toString() {
    String timespan = "No timepsan";
    if (_fromToTimestamp != null) {
      timespan = "Timespan: ";
      timespan +=
          "${TimeUtilities.ISO8601_TS_FORMATTER.format(DateTime.fromMillisecondsSinceEpoch(_fromToTimestamp[0]))}";
      timespan +=
          " to ${TimeUtilities.ISO8601_TS_FORMATTER.format(DateTime.fromMillisecondsSinceEpoch(_fromToTimestamp[1]))}";
    }

    String str = """
    Filter state:
      Projects: ${_projects?.join(";")}
      Surveyors: ${_surveyors?.join(";")}
      $timespan
    """;
    return str;
  }
}

class AttributesTableStateModel extends ChangeNotifier {
  int selectedNoteId;

  void refresh() {
    notifyListeners();
  }
}

class MapstateModel extends ChangeNotifier {
  PolylineLayerOptions logs;
  // TappablePolylineLayerOptions logs;
  List<Marker> mapMarkers = [];
  List<Attributes> attributes = [];
  LatLngBounds dataBounds = LatLngBounds();
  BuildContext currentMapContext;

  double screenHeight = 600;

  bool showAttributes = false;

  MapController mapController;

  LatLngBounds currentMapBounds;

  Map<String, TileLayerOptions> layersMap = {};

  void reloadMap() {
    notifyListeners();
  }

  void fitbounds({LatLngBounds newBounds}) {
    if (mapController != null) {
      mapController.fitBounds(newBounds ?? dataBounds);
      currentMapBounds = mapController.bounds;
    }
  }

  void setBackgroundLayers(Map<String, TileLayerOptions> layers) {
    layersMap = layers;
  }

  Map<String, TileLayerOptions> getBackgroundLayers() {
    return layersMap;
  }

  Future<void> getData(BuildContext context) async {
    // print("Data reload called");

    // var filterStateModel =
    //     Provider.of<FilterStateModel>(context, listen: false);

    // GET DATA FROM SERVER
    var notesList = await ServerApi.getRenderNotes(
        // surveyors: filterStateModel.surveyors,
        // projects: filterStateModel.projects,
        // matchString: filterStateModel.matchingText,
        // fromTo: filterStateModel.fromToTimestamp,
        );

    dataBounds = LatLngBounds();

    var logsList = await ServerApi.getGpslogs();

    // LOAD LOG DATA
    if (logsList != null) {
      // List<TaggedPolyline> lines = [];
      List<Polyline> lines = [];
      for (int i = 0; i < logsList.length; i++) {
        dynamic logItem = logsList[i];
        var id = logItem[ID];
        var name = logItem[NAME];
        var colorHex = logItem[COLOR];

        // TODO for now colortables are not supported
        const ECOLORSEP = "@";
        if (colorHex.contains(ECOLORSEP)) {
          var split = colorHex.split(ECOLORSEP);
          colorHex = split[0];
        }
        var width = logItem[WIDTH];
        var startts = logItem[STARTTS];
        var endts = logItem[ENDTS];

        var geom = logItem[THE_GEOM];
        JTS.LineString line = JTS.WKTReader().read(geom.split(";")[1]);
        var coordinates = line.getCoordinates();
        List<LatLng> points =
            coordinates.map((c) => LatLongHelper.fromLatLon(c.y, c.x)).toList();

        var env = line.getEnvelopeInternal();
        dataBounds
            .extend(LatLongHelper.fromLatLon(env.getMinY(), env.getMinX()));
        dataBounds
            .extend(LatLongHelper.fromLatLon(env.getMaxY(), env.getMaxX()));

        lines.add(
          Polyline(
            points: points,
            strokeWidth: width,
            color: ColorExt(colorHex),
          ),
        );
        // lines.add(
        //   TaggedPolyline(
        //     tag: "$id@$name@$startts@$endts",
        //     points: points,
        //     strokeWidth: width,
        //     color: ColorExt(colorHex),
        //   ),
        // );
      }
      logs = PolylineLayerOptions(
        polylines: lines,
        polylineCulling: true,
      );
      // logs = TappablePolylineLayerOptions(
      //   polylines: lines,
      //   polylineCulling: true,
      //   onTap: (List<TaggedPolyline> polylines, TapUpDetails details) {
      //     if (polylines.isEmpty) {
      //       return null;
      //     }
      //     return openLogDialog(context, polylines[0].tag);
      //   },
      //   // onMiss: () => print("No polyline tapped"),
      // );
    }

    List<Marker> markers = <Marker>[];
    List<Attributes> attributesList = [];

    // LOAD SIMPLE IMAGES
    // List<dynamic> imagesList = json[IMAGES];
    // if (imagesList != null) {
    //   for (int i = 0; i < imagesList.length; i++) {
    //     dynamic imageItem = imagesList[i];
    //     var id = imageItem[ID];
    //     var dataId = imageItem[DATAID];
    //     var data = imageItem[DATA];
    //     var name = imageItem[NAME];
    //     var ts = imageItem[TS];
    //     var x = imageItem[X];
    //     var y = imageItem[Y];
    //     var latLng = LatLongHelper.fromLatLon(y, x);
    //     dataBounds.extend(latLng);
    //     var imgData = Base64Decoder().convert(data);
    //     var imageWidget = Image.memory(
    //       imgData,
    //       scale: 6.0,
    //     );
    //     markers.add(
    //         buildImage(this, screenHeight, x, y, name, dataId, imageWidget));

    //     var surveyor = imageItem[SURVEYOR];
    //     var project = imageItem[PROJECT];
    //     attributesList.add(Attributes()
    //       ..id = id
    //       ..marker = imageWidget
    //       ..point = latLng
    //       ..project = project
    //       ..text = name
    //       ..timeStamp = ts
    //       ..user = surveyor);
    //   }
    // }

    // LOAD ALL NOTES WITH SIMPLE INFOS
    // make sure that forms are loaded properly
    if (notesList != null && notesList.isNotEmpty) {
      for (int i = 0; i < notesList.length; i++) {
        Map<String, dynamic> noteItem = notesList[i];
        var id = noteItem[ID];
        var name = noteItem[TEXT];
        var geom = noteItem[THE_GEOM];
        JTS.Point point = JTS.WKTReader().read(geom.split(";")[1]);
        var latLng = LatLongHelper.fromLatLon(point.getY(), point.getX());
        dataBounds.extend(latLng);

        var marker = noteItem[MARKER];
        var size = noteItem[SIZE];
        var color = noteItem[COLOR];
        var iconData = getSmashIcon(marker);
        var colorExt = ColorExt(color);
        var icon = Icon(
          iconData,
          size: size,
          color: colorExt,
        );
        markers
            .add(buildSimpleNote(this, latLng, name, id, icon, size, colorExt));

        attributesList.add(Attributes()
          ..id = id
          ..marker = icon
          ..point = latLng
          ..text = name);
      }
    }

    mapMarkers = markers;
    attributes = attributesList;

    var delta = 0.01;
    if (mapMarkers.length > 0) {
      dataBounds = LatLngBounds(
        LatLongHelper.fromLatLon(
            dataBounds.south - delta, dataBounds.west - delta),
        LatLongHelper.fromLatLon(
            dataBounds.north + delta, dataBounds.east + delta),
      );
    } else {
      dataBounds = LatLngBounds(LatLng(-45, -90), LatLng(45, 90));
    }
  }
}
