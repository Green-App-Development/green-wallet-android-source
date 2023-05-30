// ignore_for_file: lines_longer_than_80_chars

import '../../../clvm.dart';

final nftMetadataUpdaterProgram = Program.deserializeHex(
  'ff02ffff01ff04ffff04ffff02ffff03ffff22ff27ff3780ffff01ff02ffff03ffff21ffff09ff27ffff01826d7580ffff09ff27ffff01826c7580ffff09ff27ffff01758080ffff01ff02ff02ffff04ff02ffff04ff05ffff04ff27ffff04ff37ff808080808080ffff010580ff0180ffff010580ff0180ffff04ff0bff808080ffff01ff808080ffff04ffff01ff02ffff03ff05ffff01ff02ffff03ffff09ff11ff0b80ffff01ff04ffff04ff0bffff04ff17ff198080ff0d80ffff01ff04ff09ffff02ff02ffff04ff02ffff04ff0dffff04ff0bffff04ff17ff8080808080808080ff0180ff8080ff0180ff018080',
);

final NFT_METADATA_UPDATER = nftMetadataUpdaterProgram;
final NFT_METADATA_UPDATER_HASH = NFT_METADATA_UPDATER.hash();
