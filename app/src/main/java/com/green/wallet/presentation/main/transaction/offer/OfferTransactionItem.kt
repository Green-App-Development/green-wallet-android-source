package com.green.wallet.presentation.main.transaction.offer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.green.compose.dimens.size_30
import com.green.compose.dimens.size_5
import com.green.compose.dimens.text_15
import com.green.compose.progress.CircularProgressBar
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider


@Composable
fun OfferTransactionItem(
    heightCompose: Int = 45,
    onUpdate: (Int) -> Unit = {}
) {

    val size by remember { mutableIntStateOf(heightCompose) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(size.dp)
            .background(color = Provider.current.bcgTransactionItem)
            .padding(start = size_5)
    ) {

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)

        ) {
            DefaultText(
                text = "Take offer",
                size = text_15,
                color = Provider.current.blue,
                modifier = Modifier
                    .align(Alignment.CenterStart),
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)

        ) {
            CircularProgressBar(
                modifier = Modifier.align(Alignment.Center),
                size = size_30
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(2f)
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