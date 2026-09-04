class Product {
  final String id;
  final String name;
  final double price;
  final String emoji;
  final String description;

  const Product({
    required this.id,
    required this.name,
    required this.price,
    required this.emoji,
    required this.description,
  });
}

const List<Product> catalogoPlomeria = [
  Product(
    id: 's1',
    name: 'Reparación de Fugas',
    price: 45000,
    emoji: '💧',
    description: 'Localización y sellado de fugas en tuberías de agua potable o desagüe.',
  ),
  Product(
    id: 's2',
    name: 'Instalación de Grifería',
    price: 35000,
    emoji: '🚰',
    description: 'Montaje de grifos para lavamanos, cocinas o duchas.',
  ),
  Product(
    id: 's3',
    name: 'Limpieza de Tuberías',
    price: 60000,
    emoji: '🚿',
    description: 'Desobstrucción profunda de cañerías con equipo especializado.',
  ),
  Product(
    id: 's4',
    name: 'Instalación de Calentador',
    price: 120000,
    emoji: '🔥',
    description: 'Instalación y configuración de calentadores a gas o eléctricos.',
  ),
  Product(
    id: 's5',
    name: 'Mantenimiento de Tanque',
    price: 85000,
    emoji: '🏢',
    description: 'Lavado y desinfección de tanques de reserva de agua.',
  ),
];
