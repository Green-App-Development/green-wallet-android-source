package com.green.wallet.presentation.main.transaction.offer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.green.compose.dimens.size_10
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_3
import com.green.compose.dimens.size_30
import com.green.compose.dimens.size_5
import com.green.compose.dimens.size_8
import com.green.compose.dimens.text_12
import com.green.compose.dimens.text_14
import com.green.compose.dimens.text_15
import com.green.compose.progress.CircularProgressBar
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.wallet.R
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken


@Composable
fun OfferTransactionItem(
    state: OfferTransState = OfferTransState(),
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
                size = size_30
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
                    .padding(end = size_12),
                tint = Provider.current.green
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(color = Provider.current.offerTransactionDetails)
            .padding(
                top = size_10,
                start = size_8,
                end = size_8,
                bottom = size_10
            )
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
                .padding(end = size_5),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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

                Row {
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
        OfferTransactionItem(
            state = OfferTransState(
                offered = listOf(
                    CatToken("XCH", "", 0.00003),
//                    CatToken("GAD", "", 0.00003)
                ),
                requested = listOf(
//                    CatToken("XCC", "", 0.0013),
                    CatToken("GWT", "", 0.11),
                    CatToken("CHIA", "", 0.456)
                ),
                acceptOffer = false
            )
        )
    }
}