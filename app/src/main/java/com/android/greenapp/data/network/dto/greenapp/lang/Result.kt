package com.android.greenapp.data.network.dto.greenapp.lang

data class Result(
	val default_language: DefaultLanguage,
	val list: List<LanguageItemDto>,
	val version: String
)
