import 'dart:convert';

import 'package:chia_crypto_utils/chia_crypto_utils.dart' hide Client;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_splash_screen_module/SpeedyTransaction.dart';
import 'package:http/http.dart';
import 'dart:math';

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
            var def_tokens =
            call.arguments['tokens'].toString().split(' ').toList();
            var observer = call.arguments['observer'];
            var non_observer = call.arguments['non_observer'];
            print(
                'Generated Keys got called on flutter from android : with argument : ${call
                    .arguments}');
            try {
              generateKeys(
                  mnemonic, prefix, def_tokens, observer, non_observer);
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
            var def_tokens =
            call.arguments['tokens'].toString().split(' ').toList();
            var observer = call.arguments['observer'];
            var non_observer = call.arguments['non_observer'];
            print(
                'Generated Keys got called on flutter from android : with argument : ${call
                    .arguments}');
            try {
              generateKeysImport(
                  mnemonic, prefix, def_tokens, observer, non_observer);
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
                "GenerateWrappedCatPuzzle got called on flutter with arguments : ${call
                    .arguments}");
            generateCATPuzzleHash(
                args["puzzle_hashes"].split(' '), args["asset_id"].toString());
          }
          break;
        case "changeSettings":
          {
            var args = call.arguments;
            debugPrint(
                "Change settings wallet hashes with arguments ${call
                    .arguments}");
            changeSettingsWalletPuzzleHashes(
                mnemonics:
                args['mnemonicString'].toString().split(' ').toList(),
                observer: int.parse(args['observer'].toString()),
                non_observer: int.parse(args['non_observer'].toString()),
                asset_ids: args['asset_ids'].toString().split(' ').toList());
          }
          break;
        case "asyncCatPuzzle":
          {
            var args = call.arguments;
            print(
                "Async GenerateWrappedCatPuzzle got called on flutter with arguments : ${call
                    .arguments}");
            asyncCATPuzzleHash(
                args["puzzle_hashes"].split(' '), args["asset_id"].toString());
          }
          break;
        case "initWalletFirstTime":
          {
            var args = call.arguments;
            var mnemonics = args["mnemonics"].toString().split(' ');
            var observer = int.parse(args["observer"].toString());
            var non_observer = int.parse(args["non_observer"].toString());
            debugPrint("Caching Wallet KeyChain First Time With args : $args");
            cachedWalletKeyChain(mnemonics, observer, non_observer);
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
            var parent_coin_info = args["parent_coin_info"].toString();
            var puzzle_hash = args["puzzle_hash"].toString();
            var start_height = int.parse(args["start_height"].toString());
            var mnemonics = args["mnemonics"].toString().split(' ');
            var observer = int.parse(args["observer"].toString());
            var non_observer = int.parse(args["non_observer"].toString());
            var base_url = args["base_url"];
            unCurryNFTCoinSecond(
                mnemonics: mnemonics,
                observer: observer,
                non_observer: non_observer,
                start_height: start_height,
                parent_coin_info: parent_coin_info,
                puzzle_hash: puzzle_hash,
                base_url: base_url);
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
                  "Exception occurred in exchanging cat for xch : ${ex
                      .toString()}");
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
                  "Exception occurred in exchanging xch for cat : ${ex
                      .toString()}");
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
              var offer = args["offer"];
              var mnemonics = args["mnemonics"].toString().split(' ');
              var url = args["url"].toString();
              var observer = int.parse(args["observer"].toString());
              var nonObserver = int.parse(args["nonObserver"].toString());
              var fee = int.parse(args["fee"].toString());
              var spentCoins = args["spentCoins"];
              pushingOfferWithCAT(
                  offerString: offer,
                  mnemonics: mnemonics,
                  url: url,
                  observer: observer,
                  nonObserver: nonObserver,
                  spentCoins: spentCoins,
                  fee: fee);
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
      }
    });
    // offeringXCHForCat();
    // offeringCatForXCH();
    // testingMethod();
    analyzeOffer(
        offerStr:
        "offer1qqr83wcuu2rykcmqvpsxygqqa6wdw7l95dk68vl9mr2dkvl98r6g76an3qez5ppgyh00jfdqw4uy5w2vrk7f6wj6lw8adl4rkhlk3mflttacl4h7lur64as8zlhqhn8h04883476dz7x6mny6jzdcga33n4s8n884cxcekcemz6mk3jk4v925trha2kuwcv0k59xl377mx32g5h2c52wfycczdlns04vgl4cx77f6d8xs4f3m23zpghmeuyfzktsvljul5lf6fragjk9qdl5ewns7nam4t68j7e4se98ynkx67tjl3kfx0zse96wllnt0ahda2x20ymlh3pwqaeu7fue4anp5hn74af8dwvdt9aatrhf5pqzt2qgvnwecw8nkvmr8mqw9r83t77l05j0slhyegkjhfnvd024xtj0mxwjthmj3zgpnyqx33cqwjsnvcuzl5crrw7xrqakw6lww0c47699l82mgktl765h0rw823r3g908whmwmlhz8lkmp7lujr0jhnt4zljde56xa8uaqm9h9vxkqey94lalur70u7m0vd7pdhdnuhl24v8nknmlvy3t60ve083dajur3elel5lc49ulsv7kqnzy9smsndcr2hd07has6sjax77tmkm59e2vyce5g7revgd8a8jh6jg3zwaz9pujzh3gdlwkjjqc9xh8jw707rsde2utp3xzxks44axmempy2wmr2622mtjk0658jlkha7j3yhswmx0dxwnjg0h7zfujf64czed7mdz67lx60hsav0u2z783f2akdkcaduyavkdpjfynttzeqlul0kh68gltqcpfpfdkcgepjqhv0tdapgq8wtt37y4534ts");
  }

  Future<void> speedyTransferNFT({required String nftCoinParentInfo,
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
              "Contains checking for trans coins : $contains : ${coin
                  .parentCoinInfo.toString()}");
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
          "PuzzleHash to get nft coins on speedy up hash : ${Puzzlehash.fromHex(
              fromAddress)} Info : ${Bytes.fromHex(nftCoinParentInfo)}");
      var nftCoins = await nftService.getNFTCoinByParentCoinHash(
          parent_coin_info: Bytes.fromHex(nftCoinParentInfo),
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

  Future<void> speedyTransferCAT({required int fee,
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
      var stopwatch = Stopwatch()
        ..start();
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
          'Time taken: ${stopwatch.elapsedMilliseconds /
              1000}s  to initialize keyChain');

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

      stopwatch = Stopwatch()
        ..start();

      final responseDataCAT =
      await fullNode.getCoinsByPuzzleHashes(myOuterPuzzlehashes);

      debugPrint(
          "My Second Response From retrieving cat  : $responseDataCAT, to get unspent coins ${stopwatch
              .elapsedMilliseconds / 1000}s");

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

  Future<void> speedyTransferXCH({required int fee,
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
          if (isUsed) {
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

  Future<void> pushingOfferWithXCH({required String offer,
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
          "Result from pushing xch offer to cat : ${responseResult.item1
              .success}");
    } catch (ex) {
      _channel.invokeMethod("ErrorPushingOffer", ex);
    }
  }

  Future<void> pushingOfferWithCAT({required String offerString,
    required List<String> mnemonics,
    required String url,
    required int observer,
    required int nonObserver,
    required String spentCoins,
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

      for (var entry in analized!.requested.entries) {
        OfferAssetData? key = entry.key;
        final amountReq = entry.value[0];
        if (key?.assetId != null &&
            (key?.type == SpendType.cat2 || key?.type == SpendType.cat1)) {
          futures.add(getFullCoinsByAssetId(
              url: url,
              fullNode: fullNode,
              assetId: key!.assetId.toString(),
              fullCoins: allFullCoins,
              innerHashes: puzzleHashes,
              spentCoinsParents: spentCoinsParents,
              spentCoinsMap: spentCoinsMap,
              reqAmount: amountReq));
        } else {
          futures.add(getFullXCHCoinsByAssetId(
              url: url,
              fullNode: fullNode,
              assetId: key!.assetId.toString(),
              fullCoins: allFullCoins,
              innerHashes: puzzleHashes,
              spentCoinsParents: spentCoinsParents,
              spentCoinsMap: spentCoinsMap,
              xchAmount: amountReq + fee,
              standartWalletService: standartWalletService));
        }
      }

      debugPrint("Futures size ${futures.length}");
      await Future.wait(futures);

      final changePh = keychain.puzzlehashes[0];
      final targePh = keychain.puzzlehashes[1];

      debugPrint("Json Encode before offer32 : ${jsonEncode(spentCoinsMap)}");
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
          "Result from pushing xch offer to cat : ${responseResult.item1
              .success}");

      debugPrint(
          "Pushing offer with args got called : ${jsonEncode(spentCoinsMap)}");

      _channel.invokeMethod(
          "PushingOffer", {"spentCoins": jsonEncode(spentCoinsMap)});
    } catch (ex) {
      _channel.invokeMethod("ErrorPushingOffer", ex);
    }
  }

  Future<void> getFullXCHCoinsByAssetId({required String url,
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
      if (!isSpentCoin) {
        curAmount += coin.amount;
        neededCoins.add(coin);
      }
      if (curAmount >= xchAmount) {
        break;
      }
    }

    fullCoins.addAll(standartWalletService.convertXchCoinsToFull(neededCoins));

    spentCoinsMap["null"] = neededCoins;
  }

  Future<void> getFullCoinsByAssetId({required String url,
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
      if (!isCoinSpent) {
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

  Future<void> offerRemoveLiquidity({required List<String> mnemonics,
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
        var parent_coin_info = item["parent_coin_info"].toString();
        spentCoinsParents.add(parent_coin_info);
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

  Future<void> offerAddLiquidity({required List<String> mnemonics,
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
        var parent_coin_info = item["parent_coin_info"].toString();
        spentCoinsParents.add(parent_coin_info);
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

  Future<void> tibetSwapXCHToCAT({required List<String> mnemonics,
    required String url,
    required String assetId,
    required int xchAmount,
    required int catAmount,
    required int observer,
    required int nonObserver,
    required int fee,
    required String spentXCHCoinsJson}) async {
    NetworkContext().setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);

    final fullNodeRpc = FullNodeHttpRpc(url);

    KeychainCoreSecret keychainSecret =
    KeychainCoreSecret.fromMnemonic(mnemonics);
    final walletsSetList = <WalletSet>[];
    for (var i = 0; i < 5; i++) {
      final set1 = WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
      walletsSetList.add(set1);
    }
    final keychain = WalletKeychain.fromWalletSets(walletsSetList);

    final fullNode = ChiaFullNodeInterface(fullNodeRpc);
    final offerService = OffersService(fullNode: fullNode, keychain: keychain);

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
        var parent_coin_info = item["parent_coin_info"].toString();
        spentCoinsParents.add(parent_coin_info);
      }
    }

    var curAmount = 0;
    final totalAmount = xchAmount + fee;
    List<Coin> neededCoins = [];
    List<Coin> totalCoins = await fullNode.getCoinsByPuzzleHashes(puzzleHashes);
    for (var coin in totalCoins) {
      var isCoinSpent =
      spentCoinsParents.contains(coin.parentCoinInfo.toString());
      debugPrint(
          "Found spent coins for xch parent coin info $isCoinSpent : Parent Coin Info : ${coin
              .parentCoinInfo.toString()}");
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
  }

  Future<void> tibetSwapCATToXCH({required List<String> mnemonics,
    required String url,
    required String assetId,
    required int xchAmount,
    required int catAmount,
    required int observer,
    required int nonObserver,
    required int fee,
    required String spentCoinsJson}) async {
    var key = "${mnemonics.join(" ")}_${observer}_$nonObserver";
    var keychain = cachedWalletChains[key] ??
        generateKeyChain(mnemonics, observer, nonObserver);
    var catHash = Puzzlehash.fromHex(assetId);

    NetworkContext().setBlockchainNetwork(blockchainNetworks[Network.mainnet]!);
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

    var myOuterPuzzlehashes = keychain.getOuterPuzzleHashesForAssetId(catHash);

    for (var element in keychain.hardenedMap.keys) {
      var outer = WalletKeychain.makeOuterPuzzleHash(
        element,
        catHash,
      );
      myOuterPuzzlehashes.add(outer);
    }

    // remove duplicate puzzlehashes
    myOuterPuzzlehashes = myOuterPuzzlehashes.toSet().toList();

    /// Search for the cat coins for offered
    final responseDataCAT = await fullNode.getCoinsByPuzzleHashes(
      myOuterPuzzlehashes,
    );

    List<String> spentCoinsParents = [];
    if (spentCoinsJson.isNotEmpty) {
      List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoinsJson);
      for (var item in spentCoinsJsonDecoded) {
        var parent_coin_info = item["parent_coin_info"].toString();
        spentCoinsParents.add(parent_coin_info);
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
    final targePh = keychain.puzzlehashes[1];

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
      targetPuzzleHash: targePh,
      fee: fee,
    );
    final str = offer.toBench32();
    debugPrint("Offer generate cat to xch : $str");
    _channel.invokeMethod("offerCATToXCH", {
      "offer": str,
      "XCHCoins": jsonEncode(neededXCHCoins),
      "CATCoins": jsonEncode(neededCatCoins)
    });
  }

  Future<void> tibetSwapXCHToCat({required List<String> mnemonics,
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
    final targePh = keyChain.puzzlehashes[1];
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
      targetPuzzleHash: targePh,
    );
    final str = offer.toBench32();
    debugPrint("Offering xch for cat : $str");
    _channel.invokeMethod("offer", {"offer": str});
  }

  Future<void> cachedWalletKeyChain(List<String> mnemonics, int observer,
      int nonObserver) async {
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

  void changeSettingsWalletPuzzleHashes({required List<String> mnemonics,
    required int observer,
    required int non_observer,
    required List<String> asset_ids}) {
    try {
      KeychainCoreSecret keychainSecret =
      KeychainCoreSecret.fromMnemonic(mnemonics);
      final maxNum = max(observer, non_observer);
      final walletsSetList = <WalletSet>[];
      for (var i = 0; i < maxNum; i++) {
        final set1 =
        WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
        walletsSetList.add(set1);
      }
      final keychain = WalletKeychain.fromWalletSets(walletsSetList);
      var key = "${mnemonics.join(' ')}_${observer}_$non_observer";
      cachedWalletChains[key] = keychain;
      final main_puzzle_hashes = keychain.puzzlehashes.sublist(0, observer);
      main_puzzle_hashes
          .addAll(keychain.walletPuzzlehashes.sublist(0, non_observer));

      Map<String, List<String>> mapToAndroid = {};

      mapToAndroid["main_puzzle_hashes"] =
          main_puzzle_hashes.map((e) => e.toHex()).toList();

      asset_ids.forEach((asset_id) {
        List<String> outer_hashes = [];
        main_puzzle_hashes.forEach((main_hash) {
          if (asset_id.isNotEmpty) {
            outer_hashes.add(WalletKeychain.makeOuterPuzzleHash(
                Puzzlehash.fromHex(main_hash.toHex()),
                Puzzlehash.fromHex(asset_id))
                .toHex());
          }
        });
        mapToAndroid[asset_id] = outer_hashes;
      });

      _channel.invokeMethod("changeSettings", mapToAndroid);
    } catch (ex) {
      debugPrint(
          "Exception  caught in flutter in change settings : ${ex.toString()}");
    }
  }

  Future generateSpendBundleForToken({required int fee,
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
      var stopwatch = Stopwatch()
        ..start();
      var key = "${mnemonic.join(" ")}_${observer}_$nonObserver";
      var keyChain = cachedWalletChains[key] ??
          generateKeyChain(mnemonic, observer, nonObserver);

      debugPrint(
          'Time taken: ${stopwatch.elapsedMilliseconds /
              1000}s  to initialize keyChain');

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

      stopwatch = Stopwatch()
        ..start();

      final fullNodeRpc = FullNodeHttpRpc(httpUrl);
      final fullNode = ChiaFullNodeInterface(fullNodeRpc);

      final responseDataCAT =
      await fullNode.getCoinsByPuzzleHashes(myOuterPuzzlehashes);

      debugPrint(
          "My First Response From retrieving cat: $responseDataCAT, to get unspent coins ${stopwatch
              .elapsedMilliseconds / 1000}s");

      List<dynamic> spentCoinsJsonDecoded = json.decode(spentCoinsJson);
      List<String> spentCoinsParents = [];
      for (var item in spentCoinsJsonDecoded) {
        var parent_coin_info = item["parent_coin_info"].toString();
        spentCoinsParents.add(parent_coin_info);
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
          "Sending cat coins : $catCoins,  Dest Hash : ${Address(destAddress)
              .toPuzzlehash()}");
      final spendBundle = catWalletService.createSpendBundle(
          payments: [Payment(amount, Address(destAddress).toPuzzlehash())],
          catCoinsInput: catCoins,
          keychain: keyChainCAT,
          changePuzzlehash: keyChainCAT.puzzlehashes[0],
          fee: fee,
          standardCoinsForFee: standardCoinsForFee);

      var dest_puzzle_has = Address(destAddress).toPuzzlehash();
      var outer_dest_puzzle_hash = WalletKeychain.makeOuterPuzzleHash(
          dest_puzzle_has, Puzzlehash.fromHex(asset_id))
          .toHex();

      _channel.invokeMethod('getSpendBundle', {
        "spendBundle": spendBundle.toJson(),
        "dest_puzzle_hash": dest_puzzle_has.toHex(),
        "spentCoins": jsonEncode(standardCoinsForFee),
        "spentTokens": jsonEncode(filteredCoins)
      });
    } catch (e) {
      debugPrint("Caught exception in dart code for token" + e.toString());
      _channel.invokeMethod("exception", e.toString());
    }
  }

  static Future<void> getCatCoinsDetail({required Coin coin,
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

  Future<void> getStandardCoinsForFee({required WalletKeychain keyChain,
    required int observer,
    required int non_observer,
    required String httpUrl,
    required List<String> spentCoinsParents,
    required List<Coin> standardCoinsForFee,
    required int fee,
    required ChiaFullNodeInterface fullNode}) async {
    var main_puzzle_hashes =
    keyChain.hardenedMap.keys.toList().sublist(0, non_observer);
    main_puzzle_hashes
        .addAll(keyChain.unhardenedMap.keys.toList().sublist(0, observer));
    List<Coin> feeStandardCoinsTotal =
    await fullNode.getCoinsByPuzzleHashes(main_puzzle_hashes);
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

  Future generateSpendBundleXCH({required int fee,
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
        var parent_coin_info = item["parent_coin_info"].toString();
        spentCoinsParents.add(parent_coin_info);
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
              "AlreadySpentCoins Contains on sending xch : $isCoinSpent  ${coin
                  .parentCoinInfo} in $spentCoinsParents");
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
      List<String> def_tokens, int observer, int non_observer) {
    try {
      KeychainCoreSecret keychainSecret =
      KeychainCoreSecret.fromMnemonic(mnemonic);
      var max_hash = max(observer, non_observer);
      final walletsSetList = <WalletSet>[];
      for (var i = 0; i < max_hash; i++) {
        final set1 =
        WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
        walletsSetList.add(set1);
      }
      final keychain = WalletKeychain.fromWalletSets(walletsSetList);
      var key = "${mnemonic.join(" ")}_${observer}_$non_observer";
      cachedWalletChains[key] = keychain;
      final fingerPrint = keychainSecret.masterPublicKey.getFingerprint();
      final address = Address.fromPuzzlehash(
        keychain.puzzlehashes[0],
        prefix,
      );

      var mapToAndroid = {"address": "$address", "fingerPrint": fingerPrint};

      final main_puzzle_hashes = keychain.puzzlehashes.sublist(0, observer);
      main_puzzle_hashes
          .addAll(keychain.walletPuzzlehashes.sublist(0, non_observer));

      mapToAndroid["main_puzzle_hashes"] =
          main_puzzle_hashes.map((e) => e.toHex()).toList();

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
      List<String> def_tokens, int observer, int non_observer) {
    try {
      KeychainCoreSecret keychainSecret =
      KeychainCoreSecret.fromMnemonic(mnemonic);
      var max_hash = max(observer, non_observer);
      final walletsSetList = <WalletSet>[];
      var stopwatch = Stopwatch()
        ..start();
      for (var i = 0; i < max_hash; i++) {
        final set1 =
        WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
        walletsSetList.add(set1);
      }
      stopwatch.stop();
      debugPrint(
          'Time taken: ${stopwatch.elapsedMilliseconds /
              1000}s to initialize walletSetList');
      final keychain = WalletKeychain.fromWalletSets(walletsSetList);
      var key = "${mnemonic.join(' ')}_${observer}_$non_observer";
      cachedWalletChains[key] = keychain;
      final fingerPrint = keychainSecret.masterPublicKey.getFingerprint();
      final address = Address.fromPuzzlehash(
        keychain.puzzlehashes[0],
        prefix,
      );

      var mapToAndroid = {"address": "$address", "fingerPrint": fingerPrint};

      final main_puzzle_hashes = keychain.puzzlehashes.sublist(0, observer);
      main_puzzle_hashes
          .addAll(keychain.walletPuzzlehashes.sublist(0, non_observer));

      mapToAndroid["main_puzzle_hashes"] =
          main_puzzle_hashes.map((e) => e.toHex()).toList();

      debugPrint("Map to Android : $mapToAndroid");
      stopwatch = Stopwatch()
        ..start();
      def_tokens.forEach((asset_id) {
        List<String> outer_hashes = [];
        main_puzzle_hashes.forEach((main_hash) {
          if (asset_id.isNotEmpty)
            outer_hashes.add(WalletKeychain.makeOuterPuzzleHash(
                Puzzlehash.fromHex(main_hash.toHex()),
                Puzzlehash.fromHex(asset_id))
                .toHex());
        });
        mapToAndroid[asset_id] = outer_hashes;
      });
      stopwatch.stop();
      debugPrint(
          'Time taken: ${stopwatch.elapsedMilliseconds /
              1000}s  to generate all cat hashes');

      _channel.invokeMethod("getHash", mapToAndroid);
    } catch (ex) {
      debugPrint("Exception occurred in generating keys : $ex");
    }
  }

  WalletKeychain generateKeyChain(List<String> mnemonic, int observer,
      int non_observer) {
    KeychainCoreSecret keychainSecret =
    KeychainCoreSecret.fromMnemonic(mnemonic);
    var counter = max(observer, non_observer);
    final walletsSetList = <WalletSet>[];
    for (var i = 0; i < counter; i++) {
      final set1 = WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
      walletsSetList.add(set1);
    }
    final keychain = WalletKeychain.fromWalletSets(walletsSetList);
    var key = "${mnemonic.join(" ")}_${observer}_$non_observer";
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
            "Got Result 200 OK from response on flutter side : ${responseData
                .body}");
      } else {
        debugPrint("StatusCode is not ok  : ${responseData.body}");
      }
    } catch (ex) {
      debugPrint("Caught exception in dart code" + ex.toString());
      _channel.invokeMethod("exception", ex.toString());
    }
  }

  WalletKeychain generateKeyChainForAssets(List<String> mnemonic,
      String asset_id, int hash_counter) {
    KeychainCoreSecret keychainSecret =
    KeychainCoreSecret.fromMnemonic(mnemonic);

    final walletsSetList = <WalletSet>[];
    for (var i = 0; i < hash_counter; i++) {
      final set1 = WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
      walletsSetList.add(set1);
    }

    final keychain = WalletKeychain.fromWalletSets(walletsSetList)
      ..addOuterPuzzleHashesForAssetId(Puzzlehash.fromHex(asset_id));

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
        .map((e) =>
        FullCoin.fromCoin(
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

  void generateCATPuzzleHash(List<String> main_puzzle_hashes, String asset_id) {
    final List<String> outer_puzzle_hashes = [];
    main_puzzle_hashes.forEach((element) {
      if (element.isNotEmpty) {
        outer_puzzle_hashes.add(WalletKeychain.makeOuterPuzzleHash(
            Puzzlehash.fromHex(element), Puzzlehash.fromHex(asset_id))
            .toHex());
      }
    });
    _channel
        .invokeMethod('generate_outer_hash', {asset_id: outer_puzzle_hashes});
  }

  Future<void> asyncCATPuzzleHash(List<String> main_puzzle_hashes,
      String asset_id) async {
    final List<String> outer_puzzle_hashes = [];
    main_puzzle_hashes.forEach((element) {
      if (element.isNotEmpty) {
        outer_puzzle_hashes.add(WalletKeychain.makeOuterPuzzleHash(
            Puzzlehash.fromHex(element), Puzzlehash.fromHex(asset_id))
            .toHex());
      }
    });
    _channel.invokeMethod('$asset_id', {asset_id: outer_puzzle_hashes});
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

  Future<void> generateNFTSpendBundle({required String nftCoinJson,
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
          "PuzzleHash to get nft coins on generate spend bundle : ${Puzzlehash
              .fromHex(fromAddress)}");
      var nftCoins = await nftService.getNFTCoinByParentCoinHash(
          parent_coin_info: coin.parentCoinInfo,
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

  Future<void> getFullCoinsDetail({required Coin coin,
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
    var parentCoin = CoinRecordResponse
        .fromJson(
      jsonDecode(bodyParentCoinInfoRes.body) as Map<String, dynamic>,
    )
        .coinRecord!
        .toCoin();
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
    var parentCoinSpend = CoinSpendResponse
        .fromJson(
      jsonDecode(bodyParentCoinSpentRes.body) as Map<String, dynamic>,
    )
        .coinSpend;
    fullCoins.add(FullCoin(
      parentCoinSpend: parentCoinSpend!,
      coin: coin,
    ));
  }

  Future<FullNFTCoinInfo> convertFullCoin(FullCoin coin,
      WalletKeychain keychain) async {
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
      final did = await getMinterNft(Puzzlehash(nftInfo.item1.launcherId));
      if (did != null) {
        nftFullInfo = nftFullInfo.copyWith(minterDid: did.didId);
      }
      print("Minter DID is null");
    }
    return nftFullInfo;
  }

  Future<DidInfo?> getMinterNft(Puzzlehash launcherId,) async {
    final body = <String, dynamic>{
      'parent_ids': [launcherId].map((parentId) => parentId.toHex()).toList(),
    };
    body['include_spent_coins'] = true;

    final response = await post(Uri.parse(''),
        headers: <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: jsonEncode(body));

    final mainChildrens = CoinRecordsResponse
        .fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    )
        .coinRecords
        .map((record) => record.toCoin())
        .toList();

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
    required int non_observer,
    required int start_height,
    required String parent_coin_info,
    required String puzzle_hash,
    required String base_url,
  }) async {
    try {
      var key = "${mnemonics.join(" ")}_${observer}_$non_observer";

      final keychain = cachedWalletChains[key] ??
          generateKeyChain(mnemonics, observer, non_observer);

      ChiaNetworkContextWrapper().registerNetworkContext(Network.mainnet);
      final fullNodeRpc = FullNodeHttpRpc(base_url);
      final fullNode = ChiaFullNodeInterface(fullNodeRpc);
      final nftService =
      NftNodeWalletService(fullNode: fullNode, keychain: keychain);
      var nftCoins = await nftService.getNFTCoinByParentCoinHash(
          parent_coin_info: Bytes.fromHex(parent_coin_info),
          puzzle_hash: Puzzlehash.fromHex(puzzle_hash),
          startHeight: start_height);
      if (nftCoins.isEmpty) {
        debugPrint("NFtCoins to uncurry is empty");
      }
      final nftCoin = nftCoins[0];
      final nftFullCoin_ = await nftService.convertFullCoin(nftCoin);
      final nftInfo = nftFullCoin_.toNftCoinInfo();
      final info = UncurriedNFT.uncurry(nftInfo.fullPuzzle);
      final launcherId = info.singletonLauncherId.atom;
      final address = NftAddress
          .fromPuzzlehash(Puzzlehash(launcherId))
          .address;
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
}
