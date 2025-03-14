// Copyright 2014 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:io';

import 'package:flutter/material.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';

void main() {
  runApp(const PlatformViewApp());
}

class PlatformViewApp extends StatefulWidget {
  const PlatformViewApp({super.key});

  @override
  PlatformViewAppState createState() => PlatformViewAppState();
}

class PlatformViewAppState extends State<PlatformViewApp> {
  Widget _getBannerWidget() {
    // Test IDs from Admob:
    // https://developers.google.com/admob/ios/test-ads
    // https://developers.google.com/admob/android/test-ads
    final String bannerId =
        Platform.isAndroid
            ? 'ca-app-pub-3940256099942544/6300978111'
            : 'ca-app-pub-3940256099942544/2934735716';
    final BannerAd bannerAd = BannerAd(
      adUnitId: bannerId,
      request: const AdRequest(),
      size: AdSize.banner,
      listener: const BannerAdListener(),
    );
    bannerAd.load();
    return Align(
      alignment: Alignment.bottomCenter,
      // Use 320x50 Admob standard banner size.
      child: SizedBox(width: 320, height: 50, child: AdWidget(ad: bannerAd)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Advanced Layout',
      home: Scaffold(
        appBar: AppBar(title: const Text('Platform View Bottom Ad Banner')),
        body: Column(
          children: <Widget>[
            Expanded(
              child: ListView.builder(
                key: const Key('platform-views-scroll'), // This key is used by the driver test.
                itemCount: 250,
                itemBuilder: (BuildContext context, int index) {
                  return const Card(
                    elevation: 2,
                    child: ListTile(
                      title: Text('Breaking News!'),
                      subtitle: Text(
                        'Huge breaking news! Here is huge and breaking news which is both huge and breaking.',
                      ),
                      leading: FlutterLogo(),
                    ),
                  );
                },
              ),
            ),
            _getBannerWidget(),
          ],
        ),
      ),
    );
  }
}
