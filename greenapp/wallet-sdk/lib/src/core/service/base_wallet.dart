// ignore_for_file: lines_longer_than_80_chars

import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/clvm/keywords.dart';
import 'package:chia_crypto_utils/src/core/models/conditions/assert_puzzle_condition.dart';
import 'package:chia_crypto_utils/src/core/models/conditions/create_puzzle_announcement_condition.dart';
import 'package:chia_crypto_utils/src/standard/exceptions/spend_bundle_validation/duplicate_coin_exception.dart';
import 'package:chia_crypto_utils/src/standard/exceptions/spend_bundle_validation/failed_signature_verification.dart';
import 'package:get_it/get_it.dart';
import 'package:tuple/tuple.dart';

import '../../standard/exceptions/origin_id_not_in_coins_exception.dart';
import '../exceptions/change_puzzlehash_needed_exception.dart';
import '../models/contidions_args.dart';

class BaseWalletService {
  BlockchainNetwork get blockchainNetwork => GetIt.I.get<BlockchainNetwork>();


  SpendBundle createSpendBundleBase({
    required List<Payment> payments,
    required List<CoinPrototype> coinsInput,
    Puzzlehash? changePuzzlehash,
    int fee = 0,
    Bytes? originId,
    List<AssertCoinAnnouncementCondition> coinAnnouncementsToAssert = const [],
    List<AssertPuzzleAnnouncementCondition> puzzleAnnouncementsToAssert = const [],
    required Program Function(Puzzlehash puzzlehash) makePuzzleRevealFromPuzzlehash,
    Program Function(Program standardSolution)? transformStandardSolution,
    required JacobianPoint Function(CoinSpend coinSpend) makeSignatureForCoinSpend,
  }) {
    Program makeSolutionFromConditions(List<Condition> conditions) {
      final standardSolution = BaseWalletService.makeSolutionFromConditions(conditions);
      if (transformStandardSolution == null) {
        return standardSolution;
      }
      return transformStandardSolution(standardSolution);
    }

    // copy coins input since coins list is modified in this function
    final coins = List<CoinPrototype>.from(coinsInput);
    final totalCoinValue = coins.fold(0, (int previousValue, coin) => previousValue + coin.amount);

    final totalPaymentAmount = payments.fold(
      0,
          (int previousValue, payment) => previousValue + payment.amount,
    );
    final change = totalCoinValue - totalPaymentAmount - fee;

    if (changePuzzlehash == null && change > 0) {
      throw ChangePuzzlehashNeededException();
    }

    final signatures = <JacobianPoint>[];
    final spends = <CoinSpend>[];

    // returns -1 if originId is given but is not in coins
    final originIndex = originId == null ? 0 : coins.indexWhere((coin) => coin.id == originId);

    if (originIndex == -1) {
      throw OriginIdNotInCoinsException();
    }

    // origin coin should be processed first so move it to the front of the list
    if (originIndex != 0) {
      final originCoin = coins.removeAt(originIndex);
      coins.insert(0, originCoin);
    }

    AssertCoinAnnouncementCondition? primaryAssertCoinAnnouncement;

    var first = true;
    for (var i = 0; i < coins.length; i++) {
      final coin = coins[i];

      Program? solution;
      // create output for origin coin
      if (first) {
        first = false;
        final conditions = <Condition>[];
        final createdCoins = <CoinPrototype>[];
        for (final payment in payments) {
          final sendCreateCoinCondition = payment.toCreateCoinCondition();
          conditions.add(sendCreateCoinCondition);
          createdCoins.add(
            CoinPrototype(
              parentCoinInfo: coin.id,
              puzzlehash: payment.puzzlehash,
              amount: payment.amount,
            ),
          );
        }

        if (change > 0) {
          conditions.add(CreateCoinCondition(changePuzzlehash!, change));
          createdCoins.add(
            CoinPrototype(
              parentCoinInfo: coin.id,
              puzzlehash: changePuzzlehash,
              amount: change,
            ),
          );
        }

        if (fee > 0) {
          conditions.add(ReserveFeeCondition(fee));
        }

        conditions
          ..addAll(coinAnnouncementsToAssert)
          ..addAll(puzzleAnnouncementsToAssert);

        // generate message for coin announcements by appending coin_ids
        // see https://github.com/Chia-Network/chia-blockchain/blob/4bd5c53f48cb049eff36c87c00d21b1f2dd26b27/chia/wallet/wallet.py#L383
        //   message: bytes32 = std_hash(b"".join(message_list))
        final existingCoinsMessage = coins.fold(
          Bytes.empty,
              (Bytes previousValue, coin) => previousValue + coin.id,
        );
        final createdCoinsMessage = createdCoins.fold(
          Bytes.empty,
              (Bytes previousValue, coin) => previousValue + coin.id,
        );
        final message = (existingCoinsMessage + createdCoinsMessage).sha256Hash();
        conditions.add(CreateCoinAnnouncementCondition(message));

        primaryAssertCoinAnnouncement = AssertCoinAnnouncementCondition(coin.id, message);

        solution = makeSolutionFromConditions(conditions);
      } else {
        solution = makeSolutionFromConditions(
          [primaryAssertCoinAnnouncement!],
        );
      }

      final puzzle = makePuzzleRevealFromPuzzlehash(coin.puzzlehash);
      final coinSpend = CoinSpend(coin: coin, puzzleReveal: puzzle, solution: solution);
      spends.add(coinSpend);

      final signature = makeSignatureForCoinSpend(coinSpend);
      signatures.add(signature);
    }

    final aggregate = AugSchemeMPL.aggregate(signatures);

    return SpendBundle(coinSpends: spends, aggregatedSignature: aggregate);
  }

