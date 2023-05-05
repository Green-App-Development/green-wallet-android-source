import 'dart:convert';

import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/nft1.0/index.dart';
import 'package:equatable/equatable.dart';
import 'package:tuple/tuple.dart';
import 'package:chia_crypto_utils/src/did/puzzles/did_puzzles.dart' as didPuzzles;


class DidInfo extends Equatable {
  final CoinPrototype? originCoin;
  final List<Puzzlehash> backupsIds;
  final int numOfBackupIdsNeeded;
  final List<Tuple2<Puzzlehash, LineageProof?>> parentInfo;
  final Program? currentInner;
  final Coin? tempCoin;
  final Puzzlehash? tempPuzzlehash;
  final Puzzlehash? didId;
  final Bytes? tempPubKey;
  final bool sentRecoveryTransaction;
  final String metadata;

  DidInfo({
    this.didId,
    required this.originCoin,
    required this.backupsIds,
    required this.numOfBackupIdsNeeded,
    required this.parentInfo,
    this.currentInner,
    this.tempCoin,
    this.tempPuzzlehash,
    this.tempPubKey,
    required this.sentRecoveryTransaction,
    required this.metadata,
  });

  @override
  List<Object> get props {
    return [
      originCoin ?? "",
      backupsIds,
      numOfBackupIdsNeeded,
      parentInfo,
      currentInner ?? "",
      tempCoin ?? "",
      tempPuzzlehash ?? "",
      tempPubKey ?? "",
      sentRecoveryTransaction,
      metadata,
    ];
  }

  DidInfo copyWith(
      {Coin? originCoin,
      List<Puzzlehash>? backupsIds,
      int? numOfBackupIdsNeeded,
      List<Tuple2<Puzzlehash, LineageProof?>>? parentInfo,
      Program? currentInner,
      Coin? tempCoin,
      Puzzlehash? tempPuzzlehash,
      Bytes? tempPubKey,
      bool? sentRecoveryTransaction,
      String? metadata,
      Bytes? p2PuzzleHash,
      Puzzlehash? didId}) {
    return DidInfo(
      didId: didId ?? this.didId,
      originCoin: originCoin ?? this.originCoin,
      backupsIds: backupsIds ?? this.backupsIds,
      numOfBackupIdsNeeded: numOfBackupIdsNeeded ?? this.numOfBackupIdsNeeded,
      parentInfo: parentInfo ?? this.parentInfo,
      currentInner: currentInner ?? this.currentInner,
      tempCoin: tempCoin ?? this.tempCoin,
      tempPuzzlehash: tempPuzzlehash ?? this.tempPuzzlehash,
      tempPubKey: tempPubKey ?? this.tempPubKey,
      sentRecoveryTransaction: sentRecoveryTransaction ?? this.sentRecoveryTransaction,
      metadata: metadata ?? this.metadata,
    );
  }

  List<String> get metadataStr {
    final metadataProgram = Program.parse(metadata);
    final metadataList = metadataProgram.toAtomList().map((e) => utf8.decode(e.atom)).toList();
    return metadataList;
  }

  Puzzlehash? get p2PuzzleHash => tempPuzzlehash;

  @override
  String toString() {
    return 'DidInfo(originCoin: $originCoin, backupsIds: $backupsIds, numOfBackupIdsNeeded: $numOfBackupIdsNeeded, parentInfo: $parentInfo, currentInner: $currentInner, tempCoin: $tempCoin, tempPuzzlehash: $tempPuzzlehash, tempPubKey: $tempPubKey, sentRecoveryTransaction: $sentRecoveryTransaction, metadata: $metadata)';
  }

  DidInfo? replaceLineagePuzzlehash(Puzzlehash innerPh) {
    final newParentInfo = parentInfo.map((e) {
      final lineageProof = e.item2;
      if (lineageProof != null) {
        return Tuple2(
            e.item1,
            LineageProof(
              parentName: e.item2!.parentName,
              innerPuzzleHash: innerPh,
              amount: e.item2!.amount,
            ));
      } else {
        return e;
      }
    }).toList();
    return copyWith(parentInfo: newParentInfo);
  }


  static DidInfo? uncurry({
    required Program fullpuzzleReveal,
    required Program solution,
    Coin? actualCoin,
    CoinPrototype? originCoin,
    List<Tuple2<Puzzlehash, LineageProof?>>? parentInfo,
  }) {
    final uncurriedFull = fullpuzzleReveal.uncurry();
    final uncurried = didPuzzles.uncurryInnerpuz(uncurriedFull.arguments[1]);
    if (uncurried == null) {
      return null;
    }
    final args = List<Program>.from(uncurried.toList());
    final p2Puzzle = args[0];
    final recoveryListHash = args[1].toBytes();
    final numOfBackupIdsNeeded = args[2];
    final singletonStruct = args[3];
    final metadata = args[4];
    final launcherId = Puzzlehash(singletonStruct.rest().first().atom);
    final innerSolution = solution.rest().rest().first();
    final recoveryList = <Puzzlehash>[];
    final empythHash = Program.list([]).hash();
    if (recoveryListHash.toHex() != empythHash.toHex()) {
      try {
        final listProgram = innerSolution.rest().rest().rest().rest().rest().toList();
        recoveryList.addAll(listProgram.map((e) => Puzzlehash(e.toBytes())));
      } catch (e) {}
    }

    try {
      return DidInfo(
          originCoin: originCoin,
          backupsIds: recoveryList,
          numOfBackupIdsNeeded: numOfBackupIdsNeeded.toInt(),
          parentInfo: parentInfo ?? [],
          sentRecoveryTransaction: false,
          currentInner: p2Puzzle,
          tempPuzzlehash: p2Puzzle.hash(),
          didId: launcherId,
          tempCoin: actualCoin,
          metadata: metadata.toSource());
    } catch (e) {
      return null;
    }
  }

}
