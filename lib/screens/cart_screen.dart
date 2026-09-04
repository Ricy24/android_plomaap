import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/cart_model.dart';

class CartScreen extends StatelessWidget {
  const CartScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Resumen de Pedido'),
        actions: [
          Consumer<CartModel>(
            builder: (context, cart, _) => cart.items.isNotEmpty
                ? IconButton(
                    icon: const Icon(Icons.delete_sweep),
                    onPressed: () => _confirmarVaciado(context, cart),
                    tooltip: 'Vaciar pedido',
                  )
                : const SizedBox.shrink(),
          ),
        ],
      ),
      body: Consumer<CartModel>(
        builder: (context, cart, _) {
          if (cart.items.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.plumbing, size: 80, color: Colors.grey.shade300),
                  const SizedBox(height: 16),
                  const Text('No has seleccionado servicios aún',
                      style: TextStyle(fontSize: 16, color: Colors.grey)),
                  const SizedBox(height: 24),
                  ElevatedButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('Volver al catálogo'),
                  ),
                ],
              ),
            );
          }
          return Column(
            children: [
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.all(12),
                  children: cart.items.entries.map((e) {
                    final servicio = e.key;
                    final cantidad = e.value;
                    return Card(
                      child: ListTile(
                        leading: Text(servicio.emoji, style: const TextStyle(fontSize: 24)),
                        title: Text(servicio.name),
                        subtitle: Text('Cantidad: $cantidad · \$${(servicio.price * cantidad).toStringAsFixed(0)}'),
                        trailing: IconButton(
                          icon: const Icon(Icons.remove_circle_outline, color: Colors.redAccent),
                          onPressed: () => cart.remove(servicio),
                        ),
                      ),
                    );
                  }).toList(),
                ),
              ),
              Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: Colors.white,
                  boxShadow: [
                    BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, -5))
                  ],
                ),
                child: SafeArea(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('Total a pagar:',
                              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                          Text('\$${cart.total.toStringAsFixed(0)}',
                              style: const TextStyle(
                                  fontSize: 24, fontWeight: FontWeight.bold, color: Colors.blueAccent)),
                        ],
                      ),
                      const SizedBox(height: 24),
                      SizedBox(
                        width: double.infinity,
                        height: 54,
                        child: ElevatedButton(
                          onPressed: () {
                            // Aquí iría la lógica de pago
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Procesando solicitud...')),
                            );
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.green,
                            foregroundColor: Colors.white,
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          ),
                          child: const Text('Confirmar Pedido', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  void _confirmarVaciado(BuildContext context, CartModel cart) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('¿Vaciar pedido?'),
        content: const Text('Se eliminarán todos los servicios seleccionados.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
          TextButton(
            onPressed: () {
              cart.clear();
              Navigator.pop(context);
            },
            child: const Text('Vaciar', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
  }
}
