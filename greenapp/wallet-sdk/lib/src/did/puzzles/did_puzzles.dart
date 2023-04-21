import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/did/puzzles/did_innerpuzzle/did_innerpuz.clvm.hex.dart';
import 'package:chia_crypto_utils/src/nft1.0/index.dart';
import 'package:tuple/tuple.dart';

import '../../utils/serialization.dart';

/// Create DID inner puzzle
/// [p2Puzzle] Standard P2 puzzle
///[recoveryList] A list of DIDs used for the recovery
///[numOfBackupIdsNeeded] Need how many DIDs for the recovery
///[launcherId] ID of the launch coin
///[metadata] DID customized metadata
///
///Returns  DID inner puzzle [Program]
Program createDidInnerpuz(
    {required Program p2Puzzle,
    required List<Bytes> recoveryList,
    required int numOfBackupIdsNeeded,
    required Bytes launcherId,
    Puzzlehash? recoveryListHash,
    Program? metadata}) {
  if (metadata == null) {
    metadata = Program.fromBytes(Bytes.empty);
  }

  Puzzlehash backupIdsHash =
      Program.list(recoveryList.map((e) => Program.fromBytes(e)).toList()).hash();
  if (recoveryListHash != null) {
    backupIdsHash = recoveryListHash;
  }
  final sinletonStruct = Program.cons(
    Program.fromBytes(SINGLETON_TOP_LAYER_MOD_V1_1_HASH),
    Program.cons(
      Program.fromBytes(launcherId),
      Program.fromBytes(LAUNCHER_PUZZLE_HASH),
    ),
  );

  return DID_INNERPUZ_MOD.curry([
    p2Puzzle,
    Program.fromBytes(backupIdsHash),
    Program.fromInt(numOfBackupIdsNeeded),
    sinletonStruct,
    metadata
  ]);
}

/// Create a full puzzle of DID
///   innerpuz: DID inner puzzle
/// launcher_id:
///
/// Return DID full puzzle
Program createDidFullpuz(Program innerpuz, Bytes launcherId) {
  final mod_hash = SINGLETON_TOP_LAYER_MOD_V1_1_HASH;
  final sinletonStruct = Program.cons(
    Program.fromBytes(mod_hash),
    Program.cons(
      Program.fromBytes(launcherId),
      Program.fromBytes(LAUNCHER_PUZZLE_HASH),
    ),
  );

  return singletonTopLayerProgram.curry([sinletonStruct, innerpuz]);
}

/// Convert the metadata dict to a Chialisp program
///  [metadata]: User defined metadata
///
///  Return Chialisp program
Program metadataToProgram(Map<Bytes, dynamic> metadata) {
  final kvList = <Program>[];

  metadata.forEach((key, value) {
    kvList.add(Program.cons(
      Program.fromBytes(key),
      Program.fromBytes(serializeItem(value)),
    ));
  });

  return Program.list(kvList);
}

Tuple5<Program, Program, Program, Program, Program>? uncurryInnerpuz(Program puz) {
  final uncurried = puz.uncurry();

  if (!isDidInnerPuz(uncurried.program)) {
    return null;
  }

  final args = uncurried.arguments;
  final p2Puzzle = args[0];
  final id_list = args[1];
  final numOfBackupIdsNeeded = args[2];
  final singletonStruct = args[3];
  final metadata = args[4];

  return Tuple5(p2Puzzle, id_list, numOfBackupIdsNeeded, singletonStruct, metadata);
}

bool isDidInnerPuz(Program innerPuzzle) {
  return innerPuzzle == DID_INNERPUZ_MOD;
}

List<Program>? matchDidPuzzle({required Program mod, required Program curriedArgs}) {
  try {
    if (mod == SINGLETON_TOP_LAYER_MOD_v1_1) {
      final uncurried = curriedArgs.rest().first().uncurry();
      if (uncurried.program == DID_INNERPUZ_MOD) {
        return uncurried.arguments;
      }
    }
  } catch (e, tracert) {
    print(e);
    print(tracert);
  }
  return null;
}
