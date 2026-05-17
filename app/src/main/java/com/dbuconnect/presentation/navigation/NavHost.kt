package com.dbuconnect.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dbuconnect.presentation.screens.auth.LoginScreen
import com.dbuconnect.presentation.screens.auth.SignUpScreen
import com.dbuconnect.presentation.screens.chat.ChatScreen
import com.dbuconnect.presentation.screens.discover.DiscoverScreen
import com.dbuconnect.presentation.screens.discover.FiltersScreen
import com.dbuconnect.presentation.screens.events.EventsScreen
import com.dbuconnect.presentation.screens.matches.MatchSuccessScreen
import com.dbuconnect.presentation.screens.matches.MatchesScreen
import com.dbuconnect.presentation.screens.onboarding.OnboardingScreen
import com.dbuconnect.presentation.screens.profile.ProfileScreen
import com.dbuconnect.presentation.screens.profile.SettingsScreen
import com.dbuconnect.presentation.screens.setup.ProfileSetupScreen
import com.dbuconnect.presentation.theme.*

@Composable
fun DBUConnectNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Onboarding.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Show bottom bar only on main tabs
    val showBottomBar = currentDestination?.route in listOf(
        Screen.Discover.route,
        Screen.Matches.route,
        Screen.Events.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = PrimaryGreen,
                    tonalElevation = 0.dp
                ) {
                    BottomNavItem.entries.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryGreen,
                                selectedTextColor = PrimaryGreen,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary,
                                indicatorColor = PrimaryGreenContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Onboarding
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Login
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.ProfileSetup.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }

            // Sign Up
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(Screen.ProfileSetup.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Profile Setup
            composable(Screen.ProfileSetup.route) {
                ProfileSetupScreen(
                    onComplete = {
                        navController.navigate(Screen.Discover.route) {
                            popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                        }
                    }
                )
            }

            // Discover
            composable(Screen.Discover.route) {
                DiscoverScreen(
                    onOpenFilters = { navController.navigate(Screen.Filters.route) },
                    onMatchFound = { matchId ->
                        navController.navigate(Screen.MatchSuccess.createRoute(matchId))
                    }
                )
            }

            // Filters
            composable(Screen.Filters.route) {
                FiltersScreen(onBack = { navController.popBackStack() })
            }

            // Matches
            composable(Screen.Matches.route) {
                MatchesScreen(
                    onChatClick = { matchId ->
                        navController.navigate(Screen.Chat.createRoute(matchId))
                    },
                    onDiscoverClick = {
                        navController.navigate(Screen.Discover.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Chat
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
            ) { backStackEntry ->
                ChatScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Match Success
            composable(
                route = Screen.MatchSuccess.route,
                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
            ) {
                MatchSuccessScreen(
                    onSendMessage = {
                        val matchId = it.arguments?.getString("matchId") ?: ""
                        navController.navigate(Screen.Chat.createRoute(matchId)) {
                            popUpTo(Screen.Discover.route)
                        }
                    },
                    onKeepBrowsing = { navController.popBackStack() }
                )
            }

            // Events
            composable(Screen.Events.route) {
                EventsScreen(
                    onNavigateToCreateEvent = {
                        navController.navigate(Screen.CreateEvent.route)
                    }
                )
            }
            
            composable(Screen.CreateEvent.route) {
                com.dbuconnect.presentation.screens.events.CreateEventScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Profile
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.EditProfile.route) {
                ProfileSetupScreen(
                    onComplete = { navController.popBackStack() }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
