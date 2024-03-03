import 'dart:convert';

class FlutterToken {
  final int amount;
  final String assetID;
  final String fromAddress;
  final String type;

  FlutterToken({
    required this.amount,
    required this.assetID,
    required this.fromAddress,
    required this.type,
  });

  factory FlutterToken.fromJson(Map<String, dynamic> json) {
    return FlutterToken(
      amount: json['amount'],
      assetID: json['assetID'],
      fromAddress: json['fromAddress'],
      type: json['type'],
    );
  }

  @override
  String toString() {
    return 'FlutterToken{amount: $amount, assetID: $assetID, fromAddress: $fromAddress, type: $type}';
  }

}

List<FlutterToken> parseFlutterTokenJsonString(String jsonString) {
  List<dynamic> jsonList = json.decode(jsonString);
  List<FlutterToken> flutterTokens = [];

  for (var jsonToken in jsonList) {
    flutterTokens.add(FlutterToken.fromJson(jsonToken));
  }

  return flutterTokens;
}

