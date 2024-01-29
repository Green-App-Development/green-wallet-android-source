package com.green.wallet.presentation.main.dapp.trade

import com.green.wallet.presentation.main.dapp.trade.params.CreateOfferParams
import com.green.wallet.presentation.main.dapp.trade.params.TakeOfferParams
import com.green.wallet.presentation.tools.VLog
import de.andycandy.android.bridge.CallType
import de.andycandy.android.bridge.DefaultJSInterface
import de.andycandy.android.bridge.NativeCall

class GreenWalletJS(
    private val onEvent: (TraderEvent) -> Unit
) : DefaultJSInterface("GreenWallet") {

    @NativeCall(CallType.FULL_PROMISE)
    fun connect() = doInBackground { promise ->
        onEvent(TraderEvent.ShowConnectionDialog)
        JavaJSThreadCommunicator.wait = true
        while (JavaJSThreadCommunicator.wait) {
            Thread.sleep(5000L)
            VLog.d("Waiting for waitingConnection : Name Thread :${Thread.currentThread().name}")
        }
        promise.resolve(JavaJSThreadCommunicator.connected)
    }

    @NativeCall(CallType.FULL_PROMISE)
    fun takeOffer(params: TakeOfferParams) = doInBackground { promise ->
        if (params.offer.orEmpty().isEmpty()) {
            promise.reject(Throwable("Offer is empty"))
            return@doInBackground
        }
        onEvent(TraderEvent.ParseTakeOffer(params.offer.orEmpty()))
        JavaJSThreadCommunicator.wait = true
        JavaJSThreadCommunicator.resultTakeOffer = ""
        while (JavaJSThreadCommunicator.wait) {
            Thread.sleep(5000L)
            VLog.d("Waiting for takeOffer : Name Thread: ${Thread.currentThread().name}")
        }
        if (JavaJSThreadCommunicator.resultTakeOffer.isEmpty())
            promise.reject(Throwable("ERROR"))
        else
            promise.resolve(JavaJSThreadCommunicator.resultTakeOffer)
    }

    @NativeCall(CallType.FULL_PROMISE)
    fun createOffer(params: CreateOfferParams) =
        doInBackground { promise ->
            if (params.offerAssets.orEmpty().isEmpty() || params.requestAssets.orEmpty()
                    .isEmpty()
            ) {
                promise.reject(Throwable("OfferAssets is empty"))
                return@doInBackground
            }
            onEvent(TraderEvent.ShowCreateOfferDialog(params))
            JavaJSThreadCommunicator.wait = true
            JavaJSThreadCommunicator.resultCreateOffer = hashMapOf()
            while (JavaJSThreadCommunicator.wait) {
                Thread.sleep(1000L)
                VLog.d("Waiting for createOffer : Name Thread: ${Thread.currentThread().name}")
            }
            promise.resolve(JavaJSThreadCommunicator.resultCreateOffer)
        }


}
