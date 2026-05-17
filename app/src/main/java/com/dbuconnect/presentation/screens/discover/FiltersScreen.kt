package com.dbuconnect.presentation.screens.discover

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dbuconnect.data.models.FilterSettings
import com.dbuconnect.presentation.components.DBUChip
import com.dbuconnect.presentation.components.PrimaryButton
import com.dbuconnect.presentation.screens.setup.collegesDepartments
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.DiscoverViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    onBack: () -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val filters by viewModel.filters.collectAsState()
    var selectedDepts by remember { mutableStateOf(filters.departments) }
    var yearMin by remember { mutableFloatStateOf(filters.yearRange.first.toFloat()) }
    var yearMax by remember { mutableFloatStateOf(filters.yearRange.last.toFloat()) }
    var intent by remember { mutableStateOf(filters.intent) }

    // Use college->department mapping for organized filtering
    var expandedCollege by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Refine Search", 
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
                color = Color.White
            ) {
                PrimaryButton(
                    text = "Apply Filters",
                    onClick = {
                        viewModel.updateFilters(
                            FilterSettings(
                                departments = selectedDepts,
                                yearRange = yearMin.toInt()..yearMax.toInt(),
                                intent = intent
                            )
                        )
                        onBack()
                    },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Department Filter Card
            FilterSectionCard(
                title = "Department",
                subtitle = "Filter by college to find students in specific departments"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    collegesDepartments.forEach { (college, depts) ->
                        val isExpanded = expandedCollege == college
                        val selectedCount = depts.count { it in selectedDepts }
                        
                        Surface(
                            onClick = {
                                expandedCollege = if (isExpanded) null else college
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isExpanded) PrimaryGreenContainer else Color.White,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selectedCount > 0) PrimaryGreen.copy(alpha = 0.5f) else BorderDefault
                            )
                        ) {
                            Column(
                                modifier = Modifier.animateContentSize()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = college,
                                        fontSize = 15.sp,
                                        fontWeight = if (isExpanded) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isExpanded) PrimaryGreenDark else TextPrimary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (selectedCount > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = PrimaryGreen,
                                                modifier = Modifier.padding(end = 8.dp)
                                            ) {
                                                Text(
                                                    text = "$selectedCount",
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = if (isExpanded) PrimaryGreen else TextSecondary
                                        )
                                    }
                                }

                                if (isExpanded) {
                                    HorizontalDivider(color = PrimaryGreen.copy(alpha = 0.1f))
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        depts.forEach { dept ->
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
                                }
                            }
                        }
                    }
                }
            }

            // Year Range Card
            FilterSectionCard(
                title = "Year Range",
                subtitle = "Year ${yearMin.toInt()} - ${yearMax.toInt()}"
            ) {
                RangeSlider(
                    value = yearMin..yearMax,
                    onValueChange = { range ->
                        yearMin = range.start
                        yearMax = range.endInclusive
                    },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = PrimaryGreen,
                        inactiveTrackColor = BorderDefault
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                // Markers
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..5).forEach { year ->
                        Text(
                            text = year.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Intent Card
            FilterSectionCard(
                title = "Looking for",
                subtitle = "What kind of connection are you seeking?"
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("", "Friends", "Dating", "Study Buddy").forEach { item ->
                        DBUChip(
                            label = if (item.isEmpty()) "Any Connection" else item,
                            selected = intent == item,
                            onClick = { intent = item }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun FilterSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
