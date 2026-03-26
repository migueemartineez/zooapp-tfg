package com.example.zooapp

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("users")
    fun crearUsuario(@Body user: User): Call<User>

    @GET("users")
    fun obtenerUsuarios(): Call<List<User>>

    @DELETE("users/{id}")
    fun eliminarUsuario(@Path("id") id: String): Call<ResponseBody>
}

