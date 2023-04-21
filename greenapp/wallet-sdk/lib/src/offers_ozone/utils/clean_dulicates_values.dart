/// remove duplicates with out order change
List<T> cleanDuplicatesValues<T>(List<T> values) {
  final map = <T, dynamic>{};
  values.forEach((element) {
    map[element] = 1;
  });
  return map.keys.toList();
}
