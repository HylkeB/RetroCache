package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.di.CacheableRequestDependencyContainer
import io.github.hylkeb.retrocache.state.RequestState
import io.github.hylkeb.retrocache.utility.OpenForMocking
import io.github.hylkeb.susstatemachine.Transition
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job

@OpenForMocking
internal class RefreshFailedWithException<T>(
    override val result: T,
    override val dateTimeMillis: Long,
    override val fromCache: Boolean,
    override val exception: Throwable,
    private val dependencyContainer: CacheableRequestDependencyContainer<T>,
) : InternalRequestState.Data.RefreshFailed<T>(), RequestState.Data.RefreshFailed.WithException<T> {

    private val retryRequested: CompletableJob = Job()

    override suspend fun enter(): Transition<InternalRequestState<T>> = withParentTransition(dependencyContainer) {
        retryRequested.join()
        return@withParentTransition Transition(Refreshing(result, dateTimeMillis, fromCache, dependencyContainer))
    }

    override suspend fun retry() {
        retryRequested.complete()
        awaitTransition()
    }
}