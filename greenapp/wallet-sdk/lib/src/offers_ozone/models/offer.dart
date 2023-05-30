import 'package:bech32m/bech32m.dart';
import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/core/models/conditions/announcement.dart';
import 'package:chia_crypto_utils/src/core/models/outer_puzzle.dart';
import 'package:chia_crypto_utils/src/utils/from_bench32.dart';
import 'package:quiver/iterables.dart';

final OFFERS_HASHES = {OFFER_MOD_HASH, OFFER_MOD_V1_HASH};
final OFFER_MOD_OLD_HASH = OFFER_MOD_V1_HASH;

class Offer {
  /// The key is the asset id of the asset being requested, if is null then request XCH
  final Map<Bytes?, List<NotarizedPayment>> requestedPayments;
  final SpendBundle bundle;

  ///  asset_id -> asset driver
  final Map<Bytes, PuzzleInfo> driverDict;
  late final Map<CoinPrototype, List<CoinPrototype>> _additions;

  final bool old;

  Offer({
    required this.requestedPayments,
    required this.bundle,
    required this.driverDict,
    required this.old,
    Map<CoinPrototype, List<CoinPrototype>>? additions,
  }) {
    _additions = additions ?? _calculateAdditions();
  }
  Map<CoinPrototype, List<CoinPrototype>> _calculateAdditions() {
    // Verify that there are no duplicate payments
    for (var payments in requestedPayments.values) {
      var paymentPrograms = payments.map((e) => e.toProgram().hash());
      if (paymentPrograms.toSet().length != paymentPrograms.length) {
        throw ArgumentError('Bundle has duplicate requested payments');
      }
    }
    // Verify we have a type for every kind of asset
    for (var assetId in requestedPayments.keys) {
      if (assetId != null && !driverDict.containsKey(assetId)) {
        throw ArgumentError(
            'Offer does not have enough driver information about the requested payments');
      }
    }
    // populate the _additions cache
    var adds = <CoinPrototype, List<CoinPrototype>>{};

    for (var cs in bundle.coinSpends) {
      // you can't spend the same coin twice in the same SpendBundle
      assert(!adds.containsKey(cs.coin));
      try {
        var coins = cs.additions;

        adds[cs.coin] = coins;
      } catch (e) {
        continue;
      }
    }
    return adds;
  }

  static Puzzlehash ph(bool old) => old ? OFFER_MOD_V1_HASH : OFFER_MOD_HASH;

  /// calc the coins hash [nonce]
  static Map<Bytes?, List<NotarizedPayment>> notarizePayments({
    required Map<Bytes?, List<Payment>> requestedPayments, //`Null` means you are requesting XCH
    required List<CoinPrototype> coins,
  }) {
    // This sort should be reproducible in CLVM with `>s`

    final sortedCoins = coins.toList()..sort((a, b) => a.id.compareTo(b.id));
    final sortedCoinList = sortedCoins.toList().map((e) => e.toProgram()).toList();
    final nonce = Program.list(sortedCoinList).hash();

    final result = Map<Bytes?, List<NotarizedPayment>>();
    requestedPayments.forEach((assetId, payments) {
      result[assetId] = [];
      payments.forEach((payment) {
        result[assetId]!.add(
          NotarizedPayment(
            payment.amount,
            payment.puzzlehash,
            memos: payment.memos,
            nonce: nonce,
          ),
        );
      });
    });
    return result;
  }

  static List<Announcement> calculateAnnouncements({
    required Map<Bytes?, List<NotarizedPayment>> notarizedPayment,
    required Map<Bytes?, PuzzleInfo> driverDict,
    required bool old,
  }) {
    final result = <Announcement>[];
    notarizedPayment.forEach((assetId, payments) {
      Bytes? settlementPh;
      if (assetId != null) {
        if (!driverDict.containsKey(assetId)) {
          throw Exception(
              "Cannot calculate announcements without driver of requested item $assetId");
        }
        settlementPh = OuterPuzzleDriver.constructPuzzle(
          constructor: driverDict[assetId]!,
          innerPuzzle: old ? OFFER_MOD_V1 : OFFER_MOD,
        ).hash();
      } else {
        settlementPh = (old) ? OFFER_MOD_V1_HASH : OFFER_MOD_HASH;
      }
      final paymentsPrograms = payments.map((e) => e.toProgram()).toList();
      final msgProgram = Program.cons(
        Program.fromBytes(payments.first.nonce),
        Program.list(paymentsPrograms),
      );

      Bytes msg = msgProgram.hash();

      result.add(Announcement(settlementPh, msg));
    });
    return result;
  }

