

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
//    Bridge.interfaces.GreenWallet.createOffer({'offerAssets':[{'assetId':'I asset id','amount':'amount offer'}],
//                                               'requestAssets': [{'assetId':'I asset id','amount':'amount offer'}]
//    }).then((ans) => {
//     let id=ans['id']
//     let offer =ans['offer']
//    console.log(id,"id from create offer method ")
//    console.log(offer,"offer from create offer method")
//    });

}

function helperFunction2(){
    console.log('HelperFunction 2')
}

function helperFunction3() {
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqemhmlaekcenaz02ma6hs5w600dhjlvfjn477nkwz369h88kll73h37fefnwk3qqnz8s0lle0xp7ak2smdvvm6u66lwlh7lyjf84alptu4dhft73jyhqmyckwwhquq32qyhfmm7a0lkuhnge2ulw7cyamrnxrm8myh6dxj78eh49dnefkvmk42waxgw9jrh2fs445f8c7ntv3q9rmgcnuq37639yhdn35sx9jwu7tp7ldeu43d92n8lc90aw76t0lj0ju9e6hy6daa0anxkkmccxw96ehfvwh0gdpzjk26zyx8px9m35tkmd5m5md5msmdkmcmdkm6rfax69m34lw6dnsuxlvqlxlu7z80tvdcmd7v3jxfhyr6ykthqlvql8wmtqt38vptkakke9442sv5u48meapeajc40c8dmya949z4f27f3qnazf37xr78sm6kslt39euakz9v9wgzhw8qhn7qum83sezlx282e5wn00m2wcmfxrns76wwvu789h8akujy7t6eumll75jc5h740mydyw78x88kf4fjjyuj6stjtnvaqtxgsqr5jklvlcp59np7jqeg9zjqyldluhcsh7lcjuurglec3ht78fj9umm0lpmmlc8ealn7asckha0m22wemls6g9eyjhl9lsfk2lh0q8u8jw0hks32ywvm3dnavk442uamkdhk667yaln7ujlxw4c3pc9tvu0cmxcdlcty30jh05wh7ad97y4klvsxxwladqet9uah0ex0497n0r68e80y7w95z4533gwh60lqfpw6fms2sfxrnn5v46aw00fz7mrstt29y2wck4elhdq493nh7mmv7aj7gkq5wqcvlmqvhrhqppkqu0v6g45w84hv3uhlvra4jh4sgr9n4dpjxpucdaw8r4u3lhj4m5cjhhtaxhu3nsh4al7jcu2dd8al5r3swqjrxnyf06ekp99vcmen09geqqrja8kersf40xh7942ksa0ee9ahxjct5jt5m8jk20fmzv9n7t0c8dwtnqqypu7jyqs6w7m7"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
