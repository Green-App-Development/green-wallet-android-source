import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:flutter_splash_screen_module/flutter_token.dart';
import 'package:flutter_splash_screen_module/pushing_transaction.dart';

Future<void> saveFullCoinsCAT(
    String url,
    FlutterToken token,
    List<String> spentCoinsParents,
    List<FullCoin> fullCoins,
    List<Coin> usedCoins,
    ChiaFullNodeInterface fullNode,
    WalletKeychain keychain) async {
  var catHash = Puzzlehash.fromHex(token.assetID);

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

  // hydrate cat coins
  var curCATAmount = 0;
  for (final coin in responseDataCAT) {
    var isCoinSpent =
        spentCoinsParents.contains(coin.parentCoinInfo.toString());
    if (!isCoinSpent) {
      curCATAmount += coin.amount;
      usedCoins.add(coin);
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
}

Future<void> saveFullCoinsXCH(
    int fee,
    String url,
    FlutterToken token,
    List<String> spentCoinsParents,
    List<FullCoin> fullCoins,
    List<Coin> usedXCHCoins,
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
    var isCoinSpent =
        spentCoinsParents.contains(coin.parentCoinInfo.toString());
    if (!isCoinSpent) {
      curXCHAmount += coin.amount;
      neededXCHCoins.add(coin);
      usedXCHCoins.add(coin);
      if (curXCHAmount >= token.amount + fee) {
        break;
      }
    }
  }
  final standartWalletService = StandardWalletService();
  final xchFullCoins =
      standartWalletService.convertXchCoinsToFull(neededXCHCoins);

  fullCoins.addAll(xchFullCoins);
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

Map<OfferAssetData, List<int>> offerAssetDataParams(
    List<FlutterToken> list, bool isOffered) {
  Map<OfferAssetData, List<int>> param = <OfferAssetData, List<int>>{};

  

  return param;
}
