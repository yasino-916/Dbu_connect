package com.dbuconnect.presentation.screens.events

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dbuconnect.data.models.Event
import com.dbuconnect.data.models.RsvpStatus
import com.dbuconnect.presentation.common.UiState
import com.dbuconnect.presentation.components.DBUChip
import com.dbuconnect.presentation.components.PrimaryButton
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.EventsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onNavigateToCreateEvent: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val events by viewModel.events.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val allTags = events.flatMap { it.tags }.distinct()
    val filteredEvents = if (selectedTag != null) {
        events.filter { it.tags.contains(selectedTag) }
    } else events

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryGreenContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.School,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Campus Events",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 22.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundPrimary),
                modifier = Modifier.shadow(elevation = 2.dp, spotColor = Color.Black.copy(alpha = 0.05f))
            )
        },
        floatingActionButton = {
            if (currentUser?.isAdmin == true) {
                FloatingActionButton(
                    onClick = onNavigateToCreateEvent,
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Filled.Add, "Create Event")
                }
            }
        },
        containerColor = BackgroundPrimary
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Category tags
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 20.dp)
                ) {
                    item {
                        DBUChip(
                            label = "All",
                            selected = selectedTag == null,
                            onClick = { viewModel.selectTag(null) }
                        )
                    }
                    items(allTags) { tag ->
                        DBUChip(
                            label = tag,
                            selected = selectedTag == tag,
                            onClick = { viewModel.selectTag(tag) }
                        )
                    }
                }
            }

            when (uiState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        com.dbuconnect.presentation.components.EmptyState(
                            title = "Could not load events",
                            description = "Check your connection and try again.",
                            actionText = "Retry",
                            onAction = { viewModel.loadEvents() }
                        )
                    }
                }
                else -> {
                    if (filteredEvents.isEmpty()) {
                        item {
                            com.dbuconnect.presentation.components.EmptyState(
                                title = "No events found",
                                description = "There are no events matching this category right now."
                            )
                        }
                    } else {
                        items(filteredEvents) { event ->
                            EventCard(
                                event = event,
                                onRsvp = { viewModel.rsvp(event.id, RsvpStatus.GOING) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    onRsvp: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = BackgroundWhite,
        shadowElevation = 4.dp,
    ) {
        Column {
            // Event image
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Column(modifier = Modifier.padding(20.dp)) {
                // Tags
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    event.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryGreenContainer
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryGreenDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = event.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Date info
                EventInfoRow(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Date",
                    value = dateFormat.format(Date(event.dateTime))
                )

                Spacer(modifier = Modifier.height(12.dp))

                EventInfoRow(
                    icon = Icons.Outlined.Schedule,
                    label = "Time",
                    value = timeFormat.format(Date(event.dateTime))
                )

                Spacer(modifier = Modifier.height(12.dp))

                EventInfoRow(
                    icon = Icons.Outlined.LocationOn,
                    label = "Location",
                    value = event.location
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = BorderDefault.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Attendees + RSVP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${event.attendeeCount} Going",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (event.attendeesFromDept > 0) {
                            Text(
                                text = "Including ${event.attendeesFromDept} from your major",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onRsvp,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (event.rsvpStatus == RsvpStatus.GOING) PrimaryGreenContainer else PrimaryGreen,
                            contentColor = if (event.rsvpStatus == RsvpStatus.GOING) PrimaryGreenDark else Color.White
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = if (event.rsvpStatus == RsvpStatus.GOING) "Going ✓" else "RSVP Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = SurfaceMuted
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}
