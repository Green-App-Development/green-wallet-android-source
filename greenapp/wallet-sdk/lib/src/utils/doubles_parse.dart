import 'dart:io';

import 'package:intl/intl.dart';
import 'dart:math' as math;

class LocaleProvider {
  List? languages = [];
  String locale = "en";

  LocaleProvider._internal();
  static LocaleProvider instance = LocaleProvider._internal();
}

int decimalCount(double value) {
  var str = value.toString();
  var dot = str.indexOf(".");
  var e = str.indexOf("e", dot + 1);
  if (e < 0) {
    return str.length - (dot + 1);
  }
  // Has an exponent part, something like 1.234e-4. Try to compensate.
  var decimals = e - (dot + 1);
  var exponent = int.parse(str.substring(e + 1));
  decimals -= exponent;
  if (decimals < 0) return 0;
  return decimals;
}

String removeTrailingZeros(double original, String n, String? symbol, int decimals) {
  final value = n.replaceAll(RegExp(r"([.]*0+)(?!.*\d)"), "");
  final last = value.trim().substring(value.length - 1);

  if (decimalCount(original) == decimals) {
    //AppLogger.debug("removeTrailingZeros: ${original}");
    return value.trim() + " $symbol";
  }
  if (last == "." || last == "," || last == "") {
    if (symbol == null) {
      return value.trim() + "0";
    }
    return value.trim() + "0 " + symbol;
  }
  if (symbol == null) {
    return value.trim();
  }
  return value.trim() + " $symbol";
}

String get _getDefaultLocaleName {
  String locale = LocaleProvider.instance.locale;

  if (!NumberFormat.localeExists(locale)) {
    if (locale.contains("es_")) {
      return "es_419";
    }
    return "en_US";
  }
  return locale;
}

extension NumberParsing on String {
  double toRegionalDouble({String? locale}) {
    try {
      final defaultLocale = _getDefaultLocaleName;

      return NumberFormat.decimalPattern(locale ?? defaultLocale).parse(this).toDouble();
    } catch (_) {
      return 0.0;
    }
  }
}

String _toRegionalString(
    {String? locale, required int decimals, required String? symbol, required double value}) {
  final defaultLocale = _getDefaultLocaleName;
  final formatAsset = NumberFormat.simpleCurrency(
    name: "",
    locale: locale ?? defaultLocale,
    decimalDigits: decimals,
  );
  final rawValue = value;
  return removeTrailingZeros(
    rawValue,
    formatAsset.format(rawValue),
    symbol?.toUpperCase(),
    decimals,
  );
}

extension IntDoubleParsing on int {
  String toRegionalString({String? locale, required int decimals, String? symbol}) {
    final rawValue = this / math.pow(10, decimals);
    return _toRegionalString(decimals: decimals, locale: locale, symbol: symbol, value: rawValue);
  }
}

extension DoubleParsing on double {
  String toRegionalString({String? locale, required int decimals, String? symbol}) {
    return _toRegionalString(
      decimals: decimals,
      locale: locale,
      symbol: symbol,
      value: this,
    );
  }
}
