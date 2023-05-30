import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/core/models/outer_puzzle.dart';
import 'package:quiver/iterables.dart';

class CATOuterPuzzle extends OuterPuzzle {
  @override
  Program constructPuzzle({required PuzzleInfo constructor, required Program innerPuzzle}) {
    if (constructor.also != null) {
      innerPuzzle = OuterPuzzleDriver.constructPuzzle(
          constructor: constructor.also!, innerPuzzle: innerPuzzle);
    }
    return CatWalletService.makeCatPuzzle(createAssetId(constructor: constructor), innerPuzzle);
  }

  @override
  Puzzlehash createAssetId({required PuzzleInfo constructor}) {
    final tail = constructor.info["tail"]!;
    if (tail is Bytes) {
      return Puzzlehash(tail);
    } else if (tail is Puzzlehash) {
      return tail;
    }
    return Puzzlehash.fromHex(tail);
  }

  @override
  PuzzleInfo? matchPuzzle(Program puzzle) {
    final matched = CatWalletService.matchCatPuzzle(puzzle);
    if (matched != null) {
      final Map<String, dynamic> constructorDict = {
        "type": AssetType.CAT,
        "tail": matched.assetId.toHexWithPrefix(),
      };
      final next = matchPuzzle(matched.innerPuzzle);
      if (next != null) {
        constructorDict["also"] = next.info;
      }
      return PuzzleInfo(constructorDict);
    }
    return null;
  }

  @override
  Program solvePuzzle(
      {required PuzzleInfo constructor,
      required Solver solver,
      required Program innerPuzzle,
      required Program innerSolution}) {
    //final Bytes tailHash = solver.info["tail"];
    final spendableCatsList = <SpendableCat>[];

    CoinPrototype? targetCoin;
    final siblingsIter = (solver["siblings"] as Program).toList();
    final siblingSpends = (solver["sibling_spends"] as Program).toList();
    final siblingPuzzles = (solver["sibling_puzzles"] as Program).toList();
    final siblingSolutions = (solver["sibling_solutions"] as Program).toList();
    final zipped = zip([
      siblingsIter,
      siblingSpends,
      siblingPuzzles,
      siblingSolutions,
    ]);

    final coinProgram = Program.fromBytes(solver["coin"]);
    final _parentSpendProgram = Program.deserialize(solver["parent_spend"]);

    final base = [
      coinProgram,
      _parentSpendProgram,
      innerPuzzle,
      innerSolution,
    ];
    final workIterable = zipped.toList()..add(base);
    for (var item in workIterable) {
      final coinProg = item[0];
      final spendProg = item[1];
      Program puzzle = item[2];
      Program solution = item[3];

      final coinBytes = coinProg.atom;

      final coin = CoinPrototype.fromBytes(coinBytes);
      if (coinBytes == solver["coin"]) {
        targetCoin = coin;
      }
      final parentSpend = CoinSpend.fromProgram(spendProg);

      // final parentCoin = parentSpend.coin;
      if (constructor.also != null) {
        puzzle = OuterPuzzleDriver.constructPuzzle(
          constructor: constructor.also!,
          innerPuzzle: puzzle,
        );
        solution = OuterPuzzleDriver.solvePuzzle(
            constructor: constructor.also!,
            solver: solver,
            innerPuzzle: innerPuzzle,
            innerSolution: innerSolution);
      }
      final matched = CatWalletService.matchCatPuzzle(parentSpend.puzzleReveal);
      assert(matched != null, "Cat puzzle can be match");
      //final parentInnerPuzzle = matched!.innerPuzzle;
      final catCoin = CatCoin(parentCoinSpend: parentSpend, coin: coin);

      // the [lineage_proof] is calc in the constructor of the [SpendableCat]
      final spendableCat = SpendableCat(
        coin: catCoin,
        innerPuzzle: puzzle,
        innerSolution: solution,
      );
      spendableCatsList.add(spendableCat);
    }

    return CatWalletService.makeUnsignedSpendBundleForSpendableCats(spendableCatsList)
        .coinSpends
        .where((element) => element.coin == targetCoin)
        .first
        .solution;
  }

  @override
  Program? getInnerPuzzle({required PuzzleInfo constructor, required Program puzzleReveal}) {
    final matched = CatWalletService.matchCatPuzzle(puzzleReveal);
    if (matched != null) {
      final innerPuzzle = matched.innerPuzzle;
      if (constructor.also != null) {
        final deopInnerPuzzle = OuterPuzzleDriver.getInnerPuzzle(
          constructor: constructor.also!,
          puzzleReveal: puzzleReveal,
        );
        return deopInnerPuzzle;
      }
      return innerPuzzle;
    } else {
      throw Exception("This driver is not for the specified puzzle reveal");
    }
  }

  @override
  Program? getInnerSolution({required PuzzleInfo constructor, required Program solution}) {
    final myInnerSolution = solution.first();
    if (constructor.also != null) {
      final deepInnerSolution = OuterPuzzleDriver.getInnerSolution(
        constructor: constructor.also!,
        solution: solution,
      );
      return deepInnerSolution;
    }
    return myInnerSolution;
  }
}
