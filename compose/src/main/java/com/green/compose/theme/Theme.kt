package com.green.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.green.compose.colors.txtPrimaryColorsDark
import com.green.compose.colors.txtPrimaryColorsLight

@Immutable
data class CustomColorsPalette(
    val txtPrimaryColor: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val iconGrey: Color = Color.Unspecified,
    val green: Color = Color(0xFF3AAC59),
    val greyText: Color = Color(0xFF949494),
    val primaryAppBackground: Color = Color.Unspecified,
    val blackAppBackground: Color = Color.Unspecified,
    val secondGrey: Color = Color.Unspecified,
    val errorColor: Color = Color.Unspecified,
    val feeBackground: Color = Color.Unspecified,
    val feeBackgroundChoosen: Color = Color.Unspecified,
    val dividerColor: Color = Color.Unspecified,
    val secondPrimaryTextColor: Color = Color.Unspecified,
    val btnInActive: Color = Color.Unspecified
)

val OnLightCustomColorsPalette = CustomColorsPalette(
    txtPrimaryColor = txtPrimaryColorsLight,
    primaryAppBackground = Color(0xFFF4F4F4),
    dividerColor = Color(0xFFF2F2F2),
    btnInActive = Color(0xFFDADADA)
)

val OnDarkCustomColorsPalette = CustomColorsPalette(
    txtPrimaryColor = txtPrimaryColorsDark,
    background = Color(0xFF303030),
    iconGrey = Color(0xFF4A4A4A),
    green = Color(0xFF3AAC59),
    primaryAppBackground = Color(0xFF303030),
    blackAppBackground = Color(0xFF1C1C1C),
    secondGrey = Color(0x80757575),
    errorColor = Color(0xFFFF2222),
    feeBackground = Color(0xff363636),
    feeBackgroundChoosen = Color(0xff444444),
    dividerColor = Color(0xFF4A4A4A),
    secondPrimaryTextColor = Color(0xFFFFFFFF),
    btnInActive = Color(0xFF444444)
)

val Provider = staticCompositionLocalOf { CustomColorsPalette() }

@Composable
fun GreenWalletTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val customColorsPalette =
        if (darkTheme) OnDarkCustomColorsPalette
        else OnLightCustomColorsPalette

    // here is the important point, where you will expose custom objects
    CompositionLocalProvider(
        Provider provides customColorsPalette // our custom palette
    ) {
        content()
    }

}