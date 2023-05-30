import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/core/models/outer_puzzle.dart';
import 'package:chia_crypto_utils/src/nft1.0/index.dart';
import 'package:chia_crypto_utils/src/nft1.0/models/deconstructed_transfer_program_puzzle.dart';

DeconstructedTransferProgramPuzzle? mathTransferProgramPuzzle(Program puzzle) {
  final uncurried = puzzle.uncurry();
  if (uncurried.program.hash() == TRANSFER_PROGRAM_MOD_HASH) {
    final nftArgs = uncurried.arguments;

    final singletonStruct = nftArgs[0];
    final royaltyPuzzleHash = nftArgs[1];
    final royaltyPercentage = nftArgs[2];

    return DeconstructedTransferProgramPuzzle(
      singletonStruct: singletonStruct,
      royaltyPercentage: royaltyPercentage.toInt(),
      royaltyAddressP: royaltyPuzzleHash,
    );
  }
  return null;
}

Program puzzleForTransferProgram(
    {required Bytes launcherId, required Puzzlehash royaltyPuzzleHash, required int percentage}) {
  final sinletonStruct = Program.cons(
    Program.fromBytes(SINGLETON_TOP_LAYER_MOD_V1_1_HASH),
    Program.cons(
      Program.fromBytes(launcherId),
      Program.fromBytes(SINGLETON_LAUNCHER_HASH),
    ),
  );
  return TRANSFER_PROGRAM_MOD.curry(
    [
      sinletonStruct,
      Program.fromBytes(royaltyPuzzleHash),
      Program.fromInt(percentage),
    ],
  );
}

Program solutionForTransferProgram(
    {required Program conditions,
    required Puzzlehash? currentowner,
    required Puzzlehash newDid,
    required Puzzlehash newDidInnerHash,
    required Program tradePricesList}) {
  Program currentOwnerP = Program.nil;
  if (currentowner != null) {
    currentOwnerP = Program.fromBytes(currentowner);
  }

  return Program.list([
    conditions,
    currentOwnerP,
    Program.list([
      Program.fromBytes(newDid),
      tradePricesList,
      Program.fromBytes(newDidInnerHash),
    ])
  ]);
}

class TransferProgramOuterPuzzle extends OuterPuzzle {
  @override
  Program constructPuzzle({required PuzzleInfo constructor, required Program innerPuzzle}) {
    return puzzleForTransferProgram(
      launcherId: Puzzlehash.fromHex(constructor["launcher_id"]),
      royaltyPuzzleHash: Puzzlehash.fromHex(constructor["royalty_address"]),
      percentage: constructor["royalty_percentage"] as int,
    );
  }

  @override
  Puzzlehash? createAssetId({required PuzzleInfo constructor}) {
    return null;
  }

  @override
  PuzzleInfo? matchPuzzle(Program puzzle) {
    final matched = mathTransferProgramPuzzle(puzzle);
    if (matched != null) {
      final constructorDict = <String, dynamic>{
        "type": AssetType.ROYALTY_TRANSFER_PROGRAM,
        "launcher_id": matched.singletonStruct.rest().first().atom.toHexWithPrefix(),
        "royalty_address": matched.royaltyAddressP.atom.toHexWithPrefix(),
        "royalty_percentage": matched.royaltyPercentage
      };

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
    return Program.nil;
  }

  @override
  Program? getInnerPuzzle({required PuzzleInfo constructor, required Program puzzleReveal}) {
    return null;
  }

  @override
  Program? getInnerSolution({required PuzzleInfo constructor, required Program solution}) {
    return null;
  }
}
