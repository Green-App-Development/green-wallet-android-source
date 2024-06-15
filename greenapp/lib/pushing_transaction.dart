import 'dart:convert';
import 'dart:math';

import 'package:bs58check/bs58check.dart';
import 'package:chia_crypto_utils/chia_crypto_utils.dart' hide Client;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_splash_screen_module/SpeedyTransaction.dart';
import 'package:flutter_splash_screen_module/flutter_token.dart';
import 'package:flutter_splash_screen_module/save_full_coins_create_offer.dart';
import 'package:http/http.dart';

import 'TokenInfo.dart';
import 'nft_service.dart';

class PushingTransaction {
  static const MethodChannel _channel =
      MethodChannel('METHOD_CHANNEL_GENERATE_HASH');

  final cachedWalletChains = Map<String, WalletKeychain>();

  init() {
    final speedy = SpeedyTransaction(_channel);

    _channel.setMethodCallHandler((call) async {
      switch (call.method) {
        case "generateHash":
          {
            var mnemonic =
                call.arguments['mnemonic'].toString().split(' ').toList();
            var prefix = call.arguments['prefix'].toString();
            var defTokens =
                call.arguments['tokens'].toString().split(' ').toList();
            var observer = call.arguments['observer'];
            var nonObserver = call.arguments['non_observer'];
            print(
                'Generated Keys got called on flutter from android : with argument : ${call.arguments}');
            try {
              generateKeys(mnemonic, prefix, defTokens, observer, nonObserver);
            } catch (ex) {
              _channel.invokeMethod("exception", ex.toString());
            }
          }
          break;
        case "generateHashImport":
          {
            var mnemonic =
                call.arguments['mnemonic'].toString().split(' ').toList();
            var prefix = call.arguments['prefix'].toString();
            var defTokens =
                call.arguments['tokens'].toString().split(' ').toList();
            var observer = call.arguments['observer'];
            var nonObserver = call.arguments['non_observer'];
            print(
                'Generated Keys got called on flutter from android : with argument : ${call.arguments}');
            try {
              generateKeysImport(
                  mnemonic, prefix, defTokens, observer, nonObserver);
            } catch (ex) {
              _channel.invokeMethod("exception", ex.toString());
            }
          }
          break;
        case "generateSpendBundleXCH":
          {
            print("CreateSpendBundle got called on flutter side from android");
            try {
              var args = call.arguments;
              generateSpendBundleXCH(
                  fee: args['fee'],
                  amount: args['amount'],
                  mnemonic:
                      args['mnemonicString'].toString().split(' ').toList(),
                  httpUrl: args['url'],
                  destAddress: args['dest'],
                  networkType: args['network_type'],
                  spentCoinsJson: args['spentCoins'],
                  observer: args['observer'],
                  nonObserver: args['nonObserver']);
            } catch (e) {
              debugPrint("Caught exception in dart code" + e.toString());
              _channel.invokeMethod("exception", e.toString());
            }
          }
          break;
        case "generateSpendBundleToken":
          {
            print("Generate SpendBundle got called for token on flutter side");
            try {
              var args = call.arguments;
              generateSpendBundleForToken(
                  fee: args['fee'],
                  amount: args['amount'],
                  mnemonic:
                      args['mnemonicString'].toString().split(' ').toList(),
                  httpUrl: args['url'],
                  destAddress: args['dest'],
                  networkType: args['network_type'],
                  asset_id: args['asset_id'],
                  spentCoinsJson: args['spentCoins'],
                  observer: args['observer'],
                  nonObserver: args['nonObserver']);
            } catch (ex) {
              debugPrint(
                  "Caught exception in dart code token  " + ex.toString());
              _channel.invokeMethod("exception", ex.toString());
            }
          }
          break;
        case "generatewrappedcatpuzzle":
          {
            var args = call.arguments;
            print(
                "GenerateWrappedCatPuzzle got called on flutter with arguments : ${call.arguments}");
            generateCATPuzzleHash(
                args["puzzle_hashes"].split(' '), args["asset_id"].toString());
          }
          break;
        case "changeSettings":
          {
            var args = call.arguments;
            debugPrint(
                "Change settings wallet hashes with arguments ${call.arguments}");
            changeSettingsWalletPuzzleHashes(
                mnemonics:
                    args['mnemonicString'].toString().split(' ').toList(),
                observer: int.parse(args['observer'].toString()),
                nonObserver: int.parse(args['non_observer'].toString()),
                assetIds: args['asset_ids'].toString().split(' ').toList());
          }
          break;
        case "asyncCatPuzzle":
          {
            var args = call.arguments;
            print(
                "Async GenerateWrappedCatPuzzle got called on flutter with arguments : ${call.arguments}");
            asyncCATPuzzleHash(
                args["puzzle_hashes"].split(' '), args["asset_id"].toString());
          }
          break;
        case "initWalletFirstTime":
          {
            var args = call.arguments;
            var mnemonics = args["mnemonics"].toString().split(' ');
            var observer = int.parse(args["observer"].toString());
            var nonObserver = int.parse(args["non_observer"].toString());
            debugPrint("Caching Wallet KeyChain First Time With args : $args");
            cachedWalletKeyChain(mnemonics, observer, nonObserver);
          }
          break;
        case "unCurryNft":
          {
            var args = call.arguments;
            debugPrint("JsonNftCoin to uncurry the method called args : $args");
            var nftCoin = args["coin"].toString();
            var nftParentCoin = args["parent_coin"].toString();
            unCurryNFTCoin(nftCoin: nftCoin, nftParentCoinJson: nftParentCoin);
            break;
          }
        case "generateNftSpendBundle":
          {
            try {
              var args = call.arguments;
              debugPrint("generateNftSpendBundle got called with args : $args");
              var nftCoin = args["coin"].toString();
              var mnemonics = args["mnemonics"].toString().split(' ');
              var observer = int.parse(args["observer"].toString());
              var nonObserver = int.parse(args["non_observer"].toString());
              var destAddress = args["destAddress"].toString();
              var fee = args['fee'];
              var spentXCHCoins = args['spentCoins'];
              var baseUrl = args["base_url"];
              var fromAddress = args["fromAddress"];
              generateNFTSpendBundle(
                  nftCoinJson: nftCoin,
                  mnemonics: mnemonics,
                  observer: observer,
                  nonObserver: nonObserver,
                  destAddress: destAddress,
                  fee: fee,
                  spentCoinsJson: spentXCHCoins,
                  base_url: baseUrl,
                  fromAddress: fromAddress);
              break;
            } catch (ex) {
              debugPrint("Exception in parsing args from android : $ex");
              _channel.invokeMethod("nftSpendBundle");
            }
          }
          break;
        case "unCurryNFTSecond":
          {
            var args = call.arguments;
            debugPrint("unCurryNFTSecond got called with args : $args");
            var parentCoinInfo = args["parent_coin_info"].toString();
            var puzzleHash = args["puzzle_hash"].toString();
            var startHeight = int.parse(args["start_height"].toString());
            var mnemonics = args["mnemonics"].toString().split(' ');
            var observer = int.parse(args["observer"].toString());
            var nonObserver = int.parse(args["non_observer"].toString());
            var baseUrl = args["base_url"];
            unCurryNFTCoinSecond(
                mnemonics: mnemonics,
                observer: observer,
                nonObserver: nonObserver,
                startHeight: startHeight,
                parentCoinInfo: parentCoinInfo,
                puzzleHash: puzzleHash,
                baseUrl: baseUrl);
          }
          break;
        case "puzzle_hash_to_address":
          {
            try {
              var puzzleHashHex = call.arguments.toString();
              debugPrint("Puzzle Hash Hex got called : $puzzleHashHex");
              var minterDid = Address.fromPuzzlehash(
                  Puzzlehash.fromHex(puzzleHashHex), "did:chia:");
              _channel.invokeMethod(
                  "puzzle_hash_to_address", minterDid.address);
            } catch (ex) {
              _channel.invokeMethod("exception");
            }
          }
          break;
        case "CATToXCH":
          {
            try {
              var args = call.arguments;
              var mnemonics = args["mnemonics"].toString().split(' ');
              var url = args["url"].toString();
              var assetId = args["asset_id"].toString();
              var xchAmount = int.parse(args["amountTo"].toString());
              var catAmount = int.parse(args["amountFrom"].toString());
              var observer = int.parse(args["observer"].toString());
              var nonObserver = int.parse(args["nonObserver"].toString());
              var fee = int.parse(args["fee"].toString());
              var spentCoins = args['spentCoins'];
              tibetSwapCATToXCH(
                  mnemonics: mnemonics,
                  url: url,
                  assetId: assetId,
                  xchAmount: xchAmount,
                  catAmount: catAmount,
                  observer: observer,
                  nonObserver: nonObserver,
                  fee: fee,
                  spentCoinsJson: spentCoins);
            } catch (ex) {
              debugPrint(
                  "Exception occurred in exchanging cat for xch : ${ex.toString()}");
              _channel.invokeMethod("exception");
            }
            break;
          }
        case "XCHToCAT":
          {
            try {
              var args = call.arguments;
              var spentXCHCoins = args['spentXCHCoins'];
              var mnemonics = args["mnemonics"].toString().split(' ');
              var url = args["url"].toString();
              var assetId = args["asset_id"].toString();
              var catAmount = int.parse(args["amountTo"].toString());
              var xchAmount = int.parse(args["amountFrom"].toString());
              var observer = int.parse(args["observer"].toString());
              var nonObserver = int.parse(args["nonObserver"].toString());
              var fee = int.parse(args["fee"].toString());
              tibetSwapXCHToCAT(
                  mnemonics: mnemonics,
                  url: url,
                  assetId: assetId,
                  xchAmount: xchAmount,
                  catAmount: catAmount,
                  observer: observer,
                  nonObserver: nonObserver,
                  fee: fee,
                  spentXCHCoinsJson: spentXCHCoins);
            } catch (ex) {
              debugPrint(
                  "Exception occurred in exchanging xch for cat : ${ex.toString()}");
              _channel.invokeMethod("exception");
            }
            break;
          }
        case "CATToAddLiquidity":
          {
            try {
              var args = call.arguments;
              var mnemonics = args["mnemonics"].toString().split(' ');
              var url = args["url"].toString();
              var tokenAssetId = args["token_asset_id"].toString();
              var tibetAssetId = args["tibet_asset_id"].toString();
              var xchAmount = int.parse(args["xchAmount"].toString());
              var catAmount = int.parse(args["catAmount"].toString());
              var liquidityAmount =
                  int.parse(args["liquidityAmount"].toString());
              var observer = int.parse(args["observer"].toString());
              var nonObserver = int.parse(args["nonObserver"].toString());
              var fee = int.parse(args["fee"].toString());
              var spentCoins = args["spentCoins"].toString();
              offerAddLiquidity(
                  mnemonics: mnemonics,
                  url: url,
                  tokenAssetID: tokenAssetId,
                  xchAmount: xchAmount,
                  catAmount: catAmount,
                  observer: observer,
                  nonObserver: nonObserver,
                  fee: fee,
                  liquidityAmount: liquidityAmount,
                  tibetAssetID: tibetAssetId,
                  spentCoinsJson: spentCoins);
            } catch (ex) {
              debugPrint("Exception in offering cat to tibet : $ex");
              _channel.invokeMethod("exception");
            }
          }
          break;
        case "CATToRemoveLiquidity":
          {
            try {
              var args = call.arguments;
              var mnemonics = args["mnemonics"].toString().split(' ');
              var url = args["url"].toString();
              var tokenAssetId = args["token_asset_id"].toString();
              var tibetAssetId = args["tibet_asset_id"].toString();
              var xchAmount = int.parse(args["xchAmount"].toString());
              var catAmount = int.parse(args["catAmount"].toString());
              var liquidityAmount =
                  int.parse(args["liquidityAmount"].toString());
              var observer = int.parse(args["observer"].toString());
              var nonObserver = int.parse(args["nonObserver"].toString());
              var fee = int.parse(args["fee"].toString());
              var spentCoins = args["spentCoins"];
              offerRemoveLiquidity(
                  mnemonics: mnemonics,
                  url: url,
                  tokenAssetID: tokenAssetId,
                  xchAmount: xchAmount,
                  catAmount: catAmount,
                  observer: observer,
                  nonObserver: nonObserver,
                  fee: fee,
                  liquidityAmount: liquidityAmount,
                  tibetAssetID: tibetAssetId,
                  spentCoinsJson: spentCoins);
            } catch (ex) {
              debugPrint("Exception in offering tibet to cat : $ex");
              _channel.invokeMethod("exception");
            }
          }
          break;
        case "AnalyzeOffer":
          {
            try {
              var args = call.arguments;
              var offer = args["offer"];
              analyzeTakingOffer(offerStr: offer);
            } catch (ex) {
              debugPrint("Exception in getting params form analyzing offer");
              _channel.invokeMethod("exception");
            }
          }
          break;
        case "PushingOffer":
          {
            try {
              var args = call.arguments;
              debugPrint("PushingOffer args got called : ${call.arguments}");
              var offer = args["offer"].toString();
              var mnemonics = args["mnemonics"].toString().split(' ');
              var url = args["url"].toString();
              var observer = int.parse(args["observer"].toString());
              var nonObserver = int.parse(args["nonObserver"].toString());
              var fee = int.parse(args["fee"].toString());
              var spentCoins = args["spentCoins"].toString();
              var requestedNFT = args["requestedNFT"].toString();
              debugPrint(
                  "RequestedNFT on PushingOffer : ${call.arguments["requestedNFT"].toString()}");
              takingAnOffer(
                  offerString: offer,
                  mnemonics: mnemonics,
                  url: url,
                  observer: observer,
                  nonObserver: nonObserver,
                  spentCoins: spentCoins,
                  fee: fee,
                  requestedNFT: requestedNFT);
            } catch (ex) {
              debugPrint(
                  "Exception in getting params from pushing offer : $ex");
              _channel.invokeMethod("exception");
            }
          }
          break;
        case "SpeedyTransferXCH":
          {
            try {
              var args = call.arguments;
              debugPrint(
                  "SpeedyTransferXCH args got called : ${call.arguments}");
              speedyTransferXCH(
                  fee: args['fee'],
                  amount: args['amount'],
                  mnemonic: args['mnemonic'].toString().split(' ').toList(),
                  httpUrl: args['url'],
                  destPuzzleHash: args['dest'],
                  networkType: args['network_type'],
                  spentCoinsJson: args['spentCoins'],
                  tranCoinsJson: args['tranCoins'],
                  observer: args['observer'],
                  nonObserver: args['nonObserver']);
            } catch (ex) {
              debugPrint(
                  "Exception in getting params from RePushing tran : $ex");
              _channel.invokeMethod("exception");
            }
          }
          break;
        case "SpeedyTransferCAT":
          {
            var args = call.arguments;
            var fee = args['fee'];
            var amount = args['amount'];
            var mnemonic = args['mnemonic'].toString().split(' ').toList();
            var url = args['url'];
            var type = args['network_type'];
            var spentCoins = args['spentCoins'];
            var tranCoins = args['tranCoins'];
            var observer = args['observer'];
            var assetID = args['assetID'];
            var nonObserver = args['nonObserver'];
            debugPrint(
                'Args speedy cat : $tranCoins $fee $amount $nonObserver $assetID $nonObserver $observer $spentCoins $type $url $mnemonic');
            try {
              speedyTransferCAT(
                  fee: fee,
                  amount: args['amount'],
                  mnemonic: args['mnemonic'].toString().split(' ').toList(),
                  httpUrl: args['url'],
                  destPuzzleHash: args['dest'],
                  networkType: args['network_type'],
                  spentCoinsJson: args['spentCoins'],
                  tranCoinsJson: args['tranCoins'],
                  observer: args['observer'],
                  assetId: args['assetID'],
                  nonObserver: args['nonObserver']);
            } catch (ex) {
              debugPrint(
                  "Exception in getting params from RePushing tran for token : $ex");

              _channel.invokeMethod("exception");
            }
          }
          break;
        case "SpeedyTransferNFT":
          {
            try {
              var args = call.arguments;
              debugPrint("SpeedyTransferNFT got called with args : $args");
              var nftParentCoinInfo = args["coinParentInfo"].toString();
              var mnemonics = args["mnemonics"].toString().split(' ');
              var observer = int.parse(args["observer"].toString());
              var nonObserver = int.parse(args["non_observer"].toString());
              var destAddress = args["destAddress"].toString();
              var fee = args['fee'];
              var spentCoins = args['spentCoins'];
              var tranCoins = args['tranCoins'];
              var baseUrl = args["base_url"];
              var fromAddress = args["fromAddress"];

              speedyTransferNFT(
                  nftCoinParentInfo: nftParentCoinInfo,
                  mnemonics: mnemonics,
                  observer: observer,
                  nonObserver: nonObserver,
                  destAddress: destAddress,
                  fee: fee,
                  baseUrl: baseUrl,
                  tranCoins: tranCoins,
                  spentCoins: spentCoins,
                  fromAddress: fromAddress);
            } catch (ex) {
              debugPrint(
                  "Exception in getting params from RePushing tran : $ex");
              _channel.invokeMethod("exception");
            }
          }
          break;
        case "CreateOffer":
          {
            try {
              var args = call.arguments;
              debugPrint("CreateOffer got called with args : $args");
              var mnemonics = args["mnemonics"].toString().split(' ');
              var observer = int.parse(args["observer"].toString());
              var nonObserver = int.parse(args["nonObserver"].toString());
              var fee = int.parse(args['fee'].toString());
              var spentCoins = args['spentCoins'].toString();
              var baseUrl = args['url'].toString();
              var requested = args["requested"].toString();
              var offered = args["offered"].toString();
              creatingOffer(
                  mnemonics: mnemonics,
                  url: baseUrl,
                  observer: observer,
                  nonObserver: nonObserver,
                  fee: fee,
                  spentCoins: spentCoins,
                  offeredStr: offered,
                  requestedStr: requested);
            } catch (ex) {
              debugPrint("Exception in getting params from CreateOffer : $ex");
              _channel.invokeMethod("exception");
            }
          }
          break;
        case "testing":
          {
            var offer =
                "offer1qqr83wcuu2rykcmqvpsxygqqemhmlaekcenaz02ma6hs5w600dhjlvfjn477nkwz369h88kll73h37fefnwk3qqnz8s0lle00zu8ysw9su6m9xj76favk0p4m4m9a2a6a6lkphu6ns8uhlmanr4rxlc32zy4nksvnlnenckt5hnpehmdm4eh3flcx02ms4d0paz7uy8v0wmaevrtchlady4v5q8qdvp4lsmuahherk43clx402v7nr50tjajfwatqgjx28k67mcl626a6z58wtl8mcq58fftwcdzcme96hjevnuk8fwzet79vltz3s8clmamrc85gc2k3mp3c23q76wgda62yer55tj0l6zyeygfc2yp5nkstadt3jp00haalrsdu62wkexh0ykuwu0nn87sras3y2mww5aln3vkntul79myuan8hv24m3nul4u5nt6a8wdkv43wrma0v9dh5d4f4pkdscv7leykes0mp7j548sppyra90h9rwknnd9uem92aw822nmm5fghjluj6wtm6pmzt4p2unpxxar6scmqjcr50fn0snk0sqfleqs9hmeu8jlugwtdswgcana7mk7qjntuu24edfvm9jd6rur0avwvjv8uruj9a8lsxzgey5jst32klta8j6f9c5jlj3vfax5h5wve08vl4xg9d8zl5jvfdx25n2ve3xv5jehfu65ene49jk5ejkv948u5d624v6nxd3s9s4yk2ene5eu5j90eumu5vwv4g5u3nw29n95s2f39nxzl4pjxceaq270flu8h3qvvqtdyqpwrsq0vckqsupwh7vkfa2zknadu49flue0smhlva3gce0d6msl4uhy049tw7ydaelhldl9qt73v34nraj6d94y5cjfvf82vk2kwelah7gyqptlyxvgjar96v89qpu3xz05qn2wf9y6mt62fm9mdjevfdyvctdv9nxvavetf24j6d3d9a9ykn6t4385ntet9f95ujfvkvmz7dz2fxej529tf32q8w2wpu7ssspkz3nsr34l3d9uqerwgcp2hp52hgfnuxr2rehvltyjmkczwmrdfm5jpl0dxa6dns37l7kv3crcqme9x3sz48qk9tjmhuuh2zlwwc4kndm3m5fzsc9fzgaxzsahgz3ejh5n70d3ycpfwp0ewdull23v0vmua58elxum636hk4yemhd4dt57vre4u7h9wtxvh4emkx20wg7n3g5tj7h5ffchgjs3qralur7w4yfpknm5r8fgg98acatn72kuhu9vrwjy04lg9yl8rh8w2klud5gja79el8gdqv2a5ma7565sv5c89rpd8w7t5dfh8g6nw6p8umld8wwmhpsvh2hd92tpxx0nn8lsjhw20sf3zjl05qlnkj9vvmgmdjl8ulm9uzs8k6n35x84gp8xhsq3kkrwme5expqeh4n47slp5k4lah6d8daznn4aclql4rla3k9xrequp7akpuc6glxf56rq8k6h8lhhfk8nrwkajh7p6krhtp4v2nkyqft7ll07qllkkje78x3le8k27a0drfrnhxa4j7mk0pw5d6macwemnhhee25fallctt2zrwhklv8crycd0ctz7qxqr66zewqa22dmh3h30202edl2h7qcv2zhvxtgfsxxcc8q8acszg23m7gr9qs2czhr8lalurhzla0vkuhks3lv262jsy3lzeenu729386ur072xjve08mfxvfla95r632h3r0yx4k8cyccsdsmnajr9puemv7m2uy7djp28mv9unqcl6x8nl6vhrj020m64kkzl54x46caf8xz7l0x2nx0un3kxl6m7alrz4f38kmr9ln9hac4uydy37sun9mvqgl8jt40z05rm03ehgflljmfrxdavvm0fwhlgzkhtcwldq0n7npuejdhszqxn5vcwwcl8sn4";
            var result = dexieOfferId(offer);
            debugPrint("Result for pushing transaction : $result");
            _channel.invokeMethod("testing");
          }
          break;
      }
    });
    // offeringXCHForCat();
    // offeringCatForXCH();
    // testingMethod();
    testingNamesDao();
    final nftId = NftAddress(
        "nft1j7hsh8uwg2ce0s8xagrcm6m5uzqjr3qdm4y45zwprddfyna80srq8y9q86");
    debugPrint("NftId Address on Flutter : ${nftId.toPuzzlehash()}");
    analyzeOffer(
        offerStr:
            "offer1qqr83wcuu2rykcmqvpsxygqqa6wdw7l95dk68vl9mr2dkvl98r6g76an3qez5ppgyh00jfdqw4uy5w2vrk7f6wj6lw8adl4rkhlk3mflttacl4h7lur64as8zlhqhn8h04883476dz7x6mny6jzdcga33n4s8n884cxcekcemz6mk3jk4v925trha2kuwcv0k59xl377mx32g5h2c52wfycczdlns04vgl4cx77f6d8xs4f3m23zpghmeuyfzktsvljul5lf6fragjk9qdl5ewns7nam4t68j7e4se98ynkx67tjl3kfx0zse96wllnt0ahda2x20ymlh3pwqaeu7fue4anp5hn74af8dwvdt9aatrhf5pqzt2qgvnwecw8nkvmr8mqw9r83t77l05j0slhyegkjhfnvd024xtj0mxwjthmj3zgpnyqx33cqwjsnvcuzl5crrw7xrqakw6lww0c47699l82mgktl765h0rw823r3g908whmwmlhz8lkmp7lujr0jhnt4zljde56xa8uaqm9h9vxkqey94lalur70u7m0vd7pdhdnuhl24v8nknmlvy3t60ve083dajur3elel5lc49ulsv7kqnzy9smsndcr2hd07has6sjax77tmkm59e2vyce5g7revgd8a8jh6jg3zwaz9pujzh3gdlwkjjqc9xh8jw707rsde2utp3xzxks44axmempy2wmr2622mtjk0658jlkha7j3yhswmx0dxwnjg0h7zfujf64czed7mdz67lx60hsav0u2z783f2akdkcaduyavkdpjfynttzeqlul0kh68gltqcpfpfdkcgepjqhv0tdapgq8wtt37y4534ts");
  }

