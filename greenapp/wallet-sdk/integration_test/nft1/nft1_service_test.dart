import 'dart:io';

import 'package:chia_crypto_utils/chia_crypto_utils.dart';
import 'package:chia_crypto_utils/src/api/nft1/nft_service.dart';
import 'package:test/test.dart';

Future<void> main() async {
  final fullNodeUtils = FullNodeUtilsWindows(Network.testnet10);
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
  final nftService = NftNodeWalletService(fullNode: fullNode, keychain: keychain);

  List<FullCoin>? nftCoins;

  FullNFTCoinInfo? nftFullCoin;
  test('Get NFT Coins', () async {
    nftCoins = await nftService.getNFTCoins();
    print(nftCoins!);
    expect(nftCoins!, isNotEmpty);
  });

  test('Get full coin for transfer', () async {
    final nftCoin = nftCoins![1];
    nftFullCoin = await nftService.convertFullCoin(nftCoin);
    final nftInfo = nftFullCoin!.toNftCoinInfo();

    final uncurriedNft = UncurriedNFT.uncurry(nftInfo.fullPuzzle);
    if (uncurriedNft.supportDid) {
      expect(nftInfo.minterDid, isNotNull);
    }
    final walletVector = keychain.getWalletVector(uncurriedNft.p2PuzzleHash);
    expect(walletVector, isNotNull);
  });

  test('Get  third full coin with launcher ID', () async {
    final nftId = NftAddress("nft1aqpjrv8vyyq0djhp2s0nzks4qyvh2tf6kq8r20d0plsujdj08waqe5glhc");
    final thirdFullCoin = await nftService.getNFTFullCoinByLauncherId(nftId.toPuzzlehash());

    expect(thirdFullCoin, isNotNull);
  });

  test('Transfer NFT', () async {
    List<CoinPrototype> xchCoins = await fullNode.getCoinsByPuzzleHashes(keychain.puzzlehashes);
    final targePuzzlehash = keychain.puzzlehashes[3];
    final changePuzzlehash = keychain.puzzlehashes[2];
    final response = await nftService.transferNFt(
      targePuzzlehash: targePuzzlehash,
      nftCoinInfo: nftFullCoin!,
      standardCoinsForFee: xchCoins,
      changePuzzlehash: changePuzzlehash,
    );
    expect(response.success, true);
  });
}

class FullNodeUtilsWindows {
  static const String defaultUrl = 'https://localhost:8555';

  final String url;
  final Network network;

  FullNodeUtilsWindows(this.network, {this.url = defaultUrl});

  Bytes get certBytes {
    return _getAuthFileBytes('$sslPath\\private_full_node.crt');
  }

  String get checkNetworkMessage => 'Check if your full node is runing on $network';

  Bytes get keyBytes {
    return _getAuthFileBytes('$sslPath\\private_full_node.key');
  }

  String get sslPath =>
      '${Platform.environment['HOMEPATH']}\\.chia\\mainnet\\config\\ssl\\full_node';

  Future<void> checkIsRunning() async {
    final fullNodeRpc = FullNodeHttpRpc(
      url,
      certBytes: certBytes,
      keyBytes: keyBytes,
    );

    final fullNode = ChiaFullNodeInterface(fullNodeRpc);
    await fullNode.getBlockchainState();
  }

  static Bytes _getAuthFileBytes(String pathToFile) {
    LoggingContext()
      ..info(null, highLog: 'auth file loaded: $pathToFile')
      ..info(null, highLog: 'file contents:')
      ..info(null, highLog: File(pathToFile).readAsStringSync());
    return Bytes(File(pathToFile).readAsBytesSync());
  }
}
