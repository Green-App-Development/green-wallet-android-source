import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/api/nft1/nft_service.dart';
import 'package:tuple/tuple.dart';

class OffersService {
  final ChiaFullNodeInterface fullNode;
  final WalletKeychain keychain;
  OffersService({
    required this.fullNode,
    required this.keychain,
  });

  StandardWalletService get walletService => StandardWalletService();

  Future<AnalizedOffer?> analizeOffer({
    required int fee,
    required Puzzlehash targetPuzzleHash,
    required Puzzlehash changePuzzlehash,
    required Offer offer,
  }) async {
    final tradeManager = TradeManagerService();
    final analizedOffer = await tradeManager.analizeOffer(
      fee: fee,
      targetPuzzleHash: targetPuzzleHash,
      changePuzzlehash: changePuzzlehash,
      offer: offer,
    );
    return analizedOffer;
  }

  Future<Tuple2<ChiaBaseResponse, Offer>> responseOffer({
    required int fee,
    required Puzzlehash targetPuzzleHash,
    required Offer offer,
    required Puzzlehash changePuzzlehash,
    required List<FullCoin> coinsToUse,
  }) async {
    final tradeManager = TradeManagerService();

    final analizedOffer = await tradeManager.analizeOffer(
        fee: fee,
        targetPuzzleHash: targetPuzzleHash,
        changePuzzlehash: changePuzzlehash,
        offer: offer);

    final preparedData = await _prepareOfferDataForTrade(
        offerredAmounts: tradeManager.convertRequestedToOffered(
          analizedOffer!.requested,
        ),
        fee: fee,
        royaltyPercentage: analizedOffer.royaltyPer,
        royaltyAmount: analizedOffer.royaltyAmount,
        requesteAmounts: TradeManagerService().convertOfferedToRequested(analizedOffer.offered),
        coinsToUse: coinsToUse);

    final completedOffer = await tradeManager.responseOffer(
      groupedCoins: preparedData.selectedCoins,
      keychain: preparedData.keychain,
      fee: fee,
      targetPuzzleHash: targetPuzzleHash,
      changePuzzlehash: changePuzzlehash,
      offer: offer,
      buildKeychainForNft: (phs) async => keychain,
    );

    if (completedOffer.isValid()) {
      final spendBundle = completedOffer.toValidSpend(arbitragePh: changePuzzlehash);
      final chiaResponse = await fullNode.pushTransaction(spendBundle);
      return Tuple2(chiaResponse, completedOffer);
    } else {
      throw Exception('Offer repsonse is not valid');
    }
  }

  Future<Offer> createOffer({
    required Puzzlehash targetPuzzleHash,
    int fee = 0,
    required List<FullCoin> coins,
    required Map<OfferAssetData?, List<int>> requesteAmounts,
    required Map<OfferAssetData?, int> offerredAmounts,
    bool isOld = false,
    required Puzzlehash changePuzzlehash,
  }) async {
    final preparedData = await _prepareOfferDataForTrade(
      offerredAmounts: offerredAmounts,
      requesteAmounts: requesteAmounts,
      fee: fee,
      coinsToUse: coins,
    );

    for (final asset in preparedData.nftCoins.keys) {
      final launcherId = asset!.assetId!;
      final nft = preparedData.nftCoins[asset]!;

      if (nft is FullNFTCoinInfo) {
        final nftOfferAssetData = OfferAssetData.singletonNft(launcherPuzhash: launcherId);
        preparedData.selectedCoins[nftOfferAssetData] = [nft];
      } else {
        final nftCoinData = await NftWallet().getNFTFullCoinInfo(
          nft,
          buildKeychain: (Set<Puzzlehash> phs) async {
            return keychain;
          },
        );
        final nftOfferAssetData = OfferAssetData.singletonNft(launcherPuzhash: launcherId);
        preparedData.selectedCoins[nftOfferAssetData] = [nftCoinData.item1];
      }
    }

    final offer = await TradeManagerService().createOffer(
      groupedCoins: preparedData.selectedCoins,
      requestedAmounts: requesteAmounts,
      offerredAmounts: offerredAmounts,
      keychain: keychain,
      fee: fee,
      targetPuzzleHash: targetPuzzleHash,
      isOld: isOld,
      changePuzzlehash: changePuzzlehash,
    );
    return offer;
  }

