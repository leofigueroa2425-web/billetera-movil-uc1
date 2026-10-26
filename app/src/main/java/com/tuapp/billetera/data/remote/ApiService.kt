package com.tuapp.billetera.data.remote
import retrofit2.http.*
interface ApiService {
    @POST("api/login") suspend fun login(@Body b: LoginReq): LoginRes
    @POST("api/deposit") suspend fun deposit(@Body b: AmountReq): Receipt
    @POST("api/withdraw") suspend fun withdraw(@Body b: AmountReq): Receipt
    @POST("api/transfer") suspend fun transfer(@Body b: TransferReq): Receipt
    @POST("api/pay-service") suspend fun payService(@Body b: PayReq): Receipt
    @GET("api/balance") suspend fun balance(): BalanceRes
}
