package io.github.hylkeb.retrocache

import io.github.hylkeb.susstatemachine.StateObserver
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import retrofit2.CallAdapter
import retrofit2.Retrofit

/**
 * TODO documentation
 */
class CacheableRequestCallAdapterFactory(
    private val cacheProvider: CacheProvider = CacheProvider.NoCache,
    private val dateTimeProvider: DateTimeProvider = DateTimeProvider.fromSystem(),
    private val requestStateObserver: StateObserver? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO),
) : CallAdapter.Factory() {

    @Deprecated("Use constructor with coroutine scope")
    constructor(
        parentJob: Job,
        cacheProvider: CacheProvider = CacheProvider.NoCache,
        dateTimeProvider: DateTimeProvider = DateTimeProvider.fromSystem(),
        requestStateObserver: StateObserver? = null,
    ) : this(cacheProvider, dateTimeProvider, requestStateObserver, CoroutineScope(parentJob + Dispatchers.IO))

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
            coroutineScope,
            dateTimeProvider,
            responseBodyConverter,
            cacheProvider,
            requestStateObserver
        )
    }
}