package com.green.compose.web_header

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.green.compose.R
import com.green.compose.custom.fee.FixedSpacer
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_15
import com.green.compose.dimens.size_2
import com.green.compose.dimens.size_24
import com.green.compose.dimens.size_44
import com.green.compose.dimens.size_5
import com.green.compose.dimens.text_20
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.compose.utils.HTTPS
import com.greenwallet.core.ext.extractDomain
import kotlinx.coroutines.delay


@Composable
fun WebViewHeader(
    pageLoading: Boolean = false,
    url: String,
    modifier: Modifier = Modifier,
    back: () -> Unit = {},
    threeDots: () -> Unit = {},
    closeX: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Provider.current.background)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(size_44)
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
                verticalAlignment = Alignment.Bottom,
            ) {
                if (url.startsWith(HTTPS)) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.fa_lock
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(size_24),
                        tint = Provider.current.txtPrimaryColor
                    )
                } else {
                    Image(
                        painter = painterResource(
                            id = R.drawable.ic_unsecure_lock
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(size_24)
                    )
                }

                DefaultText(
                    text = url.extractDomain(),
                    size = text_20,
                    color = Provider.current.txtPrimaryColor,
                    modifier = Modifier.padding(start = size_5),
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

        if (pageLoading) {
            var barWidth by remember { mutableIntStateOf(10) }

            LaunchedEffect(true) {
                while (barWidth < 1000) {
                    barWidth += 10
                    delay(250)
                }
            }
            Box(
                modifier = Modifier
                    .width(barWidth.dp)
                    .height(size_2)
                    .background(
                        color = Provider.current.green
                    )
                    .align(Alignment.BottomStart)
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
            WebViewHeader(url = "http://google.com")
        }
    }
}