package com.greenwallet.core.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


@Suppress("FunctionName")
fun <T> FlowEvent(replayCache: Int = 0): MutableSharedFlow<T> =
    MutableSharedFlow(replay = replayCache, extraBufferCapacity = 5)

inline fun <T> Flow<T>.collectFlow(
    scope: CoroutineScope,
    crossinline collector: suspend (T) -> Unit
) {
    scope.launch {
        collect {
            collector(it)
        }
    }
}
