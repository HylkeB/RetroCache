package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.CacheableCall
import io.github.hylkeb.retrocache.CacheableRequest
import io.github.hylkeb.susstatemachine.StateImpl
import io.github.hylkeb.susstatemachine.Transition
import retrofit2.HttpException
import retrofit2.awaitResponse

internal class RefreshingImpl<T>(
    override val result: T,
    override val dateTimeMillis: Long,
    override val fromCache: Boolean,
    private val cacheableRequest: CacheableRequest<T>,
) : StateImpl<InternalRequestState<T>>(), InternalRequestState.Data.Refreshing<T> {

    override suspend fun enter(): Transition<InternalRequestState<T>> = withParentTransition(cacheableRequest) {
        val call = if (cacheableRequest.cacheConfiguration.persistedCache) {
            CacheableCall(
                cacheableRequest.call.clone(),
                true,
                cacheableRequest.cacheProvider,
                cacheableRequest.dateTimeProvider,
                cacheableRequest.responseBodyConverter,
                cacheableRequest.cacheConfiguration,
            )
        } else {
            cacheableRequest.call.clone()
        }

        return@withParentTransition try {
            val response = call.awaitResponse()
            val data = response.body()
            if (response.isSuccessful && data != null) {
                val fromCache = (call as? CacheableCall)?.fromCache ?: false
                val responseDateTime = (call as? CacheableCall)?.responseDateTime ?: cacheableRequest.dateTimeProvider.currentTimeMillis()
                Transition(SuccessImpl(data, responseDateTime, fromCache, cacheableRequest))
            } else {
                Transition(
                    RefreshFailedWithResponseImpl(
                        result,
                        dateTimeMillis,
                        fromCache,
                        HttpException(response),
                        cacheableRequest
                    )
                )
            }
        } catch (exception: Throwable) {
            Transition(
                RefreshFailedWithExceptionImpl(
                    result,
                    dateTimeMillis,
                    fromCache,
                    exception,
                    cacheableRequest
                )
            )
        }
    }
}