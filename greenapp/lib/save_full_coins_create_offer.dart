import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_splash_screen_module/flutter_token.dart';

import 'nft_service.dart';

Future<void> saveFullCoinsCAT(
    String url,
    FlutterToken token,
    List<String> spentCoinsParents,
    List<FullCoin> fullCoins,
    Map<String?, List<Coin>> spentCoinsMap,
    ChiaFullNodeInterface fullNode,
    WalletKeychain keychain) async {
  var catHash = Puzzlehash.fromHex(token.assetID);

  keychain.addOuterPuzzleHashesForAssetId(catHash);

  var myOuterPuzzleHashes = keychain.getOuterPuzzleHashesForAssetId(catHash);

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

  List<CatCoin> catCoins = [];
  List<Coin> neededCoins = [];

  // hydrate cat coins
  var curCATAmount = 0;
  for (final coin in responseDataCAT) {
    var isCoinSpent = spentCoinsParents.contains("0x${coin.parentCoinInfo}");
    if (!isCoinSpent && coin.amount!=0) {
      curCATAmount += coin.amount;
      neededCoins.add(coin);
      await getCatCoinsDetail(
        coin: coin,
        httpUrl: url,
        catCoins: catCoins,
        fullNode: fullNode,
      );
      if (curCATAmount >= token.amount) {
        break;
      }
    }
  }

  // hydrate full coins
  List<FullCoin>? fullCatCoins = catCoins.map((e) {
    //Search for the Coin
    final coinFounded = responseDataCAT
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

  fullCoins.addAll(fullCatCoins);

  spentCoinsMap[token.assetID] = neededCoins;
}

Future<void> saveFullCoinsXCH(
    int fee,
    String url,
    FlutterToken token,
    List<String> spentCoinsParents,
    List<FullCoin> fullCoins,
    Map<String, List<Coin>> spentCoinsMap,
    ChiaFullNodeInterface fullNode,
    WalletKeychain keychain) async {
  final puzzleHashes = keychain.hardenedMap.entries.map((e) => e.key).toList();
  for (var element in keychain.unhardenedMap.entries) {
    puzzleHashes.add(element.key);
  }

  List<Coin> totalXCHCoins =
      await fullNode.getCoinsByPuzzleHashes(puzzleHashes);

  List<Coin> neededXCHCoins = [];

  //search for xch full coins
  var curXCHAmount = 0;
  for (var coin in totalXCHCoins) {
    var isCoinSpent = spentCoinsParents.contains("0x${coin.parentCoinInfo}");
    if (!isCoinSpent && coin.amount!=0) {
      curXCHAmount += coin.amount;
      neededXCHCoins.add(coin);
      if (curXCHAmount >= token.amount + fee) {
        break;
      }
    }
  }
  final standartWalletService = StandardWalletService();
  final xchFullCoins =
      standartWalletService.convertXchCoinsToFull(neededXCHCoins);

  fullCoins.addAll(xchFullCoins);

  spentCoinsMap["null"] = neededXCHCoins;
}

Future<void> getCatCoinsDetail(
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

Future<void> offerAssetDataParamsRequested(
    List<FlutterToken> list,
    NftNodeWalletService nftService,
    Map<OfferAssetData?, List<int>> param) async {
  for (var item in list) {
    var amount = item.amount;
    if (item.type == "CAT") {
      final tokenHash = Puzzlehash.fromHex(item.assetID);
      param[OfferAssetData.cat(tailHash: tokenHash)] = [amount];
    } else if (item.type == 'XCH') {
      param[null] = [amount];
    } else {
      //with full coin
      var nftCoin = await nftService
          .getNftFullCoinWithLauncherId(Puzzlehash.fromHex(item.assetID));

      final nftFullCoin = await nftService.convertFullCoin(nftCoin!);

      debugPrint("Found NFTCoins LauncherID : ${nftFullCoin.launcherId}");
      param[OfferAssetData.singletonNft(
          launcherPuzhash: nftFullCoin.launcherId)] = [1];

      // //with just nftID
      // final nftAddress = NftAddress(item.assetID);
      // param[OfferAssetData.singletonNft(
      //     launcherPuzhash: nftAddress.toPuzzlehash())] = [1];
    }
  }
}

Map<OfferAssetData?, int> offerAssetDataParamsOffered(List<FlutterToken> list) {
  Map<OfferAssetData?, int> param = <OfferAssetData?, int>{};

  for (var item in list) {
    var amount = item.amount;
    debugPrint("Item on offerAssetDataParamsOffered : $item");
    if (item.type == "CAT") {
      final tokenHash = Puzzlehash.fromHex(item.assetID);
      param[OfferAssetData.cat(tailHash: tokenHash)] = -amount;
    } else if (item.type == 'XCH') {
      param[null] = amount;
    } else {}
  }

  return param;
}
