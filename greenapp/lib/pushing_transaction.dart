import 'dart:convert';
import 'dart:io';

import 'package:chia_crypto_utils/chia_crypto_utils.dart' hide Client;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_splash_screen_module/main.dart';
import 'package:http/http.dart';
import 'dart:math';
import 'package:http/http.dart' as http;
import 'package:http/io_client.dart';

class PushingTransaction {
  static const MethodChannel _channel =
  MethodChannel('METHOD_CHANNEL_GENERATE_HASH');

  final cachedWalletChains = Map<String, WalletKeychain>();

  init() {
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
      }
    });
    testingMethod();
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
          if (asset_id.isNotEmpty)
            outer_hashes.add(WalletKeychain.makeOuterPuzzleHash(
                Puzzlehash.fromHex(main_hash.toHex()),
                Puzzlehash.fromHex(asset_id))
                .toHex());
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
      Map<String, dynamic> body = {
        "puzzle_hashes": myOuterPuzzlehashes.map((e) => e.toHex()).toList()
      };

      final responseDataCAT =
      await post(Uri.parse("$httpUrl/get_coin_records_by_puzzle_hashes"),
          headers: <String, String>{
            'Content-Type': 'application/json; charset=UTF-8',
          },
          body: jsonEncode(body));

      debugPrint(
          "My Response From retrieving cat  : ${responseDataCAT
              .body}, to get unspent coins ${stopwatch.elapsedMilliseconds /
              1000}s");

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
            fee: fee));
      }

      await Future.wait(futures);
      debugPrint("SpentCoinsPrototypes on sending : $spentCoinsParents");

      if (responseDataCAT.statusCode == 200) {
        List<Coin> allCatCoins = [
          for (final item in jsonDecode(responseDataCAT.body)['coin_records'])
            Coin.fromChiaCoinRecordJson(item)
        ];

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
              coin: coin, httpUrl: httpUrl, catCoins: catCoins));
          if (futures.length >= 1) {
            debugPrint(
                "Filled  futures with ${futures
                    .length} wait for them to complete");
            await Future.wait(futures);
            futures.clear();
          }
        }
        debugPrint("Sending cat coins future size : ${futures.length}");
        await Future.wait(futures);
        debugPrint("Sending cat coins : $catCoins");
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
          "dest_puzzle_hash": outer_dest_puzzle_hash,
          "spentCoins": jsonEncode(standardCoinsForFee),
          "spentTokens": jsonEncode(filteredCoins)
        });
      } else {
        debugPrint("StatusCode is not ok  : ${responseDataCAT.body}");
        throw Exception("Response on coin records is not ok");
      }
    } catch (e) {
      debugPrint("Caught exception in dart code for token" + e.toString());
      _channel.invokeMethod("exception", e.toString());
    }
  }

  Future<void> getCatCoinsDetail({required Coin coin,
    required String httpUrl,
    required List<CatCoin> catCoins}) async {
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
    required int fee}) async {
    var main_puzzle_hashes = keyChain.hardenedMap.keys
        .map((e) => e.toHex())
        .toList()
        .sublist(0, non_observer);
    main_puzzle_hashes.addAll(keyChain.unhardenedMap.keys
        .map((e) => e.toHex())
        .toList()
        .sublist(0, observer));
    Map<String, dynamic> standardCoinBody = {
      "puzzle_hashes": main_puzzle_hashes
    };
    final responseDataForStandardCoins =
    await post(Uri.parse("$httpUrl/get_coin_records_by_puzzle_hashes"),
        headers: <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: jsonEncode(standardCoinBody));
    List<Coin> feeStandardCoinsTotal = [
      for (final item
      in jsonDecode(responseDataForStandardCoins.body)['coin_records'])
        Coin.fromChiaCoinRecordJson(item)
    ];
    var feeSum = 0;
    feeStandardCoinsTotal.sort((a, b) {
      return b.amount.compareTo(a.amount);
    });
    debugPrint(
        "Getting standard coins for fee : $standardCoinsForFee when sending token");
    for (var coin in feeStandardCoinsTotal) {
      var feeCoinSpent =
      spentCoinsParents.contains(coin.parentCoinInfo.toString());
      debugPrint(
          "FeeCoinIsSpent for fee when sending token : $feeCoinSpent and Infos : $spentCoinsParents");
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
      debugPrint("Exception occurred in generating keys : ${ex}");
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
      debugPrint("Exception occurred in generating keys : ${ex}");
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
    KeychainCoreSecret keychainSecret =
    KeychainCoreSecret.fromMnemonic(mnemonic);
    final walletsSetList = <WalletSet>[];
    for (var i = 0; i < 1; i++) {
      final set1 = WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
      walletsSetList.add(set1);
    }
    final keychain = WalletKeychain.fromWalletSets(walletsSetList);


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




}
