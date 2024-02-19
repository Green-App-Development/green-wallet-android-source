

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
       'requestAssets': [{'assetId':'8ebf855de6eb146db5602f0456d2f0cbe750d57f821b6f91a8592ee9f1d4cf31','amount':'1.31'}
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
    var takeOffer="offer1qqzh3wcuu2rykcmqvpsxzgqqx65r5edfjsytml75uw0862j0gdfu3a3zc76s62azgjn0yxkvf904szznqah60rkkl63mtlmga5l447u06ml28d0ll7q64lwpshas9umanufaadfkrfhekxcex5slwjpvu0aqpuaedvp7xasxwmk6ayw442pz467mmud0hm94frhdvpraj6cyk86w8u8mskklyjwn7u295n0vhr6hecd592u26ytq2ywa0arg3nyzf0990rttnjktufcfwl4hcamhdzg3rrmwp263xctlu7dt5nlv6t244gyjjzf9emsjemeaz5sxaahku9hhlkn6h05mu75v2jha4mx3ew5zdu6l3qwft78qsefqpvwh08xejm6n5mnackdga92mljesc7j7akmskuz6xp6krj9fs6t8t2gfh58tamm0kuraca7ftds6e9enx0gwnxvcmtce6tsaw2xawqu7fkeuepakvxf9qx33cdu6z8kcuqx3hd947v7ll2ds2l7w4hp2ht790w7yuwhz8zst72a0kuhmwc0lrhm7lpxkay88hk80y6m25eclhmp5tztg8dfkgpf0hlc9ulma77cmuwe7383d7atct8d8hkmewkyee6l0gmrpclznnnl0lagmel8u0u9l066rpsxkpd0vv64gmhfa4wfwu3edxhm3yl2ujk08y4r3j5s5hzwa7nptjl88n0m07mu3lvmjjzdg6ml07qs8hlv7tx7eha732mkhlqm2hulgslell8n7n80hq4wgeduuhq0m85za0qd2dnlz7krfd6cyu03zv9dwnv4vtkgmn2ujp0p224c8tlwld8peg0favg0a0scrs0r083ennuw93u660uhxfhg4agmdmv9zmpw65hh5e0m4dmv70l5jhq7jlfnm7a0lkulnge2uaw7eyamrnxrm8myhcdwn7re8h9djedkuek4twexqwfg244tjzr9raxpdsarpldl5ru5rm2anxf79luc63wve4mkfa3l3fcax54u8aztu0re0ee7e887e65zld5fl5d2atjnd3dhy3pxw2wl0fll4mn3v05lzhyhywpdhadt0r48wnjnygeywh57a4mgca4mgca3mg6a3mg7ap6cds69hqv0t3lc80wmjxal24hrkrra4pfhu0hke52j996k9znjfxxqn0uuratz8awphhjwnfe592vw67ssd3ayqwcazqtgsvrd4pk0zlunhru6mn03xydaaw3j99tkj75e9khr60mtm02cvxuvnm7z2zwykuecj609q293sqs2n2yxepfhxykqff86l79cc4dnrhh8lwfjj53uh88fh9a6yrklssre7786vqfcvkv39akfgf9jlch6q4rcs5d8dd8pl7p0u87hrthf60q7lg3rapl0u7hl3najqh9eaep5nduwez66kxmwuexp76s9mpq6udl30yr2pcq76wkacz2yn3pc0tmgxwp0dhaxc7mg2ypr8rxanlawn4khen8a0zzh9esnpupkms22t74jv9qcmlg4q0t74k3waslh24w72ge6vl7rxfjd542zg7ddjs5eteqg2vfhtce3ps9pr067lh7aef8t6uf3u8ay2s3ekyq23a5dghjs0w05v4f33r60qkq8ny9w4g6k3vh0"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
