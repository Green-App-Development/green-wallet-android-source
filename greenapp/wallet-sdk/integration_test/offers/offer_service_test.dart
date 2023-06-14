import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/api/nft1/nft_service.dart';
import 'package:chia_crypto_utils/src/api/offers2/offers_service.dart';
import 'package:test/test.dart';

import '../nft1/nft1_service_test.dart';

const testNFTOfferData =
    'offer1qqph3wlykhv8jcmqvpsxygqqwc7hynr6hum6e0mnf72sn7uvvkpt68eyumkhelprk0adeg42nlelk2mpafsyjlm5pqpj3dkq0l0mj6uhreyk28qfnsvxk2cht53vavx6g0hfnfa0j0shzy7k4fwup2n0gx5f87h424ekallhz9nf77fx8a4h9gaam5mla6cvhlvcgnly97essalumlrd9gmwr8qyppzjjpp4sy6wmtxl8n5jl3e4hrue70atv0l3ux843t0we0y0tm0dwp5qc220h26mf9nmd0aa9d4udaca7x0dd6klnvhym8kg8yvklwhzwmj6eggmt4slr2xceupzd36pqp4eeqdpmpqerkzpj75rxzgtf0dq0624ru87hvt3lhpmehymd62wjta7ugl8z0azxnrzckkkufalmpfr8lllq2nefye99v0r8w66nhmscnxfw2f0wklm2s4r0w7tfq5wcckf5eu37r3w0rl6z4xfxvswzsx6ka72xdv8jmt4nkg427w75h4hun3r90e9vu8a4t5yt2z4c7yvpm7aqfsp3s806rxl5y0hpqjlkrq8wlncn9lusy3mq7phm0marwv09kjey4tj6jekt8mu8spljeulyg2chc506l7qyw36ff9qmp99l0egk9wz70dnyj3nf2d58z6jswfl4ujrtt6y9uun747lx7c6stpn97crfdfvx5j470e045jrutfny764s0ehxye570al9glj74d0yqh366rhccp8z4v3rqhgd24xh77eeyah2e8aj94ktd665uxtxtnwme2mngxu884hqgdl8cr7r2jg04j5nsrk4hshjlqx45g9l7atelw5zu7e4ett0l7deh9q4td60nhvm2m8dugyn0eav2uhva0t4h5d5k2ps809gfysegartj36xhqymneh7ay530v83gk09qt6fmpmhta9te0625xdyg027utecw80ws4dlcnt3d6vtn7ds7pg4mfkm4f40qegsw2kx69a4ngargk36x3vz0j6qjxjh7h4yct8c64uywak9huelux2ltct4avf2lt868d0km7lcu98y932dpxrphrpl5vfdfgcy98vd943smk75ndscydedpduzgld5x3lv7qchsynvnm7myswpuulanvshythv2htas6enda8ankt8ge5g0l25ng3pevxkf34ma2pw43xx6s99lll9av89gwwad59fwt57kzjlh5v9d825njlv2gmu3j3eyll07trmmm2h0w0luh4qz4j6r0kgxxlwl804d7fc458vdatc7y7re6l44l82s08funskhax8lvxtgypzjcg8qvdxvgqx052kkqdqspzysnsp7eyq32cynp8c6u0lahlzaxvu7j5yaenmatrl00z0dakevmwunegxllrmt0nsfrwuhdwhvqehxzrvyvhdy2y0l3af6v8wl66lm7njherlmnn9ps9dyvll4zlarhg5rrzxpslnwxhxh438kl6xempwq4kakawjvta7umsrtrl67n39jlw8uyylrz9xr9kshpta7c932a80e8mmeywel85d5pl0avs6vwzrnhz0wnfwkdkjhx85rgqxnkh9cqvdfng0';

