package extralogic.wallet.greenapp.data.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Created by bekjan on 20.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface CryptocurrencyService {

    @GET
    suspend fun getLatestCurrency(@Url url: String): Response<JsonObject>


}