class PuzzleInfo {
  final Map<String, dynamic> info;

  PuzzleInfo(this.info);

  String get type => info['type'];
  PuzzleInfo? get also =>
      info['also'] != null ? PuzzleInfo(Map<String, dynamic>.from(info['also'])) : null;

  @override
  bool operator ==(Object other) => other is PuzzleInfo && other.type == type && other.also == also;

  @override
  String toString() {
    return "PuzzleInfo($info)";
  }

  bool checkType({required List<String> types}) {
    if (types.isEmpty) {
      if (also == null) {
        return true;
      } else {
        return false;
      }
    } else {
      if (type == types.first) {
        types.removeAt(0);
        if (also != null) {
          return also!.checkType(types: types);
        } else {
          return checkType(types: types);
        }
      } else {
        return false;
      }
    }
  }

  operator [](Object key) => info[key];
}
