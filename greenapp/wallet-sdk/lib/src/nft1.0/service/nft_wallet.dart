import 'dart:typed_data';

import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/core/models/conditions/announcement.dart';
import 'package:chia_crypto_utils/src/core/models/conditions/assert_puzzle_condition.dart';
import 'package:chia_crypto_utils/src/core/models/outer_puzzle.dart';
import 'package:chia_crypto_utils/src/core/service/conditions_utils.dart';
import 'package:chia_crypto_utils/src/offers_ozone/utils/build_keychain.dart'
    show BuildKeychain;
import 'package:tuple/tuple.dart';
//import '../../did/puzzles/did_puzzles.dart' as didPuzzles;

class NftWallet extends BaseWalletService {
  final StandardWalletService standardWalletService = StandardWalletService();

  SpendBundle createTransferSpendBundle({
    required NFTCoinInfo nftCoin,
    required WalletKeychain keychain,
    required Puzzlehash targetPuzzleHash,
    Puzzlehash? changePuzzlehash,
    int fee = 0,
    required List<CoinPrototype> standardCoinsForFee,
  }) {
    print(
      'p2puzzleNew = ${UncurriedNFT.uncurry(nftCoin.fullPuzzle).p2PuzzleHash.toHex()} ',
    );
    return generateSignedSpendBundle(
        payments: [
          Payment(
            nftCoin.coin.amount,
            targetPuzzleHash,
            memos: <Bytes>[
              targetPuzzleHash,
            ],
          )
        ],
        /*  coins: [
          nftCoin.coin
        ], */
        fee: fee,
        changePuzzlehash: changePuzzlehash,
        keychain: keychain,
        nftCoin: nftCoin,
        standardCoinsForFee: standardCoinsForFee,
        newOwner: null,
        newDidInnerhash: null);
  }

  SpendBundle generateSignedSpendBundle({
    required List<Payment> payments,
    // List<CoinPrototype> coins,
    required List<CoinPrototype> standardCoinsForFee,
    required WalletKeychain keychain,
    Puzzlehash? changePuzzlehash,
    List<AssertCoinAnnouncementCondition> coinAnnouncementsToAssert = const [],
    List<AssertPuzzleCondition> puzzleAnnouncementsToAssert = const [],
    int fee = 0,
    Bytes? newOwner,
    Bytes? newDidInnerhash,
    Program? tradePricesList,
    Map<String, String>? metadataUpdate,
    required NFTCoinInfo nftCoin,
    List<SpendBundle>? additionalBundles,
  }) {
    final uncurriedNft = UncurriedNFT.uncurry(nftCoin.fullPuzzle);
    print(
      'p2puzzleNew = ${uncurriedNft.p2PuzzleHash.toHex()} ',
    );
    final generateSpendsTuple = generateUnsignedSpendbundle(
      payments: payments,
      keychain: keychain,
      standardCoinsForFee: standardCoinsForFee,
      changePuzzlehash: changePuzzlehash,
      originId: nftCoin.nftId,
      nftCoin: nftCoin,
      fee: fee,
      newOwner: newOwner,
      newDidInnerhash: newDidInnerhash,
      tradePricesList: tradePricesList,
      metadataUpdate: metadataUpdate,
      coinAnnouncementsToAssert: coinAnnouncementsToAssert,
      puzzleAnnouncementsToAssert: puzzleAnnouncementsToAssert,
    );

    var unsignedSpendBundle = generateSpendsTuple.item1;
    final chiaSpendBundle = generateSpendsTuple.item2;

    SpendBundle spendBundle = _sign(
      unsignedSpendBundle: unsignedSpendBundle,
      keychain: keychain,
    );

    spendBundle =
        SpendBundle.aggregate([spendBundle] + (additionalBundles ?? []));

    if (chiaSpendBundle != null) {
      spendBundle = SpendBundle.aggregate([spendBundle, chiaSpendBundle]);
    }

    return spendBundle;
  }

