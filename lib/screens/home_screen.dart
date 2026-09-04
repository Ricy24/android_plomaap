import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/cart_model.dart';
import '../models/product.dart';
import 'product_detail_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Servicios PlomApp'),
        backgroundColor: Colors.blueAccent,
        foregroundColor: Colors.white,
        actions: [
          Stack(
            alignment: Alignment.center,
            children: [
              IconButton(
                icon: const Icon(Icons.assignment_turned_in),
                onPressed: () => Navigator.pushNamed(context, '/cart'),
                tooltip: 'Ver mi pedido',
              ),
              Positioned(
                right: 6,
                top: 6,
                child: Consumer<CartModel>(
                  builder: (context, cart, _) => cart.itemCount == 0
                      ? const SizedBox.shrink()
                      : CircleAvatar(
                          radius: 9,
                          backgroundColor: Colors.orange,
                          child: Text('${cart.itemCount}',
                              style: const TextStyle(
                                  color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold)),
                        ),
                ),
              ),
            ],
          ),
        ],
      ),
      body: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            color: Colors.blue.shade50,
            child: Row(
              children: [
                const Icon(Icons.info_outline, color: Colors.blue),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Seleccione los servicios que necesita para su hogar.',
                    style: TextStyle(color: Colors.blue.shade900),
                  ),
                ),
              ],
            ),
          ),
          Expanded(
            child: ListView.builder(
              itemCount: catalogoPlomeria.length,
              itemBuilder: (context, index) {
                final servicio = catalogoPlomeria[index];
                return Card(
                  margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundColor: Colors.blue.shade100,
                      child: Text(servicio.emoji, style: const TextStyle(fontSize: 20)),
                    ),
                    title: Text(servicio.name, style: const TextStyle(fontWeight: FontWeight.bold)),
                    subtitle: Text('\$${servicio.price.toStringAsFixed(0)}'),
                    trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                    onTap: () async {
                      final resultado = await Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => ProductDetailScreen(producto: servicio),
                        ),
                      );
                      if (resultado == 'agregado' && context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text('${servicio.name} añadido al pedido'),
                            backgroundColor: Colors.green,
                            behavior: SnackBarBehavior.floating,
                          ),
                        );
                      }
                    },
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
