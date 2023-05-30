import '../../../chia_crypto_utils.dart';

class DeconstructedUpdateMetadataPuzzle {
  final Program metadata;
  final Program metadataUpdaterHash;

  final Program innerPuzzle;

  List<Program> get metadataList => metadata.toList();
  DeconstructedUpdateMetadataPuzzle({
    required this.metadata,
    required this.metadataUpdaterHash,
    required this.innerPuzzle,
  });
}