  SpendBundle _makeStandardSpendBundleForFee({
    required int fee,
    required List<CoinPrototype> standardCoins,
    required WalletKeychain keychain,
    required Puzzlehash? changePuzzlehash,
    List<AssertCoinAnnouncementCondition> coinAnnouncementsToAsset = const [],
  }) {
    assert(
      standardCoins.isNotEmpty,
      'If passing in a fee, you must also pass in standard coins to use for that fee.',
    );

    final totalStandardCoinsValue = standardCoins.fold(
      0,
      (int previousValue, standardCoin) => previousValue + standardCoin.amount,
    );
    assert(
      totalStandardCoinsValue >= fee,
      'Total value of passed in standad coins is not enough to cover fee.',
    );

    return standardWalletService.createSpendBundle(
      payments: [],
      coinsInput: standardCoins,
      changePuzzlehash: changePuzzlehash,
      keychain: keychain,
      fee: fee,
      coinAnnouncementsToAssert: coinAnnouncementsToAsset,
    );
  }

  Tuple2<SpendBundle, SpendBundle?> generateUnsignedSpendbundle({
    required List<Payment> payments,
    // required List<CoinPrototype> coinsInput,
    required WalletKeychain keychain,
    Puzzlehash? changePuzzlehash,
    int fee = 0,
    Bytes? originId,
    List<AssertCoinAnnouncementCondition> coinAnnouncementsToAssert = const [],
    List<AssertPuzzleCondition> puzzleAnnouncementsToAssert = const [],
    required NFTCoinInfo nftCoin,
    required List<CoinPrototype> standardCoinsForFee,
    Map<String, String>? metadataUpdate,
    Bytes? newOwner,
    Bytes? newDidInnerhash,
    Program? tradePricesList,
  }) {
    Set<Bytes> announcementsToMake = {};
    SpendBundle? feeSpendBundle;
    if (fee > 0) {
      announcementsToMake = {nftCoin.coin.id};
      feeSpendBundle = _makeStandardSpendBundleForFee(
          fee: fee,
          standardCoins: standardCoinsForFee,
          keychain: keychain,
          changePuzzlehash: changePuzzlehash);
    }

    Program innerSol = BaseWalletService.makeSolution(
      primaries: payments,
      coinAnnouncements: announcementsToMake,
      coinAnnouncementsToAssert: coinAnnouncementsToAssert,
      puzzleAnnouncementsToAssert: puzzleAnnouncementsToAssert,
    );

    final unft = UncurriedNFT.uncurry(nftCoin.fullPuzzle);
    Program? magicCondition;

    if (unft.supportDid) {
      if (newOwner == null) {
        final walletVector =
            keychain.getWalletVector(payments.first.puzzlehash);
        if (walletVector != null) {
          newOwner = unft.ownerDid;
        }
      }

      magicCondition = Program.list([
        Program.fromInt(-10),
        newOwner != null ? Program.fromBytes(newOwner) : Program.list([]),
        tradePricesList != null ? tradePricesList : Program.list([]),
        newDidInnerhash != null
            ? Program.fromBytes(newDidInnerhash)
            : Program.list([]),
      ]);
    }

    if (metadataUpdate != null) {
      final metadataUpdateListP = <Program>[];
      metadataUpdate.forEach((key, value) {
        metadataUpdateListP.add(
          Program.cons(
            Program.fromBytes(Bytes.fromHex(key)),
            Program.fromBytes(Bytes.fromHex(value)),
          ),
        );
      });
      magicCondition = Program.list([
        Program.fromInt(-24),
        NFT_METADATA_UPDATER,
        Program.list(metadataUpdateListP),
      ]);
    }

    if (magicCondition != null) {
      innerSol = Program.list([
        Program.list([]),
        Program.cons(
          Program.fromInt(1),
          Program.cons(
            magicCondition,
            innerSol.filterAt("rfr"),
          ),
        ),
        Program.list([]),
      ]);
    }
    if (unft.supportDid) {
      innerSol = Program.list([innerSol]);
    }

    final nftLayerSolution = Program.list([innerSol]);
    final lineageProof = nftCoin.lineageProof;
    if (lineageProof == null) {
      throw Exception("nftCoin.lineageProo can't be null");
    }

    final singletonSolution = Program.list([
      lineageProof.toProgram(),
      Program.fromInt(nftCoin.coin.amount),
      nftLayerSolution,
    ]);

    final coinSpend = CoinSpend(
      coin: nftCoin.coin,
      puzzleReveal: nftCoin.fullPuzzle,
      solution: singletonSolution,
    );

    SpendBundle nftSpendBundle = SpendBundle(
      coinSpends: [
        coinSpend,
      ],
    );

    return Tuple2(nftSpendBundle, feeSpendBundle);
  }

