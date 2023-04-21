// ignore_for_file: lines_longer_than_80_chars

import 'package:bech32m/bech32m.dart';
import 'package:chia_crypto_utils/src/clvm/bytes.dart';
import 'package:meta/meta.dart';

import '../../nft1.0/models/nft_info.dart';

@immutable
class NftAddress {
  const NftAddress(this.address);

  NftAddress.fromPuzzlehash(
    Puzzlehash puzzlehash,
  ) : address = segwit.encode(Segwit(NFT_HRP, puzzlehash));

  final String address;

  Puzzlehash toPuzzlehash() {
    return Puzzlehash(segwit.decode(address).program);
  }

  @override
  int get hashCode => runtimeType.hashCode ^ address.hashCode;

  @override
  bool operator ==(Object other) {
    return other is NftAddress && other.address == address;
  }

  @override
  String toString() => 'Address($address)';
}
