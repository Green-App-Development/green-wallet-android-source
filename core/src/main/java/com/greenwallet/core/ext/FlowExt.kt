package com.greenwallet.core.ext

import kotlinx.coroutines.flow.MutableSharedFlow


@Suppress("FunctionName")
fun <T> FlowEvent(replayCache: Int = 0): MutableSharedFlow<T> = MutableSharedFlow(replay = replayCache, extraBufferCapacity = 5)