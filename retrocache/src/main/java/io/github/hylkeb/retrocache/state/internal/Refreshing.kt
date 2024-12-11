package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.CacheableCall
import io.github.hylkeb.retrocache.di.CacheableRequestDependencyContainer
import io.github.hylkeb.retrocache.state.RequestState
import io.github.hylkeb.retrocache.utility.OpenForMocking
import io.github.hylkeb.susstatemachine.Transition
import retrofit2.HttpException
import retrofit2.awaitResponse

@OpenForMocking
internal class Refreshing<T>(
    override val result: T,
    override val dateTimeMillis: Long,
    override val fromCache: Boolean,
    private val dependencyContainer: CacheableRequestDependencyContainer<T>,
) : InternalRequestState.Data<T>(), RequestState.Data.Refreshing<T> {

    override suspend fun enter(): Transition<InternalRequestState<T>> = withParentTransition(dependencyContainer) {
        val call = if (dependencyContainer.cacheConfiguration.persistedCache) {
            CacheableCall(
                dependencyContainer.call.clone(),
                true,
                dependencyContainer.cacheProvider,
                dependencyContainer.dateTimeProvider,
                dependencyContainer.responseBodyConverter,
                dependencyContainer.cacheConfiguration,
            )
        } else {
            dependencyContainer.call.clone()
        }

        return@withParentTransition try {
            val response = call.awaitResponse()
            val data = response.body()
            if (response.isSuccessful && data != null) {
                val fromCache = (call as? CacheableCall)?.fromCache ?: false
                val responseDateTime = (call as? CacheableCall)?.responseDateTime ?: dependencyContainer.dateTimeProvider.currentTimeMillis()
                Transition(Success(data, responseDateTime, fromCache, dependencyContainer))
            } else {
                Transition(
                    RefreshFailedWithResponse(
                        result,
                        dateTimeMillis,
                        fromCache,
                        HttpException(response),
                        dependencyContainer
                    )
                )
            }
        } catch (exception: Throwable) {
            Transition(
                RefreshFailedWithException(
                    result,
                    dateTimeMillis,
                    fromCache,
                    exception,
                    dependencyContainer
                )
            )
        }
    }
}