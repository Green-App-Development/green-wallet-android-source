import 'package:chia_crypto_utils/chia_crypto_utils.dart';

final mainnetBlockchainNetwork = BlockchainNetwork(
  name: 'mainnet',
  addressPrefix: 'xch',
  aggSigMeExtraData: 'ccd5bb71183532bff220ba46c268991a3ff07eb358e8255a65c30a2dce0e5fbb',
);


final chivesnet = BlockchainNetwork(
    name: 'chivesnet',
    addressPrefix: 'xcc',
    aggSigMeExtraData:
    '69cfa80789667c51428eaf2f2126e6be944462ee5b59b8128e90b9a650f865c1',
    ticker: "xcc",
    precision: 8);

final chivestestnet = BlockchainNetwork(
    name: 'chivestestnet',
    addressPrefix: 'txcc',
    aggSigMeExtraData:
    'ae83525ba8d1dd3f09b277de18ca3e43fc0af20d20c4b3e92ef2a48bd291ccb2',
    ticker: "txcc",
    precision: 8);