  Map<Bytes?, List<CoinPrototype>> getOfferedCoins() {
    final offeredCoins = <Bytes?, List<CoinPrototype>>{};

    for (var parentSpend in bundle.coinSpends) {
      var coinsForThisSpend = <CoinPrototype>[];

      final parentPuzzle = parentSpend.puzzleReveal;
      final parentSolution = parentSpend.solution;
      final additions = _additions[parentSpend.coin]!;

      final puzzleDriver = OuterPuzzleDriver.matchPuzzle(parentPuzzle);

      Bytes? assetId;

      if (puzzleDriver != null) {
        assetId = OuterPuzzleDriver.createAssetId(puzzleDriver);
        final innerPuzzle = OuterPuzzleDriver.getInnerPuzzle(
          constructor: puzzleDriver,
          puzzleReveal: parentPuzzle,
        );
        final innerSolution = OuterPuzzleDriver.getInnerSolution(
          constructor: puzzleDriver,
          solution: parentSolution,
        );
        if (innerPuzzle == null || innerSolution == null) {
          throw Exception("innerPuzzle or innerSolution is null");
        }

        final conditionResult = innerPuzzle.run(innerSolution);
        final conditionResultIter = conditionResult.program.toList();
        int expectedNumMatches = 0;
        List<int> offeredAmounts = [];

        for (var condition in conditionResultIter) {
          if (condition.first().toInt() == 51 &&
              OFFERS_HASHES.contains(Puzzlehash(condition.rest().first().atom))) {
            expectedNumMatches++;
            offeredAmounts.add(condition.rest().rest().first().toInt());
          }
        }
        List<CoinPrototype> matchingSpendAdditions = additions
            .where(
              (a) => offeredAmounts.contains(a.amount),
            )
            .toList();

        if (matchingSpendAdditions.length == expectedNumMatches) {
          coinsForThisSpend.addAll(matchingSpendAdditions);
        } else {
          if (matchingSpendAdditions.length < expectedNumMatches) {
            matchingSpendAdditions = additions;
          }

          matchingSpendAdditions = matchingSpendAdditions.where((a) {
            final posiblePhs = [
              OuterPuzzleDriver.constructPuzzle(
                constructor: puzzleDriver,
                innerPuzzle: OFFER_MOD_V1,
              ).hash(),
              OuterPuzzleDriver.constructPuzzle(
                constructor: puzzleDriver,
                innerPuzzle: OFFER_MOD_V2,
              ).hash(),
            ];
            return posiblePhs.contains(a.puzzlehash);
          }).toList();
          if (matchingSpendAdditions.length == expectedNumMatches) {
            coinsForThisSpend.addAll(matchingSpendAdditions);
          } else {
            throw Exception("Could not properly guess offered coins from parent spend");
          }
        }
      } else {
        assetId = null;
        coinsForThisSpend.addAll(additions
            .where(
              (element) => OFFERS_HASHES.contains(element.puzzlehash),
            )
            .toList());
      }

      //We only care about unspent coins
      List<CoinPrototype> removals = this.bundle.removals;
      coinsForThisSpend = coinsForThisSpend.where((c) => !removals.contains(c)).toList();

      if (coinsForThisSpend.isNotEmpty) {
        offeredCoins[assetId] ??= [];
        offeredCoins[assetId]!.addAll(coinsForThisSpend);
        offeredCoins[assetId] = offeredCoins[assetId]!.toSet().toList();
      }
    }
    return offeredCoins;
  }

  Map<Bytes?, int> getOfferedAmounts() {
    final Map<Bytes?, int> offeredAmounts = {};
    final Map<Bytes?, List<CoinPrototype>> offeredCoins = getOfferedCoins();
    offeredCoins.forEach((assetId, coins) {
      offeredAmounts[assetId] = coins.fold(0, (a, b) => a + b.amount);
    });
    return offeredAmounts;
  }

  Map<Bytes?, int> getRequestedAmounts() {
    final offeredAmounts = <Bytes?, int>{};
    final coins = requestedPayments;
    coins.forEach((assetId, coins) {
      offeredAmounts[assetId] = coins.fold(0, (a, b) => a + b.amount);
    });
    return offeredAmounts;
  }

