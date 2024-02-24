package com.green.wallet.presentation.main.transaction.offer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.green.compose.dimens.size_1
import com.green.compose.dimens.size_10
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_120
import com.green.compose.dimens.size_20
import com.green.compose.dimens.size_3
import com.green.compose.dimens.size_30
import com.green.compose.dimens.size_45
import com.green.compose.dimens.size_5
import com.green.compose.dimens.size_50
import com.green.compose.dimens.size_8
import com.green.compose.dimens.text_12
import com.green.compose.dimens.text_14
import com.green.compose.dimens.text_15
import com.green.compose.progress.CircularProgressBar
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.main.transaction.TransactionIntent


@Composable
fun OfferTransactionItem(
    state: OfferTransaction = OfferTransaction(),
    initialOpen: Boolean = true,
    onIntent: (TransactionIntent) -> Unit = {},
    onDetailsOpen: (Boolean) -> Unit = {}
) {

    var isDetailOpen by remember { mutableStateOf(initialOpen) }

    val additionalHeight = remember {
        (if (state.height == 0L) 50 else 0) + 210 + ((state.offered.size + state.requested.size - 2) * 10)
    }

    val heightParentColumn by animateDpAsState(
        targetValue =
        if (isDetailOpen) additionalHeight.dp
        else size_45,
        label = "",
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
    )

    val arrowRotate by animateFloatAsState(
        if (isDetailOpen)
            180f
        else 0f,
        label = "",
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
    )

    LaunchedEffect(isDetailOpen) {
        onDetailsOpen(isDetailOpen)
    }

    val alphaContent by animateFloatAsState(
        targetValue = if (isDetailOpen)
            1f
        else
            0f,
        label = "",
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing,
            delayMillis = 500
        )
    )

    Column(
        modifier = Modifier
            .height(heightParentColumn)
            .background(color = Provider.current.offerTransactionDetails)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(size_45)
                .background(color = Provider.current.bcgTransactionItem)
                .padding(start = size_5)
        ) {
            DefaultText(
                text = "Take offer",
                size = text_15,
                color = Provider.current.blue,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.2f)
                    .wrapContentHeight(),
                textAlign = TextAlign.Start
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.2f)
            ) {
                CircularProgressBar(
                    modifier = Modifier
                        .align(Alignment.Center),
                    size = size_20,
                    strokeWidth = 3.dp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.8f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_downword),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(size = size_30)
                        .padding(end = size_12)
                        .rotate(arrowRotate)
                        .clickable {
                            isDetailOpen = !isDetailOpen
                        },
                    tint = Provider.current.green
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    top = size_10,
                    start = size_8,
                    end = size_8,
                    bottom = size_10
                )
                .graphicsLayer {
                    alpha = alphaContent
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    DefaultText(
                        text = "Дата",
                        size = text_12,
                        color = Provider.current.greyText
                    )

                    DefaultText(
                        text = "23 января, 19:11",
                        size = text_14,
                        color = Provider.current.secondaryTextColor
                    )
                }
                Column {
                    state.offered.forEach { item ->
                        when (item) {
                            is CatToken -> {
                                CatTokenItemOfferTran(item, !state.acceptOffer)
                            }

                            is NftToken -> {
                                NFTTokenDAppOfferTrans(item, !state.acceptOffer)
                            }
                        }
                    }

                    state.requested.forEach { item ->
                        when (item) {
                            is CatToken -> {
                                CatTokenItemOfferTran(item, state.acceptOffer)
                            }

                            is NftToken -> {
                                NFTTokenDAppOfferTrans(item, state.acceptOffer)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end = size_5,
                        top = size_10
                    ),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    DefaultText(
                        text = "Источник",
                        size = text_12,
                        color = Provider.current.greyText
                    )

                    DefaultText(
                        text = "dexie.space",
                        size = text_14,
                        color = Provider.current.secondaryTextColor
                    )


                    DefaultText(
                        text = "Комиссия",
                        size = text_12,
                        color = Provider.current.greyText,
                        modifier = Modifier
                            .padding(
                                top = size_10
                            )
                    )

                    DefaultText(
                        text = "0 XCH",
                        size = text_14,
                        color = Provider.current.secondaryTextColor
                    )
                }

                Column {
                    DefaultText(
                        text = "Hash транзакции",
                        size = text_12,
                        color = Provider.current.greyText
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DefaultText(
                            text = "0xb4b8...k486",
                            size = text_14,
                            color = Provider.current.secondaryTextColor
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_copy_green),
                            contentDescription = null,
                            modifier = Modifier.padding(top = size_3, start = size_3)
                        )
                    }

                    DefaultText(
                        text = "Высота",
                        size = text_12,
                        color = Provider.current.greyText,
                        modifier = Modifier
                            .padding(
                                top = size_10
                            )
                    )

                    DefaultText(
                        text = "1234567",
                        size = text_14,
                        color = Provider.current.secondaryTextColor
                    )

                }
            }

            if (state.height == 0L)
                Box(
                    modifier = Modifier
                        .padding(
                            top = size_10,
                            end = size_10
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    OutlinedButton(
                        border = BorderStroke(size_1, Provider.current.green),
                        colors = androidx.compose.material.ButtonDefaults
                            .outlinedButtonColors(
                                backgroundColor = Provider.current.offerTransactionDetails,
                                contentColor = Provider.current.offerTransactionDetails
                            ),
                        modifier = Modifier.width(size_120),
                        onClick = {

                        },
                        shape = RoundedCornerShape(size_50)
                    ) {
                        DefaultText(
                            text = "Cancel",
                            color = Provider.current.green,
                            size = text_15
                        )
                    }
                }
        }
    }
}


@Composable
fun CatTokenItemOfferTran(item: CatToken, acceptOffer: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        val fromTokenClr =
            if (acceptOffer) Provider.current.errorColor else Provider.current.green
        val sign = if (acceptOffer) "-" else "+"
        DefaultText(
            text = "$sign ${formattedDoubleAmountWithPrecision(item.amount)} ${item.code}",
            size = text_14,
            color = fromTokenClr,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun NFTTokenDAppOfferTrans(item: NftToken, acceptOffer: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val nftText = if (acceptOffer) "-1 NFT" else "+1 NFT"
        val nftColor =
            if (acceptOffer) Provider.current.errorColor else Provider.current.green
        DefaultText(
            text = nftText,
            size = text_14,
            color = nftColor,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.Start
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OfferTransactionItemPreview() {
    GreenWalletTheme {
        OfferTransactionItem()
    }
}