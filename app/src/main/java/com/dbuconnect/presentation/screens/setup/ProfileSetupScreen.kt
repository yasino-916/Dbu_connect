package com.dbuconnect.presentation.screens.setup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dbuconnect.presentation.components.DBUChip
import com.dbuconnect.presentation.components.PrimaryButton
import com.dbuconnect.presentation.components.SecondaryButton
import com.dbuconnect.presentation.screens.onboarding.DBULogo
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.ProfileViewModel

// College -> Department mapping for DBU
val collegesDepartments = linkedMapOf(
    "College of Engineering" to listOf(
        "Computer Science", "Electrical Engineering", "Civil Engineering",
        "Mechanical Engineering", "Architecture"
    ),
    "College of Business & Economics" to listOf(
        "Business Administration", "Accounting & Finance", "Economics", "Management"
    ),
    "College of Medicine & Health Sciences" to listOf(
        "Medicine", "Nursing", "Public Health", "Pharmacy"
    ),
    "College of Natural & Computational Sciences" to listOf(
        "Chemistry", "Physics", "Mathematics", "Biology", "Statistics"
    ),
    "College of Social Sciences & Humanities" to listOf(
        "Psychology", "Sociology", "History", "Geography"
    ),
    "School of Law" to listOf("Law")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onComplete: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Step management: 0=Photos, 1=Basic Info (required), 2=Interests (optional), 3=Privacy (optional)
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    // Photo URIs
    var photoUris by remember { mutableStateOf(listOf<Uri?>( null, null, null, null)) }

    // College/Department state
    var selectedCollege by remember { mutableStateOf("") }
    var expandedCollege by remember { mutableStateOf(false) }
    var expandedDept by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }
    var privacyPreview by remember { mutableStateOf(true) }

    val years = (1..5).toList()
    val allInterests = listOf(
        "Studying", "Coffee", "Sports", "Music", "Volunteering",
        "Photography", "Travel", "Gaming", "Art", "Reading",
        "Dancing", "Fitness", "Technology", "Cooking", "Movies"
    )

    val progress = (currentStep + 1).toFloat() / totalSteps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                IconButton(onClick = { currentStep-- }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            // Skip to optional link (visible on required steps)
            if (currentStep < 2) {
                TextButton(onClick = { currentStep = 2 }) {
                    Text("Skip to optional →", color = PrimaryGreen, fontSize = 14.sp)
                }
            }
        }

        // Progress bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = PrimaryGreen,
                trackColor = BorderDefault,
                drawStopIndicator = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Step ${currentStep + 1} of $totalSteps",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            DBULogo()
            Spacer(modifier = Modifier.height(12.dp))

            when (currentStep) {
                0 -> PhotoUploadStep(
                    photoUris = photoUris,
                    onPhotoUrisChanged = { photoUris = it }
                )
                1 -> BasicInfoStep(
                    state = state,
                    viewModel = viewModel,
                    selectedCollege = selectedCollege,
                    onCollegeSelected = { selectedCollege = it },
                    expandedCollege = expandedCollege,
                    onExpandedCollegeChange = { expandedCollege = it },
                    expandedDept = expandedDept,
                    onExpandedDeptChange = { expandedDept = it },
                    expandedYear = expandedYear,
                    onExpandedYearChange = { expandedYear = it },
                    years = years
                )
                2 -> InterestsStep(
                    state = state,
                    viewModel = viewModel,
                    allInterests = allInterests
                )
                3 -> PrivacyStep(
                    privacyPreview = privacyPreview,
                    onPrivacyPreviewChange = { privacyPreview = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                SecondaryButton(
                    text = "← Previous",
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f)
                )
            }
            if (currentStep < totalSteps - 1) {
                PrimaryButton(
                    text = "Next →",
                    onClick = { currentStep++ },
                    modifier = Modifier.weight(1f)
                )
            } else {
                PrimaryButton(
                    text = "Save & Continue",
                    onClick = {
                        // Convert photo URIs to string list for the profile
                        val photoStrings = photoUris.filterNotNull().map { it.toString() }
                        viewModel.updatePhotos(photoStrings)
                        viewModel.saveProfile()
                        onComplete()
                    },
                    isLoading = state.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ──────────────────── Step 0: Photo Upload ────────────────────

@Composable
private fun PhotoUploadStep(
    photoUris: List<Uri?>,
    onPhotoUrisChanged: (List<Uri?>) -> Unit
) {
    Text(
        text = "Upload Your Photos",
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Add up to 4 photos to your profile. Your first photo will be your main profile picture.",
        fontSize = 14.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
    )
    Spacer(modifier = Modifier.height(24.dp))

    // 2x2 photo grid
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            PhotoSlot(index = 0, uri = photoUris[0], isMain = true) { uri ->
                onPhotoUrisChanged(photoUris.toMutableList().also { it[0] = uri })
            }
            PhotoSlot(index = 1, uri = photoUris[1]) { uri ->
                onPhotoUrisChanged(photoUris.toMutableList().also { it[1] = uri })
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            PhotoSlot(index = 2, uri = photoUris[2]) { uri ->
                onPhotoUrisChanged(photoUris.toMutableList().also { it[2] = uri })
            }
            PhotoSlot(index = 3, uri = photoUris[3]) { uri ->
                onPhotoUrisChanged(photoUris.toMutableList().also { it[3] = uri })
            }
        }
    }
}

@Composable
private fun PhotoSlot(
    index: Int,
    uri: Uri?,
    isMain: Boolean = false,
    onPhotoSelected: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { selectedUri: Uri? ->
        selectedUri?.let { onPhotoSelected(it) }
    }

    Surface(
        modifier = Modifier.size(140.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceMuted,
        border = BorderStroke(
            width = if (isMain) 2.dp else 1.dp,
            color = if (isMain) PrimaryGreen else BorderDefault
        ),
        onClick = { launcher.launch("image/*") }
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Photo ${index + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
                // Remove badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp),
                    shape = RoundedCornerShape(50),
                    color = StatusError.copy(alpha = 0.9f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = "Add photo",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isMain) "Main Photo" else "Add Photo",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ──────────────────── Step 1: Basic Info ────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicInfoStep(
    state: com.dbuconnect.presentation.viewmodels.ProfileState,
    viewModel: ProfileViewModel,
    selectedCollege: String,
    onCollegeSelected: (String) -> Unit,
    expandedCollege: Boolean,
    onExpandedCollegeChange: (Boolean) -> Unit,
    expandedDept: Boolean,
    onExpandedDeptChange: (Boolean) -> Unit,
    expandedYear: Boolean,
    onExpandedYearChange: (Boolean) -> Unit,
    years: List<Int>
) {
    val departmentsForCollege = collegesDepartments[selectedCollege] ?: emptyList()

    Text(
        text = "Basic Information",
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Tell us about yourself so others can find you.",
        fontSize = 14.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Full Name
    OutlinedTextField(
        value = state.editName,
        onValueChange = { viewModel.updateName(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Full Name") },
        placeholder = { Text("Full Name", color = TextTertiary) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = BorderDefault,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    // College dropdown (first level)
    ExposedDropdownMenuBox(
        expanded = expandedCollege,
        onExpandedChange = onExpandedCollegeChange
    ) {
        OutlinedTextField(
            value = selectedCollege,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("College") },
            placeholder = { Text("Select College", color = TextTertiary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCollege) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderDefault,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        ExposedDropdownMenu(
            expanded = expandedCollege,
            onDismissRequest = { onExpandedCollegeChange(false) }
        ) {
            collegesDepartments.keys.forEach { college ->
                DropdownMenuItem(
                    text = { Text(college) },
                    onClick = {
                        onCollegeSelected(college)
                        // Reset department when college changes
                        viewModel.updateDepartment("")
                        onExpandedCollegeChange(false)
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Department dropdown (second level - filtered by college)
    ExposedDropdownMenuBox(
        expanded = expandedDept,
        onExpandedChange = { if (selectedCollege.isNotEmpty()) onExpandedDeptChange(it) }
    ) {
        OutlinedTextField(
            value = state.editDepartment,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Department") },
            placeholder = {
                Text(
                    if (selectedCollege.isEmpty()) "Select college first" else "Select Department",
                    color = TextTertiary
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDept) },
            shape = RoundedCornerShape(12.dp),
            enabled = selectedCollege.isNotEmpty(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderDefault,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledBorderColor = BorderDefault.copy(alpha = 0.5f),
                disabledContainerColor = SurfaceMuted
            )
        )
        ExposedDropdownMenu(
            expanded = expandedDept,
            onDismissRequest = { onExpandedDeptChange(false) }
        ) {
            departmentsForCollege.forEach { dept ->
                DropdownMenuItem(
                    text = { Text(dept) },
                    onClick = {
                        viewModel.updateDepartment(dept)
                        onExpandedDeptChange(false)
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Year dropdown
    ExposedDropdownMenuBox(
        expanded = expandedYear,
        onExpandedChange = onExpandedYearChange
    ) {
        OutlinedTextField(
            value = if (state.editYear > 0) "Year ${state.editYear}" else "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Year") },
            placeholder = { Text("Year", color = TextTertiary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderDefault,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        ExposedDropdownMenu(
            expanded = expandedYear,
            onDismissRequest = { onExpandedYearChange(false) }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text("Year $year") },
                    onClick = {
                        viewModel.updateYear(year)
                        onExpandedYearChange(false)
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Bio
    OutlinedTextField(
        value = state.editBio,
        onValueChange = { viewModel.updateBio(it) },
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        label = { Text("Bio (Optional)") },
        placeholder = { Text("Tell us about yourself...", color = TextTertiary) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = BorderDefault,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        maxLines = 4
    )
}

// ──────────────────── Step 2: Interests (Optional) ────────────────────

@Composable
private fun InterestsStep(
    state: com.dbuconnect.presentation.viewmodels.ProfileState,
    viewModel: ProfileViewModel,
    allInterests: List<String>
) {
    Text(
        text = "Your Interests",
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(4.dp))
    Surface(
        shape = RoundedCornerShape(50),
        color = PrimaryGreenContainer
    ) {
        Text(
            text = "Optional",
            fontSize = 12.sp,
            color = PrimaryGreen,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Select interests to find people with similar hobbies.",
        fontSize = 14.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allInterests.forEach { interest ->
            DBUChip(
                label = interest,
                selected = state.editInterests.contains(interest),
                onClick = { viewModel.toggleInterest(interest) }
            )
        }
    }
}

// ──────────────────── Step 3: Privacy (Optional) ────────────────────

@Composable
private fun PrivacyStep(
    privacyPreview: Boolean,
    onPrivacyPreviewChange: (Boolean) -> Unit
) {
    Text(
        text = "Privacy Settings",
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(4.dp))
    Surface(
        shape = RoundedCornerShape(50),
        color = PrimaryGreenContainer
    ) {
        Text(
            text = "Optional",
            fontSize = 12.sp,
            color = PrimaryGreen,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Control what others can see on your profile.",
        fontSize = 14.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Privacy Preview",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Switch(
            checked = privacyPreview,
            onCheckedChange = onPrivacyPreviewChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = BorderDefault
            )
        )
    }
}