  SpendBundle _sign(
      {required SpendBundle unsignedSpendBundle,
      required WalletKeychain keychain,
      List<Puzzlehash>? puzzleHash}) {
    final signatures = <JacobianPoint>[];

    final puzzleHashList = puzzleHash ?? [];
    final keys = <Bytes, PrivateKey>{};

    for (final coinSpend in unsignedSpendBundle.coinSpends) {
      if (puzzleHashList.isEmpty) {
        final uncurriedNft = UncurriedNFT.tryUncurry(coinSpend.puzzleReveal);
        if (uncurriedNft != null) {
          print("Found a NFT state layer to sign");
          puzzleHashList.add(uncurriedNft.p2PuzzleHash);

          print(
            "p2puzzleNew = ${uncurriedNft.p2PuzzleHash.toHex()} ",
          );
          print(Address.fromPuzzlehash(uncurriedNft.p2PuzzleHash, "txch")
              .address);
        }
      }
      for (final ph in puzzleHashList) {
        final coinWalletVector = keychain.getWalletVector(ph);

        final coinPrivateKey = coinWalletVector!.childPrivateKey;
        keys[coinPrivateKey.getG1().toBytes()] = coinPrivateKey;

        final synthSecretKey = calculateSyntheticPrivateKey(coinPrivateKey);
        keys[synthSecretKey.getG1().toBytes()] = synthSecretKey;
      }

      final conditionsResult = conditionsDictForSolution(
        puzzleReveal: coinSpend.puzzleReveal,
        solution: coinSpend.solution,
      );

      if (conditionsResult.item2 != null) {
        final pairs = pkmPairsForConditionsDict(
          conditionsDict: conditionsResult.item2!,
          additionalData: Bytes.fromHex(
            'ccd5bb71183532bff220ba46c268991a3ff07eb358e8255a65c30a2dce0e5fbb',
          ),
          coinName: coinSpend.coin.id,
        );

        for (final pair in pairs) {
          final pk = pair.item1;
          final msg = pair.item2;
          try {
            final sk = keys[pk];
            if (sk != null) {
              print("sign message ${msg.toHex()} }");
              final signature = AugSchemeMPL.sign(sk, msg);
              signatures.add(signature);
            } else {
              throw Exception(
                  "Cant foun sk for ${pk.toHex().substring(0, 5)}...}");
            }
          } catch (e) {
            throw Exception(
                "This spend bundle cannot be signed by the NFT wallet");
          }
        }
      } else {
        throw Exception(conditionsResult.item1);
      }
    }

    final aggregatedSignature = AugSchemeMPL.aggregate(signatures);
    print(aggregatedSignature.toHex());

    return unsignedSpendBundle.addSignature(aggregatedSignature);
  }

  /// Get DID spend with announcement created we need to transfer NFT with did with current inner hash of DID

  /// We also store `did_id` and then iterate to find the did wallet as we'd otherwise have to subscribe to
  /// any changes to DID wallet and storing wallet_id is not guaranteed to be consistent on wallet crash/reset.

  Future<Tuple2<Bytes, SpendBundle>> getDidApprovalInfo({
    required List<Bytes> nftsIds,
    required DidInfo didInfo,
    required WalletKeychain keychain,
  }) async {
    print("Creating announcement from DID for nft_ids: ${nftsIds}");

    final didBundle = await DidWallet().createMessageSpend(
      didInfo,
      keychain: keychain,
      puzzleAnnouncements: nftsIds.toSet(),
    );
    final didInnerhash = didInfo.currentInner!.hash();
    print("Sending DID announcement from puzzle: ${didBundle.removals}");
    return Tuple2(didInnerhash, didBundle);
  }

