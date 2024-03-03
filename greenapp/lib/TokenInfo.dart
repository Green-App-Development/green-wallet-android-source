class TokenInfo {
  String? assetID;
  int? amount;
  String? type;

  TokenInfo({required this.assetID, required this.amount, required this.type});

  Map<String, dynamic> toJson() {
    return {'assetID': assetID, 'assetAmount': amount, 'type': type};
  }
}