  Map<Bytes?, int> arbitrage() {
    final arbitrageDict = <Bytes?, int>{};
    final offered_amounts = getOfferedAmounts();
    final requested_amounts = getRequestedAmounts();
    final keys = [...offered_amounts.keys, ...requested_amounts.keys].toSet();
    for (var assetId in keys) {
      arbitrageDict[assetId] = (offered_amounts[assetId] ?? 0) - (requested_amounts[assetId] ?? 0);
    }
    return arbitrageDict;
  }

  List<Map<String, dynamic>> summary() {
    final offered_amounts = getOfferedAmounts();
    final requested_amounts = getRequestedAmounts();

    final driverDictR = <Bytes?, Map<String, dynamic>>{};
    driverDict.forEach((key, value) {
      driverDictR[key] = value.info;
    });

    return [
      _keysToStrings(offered_amounts),
      _keysToStrings(requested_amounts),
      _keysToStrings(driverDictR)
    ];
  }

  /// Also mostly for the UI, returns a dictionary of assets and how much of them is pended for this offer
  /// This method is also imperfect for sufficiently complex spends
  Map<String, int> getPendingAmounts() {
    final allAdittions = bundle.additions;
    final allRemovals = bundle.removals;
    final notEphomeralRemovals = allRemovals
        .where(
          (coin) => !allAdittions.contains(coin),
        )
        .toList();
    Map<String, int> pendingDict = {};
    // First we add up the amounts of all coins that share an ancestor with the offered coins (i.e. a primary coin)
    final offerred = getOfferedCoins();
    offerred.forEach((assetId, coins) {
      final name = assetId == null ? "xch" : assetId.toHex();
      pendingDict[name] = 0;
      for (var coin in coins) {
        final rootRemoval = getRootRemoval(coin);
        final pocessableAdditions = allAdittions
            .where(
              (element) => element.parentCoinInfo == rootRemoval.id,
            )
            .toList();
        pocessableAdditions.forEach((addition) {
          final lastAmount = pendingDict[name]!;
          pendingDict[name] = lastAmount + addition.amount;
        });
      }
    });

    // Then we gather anything else as unknown
    final sumOfadditionssoFar = pendingDict.values.fold<int>(
      0,
      (previousValue, element) => previousValue + element,
    );

    final nonEphimeralsSum = notEphomeralRemovals.map((e) => e.amount).fold<int>(
          0,
          (previousValue, element) => previousValue + element,
        );

    final unknownAmount = nonEphimeralsSum - sumOfadditionssoFar;
    if (unknownAmount > 0) {
      pendingDict["unknown"] = unknownAmount;
    }
    return pendingDict;
  }

  List<CoinPrototype> getInvolvedCoins() {
    final additions = bundle.additions;
    return bundle.removals.where((coin) => !additions.contains(coin)).toList();
  }

  /// This returns the non-ephemeral removal that is an ancestor of the specified coins
  /// This should maybe move to the SpendBundle object at some point
  CoinPrototype getRootRemoval(CoinPrototype coin) {
    final allRemovals = bundle.removals.toSet();
    final allRemovalsIds = allRemovals.map((e) => e.id).toList().toSet();
    final nonEphemeralRemovals = allRemovals
        .where((element) => !allRemovalsIds.contains(
              element.parentCoinInfo,
            ))
        .toSet();
    if (!allRemovalsIds.contains(coin.id) && !allRemovalsIds.contains(coin.parentCoinInfo)) {
      throw CoinNotInBundle(coin.id);
    }

    while (!nonEphemeralRemovals.contains(coin)) {
      final removalsIter =
          allRemovals.where((element) => element.id == coin.parentCoinInfo).iterator;
      removalsIter.moveNext();
      coin = removalsIter.current;
    }
    return coin;
  }

  /// This will only return coins that are ancestors of settlement payments
  List<CoinPrototype> getPrimaryCoins() {
    final pCoins = Set<CoinPrototype>();
    final offerredCoins = getOfferedCoins();
    offerredCoins.forEach((_, coins) {
      coins.forEach((coin) {
        final rootRemoval = getRootRemoval(coin);
        if (!pCoins.contains(rootRemoval)) {
          pCoins.add(rootRemoval);
        }
      });
    });
    return pCoins.toList();
  }

