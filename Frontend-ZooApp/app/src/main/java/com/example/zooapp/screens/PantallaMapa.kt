package com.example.zooapp.screens

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import com.example.zooapp.SesionUsuario
import com.example.zooapp.zonaIds
import com.example.zooapp.model.Animal
import com.example.zooapp.model.User
import com.example.zooapp.network.RetrofitClient
import com.example.zooapp.network.VisitaRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PantallaMapa(zonaActual: String, navController: NavController) {

    var animales by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    // Cuando cambia la zona por beacon
    LaunchedEffect(zonaActual) {
        if (zonaActual.isNotEmpty()) {
            val zonaId = zonaIds[zonaActual]
            if (zonaId != null) {
                RetrofitClient.instance.obtenerAnimalesPorZona(zonaId)
                    .enqueue(object : Callback<List<Animal>> {
                        override fun onResponse(
                            call: Call<List<Animal>>,
                            response: Response<List<Animal>>
                        ) {
                            animales = response.body() ?: emptyList()

                            // Guardar visita
                            val usuarioId = SesionUsuario.usuario?.id
                            if (usuarioId != null && animales.isNotEmpty() && zonaActual != SesionUsuario.ultimaZonaGuardada) {
                                SesionUsuario.ultimaZonaGuardada = zonaActual
                                val visita = VisitaRequest(
                                    zonasVisitadas = listOf(zonaActual),
                                    animalesVistos = emptyList()
                                )
                                RetrofitClient.instance.anadirVisita(usuarioId, visita)
                                    .enqueue(object : Callback<User> {
                                        override fun onResponse(call: Call<User>, response: Response<User>) {
                                            response.body()?.let { SesionUsuario.usuario = it }
                                        }
                                        override fun onFailure(call: Call<User>, t: Throwable) {}
                                    })
                            }

                            // Notificar zona activa al WebView
                            webView?.post {
                                webView?.evaluateJavascript(
                                    "window.setZonaActiva('${zonaActual.replace("'", "\\'")}');",
                                    null
                                )
                            }
                        }
                        override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                            animales = emptyList()
                        }
                    })
            }
        } else {
            animales = emptyList()
            webView?.post {
                webView?.evaluateJavascript("window.setZonaActiva('');", null)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor("#F5F1E8".toColorInt())
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (zonaActual.isNotEmpty()) {
                                view?.evaluateJavascript(
                                    "window.setZonaActiva('${zonaActual.replace("'", "\\'")}');",
                                    null
                                )
                            }
                        }
                    }

                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun verDetalleAnimal(nombreAnimal: String) {
                                val animal = animales.find { it.nombre == nombreAnimal }
                                    ?: SesionUsuario.todosLosAnimales?.find { it.nombre == nombreAnimal }
                                animal?.let {
                                    (context as? android.app.Activity)?.runOnUiThread {
                                        navController.navigate("detalle/${it.id}")
                                    }
                                }
                            }
                        },
                        "AndroidBridge"
                    )

                    loadUrl("file:///android_asset/bioparc_map.html")
                    webView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                webView = view
            }
        )
    }
}