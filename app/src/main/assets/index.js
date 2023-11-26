

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
    Bridge.interfaces.GreenWallet.createOffer({'offerAssets':[{'assetId':'I asset id','amount':'amount offer'}],
                                               'requestAssets': [{'assetId':'I asset id','amount':'amount offer'}]
    }).then((ans) => {
     let id=ans['id']
     let offer =ans['offer']
    console.log(id,"id from create offer method ")
    console.log(offer,"offer from create offer method")
    });

}

function helperFunction2(){
    console.log('HelperFunction 2')
}

function helperFunction3() {
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqemhmlaekcenaz02ma6hs5w600dhjlvfjn477nkwz369h88kll73h37fefnwk3qqnz8s0lle0fp6yk0h45xxxn8kvuk8hjhv0qh0j7kxpdce9tz40m86xaed0duc46f2qyhfmm7a0lkuhnge2ulw7cyamrnxrm8myh6dxj78eh49dnefkvmk42waxlah7vamjqpdu99hafrkaaar4qumkf3lduh02830l09d0tg4atltyna60m8acralk9wxemg3uh3lpwxtth8d0vtqt2v2c4lnmm47888llv6htlnlj562wclrsqqf8tnp0vzgn0kn5rdkm4rfkm4rfkm4rf5mlfap5mf62xh7np4wtstesmlmnnc2adw3lrd4ej9g3xu50cse2uresn7anvvrwvasd0ms6txkj429jg28z69ttq2lulnkmmlecgl7e63n6lv7l5aa2t442fhujsc0mht3qcya54fq2acyx7v9revxprvmxs6l800l9xcd0lr2mj4t5lxhhdzw8t4r3g9lywhm7tahd8m3tll0sn0wzpnmmrhja04zvu0mdj6ds9jrcmh5zp35gqdxhlchjp48jl00sgmpczptqvvknlmls93wx33lj2nlx76vz7aj4tf7lf7yxxnvudhtkxhau9qnxk0r6gn8sf2z2f5llpwx0yrc9nuthal309rh685a2ltxlyxw8mltyk890jfapzakku9aetqfpw8hx479lvzkjeu70pughhn6duedegecal8yauslakhyyfjt03mxu6lz4gatu6psk3q9ssdvllchjp4yqstvytrzfqsdh2cyngzqmpvmtf7gukc4a9ul2u6a7lmgmaw4c53mclj8fpt2394wdkthmln5ullwalnthjkfejj4jaje2f8wxld06kvvyguu6um0mjj2vklw2kxm5pek82nzmnemne2v76w7w3zjy7he68034sjtha2kgqsldlakkde2wxkl4vqsqyqs050srj3ulj"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
