package com.green.wallet.presentation.custom

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

class ConnectionFlow : Flow<Boolean> {



    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<Boolean>) {

    }
}
