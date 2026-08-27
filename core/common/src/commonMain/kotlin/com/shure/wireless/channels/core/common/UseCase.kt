package com.shure.wireless.channels.core.common

import com.shure.wireless.channels.core.common.loading
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

typealias UseCaseResult<T> = Result<T, Failure>

data object NoParams

/**
 * Convo-style parameterless use case. Calling [execute] returns a cold flow that
 * emits [Loading] followed by a single [Completed] result.
 */
abstract class FlowUseCase2<T>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    fun execute(useCase: String): Flow<AsyncOperation<T>> = flow<AsyncOperation<T>> {
        Logger.d(useCase.ifBlank { TAG }, "Started")
        emit(loading<T>())
        emit(executeSafely(useCase) { executeInternal() })
    }

    protected abstract suspend fun executeInternal(): Completed<T>

    private suspend fun executeSafely(
        useCase: String,
        block: suspend () -> Completed<T>,
    ): Completed<T> = try {
        withContext(dispatcher) { block() }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Logger.e(useCase.ifBlank { TAG }, "Failed: ${throwable.message}", throwable)
        failure(ExceptionFailure(throwable))
    }

    private companion object {
        const val TAG = "FlowUseCase2"
    }
}

/** Convo-style parameterized flow use case. */
abstract class FlowUseCase3<T, P>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    fun execute(
        useCase: String,
        params: P,
    ): Flow<AsyncOperation<T>> = flow<AsyncOperation<T>> {
        Logger.d(useCase.ifBlank { TAG }, "Started")
        emit(loading<T>())
        emit(executeSafely(useCase, params))
    }

    protected abstract suspend fun executeInternal(params: P): Completed<T>

    private suspend fun executeSafely(useCase: String, params: P): Completed<T> = try {
        withContext(dispatcher) { executeInternal(params) }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Logger.e(useCase.ifBlank { TAG }, "Failed: ${throwable.message}", throwable)
        failure(ExceptionFailure(throwable))
    }

    private companion object {
        const val TAG = "FlowUseCase3"
    }
}

/** Convo-style flow use case for operations that also require a platform context. */
abstract class FlowContextUseCase<T, Context, P>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    fun execute(
        useCase: String,
        context: Context,
        params: P,
    ): Flow<AsyncOperation<T>> = flow<AsyncOperation<T>> {
        Logger.d(useCase.ifBlank { TAG }, "Started")
        emit(loading<T>())
        emit(executeSafely(useCase, context, params))
    }

    protected abstract suspend fun executeInternal(context: Context, params: P): Completed<T>

    private suspend fun executeSafely(
        useCase: String,
        context: Context,
        params: P,
    ): Completed<T> = try {
        withContext(dispatcher) { executeInternal(context, params) }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Logger.e(useCase.ifBlank { TAG }, "Failed: ${throwable.message}", throwable)
        failure(ExceptionFailure(throwable))
    }

    private companion object {
        const val TAG = "FlowContextUseCase"
    }
}

/** Convo-style use case for callers that only need the final result. */
interface ResultUseCase<T> {
    suspend fun execute(): UseCaseResult<T>
}

abstract class SuspendingUseCase<in Params, out Output>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    suspend operator fun invoke(params: Params): UseCaseResult<Output> =
        try {
            withContext(dispatcher) { execute(params) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Logger.e(tag, "Use case failed: ${throwable.message}", throwable)
            Result.failure(ExceptionFailure(throwable))
        }

    protected abstract suspend fun execute(params: Params): UseCaseResult<Output>

    protected open val tag: String = "SuspendingUseCase"
}

abstract class FlowUseCase<in Params, out Output>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    operator fun invoke(params: Params): Flow<AsyncOperation<Output>> = flow<AsyncOperation<Output>> {
        emit(execute(params).toAsyncOperation())
    }.onStart {
        emit(loading<Output>())
    }.catch { throwable ->
        if (throwable is CancellationException) throw throwable
        Logger.e(tag, "Use case failed: ${throwable.message}", throwable)
        emit(failure<Output>(ExceptionFailure(throwable)))
    }.flowOn(dispatcher)

    protected abstract suspend fun execute(params: Params): UseCaseResult<Output>

    protected open val tag: String = "FlowUseCase"
}

abstract class ObservableUseCase<in Params, out Output>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    operator fun invoke(params: Params): Flow<AsyncOperation<Output>> =
        observe(params)
            .map<Output, AsyncOperation<Output>> { success(it) }
            .onStart { emit(loading<Output>()) }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                Logger.e(tag, "Observable use case failed: ${throwable.message}", throwable)
                emit(failure<Output>(ExceptionFailure(throwable)))
            }
            .flowOn(dispatcher)

    protected abstract fun observe(params: Params): Flow<Output>

    protected open val tag: String = "ObservableUseCase"
}
