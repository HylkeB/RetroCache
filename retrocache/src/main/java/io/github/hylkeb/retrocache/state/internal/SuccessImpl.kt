package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.CacheableRequest
import io.github.hylkeb.susstatemachine.StateImpl
import io.github.hylkeb.susstatemachine.Transition
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select


internal class SuccessImpl<T>(
    override val result: T,
    override val dateTimeMillis: Long,
    override val fromCache: Boolean,
    private val cacheableRequest: CacheableRequest<T>,
) : StateImpl<InternalRequestState<T>>(), InternalRequestState.Data.Success<T> {

    private val refreshRequested: CompletableJob = Job()

    override suspend fun enter(): Transition<InternalRequestState<T>> = withParentTransition(cacheableRequest) {
        val age = getAge(cacheableRequest.dateTimeProvider.currentTimeMillis())

        val staleIn = cacheableRequest.cacheConfiguration.staleDuration - age
        val staleTimer = launch { delay(staleIn.inWholeMilliseconds) }

        return@withParentTransition select {
            refreshRequested.onJoin {
                staleTimer.cancel()
                Transition(RefreshingImpl(result, dateTimeMillis, fromCache, cacheableRequest))
            }
            staleTimer.onJoin {
                refreshRequested.cancel()
                Transition(RefreshingImpl(result, dateTimeMillis, fromCache, cacheableRequest), "Data became stale (${cacheableRequest.cacheConfiguration.staleDuration})")
            }
        }
    }

    override suspend fun refresh() {
        refreshRequested.complete()
        awaitTransition()
    }
}