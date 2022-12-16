package extralogic.wallet.greenapp.domain.interact

import extralogic.wallet.greenapp.data.network.dto.support.ListingPost
import extralogic.wallet.greenapp.data.network.dto.support.QuestionPost
import extralogic.wallet.greenapp.domain.domainmodel.FAQItem
import extralogic.wallet.greenapp.presentation.tools.Resource

interface SupportInteract {

    suspend fun getFAQQuestionAnswers(): Resource<List<FAQItem>>

    suspend fun postListing(listing: ListingPost): Resource<String>

    suspend fun postQuestion(question:QuestionPost): Resource<String>

}