package com.dbuconnect.presentation.screens.discover

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dbuconnect.data.models.ProfileCard
import com.dbuconnect.presentation.common.UiState
import com.dbuconnect.presentation.components.DBUChip
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.DiscoverViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onOpenFilters: () -> Unit,
    onMatchFound: (String) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val matchResult by viewModel.matchResult.collectAsState()

    LaunchedEffect(matchResult) {
        matchResult?.let { match ->
            onMatchFound(match.id)
            viewModel.clearMatchResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "DBU Connect",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    // Campus Mode badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryGreen,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        "Campus\nMode",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        lineHeight = 11.sp
                    )
                    IconButton(onClick = onOpenFilters) {
                        Icon(
                            Icons.Outlined.FilterList,
                            contentDescription = "Filters",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        containerColor = BackgroundPrimary
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen
                    )
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Failed to load profiles", color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadCards() }) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Empty -> {
                    com.dbuconnect.presentation.components.EmptyState(
                        title = "No more profiles",
                        description = "Check back later or adjust your filters to discover more people.",
                        actionText = "Refresh",
                        onAction = { viewModel.loadCards() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Success -> {
                    val cards = (uiState as UiState.Success<List<ProfileCard>>).data
                    if (currentIndex < cards.size) {
                        SwipeableProfileCard(
                            card = cards[currentIndex],
                            onLike = { viewModel.likeProfile(cards[currentIndex]) },
                            onPass = { viewModel.passProfile(cards[currentIndex]) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeableProfileCard(
    card: ProfileCard,
    onLike: () -> Unit,
    onPass: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val rotation = offsetX / 40f
    val swipeThreshold = 300f

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "offsetX"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), offsetY.roundToInt()) }
                .graphicsLayer { rotationZ = rotation }
                .pointerInput(card.id) {
                    detectDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThreshold -> {
                                    onLike()
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                                offsetX < -swipeThreshold -> {
                                    onPass()
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                                else -> {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y * 0.3f
                    }
                }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp
            ) {
                Box {
                    AsyncImage(
                        model = card.photoUrl,
                        contentDescription = "${card.name}'s photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    // Swipe indicators
                    if (abs(offsetX) > 50) {
                        val isLike = offsetX > 0
                        Surface(
                            modifier = Modifier
                                .padding(24.dp)
                                .align(if (isLike) Alignment.TopStart else Alignment.TopEnd),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isLike) PrimaryGreen.copy(alpha = 0.9f)
                            else StatusError.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = if (isLike) "LIKE" else "PASS",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }

                    // Profile info
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "${card.name}, ${card.age}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${card.department} • Year ${card.year}",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        if (card.interests.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                card.interests.take(3).forEach { interest ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = interest,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pass button
            OutlinedIconButton(
                onClick = onPass,
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, BorderDefault)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Pass",
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Super Like (star)
            OutlinedIconButton(
                onClick = { onLike() },
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, BorderDefault)
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Super Like",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Like button
            OutlinedIconButton(
                onClick = onLike,
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, AccentPink.copy(alpha = 0.3f))
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Like",
                    tint = AccentPink,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
