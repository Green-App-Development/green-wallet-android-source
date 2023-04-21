import 'package:collection/collection.dart';

import '../../clvm.dart';

class NftMetadata {
  final List<String> uris;

  final Bytes hash;

  final List<String> metaUris;

  final Bytes? metaHash;

  final List<String> licenseUris;

  final Bytes? licenseHash;

  final int editionNumber;

  final int editionTotal;

  final int royaltyPc;

  final Puzzlehash royaltyPh;

  NftMetadata({
    required this.uris,
    required this.hash,
    this.metaUris = const [],
    this.metaHash,
    this.licenseUris = const [],
    this.licenseHash,
    this.editionNumber = 1,
    this.editionTotal = 1,
    this.royaltyPc = 5,
    required this.royaltyPh,
  });

  NftMetadata copyWith({
    List<String>? uris,
    Bytes? hash,
    List<String>? metaUris,
    Bytes? metaHash,
    List<String>? licenseUris,
    Bytes? licenseHash,
    int? editionNumber,
    int? editionTotal,
    int? royaltyPc,
    Puzzlehash? royaltyPh,
  }) {
    return NftMetadata(
      uris: uris ?? this.uris,
      hash: hash ?? this.hash,
      metaUris: metaUris ?? this.metaUris,
      metaHash: metaHash ?? this.metaHash,
      licenseUris: licenseUris ?? this.licenseUris,
      licenseHash: licenseHash ?? this.licenseHash,
      editionNumber: editionNumber ?? this.editionNumber,
      editionTotal: editionTotal ?? this.editionTotal,
      royaltyPc: royaltyPc ?? this.royaltyPc,
      royaltyPh: royaltyPh ?? this.royaltyPh,
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    final listEquals = const DeepCollectionEquality().equals;

    return other is NftMetadata &&
        listEquals(other.uris, uris) &&
        other.hash == hash &&
        listEquals(other.metaUris, metaUris) &&
        other.metaHash == metaHash &&
        listEquals(other.licenseUris, licenseUris) &&
        other.licenseHash == licenseHash &&
        other.editionNumber == editionNumber &&
        other.editionTotal == editionTotal;
  }

  @override
  int get hashCode {
    return uris.hashCode ^
        hash.hashCode ^
        metaUris.hashCode ^
        metaHash.hashCode ^
        licenseUris.hashCode ^
        licenseHash.hashCode ^
        editionNumber.hashCode ^
        editionTotal.hashCode;
  }

  Program toProgram() {
    if (metaUris.isNotEmpty && metaHash == null) {
      throw Exception("Meta hash can't be null when meta uris is not empyth");
    }

    if (licenseUris.isNotEmpty && licenseHash == null) {
      throw Exception("Licence hash can't be null when Licence uris is not empyth");
    }

    final metaMap = <String, Program>{
      "u": Program.list(uris.map((e) => Program.fromString(e)).toList()),
      "h": Program.fromBytes(hash),
      "mu": Program.list(metaUris.map((e) => Program.fromString(e)).toList()),
      "lu": Program.list(licenseUris.map((e) => Program.fromString(e)).toList()),
      "sn": Program.fromInt(editionNumber),
      "st": Program.fromInt(editionTotal),
    };

    if (metaUris.isNotEmpty && metaHash != null) {
      metaMap["mh"] = Program.fromBytes(metaHash!);
    }

    if (licenseUris.isNotEmpty && licenseHash != null) {
      metaMap["lh"] = Program.fromBytes(licenseHash!);
    }

    final metaDataP = <Program>[];

    metaMap.forEach((key, value) {
      metaDataP.add(Program.cons(Program.fromString(key), value));
    });

    return Program.list(metaDataP);
  }
}
