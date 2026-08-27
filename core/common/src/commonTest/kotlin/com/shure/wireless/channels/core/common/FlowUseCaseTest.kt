package com.shure.wireless.channels.core.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class FlowUseCaseTest {
    @Test
    fun emitsLoadingThenSuccess() = runTest {
        val useCase = EchoUseCase()

        val states = useCase("receiver").toList()

        assertTrue(states.first().isLoading)
        assertEquals("receiver", states.last().data)
    }

    @Test
    fun convoStyleParameterizedUseCaseEmitsLoadingThenCompleted() = runTest {
        val useCase = ConvoStyleEchoUseCase()

        val states = useCase.execute("Echo", "ULXD4").toList()

        assertTrue(states.first() is Loading)
        assertTrue(states.last() is Completed)
        assertEquals("ULXD4", states.last().data)
    }
}

private class EchoUseCase : FlowUseCase<String, String>(Dispatchers.Unconfined) {
    override suspend fun execute(params: String): UseCaseResult<String> = Result.success(params)
}

private class ConvoStyleEchoUseCase : FlowUseCase3<String, String>(Dispatchers.Unconfined) {
    override suspend fun executeInternal(params: String): Completed<String> = success(params)
}
