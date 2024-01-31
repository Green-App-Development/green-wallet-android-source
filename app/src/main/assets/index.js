

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
    Bridge.interfaces.GreenWallet.createOffer({'offerAssets': [{'assetId':'7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02','amount':'1.12'},
        {'assetId':'','amount':'1.21'}],
       'requestAssets': [{'assetId':'8ebf855de6eb146db5602f0456d2f0cbe750d57f821b6f91a8592ee9f1d4cf31','amount':'1.31'},
           {'assetId':'6d95dae356e32a71db5ddcb42224754a02524c615c5fc35f568c2af04774e589','amount':'1.41'}
        ]
    }).then((ans) => {
     let id=ans['id']
     let offer =ans['offer']
    console.log(id,"id from create offer method ")
    console.log(offer,"offer from create offer method")
    });
}

function helperFunction3() {
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqzjhfmr9ee4jmh04tytjfv3wy338lkr56hww97ut65lnthx4jued6d0mpatswm4x3mflmfasl4h75w6ll68d8ld8klul4pdflhpc97c97aaeu9w7kgm3k6uer5vnwg85fv4wp7cp7wakkphxwczhd6ddjtf24qev9r3dj44sy060emdaluuyllv4fea0x0060749264vm7fgu9an4c5vjv6z4x8d2yg9zl08s3y2eypkmahxr96jx0au9senwa2yvhvrrupn0rcwwdcg46f3lawjm9zs34xr5zmkrywezkecy8ndmcgjm6eglr47h4ssvl4elc4uc7hnqkmh4kqltlx0e07fe7qs3efqpv85jn35msc83vmgl66st6fuy40lfwn4v6c8rnkj89sxkf2hk9edmmdjcfnqqkas038gu2qdrrsme5y0d3cqddh7nyamvctvmcxn8r2vylw5samawae9pn394vgvkzesjnw795h7xm8f68c2kttf7w9e5rs4xjr7caa40cpdpkg9flhlc9ulma77cmu2ewn8ed7atct8d8hkmfwkyce6l02mtpclznnnl0legtel0a0cerz3yp40klutkev6a82ju460drmde62uweqmhtwut4fvff3md3m4llyh5d0h00t60khqfahluupf5f7q2jw7hh3q47tk7lmmu8mmcacy0av7jyedh7acel2luatpd8wtead6zkmttsajh5x0ty64wcechaw9l005u0akum38c4xrh0hgtavgmuc36m92tvlj06zl4ertdya28ua8sll0l8g4eng3kaxl9mcaraed0qpkpc4guyselcn"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
