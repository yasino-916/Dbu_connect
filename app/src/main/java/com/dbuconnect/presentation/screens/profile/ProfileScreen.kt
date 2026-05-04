package com.dbuconnect.presentation.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dbuconnect.presentation.components.DBUChip
import com.dbuconnect.presentation.screens.onboarding.DBULogo
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DBULogo()

            Spacer(modifier = Modifier.height(24.dp))

            // Profile photo
            Box {
                AsyncImage(
                    model = state.user?.photos?.firstOrNull()
                        ?: "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=200",
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, PrimaryGreen, CircleShape)
                )
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd),
                    shape = CircleShape,
                    color = PrimaryGreen
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.user?.name ?: "Your Name",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Text(
                text = "${state.user?.department ?: "Department"} • Year ${state.user?.year ?: 1}",
                fontSize = 14.sp,
                color = TextSecondary
            )

            if (state.user?.interests?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.user!!.interests.forEach { interest ->
                        DBUChip(label = interest, selected = true)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Menu items
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ProfileMenuItem(
                icon = Icons.Outlined.Edit,
                title = "Edit Profile",
                onClick = {}
            )

            ProfileMenuItem(
                icon = Icons.Outlined.Shield,
                title = "Privacy Center",
                onClick = onNavigateToSettings
            )

            ProfileMenuItem(
                icon = Icons.Outlined.Notifications,
                title = "Notification Preferences",
                onClick = {}
            )

            ProfileMenuItem(
                icon = Icons.Outlined.Help,
                title = "Help & Support",
                onClick = {}
            )

            ProfileMenuItem(
                icon = Icons.Outlined.Info,
                title = "About DBU Connect",
                onClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logout
            TextButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Log Out",
                    color = StatusError,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderDefault)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextTertiary)
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val privacy = state.privacySettings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DBULogo()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings & Privacy",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Group 1: Account
            Text(
                text = "Group 1",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderDefault),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Account", fontSize = 16.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Group 2: Visibility Toggles
            Text(
                text = "Group 2: Visibility Toggles",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderDefault),
                color = Color.White
            ) {
                Column {
                    SettingsToggle(
                        icon = Icons.Outlined.Visibility,
                        title = "Hide profile",
                        checked = privacy.hideProfile,
                        onCheckedChange = {
                            viewModel.updatePrivacySetting(privacy.copy(hideProfile = it))
                        }
                    )
                    HorizontalDivider(color = BorderDefault, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsToggle(
                        icon = Icons.Outlined.Visibility,
                        title = "Campus only",
                        checked = privacy.campusMode,
                        onCheckedChange = {
                            viewModel.updatePrivacySetting(privacy.copy(campusMode = it))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Group 3: Blocked List
            Text(
                text = "Group 3",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderDefault),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Block, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Blocked List", fontSize = 16.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Group 4: Notifications
            Text(
                text = "Group 4",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderDefault),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Notification Preferences", fontSize = 16.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Log Out",
                    color = StatusError,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = BorderDefault
            )
        )
    }
}
