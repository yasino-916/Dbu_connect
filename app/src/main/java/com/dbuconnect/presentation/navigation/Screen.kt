package com.dbuconnect.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object ProfileSetup : Screen("profile_setup")
    data object Home : Screen("home")
    data object Discover : Screen("discover")
    data object Filters : Screen("filters")
    data object Matches : Screen("matches")
    data object Chat : Screen("chat/{matchId}") {
        fun createRoute(matchId: String) = "chat/$matchId"
    }
    data object Events : Screen("events")
    data object CreateEvent : Screen("create_event")
    data object EventDetail : Screen("event/{eventId}") {
        fun createRoute(eventId: String) = "event/$eventId"
    }
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object Settings : Screen("settings")
    data object MatchSuccess : Screen("match_success/{matchId}") {
        fun createRoute(matchId: String) = "match_success/$matchId"
    }
}

enum class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DISCOVER("discover", "Discover", Icons.Filled.Explore, Icons.Outlined.Explore),
    MATCHES("matches", "Matches", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    EVENTS("events", "Events", Icons.Filled.Event, Icons.Outlined.Event),
    PROFILE("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}
