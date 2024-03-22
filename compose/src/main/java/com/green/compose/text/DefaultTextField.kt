package com.green.compose.text

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.green.compose.R
import com.green.compose.dimens.size_10
import com.green.compose.dimens.size_100
import com.green.compose.dimens.size_14
import com.green.compose.dimens.size_36
import com.green.compose.dimens.size_8
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider

@Composable
fun DefaultTextField(
    modifier: Modifier = Modifier,
    input: String = "",
    onSearchClick: () -> Unit = {},
    onTextChange: (input: String) -> Unit,
    bottomRounded: Boolean = true,
) {
    var text by remember { mutableStateOf(input) }

    Row(
        modifier = modifier
            .background(
                color = Provider.current.background,
                shape = RoundedCornerShape(
                    topStart = size_8,
                    topEnd = size_8,
                    bottomStart = if (bottomRounded) size_8 else 0.dp,
                    bottomEnd = if (bottomRounded) size_8 else 0.dp,
                )
            )
            .fillMaxWidth()
            .padding(
                horizontal = size_14
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            modifier = modifier.weight(1f),
            value = text,
            onValueChange = {
                text = it
                onTextChange(it)
            },
            cursorBrush = SolidColor(Provider.current.txtSecondaryColor),
            textStyle = TextStyle(
                fontSize = 20.sp,
                color = Provider.current.txtSecondaryColor,
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Search",
                            style = TextStyle(color = Color.Gray), // Hint color
                            modifier = Modifier
                        )
                    }
                    innerTextField()
                }
            }
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = null,
            tint = Provider.current.greyText,
            modifier = Modifier.clickable {
                onSearchClick()
            }
        )
    }
}

@Preview
@Composable
fun DefaultTextFieldsPreview() {
    GreenWalletTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White
                )
                .height(
                    size_100
                ),
            verticalArrangement = Arrangement.Center
        ) {
            DefaultTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(size_36),
                onSearchClick = {},
                onTextChange = {}
            )
        }
    }
}