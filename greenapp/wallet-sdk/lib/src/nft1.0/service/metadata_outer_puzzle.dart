import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/nft1.0/index.dart';

DeconstructedUpdateMetadataPuzzle? mathMetadataLayerPuzzle(Program puzzle) {
  final uncurried = puzzle.uncurry();
  if (uncurried.program == NFT_STATE_LAYER_MOD) {
    final nftArgs = uncurried.arguments;

    final metadata = nftArgs[1];
    final metadataUpdaterHash = nftArgs[2];
    final innerPuzzle = nftArgs[3];

    return DeconstructedUpdateMetadataPuzzle(
      metadata: metadata,
      metadataUpdaterHash: metadataUpdaterHash,
      innerPuzzle: innerPuzzle,
    );
  }
  return null;
}

Program puzzleForMetadataLayer(
    {required Program metadata, required Bytes metadataUpdaterHash, required Program innerPuzzle}) {
  return NFT_STATE_LAYER_MOD.curry([
    Program.fromBytes(NFT_STATE_LAYER_MOD_HASH),
    metadata,
    Program.fromBytes(metadataUpdaterHash),
    innerPuzzle
  ]);
}

Program solutionForMetadataLayer({required int amount, required Program innerSolution}) {
  return Program.list([
    innerSolution,
    Program.fromInt(amount),
  ]);
}

class MetadataOurterPuzzle extends OuterPuzzle {
  @override
  Program constructPuzzle({required PuzzleInfo constructor, required Program innerPuzzle}) {
    if (constructor.also != null) {
      innerPuzzle = OuterPuzzleDriver.constructPuzzle(
        constructor: constructor.also!,
        innerPuzzle: innerPuzzle,
      );
    }
    Program metadata;
    if (constructor["metadata"] is Program) {
      metadata = constructor["metadata"];
    } else {
      metadata = Program.parse(constructor["metadata"]);
    }
    Bytes updaterHash;
    if (constructor["updater_hash"] is Bytes) {
      updaterHash = constructor["updater_hash"];
    } else {
      updaterHash = Bytes.fromHex(constructor["updater_hash"] as String);
    }

    return puzzleForMetadataLayer(
      metadata: metadata,
      metadataUpdaterHash: updaterHash,
      innerPuzzle: innerPuzzle,
    );
  }

  @override
  Puzzlehash createAssetId({required PuzzleInfo constructor}) {
    final updaterHash = constructor["updater_hash"]!;
    if (updaterHash is Bytes) {
      return Puzzlehash(updaterHash);
    } else if (updaterHash is Puzzlehash) {
      return updaterHash;
    }
    return Puzzlehash.fromHex(updaterHash);
  }

  @override
  PuzzleInfo? matchPuzzle(Program puzzle) {
    final matched = mathMetadataLayerPuzzle(puzzle);
    if (matched != null) {
      final Map<String, dynamic> constructorDict = {
        "type": AssetType.METADATA,
        "metadata": matched.metadata.toSource(),
        "updater_hash": matched.metadataUpdaterHash.toHexWithPrefix(),
      };
      final innerPuzzle = matched.innerPuzzle;
      final next = OuterPuzzleDriver.matchPuzzle(innerPuzzle);
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

    if (constructor.also != null) {
      innerSolution = OuterPuzzleDriver.solvePuzzle(
          constructor: constructor.also!,
          solver: solver,
          innerPuzzle: innerPuzzle,
          innerSolution: innerSolution);
    }

    return solutionForMetadataLayer(amount: coin.amount, innerSolution: innerSolution);
  }

  @override
  Program? getInnerPuzzle({required PuzzleInfo constructor, required Program puzzleReveal}) {
    final matched = mathMetadataLayerPuzzle(puzzleReveal);
    if (matched != null) {
      final innerPuzzle = matched.innerPuzzle;
      if (constructor.also != null) {
        final deopInnerPuzzle = OuterPuzzleDriver.getInnerPuzzle(
          constructor: constructor.also!,
          puzzleReveal: innerPuzzle,
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
        solution: myInnerSolution,
      );
      return deepInnerSolution;
    }
    return myInnerSolution;
  }
}
