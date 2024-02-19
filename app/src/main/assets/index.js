

var btn=document.querySelector('.btn');
var btn2=document.querySelector('.btn2');
var btn3=document.querySelector('.btn3');

function helperFunction(){

    Bridge.interfaces.GreenWallet.connect().then((ans) => {
        console.log(ans,'Call from Android for checking connection');
    });


//    Bridge.interfaces.GreenWallet.connect().then((ans) => {
//         console.log(ans,"Ans from connect method")
//    });
//
//    Bridge.interfaces.GreenWallet.transfer({'to':'to address','amount':'3213','assetId':'this is a assetId'}).then((ans) => {
//          console.log(ans,' Ans from method transfer')
//    });
//

}

function helperFunction2(){
    Bridge.interfaces.GreenWallet.createOffer({'offerAssets': [{'assetId':'6d95dae356e32a71db5ddcb42224754a02524c615c5fc35f568c2af04774e589','amount':'0.05'},
        {'assetId':'','amount':'0.0002'}],
       'requestAssets': [{'assetId':'8ebf855de6eb146db5602f0456d2f0cbe750d57f821b6f91a8592ee9f1d4cf31','amount':'1.31'},
           {'assetId':'nft133fm6pgen3mmqpwz6xg577fnxrzuxcr4k5696c7vqq456mc5k9aqd87ns6','amount':'1'}
        ]
    }).then((ans) => {
     let id=ans['id']
     let offer =ans['offer']
    console.log(id,"id from create offer method ")
    console.log(offer,"offer from create offer method")
    });
}

function helperFunction3() {
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqa6wdw7l95dk68vl9mr2dkvl98r6g76an3qez5ppgyh00jfdqw4uy5w2vrk7f6wj6lw8adl4rkhlk3mflttacl4h7lur64as8zlhqhn8h04883476dz7x6mny6jzdcga33n4s8n884cxcekcemz6mk3jk4v925trha2kuwcv0k59xl377mx32g5h2c52wfycczdlns04vgl4cx77f6d8xs4f3m23zpghmeuyfzkds7873mffmtnhzkr20fmcp8u3f83u8xnhduwl504fylawl37u4axqazr4qjfwkv37yzeh2s72hhzjt0m9r5d85lx9p07807z4nme7vrtg76mnnwvlld74x2gzzr8yv987nwfmugxfvlv5pkewm57a48ckxa5qvrh6xwxd4n0fa8mrvm9tdlnvkcx0ls4x767g86uw0v77lmyhja89jy8gkmk5t2n054mlr5u4y4mwjcqk338nezk3cmxx3gmxxsgmxksgn8k5gqx9c6dnzuzqlklasxa3q5wdk2jkqnlp78dhhlncnlej488aaeelfl654t24ndedrsnk7hz3nfng25q4esfpj77586d98fvxpr0d05e0wmzztx7f4ec6rp8m5y8wlthwfgvue0tzz9skuy5nk3d9lnke6w47z4jj60h3wdq5yf5slk80dtusnwcg6yzkm4lctemhmlaekcenaz02ma6hs5w600dhjlvfjn477nkwz369h88kll73h370elanvdds9luy6kqcedfxyfva8n9dlcpjs487l7ptffkre3e7dtt6hwj4kexpw5k6ftu08kj3mfql8pmyuhhcm0h4w7jh7myp5l4luz65kcwaj08wa84p4kfmz2msmemn6774lkn3h4tjwu5hqetge2j4wd5svv5575vw5qag8qghugz3v8l58jwhxhrn3htk7k3um6e406d6g6n3fnqrt67quw0lna5pjgfk6mlctpz6mdqyke5pa2v6yrpzcfw6pg6quyc2p7mpf40fv7wucketcucnshvx7hx7l3rpkaltyjla8clwe60t4efvcllgzsyrcm5hcrlh4rrm22z8fry0z6yee9kf4xu6563ps80jgsanjmqrvw0zmmuek054z4clfa4evaz6reynmlgnuvu7ne2zjul5ml7mafhqtqpn8sw8uxzngxa"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
