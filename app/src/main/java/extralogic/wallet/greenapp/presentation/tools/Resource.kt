package extralogic.wallet.greenapp.presentation.tools

import java.lang.Exception

/**
 * Created by bekjan on 25.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */

class Resource<T> private constructor(
    val state: State,
    val data: T?,
    val error: Exception?
) {

    enum class State {
        LOADING,
        SUCCESS,
        ERROR
    }


    companion object {
        fun<T> loading(): Resource<T> =
            Resource(
                State.LOADING,
                null,
                null
            )

        fun<T> success(data: T): Resource<T> =
            Resource(
                State.SUCCESS,
                data,
                null
            )

        fun<T> error(error: Exception): Resource<T> =
            Resource(
                State.ERROR,
                null,
                error
            )



    }
}