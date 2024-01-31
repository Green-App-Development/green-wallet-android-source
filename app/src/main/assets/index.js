

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
    Bridge.interfaces.GreenWallet.createOffer({'offerAssets': [{'assetId':'7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02','amount':'1.12'}],
//        {'assetId':'6d95dae356e32a71db5ddcb42224754a02524c615c5fc35f568c2af04774e589','amount':'1.12'}],
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
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqzjhfmr9ee4jmh04tytjfv3wy338lkr56hww97ut65lnthx4jued6d0mpatswm4x3mflmfasl4h75w6ll68d8ld8klul4pdflhpc97c97aaeu9w7kgm3k6uer5vnwg85fv4wp7cp7wakkphxwczhd6ddjtf24qev9r3dj44sy060emdaluuyllv4fea0x0060749264vm7fgu9an4c5vjv6z4x8d2yg9zl08s3y2e6zjrmm6aux752534na0kc0td7tl8gcxrwk0gtea7wzwes6k37r57shnq0fwdptp6686zfrlzm8f2unv8m00euz0l5nj0ge7a47ty3rk4ja897hl39nk87ylnpv3ffqfv0ne7fdffmlnxtlk6gka37d8amvv2gkv7lkf7tuy4a282ju7lj74dnmf8vsrtsp2nykzd4zqeec9ltsqxdl50n70falqckfhtk06m4y39qvwh083uu3atpaj0l3nv77dl2fkaj4n3aevt7uwftta4hf2hv326fpdtzcecpdfkgpf0hlc9ulma77cmuwe7383d7atct8d8hkmewkyee6l0gmrpclznnnl0lagmel8u0uemly6rqdvznsdgdm8r5lcq4r20aluznya43r4k45a2qdmhmu2xa4wacej3eae0e7a70c9reytt0akfcfxnvvhqwpd9g5g4muj0hm56ljy38x79m20vs2an0efsu6zu0dcmyxwl3cfjlwphqswjhwtm06x76uhxnzrmxwlr3hxn3z6cugjjhhex0h50ej960754pjww9famr5jt4dp420mtp2kd06fjdexxtll7vhk5rm2h98068076mnasgqqdz6qwxtayd6q"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
