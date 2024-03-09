package com.green.wallet.presentation.main.dapp.browser

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.green.compose.dimens.size_1
import com.green.compose.dimens.size_10
import com.green.compose.dimens.size_15
import com.green.compose.dimens.size_16
import com.green.compose.dimens.size_2
import com.green.compose.dimens.size_20
import com.green.compose.dimens.size_30
import com.green.compose.dimens.size_4
import com.green.compose.dimens.size_50
import com.green.compose.dimens.size_6
import com.green.compose.dimens.size_8
import com.green.compose.dimens.text_14
import com.green.compose.dimens.text_15
import com.green.compose.dimens.text_16
import com.green.compose.dimens.text_25
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.DAppModel


@Composable
fun BrowserScreen(
    state: BrowserState,
    onEvent: (BrowserEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Provider.current.blackAppBackground
            )
            .padding(
                horizontal = size_15,
                vertical = size_15
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                modifier = Modifier
                    .border(
                        width = size_1,
                        color = Provider.current.iconGrey,
                        shape = RoundedCornerShape(size_30)
                    )
                    .padding(
                        horizontal = size_10,
                        vertical = size_8
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(size_6)
                        .background(color = Provider.current.green, shape = CircleShape)

                )
                DefaultText(
                    text = "Chia Network",
                    size = text_14,
                    color = Provider.current.green,
                    modifier = Modifier
                        .padding(horizontal = size_8)
                )

                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_arrow_downword,
                    ),
                    contentDescription = null,
                    tint = Provider.current.green,
                    modifier = Modifier
                        .size(size_15)
                )
            }
        }

        DefaultText(
            text = "Browser",
            size = text_25,
            color = Provider.current.green,
            modifier = Modifier
                .padding(top = size_10)
        )

        TextField(
            placeholder = {
                DefaultText(
                    text = "Поиск",
                    size = text_15,
                    color = Provider.current.greyText
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = size_20
                ),
            value = state.searchText,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Provider.current.background,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                textColor = Provider.current.txtPrimaryColor
            ),
            onValueChange = {
                onEvent(BrowserEvent.OnSearchChange(it))
            },
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                    tint = Provider.current.greyText
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(size_50)
                .padding(
                    top = size_20
                ),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        end = size_8
                    )
                    .fillMaxHeight()
                    .background(
                        color = if (state.searchCategory == SearchCategory.ALL) Provider.current.green
                        else Provider.current.background,
                        shape = RoundedCornerShape(size_15)
                    )
                    .clickable {
                        onEvent(BrowserEvent.OnChangeCategory(SearchCategory.ALL))
                    },
                contentAlignment = Alignment.Center
            ) {
                DefaultText(
                    text = "Все",
                    size = text_14,
                    color = if (state.searchCategory == SearchCategory.ALL)
                        Color.White
                    else
                        Provider.current.greyText,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        horizontal = size_8
                    )
                    .fillMaxHeight()
                    .background(
                        color = if (state.searchCategory == SearchCategory.DeFi) Provider.current.green
                        else Provider.current.background,
                        shape = RoundedCornerShape(size_15)
                    )
                    .clickable {
                        onEvent(BrowserEvent.OnChangeCategory(SearchCategory.DeFi))
                    },
                contentAlignment = Alignment.Center
            ) {
                DefaultText(
                    text = "DeFi",
                    size = text_14,
                    color = if (state.searchCategory == SearchCategory.DeFi)
                        Color.White
                    else
                        Provider.current.greyText,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = size_8
                    )
                    .fillMaxHeight()
                    .background(
                        color = if (state.searchCategory == SearchCategory.NFT) Provider.current.green
                        else Provider.current.background,
                        shape = RoundedCornerShape(size_15)
                    )
                    .height(size_30)
                    .clickable {
                        onEvent(BrowserEvent.OnChangeCategory(SearchCategory.NFT))
                    },
                contentAlignment = Alignment.Center
            ) {
                DefaultText(
                    text = "NFT",
                    size = text_14,
                    color = if (state.searchCategory == SearchCategory.NFT)
                        Color.White
                    else
                        Provider.current.greyText,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(top = size_20)
                .weight(1f)
        ) {
            LazyColumn {
                items(state.dAppList) {
                    DAppItemRepresentation(
                        item = it
                    ) {
                        onEvent(BrowserEvent.ChosenDexie)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
        ) {
            DefaultText(
                text = "Not on the list?",
                size = text_16,
                color = Provider.current.secondGrey
            )

            DefaultText(
                modifier = Modifier
                    .padding(start = size_4)
                    .clickable {
                        onEvent(BrowserEvent.ListingApplication)
                    },
                text = "Listing application",
                size = text_16,
                color = Provider.current.green,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
fun DAppItemRepresentation(
    item: DAppModel,
    onItem: (DAppModel) -> Unit
) {
    Column(modifier = Modifier
        .clickable {
            onItem(item)
        }) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_chia),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = size_10)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                DefaultText(
                    text = item.name,
                    size = text_16,
                    color = Provider.current.txtPrimaryColor
                )
                DefaultText(
                    text = item.description,
                    size = text_14,
                    color = Provider.current.secondGrey
                )
                Row(
                    modifier = Modifier
                        .padding(
                            top = size_2
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = com.greenwallet.uikit.R.drawable.ic_checked_green),
                        contentDescription = null
                    )
                    DefaultText(
                        modifier = Modifier
                            .padding(
                                start = size_4
                            ),
                        text = "dexie.space",
                        size = text_14,
                        color = Provider.current.green
                    )
                }
            }
            Icon(
                painter = painterResource(id = com.greenwallet.uikit.R.drawable.ic_script),
                contentDescription = null,
                modifier = Modifier
                    .size(size_30),
                tint = if (item.isConnected) Provider.current.green
                else Provider.current.secondGrey
            )
        }

        Box(
            modifier = Modifier
                .padding(top = size_10)
                .fillMaxWidth()
                .height(size_1)
                .background(
                    color = Provider.current.dividerOffer
                )
        )
    }
}

@Preview
@Composable
fun BrowserScreenPreview() {
    GreenWalletTheme {
        BrowserScreen(
            state = BrowserState(),
            onEvent = {}
        )
    }
}
