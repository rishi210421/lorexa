package com.example.lorexa

import com.example.lorexa.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("chat/completions")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Header("HTTP-Referer") referer: String = "https://yourapp.com",
        @Header("X-Title") title: String = "Lorexa",
        @Body request: ChatRequest
    ): ChatResponse
}