  String dexieOfferId(String offerData) {
    final hash = offerData.toBytes().sha256Hash();
    return base58.encode(hash.byteList);
  }

  Future<void> creatingOffer(
      {required List<String> mnemonics,
      required String url,
      required int observer,
      required int nonObserver,
      required int fee,
      required String spentCoins,
      required String offeredStr,
      required String requestedStr}) async {
    try {
      NetworkContext()
          .setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);

      final fullNodeRpc = FullNodeHttpRpc(url);
      var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";

      final keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonics, observer, nonObserver);

      final fullNode = ChiaFullNodeInterface(fullNodeRpc);
      final offerService =
          OffersService(fullNode: fullNode, keychain: keychain);

      final puzzleHashes =
          keychain.hardenedMap.entries.map((e) => e.key).toList();
      keychain.unhardenedMap.entries.forEach((element) {
        puzzleHashes.add(element.key);
      });

      List<String> spentCoinsParents = [];
      if (spentCoins.isNotEmpty) {
        List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoins);
        for (var item in spentCoinsJsonDecoded) {
          var parentCoinInfo = item["parent_coin_info"].toString();
          spentCoinsParents.add(parentCoinInfo);
        }
      }

      var offered = parseFlutterTokenJsonString(offeredStr);
      var requested = parseFlutterTokenJsonString(requestedStr);

      List<FullCoin> fullCoins = [];

      Map<String, List<Coin>> spentCoinsMap = {};
      final nftService =
          NftNodeWalletService(fullNode: fullNode, keychain: keychain);

      var offerMap = offerAssetDataParamsOffered(offered);
      Map<OfferAssetData?, List<int>> requestMap =
          <OfferAssetData?, List<int>>{};

      for (var token in offered) {
        if (token.type == "XCH") {
          await saveFullCoinsXCH(fee, url, token, spentCoinsParents, fullCoins,
              spentCoinsMap, fullNode, keychain);
        } else if (token.type == "CAT") {
          keychain.addOuterPuzzleHashesForAssetId(
              Puzzlehash.fromHex(token.assetID.toString()));
          await saveFullCoinsCAT(url, token, spentCoinsParents, fullCoins,
              spentCoinsMap, fullNode, keychain);
        } else {
          var nftCoins = await nftService.getNFTCoinByParentCoinHash(
              assetId: Bytes.fromHex(token.assetID),
              puzzle_hash: Puzzlehash.fromHex(token.fromAddress));
          final nftCoin = nftCoins[0];
          final nftFullCoin = await nftService.convertFullCoin(nftCoin);
          fullCoins.add(nftFullCoin);
        }
      }

      for (var item in requested) {
        final amount = item.amount;
        if (item.type == "CAT") {
          keychain.addOuterPuzzleHashesForAssetId(
              Puzzlehash.fromHex(item.assetID.toString()));
          final tokenHash = Puzzlehash.fromHex(item.assetID);
          requestMap[OfferAssetData.cat(tailHash: tokenHash)] = [amount];
        } else if (item.type == 'XCH') {
          requestMap[null] = [amount];
        } else {
          var nftCoin = await nftService
              .getNftFullCoinWithLauncherId(Puzzlehash.fromHex(item.assetID));

          final nftFullCoin = await nftService.convertFullCoin(nftCoin!);
          fullCoins.add(nftFullCoin);
          requestMap[OfferAssetData.singletonNft(
              launcherPuzhash: nftFullCoin.launcherId)] = [1];
        }
      }

      final changePh = keychain.puzzlehashes[2];
      final targetPh = keychain.puzzlehashes[3];

      debugPrint("SpentCoinsParents on creatingOffer: $spentCoinsParents");
      debugPrint(
          "Offer Mapping when creating an offer $offerMap : $spentCoinsParents");
      debugPrint("Request Mapping when creating an offer $requestMap");

      final offer = await offerService.createOffer(
          requesteAmounts: requestMap,
          offerredAmounts: offerMap,
          coins: fullCoins.toSet().toList(),
          changePuzzlehash: changePh,
          targetPuzzleHash: targetPh,
          fee: fee);
      final str = offer.toBench32();

      _channel.invokeMethod("CreateOffer",
          {"offer": str, "spentCoins": jsonEncode(spentCoinsMap)});
    } catch (ex) {
      debugPrint("Exception in creating an offer : $ex");
      _channel.invokeMethod("ErrorPushingOffer");
    }
  }

  Future<void> speedyTransferNFT(
      {required String nftCoinParentInfo,
      required List<String> mnemonics,
      required int observer,
      required int nonObserver,
      required String destAddress,
      required int fee,
      required String baseUrl,
      required String tranCoins,
      required String spentCoins,
      required String fromAddress}) async {
    try {
      var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";

      final keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonics, observer, nonObserver);

      NetworkContext()
          .setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);

      debugPrint(
          "NFTCoin to Send after decoding and parsing : $nftCoinParentInfo and baseURL : $baseUrl");

      final fullNodeRpc = FullNodeHttpRpc(baseUrl);
      final fullNode = ChiaFullNodeInterface(fullNodeRpc);

      List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoins);
      List<String> spentCoinsParents = [];
      for (var item in spentCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        spentCoinsParents.add(parentCoinInfo);
      }

      List<dynamic> tranCoinsJsonDecoded = json.decode(tranCoins);
      List<String> tranCoinsParents = [];
      for (var item in tranCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        tranCoinsParents.add(parentCoinInfo);
      }

      List<Future<void>> futures = [];
      List<Coin> standardCoinsForFee = [];
      if (fee > 0) {
        List<Puzzlehash> myPuzzlehashes =
            keychain.unhardenedMap.keys.toList().sublist(0, observer);

        myPuzzlehashes
            .addAll(keychain.hardenedMap.keys.toList().sublist(0, nonObserver));

        List<Coin> feeStandardCoinsTotal =
            await fullNode.getCoinsByPuzzleHashes(myPuzzlehashes);

        feeStandardCoinsTotal.sort((a, b) {
          return b.amount.compareTo(a.amount);
        });

        debugPrint("TranCoinsParents : $tranCoinsParents");

        var curFee = 0;
        for (final coin in feeStandardCoinsTotal) {
          var contains =
              tranCoinsParents.contains("0x${coin.parentCoinInfo.toString()}");
          debugPrint(
              "Contains checking for trans coins : $contains : ${coin.parentCoinInfo.toString()}");
          if (contains) {
            curFee += coin.amount;
            standardCoinsForFee.add(coin);
          }
        }

        if (curFee < fee) {
          for (final coin in feeStandardCoinsTotal) {
            var isSpent = spentCoinsParents
                .contains("0x${coin.parentCoinInfo.toString()}");
            debugPrint("IsSpent checking for spent coins : $isSpent");
            if (!isSpent) {
              curFee += coin.amount;
              standardCoinsForFee.add(coin);
            }
            if (curFee >= fee) {
              break;
            }
          }
        }
      }

      final nftService =
          NftNodeWalletService(fullNode: fullNode, keychain: keychain);
      debugPrint(
          "PuzzleHash to get nft coins on speedy up hash : ${Puzzlehash.fromHex(fromAddress)} Info : ${Bytes.fromHex(nftCoinParentInfo)}");
      var nftCoins = await nftService.getNFTCoinByParentCoinHash(
          assetId: Bytes.fromHex(nftCoinParentInfo),
          puzzle_hash: Puzzlehash.fromHex(fromAddress));
      final nftCoin = nftCoins[0];
      debugPrint("Found NFTCoin to send  $nftCoin");

      final nftFullCoin = await nftService.convertFullCoin(nftCoin);
      debugPrint("Converting to FullNFTCoin : $nftFullCoin");
      debugPrint('Standard XCH for fee : $standardCoinsForFee');

      final destPuzzleHash = Puzzlehash.fromHex(destAddress);
      debugPrint("Dest Puzzle Hash on speedy up : $destPuzzleHash");

      await Future.wait(futures);
      var bundleNFT = NftWallet().createTransferSpendBundle(
        nftCoin: nftFullCoin.toNftCoinInfo(),
        keychain: keychain,
        targetPuzzleHash: destPuzzleHash,
        standardCoinsForFee: standardCoinsForFee,
        fee: fee,
        changePuzzlehash: keychain.puzzlehashes[0],
      );

      var bundleNFTJson = bundleNFT.toJson();
      _channel.invokeMethod('SpeedyTransferNFT', {
        "spendBundle": bundleNFTJson,
        "spentCoins": jsonEncode(standardCoinsForFee)
      });
    } catch (ex) {
      debugPrint("Exception in sending nft token speedy : $ex");
      _channel.invokeMethod("failedNFT", "Sorry, something went wrong : $ex");
    }
  }

  Future<void> speedyTransferCAT(
      {required int fee,
      required int amount,
      required List<String> mnemonic,
      required String httpUrl,
      required String destPuzzleHash,
      required String networkType,
      required String tranCoinsJson,
      required String spentCoinsJson,
      required int observer,
      required String assetId,
      required int nonObserver}) async {
    try {
      debugPrint(
          "flutter arguments for token fee : $fee amount : $amount  mnemonic : $mnemonic url : $httpUrl dest : $destPuzzleHash isTypeNetwork : $networkType asset_id : $assetId spentCoinJson : $spentCoinsJson observer : "
          "$observer non-observer : $nonObserver");
      var stopwatch = Stopwatch()..start();
      var key = "${mnemonic.join(" ")}_${observer}_$nonObserver";
      var keyChain = cachedWalletChains[key] ??
          generateKeyChain(mnemonic, observer, nonObserver);

      Network chosenNetwork;
      switch (networkType) {
        case "Chia":
          chosenNetwork = Network.mainnet;
          break;
        case "Chia TestNet":
          chosenNetwork = Network.testnet10;
          break;
        case "Chia TestNet":
        default:
          chosenNetwork = Network.mainnet;
      }

      NetworkContext().setBlockchainNetwork(blockchainNetworks[chosenNetwork]!);

      final fullNodeRpc = FullNodeHttpRpc(httpUrl);
      final fullNode = ChiaFullNodeInterface(fullNodeRpc);

      debugPrint(
          'Time taken: ${stopwatch.elapsedMilliseconds / 1000}s  to initialize keyChain');

      List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoinsJson);
      List<String> spentCoinsParents = [];
      for (var item in spentCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        spentCoinsParents.add(parentCoinInfo);
      }

      List<dynamic> tranCoinsJsonDecoded = json.decode(tranCoinsJson);
      List<String> tranCoinsParents = [];
      for (var item in tranCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        tranCoinsParents.add(parentCoinInfo);
      }

      List<Coin> standardCoinsForFee = [];

      if (fee > 0) {
        List<Puzzlehash> myPuzzlehashes =
            keyChain.unhardenedMap.keys.toList().sublist(0, observer);

        myPuzzlehashes
            .addAll(keyChain.hardenedMap.keys.toList().sublist(0, nonObserver));

        List<Coin> feeStandardCoinsTotal =
            await fullNode.getCoinsByPuzzleHashes(myPuzzlehashes);

        feeStandardCoinsTotal.sort((a, b) {
          return b.amount.compareTo(a.amount);
        });

        var curFee = 0;
        for (final coin in feeStandardCoinsTotal) {
          var contains =
              tranCoinsParents.contains(coin.parentCoinInfo.toString());
          if (contains) {
            curFee += coin.amount;
            standardCoinsForFee.add(coin);
          }
        }

        if (curFee < fee) {
          for (final coin in feeStandardCoinsTotal) {
            var isSpent =
                spentCoinsParents.contains(coin.parentCoinInfo.toString());
            if (!isSpent && coin.amount != 0) {
              curFee += coin.amount;
              standardCoinsForFee.add(coin);
            }
            if (curFee >= fee) {
              break;
            }
          }
        }
      }

      final keyChainCAT = keyChain
        ..addOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(assetId));

      // get outer puzzle hashes from keychain
      final myOuterPuzzlehashes = keyChainCAT
          .getOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(assetId));

      keyChainCAT.hardenedMap.keys.forEach((element) {
        var outer = WalletKeychain.makeOuterPuzzleHash(
            element, Puzzlehash.fromHex(assetId));
        myOuterPuzzlehashes.add(outer);
      });

      CatWalletService catWalletService = CatWalletService();

      stopwatch = Stopwatch()..start();

      final responseDataCAT =
          await fullNode.getCoinsByPuzzleHashes(myOuterPuzzlehashes);

      debugPrint(
          "My Second Response From retrieving cat  : $responseDataCAT, to get unspent coins ${stopwatch.elapsedMilliseconds / 1000}s");

      List<Future<void>> futures = [];

      List<Coin> allCatCoins = responseDataCAT;

      allCatCoins.sort((a, b) {
        return b.amount.compareTo(a.amount);
      });

      debugPrint("TranCoinsParents after parsing : $tranCoinsParents");

      List<Coin> filteredCoins = [];
      var sum = 0;
      for (final coin in allCatCoins) {
        var contains = tranCoinsParents.contains("0x${coin.parentCoinInfo}");
        if (contains) {
          filteredCoins.add(coin);
          sum += coin.amount;
          if (sum >= amount) {
            break;
          }
        }
      }

      debugPrint("FilteredCoins after parsing : $filteredCoins");

      List<CatCoin> catCoins = [];
      for (final coin in filteredCoins) {
        futures.add(getCatCoinsDetail(
            coin: coin,
            httpUrl: httpUrl,
            catCoins: catCoins,
            fullNode: fullNode));
      }
      debugPrint("Sending cat coins future size : ${futures.length}");
      await Future.wait(futures);
      debugPrint("Sending cat coins : $catCoins");
      debugPrint("Sending xch coins : $standardCoinsForFee");
      final spendBundle = catWalletService.createSpendBundle(
          payments: [Payment(amount, Puzzlehash.fromHex(destPuzzleHash))],
          catCoinsInput: catCoins,
          keychain: keyChainCAT,
          changePuzzlehash: keyChainCAT.puzzlehashes[0],
          fee: fee,
          standardCoinsForFee: standardCoinsForFee);

      _channel.invokeMethod('SpeedyTransfer', {
        "spendBundle": spendBundle.toJson(),
        "dest_puzzle_hash": destPuzzleHash,
        "spentCoins": jsonEncode(standardCoinsForFee),
        "spentTokens": jsonEncode(filteredCoins)
      });
    } catch (e) {
      debugPrint("Caught exception in dart code for token $e");
      _channel.invokeMethod("exception", e.toString());
    }
  }

  Future<void> speedyTransferXCH(
      {required int fee,
      required int amount,
      required List<String> mnemonic,
      required String httpUrl,
      required String destPuzzleHash,
      required String networkType,
      required String tranCoinsJson,
      required String spentCoinsJson,
      required int observer,
      required int nonObserver}) async {
    try {
      debugPrint(
          "fee : $fee amount : $amount  mnemonic : $mnemonic url : $httpUrl dest : $destPuzzleHash isTypeNetwork : $networkType  hashCounter : $observer");
      var key = "${mnemonic.join(" ")}_${observer}_$nonObserver";
      final keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonic, observer, nonObserver);
      Network chosenNetwork;
      switch (networkType) {
        case "Chia":
          chosenNetwork = Network.mainnet;
          break;
        case "Chia TestNet":
          chosenNetwork = Network.testnet10;
          break;
        case "Chives":
          chosenNetwork = Network.chivesnet;
          break;
        case "Chives TestNet":
          chosenNetwork = Network.chivestestnet;
          break;
        default:
          chosenNetwork = Network.mainnet;
      }
      NetworkContext().setBlockchainNetwork(blockchainNetworks[chosenNetwork]!);
      StandardWalletService standardWalletService = StandardWalletService();
      List<Puzzlehash> myPuzzlehashes =
          keychain.unhardenedMap.keys.toList().sublist(0, observer);

      myPuzzlehashes
          .addAll(keychain.hardenedMap.keys.toList().sublist(0, nonObserver));

      Map<String, dynamic> body = {
        "puzzle_hashes": myPuzzlehashes.map<String>((e) => e.toHex()).toList()
      };

      final responseData =
          await post(Uri.parse("$httpUrl/get_coin_records_by_puzzle_hashes"),
              headers: <String, String>{
                'Content-Type': 'application/json; charset=UTF-8',
              },
              body: jsonEncode(body));

      List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoinsJson);
      List<String> spentCoinsParents = [];
      for (var item in spentCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        spentCoinsParents.add(parentCoinInfo);
      }

      List<dynamic> tranCoinsJsonDecoded = json.decode(tranCoinsJson);
      List<String> tranCoinsParents = [];
      for (var item in tranCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        tranCoinsParents.add(parentCoinInfo);
      }

      debugPrint("SpentCoinsPrototypes on sending xch : $spentCoinsParents");

      if (responseData.statusCode == 200) {
        final List<Coin> allCoins = [
          for (final item in jsonDecode(responseData.body)['coin_records'])
            Coin.fromChiaCoinRecordJson(item)
        ];

        allCoins.sort((a, b) {
          return b.amount.compareTo(a.amount);
        });

// debugPrint("Sorted all coins : $allCoins");

        List<Coin> requiredCoins = [];
        var sum = 0;
        var total = amount + fee;
        for (final coin in allCoins) {
          var isUsed =
              tranCoinsParents.contains(coin.parentCoinInfo.toString());
          if (isUsed && coin.amount != 0) {
            sum += coin.amount;
            requiredCoins.add(coin);
          }
        }

        for (final coin in allCoins) {
          var isSpent =
              spentCoinsParents.contains(coin.parentCoinInfo.toString());
          if (!isSpent) {
            sum += coin.amount;
            requiredCoins.add(coin);
          }
          if (sum >= total) {
            break;
          }
        }

        final spendBundle = standardWalletService.createSpendBundle(
          payments: [Payment(amount, Puzzlehash.fromHex(destPuzzleHash))],
          coinsInput: requiredCoins,
          keychain: keychain,
          changePuzzlehash: keychain.puzzlehashes[0],
          fee: fee,
        );
        debugPrint('SpendBundle on flutter : ${jsonEncode(requiredCoins)}');
        _channel.invokeMethod('SpeedyTransfer', {
          "spendBundle": spendBundle.toJson(),
          "dest_puzzle_hash": destPuzzleHash,
          "spentCoins": jsonEncode(requiredCoins)
        });
      } else {
        debugPrint('responseData on coin records is not ok ');
        throw Exception("Response on coin records is not ok");
      }
    } catch (e) {
      debugPrint("Caught exception in dart code $e");
      _channel.invokeMethod("exception", e.toString());
    }
  }

  Future<void> analyzeOffer({required String offerStr}) async {
    final offer = Offer.fromBench32(offerStr);
    final tradeManager = TradeManagerService();
    final analized = await tradeManager.analizeOffer(
        fee: 0,
        targetPuzzleHash: Puzzlehash.zeros(),
        changePuzzlehash: Puzzlehash.zeros(),
        offer: offer);
    if (analized != null) {
      print("Analized Requested : ${analized.requested}");
      print("Analized Offered : ${analized.offered}");
    } else {
      print("Analized is null");
    }
  }

  Future<void> pushingOfferWithXCH(
      {required String offer,
      required List<String> mnemonics,
      required String url,
      required int observer,
      required int nonObserver,
      required int fee}) async {
    try {
      var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";
      var keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonics, observer, nonObserver);
      NetworkContext()
          .setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);
      final standartWalletService = StandardWalletService();
      final puzzleHashes =
          keychain.hardenedMap.entries.map((e) => e.key).toList();
      for (var element in keychain.unhardenedMap.entries) {
        puzzleHashes.add(element.key);
      }

      var fullNodeRpc = FullNodeHttpRpc(url);
      var fullNode = ChiaFullNodeInterface(fullNodeRpc);
      final offerService =
          OffersService(fullNode: fullNode, keychain: keychain);

      //search for xch full coins
      var curXCHAmount = 0;
      List<Coin> totalXCHCoins =
          await fullNode.getCoinsByPuzzleHashes(puzzleHashes);
      List<Coin> neededXCHCoins = [];
      for (var coin in totalXCHCoins) {
        // var isCoinSpent =
        // spentCoinsParents.contains(coin.parentCoinInfo.toString());
        if (true) {
          curXCHAmount += coin.amount;
          neededXCHCoins.add(coin);
          // if (curXCHAmount >= fee) {
          //   break;
          // }
        }
      }
      final xchFullCoins =
          standartWalletService.convertXchCoinsToFull(neededXCHCoins);
      debugPrint("Full XCH coins : $xchFullCoins");

      final changePh = keychain.puzzlehashes[0];
      final targePh = keychain.puzzlehashes[1];

      final offerFrom32 = Offer.fromBench32(offer);
      final responseResult = await offerService.responseOffer(
          fee: 0,
          targetPuzzleHash: targePh,
          offer: offerFrom32,
          changePuzzlehash: changePh,
          coinsToUse: xchFullCoins);

      debugPrint(
          "Result from pushing xch offer to cat : ${responseResult.item1.success}");
    } catch (ex) {
      _channel.invokeMethod("ErrorPushingOffer", ex);
    }
  }

  Future<void> takingAnOffer(
      {required String offerString,
      required List<String> mnemonics,
      required String url,
      required int observer,
      required int nonObserver,
      required String spentCoins,
      required int fee,
      required String requestedNFT}) async {
    try {
      debugPrint(
          "Fee : $fee Mnemonics : $mnemonics on pushingOfferWithCAT, url : $url observer : $observer nonObserver : $nonObserver fee: $fee");
      var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";
      var keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonics, observer, nonObserver);
      NetworkContext()
          .setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);
      final standartWalletService = StandardWalletService();
      final puzzleHashes =
          keychain.hardenedMap.entries.map((e) => e.key).toList();
      for (var element in keychain.unhardenedMap.entries) {
        puzzleHashes.add(element.key);
      }

      var fullNodeRpc = FullNodeHttpRpc(url);
      var fullNode = ChiaFullNodeInterface(fullNodeRpc);

      final offer = Offer.fromBench32(offerString);
      final tradeManager = TradeManagerService();
      final analized = await tradeManager.analizeOffer(
          fee: 0,
          targetPuzzleHash: Puzzlehash.zeros(),
          changePuzzlehash: Puzzlehash.zeros(),
          offer: offer);

      List<String> spentCoinsParents = [];
      if (spentCoins.isNotEmpty) {
        List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoins);
        for (var item in spentCoinsJsonDecoded) {
          var parentCoinInfo = item["parent_coin_info"].toString();
          spentCoinsParents.add(parentCoinInfo);
        }
      }

      List<Future<void>> futures = [];
      List<FullCoin> allFullCoins = [];

      Map<String, List<Coin>> spentCoinsMap = {};
      List<FlutterToken> reqNFTList = parseFlutterTokenJsonString(requestedNFT);

      final nftService =
          NftNodeWalletService(fullNode: fullNode, keychain: keychain);

      for (var nft in reqNFTList) {
        var nftCoins = await nftService.getNFTCoinByParentCoinHash(
            assetId: Bytes.fromHex(nft.assetID),
            puzzle_hash: Puzzlehash.fromHex(nft.fromAddress));
        final nftCoin = nftCoins[0];
        final nftFullCoin = await nftService.convertFullCoin(nftCoin);
        allFullCoins.add(nftFullCoin);
      }

      debugPrint("NFT Coins if requested : $allFullCoins");
      var amountReqXCH = 0;

      for (var entry in analized!.requested.entries) {
        OfferAssetData? key = entry.key;
        final amountReq = entry.value[0].abs();
        debugPrint(
            "Amount CAT for pushing transaction : $amountReq, Key : $key");

        if (key != null &&
            key.assetId != null &&
            (key.type == SpendType.cat2 || key.type == SpendType.cat1)) {
          keychain.addOuterPuzzleHashesForAssetId(
              Puzzlehash.fromHex(key.assetId.toString()));
          futures.add(getFullCoinsByAssetId(
              url: url,
              fullNode: fullNode,
              assetId: key.assetId.toString(),
              fullCoins: allFullCoins,
              innerHashes: puzzleHashes,
              spentCoinsParents: spentCoinsParents,
              spentCoinsMap: spentCoinsMap,
              reqAmount: amountReq));
        } else if (key?.type != SpendType.nft) {
          amountReqXCH = amountReq;
        }
      }

      futures.add(getFullXCHCoinsByAssetId(
          url: url,
          fullNode: fullNode,
          assetId: "",
          fullCoins: allFullCoins,
          innerHashes: puzzleHashes,
          spentCoinsParents: spentCoinsParents,
          spentCoinsMap: spentCoinsMap,
          xchAmount: amountReqXCH + fee,
          standartWalletService: standartWalletService));

      await Future.wait(futures);

      final changePh = keychain.puzzlehashes[0];
      final targePh = keychain.puzzlehashes[1];

      final offerService =
          OffersService(fullNode: fullNode, keychain: keychain);

      final offerFrom32 = Offer.fromBench32(offerString);

      final responseResult = await offerService.responseOffer(
          fee: fee,
          targetPuzzleHash: targePh,
          offer: offerFrom32,
          changePuzzlehash: changePh,
          coinsToUse: allFullCoins);

      debugPrint(
          "Json Encode after responseOffer : ${jsonEncode(spentCoinsMap)}");

      debugPrint(
          "Result from pushing xch offer to cat : ${responseResult.item1.success}");

      debugPrint(
          "Pushing offer with args got called : ${jsonEncode(spentCoinsMap)}");

      _channel.invokeMethod(
          "PushingOffer", {"spentCoins": jsonEncode(spentCoinsMap)});
    } catch (ex) {
      debugPrint("ErrorPushingOffer with exception : $ex");
      _channel.invokeMethod("ErrorPushingOffer", ex.toString());
    }
  }

  Future<void> getFullXCHCoinsByAssetId(
      {required String url,
      required ChiaFullNodeInterface fullNode,
      required String assetId,
      required StandardWalletService standartWalletService,
      required List<FullCoin> fullCoins,
      required List<Puzzlehash> innerHashes,
      required List<String> spentCoinsParents,
      required Map<String, List<Coin>> spentCoinsMap,
      required int xchAmount}) async {
    List<Coin> totalXCHCoins =
        await fullNode.getCoinsByPuzzleHashes(innerHashes);
    var curAmount = 0;
    List<Coin> neededCoins = [];

    for (final coin in totalXCHCoins) {
      var isSpentCoin =
          spentCoinsParents.contains(coin.parentCoinInfo.toString());
      if (!isSpentCoin && coin.amount != 0) {
        curAmount += coin.amount;
        neededCoins.add(coin);
      }
      if (curAmount >= xchAmount) {
        break;
      }
    }

    fullCoins.addAll(standartWalletService.convertXchCoinsToFull(neededCoins));
    debugPrint("Needed Coins for xch : $neededCoins");
    spentCoinsMap["null"] = neededCoins;
  }

  Future<void> getFullCoinsByAssetId(
      {required String url,
      required ChiaFullNodeInterface fullNode,
      required String assetId,
      required List<FullCoin> fullCoins,
      required List<Puzzlehash> innerHashes,
      required List<String> spentCoinsParents,
      required Map<String, List<Coin>> spentCoinsMap,
      required int reqAmount}) async {
    List<Puzzlehash> outer = [];
    var catHash = Puzzlehash.fromHex(assetId);
    for (var hash in innerHashes) {
      var cur = WalletKeychain.makeOuterPuzzleHash(
        hash,
        catHash,
      );
      outer.add(cur);
    }

    final responseDataCAT = await fullNode.getCoinsByPuzzleHashes(
      outer,
    );

    List<CatCoin> catCoins = [];
    List<Coin> basicCatCoins = responseDataCAT;

    var curAmount = 0;

    List<Coin> neededCatCoins = [];

    for (final coin in basicCatCoins) {
      var isCoinSpent =
          spentCoinsParents.contains(coin.parentCoinInfo.toString());
      if (!isCoinSpent && coin.amount != 0) {
        curAmount += coin.amount;
        neededCatCoins.add(coin);
        await getCatCoinsDetail(
          coin: coin,
          httpUrl: url,
          catCoins: catCoins,
          fullNode: fullNode,
        );
        if (curAmount >= reqAmount) {
          break;
        }
      }
    }

    spentCoinsMap[assetId] = neededCatCoins;

    catCoins.map((e) {
      //Search for the Coin
      final coinFounded = basicCatCoins
          .where(
            (coin_) => coin_.id == e.id,
          )
          .toList();

      final coin = coinFounded.first;

      fullCoins.add(FullCoin.fromCoin(
        coin,
        e.parentCoinSpend,
      ));
    }).toList();
  }

  Future<void> pushingOffer({
    required String offerStr,
    required List<String> mnemonics,
    required String url,
    required String assetID,
    required int observer,
    required int nonObserver,
    required int fee,
  }) async {
    var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";
    var keychain = cachedWalletChains[key] ??
        generateKeyChain(mnemonics, observer, nonObserver);
    NetworkContext().setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);
    final standartWalletService = StandardWalletService();
    final puzzleHashes =
        keychain.hardenedMap.entries.map((e) => e.key).toList();
    for (var element in keychain.unhardenedMap.entries) {
      puzzleHashes.add(element.key);
    }

    if (assetID.isNotEmpty) {
      keychain = keychain
        ..addOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(assetID));
    }

    var fullNodeRpc = FullNodeHttpRpc(url);
    var fullNode = ChiaFullNodeInterface(fullNodeRpc);
    final offerService = OffersService(fullNode: fullNode, keychain: keychain);

    final assetId = Puzzlehash.fromHex(assetID);

    var myOuterPuzzlehashes = keychain.getOuterPuzzleHashesForAssetId(assetId);

    for (var element in keychain.hardenedMap.keys) {
      var outer = WalletKeychain.makeOuterPuzzleHash(
        element,
        assetId,
      );
      myOuterPuzzlehashes.add(outer);
    }

    // remove duplicate puzzlehashes
    myOuterPuzzlehashes = myOuterPuzzlehashes.toSet().toList();
  }

  Future<void> analyzeTakingOffer({required String offerStr}) async {
    final offer = Offer.fromBench32(offerStr);
    final tradeManager = TradeManagerService();
    final analized = await tradeManager.analizeOffer(
        fee: 0,
        targetPuzzleHash: Puzzlehash.zeros(),
        changePuzzlehash: Puzzlehash.zeros(),
        offer: offer);
    if (analized != null) {
      debugPrint("Analyzed Requested : ${analized.requested}");
      debugPrint("Analyzed Offered : ${analized.offered}");
      List<TokenInfo> requested = [];
      for (var entry in analized.requested.entries) {
        OfferAssetData? key = entry.key;
        List<int> values = entry.value;
        var spentType = key?.type;
        requested.add(TokenInfo(
            assetID: key?.assetId.toString(),
            amount: values[0],
            type: spentType?.value));
      }

      List<TokenInfo> offered = [];
      for (var entry in analized.offered.entries) {
        OfferAssetData? key = entry.key;
        int value = entry.value;
        var spentType = key?.type;
        offered.add(TokenInfo(
            assetID: key?.assetId.toString(),
            amount: value,
            type: spentType?.value));
      }

      debugPrint("Requested asset list : $requested");

      _channel.invokeMethod("AnalyzeOffer",
          {"requested": jsonEncode(requested), "offered": jsonEncode(offered)});
    } else {
      print("Analyzed is null");
      _channel.invokeMethod("exception", "analyzeOffer is null");
    }
  }

  Future<void> offerRemoveLiquidity(
      {required List<String> mnemonics,
      required String url,
      required String tokenAssetID,
      required int xchAmount,
      required int catAmount,
      required int observer,
      required int nonObserver,
      required int fee,
      required int liquidityAmount,
      required String tibetAssetID,
      required String spentCoinsJson}) async {
    var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";
    var keychain = cachedWalletChains[key] ??
        generateKeyChain(mnemonics, observer, nonObserver);
    var tibetHash = Puzzlehash.fromHex(tibetAssetID);
    NetworkContext().setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);
    final standartWalletService = StandardWalletService();
    final puzzleHashes =
        keychain.hardenedMap.entries.map((e) => e.key).toList();
    for (var element in keychain.unhardenedMap.entries) {
      puzzleHashes.add(element.key);
    }
    final keyChainCAT = keychain
      ..addOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(tibetAssetID));
    var fullNodeRpc = FullNodeHttpRpc(url);
    var fullNode = ChiaFullNodeInterface(fullNodeRpc);
    final offerService =
        OffersService(fullNode: fullNode, keychain: keyChainCAT);

    var myOuterPuzzlehashes =
        keychain.getOuterPuzzleHashesForAssetId(tibetHash);
    for (var element in keychain.hardenedMap.keys) {
      var outer = WalletKeychain.makeOuterPuzzleHash(
        element,
        tibetHash,
      );
      myOuterPuzzlehashes.add(outer);
    }
    myOuterPuzzlehashes = myOuterPuzzlehashes.toSet().toList();

    /// Search for the cat coins for offered
    final responseDataCAT = await fullNode.getCoinsByPuzzleHashes(
      myOuterPuzzlehashes,
    );
    List<String> spentCoinsParents = [];
    if (spentCoinsJson.isNotEmpty) {
      List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoinsJson);
      for (var item in spentCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        spentCoinsParents.add(parentCoinInfo);
      }
    }
    List<CatCoin> catCoins = [];
    List<Coin> basicCatCoins = responseDataCAT;
    // hydrate cat coins
    var curLiquidityAmount = 0;
    var neededCatCoins = [];
    for (final coin in basicCatCoins) {
      var isCoinSpent =
          spentCoinsParents.contains(coin.parentCoinInfo.toString());
      if (!isCoinSpent) {
        curLiquidityAmount += coin.amount;
        neededCatCoins.add(coin);
        await getCatCoinsDetail(
          coin: coin,
          httpUrl: url,
          catCoins: catCoins,
          fullNode: fullNode,
        );
        if (curLiquidityAmount >= liquidityAmount) {
          break;
        }
      }
    }
    // hydrate full coins
    List<FullCoin>? fullCatCoins = catCoins.map((e) {
      //Search for the Coin
      final coinFounded = basicCatCoins
          .where(
            (coin_) => coin_.id == e.id,
          )
          .toList();

      final coin = coinFounded.first;

      return FullCoin.fromCoin(
        coin,
        e.parentCoinSpend,
      );
    }).toList();

    //search for xch full coins
    var curXCHAmount = 0;
    List<Coin> totalXCHCoins =
        await fullNode.getCoinsByPuzzleHashes(puzzleHashes);
    List<Coin> neededXCHCoins = [];
    if (fee > 0) {
      for (var coin in totalXCHCoins) {
        var isCoinSpent =
            spentCoinsParents.contains(coin.parentCoinInfo.toString());
        if (!isCoinSpent) {
          curXCHAmount += coin.amount;
          neededXCHCoins.add(coin);
          if (curXCHAmount >= fee) {
            break;
          }
        }
      }
    }
    final xchFullCoins =
        standartWalletService.convertXchCoinsToFull(neededXCHCoins);
    debugPrint("Need Cat coins : $neededCatCoins  XCH coins : $neededXCHCoins");
    final allCoins =
        fullCatCoins.toSet().toList() + xchFullCoins.toSet().toList();
    final changePh = keychain.puzzlehashes[0];
    final targePh = keychain.puzzlehashes[1];
    final tokenHash = Puzzlehash.fromHex(tokenAssetID);

    final offer = await offerService.createOffer(
      offerredAmounts: {
        OfferAssetData.cat(
          tailHash: tibetHash,
        ): -liquidityAmount
      },
      requesteAmounts: {
        // Remember, always use the requested amount is positive
        OfferAssetData.cat(
          tailHash: tokenHash,
        ): [catAmount],
        null: [xchAmount]
      },
      coins: allCoins,
      changePuzzlehash: changePh,
      targetPuzzleHash: targePh,
      fee: fee,
    );
    final str = offer.toBench32();
    debugPrint("Offer generate offer add liquidity : $str");
    _channel.invokeMethod("CATToRemoveLiquidity", {
      "offer": str,
      "XCHCoins": jsonEncode(neededXCHCoins),
      "liquidityCoins": jsonEncode(neededCatCoins)
    });
  }

  Future<void> offerAddLiquidity(
      {required List<String> mnemonics,
      required String url,
      required String tokenAssetID,
      required int xchAmount,
      required int catAmount,
      required int observer,
      required int nonObserver,
      required int fee,
      required int liquidityAmount,
      required String tibetAssetID,
      required String spentCoinsJson}) async {
    var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";
    var keychain = cachedWalletChains[key] ??
        generateKeyChain(mnemonics, observer, nonObserver);
    var catHash = Puzzlehash.fromHex(tokenAssetID);
    NetworkContext().setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);
    final standartWalletService = StandardWalletService();
    final puzzleHashes =
        keychain.hardenedMap.entries.map((e) => e.key).toList();
    for (var element in keychain.unhardenedMap.entries) {
      puzzleHashes.add(element.key);
    }
    final keyChainCAT = keychain
      ..addOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(tokenAssetID));
    var fullNodeRpc = FullNodeHttpRpc(url);
    var fullNode = ChiaFullNodeInterface(fullNodeRpc);
    final offerService =
        OffersService(fullNode: fullNode, keychain: keyChainCAT);

    var myOuterPuzzlehashes = keychain.getOuterPuzzleHashesForAssetId(catHash);
    for (var element in keychain.hardenedMap.keys) {
      var outer = WalletKeychain.makeOuterPuzzleHash(
        element,
        catHash,
      );
      myOuterPuzzlehashes.add(outer);
    }
    myOuterPuzzlehashes = myOuterPuzzlehashes.toSet().toList();

    /// Search for the cat coins for offered
    final responseDataCAT = await fullNode.getCoinsByPuzzleHashes(
      myOuterPuzzlehashes,
    );
    List<String> spentCoinsParents = [];
    if (spentCoinsJson.isNotEmpty) {
      List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoinsJson);
      for (var item in spentCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        spentCoinsParents.add(parentCoinInfo);
      }
    }
    List<CatCoin> catCoins = [];
    List<Coin> basicCatCoins = responseDataCAT;
    // hydrate cat coins
    var curCATAmount = 0;
    var neededCatCoins = [];
    for (final coin in basicCatCoins.toSet()) {
      var isCoinSpent =
          spentCoinsParents.contains(coin.parentCoinInfo.toString());
      if (!isCoinSpent) {
        curCATAmount += coin.amount;
        neededCatCoins.add(coin);
        await getCatCoinsDetail(
          coin: coin,
          httpUrl: url,
          catCoins: catCoins,
          fullNode: fullNode,
        );
        if (curCATAmount >= catAmount) {
          break;
        }
      }
    }
    // hydrate full coins
    List<FullCoin>? fullCatCoins = catCoins.map((e) {
      //Search for the Coin
      final coinFounded = basicCatCoins
          .where(
            (coin_) => coin_.id == e.id,
          )
          .toList();

      final coin = coinFounded.first;

      return FullCoin.fromCoin(
        coin,
        e.parentCoinSpend,
      );
    }).toList();

    //search for xch full coins
    var curXCHAmount = 0;
    List<Coin> totalXCHCoins =
        await fullNode.getCoinsByPuzzleHashes(puzzleHashes);
    List<Coin> neededXCHCoins = [];

    for (var coin in totalXCHCoins) {
      var isCoinSpent =
          spentCoinsParents.contains(coin.parentCoinInfo.toString());
      if (!isCoinSpent) {
        curXCHAmount += coin.amount;
        neededXCHCoins.add(coin);
        if (curXCHAmount >= xchAmount + fee) {
          break;
        }
      }
    }

    final xchFullCoins =
        standartWalletService.convertXchCoinsToFull(neededXCHCoins);
    debugPrint("Need Cat coins : $neededCatCoins  XCH coins : $neededXCHCoins");
    final allCoins =
        fullCatCoins.toSet().toList() + xchFullCoins.toSet().toList();
    final changePh = keychain.puzzlehashes[0];
    final targePh = keychain.puzzlehashes[1];
    final tibetTokenHash = Puzzlehash.fromHex(tibetAssetID);

    final offer = await offerService.createOffer(
      offerredAmounts: {
        null: -xchAmount,
        OfferAssetData.cat(
          tailHash: catHash,
        ): -catAmount
      },
      requesteAmounts: {
        // Remember, always use the requested amount is positive
        OfferAssetData.cat(
          tailHash: tibetTokenHash,
        ): [liquidityAmount]
      },
      coins: allCoins,
      changePuzzlehash: changePh,
      targetPuzzleHash: targePh,
      fee: fee,
    );
    final str = offer.toBench32();
    debugPrint("Offer generate offer add liquidity : $str");
    _channel.invokeMethod("CATToAddLiquidity", {
      "offer": str,
      "XCHCoins": jsonEncode(neededXCHCoins),
      "CATCoins": jsonEncode(neededCatCoins)
    });
  }

  Future<void> tibetSwapXCHToCAT(
      {required List<String> mnemonics,
      required String url,
      required String assetId,
      required int xchAmount,
      required int catAmount,
      required int observer,
      required int nonObserver,
      required int fee,
      required String spentXCHCoinsJson}) async {
    try {
      NetworkContext()
          .setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);

      final fullNodeRpc = FullNodeHttpRpc(url);

      var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";
      var keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonics, observer, nonObserver);

      final fullNode = ChiaFullNodeInterface(fullNodeRpc);
      final offerService =
          OffersService(fullNode: fullNode, keychain: keychain);

      final standartWalletService = StandardWalletService();

      final puzzleHashes =
          keychain.hardenedMap.entries.map((e) => e.key).toList();
      keychain.unhardenedMap.entries.forEach((element) {
        puzzleHashes.add(element.key);
      });

      List<String> spentCoinsParents = [];
      if (spentXCHCoinsJson.isNotEmpty) {
        List<dynamic> spentCoinsJsonDecoded = json.decode(spentXCHCoinsJson);
        for (var item in spentCoinsJsonDecoded) {
          var parentCoinInfo = item["parent_coin_info"].toString();
          spentCoinsParents.add(parentCoinInfo);
        }
      }

      var curAmount = 0;
      final totalAmount = xchAmount + fee;
      List<Coin> neededCoins = [];
      List<Coin> totalCoins =
          await fullNode.getCoinsByPuzzleHashes(puzzleHashes);
      for (var coin in totalCoins) {
        var isCoinSpent =
            spentCoinsParents.contains(coin.parentCoinInfo.toString());
        debugPrint(
            "Found spent coins for xch parent coin info $isCoinSpent : Parent Coin Info : ${coin.parentCoinInfo.toString()}");
        debugPrint("Searching in already spent coins : $spentCoinsParents");
        if (!isCoinSpent) {
          curAmount += coin.amount;
          neededCoins.add(coin);
          if (curAmount >= totalAmount) {
            break;
          }
        }
      }

      List<FullCoin>? xchFullCoins;

      xchFullCoins = standartWalletService.convertXchCoinsToFull(neededCoins);

      final changePh = keychain.puzzlehashes[0];
      final targePh = keychain.puzzlehashes[1];

      final assetHash = Bytes.fromHex(
        assetId,
      );

      final offer = await offerService.createOffer(
          requesteAmounts: {
            OfferAssetData.cat(
              tailHash: assetHash,
            ): [catAmount]
          },
          offerredAmounts: {
            null: -xchAmount
          },
          coins: xchFullCoins,
          changePuzzlehash: changePh,
          targetPuzzleHash: targePh,
          fee: fee);
      final str = offer.toBench32();
      debugPrint("Offering xch for cat : $str");

      _channel.invokeMethod(
          "XCHToCAT", {"offer": str, "spentXCHCoins": jsonEncode(neededCoins)});
    } catch (ex) {
      _channel.invokeMethod("exception");
    }
  }

  Future<void> tibetSwapCATToXCH(
      {required List<String> mnemonics,
      required String url,
      required String assetId,
      required int xchAmount,
      required int catAmount,
      required int observer,
      required int nonObserver,
      required int fee,
      required String spentCoinsJson}) async {
    try {
      var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";
      var keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonics, observer, nonObserver);
      var catHash = Puzzlehash.fromHex(assetId);

      NetworkContext()
          .setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);
      final standartWalletService = StandardWalletService();
      final puzzleHashes =
          keychain.hardenedMap.entries.map((e) => e.key).toList();
      for (var element in keychain.unhardenedMap.entries) {
        puzzleHashes.add(element.key);
      }

      final keyChainCAT = keychain
        ..addOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(assetId));

      var fullNodeRpc = FullNodeHttpRpc(url);
      var fullNode = ChiaFullNodeInterface(fullNodeRpc);
      final offerService =
          OffersService(fullNode: fullNode, keychain: keyChainCAT);

      var myOuterPuzzleHashes =
          keychain.getOuterPuzzleHashesForAssetId(catHash);

      for (var element in keychain.hardenedMap.keys) {
        var outer = WalletKeychain.makeOuterPuzzleHash(
          element,
          catHash,
        );
        myOuterPuzzleHashes.add(outer);
      }

      // remove duplicate puzzlehashes
      myOuterPuzzleHashes = myOuterPuzzleHashes.toSet().toList();

      /// Search for the cat coins for offered
      final responseDataCAT = await fullNode.getCoinsByPuzzleHashes(
        myOuterPuzzleHashes,
      );

      List<String> spentCoinsParents = [];
      if (spentCoinsJson.isNotEmpty) {
        List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoinsJson);
        for (var item in spentCoinsJsonDecoded) {
          var parentCoinInfo = item["parent_coin_info"].toString();
          spentCoinsParents.add(parentCoinInfo);
        }
      }

      List<CatCoin> catCoins = [];
      List<Coin> basicCatCoins = responseDataCAT;

      // hydrate cat coins
      var curCATAmount = 0;
      var neededCatCoins = [];
      for (final coin in basicCatCoins) {
        var isCoinSpent =
            spentCoinsParents.contains(coin.parentCoinInfo.toString());
        if (!isCoinSpent) {
          curCATAmount += coin.amount;
          neededCatCoins.add(coin);
          await getCatCoinsDetail(
            coin: coin,
            httpUrl: url,
            catCoins: catCoins,
            fullNode: fullNode,
          );
          if (curCATAmount >= catAmount) {
            break;
          }
        }
      }

      // hydrate full coins
      List<FullCoin>? fullCatCoins = catCoins.map((e) {
        //Search for the Coin
        final coinFounded = basicCatCoins
            .where(
              (coin_) => coin_.id == e.id,
            )
            .toList();

        final coin = coinFounded.first;

        return FullCoin.fromCoin(
          coin,
          e.parentCoinSpend,
        );
      }).toList();

      //search for xch full coins
      var curXCHAmount = 0;
      List<Coin> totalXCHCoins =
          await fullNode.getCoinsByPuzzleHashes(puzzleHashes);
      List<Coin> neededXCHCoins = [];
      if (fee > 0) {
        for (var coin in totalXCHCoins) {
          var isCoinSpent =
              spentCoinsParents.contains(coin.parentCoinInfo.toString());
          if (!isCoinSpent) {
            curXCHAmount += coin.amount;
            neededXCHCoins.add(coin);
            if (curXCHAmount >= xchAmount) {
              break;
            }
          }
        }
      }

      final xchFullCoins =
          standartWalletService.convertXchCoinsToFull(neededXCHCoins);

      // concatenate all coins, the OfferService will be grouped for asset
      final allCoins =
          fullCatCoins.toSet().toList() + xchFullCoins.toSet().toList();
      // debugPrint(" Cat Coins : $fullCatCoins  XCH Coins : $xchFullCoins ");

      final changePh = keychain.puzzlehashes[0];
      final targetPh = keychain.puzzlehashes[1];

      final offer = await offerService.createOffer(
        offerredAmounts: {
          //Remember, always use the offerred amount is negative
          OfferAssetData.cat(
            tailHash: catHash,
          ): -catAmount
        },
        requesteAmounts: {
          // Remember, always use the requested amount is positive
          null: [
            xchAmount,
          ]
        },
        coins: allCoins,
        changePuzzlehash: changePh,
        targetPuzzleHash: targetPh,
        fee: fee,
      );
      final str = offer.toBench32();
      debugPrint("Offer generate cat to xch : $str");
      _channel.invokeMethod("offerCATToXCH", {
        "offer": str,
        "XCHCoins": jsonEncode(neededXCHCoins),
        "CATCoins": jsonEncode(neededCatCoins)
      });
    } catch (ex) {
      _channel.invokeMethod("exception");
    }
  }

  Future<void> tibetSwapXCHToCat(
      {required List<String> mnemonics,
      required String url,
      required String assetId,
      required int xchAmount,
      required int catAmount,
      required int observer,
      required int nonObserver}) async {
    var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";
    var keyChain = cachedWalletChains[key] ??
        generateKeyChain(mnemonics, observer, nonObserver);
    var fullNodeRpc = FullNodeHttpRpc(url);
    var fullNode = ChiaFullNodeInterface(fullNodeRpc);
    final offerService = OffersService(fullNode: fullNode, keychain: keyChain);
    final standartWalletService = StandardWalletService();
    final puzzleHashes =
        keyChain.hardenedMap.entries.map((e) => e.key).toList();
    keyChain.unhardenedMap.entries.forEach((element) {
      puzzleHashes.add(element.key);
    });
    // final responseDataCAT = await fullNode.getCoinsByPuzzleHashes(puzzleHashes);
    // debugPrint(
    //     "My Response From retrieving just xch coins  : $responseDataCAT");
    List<FullCoin>? xchCoins;
    xchCoins = standartWalletService.convertXchCoinsToFull(
      await fullNode.getCoinsByPuzzleHashes(puzzleHashes),
    );
    final changePh = keyChain.puzzlehashes[0];
    final targetPh = keyChain.puzzlehashes[1];
    final assetIdHash = Bytes.fromHex(
      assetId,
    );
    final offer = await offerService.createOffer(
      requesteAmounts: {
        OfferAssetData.cat(
          tailHash: assetIdHash,
        ): [catAmount]
      },
      offerredAmounts: {null: xchAmount * (-1)},
      coins: xchCoins,
      changePuzzlehash: changePh,
      targetPuzzleHash: targetPh,
    );
    final str = offer.toBench32();
    debugPrint("Offering xch for cat : $str");
    _channel.invokeMethod("offer", {"offer": str});
  }

  Future<void> cachedWalletKeyChain(
      List<String> mnemonics, int observer, int nonObserver) async {
    try {
      var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";
      final maxNum = max(observer, nonObserver);
      final walletsSetList = <WalletSet>[];
      KeychainCoreSecret keychainSecret =
          KeychainCoreSecret.fromMnemonic(mnemonics);
      for (var i = 0; i < maxNum; i++) {
        final set1 =
            WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
        walletsSetList.add(set1);
      }
      final keychain = WalletKeychain.fromWalletSets(walletsSetList);
      cachedWalletChains[key] = keychain;
    } catch (ex) {
      debugPrint("Exception occurred in init wallet first time $ex");
    }
  }

  void changeSettingsWalletPuzzleHashes(
      {required List<String> mnemonics,
      required int observer,
      required int nonObserver,
      required List<String> assetIds}) {
    try {
      KeychainCoreSecret keychainSecret =
          KeychainCoreSecret.fromMnemonic(mnemonics);
      final maxNum = max(observer, nonObserver);
      final walletsSetList = <WalletSet>[];
      for (var i = 0; i < maxNum; i++) {
        final set1 =
            WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
        walletsSetList.add(set1);
      }
      final keychain = WalletKeychain.fromWalletSets(walletsSetList);
      var key = "${mnemonics.join(' ')}_${observer}_$nonObserver";
      cachedWalletChains[key] = keychain;
      final mainPuzzleHashes = keychain.puzzlehashes.sublist(0, observer);
      mainPuzzleHashes
          .addAll(keychain.walletPuzzlehashes.sublist(0, nonObserver));

      Map<String, List<String>> mapToAndroid = {};

      mapToAndroid["main_puzzle_hashes"] =
          mainPuzzleHashes.map((e) => e.toHex()).toList();

      assetIds.forEach((assetId) {
        List<String> outerHashes = [];
        mainPuzzleHashes.forEach((mainHash) {
          if (assetId.isNotEmpty) {
            outerHashes.add(WalletKeychain.makeOuterPuzzleHash(
                    Puzzlehash.fromHex(mainHash.toHex()),
                    Puzzlehash.fromHex(assetId))
                .toHex());
          }
        });
        mapToAndroid[assetId] = outerHashes;
      });

      _channel.invokeMethod("changeSettings", mapToAndroid);
    } catch (ex) {
      debugPrint(
          "Exception  caught in flutter in change settings : ${ex.toString()}");
    }
  }

  Future generateSpendBundleForToken(
      {required int fee,
      required int amount,
      required List<String> mnemonic,
      required String httpUrl,
      required String destAddress,
      required String networkType,
      required String asset_id,
      required String spentCoinsJson,
      required int observer,
      required int nonObserver}) async {
    try {
      print(
          "flutter arguments for token fee : $fee amount : $amount  mnemonic : $mnemonic url : $httpUrl dest : $destAddress isTypeNetwork : $networkType asset_id : $asset_id spentCoinJson : $spentCoinsJson observer : "
          "$observer non-observer : $nonObserver");
      var stopwatch = Stopwatch()..start();
      var key = "${mnemonic.join(" ")}_${observer}_$nonObserver";
      var keyChain = cachedWalletChains[key] ??
          generateKeyChain(mnemonic, observer, nonObserver);

      debugPrint(
          'Time taken: ${stopwatch.elapsedMilliseconds / 1000}s  to initialize keyChain');

      final keyChainCAT = keyChain
        ..addOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(asset_id));

      Network chosenNetwork;
      switch (networkType) {
        case "Chia":
          chosenNetwork = Network.mainnet;
          break;
        case "Chia TestNet":
          chosenNetwork = Network.testnet10;
          break;
        case "Chia TestNet":
        default:
          chosenNetwork = Network.mainnet;
      }

      NetworkContext().setBlockchainNetwork(blockchainNetworks[chosenNetwork]!);
      // get outer puzzle hashes from keychain
      final myOuterPuzzlehashes = keyChainCAT
          .getOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(asset_id));

      keyChainCAT.hardenedMap.keys.forEach((element) {
        var outer = WalletKeychain.makeOuterPuzzleHash(
            element, Puzzlehash.fromHex(asset_id));
        myOuterPuzzlehashes.add(outer);
      });

      CatWalletService catWalletService = CatWalletService();

      stopwatch = Stopwatch()..start();

      final fullNodeRpc = FullNodeHttpRpc(httpUrl);
      final fullNode = ChiaFullNodeInterface(fullNodeRpc);

      final responseDataCAT =
          await fullNode.getCoinsByPuzzleHashes(myOuterPuzzlehashes);

      debugPrint(
          "My First Response From retrieving cat: $responseDataCAT, to get unspent coins ${stopwatch.elapsedMilliseconds / 1000}s");

      List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoinsJson);
      List<String> spentCoinsParents = [];
      for (var item in spentCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        spentCoinsParents.add(parentCoinInfo);
      }
      List<Future<void>> futures = [];
      List<Coin> standardCoinsForFee = [];
      if (fee > 0) {
        futures.add(getStandardCoinsForFee(
            keyChain: keyChain,
            observer: observer,
            non_observer: nonObserver,
            httpUrl: httpUrl,
            spentCoinsParents: spentCoinsParents,
            standardCoinsForFee: standardCoinsForFee,
            fee: fee,
            fullNode: fullNode));
      }

      debugPrint("SpentCoinsPrototypes on sending : $spentCoinsParents");

      List<Coin> allCatCoins = responseDataCAT;

      allCatCoins.sort((a, b) {
        return b.amount.compareTo(a.amount);
      });

      // debugPrint("AllCoins sorted by decreasing order : $allCatCoins");

      List<Coin> filteredCoins = [];
      var sum = 0;
      for (final coin in allCatCoins) {
        var coinIsSpent =
            spentCoinsParents.contains(coin.parentCoinInfo.toString());
        if (coin.amount != 0 && !coinIsSpent) {
          filteredCoins.add(coin);
          sum += coin.amount;
          if (sum >= amount) {
            break;
          }
        }
      }

      List<CatCoin> catCoins = [];
      for (final coin in filteredCoins) {
        futures.add(getCatCoinsDetail(
            coin: coin,
            httpUrl: httpUrl,
            catCoins: catCoins,
            fullNode: fullNode));
      }
      debugPrint("Sending cat coins future size : ${futures.length}");
      await Future.wait(futures);
      debugPrint(
          "Sending cat coins : $catCoins,  Dest Hash : ${Address(destAddress).toPuzzlehash()}");
      final spendBundle = catWalletService.createSpendBundle(
          payments: [
            Payment(amount, Address(destAddress).toPuzzlehash(),
                memos: <Bytes>[Address(destAddress).toPuzzlehash().toBytes()])
          ],
          catCoinsInput: catCoins,
          keychain: keyChainCAT,
          changePuzzlehash: keyChainCAT.puzzlehashes[0],
          fee: fee,
          standardCoinsForFee: standardCoinsForFee);

      var destPuzzleHas = Address(destAddress).toPuzzlehash();
      var outerDestPuzzleHash = WalletKeychain.makeOuterPuzzleHash(
              destPuzzleHas, Puzzlehash.fromHex(asset_id))
          .toHex();

      _channel.invokeMethod('getSpendBundle', {
        "spendBundle": spendBundle.toJson(),
        "dest_puzzle_hash": destPuzzleHas.toHex(),
        "spentCoins": jsonEncode(standardCoinsForFee),
        "spentTokens": jsonEncode(filteredCoins)
      });
    } catch (e) {
      debugPrint("Caught exception in dart code for token" + e.toString());
      _channel.invokeMethod("exception", e.toString());
    }
  }

  static Future<void> getCatCoinsDetail(
      {required Coin coin,
      required String httpUrl,
      required List<CatCoin> catCoins,
      required ChiaFullNodeInterface fullNode}) async {
    final parentCoin = await fullNode.getCoinById(coin.parentCoinInfo);
    final parentCoinSpend = await fullNode.getCoinSpend(parentCoin!);
    catCoins.add(CatCoin(
      parentCoinSpend: parentCoinSpend!,
      coin: coin,
    ));
  }

  Future<void> getStandardCoinsForFee(
      {required WalletKeychain keyChain,
      required int observer,
      required int non_observer,
      required String httpUrl,
      required List<String> spentCoinsParents,
      required List<Coin> standardCoinsForFee,
      required int fee,
      required ChiaFullNodeInterface fullNode}) async {
    var mainPuzzleHashes =
        keyChain.hardenedMap.keys.toList().sublist(0, non_observer);
    mainPuzzleHashes
        .addAll(keyChain.unhardenedMap.keys.toList().sublist(0, observer));
    List<Coin> feeStandardCoinsTotal =
        await fullNode.getCoinsByPuzzleHashes(mainPuzzleHashes);
    var feeSum = 0;
    feeStandardCoinsTotal.sort((a, b) {
      return b.amount.compareTo(a.amount);
    });
    debugPrint(
        "Getting standard coins for fee : $standardCoinsForFee when sending token");
    for (var coin in feeStandardCoinsTotal) {
      var feeCoinSpent =
          spentCoinsParents.contains(coin.parentCoinInfo.toString());
// debugPrint(
//     "FeeCoinIsSpent for fee when sending token : $feeCoinSpent and Infos : $spentCoinsParents");
      if (coin.amount != 0 && !feeCoinSpent) {
        feeSum += coin.amount;
        standardCoinsForFee.add(coin);
        if (feeSum >= fee) {
          break;
        }
      }
    }
  }

  Future generateSpendBundleXCH(
      {required int fee,
      required int amount,
      required List<String> mnemonic,
      required String httpUrl,
      required String destAddress,
      required String networkType,
      required String spentCoinsJson,
      required int observer,
      required int nonObserver}) async {
    try {
      debugPrint(
          "fee : $fee amount : $amount  mnemonic : $mnemonic url : $httpUrl dest : $destAddress isTypeNetwork : $networkType  hashCounter : $observer");
      var key = "${mnemonic.join(" ")}_${observer}_$nonObserver";
      final keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonic, observer, nonObserver);
      Network chosenNetwork;
      switch (networkType) {
        case "Chia":
          chosenNetwork = Network.mainnet;
          break;
        case "Chia TestNet":
          chosenNetwork = Network.testnet10;
          break;
        case "Chives":
          chosenNetwork = Network.chivesnet;
          break;
        case "Chives TestNet":
          chosenNetwork = Network.chivestestnet;
          break;
        default:
          chosenNetwork = Network.mainnet;
      }
      NetworkContext().setBlockchainNetwork(blockchainNetworks[chosenNetwork]!);
      StandardWalletService standardWalletService = StandardWalletService();
      List<Puzzlehash> myPuzzlehashes =
          keychain.unhardenedMap.keys.toList().sublist(0, observer);

      myPuzzlehashes
          .addAll(keychain.hardenedMap.keys.toList().sublist(0, nonObserver));

      Map<String, dynamic> body = {
        "puzzle_hashes": myPuzzlehashes.map<String>((e) => e.toHex()).toList()
      };

      var destPuzzleHash = Address(destAddress).toPuzzlehash();

      final responseData =
          await post(Uri.parse("$httpUrl/get_coin_records_by_puzzle_hashes"),
              headers: <String, String>{
                'Content-Type': 'application/json; charset=UTF-8',
              },
              body: jsonEncode(body));

      List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoinsJson);
      List<String> spentCoinsParents = [];
      for (var item in spentCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        spentCoinsParents.add(parentCoinInfo);
      }

      debugPrint("SpentCoinsPrototypes on sending xch : $spentCoinsParents");

      if (responseData.statusCode == 200) {
        final List<Coin> allCoins = [
          for (final item in jsonDecode(responseData.body)['coin_records'])
            Coin.fromChiaCoinRecordJson(item)
        ];

        allCoins.sort((a, b) {
          return b.amount.compareTo(a.amount);
        });

// debugPrint("Sorted all coins : $allCoins");

        List<Coin> requiredCoins = [];
        var sum = 0;
        var total = amount + fee;
        for (final coin in allCoins) {
          var isCoinSpent =
              spentCoinsParents.contains(coin.parentCoinInfo.toString());
          debugPrint(
              "AlreadySpentCoins Contains on sending xch : $isCoinSpent  ${coin.parentCoinInfo} in $spentCoinsParents");
          if (!isCoinSpent) {
            sum += coin.amount;
            requiredCoins.add(coin);
            if (sum >= total) {
              break;
            }
          }
        }

        final spendBundle = standardWalletService.createSpendBundle(
          payments: [Payment(amount, destPuzzleHash)],
          coinsInput: requiredCoins,
          keychain: keychain,
          changePuzzlehash: keychain.puzzlehashes[0],
          fee: fee,
        );
        debugPrint('SpendBundle on flutter : ${jsonEncode(requiredCoins)}');
        _channel.invokeMethod('getSpendBundle', {
          "spendBundle": spendBundle.toJson(),
          "dest_puzzle_hash": destPuzzleHash.toHex(),
          "spentCoins": jsonEncode(requiredCoins)
        });
      } else {
        debugPrint('responseData on coin records is not ok ');
        throw Exception("Response on coin records is not ok");
      }
    } catch (e) {
      debugPrint("Caught exception in dart code" + e.toString());
      _channel.invokeMethod("exception", e.toString());
    }
  }

  void generateKeys(List<String> mnemonic, String prefix,
      List<String> defTokens, int observer, int nonObserver) {
    try {
      KeychainCoreSecret keychainSecret =
          KeychainCoreSecret.fromMnemonic(mnemonic);
      var maxHash = max(observer, nonObserver);
      final walletsSetList = <WalletSet>[];
      for (var i = 0; i < maxHash; i++) {
        final set1 =
            WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
        walletsSetList.add(set1);
      }
      final keychain = WalletKeychain.fromWalletSets(walletsSetList);
      var key = "${mnemonic.join(" ")}_${observer}_$nonObserver";
      cachedWalletChains[key] = keychain;
      final fingerPrint = keychainSecret.masterPublicKey.getFingerprint();
      final address = Address.fromPuzzlehash(
        keychain.puzzlehashes[0],
        prefix,
      );

      var mapToAndroid = {"address": "$address", "fingerPrint": fingerPrint};

      final mainPuzzleHashes = keychain.puzzlehashes.sublist(0, observer);
      mainPuzzleHashes
          .addAll(keychain.walletPuzzlehashes.sublist(0, nonObserver));

      mapToAndroid["main_puzzle_hashes"] =
          mainPuzzleHashes.map((e) => e.toHex()).toList();

      debugPrint("Map to Android : $mapToAndroid");

// def_tokens.forEach((asset_id) {
//   List<String> outer_hashes = [];
//   main_puzzle_hashes.forEach((main_hash) {
//     if (asset_id.isNotEmpty)
//       outer_hashes.add(WalletKeychain.makeOuterPuzzleHash(
//               Puzzlehash.fromHex(main_hash.toHex()),
//               Puzzlehash.fromHex(asset_id))
//           .toHex());
//   });
//   mapToAndroid[asset_id] = outer_hashes;
// });

      _channel.invokeMethod("getHash", mapToAndroid);
    } catch (ex) {
      debugPrint("Exception occurred in generating keys : $ex");
    }
  }

  void generateKeysImport(List<String> mnemonic, String prefix,
      List<String> defTokens, int observer, int nonObserver) {
    try {
      KeychainCoreSecret keychainSecret =
          KeychainCoreSecret.fromMnemonic(mnemonic);
      var maxHash = max(observer, nonObserver);
      final walletsSetList = <WalletSet>[];
      var stopwatch = Stopwatch()..start();
      for (var i = 0; i < maxHash; i++) {
        final set1 =
            WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
        walletsSetList.add(set1);
      }
      stopwatch.stop();
      debugPrint(
          'Time taken: ${stopwatch.elapsedMilliseconds / 1000}s to initialize walletSetList');
      final keychain = WalletKeychain.fromWalletSets(walletsSetList);
      var key = "${mnemonic.join(' ')}_${observer}_$nonObserver";
      cachedWalletChains[key] = keychain;
      final fingerPrint = keychainSecret.masterPublicKey.getFingerprint();
      final address = Address.fromPuzzlehash(
        keychain.puzzlehashes[0],
        prefix,
      );

      var mapToAndroid = {"address": "$address", "fingerPrint": fingerPrint};

      final mainPuzzleHashes = keychain.puzzlehashes.sublist(0, observer);
      mainPuzzleHashes
          .addAll(keychain.walletPuzzlehashes.sublist(0, nonObserver));

      mapToAndroid["main_puzzle_hashes"] =
          mainPuzzleHashes.map((e) => e.toHex()).toList();

      debugPrint("Map to Android : $mapToAndroid");
      stopwatch = Stopwatch()..start();
      defTokens.forEach((assetId) {
        List<String> outerHashes = [];
        mainPuzzleHashes.forEach((mainHash) {
          if (assetId.isNotEmpty)
            outerHashes.add(WalletKeychain.makeOuterPuzzleHash(
                    Puzzlehash.fromHex(mainHash.toHex()),
                    Puzzlehash.fromHex(assetId))
                .toHex());
        });
        mapToAndroid[assetId] = outerHashes;
      });
      stopwatch.stop();
      debugPrint(
          'Time taken: ${stopwatch.elapsedMilliseconds / 1000}s  to generate all cat hashes');

      _channel.invokeMethod("getHash", mapToAndroid);
    } catch (ex) {
      debugPrint("Exception occurred in generating keys : $ex");
    }
  }

  WalletKeychain generateKeyChain(
      List<String> mnemonic, int observer, int nonObserver) {
    KeychainCoreSecret keychainSecret =
        KeychainCoreSecret.fromMnemonic(mnemonic);
    var counter = max(observer, nonObserver);
    final walletsSetList = <WalletSet>[];
    for (var i = 0; i < counter; i++) {
      final set1 = WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
      walletsSetList.add(set1);
    }
    final keychain = WalletKeychain.fromWalletSets(walletsSetList);
    var key = "${mnemonic.join(" ")}_${observer}_$nonObserver";
    cachedWalletChains[key] = keychain;
    return keychain;
  }

  Future tempMethod(
      {required List<String> mnemonic, required String httpUrl}) async {
    try {
      final keychain = generateKeyChainForAssets(mnemonic, "", 1);
// initializing Service,
      Network chosenNetwork = Network.mainnet;
      NetworkContext().setBlockchainNetwork(blockchainNetworks[chosenNetwork]!);
// get outer puzzle hashes from keychain
      final myOuterPuzzlehashes = keychain.getOuterPuzzleHashesForAssetId(
          Puzzlehash.fromHex(
              '7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02'));

      var at = 0;
      for (final hash in myOuterPuzzlehashes) {
        debugPrint("At Index : $at and Puzzle_Hash for CAT : $hash");
        at++;
      }

      Map<String, dynamic> body = {
        "puzzle_hashes":
            myOuterPuzzlehashes.map<String>((e) => e.toHex()).toList()
      };
      debugPrint("Puzzle_Hashes for CAT coins : $myOuterPuzzlehashes");
      final responseData = await post(
          Uri.parse(
              "https://chia.blockchain-list.store/full-node/get_coin_records_by_puzzle_hashes"),
          headers: <String, String>{
            'Content-Type': 'application/json; charset=UTF-8',
          },
          body: jsonEncode(body));

//Here I have to get unspent GAD coins
      debugPrint("Response Data on Flutter ${responseData.body}");
      if (responseData.statusCode == 200) {
        debugPrint(
            "Got Result 200 OK from response on flutter side : ${responseData.body}");
      } else {
        debugPrint("StatusCode is not ok  : ${responseData.body}");
      }
    } catch (ex) {
      debugPrint("Caught exception in dart code" + ex.toString());
      _channel.invokeMethod("exception", ex.toString());
    }
  }

  WalletKeychain generateKeyChainForAssets(
      List<String> mnemonic, String assetId, int hashCounter) {
    KeychainCoreSecret keychainSecret =
        KeychainCoreSecret.fromMnemonic(mnemonic);

    final walletsSetList = <WalletSet>[];
    for (var i = 0; i < hashCounter; i++) {
      final set1 = WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
      walletsSetList.add(set1);
    }

    final keychain = WalletKeychain.fromWalletSets(walletsSetList)
      ..addOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(assetId));

    return keychain;
  }

  void testingMethod() async {
    var mnemonic = [
      "blast",
      "song",
      "refuse",
      "excess",
      "filter",
      "unhappy",
      "tag",
      "extra",
      "bless",
      "grain",
      "broom",
      "vanish"
    ];

    NetworkContext().setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);

    const fullNodeRpc = FullNodeHttpRpc("");

    KeychainCoreSecret keychainSecret =
        KeychainCoreSecret.fromMnemonic(mnemonic);
    final walletsSetList = <WalletSet>[];
    for (var i = 0; i < 5; i++) {
      final set1 = WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
      walletsSetList.add(set1);
    }
    final keychain = WalletKeychain.fromWalletSets(walletsSetList);

    const fullNode = ChiaFullNodeInterface(fullNodeRpc);
    final offerService = OffersService(fullNode: fullNode, keychain: keychain);

    final keyChainCAT = keychain
      ..addOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(
          '7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02'));

    final myOuterPuzzlehashes = keyChainCAT.getOuterPuzzleHashesForAssetId(
        Puzzlehash.fromHex(
            '7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02'));

    keyChainCAT.hardenedMap.keys.forEach((element) {
      var outer = WalletKeychain.makeOuterPuzzleHash(
          element,
          Puzzlehash.fromHex(
              '7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02'));
      myOuterPuzzlehashes.add(outer);
    });

    final responseDataCAT =
        await fullNode.getCoinsByPuzzleHashes(myOuterPuzzlehashes);
    debugPrint("My Response From retrieving cat  : $responseDataCAT");
    List<Future<void>> futures = [];
    List<CatCoin> catCoins = [];
    List<Coin> allCatCoins = responseDataCAT;
    for (final coin in allCatCoins) {
      futures.add(getCatCoinsDetail(
          coin: coin, httpUrl: "", catCoins: catCoins, fullNode: fullNode));
    }

    debugPrint("Offering cat coins future size : ${futures.length}");
    await Future.wait(futures);
    debugPrint("Offering cat coins : $catCoins");
    final fullCoins = catCoins
        .map((e) => FullCoin.fromCoin(
            Coin(
              confirmedBlockIndex: 0,
              spentBlockIndex: 0,
              coinbase: false,
              timestamp: 0,
              parentCoinInfo: e.parentCoinInfo,
              puzzlehash: e.puzzlehash,
              amount: e.amount,
            ),
            e.parentCoinSpend))
        .toList();

    final changePh = keychain.puzzlehashes[0];
    final targePh = keychain.puzzlehashes[1];

    final gwtHash = Bytes.fromHex(
      "7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02",
    );

    final offer = await offerService.createOffer(
      requesteAmounts: {
        null: [100000000000],
      },
      offerredAmounts: {
        OfferAssetData.cat(
          tailHash: gwtHash,
        ): -1000
      },

      coins: fullCoins,
      changePuzzlehash: changePh,
      targetPuzzleHash: targePh,
//fee: 1000000,
    );

    print("Offer from gad to xch");
    print(offer.toBench32());
  }

  static Future<void> offeringXCHForCat() async {
    var mnemonic = [
      "blast",
      "song",
      "refuse",
      "excess",
      "filter",
      "unhappy",
      "tag",
      "extra",
      "bless",
      "grain",
      "broom",
      "vanish"
    ];

    NetworkContext().setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);

    const fullNodeRpc = FullNodeHttpRpc("");

    KeychainCoreSecret keychainSecret =
        KeychainCoreSecret.fromMnemonic(mnemonic);
    final walletsSetList = <WalletSet>[];
    for (var i = 0; i < 5; i++) {
      final set1 = WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
      walletsSetList.add(set1);
    }
    final keychain = WalletKeychain.fromWalletSets(walletsSetList);

    const fullNode = ChiaFullNodeInterface(fullNodeRpc);
    final offerService = OffersService(fullNode: fullNode, keychain: keychain);

    final standartWalletService = StandardWalletService();

    final puzzleHashes =
        keychain.hardenedMap.entries.map((e) => e.key).toList();
    keychain.unhardenedMap.entries.forEach((element) {
      puzzleHashes.add(element.key);
    });

    final responseDataCAT = await fullNode.getCoinsByPuzzleHashes(puzzleHashes);

    debugPrint(
        "My Response From retrieving just xch coins  : $responseDataCAT");
    List<FullCoin>? xchCoins;

    xchCoins = standartWalletService.convertXchCoinsToFull(
      await fullNode.getCoinsByPuzzleHashes(puzzleHashes),
    );

    final changePh = keychain.puzzlehashes[0];
    final targePh = keychain.puzzlehashes[1];

    final gwtHash = Bytes.fromHex(
      "7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02",
    );

    final offer = await offerService.createOffer(
      requesteAmounts: {
        OfferAssetData.cat(
          tailHash: gwtHash,
        ): [1000]
      },
      offerredAmounts: {null: -100},
      coins: xchCoins,
      changePuzzlehash: changePh,
      targetPuzzleHash: targePh,
    );
    final str = offer.toBench32();
    debugPrint("Offering xch for gad : $str");
    _channel.invokeMethod("offer", {"offer": str});
  }

  static Future<void> offeringCatForXCH() async {
    var mnemonic = [
      "blast",
      "song",
      "refuse",
      "excess",
      "filter",
      "unhappy",
      "tag",
      "extra",
      "bless",
      "grain",
      "broom",
      "vanish"
    ];

// var mnemonic = [
//   "pact",
//   "talent",
//   "divide",
//   "syrup",
//   "kiss",
//   "second",
//   "tide",
//   "inform",
//   "device",
//   "question",
//   "season",
//   "brain"
// ];

    NetworkContext().setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);

    const fullNodeRpc = FullNodeHttpRpc("");

    KeychainCoreSecret keychainSecret =
        KeychainCoreSecret.fromMnemonic(mnemonic);
    final walletsSetList = <WalletSet>[];
    for (var i = 0; i < 5; i++) {
      final set1 = WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
      walletsSetList.add(set1);
    }
    final gwtHash = Puzzlehash.fromHex(
      "7108b478ac51f79b6ebf8ce40fa695e6eb6bef654a657d2694f1183deb78cc02",
    );
    final keychain = WalletKeychain.fromWalletSets(walletsSetList)
      ..addOuterPuzzleHashesForAssetId(gwtHash);

    const fullNode = ChiaFullNodeInterface(fullNodeRpc);
    final offerService = OffersService(fullNode: fullNode, keychain: keychain);

    final standartWalletService = StandardWalletService();

    final puzzleHashes =
        keychain.hardenedMap.entries.map((e) => e.key).toList();
    for (var element in keychain.unhardenedMap.entries) {
      puzzleHashes.add(element.key);
    }
    var myOuterPuzzlehashes = keychain.getOuterPuzzleHashesForAssetId(gwtHash);

    for (var element in keychain.hardenedMap.keys) {
      var outer = WalletKeychain.makeOuterPuzzleHash(
        element,
        gwtHash,
      );
      myOuterPuzzlehashes.add(outer);
    }

