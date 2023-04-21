import 'package:bech32m/bech32m.dart';
import 'dart:convert';
import '../../chia_crypto_utils.dart';

class SegwitValidations {
  bool isEmptyProgram(List<int> data) {
    return data.isEmpty;
  }

/*   bool isTooLongProgram(List<int> program) {
    return program.length > 40;
  }
 */
  bool isTooShortProgram(List<int> program) {
    return program.length < 2;
  }
}

/// This class converts a String to a Segwit class instance.
class OfferSegwitDecoder extends Converter<String, Segwit> with SegwitValidations {
  @override
  Segwit convert(String input) {
    var decoded = bech32.decode(input, input.length);

    if (isEmptyProgram(decoded.data)) {
      throw InvalidProgramLength('empty');
    }

    var program = _convertBits(decoded.data, 5, 8, false);

    if (isTooShortProgram(program)) {
      throw InvalidProgramLength('too short');
    }

    /* if (isTooLongProgram(program)) {
      throw InvalidProgramLength('too long');
    } */

    return Segwit(decoded.hrp, program);
  }
}

class OfferSegwitEncoder extends Converter<Segwit, String> with SegwitValidations {
  @override
  String convert(Segwit input) {
    var program = input.program;

    if (isTooShortProgram(program)) {
      throw InvalidProgramLength('too short');
    }

    var data = _convertBits(program, 8, 5, true);

    return Bech32mEncoder().convert(
      Bech32m(input.hrp, data),
      data.length + input.hrp.length + 1 + Bech32mValidations.checksumLength,
    );
  }
}

Bytes decodeFromBench32(String input) {
  return Bytes(SegwitDecoder().convert(input).program);
}

List<int> _convertBits(List<int> data, int from, int to, bool pad) {
  var acc = 0;
  var bits = 0;
  var result = <int>[];
  var maxv = (1 << to) - 1;

  data.forEach((v) {
    if (v < 0 || (v >> from) != 0) {
      throw Exception();
    }
    acc = (acc << from) | v;
    bits += from;
    while (bits >= to) {
      bits -= to;
      result.add((acc >> bits) & maxv);
    }
  });

  if (pad) {
    if (bits > 0) {
      result.add((acc << (to - bits)) & maxv);
    }
  }
  return result;
}
