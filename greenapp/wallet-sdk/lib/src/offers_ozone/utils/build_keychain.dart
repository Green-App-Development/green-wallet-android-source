import '../../clvm.dart';
import '../../core/index.dart';

typedef BuildKeychain = Future<WalletKeychain?> Function(Set<Puzzlehash> phs);
