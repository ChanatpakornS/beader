package com.beader.core.network.model

import kotlinx.serialization.Serializable

/**
 * Wire model — mirrors the API response shape exactly. `:core:data` maps
 * this to the domain model; nothing outside the data layer ever sees a DTO.
 */
@Serializable
data class SampleItemDto(
    val id: String,
    val title: String,
    val description: String,
)
