import 'dart:convert';

import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/api/did/did_service.dart';
import 'package:http/http.dart' as http;

class NftNodeWalletService {
  final ChiaFullNodeInterface fullNode;
  final WalletKeychain keychain;

  NftNodeWalletService({
    required this.fullNode,
    required this.keychain,
  });

  StandardWalletService get walletService => StandardWalletService();

  /// convert FullCoin to use to transfer or for request offer
  Future<FullNFTCoinInfo> convertFullCoin(FullCoin coin) async {
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
    // if (nftInfo.item1.minterDid == null) {
    //   final did = await getMinterNft(Puzzlehash(nftInfo.item1.launcherId));
    //   if (did != null) {
    //     nftFullInfo = nftFullInfo.copyWith(minterDid: did.didId);
    //   }
    //   print("Minter DID is null");
    // }
    return nftFullInfo;
  }

  /// Get the coins of the keychain(balance)
  Future<List<FullCoin>> getNFTCoins({
    int? startHeight,
    int? endHeight,
    bool includeSpentCoins = false,
  }) async {
    return fullNode.getNftCoinsByInnerPuzzleHashes(
      keychain.puzzlehashes,
      endHeight: endHeight,
      includeSpentCoins: includeSpentCoins,
      startHeight: startHeight,
    );
  }

  /// Get the coins of the keychain(balance)
  Future<List<FullCoin>> getNFTCoinsByCoinHash({
    required String parent_coin_info,
    int? startHeight,
    int? endHeight,
    bool includeSpentCoins = false,
  }) async {
    return fullNode.getNftCoinsByInnerPuzzleHashes(
      keychain.puzzlehashes,
      endHeight: endHeight,
      includeSpentCoins: includeSpentCoins,
      startHeight: startHeight,
    );
  }

  Future<List<FullCoin>> getNFTCoinByParentCoinHash({
    required Bytes assetId,
    required Puzzlehash puzzle_hash,
    int? startHeight,
    int? endHeight,
    bool includeSpentCoins = false,
  }) async {
    return fullNode.getNftCoinsByInnerPuzzleHashesAndCoinHash(
      assetId,
      [puzzle_hash],
      endHeight: endHeight,
      includeSpentCoins: includeSpentCoins,
      startHeight: startHeight,
    );
  }

  Future<FullCoin?> getNftFullCoinWithLauncherId(Puzzlehash launcherId) async {
    final address = NftAddress.fromPuzzlehash(launcherId).address;
    final response = await http.get(Uri.parse(
        "https://api-fin.spacescan.io/nft/transactions/$address?version=0.1.0&network=mainnet"));
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      final coins = data['data'] as List;
      final lastCoin = coins.first;
      final coin = Coin(
        confirmedBlockIndex: int.tryParse(lastCoin['confirmed_index']) ?? 0,
        amount: int.tryParse(lastCoin['amount']) ?? 1,
        parentCoinInfo: Bytes.fromHex(lastCoin['coin_parent']),
        timestamp: int.tryParse(lastCoin['timestamp']) ?? 0,
        coinbase: lastCoin['coinbase'] == true,
        puzzlehash: Puzzlehash.fromHex(lastCoin['puzzle_hash']),
        spentBlockIndex: lastCoin['spent_index'] != null
            ? int.tryParse(lastCoin['spent_index']) ?? 0
            : 0,
      );
      final fullCoin = await fullNode.hydrateFullCoins([coin]);
      return fullCoin.first;
    }
  }

  /// Allow prepare nft coin for transfer of requested in Offer
  Future<FullNFTCoinInfo?> getNFTFullCoinByLauncherId(
      Puzzlehash launcherId,
      ) async {
    final mainChildrens = await fullNode.getCoinsByParentIds(
      [launcherId],
      includeSpentCoins: true,
    );
    final mainHidratedCoins = await fullNode.hydrateFullCoins(mainChildrens);

    if (mainHidratedCoins.isEmpty) {
      throw Exception(
          "Can't be found the NFT coin with launcher ${launcherId}");
    }
    FullCoin nftCoin = mainHidratedCoins.first;
    final lastCoin = await fullNode.getLasUnespentSingletonCoin(nftCoin);
    return convertFullCoin(lastCoin);
  }

  Future<DidInfo?> getMinterNft(
      Puzzlehash launcherId,
      ) async {
    final mainChildrens = await fullNode.getCoinsByParentIds(
      [launcherId],
      includeSpentCoins: true,
    );
    final mainHidratedCoins = await fullNode.hydrateFullCoins(mainChildrens);

    if (mainHidratedCoins.isEmpty) {
      throw Exception(
          "Can't be found the NFT coin with launcher ${launcherId}");
    }
    FullCoin nftCoin = mainHidratedCoins.first;
    print(nftCoin.type);
    final foundedCoins = await fullNode.getAllLinageSingletonCoin(nftCoin);
    final eveCcoin = foundedCoins.first;
    final uncurriedNft =
    UncurriedNFT.tryUncurry(eveCcoin.parentCoinSpend!.puzzleReveal);
    if (uncurriedNft!.supportDid) {
      final minterDid = NftService().getnewOwnerDid(
          unft: uncurriedNft, solution: eveCcoin.parentCoinSpend!.solution);
      if (minterDid != null && minterDid.isNotEmpty) {
        return DidService(fullNode: fullNode, keychain: keychain)
            .getDidInfoByLauncherId(Puzzlehash(minterDid));
      }
    }

    return null;
  }

  /// Allow transfer a NFT to other wallet
  Future<ChiaBaseResponse> transferNFt(
      {required Puzzlehash targePuzzlehash,
        required FullNFTCoinInfo nftCoinInfo,
        int fee = 0,
        required List<CoinPrototype> standardCoinsForFee,
        required Puzzlehash changePuzzlehash}) async {
    final spendBundle = await NftWallet().createTransferSpendBundle(
      nftCoin: nftCoinInfo.toNftCoinInfo(),
      keychain: keychain,
      targetPuzzleHash: targePuzzlehash,
      standardCoinsForFee: standardCoinsForFee,
      fee: fee,
      changePuzzlehash: changePuzzlehash,
    );
    final response = await fullNode.pushTransaction(spendBundle);
    return response;
  }
}
