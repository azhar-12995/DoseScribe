package com.azhar.dosescribe.ui.feature.tests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val BrandBlue = Color(0xFF0982BA)
private val BrandBlueLight = Color(0xFFE8F4F8)
private val SurfaceBg = Color(0xFFF5F7FA)
private val CorrectGreen = Color(0xFF2E7D32)
private val WrongRed = Color(0xFFC62828)

@Composable
fun TestResultScreen(
    navController: NavController,
    moduleId: String,
    testType: String,
    score: Int,
    total: Int
) {
    val percentage = (score.toFloat() / total) * 100
    val isPassed = percentage >= 80

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isPassed) {
                // Pass State
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(CorrectGreen.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        null,
                        tint = CorrectGreen,
                        modifier = Modifier.size(80.dp)
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    "Congratulations!",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.DarkGray
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "You've successfully passed the ${if (testType == "pre") "Pre" else "Post"}-Questionnaire.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            } else {
                // Fail State
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(WrongRed.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.SentimentDissatisfied,
                        null,
                        tint = WrongRed,
                        modifier = Modifier.size(80.dp)
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    "Not quite — review and try again",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "Don't worry, you can always retry to improve your score.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Score Display
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Your Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        "$score / $total",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPassed) CorrectGreen else WrongRed
                        )
                    )
                    Text(
                        "${percentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(Modifier.height(48.dp))
            
            if (isPassed) {
                if (testType == "post") {
                    Button(
                        onClick = { navController.navigate("certificates") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.CardMembership, null)
                        Spacer(Modifier.width(8.dp))
                        Text("View Certificate")
                    }
                    Spacer(Modifier.height(12.dp))
                }
                
                Button(
                    onClick = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (testType == "post") Color.Gray else BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Return Home")
                }
            } else {
                Button(
                    onClick = { navController.navigate("take_test/$moduleId/$testType") { popUpTo("take_test/$moduleId/$testType") { inclusive = true } } },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Retry Test")
                }
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { navController.navigate("lesson_steps/$moduleId") { popUpTo("lesson_steps/$moduleId") { inclusive = true } } },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue)
                ) {
                    Text("Revisit Lesson")
                }
            }
        }
    }
}
