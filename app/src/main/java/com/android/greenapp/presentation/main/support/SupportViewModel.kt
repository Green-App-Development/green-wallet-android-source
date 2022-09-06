package com.android.greenapp.presentation.main.support

import androidx.lifecycle.ViewModel
import com.android.greenapp.data.network.dto.support.ListingPost
import com.android.greenapp.data.network.dto.support.QuestionPost
import com.android.greenapp.domain.interact.SupportInteract
import javax.inject.Inject

/**
 * Created by bekjan on 02.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class SupportViewModel @Inject constructor(private val supportInteract: SupportInteract) :
    ViewModel() {

    suspend fun getFAQQuestionAnswers() = supportInteract.getFAQQuestionAnswers()

    suspend fun postListing(listing: ListingPost) = supportInteract.postListing(listing)

    suspend fun postQuestion(question: QuestionPost) = supportInteract.postQuestion(question)

}