// remove duplicate puzzlehashes
    myOuterPuzzlehashes = myOuterPuzzlehashes.toSet().toList();

    /// Search for the cat coins for offered
    final responseDataCAT = await fullNode.getCoinsByPuzzleHashes(
      myOuterPuzzlehashes,
    );

    debugPrint(
        "My Response From retrieving just xch coins  : $responseDataCAT");

    List<CatCoin> catCoins = [];
    List<Coin> basicCatCoins = responseDataCAT;

// hydrate cat coins
    for (final coin in basicCatCoins) {
      await getCatCoinsDetail(
        coin: coin,
        httpUrl: "",
        catCoins: catCoins,
        fullNode: fullNode,
      );
    }

// hydrate full coins
    List<FullCoin>? fullCatCoins = catCoins.map((e) {
//Search for the Coin
      final coinFounded = basicCatCoins
          .where(
            (coin_) => coin_.id == e.id,
          )
          .toList();

      final coin = coinFounded.first;

      return FullCoin.fromCoin(
        coin,
        e.parentCoinSpend,
      );
    }).toList();

//search for xch full coins
    final xchFullCoins = standartWalletService.convertXchCoinsToFull(
      await fullNode.getCoinsByPuzzleHashes(puzzleHashes),
    );

// concatenate all coins, the OfferService will be grouped for asset
    final allCoins = fullCatCoins + xchFullCoins;

    final changePh = keychain.puzzlehashes[0];
    final targePh = keychain.puzzlehashes[1];

    final offer = await offerService.createOffer(
      offerredAmounts: {
//Remember, always use the offerred amount is negative
        OfferAssetData.cat(
          tailHash: gwtHash,
        ): -1000
      },
      requesteAmounts: {
// Remember, always use the requested amount is positive
        null: [
          44414023,
        ]
      },
      coins: allCoins,
      changePuzzlehash: changePh,
      targetPuzzleHash: targePh,
//fee: 1000000,
    );
    final str = offer.toBench32();
    debugPrint("Offer str cat for xch : $str");
    _channel.invokeMethod("offer", {"offer cat for xch : ": str});
  }

  void generateCATPuzzleHash(List<String> mainPuzzleHashes, String assetId) {
    final List<String> outerPuzzleHashes = [];
    mainPuzzleHashes.forEach((element) {
      if (element.isNotEmpty) {
        outerPuzzleHashes.add(WalletKeychain.makeOuterPuzzleHash(
                Puzzlehash.fromHex(element), Puzzlehash.fromHex(assetId))
            .toHex());
      }
    });
    _channel.invokeMethod('generate_outer_hash', {assetId: outerPuzzleHashes});
  }

  Future<void> asyncCATPuzzleHash(
      List<String> mainPuzzleHashes, String assetId) async {
    final List<String> outerPuzzleHashes = [];
    mainPuzzleHashes.forEach((element) {
      if (element.isNotEmpty) {
        outerPuzzleHashes.add(WalletKeychain.makeOuterPuzzleHash(
                Puzzlehash.fromHex(element), Puzzlehash.fromHex(assetId))
            .toHex());
      }
    });
    _channel.invokeMethod('$assetId', {assetId: outerPuzzleHashes});
  }

  void unCurryNFTCoin(
      {required String nftCoin, required String nftParentCoinJson}) {
    try {
      var coinMap = json.decode(nftCoin) as Map<String, dynamic>;
      var nftParentCoin = json.decode(nftParentCoinJson)["coin_solution"]
          as Map<String, dynamic>;
// debugPrint(
//     "NFtParentCoin after decoding and casting : $nftParentCoin $coinMap");
      final coin = Coin.fromChiaCoinRecordJson(coinMap);
// debugPrint("fromChiaCoinRecordJson : $coin");
      final parentSpendCoin = CoinSpend.fromJson(nftParentCoin);
      final puzzleReveal = parentSpendCoin.puzzleReveal;
      final nftUncurried = UncurriedNFT.uncurry(
        puzzleReveal,
      );
      final info = NFTInfo.fromUncurried(
        uncurriedNFT: nftUncurried,
        currentCoin: parentSpendCoin.coin,
        addressPrefix: "xch",
        genesisCoin: Coin(
          confirmedBlockIndex: coin.confirmedBlockIndex,
          spentBlockIndex: coin.spentBlockIndex,
          coinbase: coin.coinbase,
          timestamp: coin.timestamp,
          parentCoinInfo: coin.parentCoinInfo,
          puzzlehash: coin.puzzlehash,
          amount: (coin.amount * pow(10, 23)).toInt(),
        ), //Coin
      );
      debugPrint("Final UnCurried NFT : ${info.toMap()}");
      Map<String, dynamic> mapToAndroid = {};
      mapToAndroid["nft_hash"] = coin.parentCoinInfo.toHex();
      mapToAndroid["launcherId"] =
          NftAddress.fromPuzzlehash(info.launcherId).toString();
      mapToAndroid["nftCoinId"] = info.nftCoinId.toHex();
      mapToAndroid["didOwner"] = info.didOwner?.toHex();
      mapToAndroid["royaltyPercentage"] = info.royaltyPercentage.toString();
      mapToAndroid["royaltyAddress"] = info.royaltyAddress.toString();
      mapToAndroid["dataUris"] = info.dataUris.toList();
      mapToAndroid["dataHash"] = info.dataHash;
      mapToAndroid["metadataUris"] = info.metadataUris.toList();
      mapToAndroid["metadataHash"] = info.metadataHash;
      mapToAndroid["licenseUris"] = info.licenseUris.toList();
      mapToAndroid["licenseHash"] = info.licenseHash;
      mapToAndroid["seriesTotal"] = info.seriesTotal;
      mapToAndroid["seriesNumber"] = info.seriesNumber;
      mapToAndroid["chainInfo"] = info.chainInfo;
      mapToAndroid["mintHeight"] = info.mintHeight;
      mapToAndroid["supportsDid"] = info.supportsDid.toString();
      mapToAndroid["pendingTransaction"] = info.pendingTransaction.toString();
      mapToAndroid["p2Puzzlehash"] = info.p2Puzzlehash.toHex();
      mapToAndroid["launcherPuzzlehash"] = info.launcherPuzzlehash.toString();
      _channel.invokeMethod("unCurriedNFTInfo", mapToAndroid);
    } catch (ex) {
      debugPrint("Exception in unCurrying nft coin: ${ex.toString()}");
      _channel.invokeMethod("exceptionNFT", ex.toString());
    }
  }

  Future<void> generateNFTSpendBundle(
      {required String nftCoinJson,
      required List<String> mnemonics,
      required int observer,
      required int nonObserver,
      required String destAddress,
      required int fee,
      required String spentCoinsJson,
      required String base_url,
      required String fromAddress}) async {
    try {
      var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";

      final keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonics, observer, nonObserver);

      var coinMap = json.decode(nftCoinJson) as Map<String, dynamic>;

      NetworkContext()
          .setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);

      final coin = Coin.fromChiaCoinRecordJson(coinMap);
      debugPrint(
          "NFTCoin to Send after decoding and parsing : $coin and baseURL : $base_url");

      final fullNodeRpc = FullNodeHttpRpc(base_url);
      final fullNode = ChiaFullNodeInterface(fullNodeRpc);

      List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoinsJson);
      List<String> spentCoinsParents = [];
      for (var item in spentCoinsJsonDecoded) {
        var parentCoinInfo = item["parent_coin_info"].toString();
        spentCoinsParents.add(parentCoinInfo);
      }
      List<Future<void>> futures = [];
      List<Coin> standardCoinsForFee = [];
      if (fee > 0) {
        futures.add(getStandardCoinsForFee(
            keyChain: keychain,
            observer: observer,
            non_observer: nonObserver,
            httpUrl: base_url,
            spentCoinsParents: spentCoinsParents,
            standardCoinsForFee: standardCoinsForFee,
            fee: fee,
            fullNode: fullNode));
      }
      final nftService =
          NftNodeWalletService(fullNode: fullNode, keychain: keychain);
      debugPrint(
          "PuzzleHash to get nft coins on generate spend bundle : ${Puzzlehash.fromHex(fromAddress)}");
      var nftCoins = await nftService.getNFTCoinByParentCoinHash(
          assetId: coin.parentCoinInfo,
          puzzle_hash: Puzzlehash.fromHex(fromAddress));
      final nftCoin = nftCoins[0];
      debugPrint("Found NFTCoin to send  $nftCoin");
      final nftFullCoin = await nftService.convertFullCoin(nftCoin);
      debugPrint("Converting to FullNFTCoin : $nftFullCoin");
      debugPrint('Standard XCH for fee : $standardCoinsForFee');

      final destPuzzleHash = Address(destAddress).toPuzzlehash();
      debugPrint("Dest Puzzle Hash on generating nft bundle : $destPuzzleHash");

      await Future.wait(futures);
      var bundleNFT = NftWallet().createTransferSpendBundle(
        nftCoin: nftFullCoin.toNftCoinInfo(),
        keychain: keychain,
        targetPuzzleHash: destPuzzleHash,
        standardCoinsForFee: standardCoinsForFee,
        fee: fee,
        changePuzzlehash: keychain.puzzlehashes[0],
      );
      var bundleNFTJson = bundleNFT.toJson();

      _channel.invokeMethod('nftSpendBundle', {
        "spendBundle": bundleNFTJson,
        "spentCoins": jsonEncode(standardCoinsForFee)
      });
    } catch (ex) {
      debugPrint("Exception in sending nft token : $ex");
      _channel.invokeMethod("failedNFT", "Sorry, something went wrong : $ex");
    }
  }

  Future<void> getFullCoinsDetail(
      {required Coin coin,
      required String httpUrl,
      required List<FullCoin> fullCoins}) async {
    Map<String, dynamic> bodyParentCoinInfo = {
      "name": coin.parentCoinInfo.toString()
    };
    final bodyParentCoinInfoRes =
        await post(Uri.parse("$httpUrl/get_coin_record_by_name"),
            headers: <String, String>{
              'Content-Type': 'application/json; charset=UTF-8',
            },
            body: jsonEncode(bodyParentCoinInfo));
    var parentCoin = CoinRecordResponse.fromJson(
      jsonDecode(bodyParentCoinInfoRes.body) as Map<String, dynamic>,
    ).coinRecord!.toCoin();
    Map<String, dynamic> bodyParentCoinSpentBody = {
      'coin_id': parentCoin.id.toHex(),
      'height': parentCoin.spentBlockIndex,
    };
    final bodyParentCoinSpentRes =
        await post(Uri.parse("$httpUrl/get_puzzle_and_solution"),
            headers: <String, String>{
              'Content-Type': 'application/json; charset=UTF-8',
            },
            body: jsonEncode(bodyParentCoinSpentBody));
    var parentCoinSpend = CoinSpendResponse.fromJson(
      jsonDecode(bodyParentCoinSpentRes.body) as Map<String, dynamic>,
    ).coinSpend;
    fullCoins.add(FullCoin(
      parentCoinSpend: parentCoinSpend!,
      coin: coin,
    ));
  }

  Future<FullNFTCoinInfo> convertFullCoin(
      FullCoin coin, WalletKeychain keychain) async {
    final nftInfo =
        await NftWallet().getNFTFullCoinInfo(coin, buildKeychain: (phs) async {
      final founded = phs.where((element) =>
          keychain.getWalletVector(
            element,
          ) !=
          null);
      if (founded.length == phs.length) {
        return keychain;
      }
      return null;
    });
    FullNFTCoinInfo nftFullInfo = nftInfo.item1;
    if (nftInfo.item1.minterDid == null) {
      final did = await getMinterNft(nftInfo.item1.launcherId);
      if (did != null) {
        nftFullInfo = nftFullInfo.copyWith(minterDid: did.didId);
      }
      print("Minter DID is null");
    }
    return nftFullInfo;
  }

  Future<DidInfo?> getMinterNft(
    Bytes launcherId,
  ) async {
    final body = <String, dynamic>{
      'parent_ids': [launcherId].map((parentId) => parentId.toHex()).toList(),
    };
    body['include_spent_coins'] = true;

    final response = await post(Uri.parse(''),
        headers: <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: jsonEncode(body));

    final mainChildrens = CoinRecordsResponse.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    ).coinRecords.map((record) => record.toCoin()).toList();

