package com.example.plomaap.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.plomaap.ui.screens.appointments.AppointmentsScreen
import com.example.plomaap.ui.screens.auth.ForgotPasswordScreen
import com.example.plomaap.ui.screens.auth.LoginScreen
import com.example.plomaap.ui.screens.auth.RegisterScreen
import com.example.plomaap.ui.screens.booking.*
import com.example.plomaap.ui.screens.home.ServicesScreen
import com.example.plomaap.ui.screens.profile.EditProfileScreen
import com.example.plomaap.ui.screens.profile.ProfileScreen
import com.example.plomaap.ui.screens.technicians.TechniciansScreen
import com.example.plomaap.ui.theme.*
import com.example.plomaap.viewmodel.AuthViewModel
import com.example.plomaap.viewmodel.BookingViewModel
import com.example.plomaap.viewmodel.MainViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val MAIN = "main"
    const val EDIT_PROFILE = "edit_profile"
    
    const val SERVICE_DETAILS = "service_details/{serviceId}"
    const val BOOKING_SCHEDULE = "booking_schedule"
    const val BOOKING_LOCATION = "booking_location"
    const val BOOKING_SUMMARY = "booking_summary"
    
    fun serviceDetails(serviceId: Int) = "service_details/$serviceId"
}

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Services : BottomNavItem("services", Icons.Rounded.Plumbing, "Servicios")
    data object Appointments : BottomNavItem("appointments", Icons.Rounded.CalendarMonth, "Citas")
    data object Technicians : BottomNavItem("technicians", Icons.Rounded.Engineering, "Técnicos")
    data object Profile : BottomNavItem("profile", Icons.Rounded.Person, "Perfil")
}

val bottomNavItems = listOf(BottomNavItem.Services, BottomNavItem.Appointments, BottomNavItem.Technicians, BottomNavItem.Profile)

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val bookingViewModel: BookingViewModel = viewModel()

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            navController.navigate(Routes.MAIN) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController, startDestination = if (authState.isLoggedIn) Routes.MAIN else Routes.LOGIN,
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { 100 }, animationSpec = tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { -100 }, animationSpec = tween(300)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(targetOffsetX = { 100 }, animationSpec = tween(200)) }
    ) {
        // Auth Flow
        composable(Routes.LOGIN) { LoginScreen(navController, authViewModel) }
        composable(Routes.REGISTER) { RegisterScreen(navController, authViewModel) }
        composable(Routes.FORGOT_PASSWORD) { 
            ForgotPasswordScreen(
                isLoading = authState.isLoading, 
                successMessage = authState.successMessage, 
                onSendReset = { authViewModel.forgotPassword(it) }, 
                onNavigateBack = { navController.popBackStack() }, 
                onClearMessages = { authViewModel.clearMessages() }
            ) 
        }
        
        // Main App
        composable(Routes.MAIN) { MainScreen(navController = navController, authViewModel = authViewModel, bookingViewModel = bookingViewModel) }
        
        // Profile
        composable(Routes.EDIT_PROFILE) { 
            EditProfileScreen(
                user = authState.user, 
                isLoading = authState.isLoading, 
                successMessage = authState.successMessage, 
                onSave = { name, phone, address -> authViewModel.updateProfile(name, phone, address) }, 
                onNavigateBack = { navController.popBackStack() }, 
                onClearMessages = { authViewModel.clearMessages() }
            ) 
        }
        
        // Booking Flow
        composable(
            route = Routes.SERVICE_DETAILS,
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getInt("serviceId") ?: 0
            ServiceDetailsScreen(
                serviceId = serviceId,
                viewModel = bookingViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSchedule = { navController.navigate(Routes.BOOKING_SCHEDULE) }
            )
        }
        
        composable(Routes.BOOKING_SCHEDULE) {
            BookingScheduleScreen(
                viewModel = bookingViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLocation = { navController.navigate(Routes.BOOKING_LOCATION) }
            )
        }
        
        composable(Routes.BOOKING_LOCATION) {
            BookingLocationScreen(
                viewModel = bookingViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSummary = { navController.navigate(Routes.BOOKING_SUMMARY) }
            )
        }
        
        composable(Routes.BOOKING_SUMMARY) {
            BookingSummaryScreen(
                userId = authState.user?.id,
                viewModel = bookingViewModel,
                onNavigateBack = { navController.popBackStack() },
                onBookingSuccess = {
                    bookingViewModel.clearState()
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(navController: NavController, authViewModel: AuthViewModel, bookingViewModel: BookingViewModel) {
    val mainViewModel: MainViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { 
        mainViewModel.loadServices() 
        mainViewModel.loadAppointments(authState.user?.email ?: "")
        mainViewModel.loadTechnicians() 
    }

    Scaffold(
        containerColor = BackgroundLight, 
        bottomBar = { PremiumBottomBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedContent(targetState = selectedTab, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) }, label = "tabContent") { tab ->
                when (tab) {
                    0 -> ServicesScreen(
                        viewModel = mainViewModel,
                        onServiceClick = { serviceId ->
                            bookingViewModel.clearState()
                            navController.navigate(Routes.serviceDetails(serviceId))
                        }
                    )
                    1 -> AppointmentsScreen(viewModel = mainViewModel)
                    2 -> TechniciansScreen(viewModel = mainViewModel)
                    3 -> ProfileScreen(navController = navController, viewModel = authViewModel)
                }
            }
        }
    }
}

@Composable
private fun PremiumBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = SurfaceLight, 
        tonalElevation = 0.dp, 
        modifier = Modifier.shadow(elevation = 16.dp, ambientColor = SapphireBlue.copy(alpha = 0.1f))
    ) {
        bottomNavItems.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected, onClick = { onTabSelected(index) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label, modifier = Modifier.size(if (isSelected) 26.dp else 24.dp)) },
                label = { Text(text = item.label, fontSize = if (isSelected) 11.sp else 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SapphireBlue, 
                    selectedTextColor = SapphireBlue, 
                    unselectedIconColor = TextTertiary, 
                    unselectedTextColor = TextTertiary, 
                    indicatorColor = SurfaceElevatedLight
                )
            )
        }
    }
}
