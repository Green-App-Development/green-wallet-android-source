package com.green.wallet.presentation.main.dapp.trade


object JavaJSThreadCommunicator {

    @Volatile
    var wait = true

    @Volatile
    var connected = false

    @Volatile
    var resultTakeOffer: String = ""

    @Volatile
    var resultCreateOffer: HashMap<String, String> = hashMapOf()

}