  static Offer aggregate(List<Offer> offers) {
    Map<Bytes?, List<NotarizedPayment>> totalRequestedPayments = {};
    SpendBundle totalBundle = SpendBundle(
      coinSpends: [],
    );
    Map<Bytes, PuzzleInfo> totalDriverDict = {};
    bool old = false;

    for (int i = 0; i < offers.length; i++) {
      Offer offer = offers[i];

      // First check for any overlap in inputs
      final totalInputs = Set<CoinPrototype>.from(totalBundle.coinSpends.map((cs) => cs.coin));
      final offerInputs = Set<CoinPrototype>.from(offer.bundle.coinSpends.map((cs) => cs.coin));

      if (totalInputs.intersection(offerInputs).isNotEmpty) {
        throw Exception("The aggregated offers overlap inputs");
      }

      // Next, do the aggregation
      for (var entry in offer.requestedPayments.entries) {
        Bytes? assetId = entry.key;
        List<NotarizedPayment> payments = entry.value;
        if (totalRequestedPayments.containsKey(assetId)) {
          totalRequestedPayments[assetId]!.addAll(payments);
        } else {
          totalRequestedPayments[assetId] = payments;
        }
      }

      for (var entry in offer.driverDict.entries) {
        Bytes? key = entry.key;
        PuzzleInfo value = entry.value;
        if (totalDriverDict.containsKey(key) && totalDriverDict[key] != value) {
          throw Exception("The offers to aggregate disagree on the drivers for $key");
        }
      }

      totalBundle = SpendBundle.aggregate([totalBundle, offer.bundle]);
      totalDriverDict.addAll(offer.driverDict);
      if (i == 0) {
        old = offer.old;
      } else {
        if (offer.old != old) {
          throw Exception("Attempting to aggregate two offers with different mods");
        }
      }
    }

    return Offer(
        requestedPayments: totalRequestedPayments,
        bundle: totalBundle,
        driverDict: totalDriverDict,
        old: old);
  }

  /// Validity is defined by having enough funds within the offer to satisfy both sidess
  bool isValid() {
    final _arbitrage = arbitrage();
    final arbitrageValues = _arbitrage.values.toList();
    final satisfaceds = arbitrageValues
        .where(
          (element) => (element >= 0),
        )
        .length;
    final valid = satisfaceds == arbitrageValues.length;

    return valid;
  }

  CoinSpend _getSpendSpendOfCoin(CoinPrototype coin) {
    return bundle.coinSpends.where((element) => element.coin.id == coin.parentCoinInfo).first;
  }