  Future<Offer> makeNft1Offer(
      {required WalletKeychain keychain,
      required Map<Bytes?, int> offerDict,
      required Map<Bytes, PuzzleInfo> driverDict,
      required Puzzlehash targetPuzzleHash,
      required Map<OfferAssetData?, List<FullCoin>> selectedCoins,
      int fee = 0,
      int? mintCoinAmount,
      Puzzlehash? changePuzzlehash,
      required List<Coin> standardCoinsForFee,
      required bool old}) async {
    final DESIRED_OFFER_MOD = old ? OFFER_MOD_V1 : OFFER_MOD_V2;
    final DESIRED_OFFER_MOD_HASH = old ? OFFER_MOD_V1_HASH : OFFER_MOD_V2_HASH;

    //  First, let's take note of all the royalty enabled NFTs
    final royaltyNftAssetDict = <Bytes, int>{};
    offerDict.forEach((Bytes? assetId, int amount) {
      //  check if asset is an Royalty Enabled NFT
      if (assetId != null &&
          driverDict[assetId]!.checkType(types: [
            AssetType.SINGLETON,
            AssetType.METADATA,
            AssetType.OWNERSHIP,
          ])) {
        driverDict[assetId]!.info["also"]["also"]["owner"] = "()";
        royaltyNftAssetDict[assetId] = amount;
      }
    });
    Map<Bytes?, List<FullCoin>> selectedSimpleCoins = {};
    selectedCoins.forEach((key, value) {
      selectedSimpleCoins[key?.assetId] = value;
    });

    // Then, all of the things that trigger royalties
    Map<Bytes?, int> fungibleAssetDict = {};
    for (var asset in offerDict.keys) {
      var amount = offerDict[asset];
      if (asset == null || driverDict[asset]?['type'] != AssetType.SINGLETON) {
        fungibleAssetDict[asset] = amount!;
      }
    }

    // Let's gather some information about the royalties
    int offerSideRoyaltySplit = 0;
    int requestSideRoyaltySplit = 0;
    royaltyNftAssetDict.forEach((asset, amount) {
      if (amount > 0) {
        requestSideRoyaltySplit++;
      } else if (amount < 0) {
        offerSideRoyaltySplit++;
      }
    });

    List<Tuple2<int, Bytes>> tradePrices = [];
    fungibleAssetDict.forEach((asset, amount) {
      if (amount > 0 && offerSideRoyaltySplit > 0) {
        var settlementPh = asset == null
            ? DESIRED_OFFER_MOD_HASH
            : OuterPuzzleDriver.constructPuzzle(
                constructor: driverDict[asset]!,
                innerPuzzle: DESIRED_OFFER_MOD,
              ).hash();
        tradePrices.add(
            Tuple2((amount ~/ offerSideRoyaltySplit).floor(), settlementPh));
      }
    });

    List<Tuple3<Bytes, Bytes, int>> requiredRoyaltyInfo =
        []; // [(launcher_id, address, percentage)]
    Map<Bytes, int> offeredRoyaltyPercentages = {};

    for (var asset in royaltyNftAssetDict.keys) {
      final amount = royaltyNftAssetDict[asset]!;
      var transferInfo = driverDict[asset]!.also!.also!;

      var royaltyPercentageRaw =
          transferInfo["transfer_program"]["royalty_percentage"];
      if (royaltyPercentageRaw == null) {
        throw Exception(
            "Royalty percentage is not found in the transfer program");
      }
      // clvm encodes large ints as bytes
      int royaltyPercentage;
      if (royaltyPercentageRaw is Bytes) {
        royaltyPercentage = bytesToInt(royaltyPercentageRaw, Endian.big);
      } else if (royaltyPercentageRaw is int) {
        royaltyPercentage = royaltyPercentageRaw;
      } else {
        royaltyPercentage = int.parse(royaltyPercentageRaw);
      }

      if (amount > 0) {
        requiredRoyaltyInfo.add(Tuple3(
          asset,
          Bytes.fromHex(transferInfo["transfer_program"]["royalty_address"]),
          royaltyPercentage,
        ));
      } else {
        offeredRoyaltyPercentages[asset] = royaltyPercentage;
      }
    }

    Map<Bytes?, List<Tuple2<Bytes, Payment>>> royaltyPayments = {};
    for (var asset in fungibleAssetDict.keys) {
      // offered fungible items
      final amount = fungibleAssetDict[asset]!;
      if (amount < 0 && requestSideRoyaltySplit > 0) {
        List<Tuple2<Bytes, Payment>> paymentList = [];
        for (var royaltyInfo in requiredRoyaltyInfo) {
          var launcherId = royaltyInfo.item1;
          var address = Puzzlehash(royaltyInfo.item2);
          var percentage = royaltyInfo.item3;
          int extraRoyaltyAmount =
              ((amount.abs() / requestSideRoyaltySplit).floor() *
                      (percentage / 10000))
                  .floor();
          if (extraRoyaltyAmount == amount.abs()) {
            throw Exception(
                "Amount offered and amount paid in royalties are equal");
          }
          paymentList.add(Tuple2<Bytes, Payment>(
            launcherId,
            Payment(extraRoyaltyAmount, address, memos: <Puzzlehash>[
              address,
            ]),
          ));
        }
        royaltyPayments[asset] = paymentList;
      }
    }
    final p2Ph = targetPuzzleHash;
    Map<Bytes?, List<Payment>> requestedPayments = {};
    offerDict.forEach((asset, amount) {
      if (amount > 0) {
        requestedPayments[asset] = [
          Payment(
            amount,
            p2Ph,
            memos: <Puzzlehash>[
              if (asset != null) p2Ph,
            ],
          )
        ];
      }
    });
    // Find all the coins we're offering
    Map<Bytes?, Set<FullCoin>> offeredCoinsByAsset = {};
    Set<CoinPrototype> allOfferedCoins = {};

    for (var asset in offerDict.keys) {
      var amount = offerDict[asset];

      if (amount! < 0) {
        int royaltyAmount = 0;
        if (royaltyPayments.containsKey(asset)) {
          royaltyAmount = royaltyPayments[asset]!
              .map((p) => p.item2.amount)
              .fold(0, (a, b) => a + b);
        }

        int coinAmountNeeded;
        if (asset == null) {
          coinAmountNeeded = amount.abs() + royaltyAmount + fee;
        } else {
          coinAmountNeeded = amount.abs() + royaltyAmount;
        }

        Set<FullCoin> offeredCoins = selectedSimpleCoins[asset]!.toSet();
        final selectedCoinsAmount =
            offeredCoins.map((e) => e.amount).reduce((a, b) => a + b);
        if (selectedCoinsAmount < coinAmountNeeded) {
          throw Exception(
              "Not enough coins to offer ($selectedCoinsAmount < $coinAmountNeeded)");
        }

        if (offeredCoins.isEmpty) {
          throw Exception(
              "Did not have asset ID ${asset != null ? asset.toHex() : 'XCH'} to offer");
        }

        offeredCoinsByAsset[asset] = offeredCoins;
        final protoCoins =
            offeredCoins.map((e) => e.toCoin().toCoinPrototype()).toList();
        allOfferedCoins.addAll(protoCoins);
      }
    }

    // Notariza los pagos y obtiene los anuncios para el paquete
    Map<Bytes?, List<NotarizedPayment>> notarizedPayments =
        Offer.notarizePayments(
      requestedPayments: requestedPayments,
      coins: allOfferedCoins.toList(),
    );

    final announcementsToAssert = Offer.calculateAnnouncements(
      notarizedPayment: notarizedPayments,
      driverDict: driverDict,
      old: old,
    );

    for (var asset in royaltyPayments.keys) {
      final paymentList = royaltyPayments[asset];
      if (paymentList == null) {
        throw Exception("Payments are null for asset $asset");
      }
      Puzzlehash royaltyPh;
      Program offerPuzzle;
      if (asset == null) {
        // xch offer
        offerPuzzle = DESIRED_OFFER_MOD;
        royaltyPh = DESIRED_OFFER_MOD_HASH;
      } else {
        offerPuzzle = OuterPuzzleDriver.constructPuzzle(
          constructor: driverDict[asset]!,
          innerPuzzle: DESIRED_OFFER_MOD,
        );
        royaltyPh = offerPuzzle.hash();
        paymentList.forEach((item) {
          final payment = item.item2;
          final launcherId = item.item1;
          if (payment.amount > 0 || old) {
            Program p = payment.toProgram();
            announcementsToAssert.add(
              Announcement(
                royaltyPh,
                Program.cons(
                  Program.fromBytes(launcherId),
                  Program.list([p]),
                ).hash(),
              ),
            );
          }
        });
      }
    }
    // Crear todas las transacciones
    List<SpendBundle> allTransactions = [];
    List<SpendBundle> additionalBundles = [];
    // standard paga la tarifa si es posible

    int feeLeftToPay = 0;
    if (offerDict.containsKey(null) && (offerDict[null] ?? 0) < 0) {
      feeLeftToPay = 0;
    } else {
      feeLeftToPay = fee;
    }

    for (var assetId in offerDict.keys) {
      var amount = offerDict[assetId]!;
      if (amount < 0) {
        List<SpendBundle> txs = [];
        BaseWalletService wallet = StandardWalletService();
        if (assetId != null) {
          final type = driverDict[assetId]!["type"];
          if (type == AssetType.SINGLETON) {
            wallet = NftWallet();
          } else {
            wallet = CatWalletService();
          }
        }

        // Enviar todas las monedas a OFFER_MOD
        if (wallet is StandardWalletService) {
          var royPayments =
              royaltyPayments[assetId]?.map((e) => e.item2).toList() ?? [];
          var royPaymentSum = royPayments.isEmpty
              ? 0
              : royPayments.map((p) => p.amount).reduce((a, b) => a + b);
          final coins = offeredCoinsByAsset[assetId];
          final payments = [
            Payment(amount.abs(), DESIRED_OFFER_MOD_HASH),
            if (royPaymentSum > 0 || old)
              Payment(royPaymentSum.abs(), DESIRED_OFFER_MOD_HASH)
          ];

          final standarBundle = wallet.createSpendBundle(
            payments: payments,
            coinsInput: coins!.toList(),
            keychain: keychain,
            fee: fee,
            puzzleAnnouncementsToAssert: announcementsToAssert,
            changePuzzlehash: changePuzzlehash,
          );

          txs = [standarBundle];
        } else if (fungibleAssetDict[assetId] == null && wallet is NftWallet) {
          if (assetId == null) {
            throw Exception("Asset id is null");
          }
          final tradePriceList = <Program>[];
          for (var price in tradePrices) {
            if ((price.item1 * (offeredRoyaltyPercentages[assetId]! / 10000))
                        .floor !=
                    0 ||
                old) {
              tradePriceList.add(Program.list([
                Program.fromInt(price.item1),
                Program.fromBytes(
                  price.item2,
                ),
              ]));
            }
          }
          final payments = [
            Payment(amount.abs(), DESIRED_OFFER_MOD_HASH),
          ];
          final nftBundles = wallet.generateSignedSpendBundle(
            payments: payments,
            nftCoin: (selectedCoins[OfferAssetData.singletonNft(
              launcherPuzhash: assetId,
            )]!
                    .first as FullNFTCoinInfo)
                .toNftCoinInfo(),
            standardCoinsForFee: standardCoinsForFee,
            fee: feeLeftToPay,
            keychain: keychain,
            tradePricesList: Program.list(tradePriceList),
            puzzleAnnouncementsToAssert: announcementsToAssert,
            changePuzzlehash: changePuzzlehash,
          );

          txs = [nftBundles];
        } else if (wallet is CatWalletService) {
          List<Payment> catPayments = [];
          if (royaltyPayments[assetId] != null) {
            var royPayments =
                royaltyPayments[assetId]?.map((e) => e.item2).toList() ?? [];
            var royPaymentSum =
                royPayments.map((p) => p.amount).reduce((a, b) => a + b);

            catPayments.add(
              Payment(
                royPaymentSum.abs(),
                DESIRED_OFFER_MOD_HASH,
              ),
            );
          }
          final offerAssetData = OfferAssetData.cat(tailHash: assetId!);
          final catCoins =
              selectedCoins[offerAssetData]!.map((e) => e.toCatCoin()).toList();
          final catBundle = CatWalletService().createSpendBundle(
            payments: [
              Payment(amount.abs(), DESIRED_OFFER_MOD_HASH),
              ...catPayments,
            ],
            catCoinsInput: catCoins,
            keychain: keychain,
            fee: feeLeftToPay,
            standardCoinsForFee: standardCoinsForFee,
            puzzleAnnouncementsToAssert: announcementsToAssert,
            changePuzzlehash: changePuzzlehash,
          );

          txs = [catBundle];
        }
        allTransactions.addAll(txs);
        feeLeftToPay = 0;

        // Then, adding in the spends for the royalty offer mod
        if (fungibleAssetDict.containsKey(assetId)) {
          // Create a coin_spend for the royalty payout from OFFER MOD

          // Skip it if we're paying 0 royalties
          var payments = royaltyPayments[assetId] ?? [];
          if ((!old) && payments.isEmpty) {
            continue;
          }
          final paymentsSum =
              payments.map((p) => p.item2.amount).reduce((a, b) => a + b);
          if ((paymentsSum == 0)) {
            continue;
          }

          // We cannot create coins with the same puzzle hash and amount
          // So if there's multiple NFTs with the same royalty puzhash/percentage, we must create multiple
          // generations of offer coins
          CoinPrototype? royaltyCoin;
          CoinSpend? parentSpend;
          while (true) {
            List<Tuple2<Bytes, Payment>> duplicatePayments = [];
            List<Tuple2<Bytes, Payment>> dedupedPaymentList = [];
            payments.forEach((item) {
              final launcherId = item.item1;
              final payment = item.item2;
              if (dedupedPaymentList
                  .any((dedupedPayment) => dedupedPayment.item2 == payment)) {
                duplicatePayments.add(Tuple2(launcherId, payment));
              } else {
                dedupedPaymentList.add(Tuple2(launcherId, payment));
              }
            });

            // ((nft_launcher_id . ((ROYALTY_ADDRESS, royalty_amount, memos) ...)))
            final innerRoyaltySolList = dedupedPaymentList.map((item) {
              final launcherId = item.item1;
              final payment = item.item2;
              return Program.cons(
                Program.fromBytes(launcherId),
                Program.list([payment.toProgram()]),
              );
            }).toList();
            Program innerRoyaltySol = Program.list(innerRoyaltySolList);

            if (duplicatePayments.isNotEmpty) {
              final duplicatePaymentsSum = duplicatePayments.fold(
                  0, (sum, payment) => sum + payment.item2.amount);
              innerRoyaltySol = Program.cons(
                  Program.cons(
                    Program.nil,
                    Program.list([
                      Payment(duplicatePaymentsSum, Offer.ph(old)).toProgram(),
                    ]),
                  ),
                  innerRoyaltySol);
            }
            Program offerPuzzle;
            Puzzlehash royaltyPh;
            if (assetId == null) {
              // xch offer
              offerPuzzle = DESIRED_OFFER_MOD;
              royaltyPh = DESIRED_OFFER_MOD_HASH;
            } else {
              offerPuzzle = OuterPuzzleDriver.constructPuzzle(
                  constructor: driverDict[assetId]!,
                  innerPuzzle: DESIRED_OFFER_MOD);
              royaltyPh = offerPuzzle.hash();
            }
            if (royaltyCoin == null) {
              for (var tx in txs) {
                final spendBundle = tx;

                for (var coin in spendBundle.additions) {
                  int royaltyPaymentAmount = payments
                      .map((e) => e.item2)
                      .fold(0, (sum, payment) => sum + payment.amount);
                  if (coin.amount == royaltyPaymentAmount &&
                      coin.puzzlehash == royaltyPh) {
                    royaltyCoin = coin;
                    parentSpend = spendBundle.coinSpends
                        .where(
                          (cs) => cs.coin.id == royaltyCoin!.parentCoinInfo,
                        )
                        .first;
                    break;
                  }
                }
                if (royaltyCoin != null) {
                  break;
                }
              }
            }
            if (royaltyCoin == null) {
              throw Exception("Could not find royalty coin");
            }
            if (parentSpend == null) {
              throw Exception("Could not find royalty parent spend");
            }
            Program royaltySol;
            if (assetId == null) {
              // If XCH
              royaltySol = innerRoyaltySol;
            } else {
              // Call our drivers to solve the puzzle
              String royaltyCoinHex = "0x" +
                  royaltyCoin.parentCoinInfo.toHex() +
                  royaltyCoin.puzzlehash.toHex() +
                  Bytes(intTo64Bits(royaltyCoin.amount)).toHex();
              String parentSpendHex = "0x" + parentSpend.toBytes().toHex();
              Solver solver = Solver({
                "coin": royaltyCoinHex,
                "parentSpend": parentSpendHex,
                "siblings": "()",
                "siblingSpends": "()",
                "siblingPuzzles": "()",
                "siblingSolutions": "()",
              });
              royaltySol = OuterPuzzleDriver.solvePuzzle(
                  constructor: driverDict[assetId]!,
                  solver: solver,
                  innerPuzzle: DESIRED_OFFER_MOD,
                  innerSolution: innerRoyaltySol);
            }

            CoinSpend newCoinSpend = CoinSpend(
                coin: royaltyCoin,
                puzzleReveal: offerPuzzle,
                solution: royaltySol);
            additionalBundles.add(SpendBundle(coinSpends: [newCoinSpend]));

            if (duplicatePayments.isNotEmpty) {
              payments = duplicatePayments;
              royaltyCoin = newCoinSpend.additions
                  .where((c) => c.puzzlehash == royaltyPh)
                  .first;
              parentSpend = newCoinSpend;
              continue;
            } else {
              break;
            }
          }
        }
      }
    }
    // Finalmente, ensambla los registros de transacciones correctamente
    SpendBundle txsBundle = SpendBundle.aggregate(allTransactions);
    SpendBundle aggregateBundle =
        SpendBundle.aggregate([txsBundle, ...additionalBundles]);

    Offer offer = Offer(
        requestedPayments: notarizedPayments,
        bundle: aggregateBundle,
        driverDict: driverDict,
        old: old);
    return offer;
  }