Future<void> main() async {
  final fullNodeUtils = FullNodeUtilsWindows(Network.mainnet, url: 'https://chia.green-app.io/full-node');
  try {
    await fullNodeUtils.checkIsRunning();
  } catch (e) {
    print(e);
    return;
  }

  final mnemonic =
      'kitten seat dial receive water peasant obvious tuition rifle ethics often improve mutual invest gospel unaware cushion trigger credit scare critic edge digital valid'
          .split(' ');

  final keychainSecret = KeychainCoreSecret.fromMnemonic(mnemonic);
  final walletsSetList = <WalletSet>[];
  for (var i = 0; i < 5; i++) {
    final set1 = WalletSet.fromPrivateKey(keychainSecret.masterPrivateKey, i);
    walletsSetList.add(set1);
  }

  final keychain = WalletKeychain.fromWalletSets(walletsSetList);

  ChiaNetworkContextWrapper().registerNetworkContext(Network.testnet10);
  final fullNodeRpc = FullNodeHttpRpc(
    fullNodeUtils.url,
    certBytes: fullNodeUtils.certBytes,
    keyBytes: fullNodeUtils.keyBytes,
  );

  final fullNode = ChiaFullNodeInterface(fullNodeRpc);
  final nftService =
      NftNodeWalletService(fullNode: fullNode, keychain: keychain);
  final offerService = OffersService(fullNode: fullNode, keychain: keychain);
  final standartWalletService = StandardWalletService();

  List<FullCoin>? nftCoins;
  List<FullCoin>? xchCoins;

  String? offeringNFTStr;

  test('Get NFT, XCH Coins', () async {
    nftCoins = await nftService.getNFTCoins();
    xchCoins = standartWalletService.convertXchCoinsToFull(
      await fullNode.getCoinsByPuzzleHashes(keychain.puzzlehashes),
    );
    expect(nftCoins!, isNotEmpty);
  });

  test('Analized offers', () async {
    final offer = Offer.fromBench32(testNFTOfferData);
    final summary = offer.summary();
    print(summary);
    expect(offer.requestedPayments.length, 1);
    expect(offer.getOfferedAmounts().length, 1);

    final analized = offerService.analizeOffer(
        fee: 0,
        targetPuzzleHash: Puzzlehash.zeros(),
        changePuzzlehash: Puzzlehash.zeros(),
        offer: offer);
    expect(analized, isNotNull);
  });

  test('Create offer for offer the first NFT', () async {
    final firstNft = nftCoins!.first;

    final nftFullCoinInfo = await nftService.convertFullCoin(firstNft);
    final changePh = keychain.puzzlehashes[2];
    final targePh = keychain.puzzlehashes[3];

    // Request 100000000000 mojos for user NFT
    final offer = await offerService.createOffer(
      offerredAmounts: {
        OfferAssetData.singletonNft(
          launcherPuzhash: nftFullCoinInfo.launcherId,
        ): -1
      },
      requesteAmounts: {
        null: [100000000000],
      },

      // Don't use XCH coins for can use it in response offer below
      //coins: [nftFullCoinInfo, ...xchCoins!],
      coins: [nftFullCoinInfo],
      changePuzzlehash: changePh,
      targetPuzzleHash: targePh,
      //fee: 1000000,
    );
    final summary = offer.summary();
    print(summary);
    final requested = offer.requestedPayments;
    final requestedAmount = requested.values.fold<int>(
        0,
        (a, b) =>
            a +
            b.fold(
                0, (previousValue, element) => previousValue + element.amount));
    expect(requested.length, 1);
    expect(requestedAmount, 100000000000);

    final offered = offer.getOfferedAmounts();
    final offeredAmount = offered.values.fold<int>(0, (a, b) => a + b);
    expect(offered.length, 1);
    expect(offeredAmount, 1);

    offeringNFTStr = offer.toBench32();
    expect(offeringNFTStr, isNotNull);
  });

  test('Create offer for request third nft', () async {
    final nftAddress = NftAddress(
        "nft1aqpjrv8vyyq0djhp2s0nzks4qyvh2tf6kq8r20d0plsujdj08waqe5glhc");

    final changePh = keychain.puzzlehashes[2];
    final targePh = keychain.puzzlehashes[3];
    final offer = await offerService.createOffer(
      offerredAmounts: {
        null: -100000000000,
      },
      requesteAmounts: {
        OfferAssetData.singletonNft(launcherPuzhash: nftAddress.toPuzzlehash()):
            [1]
      },
      coins: [...xchCoins!],
      changePuzzlehash: changePh,
      targetPuzzleHash: targePh,
      fee: 1000000,
    );
    final summary = offer.summary();
    print(summary);
    final requested = offer.requestedPayments;
    final requestedAmount = requested.values.fold<int>(
        0,
        (a, b) =>
            a +
            b.fold(
                0, (previousValue, element) => previousValue + element.amount));
    expect(requested.length, 1);
    expect(requestedAmount, 1);

    final offered = offer.getOfferedAmounts();
    final offeredAmount = offered.values.fold<int>(0, (a, b) => a + b);
    expect(offered.length, 1);
    expect(offeredAmount, 100000000000);
  });

  test('Response offerred NFT Offer', () async {
    final offer = Offer.fromBench32(offeringNFTStr!);

    final changePh = keychain.puzzlehashes[2];
    final targePh = keychain.puzzlehashes[3];
    final responseResult = await offerService.responseOffer(
        fee: 1000000,
        targetPuzzleHash: targePh,
        offer: offer,
        changePuzzlehash: changePh,
        coinsToUse: xchCoins!);
    expect(responseResult.item1.success, true);
  });
}
