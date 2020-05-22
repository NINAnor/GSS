import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';

const MAPSFORGE = "Mapsforge";

final AVAILABLE_LAYERS_MAP = {
  '${MAPSFORGE}': TileLayerOptions(
    tms: false,
    urlTemplate: '$WEBAPP_URL/tiles/mapsforge/{z}/{x}/{y}',
    tileProvider: NonCachingNetworkTileProvider(),
  ),
  'Openstreetmap': TileLayerOptions(
    tms: false,
    subdomains: const ['a', 'b', 'c'],
    maxZoom: 19,
    urlTemplate: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    tileProvider: NonCachingNetworkTileProvider(),
  ),
  'OpenTopoMap': TileLayerOptions(
    tms: false,
    maxZoom: 19,
    subdomains: const ['a', 'b', 'c'],
    urlTemplate: "https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png",
    tileProvider: NonCachingNetworkTileProvider(),
  ),
  'Stamen Watercolor': TileLayerOptions(
    tms: false,
    maxZoom: 19,
    urlTemplate: "http://c.tile.stamen.com/watercolor/{z}/{x}/{y}.jpg",
    tileProvider: NonCachingNetworkTileProvider(),
  ),
  'Wikimedia Map': TileLayerOptions(
    tms: false,
    maxZoom: 19,
    urlTemplate: "https://maps.wikimedia.org/osm-intl/{z}/{x}/{y}.png",
    tileProvider: NonCachingNetworkTileProvider(),
  ),
};

//TileSource.Open_Stree_Map_Cicle({
//this.label: "Open Cicle Map",
//this.url: "https://tile.opencyclemap.org/cycle/{z}/{x}/{y}.png",
//this.attribution: "OpenStreetMap, ODbL",
//this.minZoom: 0,
//this.maxZoom: 19,
//this.isVisible: true,
//});
//
//TileSource.Open_Street_Map_HOT({
//this.label: "Open Street Map H.O.T.",
//this.url: "https://tile.openstreetmap.fr/hot/{z}/{x}/{y}.png",
//this.attribution: "OpenStreetMap, ODbL",
//this.minZoom: 0,
//this.maxZoom: 19,
//this.isVisible: true,
//this.isTms: false,
//});
