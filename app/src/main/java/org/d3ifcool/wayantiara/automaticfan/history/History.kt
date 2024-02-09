package org.d3ifcool.wayantiara.automaticfan.history

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class History(
    val waktu: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class ResponseHistori(
    @Json(name = "kode")
    val kode: String,

    @Json(name = "pesan")
    val pesan: String,

    @Json(name = "result")
    val result: List<History>
)
