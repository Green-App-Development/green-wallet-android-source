import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/core/models/outer_puzzle.dart';
import 'package:chia_crypto_utils/src/nft1.0/index.dart';
import 'package:chia_crypto_utils/src/nft1.0/models/deconstructed_singleton_puzzle.dart';

Program puzzleForSingletonV1_1(
  Bytes launcherId,
  Program innerPuzzle, {
  Bytes? launcherHash,
}) {
  return SINGLETON_TOP_LAYER_MOD_v1_1.curry([
    Program.cons(
      Program.fromBytes(SINGLETON_TOP_LAYER_MOD_V1_1_HASH),
      Program.cons(
        Program.fromBytes(launcherId),
        Program.fromBytes(
          launcherHash ?? LAUNCHER_PUZZLE_HASH,
        ),
      ),
    ),
    innerPuzzle
  ]);
}

DeconstructedSingletonPuzzle? mathSingletonPuzzle(Program puzzle) {
  final uncurried = puzzle.uncurry();
  if (uncurried.program.hash() == SINGLETON_TOP_LAYER_MOD_V1_1_HASH) {
    final nftArgs = uncurried.arguments;

    final singletonStruct = nftArgs[0];
    final sinletonModHash = singletonStruct.first();
    final singletonLauncherId = singletonStruct.rest().first();
    final launcherPuzzhash = singletonStruct.rest().rest();

    return DeconstructedSingletonPuzzle(
        innerPuzzle: nftArgs[1],
        launcherPuzzhash: Puzzlehash(launcherPuzzhash.atom),
        singletonLauncherId: Puzzlehash(singletonLauncherId.atom),
        sinletonModHash: Puzzlehash(sinletonModHash.atom));
  }
  return null;
}

Program solutionForSingleton({
  required LineageProof lineageProof,
  required int amount,
  required Program innerSolution,
}) {
  Program parentInfo;

  if (lineageProof.innerPuzzleHash == null) {
    parentInfo = Program.list([
      Program.fromBytes(
        lineageProof.parentName!,
      ),
      Program.fromInt(lineageProof.amount!)
    ]);
  } else {
    parentInfo = Program.list([
      Program.fromBytes(
        lineageProof.parentName!,
      ),
      Program.fromBytes(
        lineageProof.innerPuzzleHash!,
      ),
      Program.fromInt(lineageProof.amount!)
    ]);
  }

  return Program.list([
    parentInfo,
    Program.fromInt(amount),
    innerSolution,
  ]);
}

class SingletonOuterPuzzle extends OuterPuzzle {
  @override
  Program constructPuzzle({required PuzzleInfo constructor, required Program innerPuzzle}) {
    if (constructor.also != null) {
      innerPuzzle = OuterPuzzleDriver.constructPuzzle(
          constructor: constructor.also!, innerPuzzle: innerPuzzle);
    }

    Bytes launcherHash = SINGLETON_LAUNCHER_HASH;
    final Bytes launcherId = Bytes.fromHex(constructor["launcher_id"]);
    if (constructor["launcher_ph"] != null) {
      launcherHash = Bytes.fromHex(constructor["launcher_ph"]);
    }
    return puzzleForSingletonV1_1(
      launcherId,
      innerPuzzle,
      launcherHash: launcherHash,
    );
  }

  @override
  Puzzlehash? createAssetId({required PuzzleInfo constructor}) {
    return Puzzlehash.fromHex(constructor["launcher_id"]);
  }

  @override
  PuzzleInfo? matchPuzzle(Program puzzle) {
    final matched = mathSingletonPuzzle(puzzle);
    if (matched != null) {
      final Map<String, dynamic> constructorDict = {
        "type": AssetType.SINGLETON,
        "launcher_id": matched.singletonLauncherId.toHexWithPrefix(),
        "launcher_ph": matched.launcherPuzzhash.toHexWithPrefix(),
      };
      final next = OuterPuzzleDriver.matchPuzzle(matched.innerPuzzle);
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
    Bytes coinBytes;

    if (solver["coin"] is Bytes) {
      coinBytes = solver["coin"];
    } else {
      coinBytes = Bytes.fromHex(solver["coin"] as String);
    }

    final coin = CoinPrototype.fromBytes(coinBytes);
    var parentSpend = CoinSpend.fromProgram(Program.deserialize(solver["parent_spend"]));

    final parentCoin = parentSpend.coin;

    if (constructor.also != null) {
      innerSolution = OuterPuzzleDriver.solvePuzzle(
        constructor: constructor.also!,
        solver: solver,
        innerPuzzle: innerPuzzle,
        innerSolution: innerSolution,
      );
    }
    final matched = mathSingletonPuzzle(parentSpend.puzzleReveal);
    if (matched == null) {
      throw Exception("Math fail SingletonPuzzle");
    }
    final parentInnerPuzzle = matched.innerPuzzle;

    return solutionForSingleton(
      lineageProof: LineageProof(
          parentName: Puzzlehash(parentCoin.parentCoinInfo),
          innerPuzzleHash: parentInnerPuzzle.hash(),
          amount: parentCoin.amount),
      amount: coin.amount,
      innerSolution: innerSolution,
    );
  }

  @override
  Program? getInnerPuzzle({required PuzzleInfo constructor, required Program puzzleReveal}) {
    final matched = mathSingletonPuzzle(puzzleReveal);
    if (matched != null) {
      final innerPuzzle = matched.innerPuzzle;
      if (constructor.also != null) {
        final deepInnerPuzzle = OuterPuzzleDriver.getInnerPuzzle(
          constructor: constructor.also!,
          puzzleReveal: innerPuzzle,
        );
        return deepInnerPuzzle;
      }
      return innerPuzzle;
    } else {
      throw Exception("This driver is not for the specified puzzle reveal");
    }
  }

  @override
  Program? getInnerSolution({required PuzzleInfo constructor, required Program solution}) {
    final myInnerSolution = solution.filterAt("rrf");
    if (constructor.also != null) {
      final deepInnerSolution = OuterPuzzleDriver.getInnerSolution(
        constructor: constructor.also!,
        solution: myInnerSolution,
      );
      return deepInnerSolution;
    }
    return myInnerSolution;
  }
}
