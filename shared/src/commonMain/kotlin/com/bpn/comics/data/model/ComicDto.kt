package com.bpn.comics.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO that mirrors the XKCD API response.
 * Domain layers should never depend on this type directly.
 */
@Serializable
data class ComicDto(
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
    val transcript: String = ""
)

