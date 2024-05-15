package io.github.hylkeb.retrocache

import io.github.hylkeb.susstatemachine.StateObserver
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlinx.coroutines.Job
import retrofit2.CallAdapter
import retrofit2.Retrofit

/**
 * TODO documentation
 */
class CacheableRequestCallAdapterFactory(
    private val parentJob: Job = Job(),
    private val cacheProvider: CacheProvider = CacheProvider.NoCache,
    private val dateTimeProvider: DateTimeProvider = DateTimeProvider.fromSystem(),
    private val requestStateObserver: StateObserver? = null,
) : CallAdapter.Factory() {
    override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val rawType = getRawType(returnType)
        if (rawType != CacheableRequest::class.java) {
            return null
        }

        // TODO since we do need an interceptor, warn if no caching interceptor is added? Or do we warn that only once an CacheConfig says we want to persist?
        //  probably the latter; so give a boolean which indicates if the CachingInterceptor is added.

        val observableType = getParameterUpperBound(0, returnType as ParameterizedType)
        val responseBodyConverter = retrofit.nextResponseBodyConverter<Any>(null, observableType, annotations)
        return CacheableRequestCallAdapter<Any>(
            observableType,
            parentJob,
            dateTimeProvider,
            responseBodyConverter,
            cacheProvider,
            requestStateObserver
        )
    }
}