package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.di.CacheableRequestDependencyContainer
import io.github.hylkeb.retrocache.state.RequestState
import io.github.hylkeb.retrocache.utility.OpenForMocking
import io.github.hylkeb.susstatemachine.Transition
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

@OpenForMocking
internal class Success<T>(
    override val result: T,
    override val dateTimeMillis: Long,
    override val fromCache: Boolean,
    private val dependencyContainer: CacheableRequestDependencyContainer<T>,
) : InternalRequestState.Data<T>(), RequestState.Data.Success<T> {

    private val refreshRequested: CompletableJob = Job()

    override suspend fun enter(): Transition<InternalRequestState<T>> = withParentTransition(dependencyContainer) {
        val age = getAge(dependencyContainer.dateTimeProvider.currentTimeMillis())

        val staleIn = dependencyContainer.cacheConfiguration.staleDuration - age
        val staleTimer = launch { delay(staleIn.inWholeMilliseconds) }

        return@withParentTransition select {
            refreshRequested.onJoin {
                staleTimer.cancel()
                Transition(Refreshing(result, dateTimeMillis, fromCache, dependencyContainer))
            }
            staleTimer.onJoin {
                refreshRequested.cancel()
                Transition(Refreshing(result, dateTimeMillis, fromCache, dependencyContainer), "Data became stale (${dependencyContainer.cacheConfiguration.staleDuration})")
            }
        }
    }

    suspend fun refresh() {
        refreshRequested.complete()
        awaitTransition()
    }
}
