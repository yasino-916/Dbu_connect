package com.dbuconnect.presentation.screens.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.CallState
import com.dbuconnect.presentation.viewmodels.VideoCallViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun VideoCallScreen(
    onBack: () -> Unit,
    viewModel: VideoCallViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Request permissions
    var permissionsGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionsGranted = perms[Manifest.permission.CAMERA] == true && perms[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    // Auto navigate back when call ends
    LaunchedEffect(state.callState) {
        if (state.callState == CallState.ENDED || state.callState == CallState.REJECTED) {
            delay(1500)
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Sleek slate-900 background
    ) {
        // Blurred background of the peer photo for ultra premium vibe
        if (state.peerPhotoUrl.isNotBlank()) {
            AsyncImage(
                model = state.peerPhotoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp)
                    .alpha(0.35f)
            )
        }

        // Main overlay gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Renders active call screens or calling/dialing states
        when (state.callState) {
            CallState.DIALING, CallState.RINGING -> {
                DialingOverlay(
                    peerName = state.peerName,
                    peerPhotoUrl = state.peerPhotoUrl,
                    statusText = if (state.callState == CallState.DIALING) "Dialing..." else "Ringing...",
                    onCancel = { viewModel.endCall() }
                )
            }
            CallState.CONNECTED -> {
                // Fullscreen Remote stream (simulated with interactive high-quality looping avatar)
                FullscreenRemoteStream(
                    peerName = state.peerName,
                    peerPhotoUrl = state.peerPhotoUrl,
                    isVideoDisabled = state.isVideoDisabled
                )

                // Dragging Local PIP Camera feed
                if (permissionsGranted && !state.isVideoDisabled) {
                    LocalCameraPip(
                        context = context,
                        isFrontCamera = state.isFrontCamera,
                        lifecycleOwner = lifecycleOwner
                    )
                }

                // Call Controls Bottom HUD
                CallControlsHud(
                    isMuted = state.isMuted,
                    isVideoDisabled = state.isVideoDisabled,
                    isFrontCamera = state.isFrontCamera,
                    durationSeconds = state.durationSeconds,
                    onToggleMute = { viewModel.toggleMute() },
                    onToggleVideo = { viewModel.toggleVideo() },
                    onFlipCamera = { viewModel.flipCamera() },
                    onEndCall = { viewModel.endCall() }
                )
            }
            CallState.REJECTED -> {
                StatusToastOverlay(text = "Call Declined", icon = Icons.Filled.CallEnd)
            }
            CallState.ENDED -> {
                StatusToastOverlay(text = "Call Ended", icon = Icons.Filled.CallEnd)
            }
        }
    }
}

@Composable
private fun DialingOverlay(
    peerName: String,
    peerPhotoUrl: String,
    statusText: String,
    onCancel: () -> Unit
) {
    // Pulsing animation for calling circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Peer Name & Status Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 80.dp)
        ) {
            Text(
                text = peerName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreenLight
            )
        }

        // Center Pulsing Photo Slot
        Box(contentAlignment = Alignment.Center) {
            // Pulse rings
            Box(
                modifier = Modifier
                    .size(150.dp * pulseScale)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.1f))
            )
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(3.dp, PrimaryGreen, CircleShape)
            ) {
                AsyncImage(
                    model = peerPhotoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Hang Up Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 60.dp)
        ) {
            Surface(
                onClick = onCancel,
                shape = CircleShape,
                color = StatusError,
                modifier = Modifier.size(64.dp),
                tonalElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "Cancel Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Cancel",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun FullscreenRemoteStream(
    peerName: String,
    peerPhotoUrl: String,
    isVideoDisabled: Boolean
) {
    if (isVideoDisabled) {
        // Disabled remote stream showing blurred photo
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                ) {
                    AsyncImage(
                        model = peerPhotoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$peerName's Camera Paused",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        // Simulated premium looping live streaming frame of peer
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = peerPhotoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Premium ambient dynamic lighting
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                            radius = 1200f
                        )
                    )
            )
        }
    }
}

@Composable
private fun LocalCameraPip(
    context: Context,
    isFrontCamera: Boolean,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    // Draggable position coordinates
    var offsetX by remember { mutableStateOf(20f) }
    var offsetY by remember { mutableStateOf(100f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .size(width = 110.dp, height = 160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview
                        )
                    } catch (exc: Exception) {
                        exc.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )
    }
}

@Composable
private fun BoxScope.CallControlsHud(
    isMuted: Boolean,
    isVideoDisabled: Boolean,
    isFrontCamera: Boolean,
    durationSeconds: Int,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit,
    onEndCall: () -> Unit
) {
    // Format call duration timer beautifully: e.g. 02:45
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val timerString = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Call Timer Box
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timerString,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // HUD panel
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                IconButtonHUD(
                    icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    tint = if (isMuted) StatusError else Color.White,
                    bgColor = if (isMuted) StatusError.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f),
                    onClick = onToggleMute
                )

                // Video Toggle Button
                IconButtonHUD(
                    icon = if (isVideoDisabled) Icons.Filled.VideocamOff else Icons.Filled.Videocam,
                    tint = if (isVideoDisabled) StatusError else Color.White,
                    bgColor = if (isVideoDisabled) StatusError.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f),
                    onClick = onToggleVideo
                )

                // Flip Camera Button
                IconButtonHUD(
                    icon = Icons.Filled.FlipCameraAndroid,
                    tint = Color.White,
                    bgColor = Color.White.copy(alpha = 0.15f),
                    onClick = onFlipCamera
                )

                // Hang Up Button
                IconButtonHUD(
                    icon = Icons.Filled.CallEnd,
                    tint = Color.White,
                    bgColor = StatusError,
                    onClick = onEndCall
                )
            }
        }
    }
}

@Composable
private fun IconButtonHUD(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bgColor,
        modifier = Modifier.size(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StatusToastOverlay(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = StatusError.copy(alpha = 0.15f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = StatusError,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
