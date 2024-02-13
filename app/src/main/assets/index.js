

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
    Bridge.interfaces.GreenWallet.createOffer({'offerAssets': [{'assetId':'7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02','amount':'20.1'},
        {'assetId':'','amount':'0.0002'}],
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
    var takeOffer="offer1qqr83wcuu2ryhmva0929x4c7caphzqvtg4wypz4zdqgjhpnqzgyghqyzqk46ez5z2jgjctett9ygygg5qsge2p4d3zqxs59zsg5yx32ptx6zq59f9s3xty29zc24zhqpz75yhcf4y8gr8n8gssusw88fmn70rynn7t9hmalhm6al0aaemae7udcgq33ryskpawxtnslzhfk9e86lxutrulfsmly0sstyt3c3lclgaka94gkguaza65xut7wq423l4rl2p75rag8650aglmc0l2h6paxwuyjule9elnja8vc9j3eh09znrtmw299803galhyu6w7hf46rj68wjtqvrvrxcxjhjxcwhuj3vtk2hylamse64hn9hp44pdjh49d759r2ythr99etq4qq5gvvrujlf8lgm39dy30gnda0kwnjfjhmnwju5a34qkre5fep6lvrc0cazdeg9kmnh9234chq09wvksjumhh6d7fxsufml6yflf8808ttjj967um7knpef23z9lqnkywex53c3yua2ygu2azl58k32fygf2mrfg8mnxu57ecedt43652asf73pulmsgpjky39avqv2qy2qy2qx2qx2qx2pxr3fgcntfysrp9vfwfz78jgvace26relf64em5hmhvazxhtv6u6hduu697yc9fqz2rxl3fge65ma6lqtpukt0als8zc4vwfm24j4j54xg76tmedh88fln9tj7244uqv2qy2qy2qx2qx2qx2pxr3fgcstt9kzpcj8jjl0086xkjt9h2jav3nh4ek4t7dsj5ss870g5taw5qxfvz6px83fvcunrtpsfkre9q844qa5ny70mq5mdfzga3w3wts783qe88f47pdj8vqvgqv2qy2qy2qx2qx2rxr3fvcwww76tde77hmdmx7n7pn6xclan4298n2nal4mkzy50pxm2mdjyng8fgz2rxv0fscf480q2z33nh2cvu02s9m2rmdz4v7jlak7x2vktt0uty6the460v728dwcs03lxqr3gq3gqegqegqegqcgqc789rzpslcldf43pkdckgatkdkjc40de6mhm7qrsudaqxar6gk046n6v63sp3ggmg4sxmd3gy78d50ztzreare4w3uwmkw4ny23jw7t4xf3yu7jjm8rymspdwm0gasr4t3k7lph5gz8wejcprw8l0uqdkrfqydqm7gnkmgez0k6j2g9fa0m9vk4ga6med2jvef5zgwj7h4d7hv7lzxa5djdq3ra0qsjsyyh8sahdh0n3wj8n7735eexd2x996c8tuxn9pja7gdsgdp87a6y2l2pvj9mh07pwl8taffyrmm95tfg55aktdv0xqktyt8jw4yuwf8252948getcswpamv9jrcz7g8xrhhsletc0nv9lmt7w8gzp67yfc54axcum3fue9acgu0l82napn6c0gnk4f57yd3va03hwmv2yl9sdp59m7wzqszjh870upv2q5pdxw0uwemj7q2vnjykce32q6qaxv8ytsvjsszgjpjqrjlaq8ezjpxrsdfzjk82q5ycg26cr3nqu2z3pywd7633ywzrhv5ec90q9ejs3zpeku5rdyyxlwrjnlv3zxr5tqqtr4jc6jwwxy4u6jyj539806fz2zakscujtgp5tva79pkjszjn9ela8vtackz20a2ashun8fqjw5v98xuuamvkdpkyvtxyswgzcr2gnnmzktsc8rdrjnn0rpa9rxr0jjanmq6fym9fd5qklxelnssvz5mudpp6xzv83tztpwgzjgq8kqydghm6y5ksg59sn0gdc2sqknzcty9g0qd8l2s3c6z93pzgtaxscejnv4nmwh6ktune6fmnvkrmk8rh9xg3wc3utezqgcw3ykwkrv0yhd4nnlrew8ye2kydm0lwk237m0929kpu2yljwgp6a8u7gcah2llhhxf5ta9j2a8vf0da904nu4tz2r723jlrq7cddtdeph2uk3g8t6yvcrc2vctcndd8kjjv8repy8w5xnmwha56mv7q3xmzynek6eh36hqkmp2hdlzn6a8jdguk2ede7jdp2hty004x25qt8xmqxgpnr2spvc6qh7crq2ee4ywl5eljlcmzvatj88hm30l0qkycn69azuphhykj32d4943e4avyagzmejpdt0rt9nmgx3vj2rsmntrl7edf9n4qwvtuqxnja8a4aq9lmyykjjff0pzjtdxv0uqhlvczswnt9eeq6l5lvk2ukjuyz62va0jl4g0uryp0yj2vxyfyg0pp55ftlu42gu7cmv5v3962yv2j6mqvm7l6n0ynkf0mmm42jyl5xqkdgndgqda8ur2wd3j3wkm8jmtfk2ytnvtpflq0zc98tldyx83cf8x7xp47wal96q76t2hd0mm34m27yrntjmlndycz6qfg3933zyk0aeazynl77d9kcakwwaehgt06dpwzf8ksmkk3zmy0u8l2zez03dgkjh3vcs95qk3w2jr4hlsacqazujvg497vjyyxq7fvsap6yp2jtp6c00afr7xyrgh6quyzskgu6rzftqrzvz586t56gzs992ty0etjrrjrgna49vqsew8zmc2syzfrakmek5lghssa65hrmpht023dvu4gfn9zel4l0vcx205jhnrw3hm6k6hgwv6lsljtud05h2t4t8rxfe2pwdtgpa9fnq50jcedxpzzt3rgguweg923y0c8my8nu3lg5ch8rt30hhqulpv39jcfag7mrs53kd6kypnmt3zd2xcyddmreal8mc962qs8zquqw26j58q38zqur7q79mkpnhpxypcvys8qlnq458qfn33zvuyw60e98y7eh65lnnfmx4mhtyyjdlefp8aenyf2pwk0nr0k59mv3e07t755lzcrmmqrqtndp5q6k6lfh7phlw0u3qv8euj6f39m5dn8k8pej7kc0l4phmtnh206fywy20fftqmd0f5374jd5a0rf0f8nekjwmx9apa76f6ru0e07tp09fazhvryh97dnmahgmjd7tp5nrdvsr3lkjh9lwjx0nht5lw6kt25fd2zxx92dq80gnd8hsx8gkv2v7eqlu6ukvfyg9at59pgpzjuetlq54ryq53qh9tdwjgznkmt92p96m3m93gmtxjy8at6fjmdf8jft4m8nvwyad260h0nx04h03kndhkzg4w0ecreg32583lv8m6x5wsxv405gs9jkekkpz8eg3v03cc9c8v6dma8dht24haee45xzulplq5h276sm64qcgjzr5hga5356pchaw02d3wyh38j5yaf7rurd0sm0vjwuudxpy5r3skc3xh270j4rj4j5uk520ve7a967sup6rz2pv3t27sa7vkk93e6g96akynkwadwjs765tajp0dkdhd77vrueg0zpmyxe23pcnx8dj6va2famnyky7uahhche239yg3qjjns5dk7afrlhvwf9zh004773ww4z5hl4d6xgexrdwwxh2yy7w5j2ae6a38666jdpte4420yknpyswap0f2lv0h0fwam9r9dzkchsm866zzmswne4wykg3t4584tmm6mvrfagha3ztxtw939e5h9w0mh246yj4vuk00kqtj8lgkcngwuwvav4nfu3uctkmq9mlgxk83m090y5farm7pkxufr6va4wnrf46k40gnx37haywl4ek0rw7qmgcwjmkuyl4es87a9lz09yld5qcpu7687"
    Bridge.interfaces.GreenWallet.takeOffer({'offer':takeOffer}).then((ans) => {
        console.log(ans,"Ans from take offer method")
    });
 }

btn.onclick=helperFunction;
btn2.onclick=helperFunction2;
btn3.onclick=helperFunction3;
