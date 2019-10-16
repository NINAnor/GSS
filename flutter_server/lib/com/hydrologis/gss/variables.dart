import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong/latlong.dart';
import 'package:flutter_server/com/hydrologis/gss/layers.dart';

const TITLE = 'Geopaparazzi Survey Server';
const Color MAIN_COLOR = Colors.green;
final Color MAIN_COLOR_BACKGROUND = Colors.green[200];
const DEFAULT_PADDING = 15.0;
const DEFAULT_FONTSIZE = 48.0;
const CHART_FONTSIZE = 24;
const NOVALUE = " - ";
const ABOUTPAGE_INDEX = 1000;

const WEBAPP = 'http://localhost:8080';

const DEFAULT_GREY_COLOR = Colors.grey;
final TextStyle DEFAULT_GREYSTYLE =
    TextStyle(fontSize: DEFAULT_FONTSIZE, color: DEFAULT_GREY_COLOR);
final TextStyle DEFAULT_BLACKSTYLE =
    TextStyle(fontSize: DEFAULT_FONTSIZE, color: Colors.black);
final TextStyle DEFAULT_GREYSTYLE_SMALL =
    TextStyle(fontSize: DEFAULT_FONTSIZE / 2, color: DEFAULT_GREY_COLOR);

/// An ISO8601 date formatter (yyyy-MM-dd HH:mm:ss).
final DateFormat ISO8601_TS_FORMATTER = DateFormat("yyyy-MM-dd HH:mm:ss");

/// An ISO8601 time formatter (HH:mm:ss).
final DateFormat ISO8601_TS_TIME_FORMATTER = DateFormat("HH:mm:ss");

/// An ISO8601 day formatter (yyyy-MM-dd).
final DateFormat ISO8601_TS_DAY_FORMATTER = DateFormat("yyyy-MM-dd");

// API VARS START
final String Y = "y";
final String X = "x";
final String COORDS = "coords";
final String ENDTS = "endts";
final String STARTTS = "startts";
final String NAME = "name";
final String WIDTH = "width";
final String COLOR = "color";
final String ID = "id";
final String DATAID = "dataid";
final String DATA = "data";
final String LOGS = "logs";
final String NOTES = "notes";
final String FORMS = "forms";
final String FORM = "form";
final String IMAGES = "images";
final String TS = "ts";

// API VARS END

final ColorExt mainBackground = ColorExt("#ffFFFFFF");
final ColorExt mainDecorations = ColorExt("#ff1976d2");
final ColorExt mainDecorationsDark = ColorExt("#ff004ba0");

final DEFAULT_TILELAYER = AVAILABLE_LAYERS_MAP[MAPSFORGE]; //'Openstreetmap'];

class MapstateModel extends ChangeNotifier {
  TileLayerOptions _backgroundLayer = DEFAULT_TILELAYER;

  double _centerLon = 11.0;
  double _centerLat = 46.0;
  double _currentZoom = 8;

  TileLayerOptions get backgroundLayer => _backgroundLayer;

  set backgroundLayer(TileLayerOptions backgroundLayer) {
    _backgroundLayer = backgroundLayer;
    print("event backgroundLayer");
    notifyListeners();
  }

  get centerLat => _centerLat;

  get centerLon => _centerLon;

  get currentZoom => _currentZoom;

  void setMapPosition(double lon, double lat, double zoom) {
    _centerLat = lat;
    _centerLon = lon;
    _currentZoom = zoom;
    print("event setMapPosition");
    notifyListeners();
  }

  void reset() {
    _backgroundLayer = DEFAULT_TILELAYER;
    print("event reset");
    notifyListeners();
  }
}

/// The Flutter Color class Extended
///
/// A color class that also allows to use hex and wkt colors in the constructor.
class ColorExt extends Color {
  static int _getColorFromHex(String hexOrNamedColor) {
    if (hexOrNamedColor.startsWith("#")) {
      hexOrNamedColor = hexOrNamedColor.toUpperCase().replaceAll("#", "");
      if (hexOrNamedColor.length == 6) {
        hexOrNamedColor = "FF" + hexOrNamedColor;
      }
      return int.parse(hexOrNamedColor, radix: 16);
    } else {
      // compatibility with older geopaparazzi
      String colorName = hexOrNamedColor.toLowerCase();
      switch (colorName) {
        case "red":
          return int.parse("ffd32f2f", radix: 16); //
        case "pink":
          return int.parse("ffc2185b", radix: 16); //
        case "purple":
          return int.parse("ff7b1fa2", radix: 16); //
        case "deep_purple":
          return int.parse("ff512da8", radix: 16); //
        case "indigo":
          return int.parse("ff303f9f", radix: 16); //
        case "blue":
          return int.parse("ff1976d2", radix: 16); //
        case "light_blue":
          return int.parse("ff0288d1", radix: 16); //
        case "cyan":
          return int.parse("ff0097a7", radix: 16); //
        case "teal":
          return int.parse("ff00796b", radix: 16); //
        case "green":
          return int.parse("ff00796b", radix: 16); //
        case "light_green":
          return int.parse("ff689f38", radix: 16); //
        case "lime":
          return int.parse("ffafb42b", radix: 16); //
        case "yellow":
          return int.parse("fffbc02d", radix: 16); //
        case "amber":
          return int.parse("ffffa000", radix: 16); //
        case "orange":
          return int.parse("fff57c00", radix: 16); //
        case "deep_orange":
          return int.parse("ffe64a19", radix: 16); //
        case "brown":
          return int.parse("ff5d4037", radix: 16); //
        case "grey":
          return int.parse("ff616161", radix: 16); //
        case "blue_grey":
          return int.parse("ff455a64", radix: 16); //
        case "white":
          return int.parse("ffffffff", radix: 16); //
        case "almost_black":
          return int.parse("ff212121", radix: 16); //
        default:
          return Colors.red.value;
      }
    }
  }

  ColorExt(final String hexColor) : super(_getColorFromHex(hexColor));

  static ColorExt fromColor(Color color) {
    String hex = asHex(color);
    return ColorExt(hex);
  }

  static String asHex(Color color) {
    var hex = '#${color.value.toRadixString(16)}';
    return hex;
  }
}
