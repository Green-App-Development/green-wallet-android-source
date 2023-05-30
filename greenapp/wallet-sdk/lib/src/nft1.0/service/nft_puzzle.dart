import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/nft1.0/index.dart';
import 'package:chia_crypto_utils/src/standard/puzzles/p2_delegated_puzzle_or_hidden_puzzle/p2_delegated_puzzle_or_hidden_puzzle.clvm.hex.dart';
import 'package:tuple/tuple.dart';

Program _parseValue(dynamic value) {
  Program? valueP;
  if (value is int) {
    valueP = Program.fromInt(value);
  } else if (value is String) {
    valueP = Program.fromString(value);
  } else if (value is Bytes) {
    valueP = Program.fromBytes(value);
  } else if (value is List) {
    final listValues = <Program>[];
    for (final item in value) {
      listValues.add(_parseValue(item));
    }
    valueP = Program.list(listValues);
  }
  if (valueP == null) {
    throw Exception("Can convert to metadata ${value}");
  }
  return valueP;
}

class NftService {
  static Program createNftLayerPuzzleWithCurryParams({
    required Program metadata,
    required Bytes metadataUpdaterHash,
    required Program innerPuzzle,
  }) {
    return puzzleForMetadataLayer(
      metadata: metadata,
      metadataUpdaterHash: metadataUpdaterHash,
      innerPuzzle: innerPuzzle,
    );
  }

  static Program createFullPuzzleWithNftPuzzle({
    required Bytes singletonId,
    required Program innerPuzzle,
  }) {
    return puzzleForSingletonV1_1(
      singletonId,
      innerPuzzle,
      launcherHash: LAUNCHER_PUZZLE_HASH,
    );
  }

  static Program createFullPuzzle(
      {required Bytes singletonId,
      required Program metadata,
      required Bytes metadataUpdaterHash,
      required Program innerPuzzle}) {
    final singletonStruct = Program.cons(
      Program.fromBytes(SINGLETON_TOP_LAYER_MOD_V1_1_HASH),
      Program.cons(
        Program.fromBytes(singletonId),
        Program.fromBytes(
          LAUNCHER_PUZZLE_HASH,
        ),
      ),
    );

    final sinletonInnerPuzzle = createNftLayerPuzzleWithCurryParams(
      metadata: metadata,
      metadataUpdaterHash: metadataUpdaterHash,
      innerPuzzle: innerPuzzle,
    );

    return SINGLETON_TOP_LAYER_MOD_v1_1.curry([singletonStruct, sinletonInnerPuzzle]);
  }

  static NFTInfo getNftInfoFromPuzzle(NFTCoinInfo nftCoinInfo) {
    final uncurriedNft = UncurriedNFT.uncurry(nftCoinInfo.fullPuzzle);
    return NFTInfo.fromUncurried(
      uncurriedNFT: uncurriedNft,
      currentCoin: nftCoinInfo.coin,
      mintHeight: nftCoinInfo.mintHeight,
    );
  }

  ///  Convert the metadata dict to a Chialisp program
  static Program metadataToProgram(Map<Bytes, dynamic> metadata) {
    final programList = <Program>[];
    metadata.forEach((key, value) {
      Program? valueP = _parseValue(value);

      programList.add(Program.cons(
        Program.fromBytes(key),
        valueP,
      ));
    });
    return Program.list(programList);
  }

  /// Convert a program to a metadata dict, [program] Chialisp
  /// program contains the metadata return: Metadata dict
  static Map<Bytes, dynamic> programToMetadata(Program program) {
    final metadata = <Bytes, dynamic>{};
    for (var con in program.toList()) {
      metadata[con.first().atom] = con.rest().atom;
    }
    return metadata;
  }

  /// Prepend a value to a list in the metadata
  static void prependValue(
      {required Map<Bytes, dynamic> metadata, required Program value, required Bytes key}) {
    if (value == Program.list([])) return;

    if ((metadata[key] as List?)?.isEmpty ?? true) {
      metadata[key] = [value.atom];
    } else {
      (metadata[key] as List).insert(0, value.atom);
    }
  }

  /// Apply conditions of metadata updater to the previous metadata
  static Program updateMetadata({required Program metadata, required Program updateCondition}) {
    final newMetadata = programToMetadata(metadata);
    final uri = updateCondition.rest().rest().first();
    prependValue(metadata: newMetadata, value: uri.first(), key: uri.rest().atom);
    return metadataToProgram(newMetadata);
  }

  static Program constructOwnershipLayer(
          {required Bytes? currentOwner,
          required Program transferProgram,
          required Program innerPuzzle}) =>
      puzzleForOwnershipLayer(
        currentOwner: currentOwner,
        transferProgram: transferProgram,
        innerPuzzle: innerPuzzle,
      );

