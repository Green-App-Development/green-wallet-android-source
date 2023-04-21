import 'dart:convert';

import '../../../chia_crypto_utils.dart';

const NFT_HRP = "nft";

/// NFT Info for displaying NFT on the UI
class NFTInfo {
  /// Launcher coin ID
  final Puzzlehash launcherId;

  /// Current NFT coin ID
  final Bytes nftCoinId;

  /// Owner DID
  final Bytes? didOwner;

  /// Percentage of the transaction fee paid to the author, e.g. 1000 = 1%
  final int? royaltyPercentage;

  /// Puzzle hash where royalty will be sent to
  final Address? royaltyAddress;

  /// A list of content URIs
  final List<String> dataUris;

  /// Hash of the content
  final String dataHash;

  /// A list of metadata URIs
  final List<String> metadataUris;

  /// Hash of the metadata
  final String metadataHash;

  /// A list of license URIs
  final List<String> licenseUris;

  /// Hash of the license
  final String licenseHash;

  /// How many NFTs in the current series
  final int seriesTotal;

  /// Number of the current NFT in the series
  final int seriesNumber;

  final Bytes updaterPuzzlehash;

  /// Information saved on the chain in hex
  final String chainInfo;

  /// Block height of the NFT minting
  final int mintHeight;

  /// If the inner puzzle supports DID
  final bool supportsDid;

  final bool pendingTransaction;

  final Puzzlehash p2Puzzlehash;

  final launcherPuzzlehash = singletonLauncherProgram.hash;

  NftAddress get nftAddress => NftAddress.fromPuzzlehash(this.launcherId);

  NFTInfo(
      {required this.launcherId,
      required this.nftCoinId,
      required this.didOwner,
      required this.royaltyPercentage,
      required this.dataUris,
      required this.metadataUris,
      required this.licenseUris,
      required this.dataHash,
      required this.metadataHash,
      required this.licenseHash,
      required this.chainInfo,
      required this.mintHeight,
      required this.pendingTransaction,
      required this.royaltyAddress,
      required this.seriesNumber,
      required this.seriesTotal,
      required this.updaterPuzzlehash,
      required this.p2Puzzlehash,
      required this.supportsDid});

  factory NFTInfo.fromJson(Map<String, dynamic> json, {String prefix = "xch"}) {
    return NFTInfo(
        launcherId: Puzzlehash.fromHex(json['launcher_id'] as String),
        nftCoinId: Bytes.fromHex(json['nft_coin_id'] as String),
        didOwner: Bytes.fromHex(json['did_owner'] as String),
        royaltyPercentage: json['royalty'] as int,
        dataUris: List<String>.from(json['data_uris'] as List),
        dataHash: json['data_hash'] as String,
        metadataUris: List<String>.from(json['metadata_uris'] as List),
        metadataHash: json['metadata_hash'] as String,
        licenseUris: List<String>.from(json['license_uris'] as List),
        licenseHash: json['license_hash'] as String,
        chainInfo: json["chain_info"] as String,
        p2Puzzlehash: Puzzlehash.fromHex(json["p2_puzzle_hash"]),
        mintHeight: json["mint_height"],
        pendingTransaction: json["pending_transaction"],
        royaltyAddress: json["royalty_puzzle_hash"] != null
            ? Address.fromPuzzlehash(Puzzlehash.fromHex(json["royalty_puzzle_hash"]), prefix)
            : null,
        seriesNumber: json['series_number'],
        seriesTotal: json["series_total"],
        supportsDid: json["supports_did"],
        updaterPuzzlehash: Bytes.fromHex(json['updater_puzhash']));
  }

  Map<String, dynamic> toMap() {
    return {
      'launcher_id': launcherId.toHex(),
      'nft_coin_id': nftCoinId.toHex(),
      'did_owner': didOwner?.toHex(),
      'royalty': royaltyPercentage,
      'data_uris': dataUris,
      'data_hash': dataHash,
      'metadata_uris': metadataUris,
      'metadata_hash': metadataHash,
      'license_uris': licenseUris,
      'license_hash': licenseHash,
      "chain_info": chainInfo,
      'mint_height': mintHeight,
      'pending_transaction': pendingTransaction,
      'royalty_puzzle_hash': royaltyAddress,
      "series_number": seriesNumber,
      "series_total": seriesTotal,
      "supports_did": supportsDid,
      "nft_address": nftAddress.address,
      "p2_puzzle_hash": p2Puzzlehash.toHex()
    };
  }

  static NFTInfo fromUncurried(
      {required UncurriedNFT uncurriedNFT,
      required CoinPrototype currentCoin,
      int? mintHeight,
      Coin? genesisCoin,
      String addressPrefix = 'xch'}) {
    final dataUris = <String>[];
    final metaUris = <String>[];
    final licenceUris = <String>[];

    for (var uri in uncurriedNFT.dataUris.toList()) {
      dataUris.add(utf8.decode(uri.atom));
    }
    for (var uri in uncurriedNFT.metaUris.toList()) {
      metaUris.add(utf8.decode(uri.atom));
    }
    for (var uri in uncurriedNFT.licenseUris.toList()) {
      licenceUris.add(utf8.decode(uri.atom));
    }
    print(dataUris);

    if (mintHeight == null) {
      mintHeight = genesisCoin!.spentBlockIndex;
    }
    return NFTInfo(
        launcherId: Puzzlehash(uncurriedNFT.launcherPuzhash.atom),
        p2Puzzlehash: uncurriedNFT.p2Puzzle.hash(),
        nftCoinId: currentCoin.id,
        didOwner: uncurriedNFT.ownerDid,
        royaltyPercentage: uncurriedNFT.tradePricePercentage,
        dataUris: dataUris,
        metadataUris: metaUris,
        licenseUris: licenceUris,
        dataHash: uncurriedNFT.dataHash.atom.toHex(),
        metadataHash: uncurriedNFT.metaHash.atom.toHex(),
        licenseHash: uncurriedNFT.licenseHash.atom.toHex(),
        chainInfo: uncurriedNFT.metadata.toSource(),
        mintHeight: mintHeight,
        pendingTransaction: false,
        royaltyAddress: uncurriedNFT.royaltyPuzzlehash != null
            ? Address.fromPuzzlehash(uncurriedNFT.royaltyPuzzlehash!, addressPrefix)
            : null,
        seriesNumber: uncurriedNFT.seriesNumber.toInt(),
        seriesTotal: uncurriedNFT.seriesTotal.toInt(),
        updaterPuzzlehash: uncurriedNFT.metadataUpdaterHash.atom,
        supportsDid: uncurriedNFT.supportDid);
  }

  @override
  String toString() {
    return "NftInfo ${toMap()}";
  }
}
