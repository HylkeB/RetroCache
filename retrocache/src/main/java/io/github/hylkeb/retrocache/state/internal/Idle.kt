package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.di.CacheableRequestDependencyContainer
import io.github.hylkeb.retrocache.utility.OpenForMocking
import io.github.hylkeb.susstatemachine.Transition
import kotlinx.coroutines.CompletableDeferred

@OpenForMocking
internal class Idle<T>(
    private val dependencyContainer: CacheableRequestDependencyContainer<T>
) : InternalRequestState<T>() {

    private val fetchRequested: CompletableDeferred<Boolean> = CompletableDeferred()

    override suspend fun enter(): Transition<InternalRequestState<T>> {
        return Transition(Fetching(fetchRequested.await(), dependencyContainer))
    }

    suspend fun fetch(forceRefresh: Boolean) {
        fetchRequested.complete(forceRefresh)
        awaitTransition()
    }
}