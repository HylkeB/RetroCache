package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.di.CacheableRequestDependencyContainer
import io.github.hylkeb.retrocache.state.RequestState
import io.github.hylkeb.retrocache.utility.OpenForMocking
import io.github.hylkeb.susstatemachine.Transition
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import retrofit2.HttpException

@OpenForMocking
internal class RefreshFailedWithResponse<T>(
    override val result: T,
    override val dateTimeMillis: Long,
    override val fromCache: Boolean,
    override val exception: HttpException,
    private val dependencyContainer: CacheableRequestDependencyContainer<T>,
) : InternalRequestState.Data.RefreshFailed<T>(), RequestState.Data.RefreshFailed.WithResponse<T> {

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