package com.example.zooapp

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object UserRepository {
    fun crearUsuario(user: User, onResult: (User?) -> Unit) {
        RetrofitClient.instance.crearUsuario(user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    onResult(response.body()) // Devuelve el usuario con id real
                } else {
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("UserRepository", "Error crearUsuario: ${t.message}")
                onResult(null)
            }
        })
    }

    fun obtenerUsuarios(onResult: (List<User>) -> Unit) {
        RetrofitClient.instance.obtenerUsuarios().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    onResult(response.body() ?: emptyList())
                } else {
                    onResult(emptyList())
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("UserRepository", "Error obtenerUsuarios: ${t.message}")
                onResult(emptyList())
            }
        })
    }

    fun eliminarUsuario(userId: String?, onResult: (Boolean) -> Unit) {
        if (userId == null) {
            Log.e("UserRepository", "No se puede eliminar usuario sin ID")
            onResult(false)
            return
        }

        RetrofitClient.instance.eliminarUsuario(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onResult(response.isSuccessful)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("UserRepository", "Error eliminarUsuario: ${t.message}")
                onResult(false)
            }
        })
    }
}