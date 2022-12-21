package com.green.wallet.data.network.dto.greenapp.lang

data class Result(
	val default_language: DefaultLanguage,
	val list: List<LanguageItemDto>,
	val version: String
)
