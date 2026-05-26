package com.example.zooapp.repository

import android.util.Log
import com.example.zooapp.network.LoginRequest
import com.example.zooapp.network.LoginResponse
import com.example.zooapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object UserRepository {
    fun loginUsuario(email: String, password: String, onResult: (LoginResponse?) -> Unit) {
        val request = LoginRequest(email, password)
        RetrofitClient.instance.loginUsuario(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) onResult(response.body())
                else onResult(null)
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("UserRepository", "Error loginUsuario: ${t.message}")
                onResult(null)
            }
        })
    }
}