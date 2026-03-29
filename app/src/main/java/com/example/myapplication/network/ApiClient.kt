package com.example.myapplication.network

import com.example.myapplication.data.AuthManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClient {

    // L'émulateur Android utilise 10.0.2.2 pour accéder à localhost du PC
    // Sur téléphone physique connecté au réseau local (Wi-Fi), il faut utiliser l'IP locale du PC
    private const val BASE_URL = "https://10.245.216.187:4000/"





    private fun getUnsafeTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    private fun buildOkHttpClient(authManager: AuthManager): OkHttpClient {
        val trustManagers: Array<TrustManager> = arrayOf(getUnsafeTrustManager())
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustManagers, SecureRandom())
        }
        val cookieStore = mutableMapOf<String, List<Cookie>>()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, getUnsafeTrustManager())
            .hostnameVerifier { _, _ -> true } // Dev uniquement
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url.host] = cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url.host].orEmpty()
                }
            })
            .addInterceptor { chain ->
                chain.proceed(chain.request())
            }
            .addInterceptor(logging)
            .build()
    }

    fun create(authManager: AuthManager): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(buildOkHttpClient(authManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
