package io.github.hylkeb.retrocache

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

class CachingInterceptor(
    private val cacheProvider: CacheProvider,
    private val dateTimeProvider: DateTimeProvider = DateTimeProvider.fromSystem(),
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val cacheConfiguration = request.tag(Invocation::class.java)
            ?.arguments()?.firstOrNull { it is CacheConfiguration } as? CacheConfiguration
            ?: return chain.proceed(request)

        if (!cacheConfiguration.persistedCache) {
            return chain.proceed(request)
        }

        // Only use this to store cached responses.
        val response = chain.proceed(request)
        cacheProvider.storeResponseInCache(request, response, dateTimeProvider.currentTimeMillis())
        return response
    }
}