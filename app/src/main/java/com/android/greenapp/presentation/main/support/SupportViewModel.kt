package com.android.greenapp.presentation.main.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.greenapp.data.network.dto.support.ListingPost
import com.android.greenapp.data.network.dto.support.QuestionPost
import com.android.greenapp.domain.domainmodel.FAQItem
import com.android.greenapp.domain.interact.SupportInteract
import com.android.greenapp.presentation.custom.isExceptionBelongsToNoInternet
import com.android.greenapp.presentation.tools.Resource
import com.example.common.tools.VLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 02.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class SupportViewModel @Inject constructor(private val supportInteract: SupportInteract) :
    ViewModel() {

    private var _postQuestion = MutableStateFlow<Resource<String>?>(null)
    var postQuestion: StateFlow<Resource<String>?> = _postQuestion.asStateFlow()
    private var job: Job? = null

    private var _postListing = MutableStateFlow<Resource<String>?>(null)
    var postListing: StateFlow<Resource<String>?> = _postListing.asStateFlow()

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

    fun postListing(listing: ListingPost, times: Int) {
        job?.cancel()
        job = viewModelScope.launch {
            val res = supportInteract.postListing(listing)
            _postListing.emit(res)
            if (res.state == Resource.State.ERROR && times != 0 && isExceptionBelongsToNoInternet(
                    res.error!!
                )
            ) {
                VLog.d("No internet exception in posting a listing : $times")
                delay(2500)
                postListing(listing, times - 1)
            }
        }
    }

    fun postQuestion(question: QuestionPost, times: Int) {
        job?.cancel()
        job = viewModelScope.launch {
            val res = supportInteract.postQuestion(question)
            _postQuestion.emit(res)
            if (res.state == Resource.State.ERROR && times != 0 && isExceptionBelongsToNoInternet(
                    res.error!!
                )
            ) {
                VLog.d("No internet exception in posting a question : $times")
                delay(2500)
                postQuestion(question, times - 1)
            }
        }
    }

}