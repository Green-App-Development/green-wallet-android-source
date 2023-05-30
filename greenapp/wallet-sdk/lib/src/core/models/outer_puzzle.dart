import 'package:chia_crypto_utils/chia_crypto_utils.dart';

import '../../cat/service/cat_outer_puzzle.dart';
import '../../nft1.0/index.dart';

class AssetType {
  AssetType._();
  static String get CAT => 'CAT';
  static String get SINGLETON => 'singleton';
  static String get METADATA => 'metadata';
  static String get OWNERSHIP => 'ownership';
  static String get ROYALTY_TRANSFER_PROGRAM => 'royalty transfer program';
}

class OuterPuzzleDriver {
  OuterPuzzleDriver._();
  static final Map<String, OuterPuzzle> _driverLookup = {
    AssetType.CAT: CATOuterPuzzle(),
    AssetType.SINGLETON: SingletonOuterPuzzle(),
    AssetType.METADATA: MetadataOurterPuzzle(),
    AssetType.OWNERSHIP: OwnershipOuterPuzzle(),
    AssetType.ROYALTY_TRANSFER_PROGRAM: TransferProgramOuterPuzzle()
  };

  static Program constructPuzzle({
    required PuzzleInfo constructor,
    required Program innerPuzzle,
  }) {
    final driver = _driverLookup[constructor.info['type']];
    if (driver == null) throw Exception('Unknown asset type: ${constructor.info["type"]}');

    return driver.constructPuzzle(
      constructor: constructor,
      innerPuzzle: innerPuzzle,
    );
  }

  static Bytes? createAssetId(PuzzleInfo constructor) {
    final driver = _driverLookup[constructor.info['type']];
    if (driver == null) throw Exception('Unknown asset type: ${constructor.info["type"]}');
    return driver.createAssetId(constructor: constructor);
  }

  static PuzzleInfo? matchPuzzle(Program puzzle) {
    for (var driver in _driverLookup.values) {
      final matched = driver.matchPuzzle(puzzle);
      if (matched != null) {
        return matched;
      }
    }
    return null;
  }

  static Program solvePuzzle({
    required PuzzleInfo constructor,
    required Solver solver,
    required Program innerPuzzle,
    required Program innerSolution,
  }) {
    final driver = _driverLookup[constructor.info['type']];
    if (driver == null) throw Exception('Unknown asset type: ${constructor.info["type"]}');

    return driver.solvePuzzle(
      constructor: constructor,
      solver: solver,
      innerPuzzle: innerPuzzle,
      innerSolution: innerSolution,
    );
  }

  static Program? getInnerPuzzle({
    required PuzzleInfo constructor,
    required Program puzzleReveal,
  }) {
    final driver = _driverLookup[constructor.info['type']];
    if (driver == null) throw Exception('Unknown asset type: ${constructor.info["type"]}');
    return driver.getInnerPuzzle(constructor: constructor, puzzleReveal: puzzleReveal);
  }

  static Program? getInnerSolution({
    required PuzzleInfo constructor,
    required Program solution,
  }) {
    final driver = _driverLookup[constructor.info['type']];
    if (driver == null) throw Exception('Unknown asset type: ${constructor.info["type"]}');
    return driver.getInnerSolution(constructor: constructor, solution: solution);
  }
}

abstract class OuterPuzzle {
  Program constructPuzzle({
    required PuzzleInfo constructor,
    required Program innerPuzzle,
  });
  Puzzlehash? createAssetId({
    required PuzzleInfo constructor,
  });
  PuzzleInfo? matchPuzzle(Program puzzle);
  Program solvePuzzle({
    required PuzzleInfo constructor,
    required Solver solver,
    required Program innerPuzzle,
    required Program innerSolution,
  });
  Program? getInnerPuzzle({
    required PuzzleInfo constructor,
    required Program puzzleReveal,
  });
  Program? getInnerSolution({
    required PuzzleInfo constructor,
    required Program solution,
  });
}
