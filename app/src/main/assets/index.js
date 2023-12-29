

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
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqemhmlaekcenaz02ma6hs5w600dhjlvfjn477nkwz369h88kll73h37fefnwk3qqnz8s0lle0mre89e06vlftnju4us3wywzn9wh9er84zru0d4kjmtemqxytj208s9jse96wllnt0ahda2x20ymlh3pwqaeu7fue4anp5hn74af8dwvdt9aatrhflad430e7vssyygwgrxdddnchuak648l26amarmtq56qhf376khwwjjfdrnu7ed5t3ahwf9s4ud0j3vfjhghwhlhnf5n448cfl7h0x2klwpa5ncapedvexzjzg3pktgx3evygkhayx8qarxv56xveflarxv55xupxqkftv9754ueq4hnl7l3gx7a888vndh2fwt0hmerugflcgjdvhr27meqhtd4wvq6tnr6aeulmnsmyu0lsas2hhmltjgrcmzj80rhh53n3r62gaxhyh77gvmwplv862j57qyys0547u5d66wd5hn8v4t4caf200w39z7tljtfe00g8vfw59tjvycm5l6rrvztqwnlxd7xwm7qp8lyzqkl08s7tl3pedkperrk0hmwmcz2d0n32h949nvkfhg0sdl43ejfsls0jgh5l7qufryjj2pw92ma057tfyhzjt7293856j73en9uan75eq45ut7jf395e2jdfnx56j34xvevcnfs4nx25t2wf5mrwn2vejkjcj4wx5ery2x2xyeryd9wx24zude39cc54j3s4exy7d3f9ny26n75kuenxv7s9085l7rmcs0xqykjpphfcqhkvtrcyng65u4c75ph3xm8tpa4qgwfl2flxrfydcn9tn33nlnl9st9fyanqs7jjj9lgku6f47fdx76jgvf9kyj9ztat8ql7llupqq48syx9fzszlxmjsqlg3p8eqp48yjjwdeu4zaj7kevkyk2xvfhxzet9w6d9j426dxckj723tfa9ucnefea9j52ewf9xdxd30xs4zn5629z4jcdzrmy8z7hggyqt9gfcpu6ygkjuqg38xvckd33ahwtlnmnq98tn64fk2ef8lhw75u70swelynyrv3k7ktm4qvpc8hfqxajjexqj9nnt4avn2wl7qe4hnfmph4e9sy9fvg4pzvl8gzeujt5n6wdhy5qevphlw4ulh2f00g6vh4l7lzumjs24ka8emkd4dhk7yzfku7k4wtk0h36t6x6tsyln46ugj2h5wecagan36zdcull73tprk8c5f86egaydjam57k4u4adtrzjy84809uu8rhhf2jlvt4ckax9flxc0q52akmd657hqv5gx90raqwkv5weh0ganw7phaahw87wnhpstje004f8j7z2fpqysmaue00tl7wnn4nwlv6g7el02nngq5jat7uhr67eqe2dpsa2qee4upyd4smkhux4scydnv6rtuaalu5mp4ludtw24wnu67a5fcaw5w9qhu36l0e0ka5lw90la7zdacgx00vw7t475fn3ldktfkqkg09v2pk5dflllpdzx8sk4emxyclzshg5j6aqzcwx30e7hf3dweh4w47whp638s059dc2dlutsf39gn4hddjhy98uvalrnq0n7cevhqh403enw8pca8vyqdhta7zx3yjcvks9rvfsqwqumv7sgcrs73ge0a0uq6pvrzjq8g9zjpvlvluhkp8v4900mhl5vmmnetasm9df5ykxd4fzu27g56ukm473mw9dkntzrrskpkn3ucdcr86uclk629xu9430n4e57n2c4n79ankplvwu3r576u85kw42kd09my5z0e57lyj79napfhjhn88r02mla4geh3ar9gtttjrl8taj928274lhmf0htehhkdl9t60z4famctnrrkdzrlzgahjmr8wa9gh3lfvdfqpds6flnzpwgmv"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
