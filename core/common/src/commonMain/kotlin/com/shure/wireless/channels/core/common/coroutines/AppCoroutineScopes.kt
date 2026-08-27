package com.shure.wireless.channels.core.common.coroutines

import com.shure.wireless.channels.core.common.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

class AppCoroutineScope : CoroutineScope {
    private val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(TAG, "Unhandled application coroutine", throwable)
    }

    override val coroutineContext: CoroutineContext =
        Dispatchers.Main + job + exceptionHandler

    fun close() {
        coroutineContext.cancel()
    }

    private companion object {
        const val TAG = "AppCoroutineScope"
    }
}

class AppBackgroundCoroutineScope : CoroutineScope {
    private val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(TAG, "Unhandled background coroutine", throwable)
    }

    override val coroutineContext: CoroutineContext =
        Dispatchers.Default + job + exceptionHandler

    fun close() {
        coroutineContext.cancel()
    }

    private companion object {
        const val TAG = "AppBackgroundScope"
    }
}
