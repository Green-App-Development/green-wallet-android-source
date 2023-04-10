
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:flutter_splash_screen_module/pushing_transaction.dart';

void main() {

  runApp(chooseWidget(window.defaultRouteName));
  PushingTransaction().init();

}

Widget chooseWidget(String route) {
  switch (route) {
    // name of the route defined in the host app
    case 'splashRoute':
      return MyFlutterView();

    default:
      return MyFlutterView();
  }
}


class MyFlutterView extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    print('My Flutter view got called');

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: SplashScreen(),
    );
  }
}

class SplashScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: Container(

      ),
    );
  }
}
