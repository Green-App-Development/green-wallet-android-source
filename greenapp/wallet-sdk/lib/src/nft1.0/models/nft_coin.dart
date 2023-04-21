import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/nft1.0/index.dart';

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
