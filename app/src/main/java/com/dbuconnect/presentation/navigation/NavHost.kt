package com.dbuconnect.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.dbuconnect.presentation.screens.chat.VideoCallScreen
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
import com.dbuconnect.presentation.viewmodels.DiscoverViewModel
import com.dbuconnect.presentation.viewmodels.StartupViewModel

@Composable
private fun StartupRouterScreen(
    navController: NavHostController,
    viewModel: StartupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (!state.isReady) return@LaunchedEffect

        val destination = when {
            !state.onboardingCompleted -> Screen.Onboarding.route
            !state.isLoggedIn -> Screen.Login.route
            !state.isProfileComplete -> Screen.ProfileSetup.route
            else -> Screen.Discover.route
        }

        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryGreen)
    }
}

@Composable
fun DBUConnectNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
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
                    containerColor = BackgroundWhite,
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
            // Startup router
            composable(Screen.Splash.route) {
                StartupRouterScreen(navController = navController)
            }

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
                    onLoginSuccess = { isProfileComplete ->
                        val destination = if (isProfileComplete) {
                            Screen.Discover.route
                        } else {
                            Screen.ProfileSetup.route
                        }

                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
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
                // Issue #12: Scope DiscoverViewModel to the parent back stack entry
                // so Filters and Discover share the same instance
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Screen.Discover.route)
                }
                val discoverViewModel: DiscoverViewModel = hiltViewModel(parentEntry)

                DiscoverScreen(
                    viewModel = discoverViewModel,
                    onOpenFilters = { navController.navigate(Screen.Filters.route) },
                    onMatchFound = { matchId ->
                        navController.navigate(Screen.MatchSuccess.createRoute(matchId))
                    }
                )
            }

            // Filters - Issue #12: share DiscoverViewModel with Discover screen
            composable(Screen.Filters.route) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Screen.Discover.route)
                }
                val discoverViewModel: DiscoverViewModel = hiltViewModel(parentEntry)

                FiltersScreen(
                    viewModel = discoverViewModel,
                    onBack = { navController.popBackStack() }
                )
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
                    onBack = { navController.popBackStack() },
                    onStartVideoCall = { matchId ->
                        navController.navigate(Screen.VideoCall.createRoute(matchId, true))
                    }
                )
            }

            // Video Call
            composable(
                route = Screen.VideoCall.route,
                arguments = listOf(
                    navArgument("matchId") { type = NavType.StringType },
                    navArgument("isOutgoing") { type = NavType.BoolType }
                )
            ) {
                VideoCallScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Match Success - Issue #13: pass real match data via nav args
            composable(
                route = Screen.MatchSuccess.route,
                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                // Get match name/photo from the DiscoverViewModel's last match result
                // For now we pass the matchId and let the screen show data
                MatchSuccessScreen(
                    onSendMessage = {
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
                    onComplete = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
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
