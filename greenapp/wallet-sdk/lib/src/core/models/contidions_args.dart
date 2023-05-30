import 'dart:typed_data';

import 'package:chia_crypto_utils/chia_crypto_utils.dart';

class ConditionWithArgs {
  final ConditionOpcode conditionOpcode;
  final List<Bytes> vars;

  ConditionWithArgs({required this.conditionOpcode, required this.vars});
}

class ConditionOpcode extends Bytes {
  // AGG_SIG is ascii "1"

  // the conditions below require bls12-381 signatures

  static final ConditionOpcode AGG_SIG_UNSAFE = ConditionOpcode.fromInt(49);
  static final ConditionOpcode AGG_SIG_ME = ConditionOpcode.fromInt(50);

  // the conditions below reserve coin amounts and have to be accounted for in output totals

  static final ConditionOpcode CREATE_COIN = ConditionOpcode.fromInt(51);
  static final ConditionOpcode RESERVE_FEE = ConditionOpcode.fromInt(52);

  // the conditions below deal with announcements, for inter-coin communication

  static final ConditionOpcode CREATE_COIN_ANNOUNCEMENT = ConditionOpcode.fromInt(60);
  static final ConditionOpcode ASSERT_COIN_ANNOUNCEMENT = ConditionOpcode.fromInt(61);
  static final ConditionOpcode CREATE_PUZZLE_ANNOUNCEMENT = ConditionOpcode.fromInt(62);
  static final ConditionOpcode ASSERT_PUZZLE_ANNOUNCEMENT = ConditionOpcode.fromInt(63);

  // the conditions below let coins inquire about themselves

  static final ConditionOpcode ASSERT_MY_COIN_ID = ConditionOpcode.fromInt(70);
  static final ConditionOpcode ASSERT_MY_PARENT_ID = ConditionOpcode.fromInt(71);
  static final ConditionOpcode ASSERT_MY_PUZZLEHASH = ConditionOpcode.fromInt(72);
  static final ConditionOpcode ASSERT_MY_AMOUNT = ConditionOpcode.fromInt(73);

  // the conditions below ensure that we're "far enough" in the future

  // wall-clock time
  static final ConditionOpcode ASSERT_SECONDS_RELATIVE = ConditionOpcode.fromInt(80);
  static final ConditionOpcode ASSERT_SECONDS_ABSOLUTE = ConditionOpcode.fromInt(81);

  // block index
  static final ConditionOpcode ASSERT_HEIGHT_RELATIVE = ConditionOpcode.fromInt(82);
  static final ConditionOpcode ASSERT_HEIGHT_ABSOLUTE = ConditionOpcode.fromInt(83);

  ConditionOpcode(Bytes bytesList) : super(bytesList);

  factory ConditionOpcode.fromInt(int value) {
    return ConditionOpcode(intToBytes(value, 2, Endian.big));
  }
  @override
  String toString() {
    return "ConditionOpcode(${bytesToInt(this, Endian.big)})";
  }

  int toInt() => bytesToInt(this, Endian.big);

  @override
  bool operator ==(Object other) {
    final first = other is ConditionOpcode;
    if (first) {
      final second = other.toInt() == this.toInt();
      return second;
    }
    return first;
  }
}
