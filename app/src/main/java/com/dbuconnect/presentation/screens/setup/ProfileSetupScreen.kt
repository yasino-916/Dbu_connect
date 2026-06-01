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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        "Civil Engineering", "Electrical and Computer Engineering", "Mechanical Engineering",
        "Chemical Engineering", "Industrial Engineering", "Water Resources and Irrigation Engineering"
    ),
    "College of Computing" to listOf(
        "Computer Science", "Information Technology", "Information Systems", "Software Engineering"
    ),
    "College of Natural and Computational Sciences" to listOf(
        "Mathematics", "Physics", "Chemistry", "Biology", "Statistics", "Environmental Science"
    ),
    "College of Agriculture and Natural Resource" to listOf(
        "Plant Science", "Animal Science", "Horticulture", "Natural Resource Management",
        "Agricultural Economics", "Rural Development and Agricultural Extension"
    ),
    "College of Business and Economics" to listOf(
        "Accounting and Finance", "Management", "Economics", "Marketing Management",
        "Public Administration and Development Management", "Logistics and Supply Chain Management"
    ),
    "College of Social Sciences and Humanities" to listOf(
        "History and Heritage Management", "Geography and Environmental Studies", "Sociology",
        "Psychology", "Journalism and Communication", "Tourism and Hotel Management",
        "English Language and Literature", "Amharic Language and Literature"
    ),
    "College of Education" to listOf(
        "Biology Education", "Chemistry Education", "Mathematics Education",
        "Physics Education", "English Language Teaching", "Special Needs and Inclusive Education"
    ),
    "School of Law" to listOf(
        "Law (LLB)"
    ),
    "Asrat Woldeyes Health Science Campus" to listOf(
        "Medicine (MBBS)", "Pharmacy", "Medical Laboratory Science", "Nursing",
        "Midwifery", "Public Health", "Anesthesia", "Physiotherapy"
    ),
    "Mehal-Meda Campus (Highland Agriculture & Tourism)" to listOf(
        "Highland Agriculture", "Tourism and Hotel Management"
    )
)

