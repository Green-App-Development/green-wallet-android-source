import 'package:chia_crypto_utils/chia_crypto_utils.dart';

class NFTCoinInfo extends Coin {
  /// The launcher coin ID of the NFT
  final Bytes nftId;
  final CoinPrototype coin;
  final LineageProof? lineageProof;
  final Program fullPuzzle;
  final int mintHeight;
  final Bytes? minterDid;
  final int latestHeight;
  final bool pendingTransaction;

  Bytes get launcherId => nftId;

  NFTCoinInfo({
    required this.nftId,
    required this.coin,
    this.lineageProof,
    required this.fullPuzzle,
    required this.mintHeight,
    required this.latestHeight,
    this.pendingTransaction = false,
    this.minterDid,
    int confirmedBlockIndex = 0,
    int spentBlockIndex = 0,
  }) : super(
          amount: coin.amount,
          puzzlehash: coin.puzzlehash,
          parentCoinInfo: coin.parentCoinInfo,
          coinbase: false,
          confirmedBlockIndex: confirmedBlockIndex,
          spentBlockIndex: spentBlockIndex,
          timestamp: 0,
        );

  @override
  String toString() {
    return 'NFTCoinInfo(nftId: $nftId, coin: $coin, lineageProof: $lineageProof, fullPuzzle: $fullPuzzle, mintHeight: $mintHeight, latestHeight: $latestHeight, pendingTransaction: $pendingTransaction)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;

    return other is NFTCoinInfo &&
        other.nftId == nftId &&
        other.coin == coin &&
        other.lineageProof == lineageProof &&
        other.fullPuzzle == fullPuzzle &&
        other.mintHeight == mintHeight &&
        other.latestHeight == latestHeight &&
        other.pendingTransaction == pendingTransaction;
  }

  @override
  int get hashCode {
    return nftId.hashCode ^
        coin.hashCode ^
        lineageProof.hashCode ^
        fullPuzzle.hashCode ^
        mintHeight.hashCode ^
        latestHeight.hashCode ^
        pendingTransaction.hashCode;
  }
}

class FullNFTCoinInfo extends FullCoin {
  /// The launcher coin ID of the NFT
  final Bytes nftId;

  final LineageProof? nftLineageProof;
  final Program fullPuzzle;
  final int mintHeight;
  final Bytes? minterDid;
  final int latestHeight;
  final bool pendingTransaction;

  Bytes get launcherId => nftId;

  FullNFTCoinInfo({
    required this.nftId,
    required Coin coin,
    this.nftLineageProof,
    required this.fullPuzzle,
    required this.mintHeight,
    required this.latestHeight,
    this.pendingTransaction = false,
    this.minterDid,
    int confirmedBlockIndex = 0,
    int spentBlockIndex = 0,
    required CoinSpend? parentCoinSpend,
  }) : super(
          coin: coin,
          parentCoinSpend: parentCoinSpend,
        );

  @override
  String toString() {
    return 'NFTCoinInfo(nftId: $nftId, coin: $coin, lineageProof: $lineageProof, fullPuzzle: $fullPuzzle, mintHeight: $mintHeight, latestHeight: $latestHeight, pendingTransaction: $pendingTransaction)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;

    return other is NFTCoinInfo &&
        other.nftId == nftId &&
        other.coin == coin &&
        other.lineageProof == lineageProof &&
        other.fullPuzzle == fullPuzzle &&
        other.mintHeight == mintHeight &&
        other.latestHeight == latestHeight &&
        other.pendingTransaction == pendingTransaction;
  }

  @override
  int get hashCode {
    return nftId.hashCode ^
        coin.hashCode ^
        lineageProof.hashCode ^
        fullPuzzle.hashCode ^
        mintHeight.hashCode ^
        latestHeight.hashCode ^
        pendingTransaction.hashCode;
  }

  NFTCoinInfo toNftCoinInfo() {
    return NFTCoinInfo(
      nftId: nftId,
      coin: coin,
      lineageProof: nftLineageProof,
      fullPuzzle: fullPuzzle,
      mintHeight: mintHeight,
      latestHeight: latestHeight,
      pendingTransaction: pendingTransaction,
      minterDid: minterDid,
      confirmedBlockIndex: 0,
      spentBlockIndex: 0,
    );
  }
}
