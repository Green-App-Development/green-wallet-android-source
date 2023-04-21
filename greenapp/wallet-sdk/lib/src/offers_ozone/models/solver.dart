import 'dart:convert';
import 'dart:typed_data';

import '../../../chia_crypto_utils.dart';

bool _isQuotedProgram(Program program) {
  try {
    final _ = utf8.decode(program.atom);
    return true;
  } catch (e) {
    return false;
  }
}

enum AtomType { quoted, integer, atom }

AtomType typeForAtom(Program program) {
  final isQuoted = _isQuotedProgram(program);
  if (isQuoted) return AtomType.quoted;
  final atom = program.atom;
  try {
    if (intToBytes(decodeInt(atom), 4, Endian.big) == atom) {
      return AtomType.integer;
    }
  } catch (e) {}

  return AtomType.atom;
}

class Solver {
  final Map<String, dynamic> info;

  Solver(this.info);

  dynamic operator [](String key) {
    final value = info[key];
    return decodeInfoValue(this, value);
  }

  static dynamic decodeInfoValue(Solver solver, dynamic value) {
    if (value is Solver) {
      return value;
    } else if (value is List && !(value is Bytes)) {
      return value.map((e) => decodeInfoValue(solver, e)).toList();
    }
    // special case
    if (value == "()") {
      return Program.list([]);
    }
    final programValue = Program.parse(value);

    if (!programValue.isAtom) {
      return programValue;
    } else {
      final type = typeForAtom(programValue);
      switch (type) {
        case AtomType.quoted:
          return utf8.decode(programValue.atom);
        case AtomType.integer:
          return decodeInt(programValue.atom);

        case AtomType.atom:
          return programValue.atom;
      }
    }
    //return Program.nil as T;
  }
}