fun getDepartmentDuration(dept: String): Int {
    return when (dept) {
        "Medicine (MBBS)" -> 8
        
        "Civil Engineering",
        "Electrical and Computer Engineering",
        "Mechanical Engineering",
        "Chemical Engineering",
        "Industrial Engineering",
        "Water Resources and Irrigation Engineering",
        "Software Engineering",
        "Law (LLB)",
        "Pharmacy" -> 5
        
        "Accounting and Finance",
        "Management",
        "Economics",
        "Marketing Management",
        "Public Administration and Development Management",
        "Logistics and Supply Chain Management",
        "History and Heritage Management",
        "Geography and Environmental Studies",
        "Sociology",
        "Psychology",
        "Journalism and Communication",
        "Tourism and Hotel Management",
        "English Language and Literature",
        "Amharic Language and Literature",
        "Biology Education",
        "Chemistry Education",
        "Mathematics Education",
        "Physics Education",
        "English Language Teaching",
        "Special Needs and Inclusive Education" -> 3
        
        else -> 4
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onComplete: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Navigate only after save succeeds (issue #6)
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.clearSaveSuccess()
            onComplete()
        }
    }

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

    // Auto-resolve college from loaded department (for Edit Profile mode)
    LaunchedEffect(state.editDepartment) {
        if (state.editDepartment.isNotEmpty() && selectedCollege.isEmpty()) {
            val college = collegesDepartments.entries.find { it.value.contains(state.editDepartment) }?.key
            if (college != null) {
                selectedCollege = college
            }
        }
    }

    val duration = getDepartmentDuration(state.editDepartment)
    val years = (1..duration).toList()
    val allInterests = listOf(
        "Studying", "Coffee", "Sports", "Music", "Volunteering",
        "Photography", "Travel", "Gaming", "Art", "Reading",
        "Dancing", "Fitness", "Technology", "Cooking", "Movies"
    )

    val progress = (currentStep + 1).toFloat() / totalSteps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .systemBarsPadding()
            .imePadding()
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
            } else if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            // Skip button (visible on steps that are optional or have optional data)
            val isStepOptional = currentStep == 0 || currentStep == 2 || currentStep == 3
            if (isStepOptional) {
                TextButton(
                    onClick = {
                        when (currentStep) {
                            0 -> currentStep = 1
                            2 -> currentStep = 3
                            3 -> {
                                val photoStrings = photoUris.filterNotNull().map { it.toString() }
                                viewModel.updatePhotos(photoStrings)
                                if (viewModel.validateProfile()) {
                                    viewModel.saveProfile()
                                }
                            }
                        }
                    }
                ) {
                    Text("Skip", color = PrimaryGreen, fontSize = 14.sp)
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
                    onPhotoUrisChanged = { photoUris = it },
                    error = state.photosError
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
                    state = state,
                    viewModel = viewModel
                )
            }

            // Show save error if any
            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.error!!,
                    color = StatusError,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
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
                    text = "Previous",
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f)
                )
            } else if (onBack != null) {
                SecondaryButton(
                    text = "Previous",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
            }
            if (currentStep < totalSteps - 1) {
                PrimaryButton(
                    text = "Next",
                    onClick = {
                        // Validate before moving to next step
                        when (currentStep) {
                            0 -> {
                                // Update photos first so validation can check
                                val photoStrings = photoUris.filterNotNull().map { it.toString() }
                                viewModel.updatePhotos(photoStrings)
                                if (photoStrings.isEmpty()) {
                                    viewModel.updatePhotos(emptyList()) // trigger error
                                    // Don't block — photos are optional for next step
                                }
                                currentStep++
                            }
                            else -> currentStep++
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                PrimaryButton(
                    text = "Save & Continue",
                    onClick = {
                        // Convert photo URIs to string list for the profile
                        val photoStrings = photoUris.filterNotNull().map { it.toString() }
                        viewModel.updatePhotos(photoStrings)

                        // Validate required fields (issue #7)
                        if (viewModel.validateProfile()) {
                            // saveProfile() is async; navigation happens via
                            // LaunchedEffect on saveSuccess (issue #6)
                            viewModel.saveProfile()
                        }
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
    onPhotoUrisChanged: (List<Uri?>) -> Unit,
    error: String? = null
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

    if (error != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = error, color = StatusError, fontSize = 13.sp)
    }

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
        label = { Text("Full Name *") },
        placeholder = { Text("Full Name", color = TextTertiary) },
        isError = state.nameError != null,
        supportingText = state.nameError?.let { { Text(it, color = StatusError) } },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = BorderDefault,
            focusedContainerColor = BackgroundWhite,
            unfocusedContainerColor = BackgroundWhite
        ),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    // College dropdown (first level)
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedCollege,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("College *") },
            placeholder = { Text("Select College", color = TextTertiary) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderDefault,
                focusedContainerColor = BackgroundWhite,
                unfocusedContainerColor = BackgroundWhite
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onExpandedCollegeChange(true) }
        )
    }

    if (expandedCollege) {
        DBUSelectorBottomSheet(
            title = "Select College",
            items = collegesDepartments.keys.toList(),
            onItemSelected = { college ->
                onCollegeSelected(college)
                // Reset department when college changes
                viewModel.updateDepartment("")
            },
            onDismiss = { onExpandedCollegeChange(false) }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Department dropdown (second level - filtered by college)
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.editDepartment,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Department *") },
            placeholder = {
                Text(
                    if (selectedCollege.isEmpty()) "Select college first" else "Select Department",
                    color = TextTertiary
                )
            },
            isError = state.departmentError != null,
            supportingText = state.departmentError?.let { { Text(it, color = StatusError) } },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary) },
            shape = RoundedCornerShape(12.dp),
            enabled = selectedCollege.isNotEmpty(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderDefault,
                focusedContainerColor = BackgroundWhite,
                unfocusedContainerColor = BackgroundWhite,
                disabledBorderColor = BorderDefault.copy(alpha = 0.5f),
                disabledContainerColor = SurfaceMuted
            )
        )
        if (selectedCollege.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onExpandedDeptChange(true) }
            )
        }
    }

    if (expandedDept) {
        DBUSelectorBottomSheet(
            title = "Select Department",
            items = departmentsForCollege,
            onItemSelected = { dept ->
                viewModel.updateDepartment(dept)
                val maxYears = getDepartmentDuration(dept)
                if (state.editYear > maxYears) {
                    viewModel.updateYear(maxYears)
                }
            },
            onDismiss = { onExpandedDeptChange(false) }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Year dropdown
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = if (state.editYear > 0) "Year ${state.editYear}" else "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Year *") },
            placeholder = { Text("Year", color = TextTertiary) },
            isError = state.yearError != null,
            supportingText = state.yearError?.let { { Text(it, color = StatusError) } },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderDefault,
                focusedContainerColor = BackgroundWhite,
                unfocusedContainerColor = BackgroundWhite
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onExpandedYearChange(true) }
        )
    }

    if (expandedYear) {
        DBUSelectorBottomSheet(
            title = "Select Year",
            items = years.map { "Year $it" },
            onItemSelected = { yearString ->
                val year = yearString.substringAfter("Year ").toIntOrNull() ?: 1
                viewModel.updateYear(year)
            },
            onDismiss = { onExpandedYearChange(false) }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Bio
    OutlinedTextField(
        value = state.editBio,
        onValueChange = { if (it.length <= 300) viewModel.updateBio(it) },
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        label = { Text("Bio (Optional)") },
        placeholder = { Text("Tell us about yourself...", color = TextTertiary) },
        supportingText = { Text("${state.editBio.length}/300", color = TextTertiary) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = BorderDefault,
            focusedContainerColor = BackgroundWhite,
            unfocusedContainerColor = BackgroundWhite
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
// Issue #9: Privacy step now persists settings via viewModel

@Composable
private fun PrivacyStep(
    state: com.dbuconnect.presentation.viewmodels.ProfileState,
    viewModel: ProfileViewModel
) {
    val privacy = state.privacySettings

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

    // Show Department toggle
    PrivacyToggleRow(
        title = "Show Department",
        description = "Allow others to see your department",
        checked = privacy.showDepartment,
        onCheckedChange = {
            viewModel.updatePrivacySetting(privacy.copy(showDepartment = it))
        }
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Show Year toggle
    PrivacyToggleRow(
        title = "Show Year",
        description = "Allow others to see your year",
        checked = privacy.showYear,
        onCheckedChange = {
            viewModel.updatePrivacySetting(privacy.copy(showYear = it))
        }
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Hide Profile toggle
    PrivacyToggleRow(
        title = "Hide Profile",
        description = "Your profile won't appear in discover",
        checked = privacy.hideProfile,
        onCheckedChange = {
            viewModel.updatePrivacySetting(privacy.copy(hideProfile = it))
        }
    )
}

@Composable
private fun PrivacyToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DBUSelectorBottomSheet(
    title: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BackgroundWhite,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderDefault) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderDefault))
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                items(items) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemSelected(item)
                                onDismiss()
                            },
                        color = BackgroundWhite
                    ) {
                        Column {
                            Text(
                                text = item,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(BorderDefault.copy(alpha = 0.5f)))
                        }
                    }
                }
            }
        }
    }
}

