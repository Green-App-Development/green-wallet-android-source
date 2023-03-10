package com.green.wallet.presentation.main.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.data.network.dto.support.ListingPost
import com.green.wallet.data.network.dto.support.QuestionPost
import com.green.wallet.domain.domainmodel.FAQItem
import com.green.wallet.domain.interact.SupportInteract
import com.green.wallet.presentation.custom.isExceptionBelongsToNoInternet
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class SupportViewModel @Inject constructor(private val supportInteract: SupportInteract) :
    ViewModel() {

    private var job: Job? = null

    private var _faqList = MutableStateFlow<Resource<List<FAQItem>>?>(null)
    var faqList = _faqList.asStateFlow()

    fun getFAQQuestionAnswers(times: Int) {
        job?.cancel()
        job = viewModelScope.launch {
            val res = supportInteract.getFAQQuestionAnswers()
            _faqList.emit(res)
            if (res.state == Resource.State.ERROR && times != 0 && isExceptionBelongsToNoInternet(
                    res.error!!
                )
            ) {
                VLog.d("No internet exception in posting a listing : $times")
                delay(2500)
                getFAQQuestionAnswers(times - 1)
            }
        }
    }

    suspend fun postListing(listing: ListingPost) =supportInteract.postListing(listing)

   suspend fun postQuestion(question: QuestionPost):Resource<String> = supportInteract.postQuestion(question=question)

}
