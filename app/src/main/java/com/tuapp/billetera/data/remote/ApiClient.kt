package com.tuapp.billetera.data.remote
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object TokenStore { @Volatile var token: String? = null }

// Para EMULADOR local: http://10.0.2.2:3000/
// Para CELULAR físico misma WiFi: http://TU_IP_PC:3000/  (ej. 192.168.1.15)
// Para PRODUCCIÓN Render: https://billetera-xxxx.onrender.com/
const val BASE_URL = "https://billetera-backend-ldne.onrender.com/"

object ApiClient {
    val api: ApiService by lazy { create(null) }

    fun create(token: String?): ApiService {
        val client = OkHttpClient.Builder()
            // Render may need a cold start; keep the loading state visible instead of timing out after 10s.
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(65, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val b = chain.request().newBuilder()
                token?.let { b.addHeader("Authorization", "Bearer $it") }
                chain.proceed(b.build())
            }
            .build()
        return Retrofit.Builder().baseUrl(BASE_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ApiService::class.java)
    }
}