// final mainHidratedCoins = await fullNode.hydrateFullCoins(mainChildrens);
//
// if (mainHidratedCoins.isEmpty) {
//   throw Exception("Can't be found the NFT coin with launcher ${launcherId}");
// }
// FullCoin nftCoin = mainHidratedCoins.first;
// print(nftCoin.type);
// final foundedCoins = await fullNode.getAllLinageSingletonCoin(nftCoin);
// final eveCcoin = foundedCoins.first;
// final uncurriedNft = UncurriedNFT.tryUncurry(eveCcoin.parentCoinSpend!.puzzleReveal);
// if (uncurriedNft!.supportDid) {
//   final minterDid = NftService()
//       .getnewOwnerDid(unft: uncurriedNft, solution: eveCcoin.parentCoinSpend!.solution);
//   if (minterDid != null) {
//     return DidService(fullNode: fullNode, keychain: keychain)
//         .getDidInfoByLauncherId(Puzzlehash(minterDid));
//   }
// }
    return null;
  }

  Future<void> unCurryNFTCoinSecond({
    required List<String> mnemonics,
    required int observer,
    required int nonObserver,
    required int startHeight,
    required String parentCoinInfo,
    required String puzzleHash,
    required String baseUrl,
  }) async {
    try {
      var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";

      final keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonics, observer, nonObserver);

      ChiaNetworkContextWrapper().registerNetworkContext(Network.mainnet);
      final fullNodeRpc = FullNodeHttpRpc(baseUrl);
      final fullNode = ChiaFullNodeInterface(fullNodeRpc);
      final nftService =
          NftNodeWalletService(fullNode: fullNode, keychain: keychain);
      var nftCoins = await nftService.getNFTCoinByParentCoinHash(
          assetId: Bytes.fromHex(parentCoinInfo),
          puzzle_hash: Puzzlehash.fromHex(puzzleHash),
          startHeight: startHeight);
      if (nftCoins.isEmpty) {
        debugPrint("NFtCoins to uncurry is empty");
      }
      final nftCoin = nftCoins[0];
      final nftFullCoin_ = await nftService.convertFullCoin(nftCoin);
      final nftInfo = nftFullCoin_.toNftCoinInfo();
      final info = UncurriedNFT.uncurry(nftInfo.fullPuzzle);
      final launcherId = info.singletonLauncherId.atom;
      final address = NftAddress.fromPuzzlehash(Puzzlehash(launcherId)).address;
      debugPrint("Address of nft found : $address");
      Map<String, dynamic> mapToAndroid = {};
      mapToAndroid["nft_hash"] = nftCoin.coin.parentCoinInfo.toString();
      mapToAndroid["launcherId"] = info.singletonLauncherId.toHex();
      mapToAndroid["nftCoinId"] = address;
      mapToAndroid["didOwner"] = info.ownerDid?.toHex();
      if (info.tradePricePercentage != null) {
        mapToAndroid["royaltyPercentage"] = info.tradePricePercentage! / 100;
      } else {
        mapToAndroid["royaltyPercentage"] = 0;
      }
      mapToAndroid["dataUrl"] = info.dataUris.toList()[0].toString();
      mapToAndroid["dataHash"] = info.dataHash.toHex();
      mapToAndroid["metadataUrl"] =
          info.metadata.toList()[2].cons[1].toString();
      mapToAndroid["metadataHash"] = info.metadataUpdaterHash.toHex();
// mapToAndroid["licenseUris"] = info.licenseUris.toList();
      mapToAndroid["supportsDid"] = info.supportDid;

// debugPrint("Data url photo : ${info.metadata.toList()[2].cons[1].toString()}");
// debugPrint("Data url photo : ${info.metadata.toList()}");

      _channel.invokeMethod("unCurriedNFTInfo", mapToAndroid);
    } catch (ex) {
      debugPrint("Exception in uncurry nft second way : $ex");
      _channel.invokeMethod("exceptionNFT", ex.toString());
    }
  }

  Future<void> testingNamesDao() async {
    await Future.delayed(Duration(seconds: 2));

    final namesdaoInterface = NamesdaoApi();
    const name = '___hahaha';
    final nameInfo = await namesdaoInterface.getNameInfo(name);
    debugPrint(
        "Name info Address on testingNamesDao: ${nameInfo?.address.address}");
    // expect(
    //   nameInfo?.address.address,
    //   equals('xch1l9hj8emh7xdk3y2d4kszeuu0z6gn27s9rlc0yz7uqgyjjtd0fegsvgsjtv'),
    // );
  }
}
