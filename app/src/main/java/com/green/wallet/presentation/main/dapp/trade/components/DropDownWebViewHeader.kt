package com.green.wallet.presentation.main.dapp.trade.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.green.compose.dimens.size_1
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_15
import com.green.compose.dimens.size_218
import com.green.compose.dimens.size_40
import com.green.compose.dimens.text_12
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.wallet.R
import com.green.wallet.presentation.main.dapp.trade.TraderEvent


@Composable
fun DropDownWebViewHeader(
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onEvent: (TraderEvent) -> Unit = {},
    close: () -> Unit = {}
) {
    DropdownMenu(
        expanded = expanded,
        modifier = modifier
            .background(
                color = Provider.current.iconGrey
            )
            .width(size_218),
        offset = DpOffset(300.dp, 0.dp),
        onDismissRequest = {
            close()
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


@Preview(showBackground = true)
@Composable
fun DropDownWebViewHeaderPreview() {
    GreenWalletTheme {
        Column(
            modifier = Modifier.wrapContentSize()
        ) {
            DropDownWebViewHeader(
                expanded = true
            )
        }
    }
}