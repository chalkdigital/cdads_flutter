// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';

import 'package:cdads_example/main.dart';

void main() {
  testWidgets('Home screen renders all ad-type nav tiles', (WidgetTester tester) async {
    await tester.pumpWidget(const CDAdsExampleApp());

    expect(find.text('CDAds Example'), findsOneWidget);
    expect(find.text('Banner Ads'), findsOneWidget);
    expect(find.text('Interstitial Ad'), findsOneWidget);
    expect(find.text('Rewarded Video'), findsOneWidget);
    expect(find.text('Native Ad'), findsOneWidget);
    expect(find.text('Location Tracking'), findsOneWidget);
  });
}
