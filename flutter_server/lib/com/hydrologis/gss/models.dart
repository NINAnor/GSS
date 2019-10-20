import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong/latlong.dart';
import 'package:flutter_server/com/hydrologis/gss/layers.dart';

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

  List<int> get fromToTimestamp => _fromToTimestamp;

  void setFromToTimestamp(List<int> fromToTimestamp) {
    _fromToTimestamp = fromToTimestamp;
    notifyListeners();
  }

  List<String> get projects => _projects;

  void setProjects(List<String> _projects) {
    _projects = projects;
    notifyListeners();
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
}

class MapstateModel extends ChangeNotifier {
//  String _backgroundLayer = MAPSFORGE;
//
//  double _centerLon = 11.0;
//  double _centerLat = 46.0;
//  double _currentZoom = 8;
//
//  String get backgroundLayer => _backgroundLayer;
//
//  set backgroundLayer(String backgroundLayer) {
//    _backgroundLayer = backgroundLayer;
//    notifyListeners();
//  }
//
//  setBackgroundLayerNoevent(String backgroundLayer) {
//    _backgroundLayer = backgroundLayer;
//    notifyListeners();
//  }
//
//  TileLayerOptions getBackgroundLayerOption() {
//    return AVAILABLE_LAYERS_MAP[_backgroundLayer] ??=
//        AVAILABLE_LAYERS_MAP[MAPSFORGE];
//  }
//
//  get centerLat => _centerLat;
//
//  get centerLon => _centerLon;
//
//  get currentZoom => _currentZoom;
//
//  void setMapPosition(double lon, double lat, double zoom) {
//    _centerLat = lat;
//    _centerLon = lon;
//    _currentZoom = zoom;
//    notifyListeners();
//  }
//
//  void setMapPositionNoEvent(double lon, double lat, double zoom) {
//    _centerLat = lat;
//    _centerLon = lon;
//    _currentZoom = zoom;
//  }
//
//  void reset() {
//    _backgroundLayer = MAPSFORGE;
//    notifyListeners();
//  }
}
