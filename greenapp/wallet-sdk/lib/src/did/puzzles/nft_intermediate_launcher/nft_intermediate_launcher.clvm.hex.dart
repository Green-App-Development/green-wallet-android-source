import 'package:chia_crypto_utils/src/clvm/program.dart';

final INTERMEDIATE_LAUNCHER_MOD = Program.deserializeHex(
    "ff02ffff01ff04ffff04ff04ffff04ff05ffff01ff01808080ffff04ffff04ff06ffff04ffff0bff0bff1780ff808080ff808080ffff04ffff01ff333cff018080");
final INTERMEDIATE_LAUNCHER_MOD_HASH = INTERMEDIATE_LAUNCHER_MOD.hash();
