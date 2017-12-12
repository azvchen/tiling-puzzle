package server

import Tile
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import textToTile


class TileAdapter {

    @ToJson
    fun toJson(tile: Tile): Tile.SerializableTile {
        return Tile.SerializableTile.fromTile(tile)
    }

    @FromJson
    fun fromJson(json: String): Tile {
        return textToTile(json)
    }
}
