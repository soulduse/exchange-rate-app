package com.dave.soul.exchange_app.core.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ExchangeApi {

    @GET("/api/v1/exchange/rates")
    suspend fun getRates(): BoardResponse

    @GET("/api/v1/exchange/rates/{code}/history")
    suspend fun getHistory(
        @Path("code") currencyCode: String,
        @Query("range") range: String,
    ): HistoryResponse

    @POST("/api/v1/exchange/devices")
    suspend fun registerDevice(@Body body: DeviceRegisterRequest): DeviceRegisterResponse

    @GET("/api/v1/exchange/alerts")
    suspend fun getAlerts(@Query("deviceId") deviceId: String): AlertListResponse

    @POST("/api/v1/exchange/alerts")
    suspend fun createAlert(@Body body: AlertCreateRequest): AlertDto

    @PATCH("/api/v1/exchange/alerts/{id}")
    suspend fun updateAlert(@Path("id") id: Long, @Body body: AlertUpdateRequest): AlertDto

    @DELETE("/api/v1/exchange/alerts/{id}")
    suspend fun deleteAlert(@Path("id") id: Long, @Query("deviceId") deviceId: String)

    // 공통 피드백 도메인 (모든 앱 공용) — 서버가 X-Package-Name 으로 app 역판별
    @Multipart
    @POST("/api/v1/feedback")
    suspend fun submitFeedback(
        @Header("X-Package-Name") packageName: String,
        @Part("category") category: RequestBody,
        @Part("content") content: RequestBody,
        @Part("deviceId") deviceId: RequestBody,
        @Part("appVersion") appVersion: RequestBody,
        @Part images: List<MultipartBody.Part> = emptyList(),
    )
}
