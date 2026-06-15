package com.example.vecizervi.data.utils

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://vecizervi-backend.onrender.com/api/"

    // S04: token JWT guardado en memoria; se asigna tras el login exitoso
    var jwtToken: String? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // S04: interceptor que añade el header Authorization en cada petición
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val request = jwtToken?.let { token ->
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } ?: original
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)   // primero auth, luego logging
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val servicioUsuarios: UsuarioService = retrofit.create(UsuarioService::class.java)
    val servicioTrabajos: TrabajoService = retrofit.create(TrabajoService::class.java)
    val servicioMensajes: MensajeService = retrofit.create(MensajeService::class.java)
    val servicioResenas:  ResenaService  = retrofit.create(ResenaService::class.java)
}
