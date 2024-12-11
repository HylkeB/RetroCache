package io.github.hylkeb.retrocache

import io.github.hylkeb.susstatemachine.StateObserver
import java.lang.reflect.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter

internal class CacheableRequestCallAdapter<R>(
    private val responseType: Type,
    private val coroutineScope: CoroutineScope,
    private val dateTimeProvider: DateTimeProvider,
    private val responseBodyConverter: Converter<ResponseBody, R>,
    private val cacheProvider: CacheProvider,
    private val requestStateObserver: StateObserver?,
) : CallAdapter<R, CacheableRequest<R>> {
    override fun responseType(): Type = responseType
    override fun adapt(call: Call<R>): CacheableRequest<R> {
        val cacheConfiguration = call.request().tag(CacheConfiguration::class.java) ?: CacheConfiguration.inMemoryCache
        return CacheableRequest(
            coroutineScope,
            call,
            cacheConfiguration,
            dateTimeProvider,
            responseBodyConverter,
            cacheProvider,
            requestStateObserver,
        )
    }
}