  ///  A "valid" spend means that this bundle can be pushed to the network and will succeed
  /// This differs from the `to_spend_bundle` method which deliberately creates an invalid SpendBundle
  SpendBundle toValidSpend({Bytes? arbitragePh}) {
    if (!isValid()) {
      throw Exception("Offer is currently incomplete");
    }
    final completionSpends = <CoinSpend>[];
    final allOfferredCoins = getOfferedCoins();
    final totalArbitrageAmount = arbitrage();

    requestedPayments.forEach((Bytes? assetId, List<NotarizedPayment> payments) {
      final List<CoinPrototype> offerredCoins = allOfferredCoins[assetId]!;

      // Because of CAT supply laws, we must specify a place for the leftovers to go
      final int? arbitrageAmount = totalArbitrageAmount[assetId];
      final allPayments = List<NotarizedPayment>.from(payments.toList());
      if (arbitrageAmount == null) {
        throw Exception("arbitrageAmount is null, ${arbitrageAmount}");
      }

      if (arbitrageAmount > 0) {
        if (arbitragePh == null) {
          throw Exception(
            "ArbitragePH can't be null when arbitrage Amount is more than 0, ${arbitrageAmount}",
          );
        }

        allPayments.add(NotarizedPayment(
          arbitrageAmount,
          Puzzlehash(arbitragePh),
        ));
      }

      // Some assets need to know about siblings so we need to collect all spends first to be able to use them
      final coinToSpendDict = <CoinPrototype, CoinSpend>{};
      final coinToSolutionDict = <CoinPrototype, Program>{};
      for (var coin in offerredCoins) {
        final parentSpend = _getSpendSpendOfCoin(coin);
        coinToSpendDict[coin] = parentSpend;
        final List<Program> innerSolutions = [];
        if (coin == offerredCoins.first) {
          final nonces = allPayments.map((e) => e.nonce).toList();

          final noncesValues = cleanDuplicatesValues(nonces);
          for (var nonce in noncesValues) {
            // List<NotarizedPayment>
            final noncePayments = allPayments.where((p) => p.nonce == nonce).toList();

            innerSolutions.add(Program.cons(
              Program.fromBytes(nonce),
              Program.list(
                noncePayments.map((e) => e.toProgram()).toList(),
              ),
            ));
          }
        }
        coinToSolutionDict[coin] = Program.list(innerSolutions);
      }

      for (var coin in offerredCoins) {
        Program? solution;
        Program offerMod = OFFER_MOD;
        if (assetId != null) {
          if (OuterPuzzleDriver.constructPuzzle(
                constructor: driverDict[assetId]!,
                innerPuzzle: OFFER_MOD_V1,
              ).hash() ==
              coin.puzzlehash) {
            print("Using OFFER V1 ${OFFER_MOD_V1.hash().toHex()}");
            offerMod = OFFER_MOD_V1;
          } else {
            // for default offermod is equal to OFFER_MOD
            //offerMod = OFFER_MOD;
          }
          String siblings = "(";
          String siblingsSpends = "(";
          String silblingsPuzzles = "(";
          String silblingsSolutions = "(";

          String disassembledOfferMod = offerMod.toSource();

          for (var siblingCoin in offerredCoins) {
            if (siblingCoin != coin) {
              siblings += siblingCoin.toBytes().toHexWithPrefix();
              siblingsSpends += coinToSpendDict[siblingCoin]!.toBytes().toHexWithPrefix();
              silblingsPuzzles += disassembledOfferMod;
              silblingsSolutions += coinToSolutionDict[siblingCoin]!.serialize().toHexWithPrefix();
            }
          }
          siblings += ")";
          siblingsSpends += ")";
          silblingsPuzzles += ")";
          silblingsSolutions += ")";
          final solverDict = {
            "coin": coin.toBytes().toHexWithPrefix(),
            "parent_spend": coinToSpendDict[coin]!.toProgram().serialize().toHexWithPrefix(),
            "siblings": siblings,
            "sibling_spends": siblingsSpends,
            "sibling_puzzles": silblingsPuzzles,
            "sibling_solutions": silblingsSolutions,
          };

          final solver = Solver(solverDict);

          solution = OuterPuzzleDriver.solvePuzzle(
            constructor: driverDict[assetId]!,
            solver: solver,
            innerPuzzle: offerMod,
            innerSolution: coinToSolutionDict[coin]!,
          );
        } else {
          if (coin.puzzlehash == OFFER_MOD_V1_HASH) {
            offerMod = OFFER_MOD_V1;
            print("2 Using OFFER V1 ${OFFER_MOD_V1.hash().toHex()}");
          } else {
            // for default offermod is equal to OFFER_MOD
            //offerMod = OFFER_MOD;
          }
          solution = coinToSolutionDict[coin]!;
        }
        final puzzleReveal = (assetId != null)
            ? OuterPuzzleDriver.constructPuzzle(
                constructor: driverDict[assetId]!,
                innerPuzzle: offerMod,
              )
            : offerMod;
        final coinSpend = CoinSpend(
          coin: coin,
          puzzleReveal: puzzleReveal,
          solution: solution,
        );

        completionSpends.add(coinSpend);
      }
    });
    final completionSpendBundle = SpendBundle(coinSpends: completionSpends);

    return completionSpendBundle + bundle;
  }

  Program get _OFFER_PROGRAM {
    return old ? OFFER_MOD_V1 : OFFER_MOD;
  }

