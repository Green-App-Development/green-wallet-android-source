

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
    var takeOffer="offer1qqr83wcuu2rykcmqvpsxygqqzjhfmr9ee4jmh04tytjfv3wy338lkr56hww97ut65lnthx4jued6d0mpatswm4x3mflmfasl4h75w6ll68d8ld8klul4pdflhpc97c97aaeu9w7kgm3k6uer5vnwg85fv4wp7cp7wakkphxwczhd6ddjtf24qev9r3dj44sy060emdaluuyllv4fea0x0060749264vm7fgu9an4c5vjv6z4x8d2yg9zl08s3y2exq3mc5zqv6nf5au46ydv29fea4pdlw867a0j3thex0vh4vlln9xehqtev8qhfml7ddlkah4gefun077y9crh8ne8nxhkvxj706h4ya4e34vhh4vwa8s9z8kzefqpvn3ka2fp8hg44rm7majw8hthx36ltlwtfexduf9ewdlf8dxk0vtk05lehyqeuqtverrf3rqmdcd72q8keuyxphdd47v7la2ds2m7w4hp2h074dw7yuwhz8zs27wa0kuhmwu0lphnllpxxly0xhj8ly6nt5ac04mp5txtc9dpkg9flhlc9ulma77cmu2ewn8ed7atct8d8hkmfwkyce6l02mtpclznnnl0legtel0a0c9zjjype0klutkc947xe87t2wc4une7eufjah8n3su984hcnrq09ghnjrwnu58azl02fsdlnlclat69cekl0ng08akc7l4xj6ckazh4v2p6yp7dv6ltu00klt90avt9q99cjn64tm38afqf3r0jc0rweg4dh0z7u9yv5d47cmefr5ldhdewvn5e63wuh02lnr6wawa4vlann5u7lt9l93x7aw708sxj487986nyuc5qzc3kejg9g6hhn"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
