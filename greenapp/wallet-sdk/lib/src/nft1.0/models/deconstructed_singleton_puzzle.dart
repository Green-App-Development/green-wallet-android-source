import '../../../chia_crypto_utils.dart';

class DeconstructedSingletonPuzzle {
  final Puzzlehash sinletonModHash;
  final Puzzlehash launcherPuzzhash;
  final Puzzlehash singletonLauncherId;
  final Program innerPuzzle;

  DeconstructedSingletonPuzzle({
    required this.sinletonModHash,
    required this.launcherPuzzhash,
    required this.singletonLauncherId,
    required this.innerPuzzle,
  });
}
