package com.greenwallet.core.base

import androidx.lifecycle.ViewModel
import com.greenwallet.core.ext.FlowEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<S, E>(initialState: S) : ViewModel() {

    protected val _viewState = MutableStateFlow(initialState)
    val viewState = _viewState.asStateFlow()

    private val _event = FlowEvent<E>()
    val event = _event.asSharedFlow()

    protected open fun setEvent(e: E) {
        _event.tryEmit(e)
    }

}