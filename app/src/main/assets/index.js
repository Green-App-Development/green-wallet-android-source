

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
       'requestAssets': [{'assetId':'8ebf855de6eb146db5602f0456d2f0cbe750d57f821b6f91a8592ee9f1d4cf31','amount':'1.31'},
           {'assetId':'nft133fm6pgen3mmqpwz6xg577fnxrzuxcr4k5696c7vqq456mc5k9aqd87ns6','amount':'1'}
        ]
    }).then((ans) => {
     let id=ans['id']
     let offer =ans['offer']
    console.log(id,"id from create offer method ")
    console.log(offer,"offer from create offer method")
    });
}

function helperFunction3() {
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxvgqqpt8j0lh3tet293jc0t5hd36vmdazla64futpucvsmdg4rdmgkal37p6vrk7f6w36628tlcfs8tfruwkjlelepra5g0f0dqh624rvr7l0te78pme5yld6vw6fa6acl8z0az8npzg4k7afmlrzf0xlelct56h708klu768r75pv7has76lju7xs4pucfaawvu27ws8f4pylljn2mqtmeljs40jp3yr3y8k9h0k3ndpu464vak92knm44edhxuvcm7fmxpmv2ap2cs3w84ru0amgjgq503lhsmh0s0eecqh9ncf6867uc0m93xwc9sd7m7lq6ntcajke92uhyjd6m7npuy0vk08czyk9a90786ges5zj2fgkmfd0n6t9v5jajf0ffxyl4wte8xvh4k093yzh4j0ffx2knz2e4xy6dfjxh955d9dezkzcjjdxc65un7d4skymtpjxc6j4j3sxgcn92354c4ryd30x98vjv42fp8rgtewexky34dhxsmn85etep9l7l7qrdjpd5q2d2vqdarzc6py7x499284xdugkaccddg8rkd2c0e36aqwyej6u5w0k7fwza2fddqrrw6ymw2qh7jarnr0hx5jtzff3yjcj7jechuhnlc0q3gvrqeggcp8mkv5q06yzfxvqagfx5mjwt9chvhnktx39jejzfesk2etk0fue2kjf295mjut6tf0xyktw0fvhzk2jffn9juve59c6ukj3s4v5zsn74qga56rpupfqrw90xszrd8zzggfnxa3vqdahnha7v6p06yu4w0z52ema8h4d8mmr6wf2ec6y344klafrvvp3mqxhy5k8sv5vqca0tvlnnaq2dam6k6dhwpwp92tq9qfnhc2zklqj9xwljde95ymqd8c4l9aknma6x34n0hkcl8u6rd2hf774n9wd443wnas89hm6uneu6j7hh2cpt7dzkffj37r6w3tx5azk3wq8hl5wea93zxj07kvffqu404r4d04ttjlc5spmgn70lqcnu6w5lezm04kfptlehru9z5dsmkn846ntzpjtqucd4npr6wa6xvam5vlfvemll5acv2ukr6dyfu4s7ncgpyyln8thm8anvla2607x69kfm6hu7z9vkz6h4m7nkgr28tvf2qrwf0qxkkkrgwleschj7x0qgsjgm7ll06q79fkkexw27xu0dsw794vvznh958m5v7f7sref3k9k053ttrkfj7qw4rzjcmv8eme5mu892fecam4hacumc5lunp4dm27ksu38vz0hh3dxlk95qg2vxpg5srka6lthxdp98474qc9pxlrdh65lal73mr3j38272zu6kthf7whhvv5m7w32hp2r7d3jtka54v4qelr920f746hm5cd08udht4dnxu0rv5s3s3hfez3p6jjg8va0qwf67qun4upe8tcrjwhs8ya0pzz50usmdjtf8tcqst7uz7v7a75u7xhmf5tcmtwvn2gfhprkxxwkq7vu7hqmrxmr8vttw6x264s42kv0zfel3m9ldharalrk67xgmecr540pu4q68fn9v7t2td22huugtt7ssdt3tglvr93a5vkps3qmrmqg23pmzkfgg6zz8ztupghtwmgg23wmyspwahls99l9r0vt09k7nz400s0kmlh7wps08k5qmdtcf9zch2vedxs50sllq5a7h3fm8u6d740fhcskqkwfw9u9n3d2xlttthzdlw49a6cu8p77m4q95pjgfk6dlctpz6e4lktx5gxtnhelhmaa3hc9jax0jla24s7w600a5j4df4n9u74hkr3w88087mlzshn7pueqd9pmp9u0acwlrdrfsh5yqjelxthpahazf90nwt8ewf98p60lqn7d99x8txtjhqddmpsrlflm29pkmw93kmwy3jmwx366w9smp5zkrs7hrlcw4etp6tgnpld9ysxpqeh4n47slp5k4lah6d8daznn4aclql4rla3k9xrequp7akpuc6glxf56rq8k6h8lhhfk8nrwkajh7p6krhtphjrvhqr53v5k3pr5jy3ndz2sqvf2ussw4ztf30x8gqetkhlt7cyauwyn2vj06mlhllrfhlljttdnas9559nuhmuaw4rm4vyf9sxh65dyvg92tkdh5rmfe0ur2medtju0d6dyehrwuf8e5glg5aql0twuj8w9zxu4qy9ge0n0lq54hsatywnh06tt32d3tkp7d77047n97wdgzna00h8f643dqvlcpkkmrszeunlc8sm3fttzvhem0tsszehhm583t0z6ggnujkdez69za8kkrf2n2m70m6nyg7q5m2vzfkwzywnwgjag7yrtfrv233s9qw8d4pnajwnvudm4kp0tudxyl0jvdye8lal97yht0q2u8u6xdzzd8vuv7v8addkyxqlrl2wlvhavytl277kscnfhufxx4m3er555syj8xt7fdmzkh5ndu39ln39rv28d2904l7antytcujlcx3cw4n399ww7sexhn34vqqqvhlu7pgmue4je"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
