package com.green.wallet.presentation.main.transaction

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.green.compose.dimens.size_4
import com.green.compose.dimens.size_45
import com.green.compose.dimens.size_5
import com.green.compose.dimens.text_15
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.domain.domainmodel.TransferTransaction
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.main.transaction.offer.OfferTransactionItem
import com.green.wallet.presentation.tools.Status
import de.charlex.compose.RevealDirection
import de.charlex.compose.RevealSwipe


@Composable
fun TransactionListScreen(
    state: TransactionState,
    onIntent: (TransactionIntent) -> Unit
) {

    val openedTrans = remember { mutableSetOf<String>() }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            itemsIndexed(state.transactionList) { index, item ->
                when (item) {
                    is TransferTransaction -> {
                        TransferTransactionItem(item, onIntent)
                    }

                    is OfferTransaction -> {
                        OfferTransactionItem(
                            state = OfferTransaction(
                                transId = "$index"
                            ),
                            initialOpen = openedTrans.contains("$index"),
                            onIntent = onIntent,
                            onDetailsOpen = {
                                if (it)
                                    openedTrans.add("$index")
                                else
                                    openedTrans.remove("$index")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransferTransactionItem(
    item: TransferTransaction,
    onIntent: (TransactionIntent) -> Unit
) {

    RevealSwipe(
        modifier = Modifier,
        directions = setOf(
            RevealDirection.EndToStart
        ),
        maxRevealDp = size_45,
        hiddenContentStart = {

        },
        hiddenContentEnd = {
            Box(modifier = Modifier.size(size_45)) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Provider.current.red)
                        .clickable {
                            onIntent(TransactionIntent.OnDeleteTransaction(item))
                        },
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = null
                )
            }
        },
//        backgroundCardEndColor = Provider.current.bcgTransactionItem
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(size_45)
                .background(color = Provider.current.bcgTransactionItem),
            shape = it,
            backgroundColor = Provider.current.bcgTransactionItem,
        ) {
            Row(
                modifier = Modifier
                    .background(color = Provider.current.bcgTransactionItem)
                    .padding(start = size_5)
                    .clickable {
                        onIntent(TransactionIntent.OnShowTransactionDetails(item))
                    }
            ) {

                val (txtStatus, clrStatus) = when (item.status) {
                    Status.Incoming -> {
                        stringResource(R.string.transactions_incoming) to Provider.current.green
                    }

                    Status.Outgoing -> {
                        stringResource(R.string.incoming_outgoing) to Provider.current.red
                    }

                    else -> {
                        stringResource(R.string.transactions_pendind) to Provider.current.blue
                    }
                }

                DefaultText(
                    text = txtStatus,
                    size = text_15,
                    color = clrStatus,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1.2f)
                        .wrapContentHeight(),
                    textAlign = TextAlign.Start
                )

                val infiniteTransition = rememberInfiniteTransition(label = "")
                val offsetAnimation by infiniteTransition.animateValue(
                    initialValue = 0.dp,
                    targetValue = 3.dp,
                    typeConverter = Dp.VectorConverter,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            300,
                            easing = LinearEasing, delayMillis = 600
                        ),
                        repeatMode = RepeatMode.Reverse
                    ), label = ""
                )

                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .then(
                            if (item.confirmedAtHeight == 0L)
                                Modifier
                                    .offset(offsetAnimation)
                                    .clickable {
                                        onIntent(TransactionIntent.OnSpeedyTran(item))
                                    }
                            else Modifier
                        )
                        .weight(1.2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {

                    val (txtHeight, heightClr) = if (item.confirmedAtHeight == 0L)
                        "Speed up" to Provider.current.blue
                    else item.confirmedAtHeight.toString() to Provider.current.secondaryTextColor

                    DefaultText(
                        text = txtHeight,
                        color = heightClr,
                        size = text_15
                    )

                    if (item.confirmedAtHeight == 0L) {
                        Image(
                            painter = painterResource(
                                R.drawable.ic_speed_up
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(
                                    top = size_4,
                                    start = size_4
                                ),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1.8f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    val amount = "${formattedDoubleAmountWithPrecision(item.amount)} ${
                        item.code
                    }"
                    DefaultText(
                        text = amount,
                        size = text_15,
                        color = Provider.current.secondaryTextColor,
                        modifier = Modifier
                            .padding(end = size_5)
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun TransferTransactionPreview() {
    GreenWalletTheme {

        TransactionListScreen(
            state = TransactionState(
                transactionList = listOf(
                    TransferTransaction(
                        "adasdfs",
                        0.0000323,
                        System.currentTimeMillis(),
                        0L,
                        status = Status.Incoming,
                        networkType = "",
                        "",
                        "",
                        0.0003,
                        "XCH",
                        ""
                    )
                )
            ),
            onIntent = {}
        )
    }

}






























