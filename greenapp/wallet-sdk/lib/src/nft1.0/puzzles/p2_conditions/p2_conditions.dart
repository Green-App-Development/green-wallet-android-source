// ignore_for_file: lines_longer_than_80_chars

import '../../../clvm.dart';
import '../../../clvm/program.dart';

final P2_CONDITIONS_MOD = Program.deserializeHex(
  "ff04ffff0101ff0280",
);

Program puzzleForConditions(Program conditions) {
  return P2_CONDITIONS_MOD.run(Program.list([conditions])).program;
}

Program solution_for_conditions(Program conditions) {
  return Program.list([puzzleForConditions(conditions), Program.fromInt(0)]);
}
