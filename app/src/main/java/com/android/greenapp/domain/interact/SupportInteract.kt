package com.android.greenapp.domain.interact

import com.android.greenapp.data.network.dto.support.ListingPost
import com.android.greenapp.data.network.dto.support.QuestionPost
import com.android.greenapp.domain.domainmodel.FAQItem
import com.android.greenapp.presentation.tools.Resource

interface SupportInteract {

    suspend fun getFAQQuestionAnswers(): Resource<List<FAQItem>>

    suspend fun postListing(listing: ListingPost): Resource<String>

    suspend fun postQuestion(question:QuestionPost): Resource<String>

}