package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.CacheableCall
import io.github.hylkeb.retrocache.CacheableRequest
import io.github.hylkeb.susstatemachine.StateImpl
import io.github.hylkeb.susstatemachine.Transition
import retrofit2.HttpException
import retrofit2.awaitResponse

internal class FetchingImpl<T>(
    private val forceRefresh: Boolean,
    private val cacheableRequest: CacheableRequest<T>,
) : StateImpl<InternalRequestState<T>>(), InternalRequestState.Fetching<T> {

    override suspend fun enter(): Transition<InternalRequestState<T>> {
        val call = if (cacheableRequest.cacheConfiguration.persistedCache) {
            CacheableCall(
                cacheableRequest.call.clone(),
                forceRefresh,
                cacheableRequest.cacheProvider,
                cacheableRequest.dateTimeProvider,
                cacheableRequest.responseBodyConverter,
                cacheableRequest.cacheConfiguration
            )
        } else {
            cacheableRequest.call.clone()
        }

        return try {
            val response = call.awaitResponse()
            val data = response.body()
            if (response.isSuccessful && data != null) {
                val fromCache = (call as? CacheableCall)?.fromCache ?: false
                val responseDateTime = (call as? CacheableCall)?.responseDateTime ?: cacheableRequest.dateTimeProvider.currentTimeMillis()
                // TODO should this class be responsible for recognizing cached data is expired? thus doing the remote call? Probaly so.
                Transition(SuccessImpl(data, responseDateTime, fromCache, cacheableRequest))
            } else {
                Transition(ErrorWithResponseImpl(HttpException(response), cacheableRequest))
            }
        } catch (exception: Throwable) {
            Transition(ErrorWithExceptionImpl(exception, cacheableRequest))
        }
    }
}