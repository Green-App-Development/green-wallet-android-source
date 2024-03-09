package com.green.compose.web_header

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.R
import com.green.compose.custom.fee.FixedSpacer
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_15
import com.green.compose.dimens.size_24
import com.green.compose.dimens.size_44
import com.green.compose.dimens.text_20
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider


@Composable
fun WebViewHeader(
    modifier: Modifier = Modifier,
    back: () -> Unit = {},
    threeDots: () -> Unit = {},
    closeX: () -> Unit = {}
) {
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
            modifier = Modifier
                .size(size_24)
                .clickable {
                    back()
                },
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
                        threeDots()
                    }
            )

            FixedSpacer(width = size_12)
            Icon(
                painter = painterResource(id = R.drawable.ic_x),
                contentDescription = null,
                modifier = Modifier
                    .size(size_24)
                    .clickable {
                        back()
                    },
                tint = Provider.current.txtPrimaryColor
            )
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