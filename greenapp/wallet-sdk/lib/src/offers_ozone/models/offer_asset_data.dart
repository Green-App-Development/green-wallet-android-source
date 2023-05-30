import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:equatable/equatable.dart';

class OfferAssetData extends Equatable {
  final Bytes? assetId;
  final SpendType type;

  OfferAssetData({required this.assetId, required this.type});
  static OfferAssetData? fromFullCoin(FullCoin coin) {
    final type = coin.parentCoinSpend?.type ?? SpendType.standard;
    Bytes? assetId;
    if (type == SpendType.cat2) {
      assetId = coin.parentCoinSpend!.getTailHash();
    } else if (type == SpendType.nft) {
      // The nft info will be completed for  prepareFullCoins
    } else {
      return OfferAssetData.standart();
    }
    return OfferAssetData(assetId: assetId, type: type);
  }

  factory OfferAssetData.cat({
    required Bytes tailHash,
  }) {
    return OfferAssetData(
      assetId: tailHash,
      type: SpendType.cat2,
    );
  }
  factory OfferAssetData.singletonNft({
    required Bytes launcherPuzhash,
  }) {
    return OfferAssetData(
      assetId: launcherPuzhash,
      type: SpendType.nft,
    );
  }
  static OfferAssetData? standart() {
    return null;
  }

  @override
  List<Object?> get props => [type, assetId];
}

class PreparedTradeData {
  final Map<Bytes, PuzzleInfo> driverDict;
  final Map<Bytes?, List<Payment>> payments;
  final Map<Bytes?, int> offerredAmounts;
  final Bytes? nftOfferedLauncher;
  final bool requestedLauncher;

  const PreparedTradeData({
    required this.driverDict,
    required this.payments,
    required this.offerredAmounts,
    required this.nftOfferedLauncher,
    required this.requestedLauncher,
  });
}