  /// Before we serialze this as a SpendBundle, we need to serialze the `requested_payments` as dummy CoinSpends
  SpendBundle toSpendBundle() {
    final aditionalCoinSpends = <CoinSpend>[];
    requestedPayments.forEach((assetId, payments) {
      final puzzleReveal = (assetId == null)
          ? _OFFER_PROGRAM
          : OuterPuzzleDriver.constructPuzzle(
              constructor: driverDict[assetId]!,
              innerPuzzle: _OFFER_PROGRAM,
            );

      List<Program> innerSolutions = [];
      final nonces = cleanDuplicatesValues(payments.map((e) => e.nonce).toList());
      nonces.forEach((nonce) {
        final noncePayments = payments.where((element) => element.nonce == nonce).toList();
        innerSolutions.add(Program.cons(
            Program.fromBytes(nonce),
            Program.list(
              noncePayments
                  .map(
                    (e) => e.toProgram(),
                  )
                  .toList(),
            )));
      });
      final cs = CoinSpend(
        coin: CoinPrototype(
          parentCoinInfo: ZERO_32,
          puzzlehash: puzzleReveal.hash(),
          amount: 0,
        ),
        puzzleReveal: puzzleReveal,
        solution: Program.list(innerSolutions),
      );

      aditionalCoinSpends.add(cs);
    });

    return SpendBundle(coinSpends: aditionalCoinSpends) + this.bundle;
  }

  static Offer fromSpendBundle(SpendBundle bundle) {
    // Because of the `to_spend_bundle` method, we need to parse the dummy CoinSpends as `requested_payments`
    Map<Bytes?, List<NotarizedPayment>> requestedPayments = {};
    Map<Bytes, PuzzleInfo> driverDict = {};
    List<CoinSpend> leftoverCoinSpends = [];
    bool old = false;
    for (CoinSpend coinSpend in bundle.coinSpends) {
      if (!old && coinSpend.toBytes().toHex().contains(OFFER_MOD_V1.toBytes().toHex())) {
        old = true;
      }
      Bytes? assetId;
      final driver = OuterPuzzleDriver.matchPuzzle(coinSpend.puzzleReveal);
      if (driver != null) {
        assetId = OuterPuzzleDriver.createAssetId(driver);
        if (assetId == null) {
          throw Exception("assetId is null");
        }

        driverDict[assetId] = driver;
      } else {
        assetId = null;
      }
      if (coinSpend.coin.parentCoinInfo == ZERO_32) {
        List<NotarizedPayment> notarizedPayments = [];

        for (var paymentGroup in coinSpend.solution.toList()) {
          final nonce = paymentGroup.first().atom;
          final paymentArgsList = paymentGroup.rest().toList();

          notarizedPayments.addAll(paymentArgsList.map((condition) {
            return NotarizedPayment.fromConditionAndNonce(condition: condition, nonce: nonce);
          }).toList());
        }

        requestedPayments[assetId] = notarizedPayments;
      } else {
        leftoverCoinSpends.add(coinSpend);
      }
    }
    final offerBundle = SpendBundle(
      coinSpends: leftoverCoinSpends,
      aggregatedSignature: bundle.aggregatedSignature,
    );

    return Offer(
        requestedPayments: requestedPayments,
        bundle: offerBundle,
        old: old,
        driverDict: driverDict);
  }

  Bytes compress({int? version}) {
    final asSpendBundle = toSpendBundle();
    if (version == null) {
      final mods =
          asSpendBundle.coinSpends.map((e) => e.puzzleReveal.uncurry().program.toBytes()).toList();
      version = max([lowestBestVersion(mods), 5])!;
    }

    return compressObjectWithPuzzles(asSpendBundle.toBytes(), version);
  }

  Bytes get id => toBytes().sha256Hash();

  Bytes toBytes() {
    return toSpendBundle().toBytes();
  }

  String toBench32({String prefix = "offer", int? compressionVersion}) {
    final offerBytes = compress(version: compressionVersion);

    final encoded = OfferSegwitEncoder().convert(Segwit(prefix, offerBytes));
    return encoded;
  }

  static Offer fromBench32(String offerBech32) {
    final bytes = Bytes(OfferSegwitDecoder().convert(offerBech32).program);

    return try_offer_decompression(bytes);
  }

  static Offer try_offer_decompression(Bytes dataBytes) {
    return Offer.fromCompressed(dataBytes);
  }

  static Offer fromCompressed(Bytes compressedBytes) {
    return Offer.fromBytes(decompressObjectWithPuzzles(compressedBytes));
  }

  static Offer fromBytes(Bytes objectBytes) {
    return Offer.fromSpendBundle(SpendBundle.fromBytes(objectBytes));
  }
}

Map<String, dynamic> _keysToStrings(Map<Bytes?, dynamic> dic) {
  final result = <String, dynamic>{};
  dic.forEach((key, value) {
    if (key == null) {
      result["XCH"] = value;
    } else {
      result[key.toHex()] = value;
    }
  });
  return result;
}
