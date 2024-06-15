package com.greenwallet.core.ext


fun String.extractDomain(): String {
    val pattern = Regex("(?:https?://)?(?:www\\.)?([\\w\\d.-]+)")
    val matchResult = pattern.find(this)
    return matchResult?.groups?.get(1)?.value ?: ""
}
