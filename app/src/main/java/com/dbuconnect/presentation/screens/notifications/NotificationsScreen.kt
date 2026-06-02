package com.dbuconnect.presentation.screens.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dbuconnect.data.models.Notification
import com.dbuconnect.data.models.NotificationType
import com.dbuconnect.presentation.components.AppTopBar
import com.dbuconnect.presentation.components.EmptyState
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNotificationClick: (Notification) -> Unit,
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 1.dp,
                color = BackgroundWhite
            ) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Notifications",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = PrimaryGreen
                            )
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = AccentPink
                                ) {
                                    Text(
                                        text = "$unreadCount",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    actions = {
                        if (unreadCount > 0) {
                            TextButton(onClick = { viewModel.markAllAsRead() }) {
                                Text(
                                    "Mark all read",
                                    color = PrimaryGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
                )
            }
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "No notifications yet",
                    description = "When someone likes your profile or you get a match, you'll see it here!"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            viewModel.markAsRead(notification.id)
                            onNotificationClick(notification)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val iconData = when (notification.type) {
        NotificationType.MUTUAL_LIKE -> Triple(Icons.Filled.Favorite, AccentPink, AccentPinkContainer)
        NotificationType.NEW_MESSAGE -> Triple(Icons.Filled.Chat, PrimaryGreen, PrimaryGreenContainer)
        NotificationType.PROFILE_LIKED -> Triple(Icons.Filled.ThumbUp, PrimaryGreenLight, PrimaryGreenContainer)
        NotificationType.EVENT_REMINDER -> Triple(Icons.Filled.Event, BrandPurple, Color(0xFFEDE9FE))
        NotificationType.INCOMING_CALL -> Triple(Icons.Filled.Videocam, PrimaryGreen, PrimaryGreenContainer)
        NotificationType.CALL_ACCEPTED -> Triple(Icons.Filled.Check, PrimaryGreen, PrimaryGreenContainer)
        NotificationType.CALL_REJECTED -> Triple(Icons.Filled.Close, StatusError, StatusError.copy(alpha = 0.15f))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (!notification.isRead) PrimaryGreenContainer.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon or photo
        Box {
            if (notification.fromUserPhoto.isNotBlank()) {
                AsyncImage(
                    model = notification.fromUserPhoto,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = iconData.third
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            iconData.first,
                            contentDescription = null,
                            tint = iconData.second,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            // Type badge overlay
            Surface(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomEnd),
                shape = CircleShape,
                color = iconData.second,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        iconData.first,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                fontSize = 15.sp,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.message,
                fontSize = 13.sp,
                color = if (!notification.isRead) TextPrimary else TextSecondary,
                fontWeight = if (!notification.isRead) FontWeight.Medium else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatNotificationTime(notification.timestamp),
                fontSize = 12.sp,
                color = TextTertiary
            )
        }

        // Unread dot
        if (!notification.isRead) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen)
                    .align(Alignment.CenterVertically)
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 78.dp),
        color = BorderDefault.copy(alpha = 0.5f)
    )
}

private fun formatNotificationTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 172_800_000 -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
