// ignore_for_file: lines_longer_than_80_chars

import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/core/service/base_wallet.dart';
import 'package:chia_crypto_utils/src/offers_ozone/models/full_coin.dart'
as fullCoin;
import '../../did/puzzles/did_puzzles.dart' as didPuzzles;
import 'package:chia_crypto_utils/src/standard/puzzles/p2_delegated_puzzle_or_hidden_puzzle/p2_delegated_puzzle_or_hidden_puzzle.clvm.hex.dart';
import 'package:hex/hex.dart';

class CoinSpend with ToBytesMixin {
  CoinPrototype coin;
  Program puzzleReveal;
  Program solution;

  CoinSpend({
    required this.coin,
    required this.puzzleReveal,
    required this.solution,
  });

  List<CoinPrototype> get additions {
    final result = puzzleReveal.run(solution).program;
    final createCoinConditions = BaseWalletService.extractConditionsFromResult(
      result,
      CreateCoinCondition.isThisCondition,
      CreateCoinCondition.fromProgram,
    );

    return createCoinConditions
        .map(
          (ccc) => CoinPrototype(
        parentCoinInfo: coin.id,
        puzzlehash: ccc.destinationPuzzlehash,
        amount: ccc.amount,
      ),
    )
        .toList();
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
    'coin': coin.toJson(),
    'puzzle_reveal': const HexEncoder().convert(puzzleReveal.serialize()),
    'solution': const HexEncoder().convert(solution.serialize())
  };

  factory CoinSpend.fromBytes(Bytes bytes) {
    final iterator = bytes.iterator;
    return CoinSpend.fromStream(iterator);
  }

  factory CoinSpend.fromStream(Iterator<int> iterator) {
    final coin = CoinPrototype.fromStream(iterator);
    final puzzleReveal = Program.fromStream(iterator);
    final solution = Program.fromStream(iterator);
    return CoinSpend(
      coin: coin,
      puzzleReveal: puzzleReveal,
      solution: solution,
    );
  }

  @override
  Bytes toBytes() {
    Bytes coinBytes = coin.toBytes();
    if (coin is CatCoin) {
      coinBytes = (coin as CatCoin).toCoinPrototype().toBytes();
    }
    return coinBytes +
        Bytes(puzzleReveal.serialize()) +
        Bytes(solution.serialize());
  }

  Puzzlehash? getTailHash() {
    return fullCoin.getTailHash(this);
  }

  factory CoinSpend.fromJson(Map<String, dynamic> json) {
    return CoinSpend(
      coin: CoinPrototype.fromJson(json['coin'] as Map<String, dynamic>),
      puzzleReveal: Program.deserializeHex(json['puzzle_reveal'] as String),
      solution: Program.deserializeHex(json['solution'] as String),
    );
  }

  SpendType get type {
    final uncurried = puzzleReveal.uncurry();
    final uncurriedPuzzleSource = uncurried.program.toSource();
    if (uncurriedPuzzleSource ==
        p2DelegatedPuzzleOrHiddenPuzzleProgram.toSource()) {
      return SpendType.standard;
    }
    if (uncurriedPuzzleSource == CAT_MOD.toSource()) {
      return SpendType.cat2;
    }
    if (uncurriedPuzzleSource == LEGACY_CAT_MOD.toSource()) {
      return SpendType.cat1;
    }
    if (uncurriedPuzzleSource == SINGLETON_TOP_LAYER_MOD_v1_1.toSource()) {
      final nftUncurried = UncurriedNFT.tryUncurry(puzzleReveal);
      if (nftUncurried != null) {
        return SpendType.nft;
      }

      final args = uncurried.arguments;

      final uncurriedDid = didPuzzles.uncurryInnerpuz(args[1]);
      if (uncurriedDid != null) {
        return SpendType.did;
      }
    }
    return SpendType.unknown;
    //throw UnimplementedError('Unimplemented spend type');
  }

  Program toProgram() {
    Bytes coinBytes = coin.toBytes();
    if (coin is CatCoin) {
      coinBytes = (coin as CatCoin).toCoinPrototype().toBytes();
    }
    return Program.list([
      Program.fromBytes(coinBytes),
      Program.fromBytes(puzzleReveal.serialize()),
      Program.fromBytes(solution.serialize()),
    ]);
  }

  static CoinSpend fromProgram(Program program) {
    final args = program.toList();
    final coin = CoinPrototype.fromBytes(args[0].atom);
    final puzzleReveal = Program.deserialize(args[1].atom);
    final solution = Program.deserialize(args[2].atom);
    return CoinSpend(
        coin: coin, puzzleReveal: puzzleReveal, solution: solution);
  }

  @override
  String toString() =>
      'CoinSpend(coin: $coin, puzzleReveal: $puzzleReveal, solution: $solution)';
}

enum SpendType {
  unknown("unknown"),
  standard('xch'),
  cat1("cat1"),
  cat2("cat"),
  nft("nft"),
  did('did');

  const SpendType(this.value);
  final String value;
}
