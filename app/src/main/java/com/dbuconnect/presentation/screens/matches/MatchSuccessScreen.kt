package com.dbuconnect.presentation.screens.matches

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dbuconnect.presentation.components.PrimaryButton
import com.dbuconnect.presentation.components.SecondaryButton
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.data.api.getValidPhotoUrl

/**
 * Issue #13: matchName, matchPhotoUrl, and currentUserPhotoUrl are now parameters
 * so the NavHost can pass the real matched user's data from the match object.
 */
@Composable
fun MatchSuccessScreen(
    matchName: String = "Someone",
    matchPhotoUrl: String = "",
    currentUserPhotoUrl: String = "",
    onSendMessage: () -> Unit,
    onKeepBrowsing: () -> Unit
) {
    val matchPhoto = getValidPhotoUrl(matchPhotoUrl, "match", matchName)
    val userPhoto = getValidPhotoUrl(currentUserPhotoUrl, "current_user", "Me")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Overlapping circles
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Left photo (current user)
                AsyncImage(
                    model = userPhoto,
                    contentDescription = "Your photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = (-30).dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                )

                // Right photo (matched user)
                AsyncImage(
                    model = matchPhoto,
                    contentDescription = "$matchName's photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 30.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                )

                // Heart badge
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 8.dp),
                    shape = CircleShape,
                    color = PrimaryGreen
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "It's a Match!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "You and $matchName liked each other.\nWhy not start a conversation?",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            PrimaryButton(
                text = "💬  Send a Message",
                onClick = onSendMessage,
                modifier = Modifier.widthIn(max = 300.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryButton(
                text = "Keep Browsing",
                onClick = onKeepBrowsing,
                modifier = Modifier.widthIn(max = 300.dp)
            )
        }
    }
}
