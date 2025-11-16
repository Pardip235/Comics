package com.bpn.comics.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Comic data model representing an xkcd comic.
 * This model matches the xkcd API response structure.
 */
@Serializable
data class Comic(
    val num: Int,
    val title: String,
    val img: String,
    val alt: String,
    @SerialName("year")
    val year: String,
    @SerialName("month")
    val month: String,
    @SerialName("day")
    val day: String,
    val link: String? = null,
    val news: String? = null,
    @SerialName("safe_title")
    val safe_title: String = title,
    val transcript: String = "",
)
