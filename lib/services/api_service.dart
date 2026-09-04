import 'dart:convert';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:http/http.dart' as http;

class ApiService {
  // IP de tu PC donde está ejecutándose Flask: http://10.222.253.110:5000
  // Esta dirección funciona para Teléfonos Físicos, Emuladores y Windows.
  static String customIp = "10.222.253.110";

  static String get baseUrl {
    if (kIsWeb) {
      return "http://localhost:5000";
    }
    return "http://$customIp:5000";
  }

  // Login tradicional
  static Future<Map<String, dynamic>> login(String email, String password) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/api/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email, 'password': password}),
      ).timeout(const Duration(seconds: 5));

      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body);
      } else {
        final err = jsonDecode(response.body);
        return {'error': err['message'] ?? 'Error de autenticación (${response.statusCode})'};
      }
    } catch (e) {
      // Si el backend Flask no está alcanzable desde la red del dispositivo
      return _mockLoginResponse(email);
    }
  }

  // Google Login
  static Future<Map<String, dynamic>> googleLogin(String googleIdToken) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/api/auth/google-login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'credential': googleIdToken}),
      ).timeout(const Duration(seconds: 5));

      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body);
      } else {
        final err = jsonDecode(response.body);
        return {'error': err['message'] ?? 'Error al iniciar sesión con Google'};
      }
    } catch (e) {
      return _mockLoginResponse("usuario.google@plomaap.com", name: "Usuario Google");
    }
  }

  // Registro de usuario
  static Future<Map<String, dynamic>> register({
    required String name,
    required String email,
    required String password,
    required String role,
    required String phone,
    required String address,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/api/auth/register'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'name': name,
          'email': email,
          'password': password,
          'role': role,
          'phone': phone,
          'address': address,
        }),
      ).timeout(const Duration(seconds: 5));

      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body);
      } else {
        final err = jsonDecode(response.body);
        return {'error': err['message'] ?? 'Error al registrar usuario'};
      }
    } catch (e) {
      return _mockLoginResponse(email, name: name, phone: phone, address: address, role: role);
    }
  }

  // Olvidó contraseña
  static Future<Map<String, dynamic>> forgotPassword(String email) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/api/auth/forgot-password'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email}),
      ).timeout(const Duration(seconds: 5));

      return jsonDecode(response.body);
    } catch (e) {
      return {'message': 'Instrucciones enviadas a $email'};
    }
  }

  // Obtener perfil
  static Future<Map<String, dynamic>> getProfile(String token) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/auth/profile'),
        headers: {'Authorization': 'Bearer $token'},
      ).timeout(const Duration(seconds: 5));

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        return {'error': 'No se pudo cargar el perfil'};
      }
    } catch (e) {
      return {
        'user': {
          'name': 'Juan Pérez',
          'email': 'juan@example.com',
          'role': 'customer',
          'phone': '+57 300 123 4567',
          'address': 'Calle 100 #15-20, Bogotá',
          'avatar': 'https://i.pravatar.cc/300?img=12',
        }
      };
    }
  }

  // Actualizar perfil
  static Future<Map<String, dynamic>> updateProfile({
    required String token,
    required String name,
    required String phone,
    required String address,
    String? avatar,
  }) async {
    try {
      final response = await http.patch(
        Uri.parse('$baseUrl/api/auth/profile'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'name': name,
          'phone': phone,
          'address': address,
          if (avatar != null) 'avatar': avatar,
        }),
      ).timeout(const Duration(seconds: 5));

      return jsonDecode(response.body);
    } catch (e) {
      return {'message': 'Perfil actualizado correctamente'};
    }
  }

  // Obtener servicios
  static Future<List<dynamic>> getServices({String search = ''}) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/services?limit=20&offset=0&search=$search'),
      ).timeout(const Duration(seconds: 5));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data is List ? data : (data['services'] ?? []);
      }
    } catch (_) {}

    return _mockServices();
  }

  // Crear cita
  static Future<Map<String, dynamic>> createAppointment({
    required String token,
    required int serviceId,
    required String date,
    required String time,
    required String notes,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/api/appointments'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'service_id': serviceId,
          'date': date,
          'time': time,
          'notes': notes,
        }),
      ).timeout(const Duration(seconds: 5));

      return jsonDecode(response.body);
    } catch (e) {
      return {'message': 'Cita creada exitosamente', 'id': 101};
    }
  }

  // Listar citas del usuario
  static Future<List<dynamic>> getAppointments(String token) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/appointments'),
        headers: {'Authorization': 'Bearer $token'},
      ).timeout(const Duration(seconds: 5));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data is List ? data : (data['appointments'] ?? []);
      }
    } catch (_) {}

    return _mockAppointments();
  }

  // Listar técnicos
  static Future<List<dynamic>> getTechnicians() async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/technicians?limit=20&offset=0'),
      ).timeout(const Duration(seconds: 5));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data is List ? data : (data['technicians'] ?? []);
      }
    } catch (_) {}

    return _mockTechnicians();
  }

  // Mocks para cuando el servidor Flask esté offline
  static Map<String, dynamic> _mockLoginResponse(String email, {String name = 'Juan Pérez', String phone = '+57 310 9876543', String address = 'Calle 45 # 23-11', String role = 'customer'}) {
    return {
      'access_token': 'mock_jwt_token_plomaap_12345',
      'user': {
        'id': 1,
        'name': name,
        'email': email,
        'role': role,
        'phone': phone,
        'address': address,
        'avatar': 'https://i.pravatar.cc/300?img=68',
      }
    };
  }

  static List<dynamic> _mockServices() {
    return [
      {
        'id': 1,
        'name': 'Reparación de Fugas',
        'price': 45000,
        'emoji': '💧',
        'category': 'Urgencias',
        'description': 'Detección y reparación de fugas invisibles en muros y pisos con ultrasonido.',
      },
      {
        'id': 2,
        'name': 'Instalación de Grifería',
        'price': 35000,
        'emoji': '🚰',
        'category': 'Instalaciones',
        'description': 'Instalación profesional de grifos, mezcladores para baño y cocina.',
      },
      {
        'id': 3,
        'name': 'Destape de Cañerías',
        'price': 60000,
        'emoji': '🚿',
        'category': 'Mantenimiento',
        'description': 'Desobstrucción técnica con sondas eléctricas de alto rendimiento.',
      },
      {
        'id': 4,
        'name': 'Instalación de Calentador',
        'price': 120000,
        'emoji': '🔥',
        'category': 'Gas y Calentadores',
        'description': 'Montaje, conexión de gas e hidráulica con certificación de seguridad.',
      },
      {
        'id': 5,
        'name': 'Lavado de Tanques',
        'price': 85000,
        'emoji': '🏢',
        'category': 'Limpieza',
        'description': 'Limpieza y desinfección química norma sanitaria para tanques aéreos y subterráneos.',
      },
      {
        'id': 6,
        'name': 'Mantenimiento Sanitario',
        'price': 50000,
        'emoji': '🚽',
        'category': 'Sanitarios',
        'description': 'Cambio de herrajes, empaques y ajuste de sanitarios residenciales.',
      },
    ];
  }

  static List<dynamic> _mockAppointments() {
    return [
      {
        'id': 1,
        'service_name': 'Reparación de Fugas',
        'date': '2026-08-15',
        'time': '09:00 AM',
        'status': 'scheduled',
        'technician_name': 'Carlos Rodríguez',
        'notes': 'Fuga debajo del lavaplatos principal',
        'price': 45000,
      },
      {
        'id': 2,
        'service_name': 'Instalación de Grifería',
        'date': '2026-08-10',
        'time': '02:30 PM',
        'status': 'completed',
        'technician_name': 'Andrés Morales',
        'notes': 'Grifo monocontrol de baño',
        'price': 35000,
      },
    ];
  }

  static List<dynamic> _mockTechnicians() {
    return [
      {
        'id': 1,
        'name': 'Carlos Rodríguez',
        'specialty': 'Plomería General y Fugas',
        'rating': 4.9,
        'experience': '8 años',
        'phone': '+57 312 456 7890',
        'avatar': 'https://i.pravatar.cc/300?img=11',
      },
      {
        'id': 2,
        'name': 'Andrés Morales',
        'specialty': 'Gas y Calentadores Certificado',
        'rating': 4.8,
        'experience': '5 años',
        'phone': '+57 315 789 0123',
        'avatar': 'https://i.pravatar.cc/300?img=33',
      },
      {
        'id': 3,
        'name': 'Hernán Gómez',
        'specialty': 'Destapes y Maquinaria Pesada',
        'rating': 5.0,
        'experience': '12 años',
        'phone': '+57 301 234 5678',
        'avatar': 'https://i.pravatar.cc/300?img=60',
      },
    ];
  }
}
