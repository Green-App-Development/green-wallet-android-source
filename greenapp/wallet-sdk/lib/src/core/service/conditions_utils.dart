import 'package:tuple/tuple.dart';

import '../../clvm.dart';
import '../models/contidions_args.dart';

List<Tuple2<Bytes, Bytes>> pkmPairsForConditionsDict({
  required Map<ConditionOpcode, List<ConditionWithArgs>> conditionsDict,
  required Bytes coinName,
  required Bytes additionalData,
}) {
  final ret = <Tuple2<Bytes, Bytes>>[];

  conditionsDict.forEach((condition, values) {
    if (ConditionOpcode.AGG_SIG_ME == condition) {
      for (var cwa in values) {
        if (cwa.vars.length != 2) {
          throw Exception("${cwa.vars.length} len is not 2");
        }
        if (cwa.vars[0].length != 48 || cwa.vars[1].length > 1024) {
          throw Exception(
              "${cwa.vars[0].length} is different to 48 OR ${cwa.vars[1].length} is greather than 1024");
        }
        final msg = cwa.vars[1] + coinName + additionalData;

        ret.add(Tuple2(cwa.vars[0], msg));
      }
    } else if (ConditionOpcode.AGG_SIG_UNSAFE == condition) {
      for (var cwa in values) {
        if (cwa.vars.length != 2) {
          throw Exception("${cwa.vars.length} len is not 2");
        }
        if (cwa.vars[0].length != 48 || cwa.vars[1].length > 1024) {
          throw Exception(
              "${cwa.vars[0].length} is different to 48 OR ${cwa.vars[1].length} is greather than 1024");
        }
        ret.add(Tuple2(cwa.vars[0], cwa.vars[1]));
      }
    }
  });

  return ret;
}
