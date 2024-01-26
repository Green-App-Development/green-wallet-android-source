package com.green.wallet.presentation.main.dapp.trade.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.custom.fee.FixedSpacer
import com.green.compose.dimens.size_1
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_15
import com.green.compose.dimens.size_218
import com.green.compose.dimens.size_24
import com.green.compose.dimens.size_40
import com.green.compose.dimens.size_44
import com.green.compose.dimens.text_12
import com.green.compose.dimens.text_20
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.wallet.R


@Composable
fun WebViewHeader(
    modifier: Modifier = Modifier,
    back: () -> Unit = {},
    threeDots: () -> Unit = {},
    closeX: () -> Unit = {}
) {

    var dropDownVisible by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(size_44)
            .background(color = Provider.current.background)
            .padding(horizontal = size_15),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back_arrow),
            contentDescription = null,
            modifier = Modifier.size(size_24),
            tint = Provider.current.txtPrimaryColor
        )

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Icon(
                painter = painterResource(id = R.drawable.fa_lock),
                contentDescription = null,
                modifier = Modifier.size(size_24),
                tint = Provider.current.txtPrimaryColor
            )
            DefaultText(
                text = "dexie.space",
                size = text_20,
                color = Provider.current.txtPrimaryColor
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.three_dots),
                contentDescription = null,
                tint = Provider.current.txtPrimaryColor,
                modifier = Modifier
                    .clickable {
                        dropDownVisible = true
                    }
            )
            FixedSpacer(width = size_12)

            Icon(
                painter = painterResource(id = R.drawable.ic_x),
                contentDescription = null,
                modifier = Modifier.size(size_24),
                tint = Provider.current.txtPrimaryColor
            )
        }

    }
    DropdownMenu(
        expanded = dropDownVisible,
        modifier = Modifier
            .background(
                color = Provider.current.iconGrey
            )
            .width(size_218),
        onDismissRequest = {
        }) {
        DropdownMenuItem(
            modifier = Modifier
                .height(size_40),
            onClick = {
            }) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_turn_off),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = size_15)
                )
                DefaultText(
                    text = "Обновить",
                    size = text_12,
                    color = Provider.current.txtPrimaryColor,
                    modifier = Modifier.padding(start = size_12)
                )
            }
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(size_1)
                .background(Provider.current.dividerColor)
        )
        DropdownMenuItem(
            modifier = Modifier.height(size_40),
            onClick = {

            }) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = size_15)
                )
                DefaultText(
                    text = "Поделиться",
                    size = text_12,
                    color = Provider.current.txtPrimaryColor,
                    modifier = Modifier.padding(start = size_12)
                )
            }
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(size_1)
                .background(Provider.current.dividerColor)
        )
        DropdownMenuItem(
            modifier = Modifier.height(size_40),
            onClick = {

            }) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_copy_green),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = size_15)
                )
                DefaultText(
                    text = "Поделиться",
                    size = text_12,
                    color = Provider.current.txtPrimaryColor,
                    modifier = Modifier.padding(start = size_12)
                )
            }
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(size_1)
                .background(Provider.current.dividerColor)
        )

        DropdownMenuItem(
            modifier = Modifier.height(size_40),
            onClick = {

            }) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_turn_off),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = size_15)
                )
                DefaultText(
                    text = "Поделиться",
                    size = text_12,
                    color = Provider.current.txtPrimaryColor,
                    modifier = Modifier.padding(start = size_12)
                )
            }
        }
    }
}


@Preview
@Composable
fun WebViewHeaderPreview() {
    GreenWalletTheme {
        Column(
            modifier = Modifier.background(
                color =
                Provider.current.background
            )
        ) {
            WebViewHeader()
        }
    }
}