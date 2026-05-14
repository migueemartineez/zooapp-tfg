package com.example.zooapp

import retrofit2.Call
import retrofit2.http.*

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val mensaje: String, val usuario: User)
data class RegistroResponse(val mensaje: String, val id: String)
data class VisitaRequest(
    val zonasVisitadas: List<String>,
    val animalesVistos: List<String>
)
data class PreferenciasRequest(
    val preferencias: Preferencias
)

interface ApiService {

    // Usuarios
    @POST("users/registro")
    fun registrarUsuario(@Body user: User): Call<RegistroResponse>

    @POST("users/login")
    fun loginUsuario(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("users/{id}")
    fun obtenerUsuario(@Path("id") id: String): Call<User>

    @POST("users/{id}/visita")
    fun anadirVisita(@Path("id") id: String, @Body visita: VisitaRequest): Call<User>

    @PUT("users/{id}/preferencias")
    fun actualizarPreferencias(@Path("id") id: String, @Body preferencias: PreferenciasRequest): Call<User>

    // Zonas
    @GET("zonas")
    fun obtenerZonas(): Call<List<Zona>>

    // Animales
    @GET("animales")
    fun obtenerAnimales(): Call<List<Animal>>

    @GET("animales/zona/{zonaId}")
    fun obtenerAnimalesPorZona(@Path("zonaId") zonaId: String): Call<List<Animal>>

    @GET("animales/{id}")
    fun obtenerAnimalPorId(@Path("id") id: String): Call<Animal>
}