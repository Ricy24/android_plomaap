import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/api_service.dart';

class AuthProvider extends ChangeNotifier {
  bool _isLoading = false;
  bool _isLoggedIn = false;
  String? _token;
  Map<String, dynamic>? _user;

  bool get isLoading => _isLoading;
  bool get isLoggedIn => _isLoggedIn;
  String? get token => _token;
  Map<String, dynamic>? get user => _user;

  AuthProvider() {
    _tryAutoLogin();
  }

  Future<void> _tryAutoLogin() async {
    final prefs = await SharedPreferences.getInstance();
    final savedToken = prefs.getString('jwt_token');
    if (savedToken != null && savedToken.isNotEmpty) {
      _token = savedToken;
      _isLoggedIn = true;
      notifyListeners();
      await fetchProfile();
    }
  }

  Future<bool> login(String email, String password) async {
    _setLoading(true);
    final res = await ApiService.login(email, password);
    _setLoading(false);

    if (res.containsKey('access_token')) {
      await _saveSession(res['access_token'], res['user']);
      return true;
    }
    return false;
  }

  Future<bool> googleLogin(String googleIdToken) async {
    _setLoading(true);
    final res = await ApiService.googleLogin(googleIdToken);
    _setLoading(false);

    if (res.containsKey('access_token')) {
      await _saveSession(res['access_token'], res['user']);
      return true;
    }
    return false;
  }

  Future<bool> register({
    required String name,
    required String email,
    required String password,
    required String role,
    required String phone,
    required String address,
  }) async {
    _setLoading(true);
    final res = await ApiService.register(
      name: name,
      email: email,
      password: password,
      role: role,
      phone: phone,
      address: address,
    );
    _setLoading(false);

    if (res.containsKey('access_token')) {
      await _saveSession(res['access_token'], res['user']);
      return true;
    }
    return false;
  }

  Future<bool> forgotPassword(String email) async {
    _setLoading(true);
    final res = await ApiService.forgotPassword(email);
    _setLoading(false);
    return res.containsKey('message');
  }

  Future<void> fetchProfile() async {
    if (_token == null) return;
    final res = await ApiService.getProfile(_token!);
    if (res.containsKey('user')) {
      _user = res['user'];
      notifyListeners();
    }
  }

  Future<bool> updateProfile({
    required String name,
    required String phone,
    required String address,
  }) async {
    if (_token == null) return false;
    _setLoading(true);
    final res = await ApiService.updateProfile(
      token: _token!,
      name: name,
      phone: phone,
      address: address,
    );
    _setLoading(false);

    if (_user != null) {
      _user!['name'] = name;
      _user!['phone'] = phone;
      _user!['address'] = address;
      notifyListeners();
    }
    return res.containsKey('message') || res.containsKey('user');
  }

  Future<void> logout() async {
    _isLoggedIn = false;
    _token = null;
    _user = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('jwt_token');
    notifyListeners();
  }

  Future<void> _saveSession(String token, Map<String, dynamic>? userData) async {
    _token = token;
    _user = userData;
    _isLoggedIn = true;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('jwt_token', token);
    notifyListeners();
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
