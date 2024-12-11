package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.CacheableCall
import io.github.hylkeb.retrocache.di.CacheableRequestDependencyContainer
import io.github.hylkeb.retrocache.state.RequestState
import io.github.hylkeb.retrocache.utility.OpenForMocking
import io.github.hylkeb.susstatemachine.Transition
import retrofit2.HttpException
import retrofit2.awaitResponse

@OpenForMocking
internal class Fetching<T>(
    private val forceRefresh: Boolean,
    private val dependencyContainer: CacheableRequestDependencyContainer<T>
) : InternalRequestState<T>(), RequestState.Fetching<T> {

    override suspend fun enter(): Transition<InternalRequestState<T>> {
        val call = if (dependencyContainer.cacheConfiguration.persistedCache) {
            CacheableCall(
                dependencyContainer.call.clone(),
                forceRefresh,
                dependencyContainer.cacheProvider,
                dependencyContainer.dateTimeProvider,
                dependencyContainer.responseBodyConverter,
                dependencyContainer.cacheConfiguration
            )
        } else {
            dependencyContainer.call.clone()
        }

        return try {
            val response = call.awaitResponse()
            val data = response.body()
            if (response.isSuccessful && data != null) {
                val fromCache = (call as? CacheableCall)?.fromCache ?: false
                val responseDateTime = (call as? CacheableCall)?.responseDateTime ?: dependencyContainer.dateTimeProvider.currentTimeMillis()
                // TODO should this class be responsible for recognizing cached data is expired? thus doing the remote call? Probaly so.
                Transition(Success(data, responseDateTime, fromCache, dependencyContainer))
            } else {
                Transition(ErrorWithResponse(HttpException(response), dependencyContainer))
            }
        } catch (exception: Throwable) {
            Transition(ErrorWithException(exception, dependencyContainer))
        }
    }
}