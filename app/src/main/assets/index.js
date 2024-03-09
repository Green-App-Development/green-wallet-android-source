

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

//nft1k7t35zy6kq4ss70jjf7h5kt6rydh9nk0yr8wfnyr0ugffg7j7m0s839gy4

function helperFunction2(){
    Bridge.interfaces.GreenWallet.createOffer({'offerAssets': [
//    {'assetId':'nft133fm6pgen3mmqpwz6xg577fnxrzuxcr4k5696c7vqq456mc5k9aqd87ns6','amount':'1'},
        {'assetId':'','amount':'0.0002'}],
       'requestAssets': [{'assetId':'8ebf855de6eb146db5602f0456d2f0cbe750d57f821b6f91a8592ee9f1d4cf31','amount':'1.31'},
           {'assetId':'nft1k7t35zy6kq4ss70jjf7h5kt6rydh9nk0yr8wfnyr0ugffg7j7m0s839gy4','amount':'1'}
        ]
    }).then((ans) => {
     let id=ans['id']
     let offer =ans['offer']
    console.log(id,"id from create offer method ")
    console.log(offer,"offer from create offer method")
    });
}

function helperFunction3() {
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqzjhfmr9ee4jmh04tytjfv3wy338lkr56hww97ut65lnthx4jued6d0mpatswm4x3mflmfasl4h75w6ll68d8ld8klul4pdflhpc97c97aaeu9w7kgm3k6uer5vnwg85fv4wp7cp7wakkphxwczhd6ddjtf24qev9r3dj44sy060emdaluuyllv4fea0x0060749264vm7fgu9an4c5vjv6z4x8d2yg9zl08s3y2e6rvuywm85llgwys8pe7suxthzwcl60y04u9tgdjwhpdrmgawgfahuy6sfxe753xnjv7g5e6thvmp6mnl0vn0eyuj6w0ht066ygu4vhe6047utves7q5q7ct9yq9j8es7n87wmm4r9d6sepw2f0nnl8fple42x5hxa8p548ke0gnylaprjgyejqtwcyctvt2qd8zqeeu90fsgxdl5ln70falqgkfltkt6thye9qvw8d83uu3atrajwl4nu779l2dkdj4nsaavm7uxft0a4hfzhv326tp9tzcecpdpkg9flhlc9ulma77cmu2ewn8ed7atct8d8hkmfwkyce6l02mtpclznnnl0legtel0a0cerz3yp40kluthehrmh9ladesap7usmesgyec57tec79h4dem7hhfah037xkwj5te6w5ea3u7tfayet98lzs8axudlew09260skavanj0esrdsgllphpdd75xn3w7tmswpld26ymmemh70h2hul0muny696ckxrkdjw2j7z6vn5gzcttxnvuv5cefgtpakwfll385p2wkd40entdqrkn4r3q3jyhx7mcnl4j274hctqzukvhh5qusy0n"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
