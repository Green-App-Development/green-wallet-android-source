import '../../../chia_crypto_utils.dart';

export '../puzzles/nft_ownership_layer/nft_ownership_layer.clvm.hex.dart';
export '../puzzles/nft_state_layer/nft_state_layer.clvm.hex.dart';
export '../puzzles/singleton_top_layer_v1_1/singleton_top_layer_v1_1.clvm.hex.dart';

/// A simple solution for uncurry NFT puzzle.

class UncurriedNFT {
  /// Initial the class with a full NFT puzzle, it will do a deep uncurry.
  /// This is the only place you need to change after modified the Chialisp curried parameters.

  final Program nftModHash;

  /// NFT module hash

  final Program nftStateLayer;

  /// NFT state layer puzzle
  final Program singletonStruct;

  ///  Singleton struct
  /// [singleton_mod_hash, singleton_launcher_id, launcher_puzhash]
  final Program singletonModHash;
  final Program singletonLauncherId;
  final Program launcherPuzhash;

  /// Metadata updater puzzle hash
  final Program metadataUpdaterHash;

  /// NFT metadata
  /// [("u", data_uris), ("h", data_hash)]
  final Program metadata;

  /// Puzzle hash of the transfer program
  final Program? transferProgram;

  //final Program settlementModHash;
  //final Program catModHash;

  ///  NFT metadata
  /// [("u", data_uris), ("h", data_hash)]
  final Program dataUris;
  final Program dataHash;
  final Program metaUris;
  final Program metaHash;
  final Program licenseUris;
  final Program licenseHash;
  final Program seriesNumber;
  final Program seriesTotal;

  ///  NFT state layer inner puzzle
  final Program innerPuzzle;

  /// p2 puzzle of the owner, either for ownership layer or standard

  final Program p2Puzzle;

  Puzzlehash get p2PuzzleHash => p2Puzzle.hash();

  /// Owner's DID
  final Bytes? ownerDid;

  /// If the inner puzzle support the DID
  final bool supportDid;

  /// Puzzle hash of the ownership layer inner puzzle
  //final Bytes? nftInnerPuzzleHash;

  /// Curried parameters of the transfer program
  /// [royalty_address, trade_price_percentage, settlement_mod_hash, cat_mod_hash]
  final Program? transferProgramCurryParams;

  final Puzzlehash? royaltyPuzzlehash;
  final int? tradePricePercentage;

  UncurriedNFT._({
    required this.metaUris,
    required this.metaHash,
    required this.licenseUris,
    required this.licenseHash,
    required this.seriesNumber,
    required this.seriesTotal,
    required this.p2Puzzle,
    required this.supportDid,
    //required this.nftInnerPuzzleHash,
    required this.transferProgram,
    required this.nftModHash,
    required this.nftStateLayer,
    required this.singletonStruct,
    required this.singletonModHash,
    required this.singletonLauncherId,
    required this.launcherPuzhash,
    required this.ownerDid,
    required this.metadataUpdaterHash,
    required this.transferProgramCurryParams,
    required this.royaltyPuzzlehash,
    required this.tradePricePercentage,
    required this.metadata,
    required this.dataUris,
    required this.dataHash,
    required this.innerPuzzle,
  });

  static UncurriedNFT? tryUncurry(Program puzzle) {
    try {
      return UncurriedNFT.uncurry(puzzle);
    } catch (e) {
      print(e);
      return null;
    }
  }

