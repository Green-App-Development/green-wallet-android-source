

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
       'requestAssets': [{'assetId':'7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02','amount':'1.31'}
//           {'assetId':'nft1k7t35zy6kq4ss70jjf7h5kt6rydh9nk0yr8wfnyr0ugffg7j7m0s839gy4','amount':'1'}
        ]
    }).then((ans) => {
     let id=ans['id']
     let offer =ans['offer']
    console.log(id,"id from create offer method ")
    console.log(offer,"offer from create offer method")
    });
}

function helperFunction3() {
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqzjhfmr9ee4jmh04tytjfv3wy338lkr56hww97ut65lnthx4jued6d0mpatswm4x3mflmfasl4h75w6ll68d8ld8klul4pdflhpc97c97aaeu9w7kgm3k6uer5vnwg85fv4wp7cp7wakkphxwczhd6ddjtf24qev9r3dj44sy060emdaluuyllv4fea0x0060749264vm7fgu9an4c5vjv6z4x8d2yg9zl08s3y2e2zv8auj6c273763mpdeddxv7lp4a72ajwnr3y3u7dhs478h22lrmc2q89f54d8tg02fy0utvd8t6dslddl8cgl6jwtat8mjhfvjv06jt57h677qk76lujnupppjjqzmlhs8w6dhrxg4nse7jh3xydadlu7kstcsmysea66mh57tlukfqlqug3yqeuqtgctf8dg3kvwp06sp3h09puln600c897d2en7kad9fvrr5m3ul9y8tc8drluvm8hnl6j44v4dutwtrlhrz26ldd724ay24jyt2ukkvqrtd5qzfahl308xl0lhkmrx05famdhzlz4mfaak7te3x2whm62ec28ghuu7tll6878e08l7dl38qurtq55z28wje7lvqrgzjl0lsh9zz2vmf0w4vt0kay43cx97tusm8xfl74a64mx8h07ns0m7l93nhh5jn38968s8ynxewxh3h8wtk7w7kk9w88wa7mettlht5lkxptqcuxaarlk3dnlll0lmjelktm362z00h688vppnv76r9hpex03wmy7ktc7d7epetc00maedaj5j2f89jalq69qe0wcnj58djl2g8f3d6vz8wus54tvzawktls5qsqwmasygszmqap9"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
