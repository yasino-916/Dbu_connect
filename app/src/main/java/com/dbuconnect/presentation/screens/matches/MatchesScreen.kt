package com.dbuconnect.presentation.screens.matches

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.dbuconnect.data.models.Match
import com.dbuconnect.presentation.common.UiState
import com.dbuconnect.presentation.components.AppTopBar
import com.dbuconnect.presentation.components.EmptyState
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.MatchesViewModel
import com.dbuconnect.presentation.viewmodels.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    onChatClick: (String) -> Unit,
    onDiscoverClick: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: MatchesViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val newMatches by viewModel.newMatches.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val unreadNotificationsCount by notificationsViewModel.unreadCount.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "DBU Connect",
                actions = {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                Badge(
                                    containerColor = AccentPink,
                                    contentColor = Color.White
                                ) {
                                    Text(text = "$unreadNotificationsCount")
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            )
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search", color = TextTertiary) },
                    trailingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderDefault,
                        focusedContainerColor = SurfaceMuted,
                        unfocusedContainerColor = SurfaceMuted
                    ),
                    singleLine = true
                )
            }

            when {
                uiState is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                }
                newMatches.isEmpty() && chats.isEmpty() -> {
                    item {
                        EmptyState(
                            title = "No matches yet",
                            description = "Keep exploring and connecting with students from your department and beyond.",
                            actionText = "Start Discovering",
                            onAction = onDiscoverClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                else -> {
                    // New Matches section
                    if (newMatches.isNotEmpty()) {
                        item {
                            Text(
                                text = "New Matches",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(newMatches) { match ->
                                    NewMatchItem(
                                        match = match,
                                        onClick = { onChatClick(match.id) }
                                    )
                                }
                            }
                        }
                    }

                    // Messages section
                    if (chats.isNotEmpty()) {
                        item {
                            Text(
                                text = "Messages",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                            HorizontalDivider(color = BorderDefault)
                        }
                        items(chats) { chat ->
                            ChatListItem(
                                match = chat,
                                onClick = { onChatClick(chat.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewMatchItem(
    match: Match,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(80.dp)
    ) {
        Box {
            AsyncImage(
                model = match.userPhotoUrl,
                contentDescription = match.userName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(2.dp, PrimaryGreen, CircleShape)
            )
            if (match.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(StatusOnline)
                        .border(2.dp, BackgroundWhite, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = match.userName.split(" ").first(),
            fontSize = 12.sp,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChatListItem(
    match: Match,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = match.userPhotoUrl,
                contentDescription = match.userName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            if (match.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(StatusOnline)
                        .border(2.dp, BackgroundWhite, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = match.userName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (match.lastMessage != null) {
                Text(
                    text = match.lastMessage,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            match.lastMessageTime?.let { time ->
                Text(
                    text = formatTime(time),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            if (match.unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = CircleShape,
                    color = PrimaryGreen,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${match.unreadCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
