package server

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson


class PairAdapter {
    @ToJson
    fun toJson(pair: Pair<Int, Int>): String {
        return "${pair.second} ${pair.first}"
    }
    @FromJson
    fun fromJson(json: String): Pair<Int, Int> {
        val items = json.split(" ").map(Integer::parseInt)
        return items[1] to items[0]
    }
}
