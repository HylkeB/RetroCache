package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.CacheableRequest
import io.github.hylkeb.susstatemachine.StateImpl
import io.github.hylkeb.susstatemachine.Transition
import kotlinx.coroutines.CompletableDeferred

internal class IdleImpl<T>(
    private val cacheableRequest: CacheableRequest<T>
) : StateImpl<InternalRequestState<T>>(), InternalRequestState.Idle<T> {

    override val name: String = "Idle"

    private val fetchRequested: CompletableDeferred<Boolean> = CompletableDeferred()

    override suspend fun enter(): Transition<InternalRequestState<T>> {
        return Transition(FetchingImpl(fetchRequested.await(), cacheableRequest))
    }

    override suspend fun fetch(forceRefresh: Boolean) {
        fetchRequested.complete(forceRefresh)
        awaitTransition()
    }
}