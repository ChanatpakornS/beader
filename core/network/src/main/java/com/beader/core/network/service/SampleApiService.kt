package com.beader.core.network.service

import com.beader.core.network.model.SampleItemDto
import retrofit2.http.GET

interface SampleApiService {
    @GET("sample-items")
    suspend fun getSampleItems(): List<SampleItemDto>
}
