import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/api/did/did_service.dart';

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
    final nftInfo = await NftWallet().getNFTFullCoinInfo(coin, buildKeychain: (phs) async {
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
      throw Exception("Can't be found the NFT coin with launcher ${launcherId}");
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
      throw Exception("Can't be found the NFT coin with launcher ${launcherId}");
    }
    FullCoin nftCoin = mainHidratedCoins.first;
    print(nftCoin.type);
    final foundedCoins = await fullNode.getAllLinageSingletonCoin(nftCoin);
    final eveCcoin = foundedCoins.first;
    final uncurriedNft = UncurriedNFT.tryUncurry(eveCcoin.parentCoinSpend!.puzzleReveal);
    if (uncurriedNft!.supportDid) {
      final minterDid = NftService()
          .getnewOwnerDid(unft: uncurriedNft, solution: eveCcoin.parentCoinSpend!.solution);
      if (minterDid != null) {
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
