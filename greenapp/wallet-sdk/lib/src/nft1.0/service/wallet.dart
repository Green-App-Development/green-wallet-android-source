import 'package:chia_crypto_utils/src/nft1.0/index.dart';

import '../../../chia_crypto_utils.dart';
export '../models/index.dart';

class NftWalletService extends BaseWalletService {
  final standardWalletService = StandardWalletService();

  static Program createFullpuzzle(Program innerpuz, Bytes launcherId) {
    final singletonStruct = Program.cons(
      Program.fromBytes(SINGLETON_TOP_LAYER_MOD_V1_1_HASH),
      Program.cons(Program.fromBytes(launcherId), Program.fromBytes(SINGLETON_LAUNCHER_HASH)),
    );
    return SINGLETON_TOP_LAYER_MOD_v1_1.curry([
      singletonStruct,
      innerpuz,
    ]);
  }
}
