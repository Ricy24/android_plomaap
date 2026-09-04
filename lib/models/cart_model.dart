import 'package:flutter/foundation.dart';
import 'product.dart';

class CartModel extends ChangeNotifier {
  final Map<Product, int> _items = {};

  Map<Product, int> get items => _items;

  int get itemCount =>
      _items.values.fold(0, (total, qty) => total + qty);

  double get total => _items.entries
      .fold(0, (sum, e) => sum + e.key.price * e.value);

  void add(Product product) {
    _items.update(product, (qty) => qty + 1, ifAbsent: () => 1);
    notifyListeners();
  }

  void remove(Product product) {
    if (!_items.containsKey(product)) return;
    if (_items[product]! > 1) {
      _items[product] = _items[product]! - 1;
    } else {
      _items.remove(product);
    }
    notifyListeners();
  }

  void clear() {
    _items.clear();
    notifyListeners();
  }
}
