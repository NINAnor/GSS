import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';

const DEFAULTMAP = "Openstreetmap";

final AVAILABLE_MAPS = {
  'Mapsforge': TileLayerOptions(
    tms: false,
    urlTemplate: '$WEBAPP_URL/tiles/mapsforge/{z}/{x}/{y}',
    tileProvider: NonCachingNetworkTileProvider(),
    maxZoom: 25,
    maxNativeZoom: 25,
  ),
  DEFAULTMAP: TileLayerOptions(
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
