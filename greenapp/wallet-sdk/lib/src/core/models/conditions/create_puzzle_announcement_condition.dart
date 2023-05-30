// ignore_for_file: lines_longer_than_80_chars

import 'package:chia_crypto_utils/chia_crypto_utils.dart';

class CreatePuzzleAnnouncementCondition implements Condition {
  static int conditionCode = 62;

  Bytes message;

  CreatePuzzleAnnouncementCondition(this.message);

  @override
  Program get program {
    return Program.list([
      Program.fromInt(conditionCode),
      Program.fromBytes(message),
    ]);
  }

  static bool isThisCondition(Program condition) {
    final conditionParts = condition.toList();
    if (conditionParts.length < 3 || conditionParts[0].toInt() != conditionCode) {
      return false;
    }
    return true;
  }

  @override
  String toString() => 'CreatePuzzleAnnouncementCondition(code: $conditionCode, message: $message)';
}
