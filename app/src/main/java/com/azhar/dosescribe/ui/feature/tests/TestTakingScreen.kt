package com.azhar.dosescribe.ui.feature.tests

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.azhar.dosescribe.ui.feature.lessons.LessonProgressViewModel
import com.azhar.dosescribe.ui.feature.lessons.getMcqsForModule

private val BrandBlue = Color(0xFF0982BA)
private val BrandBlueLight = Color(0xFFE8F4F8)
private val SurfaceBg = Color(0xFFF5F7FA)

@Composable
fun TestTakingScreen(
    navController: NavController,
    moduleId: String,
    testType: String, // "pre" or "post"
    progressVm: LessonProgressViewModel = hiltViewModel()
) {
    val questions = getMcqsForModule(moduleId)
    
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateListOf(*IntArray(questions.size) { -1 }.toTypedArray()) }
    
    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            Surface(tonalElevation = 4.dp, shadowElevation = 4.dp, color = Color.White) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.DarkGray)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (testType == "pre") "Pre-Questionnaire" else "Post-Questionnaire",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandBlue
                            )
                        }
                    }
                    
                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = BrandBlue,
                        trackColor = BrandBlueLight
                    )
                }
            }
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Previous Button
                    OutlinedButton(
                        onClick = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                        enabled = currentQuestionIndex > 0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue),
                        border = BorderStroke(1.5.dp, if (currentQuestionIndex > 0) BrandBlue else Color.LightGray)
                    ) {
                        Icon(Icons.Filled.ChevronLeft, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Previous", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    // Next / Submit Button
                    val isLast = currentQuestionIndex == questions.size - 1
                    val canProceed = answers[currentQuestionIndex] != -1
                    
                    Button(
                        onClick = {
                            if (isLast) {
                                // Submit
                                if (testType == "pre") {
                                    progressVm.savePreAnswers(moduleId, answers.toList())
                                    progressVm.completeStep(moduleId, 0)
                                } else {
                                    progressVm.savePostAnswers(moduleId, answers.toList())
                                    progressVm.completeStep(moduleId, 3)
                                }
                                
                                // End test and go back to lesson steps where results are shown in step 5
                                navController.popBackStack()
                            } else {
                                currentQuestionIndex++
                            }
                        },
                        enabled = canProceed,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text(if (isLast) "Submit" else "Next", fontWeight = FontWeight.Bold)
                        if (!isLast) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.ChevronRight, null)
                        }
                    }
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = currentQuestionIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }.using(
                    SizeTransform(clip = false)
                )
            },
            label = "QuestionTransition"
        ) { targetIndex ->
            val question = questions.getOrNull(targetIndex)
            if (question != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text(
                        question.question,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp
                        ),
                        color = Color.DarkGray
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    question.options.forEachIndexed { index, option ->
                        val isSelected = answers[targetIndex] == index
                        
                        OptionCard(
                            text = option,
                            isSelected = isSelected,
                            onClick = { answers[targetIndex] = index }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BrandBlueLight else Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, BrandBlue) else BorderStroke(1.dp, Color.LightGray.copy(0.4f)),
        elevation = CardDefaults.cardElevation(if (isSelected) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) BrandBlue else Color.White)
                    .border(2.dp, if (isSelected) BrandBlue else Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    lineHeight = 24.sp
                ),
                color = if (isSelected) BrandBlue else Color.DarkGray
            )
        }
    }
}
