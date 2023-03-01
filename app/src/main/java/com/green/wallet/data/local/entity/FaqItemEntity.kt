package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.FAQItem


@Entity(tableName = "FAQs")
data class FaqItemEntity(
	@PrimaryKey(autoGenerate = false)
	val question: String,
	val answer: String,
	val langCode: String
) {

	fun toFAQItem() = FAQItem(question, answer, false)

}
