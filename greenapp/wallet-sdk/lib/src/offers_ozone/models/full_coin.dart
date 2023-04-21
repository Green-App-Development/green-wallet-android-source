import 'dart:math';

import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/utils/doubles_parse.dart';

Puzzlehash? getTailHash(CoinSpend? parentCoinSpend) {
  try {
    final uncurried = parentCoinSpend!.puzzleReveal.uncurry();
    final arguments = uncurried.arguments;
    if (uncurried.program.hash() == CAT_MOD_HASH) {
      if (arguments.length > 1) {
        return Puzzlehash(parentCoinSpend.puzzleReveal.uncurry().arguments[1].atom);
      }
    }

    return null;
  } catch (e) {
    return null;
  }
}

class FullCoin extends CoinPrototype {
  final CoinSpend? parentCoinSpend;
  late final Puzzlehash? assetId;
  late final Program lineageProof;
  late final Coin coin;
  bool get isCatCoin {
    try {
      final _ = toCatCoin();
      return true;
    } catch (_) {
      return false;
    }
  }

  FullCoin({
    this.parentCoinSpend,
    required this.coin,
  })  : assetId = getTailHash(parentCoinSpend),
        lineageProof = (parentCoinSpend?.puzzleReveal.uncurry().arguments.length ?? 0) > 2
            ? Program.list([
                Program.fromBytes(
                  parentCoinSpend!.coin.parentCoinInfo,
                ),
                // third argument to the cat puzzle is the inner puzzle
                Program.fromBytes(
                  parentCoinSpend.puzzleReveal.uncurry().arguments[2].hash(),
                ),
                Program.fromInt(parentCoinSpend.coin.amount)
              ])
            : Program.nil,
        super(
          parentCoinInfo: coin.parentCoinInfo,
          puzzlehash: coin.puzzlehash,
          amount: coin.amount,
        );

  String toFormatedAmount(String symbol) {
    switch (type) {
      case SpendType.standard:
        return (amount / pow(10, 12)).toRegionalString(decimals: 12, symbol: symbol);
      case SpendType.cat1:
      case SpendType.cat2:
        return (amount / pow(10, 3)).toRegionalString(decimals: 3, symbol: symbol);
      default:
        return amount.toDouble().toRegionalString(decimals: 2, symbol: symbol);
    }
  }

  CatCoin toCatCoin() {
    return CatCoin(parentCoinSpend: parentCoinSpend!, coin: coin);
  }

  @override
  String toString() =>
      'CatCoin(id: $id, parentCoinSpend: $parentCoinSpend, assetId: $assetId, lineageProof: $lineageProof)';

  factory FullCoin.fromCoin(Coin coin, CoinSpend parentCoinSpend) {
    return FullCoin(parentCoinSpend: parentCoinSpend, coin: coin);
  }
  SpendType get type {
    final t = this.parentCoinSpend?.type;
    return t ?? SpendType.standard;
  }

  Coin toCoin() {
    return coin;
  }
}