  static UncurriedNFT uncurry(Program puzzle) {
    late Program singletonStruct;
    late Program nftStateLayer;
    late Program sinletonModHash;
    late Program singletonLauncherId;
    late Program launcherPuzzhash;

    Program dataUris = Program.list([]);
    Program dataHash = Program.fromInt(0);
    Program metaUris = Program.list([]);
    Program metaHash = Program.fromInt(0);
    Program licenseUris = Program.list([]);
    Program licenseHash = Program.fromInt(0);
    Program seriesNumber = Program.fromInt(1);
    Program seriesTotal = Program.fromInt(0);

    final uncurried = puzzle.uncurry();
    final mod = uncurried.program;
    final curried_args = uncurried.arguments;
    // print(mod.serializeHex());
    if (mod.hash() != SINGLETON_TOP_LAYER_MOD_V1_1_HASH) {
      throw ArgumentError("Cannot uncurry NFT puzzle, failed on singleton top layer: Mod ${mod}");
    }

    try {
      singletonStruct = curried_args[0];
      nftStateLayer = curried_args[1];

      sinletonModHash = singletonStruct.first();
      singletonLauncherId = singletonStruct.rest().first();
      launcherPuzzhash = singletonStruct.rest().rest();
    } catch (e) {
      throw ArgumentError("Cannot uncurry singleton top layer: Args ${curried_args}");
    }

    final uncurred = curried_args[1].uncurry();
    final nftMod = uncurred.program;
    final nftArgs = uncurred.arguments;

    if (nftMod.toSource() != nftStateLayerProgram.toSource()) {
      throw ArgumentError("Cannot uncurry NFT puzzle, failed on NFT state layer: Mod ${mod}");
    }
    try {
      final nftModHash = nftArgs[0];
      final metadata = nftArgs[1];
      final metadataUpdaterHash = nftArgs[2];
      final innerPuzzle = nftArgs[3];
      final metadataList = metadata.toList();

      for (var kvPair in metadataList) {
        if (bytesEqual(kvPair.first().atom, 'u'.toBytes())) {
          dataUris = kvPair.rest();
        }
        if (bytesEqual(kvPair.first().atom, 'h'.toBytes())) {
          dataHash = kvPair.rest();
        }
        if (bytesEqual(kvPair.first().atom, 'mu'.toBytes())) {
          metaUris = kvPair.rest();
        }
        if (bytesEqual(kvPair.first().atom, 'mmhu'.toBytes())) {
          metaHash = kvPair.rest();
        }
        if (bytesEqual(kvPair.first().atom, 'lu'.toBytes())) {
          licenseUris = kvPair.rest();
        }
        if (bytesEqual(kvPair.first().atom, 'lh'.toBytes())) {
          licenseHash = kvPair.rest();
        }
        if (bytesEqual(kvPair.first().atom, 'sn'.toBytes())) {
          seriesNumber = kvPair.rest();
        }
        if (bytesEqual(kvPair.first().atom, 'st'.toBytes())) {
          seriesTotal = kvPair.rest();
        }
      }

      Bytes? currentDid;
      Program? transferProgram;
      List<Program>? transferProgramArgs;
      late Program p2Puzzle;
      Puzzlehash? royaltyPuzzlehash;
      int? royaltyPercentage;
      Bytes? nftInnerPuzzleMod;

      final innerPuzzleUncurried = innerPuzzle.uncurry();
      final mod = innerPuzzleUncurried.program;
      final olArgs = innerPuzzleUncurried.arguments;

      bool supportsDid = false;

      if (mod.toSource() == nftOwnershipLayer.toSource()) {
        supportsDid = true;
        print("parsing ownership layer");
        final olArgsList = olArgs;
        final currentDidP = olArgsList[1];
        transferProgram = olArgsList[2];
        p2Puzzle = olArgsList[3];
        final uncurriedTransferProgram = transferProgram.uncurry();
        //final transferProgramMod = uncurriedTransferProgram.program;
        transferProgramArgs = uncurriedTransferProgram.arguments;
        final royaltyAddressP = transferProgramArgs[1];
        final royaltyPercentageP = transferProgramArgs[2];
        royaltyPercentage = royaltyPercentageP.toInt();
        royaltyPuzzlehash = Puzzlehash(royaltyAddressP.atom);
        currentDid = currentDidP.atom;
        if (currentDid.isEmpty) {
          currentDid = null;
        }
      } else {
        p2Puzzle = innerPuzzle;
      }

      return UncurriedNFT._(
        nftModHash: nftModHash,
        nftStateLayer: nftStateLayer,
        singletonStruct: singletonStruct,
        singletonModHash: sinletonModHash,
        singletonLauncherId: singletonLauncherId,
        launcherPuzhash: launcherPuzzhash,
        metadata: metadata,
        dataUris: dataUris,
        dataHash: dataHash,
        p2Puzzle: p2Puzzle,
        metaUris: metaUris,
        metaHash: metaHash,
        licenseUris: licenseUris,
        licenseHash: licenseHash,
        seriesNumber: seriesNumber,
        seriesTotal: seriesTotal,
        innerPuzzle: innerPuzzle,
        metadataUpdaterHash: metadataUpdaterHash,
        ownerDid: currentDid,
        supportDid: supportsDid,
        transferProgram: transferProgram,
        transferProgramCurryParams:
            transferProgramArgs != null ? Program.list(transferProgramArgs) : null,
        royaltyPuzzlehash: royaltyPuzzlehash,
        tradePricePercentage: royaltyPercentage,
        // nftInnerPuzzleHash: nftInnerPuzzleMod,
      );
    } catch (e) {
      print(e);
      throw Exception("Cannot uncurry NFT state layer: Args ${curried_args}");
    }
  }

  // get_innermost_solution
  Program getInnermostSolution(Program solution) {
    final stateLayerInnerSolution = solution.filterAt("rrff");
    if (supportDid) {
      return stateLayerInnerSolution.first();
    } else {
      return stateLayerInnerSolution;
    }
  }
}
