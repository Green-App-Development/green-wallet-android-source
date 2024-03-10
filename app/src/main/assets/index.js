

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
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxvgqqzjhfmr9ee4jmh04tytjfv3wy338lkr56hww97ut65lnthx4jued6d0mpatswm4x3mflmfasl4h75w6ll68d8ld8klul4pdflhpc97c97aaeu9w7kgm3k6uer5vnwg85fv4wp7cp7wakkphxwczhd6ddjtf24qev9r3dj44sy060emdaluuyllv4fea0x0060749264vm7fgu9an4c5vjv6z4x8d2yg9zl08s3y2e5r670lw5n9t8uzmke5e0828v3y5zmefknwdxda2lfmakdxrrnwxvj54qjfndtzdxyau3fnukwekn4h877ex0sfe95ul7kl44g3e2e0n5ltag5empupg3ask2gqtxd3rctm9dajlln9zh8mnekal484mfmxwfh303vrqkgfwenwem7h47hkffqx0qzex8gtmzkpkmst75qvdmeg08u7nm7pe0n2kvl4htf2tqcaxu08efp67pmgllrxeaul754dt9t0zmjclaccjkhmt0j40fz4v3z6h94nqq6c9rdu9lehlstu26edm0ntvfhdemvv0hukpj0nackeawkmc3fjvx3d9q7xtklg845wt5ppdklut6g8ntx0m7jl7llmvt8lww66mckxttg5uu57n09u7u3gkyl8efc55p8mvdlul8sslxsm056kum0gnau8dz686vrtas9dnt2hkngtwtpqcu49aj8pr4flz4pycce3slk8ck0lrlc8ulma77cmuzewn8e0742c08d8h76f2k56ej702mmpchrnhnldl3gtelll0uyvrrrtyzucez7f4txtf8gvpnskdgml5xewk39mslym6z4lna90pc4787jlahw778delc8v7up6tx400n2j9jwr06k4dtht35ar73mkye8r4j4num83kf33juu8q3mxydl8vasyw97lsz2hvl7hes3075gkmwetat444yuvl38hrh6n2pt76gtz742k3m456h4v2rd6yq6wxswlc9lkd2lvkj4f9zj3hj9fmdh2zv0fwu0824rwcetnn3fp2uk90ugv60vm005zv4k73p9a4w8cghjfrzj2ctk3t36udrw0ue9dmc2xp5ue2lua9a0mhlswfkt0t45zd79lxdlj34le7hyk869hh8ehvu0u7j2t4ldupsqzvfnezsln2gs3"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
