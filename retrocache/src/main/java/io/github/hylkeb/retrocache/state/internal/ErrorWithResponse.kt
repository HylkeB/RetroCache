package io.github.hylkeb.retrocache.state.internal

import io.github.hylkeb.retrocache.di.CacheableRequestDependencyContainer
import io.github.hylkeb.retrocache.state.RequestState
import io.github.hylkeb.retrocache.utility.OpenForMocking
import io.github.hylkeb.susstatemachine.Transition
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import retrofit2.HttpException

@OpenForMocking
internal class ErrorWithResponse<T>(
    override val exception: HttpException,
    private val dependencyContainer: CacheableRequestDependencyContainer<T>,
) : InternalRequestState.Error<T>(), RequestState.Error.WithResponse<T> {

    private val retryRequested: CompletableJob = Job()

    override suspend fun enter(): Transition<InternalRequestState<T>> {
        retryRequested.join()
        return Transition(Fetching(true, dependencyContainer))
    }

    override suspend fun retry() {
        retryRequested.complete()
        awaitTransition()
    }
}