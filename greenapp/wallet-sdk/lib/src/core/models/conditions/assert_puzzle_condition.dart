// ignore_for_file: lines_longer_than_80_chars

import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/standard/exceptions/invalid_condition_cast_exception.dart';

class AssertPuzzleCondition extends AssertPuzzleAnnouncementCondition {
  AssertPuzzleCondition(super.announcementHash);

  factory AssertPuzzleCondition.fromProgram(Program program) {
    final programList = program.toList();
    if (!isThisCondition(program)) {
      throw InvalidConditionCastException(AssertPuzzleCondition);
    }
    return AssertPuzzleCondition(Bytes(programList[1].atom));
  }

  static bool isThisCondition(Program condition) {
    final conditionParts = condition.toList();
    if (conditionParts.length != 2) {
      return false;
    }
    if (conditionParts[0].toInt() != AssertPuzzleAnnouncementCondition.conditionCode) {
      return false;
    }
    return true;
  }
}
