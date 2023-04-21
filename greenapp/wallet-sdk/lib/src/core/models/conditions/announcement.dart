// TODO Implement this library.

import 'package:chia_crypto_utils/src/core/models/conditions/assert_puzzle_condition.dart';

import '../../../../chia_crypto_utils.dart';

class Announcement extends AssertPuzzleCondition {
  Announcement(
      Bytes settlementPh,
      Bytes message,
      ) : super((settlementPh + message).sha256Hash());
}
