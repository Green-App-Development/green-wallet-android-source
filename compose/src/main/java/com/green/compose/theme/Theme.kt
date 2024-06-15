package com.green.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColorsPalette(
    val txtPrimaryColor: Color = Color.Unspecified,
    val txtSecondaryColor: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val iconGrey: Color = Color.Unspecified,
    val green: Color = Color(0xFF3AAC59),
    val red: Color = Color(0xFFFF2222),
    val greyText: Color = Color(0xFF949494),
    val primaryAppBackground: Color = Color.Unspecified,
    val blackAppBackground: Color = Color.Unspecified,
    val secondGrey: Color = Color.Unspecified,
    val errorColor: Color = Color.Unspecified,
    val feeBackground: Color = Color.Unspecified,
    val feeBackgroundChosen: Color = Color.Unspecified,
    val dividerColor: Color = Color.Unspecified,
    val secondPrimaryTextColor: Color = Color.Unspecified,
    val btnInActive: Color = Color.Unspecified,
    val secondaryTextColor: Color = Color.Unspecified,
    val dividerOffer: Color = Color.Unspecified,
    val progressTrackColor: Color = Color.Unspecified,
    val bcgTransactionItem: Color = Color.Unspecified,
    val blue: Color = Color(0xFF1E93FF),
    val offerTransactionDetails: Color = Color.Unspecified,
    val resultResultBackground: Color = Color.Unspecified,
)

val OnLightCustomColorsPalette = CustomColorsPalette(
    txtPrimaryColor = Color.Black,
    txtSecondaryColor = Color.White,
    primaryAppBackground = Color(0xFFF4F4F4),
    dividerColor = Color(0xFFF2F2F2),
    btnInActive = Color(0xFFDADADA),
    blackAppBackground = Color(0xFFffffff),
    greyText = Color(0xFFC9C9C9),
    secondaryTextColor = Color(0xFF202020),
    secondGrey = Color(0xFF757575),
    errorColor = Color(0xFFFF2222),
    dividerOffer = Color(0xFFF2F2F2),
    feeBackgroundChosen = Color.White,
    feeBackground = Color(0xFFf4f4f4),
    iconGrey = Color(0xFFC9C9C9),
    progressTrackColor = Color(0xFFC9C9C9)
)

val OnDarkCustomColorsPalette = CustomColorsPalette(
    txtPrimaryColor = Color(0xFFFFFFFF),
    txtSecondaryColor = Color.White,
    background = Color(0xFF303030),
    iconGrey = Color(0xFF4A4A4A),
    green = Color(0xFF3AAC59),
    primaryAppBackground = Color(0xFF303030),
    blackAppBackground = Color(0xFF1C1C1C),
    secondGrey = Color(0x80757575),
    errorColor = Color(0xFFFF2222),
    feeBackground = Color(0xFF363636),
    feeBackgroundChosen = Color(0xFF444444),
    dividerColor = Color(0xFF4A4A4A),
    secondPrimaryTextColor = Color(0xFFFFFFFF),
    btnInActive = Color(0xFF444444),
    greyText = Color(0xFF949494),
    secondaryTextColor = Color(0xFFFFFFFF),
    dividerOffer = Color(0xFF494949),
    progressTrackColor = Color(0xFF494949),
    bcgTransactionItem = Color(0xFF303030),
    offerTransactionDetails = Color(0xFF262626),
    resultResultBackground = Color(0xFF262626)
)

val Provider = staticCompositionLocalOf { CustomColorsPalette() }

@Composable
fun GreenWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val customColorsPalette =
        if (darkTheme || true) OnDarkCustomColorsPalette
        else OnLightCustomColorsPalette

    // here is the important point, where you will expose custom objects
    CompositionLocalProvider(
        Provider provides customColorsPalette // our custom palette
    ) {
        content()
    }

}