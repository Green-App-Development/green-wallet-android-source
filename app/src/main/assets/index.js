

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
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqzjhfmr9ee4jmh04tytjfv3wy338lkr56hww97ut65lnthx4jued6d0mpatswm4x3mflmfasl4h75w6ll68d8ld8klul4pdflhpc97c97aaeu9w7kgm3k6uer5vnwg85fv4wp7cp7wakkphxwczhd6ddjtf24qev9r3dj44sy060emdaluuyllv4fea0x0060749264vm7fgu9an4c5vjv6z4x8d2yg9zl08s3y2expx6hv0etauelztxf8ufr0txwl4q295mddufw8ynl5eh7c6rxrmn76dqjfwkv37yzeh2s72hhzjt0m9r5d85lx9p07807z4nme7vrtg76mnnwvurgauss2crtyv9d07nguemjmu7vhe6yam3psvuz07z25wcg03dtvshpc0gzwd3l8lkzveq8hyzu9mhysrfcsxw0pt6vzpn0d84e0wtzzt87d4fc6tq8m559whthwfcwu3wtxz4sk5y5hkpd9ljka6747z5jj6l43xdq5yekshk80dt7qntdkq2fahlp08wl0lhxmrx05fat0h27z3mfaak7ta3x2whm6wec28gkuu7mll6x78el8lkvcygqf2ahl6lcqmnxdl7uf4dn8fafdm0g48s9uh556s0hk0rmqfry2d42jxp2e8w6z7c7vk7mf0nz6dpldzmlcmt7dnkw6l67etv62zlhj7m0c78e08hln3kyljxll33x8ednumt0efj60j885jfm47mcncuncje77hh2lj4fm2mr0juqjctdllcuulu0ra64mtrgue7teke5l74mu5e9e7w98j4knlh4cawfk9hjqdc2ev25ecsr4n"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
