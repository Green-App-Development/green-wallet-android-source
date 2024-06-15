package com.green.compose.utils

import java.text.DecimalFormat


fun formattedDoubleAmountWithPrecision(double: Double, precision: Int = 13): String {
    if (double == 0.0) return "0.0"
    val df = DecimalFormat("0")
    df.maximumFractionDigits = 340
    val formatted = df.format(double).replace(",", ".")
    val dotPos = formatted.indexOf('.')
    val endMin = Math.min(dotPos + precision, formatted.length)
    return formatted.substring(0, endMin)
}

fun doubleCeilString(value: Double): String {
    val precision = formattedDoubleAmountWithPrecision(value, 6)
    return formattedDoubleAmountWithPrecision((precision.toDouble() + 0.00001), 6)
}