  Future<PuzzleInfo> getPuzzleInfo(NFTCoinInfo nftCoin) async {
    PuzzleInfo? puzzleInfo = OuterPuzzleDriver.matchPuzzle(nftCoin.fullPuzzle);
    if (puzzleInfo == null) {
      throw ArgumentError(
          "Internal Error: NFT wallet is tracking a non NFT coin");
    } else {
      return puzzleInfo;
    }
  }

  /// From FullCoin It prepare the FullNftCoinInfo with the updated data for transfer,
  /// if you want use It only for requested NFT, you can return null the the buildKeychain
  /// for create the last spend fullNftCoinInfo
  Future<Tuple3<FullNFTCoinInfo, Program, WalletKeychain?>> getNFTFullCoinInfo(
      FullCoin nftFullCoin,
      {BuildKeychain? buildKeychain}) async {
    final coin = nftFullCoin.coin;
    final coinSpend = nftFullCoin.parentCoinSpend!;

    final nftUncurried = UncurriedNFT.uncurry(coinSpend.puzzleReveal);

    final nftInfo = NFTInfo.fromUncurried(
      uncurriedNFT: nftUncurried,
      currentCoin: coin,
      mintHeight: nftFullCoin.coin.confirmedBlockIndex,
    );

    final data = NftService().getMetadataAndPhs(
      nftUncurried,
      coinSpend.solution,
    );
    final metadata = data.item1;
    final p2PuzzleHash = Puzzlehash(data.item2);

    WalletKeychain? keychainForNft;

    Program innerPuzzle = nftUncurried.p2Puzzle;
    if (buildKeychain != null) {
      keychainForNft = await buildKeychain({p2PuzzleHash});
      final vector = keychainForNft?.getWalletVector(p2PuzzleHash);
      if (vector != null) {
        innerPuzzle = getPuzzleFromPk(vector.childPublicKey);
      } else {
        //innerPuzzle = nftUncurried.p2Puzzle;
        print("User parent spend innerPuzzle for ${nftInfo.launcherId}");
      }
    }

    if (nftUncurried.supportDid) {
      innerPuzzle = NftService().recurryNftPuzzle(
        unft: nftUncurried,
        solution: coinSpend.solution,
        newInnerPuzzle: innerPuzzle,
      );
    }

    Program fullPuzzle = NftService.createFullPuzzle(
      singletonId: nftUncurried.singletonLauncherId.atom,
      metadata: metadata,
      metadataUpdaterHash: nftUncurried.metadataUpdaterHash.atom,
      innerPuzzle: innerPuzzle,
    );

    final nftCoin = FullNFTCoinInfo(
      coin: coin,
      fullPuzzle: fullPuzzle,
      latestHeight: coin.confirmedBlockIndex,
      mintHeight: nftInfo.mintHeight,
      nftId: nftUncurried.singletonLauncherId.atom,
      pendingTransaction: false,
      parentCoinSpend: nftFullCoin.parentCoinSpend,
      confirmedBlockIndex: nftFullCoin.coin.confirmedBlockIndex,
      ownerDid: nftUncurried.ownerDid,
      minterDid: null,
      nftLineageProof: LineageProof(
        amount: coinSpend.coin.amount,
        innerPuzzleHash: nftUncurried.nftStateLayer.hash(),
        parentName: Puzzlehash(coinSpend.coin.parentCoinInfo),
      ),
      spentBlockIndex: nftFullCoin.coin.spentBlockIndex,
    );
    return Tuple3(nftCoin, fullPuzzle, keychainForNft);
  }
}
