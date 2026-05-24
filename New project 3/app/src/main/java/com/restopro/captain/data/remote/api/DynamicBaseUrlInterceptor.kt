package com.restopro.captain.data.remote.api

import com.restopro.captain.utils.ServerConfigStore
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class DynamicBaseUrlInterceptor @Inject constructor(
    private val store: ServerConfigStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val base = store.blockingBaseUrl().toHttpUrl()
        val original = chain.request()
        val url = original.url.newBuilder()
            .scheme(base.scheme)
            .host(base.host)
            .port(base.port)
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}
