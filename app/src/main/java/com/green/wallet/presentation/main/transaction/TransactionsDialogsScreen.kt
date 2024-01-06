package com.green.wallet.presentation.main.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.dimens.size_100
import com.green.compose.theme.GreenWalletTheme
import com.green.wallet.presentation.main.transaction.states.CatDialogState


@Composable
fun SpeedyTransactionBtmCatScreen(
    catViewState: CatDialogState = CatDialogState()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(size_100)
            .background(color = Color.Blue)
    ) {

    }
}


@Preview(showBackground = true)
@Composable
fun TransactionComposeScreenPreview() {
    GreenWalletTheme {
        SpeedyTransactionBtmCatScreen()
    }
}