  static Program createOwnwershipLayerPuzzle({
    required Bytes nftId,
    required Bytes? didId,
    required Program p2Puzzle,
    required int percentage,
    Puzzlehash? royaltyPuzzleHash,
  }) {
    final singletonStruct = Program.cons(
      Program.fromBytes(SINGLETON_TOP_LAYER_MOD_V1_1_HASH),
      Program.cons(
        Program.fromBytes(nftId),
        Program.fromBytes(LAUNCHER_PUZZLE_HASH),
      ),
    );
    if (royaltyPuzzleHash == null) {
      royaltyPuzzleHash = p2Puzzle.hash();
    }

    final transferProgram = NFT_TRANSFER_PROGRAM_DEFAULT.curry([
      singletonStruct,
      Program.fromBytes(royaltyPuzzleHash),
      Program.fromInt(percentage),
    ]);

    final nftInnerPuzzle = p2Puzzle;
    final nftOwnershipLayerPuzzle = constructOwnershipLayer(
      currentOwner: didId,
      transferProgram: transferProgram,
      innerPuzzle: nftInnerPuzzle,
    );
    return nftOwnershipLayerPuzzle;
  }

  static Program createOwnershipLayerTransferSolution({
    required Bytes newDid,
    required Puzzlehash newDidInnerHash,
    required List<List<int>> tradePricesList,
    required Puzzlehash newPuzzleHash,
  }) {
    final tradePricesListP = Program.list(
      tradePricesList.map((root) {
        final innerList = root.map((e) => Program.fromInt(e)).toList();
        return Program.list(innerList);
      }).toList(),
    );
    final conditionList = Program.list([
      Program.list(
        [
          Program.fromInt(51),
          Program.fromBytes(newPuzzleHash),
          Program.fromInt(1),
          Program.list([
            Program.fromBytes(newPuzzleHash),
          ]),
        ],
      ),
      Program.list([
        Program.fromInt(-10),
        Program.fromBytes(newDid),
        tradePricesListP,
        Program.fromBytes(newDidInnerHash),
      ])
    ]);

    final solution = Program.list(
      [
        Program.list(
          [
            solutionForConditions(conditionList),
          ],
        ),
      ],
    );
    return solution;
  }

  //get_metadata_and_phs
  Tuple2<Program, Bytes> getMetadataAndPhs(UncurriedNFT unft, Program solution) {
    final conditions = unft.p2Puzzle.run(unft.getInnermostSolution(solution)).program;
    Program metadata = unft.metadata;
    Bytes? puzzlehashForDerivation;
    for (var condition in conditions.toList()) {
      final conditionList = condition.toList();
      if (conditionList.length < 2) {
        // invalid condition
        continue;
      }
      final conditionCode = condition.first().toInt();
      if (conditionCode == -24) {
        metadata = updateMetadata(metadata: metadata, updateCondition: condition);
      } else if (conditionCode == 51 && condition.rest().rest().first().toInt() == 1) {
        //destination puzhash
        if (puzzlehashForDerivation != null) {
          // ignore duplicated create coin conditions
          continue;
        }
        final memo = conditionList.last.first().atom;
        puzzlehashForDerivation = memo;
        print("Got back puzhash from solution: ${puzzlehashForDerivation.toHex()}");
      }
    }
    if (puzzlehashForDerivation == null) {
      throw Exception("puzhash_for_derivation can't be null");
    }
    return Tuple2(metadata, puzzlehashForDerivation);
  }

  Program recurryNftPuzzle(
      {required UncurriedNFT unft, required Program solution, required Program newInnerPuzzle}) {
    print("Generating NFT puzzle with ownership support: ${solution.toSource()}");

    final conditions = unft.p2Puzzle.run(unft.getInnermostSolution(solution)).program;
    Bytes? newDidId = unft.ownerDid;
    Bytes? newPuzhash;

    for (var condition in conditions.toList()) {
      if (condition.first().toInt() == -10) {
        // this is the change owner magic condition
        newDidId = condition.filterAt("rf").atom;
      } else if (condition.first().toInt() == 51) {
        newPuzhash = condition.filterAt("rf").atom;
      }
    }
    print(
      "Found NFT puzzle details: ${newDidId?.toHexWithPrefix()} ${newPuzhash?.toHexWithPrefix()}",
    );

    if (unft.transferProgram == null) {
      throw Exception("TransferProgram in uncurriedNFT can't be null");
    }

    final newOwnershipPuzzle = constructOwnershipLayer(
      currentOwner: newDidId,
      transferProgram: unft.transferProgram!,
      innerPuzzle: newInnerPuzzle,
    );

    return newOwnershipPuzzle;
  }

  Bytes? getnewOwnerDid({required UncurriedNFT unft, required Program solution}) {
    final conditions = unft.p2Puzzle.run(unft.getInnermostSolution(solution)).program;
    Bytes? newDidId = unft.ownerDid;

    for (var condition in conditions.toList()) {
      if (condition.first().toInt() == -10) {
        // this is the change owner magic condition

        newDidId = condition.filterAt("rf").atom;
      }
    }
    return newDidId;
  }
}
