package com.android.greenapp.domain.entity

import com.android.greenapp.presentation.tools.Status
import com.android.greenapp.presentation.tools.Token

data class TransactionTemp(val status: Status, val height: Int, val amount: Double, val token: Token)