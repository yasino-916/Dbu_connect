package com.dbuconnect.presentation.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dbuconnect.presentation.components.PrimaryButton
import com.dbuconnect.presentation.components.SecondaryButton
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.OnboardingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) onComplete()
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setPage(pagerState.currentPage)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        // Skip button (not on last page)
        if (pagerState.currentPage < 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.skipToLogin() }) {
                    Text(
                        "Skip",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(56.dp))
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingPage1()
                1 -> OnboardingPage2()
                2 -> OnboardingPage3(
                    selectedIntent = state.selectedIntent,
                    onIntentSelected = { viewModel.selectIntent(it) }
                )
            }
        }

        // Bottom section
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (pagerState.currentPage == 2) {
                PrimaryButton(
                    text = "Get Started",
                    onClick = { viewModel.completeOnboarding() },
                    enabled = state.selectedIntent.isNotEmpty()
                )
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(
                    text = "Already have an account?",
                    onClick = { viewModel.skipToLogin() }
                )
            } else {
                PrimaryButton(
                    text = "Next",
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 24.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index == pagerState.currentPage) PrimaryGreen
                                else BorderDefault
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingPage1() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Logo
        DBULogo()

        Spacer(modifier = Modifier.height(24.dp))

        // Image placeholder
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceMuted
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1523580494863-6f3031224c94?w=600",
                contentDescription = "Students on campus",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Meet people from DBU",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Connect with fellow students, discover common interests, and build meaningful relationships in a trusted community dedicated to safety.",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun OnboardingPage2() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        DBULogo()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Onboarding: Privacy Control",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You control what you share",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        PrivacyBullet(
            icon = Icons.Outlined.Shield,
            text = "Your privacy is our priority. We use industry-standard encryption."
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrivacyBullet(
            icon = Icons.Outlined.VisibilityOff,
            text = "You decide what profile details are visible to others."
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrivacyBullet(
            icon = Icons.Outlined.Settings,
            text = "Adjust your privacy settings anytime from your profile."
        )
    }
}

@Composable
private fun PrivacyBullet(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = PrimaryGreen
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = TextPrimary,
            lineHeight = 24.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OnboardingPage3(
    selectedIntent: String,
    onIntentSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        DBULogo()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose your intent",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select what you're looking for to connect with the right community.",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        IntentCard(
            icon = Icons.Outlined.People,
            label = "Friends",
            selected = selectedIntent == "Friends",
            onClick = { onIntentSelected("Friends") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        IntentCard(
            icon = Icons.Outlined.Favorite,
            label = "Dating",
            selected = selectedIntent == "Dating",
            onClick = { onIntentSelected("Dating") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        IntentCard(
            icon = Icons.Outlined.MenuBook,
            label = "Study Buddy",
            selected = selectedIntent == "Study Buddy",
            onClick = { onIntentSelected("Study Buddy") }
        )
    }
}

@Composable
private fun IntentCard(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) PrimaryGreenContainer else Color.White,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) PrimaryGreen else BorderDefault
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (selected) PrimaryGreen.copy(alpha = 0.15f) else SurfaceMuted
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun DBULogo(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.School,
            contentDescription = "DBU Connect Logo",
            modifier = Modifier.size(40.dp),
            tint = PrimaryGreen
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "DBU Connect",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPurple
        )
    }
}
