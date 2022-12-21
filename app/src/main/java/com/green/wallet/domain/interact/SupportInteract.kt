package com.green.wallet.domain.interact

import com.green.wallet.data.network.dto.support.ListingPost
import com.green.wallet.data.network.dto.support.QuestionPost
import com.green.wallet.domain.domainmodel.FAQItem
import com.green.wallet.presentation.tools.Resource

interface SupportInteract {

    suspend fun getFAQQuestionAnswers(): Resource<List<FAQItem>>

    suspend fun postListing(listing: ListingPost): Resource<String>

    suspend fun postQuestion(question:QuestionPost): Resource<String>

}
