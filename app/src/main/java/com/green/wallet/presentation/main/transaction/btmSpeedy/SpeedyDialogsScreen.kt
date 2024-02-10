package com.green.wallet.presentation.main.transaction.btmSpeedy

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.common.tools.formatString
import com.green.compose.buttons.DefaultButton
import com.green.compose.custom.fee.FeeContainer
import com.green.compose.custom.fee.FixedSpacer
import com.green.compose.dimens.size_1
import com.green.compose.dimens.size_10
import com.green.compose.dimens.size_100
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_14
import com.green.compose.dimens.size_15
import com.green.compose.dimens.size_16
import com.green.compose.dimens.size_18
import com.green.compose.dimens.size_20
import com.green.compose.dimens.size_26
import com.green.compose.dimens.size_5
import com.green.compose.dimens.size_6
import com.green.compose.dimens.size_80
import com.green.compose.dimens.size_9
import com.green.compose.dimens.text_12
import com.green.compose.dimens.text_14
import com.green.compose.dimens.text_15
import com.green.compose.dimens.text_16
import com.green.compose.extension.getActivity
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.compose.utils.formattedDoubleAmountWithPrecision
import com.green.wallet.R
import com.green.wallet.presentation.main.dapp.trade.bottom.CatTokenItem
import com.green.wallet.presentation.main.dapp.trade.bottom.NftItem
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.tools.getStringResource


@Composable
fun SpeedyTransactionBtmScreen(
    state: SpeedyTokenState = SpeedyTokenState(address = "asdlsjadljsaldfjsakldfa"),
    onEvent: (SpeedyTokenEvent) -> Unit = {}
) {
    val activity = LocalContext.current.getActivity()!!
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Provider.current.primaryAppBackground)
            .padding(
                horizontal = size_16
            ), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(size_80)
                .height(size_6)
                .background(
                    color = Provider.current.iconGrey, shape = RoundedCornerShape(size_100)
                )

        )
        FixedSpacer(height = size_16)
        Column(
            modifier = Modifier
                .background(
                    color = Provider.current.blackAppBackground, shape = RoundedCornerShape(size_15)
                )
                .fillMaxWidth()
                .padding(
                    top = size_18, start = size_16, end = size_16
                ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                DefaultText(
                    text = activity.getStringResource(R.string.speed_up_dialog_title),
                    size = text_16,
                    color = Provider.current.green,
                    fontWeight = FontWeight.W500
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_speed_up),
                    contentDescription = null,
                    tint = Provider.current.green,
                    modifier = Modifier.padding(start = size_5, top = size_5)
                )
            }
            FixedSpacer(height = size_26)
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DefaultText(
                    text = activity.getStringResource(R.string.current_account),
                    size = text_14,
                    color = Provider.current.green
                )
                DefaultText(
                    text = formatString(10, state.address, 6),
                    size = text_16,
                    color = Provider.current.secondaryTextColor
                )
            }
            FixedSpacer(height = size_12)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(size_1)
                    .background(color = Provider.current.dividerOffer),
            )
            FixedSpacer(height = size_12)
            DefaultText(
                text = activity.getStringResource(R.string.transaction_acceleration),
                size = text_14,
                color = Provider.current.green,
                fontWeight = FontWeight.W500,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            FixedSpacer(height = size_12)
            if (state.token != null) {
                when (state.token) {
                    is CatToken -> {
                        CatTokenItem(state.token, true)
                    }

                    is NftToken -> {
                        NftItem(state.token, true)
                    }

                    else -> Unit
                }
            }
            FixedSpacer(height = size_15)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(size_1)
                    .background(color = Provider.current.dividerOffer),
            )

            FixedSpacer(height = size_14)

            val amount = formattedDoubleAmountWithPrecision(state.spendableBalance)
            val feeEnough = state.spendableBalance >= state.fee

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = size_6),
                horizontalAlignment = Alignment.Start
            ) {
                DefaultText(
                    text = "${activity.getStringResource(R.string.spendable_balance)}: $amount",
                    size = text_12,
                    color = if (feeEnough) Provider.current.greyText
                    else Provider.current.errorColor
                )
            }

            DefaultText(
                text = activity.getStringResource(R.string.network_commission_fee),
                size = text_14,
                color = Provider.current.green,
                fontWeight = FontWeight.W500,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            FixedSpacer(height = size_9)

            FeeContainer(normal = state.normalFeeDexie,
                isEnough = state.isChosenFeeEnough,
                fee = {
                    onEvent(SpeedyTokenEvent.OnFeeChosen(it))
                }
            )
            FixedSpacer(height = size_20)
        }

        FixedSpacer(height = size_20)

        DefaultButton(
            bcgColor = if (state.isChosenFeeEnough) Provider.current.green else Provider.current.btnInActive,
            onClick = {
                onEvent(SpeedyTokenEvent.OnSign)
            }) {
            DefaultText(
                text = activity.getStringResource(R.string.sign),
                size = text_15,
                color = Provider.current.txtSecondaryColor
            )
        }
        FixedSpacer(height = size_10)
    }

}


@Preview(showBackground = true)
@Composable
fun TransactionComposeScreenPreview() {
    GreenWalletTheme {
        SpeedyTransactionBtmScreen()
    }
}


