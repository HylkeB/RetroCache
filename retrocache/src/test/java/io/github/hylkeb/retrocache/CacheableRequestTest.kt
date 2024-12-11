package io.github.hylkeb.retrocache

import dev.mokkery.answering.returns
import dev.mokkery.every
import io.github.hylkeb.retrocache.state.internal.InternalRequestState
import io.github.hylkeb.susstatemachine.StateMachine
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.github.hylkeb.retrocache.state.internal.ErrorWithException
import io.github.hylkeb.retrocache.state.internal.Fetching
import io.github.hylkeb.retrocache.state.internal.Idle
import io.github.hylkeb.retrocache.utility.OpenForMocking
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class CacheableRequestTest {

    private val cacheableRequestStateMachine = mock<StateMachine<InternalRequestState<String>>>()
    private val idleState = mock<Idle<String>>()
    private val fetchingState = mock<Fetching<String>>()
    private val errorState = mock<ErrorWithException<String>>()

    private val dependencyContainer = MockCacheableRequestDependencyContainer(
        _requestStateMachine = cacheableRequestStateMachine
    )

    @Test
    fun `getData(false) called when starts in Idle state assert Fetching`() = runTest {
        // Arrange
        val fakeException = Exception("fake exception")
        val fakeStateFlow = MutableSharedFlow<InternalRequestState<String>>()
        every { cacheableRequestStateMachine.stateFlow } returns fakeStateFlow
        every { errorState.exception } returns fakeException
        val request = CacheableRequest(backgroundScope, dependencyContainer)

        // Act
        val result = async { request.getData(false) }
        runCurrent()
        fakeStateFlow.emit(idleState)
        fakeStateFlow.emit(fetchingState)
        fakeStateFlow.emit(errorState)

        // Assert
        result.await().shouldBeFailure().shouldBe(fakeException)
        verifySuspend {
            idleState.fetch(false)
            errorState.exception
        }
    }
}