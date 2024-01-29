

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
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqzjhfmr9ee4jmh04tytjfv3wy338lkr56hww97ut65lnthx4jued6d0mpatswm4x3mflmfasl4h75w6ll68d8ld8klul4pdflhpc97c97aaeu9w7kgm3k6uer5vnwg85fv4wp7cp7wakkphxwczhd6ddjtf24qev9r3dj44sy060emdaluuyllv4fea0x0060749264vm7fgu9an4c5vjv6z4x8d2yg9zl08s3y2ekz39jd8cnlfef2awftfkmtsau68ck0967mcf0wd69t2nx980e9v6lksw2nf2cwk37sjglcke62hymp7mm70qnlayun6x0hd0jeyga4vhfe04lufvps7v5s6cr8yvyjcdlvh5jpealaexck0nr58mnssryehwl8jdhnakn5gy7wh49xehnjl5djqvwq9cdvtdq99zxe3c9l2qxxau58n70falquhe4tx06m4549svwnw8nu5sa0qa50l3nv770l22k4j4h3dev07uvftta4he2h532kg3dtj6esqddksqf8kl79uumal77mvve738hdkutu2hd8hkme0xyef6l0ft8pgazlnne0llglcl9ullehqpqpd2kmlm7srqawa00kjn5ec8e0e006j26l8008vhn3te9dkv79ylauu467plxhjnpk8navjr4kwh8na3j8us504eaa0esmm04x0nxanw7hjlat77893qp70u0mm94sre5rzkwfaum757za3j9arm4fzvvat79wtnhzs70ltsw4v8nlgyndkcak6kfuhuuun70uf8qn2mpv3sdukgje4a76h8t9vzy4l7la6atgq9wrr9mvu69gtp"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
