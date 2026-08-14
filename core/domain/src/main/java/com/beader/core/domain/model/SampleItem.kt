package com.beader.core.domain.model

/**
 * Pure domain model — no persistence or network annotations belong here.
 * `:core:data` maps its entity/DTO types to and from this at the repository
 * boundary so the domain and presentation layers never see them.
 */
data class SampleItem(
    val id: String,
    val title: String,
    val description: String,
    val isFavorite: Boolean = false,
)
