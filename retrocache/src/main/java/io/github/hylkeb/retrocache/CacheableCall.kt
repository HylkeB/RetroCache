package io.github.hylkeb.retrocache

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

internal class CacheableCall<R>(
    private val remoteCall: Call<R>,
    private val forceRefresh: Boolean,
    private val cacheProvider: CacheProvider,
    private val dateTimeProvider: DateTimeProvider,
    private val responseBodyConverter: Converter<ResponseBody, R>,
    private val cacheConfiguration: CacheConfiguration,
) : Call<R> by remoteCall {

    var responseDateTime: Long = 0L
    var fromCache: Boolean = false

    override fun clone(): Call<R> = CacheableCall(
        remoteCall.clone(),
        forceRefresh,
        cacheProvider,
        dateTimeProvider,
        responseBodyConverter,
        cacheConfiguration
    )

    override fun execute(): Response<R> {
        throw RuntimeException("Execute should not have been called.")
    }

    override fun enqueue(callback: Callback<R>) {
        val request = request()
        if (forceRefresh) {
            return realEnqueue(callback)
        }

        val cachedResponse = cacheProvider.getCachedResponse(request) ?: return realEnqueue(callback)
        this.responseDateTime = cachedResponse.responseDate
        this.fromCache = true
        try {
            val body = cachedResponse.response.body!!
            val typedResponse = responseBodyConverter.convert(body)
            callback.onResponse(this, Response.success(typedResponse!!, cachedResponse.response))
        } catch (t: Throwable) {
            cacheProvider.deleteCachedResponse(request)
            callback.onFailure(this, t)
        }
    }

    private fun realEnqueue(callback: Callback<R>) {
        remoteCall.enqueue(object : Callback<R> {
            override fun onResponse(call: Call<R>, response: Response<R>) {
                if (response.isSuccessful && response.body() != null) {
                    this@CacheableCall.responseDateTime = dateTimeProvider.currentTimeMillis()
                    this@CacheableCall.fromCache = false
                }
                callback.onResponse(this@CacheableCall, response)
            }

            override fun onFailure(call: Call<R>, t: Throwable) {
                callback.onFailure(this@CacheableCall, t)
            }
        })
    }
}