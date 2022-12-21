package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.green.wallet.data.local.entity.FaqItemEntity

/**
 * Created by bekjan on 18.11.2022.
 * email: bekjan.omirzak98@gmail.com
 */

@Dao
interface FAQDao {

	@Insert
	suspend fun insertFAQS(faqs: List<FaqItemEntity>)

	@Query("SELECT * FROM FAQs WHERE langCode=:code")
	suspend fun getAllFAQList(code: String): List<FaqItemEntity>


}
