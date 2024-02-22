package com.green.wallet.presentation.main.transaction.offer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.dimens.size_45
import com.green.compose.dimens.text_30
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider


@Composable
fun OfferTransactionItem() {
    Column(
        modifier = Modifier
            .height(size_45)
            .background(color = Provider.current.bcgTransactionItem)
    ) {
        DefaultText(
            text = "Take offer",
            size = text_30,
            color = Color.Green,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
        ) {

        }

        Box(
            modifier = Modifier
        ) {

        }
    }
}


@Preview(showBackground = true)
@Composable
fun OfferTransactionItemPreview() {
    GreenWalletTheme {
        OfferTransactionItem()
    }
}