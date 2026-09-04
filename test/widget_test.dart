import 'package:flutter_test/flutter_test.dart';
import 'package:plomaap/main.dart';

void main() {
  testWidgets('PlomApp smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const PlomApp());
    expect(find.text('PlomApp'), findsOneWidget);
  });
}
