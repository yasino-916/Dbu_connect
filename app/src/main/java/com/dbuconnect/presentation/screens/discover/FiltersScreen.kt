package com.dbuconnect.presentation.screens.discover

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dbuconnect.data.models.FilterSettings
import com.dbuconnect.presentation.components.DBUChip
import com.dbuconnect.presentation.components.PrimaryButton
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.DiscoverViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    onBack: () -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val filters by viewModel.filters.collectAsState()
    var distance by remember { mutableFloatStateOf(filters.maxDistance) }
    var campusOnly by remember { mutableStateOf(filters.campusOnly) }
    var selectedDepts by remember { mutableStateOf(filters.departments) }
    var yearMin by remember { mutableFloatStateOf(filters.yearRange.first.toFloat()) }
    var yearMax by remember { mutableFloatStateOf(filters.yearRange.last.toFloat()) }
    var intent by remember { mutableStateOf(filters.intent) }

    val departments = listOf(
        "Computer Science", "Electrical Engineering", "Civil Engineering",
        "Business Administration", "Medicine", "Law", "Architecture",
        "Chemistry", "Physics", "Mathematics"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filters", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                PrimaryButton(
                    text = "Apply Filters",
                    onClick = {
                        viewModel.updateFilters(
                            FilterSettings(
                                maxDistance = distance,
                                departments = selectedDepts,
                                yearRange = yearMin.toInt()..yearMax.toInt(),
                                intent = intent,
                                campusOnly = campusOnly
                            )
                        )
                        onBack()
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Campus Mode Toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (campusOnly) PrimaryGreenContainer else SurfaceMuted,
                border = BorderStroke(1.dp, if (campusOnly) PrimaryGreen else BorderDefault)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Campus Mode", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("DBU students only", color = TextSecondary, fontSize = 13.sp)
                    }
                    Switch(
                        checked = campusOnly,
                        onCheckedChange = { campusOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = BorderDefault
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Distance
            Text("Distance", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${distance.toInt()} km radius", color = TextSecondary, fontSize = 13.sp)
            Slider(
                value = distance,
                onValueChange = { distance = it },
                valueRange = 1f..20f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryGreen,
                    activeTrackColor = PrimaryGreen,
                    inactiveTrackColor = BorderDefault
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Department
            Text("Department", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                departments.forEach { dept ->
                    DBUChip(
                        label = dept,
                        selected = selectedDepts.contains(dept),
                        onClick = {
                            selectedDepts = if (selectedDepts.contains(dept)) {
                                selectedDepts - dept
                            } else {
                                selectedDepts + dept
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Year Range
            Text("Year Range", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Year ${yearMin.toInt()} - ${yearMax.toInt()}", color = TextSecondary, fontSize = 13.sp)
            RangeSlider(
                value = yearMin..yearMax,
                onValueChange = { range ->
                    yearMin = range.start
                    yearMax = range.endInclusive
                },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryGreen,
                    activeTrackColor = PrimaryGreen,
                    inactiveTrackColor = BorderDefault
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Intent
            Text("Looking for", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("", "Friends", "Dating", "Study Buddy").forEach { item ->
                    DBUChip(
                        label = if (item.isEmpty()) "Any" else item,
                        selected = intent == item,
                        onClick = { intent = item }
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
