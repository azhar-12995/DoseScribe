package com.azhar.dosescribe.ui.feature.tests

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.azhar.dosescribe.ui.feature.dashboard.allModules
import com.azhar.dosescribe.ui.feature.lessons.getMcqsForModule

private val BrandBlue = Color(0xFF0982BA)
private val SurfaceBg = Color(0xFFF5F7FA)

@Composable
fun TestsScreen(
    navController: NavController,
) {
    var expandedModuleId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 2.dp, color = Color.White) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.DarkGray)
                    }
                    Text(
                        "Tests",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allModules) { module ->
                val isExpanded = expandedModuleId == module.id
                val questions = getMcqsForModule(module.id)
                
                TestModuleCard(
                    moduleTitle = module.title,
                    moduleIcon = module.icon,
                    moduleColor = module.iconBg,
                    isExpanded = isExpanded,
                    onClick = {
                        expandedModuleId = if (isExpanded) null else module.id
                    }
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        // Pre-Questionnaire Row
                        TestRow(
                            title = "Pre-Questionnaire",
                            questionCount = questions.size,
                            durationMin = questions.size,
                            onClick = {
                                navController.navigate("take_test/${module.id}/pre")
                            }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(0.3f))
                        
                        // Post-Questionnaire Row
                        TestRow(
                            title = "Post-Questionnaire",
                            questionCount = questions.size,
                            durationMin = questions.size,
                            onClick = {
                                navController.navigate("take_test/${module.id}/post")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TestModuleCard(
    moduleTitle: String,
    moduleIcon: ImageVector,
    moduleColor: Color,
    isExpanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(moduleColor.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(moduleIcon, null, tint = moduleColor, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    moduleTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null,
                    tint = Color.Gray
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                content()
            }
        }
    }
}

@Composable
private fun TestRow(
    title: String,
    questionCount: Int,
    durationMin: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.HelpOutline, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("$questionCount Questions", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Filled.Timer, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("$durationMin min", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.ChevronRight, null, tint = BrandBlue, modifier = Modifier.size(20.dp))
    }
}