  Future<_PreparedOfferDataForTradeService> _prepareOfferDataForTrade({
    required Map<OfferAssetData?, int> offerredAmounts,
    required List<FullCoin> coinsToUse,
    required int fee,
    int? royaltyPercentage,
    int? royaltyAmount,
    required Map<OfferAssetData?, List<int>> requesteAmounts,
  }) async {
    Map<OfferAssetData?, List<FullCoin>> coins = {};
    Map<OfferAssetData?, FullCoin> nftCoins = {};
    List<Bytes> unknowLauncherIds = [];

    for (final asset in requesteAmounts.keys) {
      final type = asset?.type;
      if (type == SpendType.nft) {
        final coinsFounded = coinsToUse;
        final nftCoinsFounded = _filterCoins(coinsFounded, asset);
        if (nftCoinsFounded.isEmpty) {
          unknowLauncherIds.add(asset!.assetId!);
        } else {
          nftCoins[asset] = nftCoinsFounded.first;
        }
      }
    }

    for (var i = 0; i < offerredAmounts.length; i++) {
      final asset = offerredAmounts.keys.elementAt(i);
      final value = offerredAmounts.values.elementAt(i);
      final type = asset?.type;
      if (asset == null) {
        final xchCoins = _filterCoins(coinsToUse, asset);
        final selectedCoins = _getCoinsForAmount(
          xchCoins,
          (value.abs() + fee.abs() + (royaltyAmount?.abs() ?? 0)),
        );

        coins[asset] = selectedCoins.map((e) {
          return e;
        }).toList();
      } else if (type == SpendType.nft) {
        final nftCoinsFounded = _filterCoins(coinsToUse, asset);

        coins[asset] = nftCoinsFounded.map((e) {
          return e;
        }).toList();

        if (coins[asset]!.isEmpty) {
          throw Exception("No coins found for Offer this NFT");
        }
        nftCoins[asset] = nftCoinsFounded.first;
      } else if (type == SpendType.cat2) {
        final catCoins = _filterCoins(coinsToUse, asset);

        final selectedCoins = _getCoinsForAmount(
          catCoins,
          value.abs() + (royaltyAmount?.abs() ?? 0),
        );

        coins[asset] = selectedCoins.map((e) {
          return e;
        }).toList();
      }
    }
    if (coins[null] == null && fee > 0) {
      final xchCoins = _filterCoins(coinsToUse, null);
      final selectedCoins = _getCoinsForAmount(xchCoins, fee);

      coins[null] = selectedCoins.map((e) {
        return e;
      }).toList();
    }
    if (unknowLauncherIds.isNotEmpty) {
      for (final launcherId in unknowLauncherIds) {
        final nftCoinsDownloaded =
            await NftNodeWalletService(fullNode: fullNode, keychain: keychain)
                .getNFTFullCoinByLauncherId(Puzzlehash(launcherId));
        if (nftCoinsDownloaded == null) {
          throw Exception('NFT not found');
        }
        coins[OfferAssetData.singletonNft(
          launcherPuzhash: launcherId,
        )] = [nftCoinsDownloaded];
        nftCoins[OfferAssetData.singletonNft(
          launcherPuzhash: launcherId,
        )] = nftCoinsDownloaded;
      }
    }

    return _PreparedOfferDataForTradeService(
        selectedCoins: coins, keychain: keychain, nftCoins: nftCoins);
  }

  List<FullCoin> _filterCoins(List<FullCoin> coins, OfferAssetData? asset) {
    if (asset == null) {
      return coins.where((element) => element.type == SpendType.standard).toList();
    }
    if (asset.type == SpendType.nft) {
      final coinsFounded = coins.where((element) {
        if (element is FullNFTCoinInfo) {
          final launcherId = element.launcherId;
          return launcherId == asset.assetId;
        }
        return false;
      }).toList();
      // get the item with max value of confirmedBlockIndex
      coinsFounded.sort(
        (a, b) => b.coin.confirmedBlockIndex.compareTo(
          a.coin.confirmedBlockIndex,
        ),
      );
      return coinsFounded.isNotEmpty ? [coinsFounded.first] : [];
    }
    if (asset.type == SpendType.cat2) {
      return coins.where((element) {
        if (element.type != SpendType.cat2) {
          return false;
        }
        return element.toCatCoin().assetId == asset.assetId;
      }).toList();
    }
    return coins;
  }

  List<FullCoin> _getCoinsForAmount(List<FullCoin> coins, int amount, {int minCoinsCount = 1}) {
    coins.sort((a, b) => a.amount.compareTo(b.amount));
    var coinsStandDefinitive = <FullCoin>[];
    var amountSum = 0;

    for (var i = 0; i < coins.length; i++) {
      amountSum += coins[i].amount;
      coinsStandDefinitive.add(coins[i]);
      if (amountSum >= amount && coinsStandDefinitive.length >= (minCoinsCount)) {
        break;
      }
    }
    return coinsStandDefinitive;
  }
}

class _PreparedOfferDataForTradeService {
  final Map<OfferAssetData?, List<FullCoin>> selectedCoins;
  final Map<OfferAssetData?, FullCoin> nftCoins;
  final WalletKeychain keychain;

  const _PreparedOfferDataForTradeService(
      {required this.selectedCoins, required this.keychain, required this.nftCoins});
  List<FullCoin> get selectedCoinsList {
    return selectedCoins.values.expand((element) => element).toList();
  }
}
