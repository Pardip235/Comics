package com.bpn.comics.domain.model

/**
 * Domain model representing a comic.
 * 
 * This is the business logic model used throughout the domain and presentation layers.
 * It includes the `isFavorite` field which is derived from the database.
 * 
 * @property num The comic number (unique identifier)
 * @property title The comic title
 * @property img URL to the comic image
 * @property alt Alt text for the image
 * @property year Publication year
 * @property month Publication month
 * @property day Publication day
 * @property link Optional link URL
 * @property news Optional news text
 * @property safeTitle Safe title (defaults to title)
 * @property transcript Comic transcript
 * @property isFavorite Whether the comic is marked as favorite (derived from database)
 */
data class Comic(
    val num: Int,
    val title: String,
    val img: String,
    val alt: String,
    val year: String,
    val month: String,
    val day: String,
    val link: String? = null,
    val news: String? = null,
    val safeTitle: String = title,
    val transcript: String = "",
    val isFavorite: Boolean = false
)

