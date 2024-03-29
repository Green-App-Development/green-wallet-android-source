// ignore_for_file: lines_longer_than_80_chars

import 'package:chia_crypto_utils/src/clvm/program.dart';

final singletonLauncherProgram = Program.deserializeHex(
  'ff02ffff01ff04ffff04ff04ffff04ff05ffff04ff0bff80808080ffff04ffff04ff0affff04ffff02ff0effff04ff02ffff04ffff04ff05ffff04ff0bffff04ff17ff80808080ff80808080ff808080ff808080ffff04ffff01ff33ff3cff02ffff03ffff07ff0580ffff01ff0bffff0102ffff02ff0effff04ff02ffff04ff09ff80808080ffff02ff0effff04ff02ffff04ff0dff8080808080ffff01ff0bffff0101ff058080ff0180ff018080',
);

final SINGLETON_MOD = singletonLauncherProgram;
final LAUNCHER_PUZZLE = singletonLauncherProgram;

/// SINGLETON_LAUNCHER_HASH
final LAUNCHER_PUZZLE_HASH = SINGLETON_MOD.hash();

/// LAUNCHER_PUZZLE_HASH
final SINGLETON_LAUNCHER_HASH = LAUNCHER_PUZZLE_HASH;
