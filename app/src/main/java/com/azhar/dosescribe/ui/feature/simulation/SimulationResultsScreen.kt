package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimulationResultsScreen(score: CaseScore, onFinish: () -> Unit) {
    Scaffold(containerColor = SimSurface) { padding ->
        SimulationResultContent(
            score = score,
            modifier = Modifier.fillMaxSize().padding(padding),
            onFinish = onFinish
        )
    }
}

@Composable
fun SimulationResultContent(
    score: CaseScore,
    modifier: Modifier = Modifier,
    onFinish: (() -> Unit)? = null
) {
    val mins = score.timeSeconds / 60
    val secs = score.timeSeconds % 60
    val barColor = if (score.passed) SimSuccess else SimDanger
    val bg = if (score.passed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header / overall score
        item {
            Card(colors = CardDefaults.cardColors(containerColor = bg), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (score.passed) "Case Passed" else "Needs Improvement",
                        fontWeight = FontWeight.Bold, color = barColor, fontSize = 16.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("${score.correct} / ${score.total}", fontSize = 36.sp,
                        fontWeight = FontWeight.Bold, color = barColor)
                    Text("${score.percent}%  ·  ${mins}m ${secs}s",
                        fontSize = 12.sp, color = SimMuted)
                }
            }
        }

        // ── Action Checklist (Step 5) ──
        if (score.actionChecklist.isNotEmpty()) {
            item {
                Text(
                    "Action Checklist",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = SimDeepBlue, modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(score.actionChecklist) { ck ->
                ChecklistRow(ck)
            }
        }

        // ── Per-drug field comparison cards ──
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                "Drug-by-Drug Review",
                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = SimDeepBlue
            )
        }
        // Per-drug cards
        items(score.drugResults) { dr ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(dr.drugDisplay, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            modifier = Modifier.weight(1f))
                        Text("${dr.correctCount}/${dr.total}",
                            color = SimDeepBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    dr.fields.forEach { f ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                if (f.correct) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                null,
                                tint = if (f.correct) SimSuccess else SimDanger,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(f.field, fontSize = 11.sp, color = SimMuted, fontWeight = FontWeight.SemiBold)
                                Text("Expected: ${f.expected}", fontSize = 11.sp, color = Color(0xFF333333))
                                Text("Yours:    ${f.actual}", fontSize = 11.sp,
                                    color = if (f.correct) SimSuccess else SimDanger)
                            }
                        }
                    }
                }
            }
        }

        // Final summary
        item {
            Card(colors = CardDefaults.cardColors(containerColor = SimSurface),
                shape = RoundedCornerShape(10.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Session Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    SummaryRow("Time taken", "${mins}m ${secs}s")
                    SummaryRow("Calculator tools used", score.calculatorsUsed.toString())
                    SummaryRow("Chat questions asked", score.chatQuestionsAsked.toString())
                    SummaryRow("Notes length", "${score.notesLength} chars")
                }
            }
        }

        if (onFinish != null) {
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue),
                    shape = RoundedCornerShape(24.dp)
                ) { Text("Finish & Save", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = SimMuted)
        Text(value, fontSize = 12.sp, color = SimDeepBlue, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ChecklistRow(item: ChecklistItem) {
    val (icon, tint, bg) = when (item.status) {
        ChecklistStatus.DONE -> Triple(Icons.Filled.CheckCircle, SimSuccess, Color(0xFFEAF5EC))
        ChecklistStatus.MISSED -> Triple(Icons.Filled.Cancel, SimDanger, Color(0xFFFFEBEE))
        ChecklistStatus.NOT_NEEDED -> Triple(Icons.Filled.RadioButtonUnchecked, SimMuted, Color(0xFFF1F3F6))
    }
    Card(colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(10.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp).padding(top = 2.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF222222))
                Text("Your action: ${item.userValue}", fontSize = 11.sp, color = Color(0xFF333333))
                Text("Expected:    ${item.expectedValue}", fontSize = 11.sp, color = SimMuted)
                if (item.note.isNotBlank()) {
                    Text(item.note, fontSize = 10.sp, color = tint, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

