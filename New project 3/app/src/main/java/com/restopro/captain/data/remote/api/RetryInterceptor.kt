package com.restopro.captain.data.remote.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import kotlin.math.min

class RetryInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var last: IOException? = null
        while (attempt < 3) {
            try {
                return chain.proceed(chain.request())
            } catch (error: IOException) {
                last = error
                Thread.sleep(min(250L * (attempt + 1), 750L))
                attempt++
            }
        }
        throw last ?: IOException("Network retry failed")
    }
}
