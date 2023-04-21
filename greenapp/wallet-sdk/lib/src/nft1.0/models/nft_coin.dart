import '../../../chia_crypto_utils.dart';

class NftCoin {
  final CoinPrototype coin;
  final LineageProof lineageProof;
  final Program fullPuzzle;

  NftCoin({
    required this.coin,
    required this.lineageProof,
    required this.fullPuzzle,
  });
}