  JacobianPoint makeSignature(
    PrivateKey privateKey,
    CoinSpend coinSpend,
  ) {
    final result = coinSpend.puzzleReveal.run(coinSpend.solution);

    final addsigmessage = getAddSigMeMessageFromResult(result.program, coinSpend.coin);

    final synthSecretKey = calculateSyntheticPrivateKey(privateKey);
    final signature = AugSchemeMPL.sign(synthSecretKey, addsigmessage);

    return signature;
  }


  Bytes getAddSigMeMessageFromResult(Program result, CoinPrototype coin) {
    final aggSigMeCondition = result.toList().singleWhere(AggSigMeCondition.isThisCondition);
    return Bytes(aggSigMeCondition.toList()[2].atom) +
        coin.id +
        Bytes.fromHex(
          blockchainNetwork.aggSigMeExtraData,
        );
  }


  Tuple3<Exception?, Map<ConditionOpcode, List<ConditionWithArgs>>?, BigInt>
  conditionsDictForSolution({
    required Program puzzleReveal,
    required Program solution,
    int maxCost = Program.cost,
  }) {
    final result =
    conditionsForSolution(puzzleReveal: puzzleReveal, solution: solution, maxCost: maxCost);
    if (result.item1 != null || result.item2 == null) {
      return Tuple3(result.item1, null, result.item3);
    }
    final dictResult = conditionsByOpcode(conditions: result.item2!);
    return Tuple3(null, dictResult, result.item3);
  }

  Tuple3<Exception?, List<ConditionWithArgs>?, BigInt> conditionsForSolution(
      {required Program puzzleReveal, required Program solution, int maxCost = Program.cost}) {
    try {
      final result = puzzleReveal.run(solution);
      print(result.program.hash().toHex());
      final parsed = parseSexpToConditions(result.program);
      return Tuple3(parsed.item1, parsed.item2, result.cost);
    } on Exception catch (e, stackTrace) {
      print(stackTrace);
      print(solution);
      return Tuple3(e, null, BigInt.from(0));
    }
  }


  Map<ConditionOpcode, List<ConditionWithArgs>> conditionsByOpcode(
      {required List<ConditionWithArgs> conditions}) {
    final dict = <ConditionOpcode, List<ConditionWithArgs>>{};

    for (final condition in conditions) {
      if (dict[condition.conditionOpcode] == null) {
        dict[condition.conditionOpcode] = <ConditionWithArgs>[];
      }
      dict[condition.conditionOpcode]!.add(condition);
    }
    return dict;
  }

  ///   Takes a ChiaLisp sexp (list) and returns the list of ConditionWithArgss
  /// If it fails, returns as Error
  Tuple2<Exception?, List<ConditionWithArgs>?> parseSexpToConditions(
      Program sexp,
      ) {
    final results = <ConditionWithArgs>[];
    try {
      final sexpList = sexp.toList();
      for (final item in sexp.toList()) {
        final result = parseSexpToCondition(item);
        if (result.item1 != null) {
          return Tuple2(result.item1, null);
        }
        results.add(result.item2!);
      }
      return Tuple2(null, results);
    } on Exception catch (e) {
      return Tuple2(e, null);
    }
  }

