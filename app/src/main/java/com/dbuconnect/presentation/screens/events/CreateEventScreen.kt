package com.dbuconnect.presentation.screens.events

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dbuconnect.presentation.components.PrimaryButton
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.CreateEventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    // Issue #18: actual image URI from picker
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val isPublishing by viewModel.isPublishing.collectAsState()
    val publishSuccess by viewModel.publishSuccess.collectAsState()
    val error by viewModel.error.collectAsState()

    // Issue #18: image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    LaunchedEffect(publishSuccess) {
        if (publishSuccess) {
            onBack()
        }
    }

    val isFormValid = title.isNotBlank() && date.isNotBlank() && location.isNotBlank() && !isPublishing

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Create Event", 
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundPrimary),
                modifier = Modifier.shadow(elevation = 2.dp, spotColor = Color.Black.copy(alpha = 0.05f))
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(16.dp, spotColor = Color.Black.copy(alpha = 0.1f)),
                color = BackgroundWhite
            ) {
                PrimaryButton(
                    text = if (isPublishing) "Publishing..." else "Publish Event",
                    onClick = { viewModel.publishEvent(title, description, date, time, location) },
                    enabled = isFormValid,
                    modifier = Modifier.padding(20.dp).height(54.dp)
                )
            }
        },
        containerColor = BackgroundPrimary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            Text(
                text = "Event Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Issue #19: display error if publishing fails
            if (error != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = StatusError.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, StatusError.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = error!!,
                        color = StatusError,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Event Title") },
                placeholder = { Text("e.g. Freshman Tech Meetup") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderDefault,
                    focusedContainerColor = BackgroundWhite,
                    unfocusedContainerColor = BackgroundWhite
                ),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                label = { Text("Description") },
                placeholder = { Text("What is this event about?") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderDefault,
                    focusedContainerColor = BackgroundWhite,
                    unfocusedContainerColor = BackgroundWhite
                )
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Date
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Date") },
                    placeholder = { Text("MM/DD/YYYY") },
                    leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null, tint = TextSecondary) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderDefault,
                        focusedContainerColor = BackgroundWhite,
                        unfocusedContainerColor = BackgroundWhite
                    ),
                    singleLine = true
                )
                
                // Time
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Time") },
                    placeholder = { Text("00:00 AM") },
                    leadingIcon = { Icon(Icons.Outlined.Schedule, null, tint = TextSecondary) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderDefault,
                        focusedContainerColor = BackgroundWhite,
                        unfocusedContainerColor = BackgroundWhite
                    ),
                    singleLine = true
                )
            }
            
            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Location") },
                placeholder = { Text("e.g. Main Library, Room 104") },
                leadingIcon = { Icon(Icons.Outlined.LocationOn, null, tint = TextSecondary) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderDefault,
                    focusedContainerColor = BackgroundWhite,
                    unfocusedContainerColor = BackgroundWhite
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Issue #18: Real Image Uploader with picker
            Surface(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceMuted.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, if (imageUri != null) PrimaryGreen else BorderDefault),
                onClick = { imagePickerLauncher.launch("image/*") }
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Event poster",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryGreenContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = "Upload Poster",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Upload Event Poster", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Tap to select an image from gallery", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