  /// Takes a ChiaLisp sexp (list) and returns the list of ConditionWithArgss
  /// If it fails, returns as Error
  Tuple2<Exception?, ConditionWithArgs?> parseSexpToCondition(Program sexp) {
    try {
      print(sexp.toSource());
      final atoms = sexp.toList();
      if (atoms.length < 1) {
        return Tuple2(Exception("INVALID_CONDITION"), null);
      }
      final opCode = ConditionOpcode(atoms.first.atom);
      return Tuple2(
          null,
          ConditionWithArgs(
            conditionOpcode: opCode,
            vars: atoms
                .sublist(1)
                .where((e) {
              try {
                final _ = e.atom;
                return true;
              } catch (e) {
                return false;
              }
            })
                .map((e) => e.atom)
                .toList(),
          ));
    } catch (e, stackTrace) {
      print(stackTrace);
      print(e);
      print(sexp.toSource());
      return Tuple2(Exception("INVALID_CONDITION"), null);
    }
  }


  static Program makeSolutionFromConditions(List<Condition> conditions) {
    return makeSolutionFromProgram(
      Program.list([
        Program.fromBigInt(keywords['q']!),
        ...conditions.map((condition) => condition.program).toList()
      ]),
    );
  }

  static List<T> extractConditionsFromSolution<T>(
    Program solution,
    ConditionChecker<T> conditionChecker,
    ConditionFromProgramConstructor<T> conditionFromProgramConstructor,
  ) {
    return extractConditionsFromResult(
      solution.toList()[1],
      conditionChecker,
      conditionFromProgramConstructor,
    );
  }

  static List<T> extractConditionsFromResult<T>(
    Program result,
    ConditionChecker<T> conditionChecker,
    ConditionFromProgramConstructor<T> conditionFromProgramConstructor,
  ) {
    return result
        .toList()
        .where(conditionChecker)
        .map((p) => conditionFromProgramConstructor(p))
        .toList();
  }

  static Program makeSolutionFromProgram(Program program) {
    return Program.list([
      Program.nil,
      program,
      Program.nil,
    ]);
  }

  void validateSpendBundleSignature(SpendBundle spendBundle) {
    final publicKeys = <JacobianPoint>[];
    final messages = <List<int>>[];
    for (final spend in spendBundle.coinSpends) {
      final outputConditions = spend.puzzleReveal.run(spend.solution).program.toList();

      // look for assert agg sig me condition
      final aggSigMeProgram = outputConditions.singleWhere(AggSigMeCondition.isThisCondition);

      final aggSigMeCondition = AggSigMeCondition.fromProgram(aggSigMeProgram);
      publicKeys.add(aggSigMeCondition.publicKey);
      messages.add(
        aggSigMeCondition.message +
            spend.coin.id +
            Bytes.fromHex(blockchainNetwork.aggSigMeExtraData),
      );
    }

    // validate signature
    if (!AugSchemeMPL.aggregateVerify(
      publicKeys,
      messages,
      spendBundle.aggregatedSignature!,
    )) {
      throw FailedSignatureVerificationException();
    }
  }

  static void checkForDuplicateCoins(List<CoinPrototype> coins) {
    final idSet = <String>{};
    for (final coin in coins) {
      final coinIdHex = coin.id.toHex();
      if (idSet.contains(coinIdHex)) {
        throw DuplicateCoinException(coinIdHex);
      } else {
        idSet.add(coinIdHex);
      }
    }
  }

  static Program makeSolution({
    required List<Payment> primaries,
    List<AssertCoinAnnouncementCondition> coinAnnouncementsToAssert = const [],
    List<AssertPuzzleCondition> puzzleAnnouncementsToAssert = const [],
    Set<Bytes> coinAnnouncements = const {},
    Set<Bytes> puzzleAnnouncements = const {},
  }) {
    final conditions = <Condition>[];
    if (primaries.isNotEmpty) {
      for (final payment in primaries) {
        final createCondition = payment.toCreateCoinCondition();
        conditions.add(createCondition);
      }
    }

    conditions..addAll(coinAnnouncements.map(
          (coinAnnouncement) => CreateCoinAnnouncementCondition(coinAnnouncement),
    ))
    ..addAll(coinAnnouncementsToAssert);

    conditions.addAll(puzzleAnnouncements.map(
          (coinAnnouncement) => CreatePuzzleAnnouncementCondition(coinAnnouncement),
    ));
    conditions.addAll(puzzleAnnouncementsToAssert);

    return BaseWalletService.makeSolutionFromConditions(conditions);
  }

}

class CoinSpendAndSignature {
  const CoinSpendAndSignature(this.coinSpend, this.signature);

  final CoinSpend coinSpend;
  final JacobianPoint signature;
}
