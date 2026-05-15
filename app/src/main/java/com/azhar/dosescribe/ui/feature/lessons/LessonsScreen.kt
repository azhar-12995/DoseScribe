package com.azhar.dosescribe.ui.feature.lessons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.azhar.dosescribe.ui.feature.dashboard.ModuleInfo
import com.azhar.dosescribe.ui.feature.dashboard.allModules
import com.azhar.dosescribe.ui.feature.simulation.CaseScore
import com.azhar.dosescribe.ui.feature.simulation.DrugResult
import com.azhar.dosescribe.ui.feature.simulation.FieldResult
import com.azhar.dosescribe.ui.feature.simulation.SimulationResultContent
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.launch

// ── Brand colours ─────────────────────────────────────────────────
private val BrandBlue = Color(0xFF0982BA)
private val BrandBlueLight = Color(0xFFE8F4F8)
private val SurfaceBg = Color(0xFFF5F7FA)
private val CorrectGreen = Color(0xFF2E7D32)
private val CorrectGreenBg = Color(0xFFE8F5E9)
private val WrongRed = Color(0xFFC62828)
private val WrongRedBg = Color(0xFFFFEBEE)
private val LockedGray = Color(0xFFBDBDBD)
private val QuickSimColor = Color(0xFF0B78B6)

// ── Lesson Step enum ──────────────────────────────────────────────
enum class LessonStep(val label: String, val shortLabel: String) {
    PRE_QUESTIONNAIRE("Step 1: Pre Questionnaire", "Pre-Q"),
    LEARNING_MODULE("Step 2: Learning Module", "Learn"),
    SIMULATION("Step 3: Simulation", "Sim"),
    POST_QUESTIONNAIRE("Step 4: Post Questionnaire", "Post-Q"),
    RESULTS("Step 5: Results", "Results")
}

// ── MCQ data is defined in ModuleMcqs.kt ──────────────────────────

// ── Progress ViewModel (persisted to Firestore, shared across screens) ─────────────────────────
@dagger.hilt.android.lifecycle.HiltViewModel
class LessonProgressViewModel @javax.inject.Inject constructor(
    private val firestore: com.google.firebase.firestore.FirebaseFirestore,
    private val auth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {
    // moduleId -> set of completed step indices (0-4)
    val completedSteps = mutableStateMapOf<String, MutableSet<Int>>()
    // Track a version counter to force recomposition on any change
    var version by mutableStateOf(0)
        private set
    // moduleId -> pre-questionnaire answers
    val preAnswers = mutableStateMapOf<String, List<Int>>()
    // moduleId -> post-questionnaire answers
    val postAnswers = mutableStateMapOf<String, List<Int>>()
    // moduleId -> simulation score (percent)
    val simScores = mutableStateMapOf<String, Int>()
    // moduleId -> simulation detailed results
    val simResults = mutableStateMapOf<String, CaseScore>()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("progress")
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val moduleId = doc.id
                    val steps = (doc.get("completedSteps") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.toMutableSet()
                    if (steps != null) completedSteps[moduleId] = steps
                    val pre = (doc.get("preAnswers") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }
                    if (pre != null) preAnswers[moduleId] = pre
                    val post = (doc.get("postAnswers") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }
                    if (post != null) postAnswers[moduleId] = post
                    val simScore = (doc.get("simScore") as? Number)?.toInt()
                    if (simScore != null) simScores[moduleId] = simScore
                    
                    // Read back detailed simulation result
                    (doc.get("simDetailedResult") as? Map<*, *>)?.let { map ->
                        try {
                            val drugResultsMaps = map["drugResults"] as? List<Map<*, *>> ?: emptyList()
                            val drugResults = drugResultsMaps.map { drMap ->
                                val fieldsMaps = drMap["fields"] as? List<Map<*, *>> ?: emptyList()
                                val fields = fieldsMaps.map { fMap ->
                                    FieldResult(
                                        field = fMap["field"] as? String ?: "",
                                        expected = fMap["expected"] as? String ?: "",
                                        actual = fMap["actual"] as? String ?: "",
                                        correct = fMap["correct"] as? Boolean ?: false
                                    )
                                }
                                DrugResult(
                                    drugDisplay = drMap["drugDisplay"] as? String ?: "",
                                    fields = fields
                                )
                            }
                            simResults[moduleId] = CaseScore(
                                caseId = map["caseId"] as? String ?: "",
                                moduleId = moduleId,
                                correct = (map["correct"] as? Number)?.toInt() ?: 0,
                                total = (map["total"] as? Number)?.toInt() ?: 0,
                                drugResults = drugResults,
                                timeSeconds = (map["timeSeconds"] as? Number)?.toLong() ?: 0L,
                                calculatorsUsed = (map["calculatorsUsed"] as? Number)?.toInt() ?: 0,
                                chatQuestionsAsked = (map["chatQuestionsAsked"] as? Number)?.toInt() ?: 0,
                                notesLength = (map["notesLength"] as? Number)?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
    }

    private fun saveToFirestore(moduleId: String) {
        val uid = auth.currentUser?.uid ?: return
        val data = mutableMapOf<String, Any>(
            "completedSteps" to (completedSteps[moduleId]?.toList() ?: emptyList<Int>())
        )
        preAnswers[moduleId]?.let { data["preAnswers"] = it }
        postAnswers[moduleId]?.let { data["postAnswers"] = it }
        simScores[moduleId]?.let { data["simScore"] = it }
        
        // Persist detailed simulation result
        simResults[moduleId]?.let { score ->
            data["simDetailedResult"] = mapOf(
                "caseId" to score.caseId,
                "correct" to score.correct,
                "total" to score.total,
                "timeSeconds" to score.timeSeconds,
                "calculatorsUsed" to score.calculatorsUsed,
                "chatQuestionsAsked" to score.chatQuestionsAsked,
                "notesLength" to score.notesLength,
                "drugResults" to score.drugResults.map { dr ->
                    mapOf(
                        "drugDisplay" to dr.drugDisplay,
                        "fields" to dr.fields.map { f ->
                            mapOf(
                                "field" to f.field,
                                "expected" to f.expected,
                                "actual" to f.actual,
                                "correct" to f.correct
                            )
                        }
                    )
                }
            )
        }

        firestore.collection("users").document(uid).collection("progress")
            .document(moduleId).set(data)
    }

    fun completeStep(moduleId: String, stepIndex: Int) {
        val current = completedSteps[moduleId]?.toMutableSet() ?: mutableSetOf()
        current.add(stepIndex)
        completedSteps[moduleId] = current  // new instance triggers recomposition
        version++
        saveToFirestore(moduleId)
    }

    fun isStepUnlocked(moduleId: String, stepIndex: Int): Boolean {
        if (stepIndex == 0) return true
        val completed = completedSteps[moduleId] ?: return false
        return completed.contains(stepIndex - 1)
    }

    fun isStepCompleted(moduleId: String, stepIndex: Int): Boolean {
        return completedSteps[moduleId]?.contains(stepIndex) == true
    }

    fun isModuleCompleted(moduleId: String): Boolean {
        return (completedSteps[moduleId]?.size ?: 0) == 5
    }

    fun getProgress(moduleId: String): Float {
        val completed = completedSteps[moduleId]?.size ?: 0
        return completed / 5f
    }

    fun getOverallProgress(): Float {
        val totalCompleted = completedSteps.values.sumOf { it.size }
        return totalCompleted / (allModules.size * 5f)
    }

    fun getCompletedLessonCount(): Int {
        return completedSteps.count { it.value.size == 5 }
    }

    fun getInProgressModules(): List<ModuleInfo> {
        return allModules.filter { module ->
            val steps = completedSteps[module.id]?.size ?: 0
            steps in 1..4
        }
    }

    fun getCompletedModules(): List<ModuleInfo> {
        return allModules.filter { module ->
            (completedSteps[module.id]?.size ?: 0) == 5
        }
    }

    fun getNotStartedModules(): List<ModuleInfo> {
        return allModules.filter { module ->
            (completedSteps[module.id]?.size ?: 0) == 0
        }
    }

    fun restartLesson(moduleId: String) {
        completedSteps[moduleId] = mutableSetOf()
        preAnswers.remove(moduleId)
        postAnswers.remove(moduleId)
        version++
        saveToFirestore(moduleId)
    }

    fun savePreAnswers(moduleId: String, answers: List<Int>) {
        preAnswers[moduleId] = answers
        saveToFirestore(moduleId)
    }

    fun savePostAnswers(moduleId: String, answers: List<Int>) {
        postAnswers[moduleId] = answers
        saveToFirestore(moduleId)
    }

    fun saveSimScore(moduleId: String, score: CaseScore) {
        simScores[moduleId] = score.percent
        simResults[moduleId] = score
        saveToFirestore(moduleId)
    }
}

// ════════════════════════════════════════════════════════════════════
//  ALL LESSONS SCREEN (View All with tabs)
// ════════════════════════════════════════════════════════════════════
@Composable
fun LessonsScreen(
    navController: NavController,
    viewModel: LessonsViewModel = hiltViewModel(),
    progressVm: LessonProgressViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Completed", "Not Started")

    val filteredModules = when (selectedTab) {
        1 -> progressVm.getCompletedModules()
        2 -> progressVm.getNotStartedModules()
        else -> allModules
    }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SimpleTopBar(title = "All Lessons", onBackClick = { navController.popBackStack() })

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BrandBlue
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredModules) { module ->
                    AllLessonsCard(
                        module = module,
                        progress = progressVm.getProgress(module.id),
                        onClick = { navController.navigate("lesson_steps/${module.id}") }
                    )
                }
                if (filteredModules.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                when (selectedTab) {
                                    1 -> "No completed lessons yet"
                                    2 -> "All lessons have been started!"
                                    else -> "No lessons available"
                                },
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  LESSON STEPS SCREEN (5 steps per lesson with restart)
// ════════════════════════════════════════════════════════════════════
@Composable
fun LessonStepsScreen(
    navController: NavController,
    moduleId: String,
    progressVm: LessonProgressViewModel = hiltViewModel()
) {
    val module = allModules.find { it.id == moduleId }
    if (module == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Module not found") }; return }

    val progress = progressVm.getProgress(moduleId)
    val isCompleted = progressVm.isModuleCompleted(moduleId)
    val steps = LessonStep.entries
    var showRestartDialog by remember { mutableStateOf(false) }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Restart Lesson") },
            text = { Text("This will reset all progress for this lesson. Your previous scores are saved in your history. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    progressVm.restartLesson(moduleId)
                    showRestartDialog = false
                }) { Text("Restart", color = WrongRed) }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SimpleTopBar(title = module.title, onBackClick = { navController.popBackStack() })

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = BrandBlue)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(module.title, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(4.dp))
                            Text(module.description, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${(progress * 100).toInt()}% Complete", color = Color.White.copy(0.9f), style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.weight(1f))
                                LinearProgressIndicator(progress = { progress }, modifier = Modifier.width(120.dp).height(6.dp).clip(RoundedCornerShape(3.dp)), color = Color.White, trackColor = Color.White.copy(0.3f))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Lesson Steps", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        if (isCompleted) {
                            TextButton(onClick = { showRestartDialog = true }) {
                                Icon(Icons.Filled.Refresh, null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Restart", color = BrandBlue)
                            }
                        }
                    }
                }

                items(steps.size) { idx ->
                    val step = steps[idx]
                    val isUnlocked = progressVm.isStepUnlocked(moduleId, idx)
                    val isStepCompleted = progressVm.isStepCompleted(moduleId, idx)

                    StepCard(
                        step = step,
                        stepIndex = idx,
                        isUnlocked = isUnlocked,
                        isCompleted = isStepCompleted,
                        onClick = {
                            if (isUnlocked) navController.navigate("lesson_step/${moduleId}/${idx}")
                        }
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  STEP CARD
// ════════════════════════════════════════════════════════════════════
@Composable
private fun StepCard(step: LessonStep, stepIndex: Int, isUnlocked: Boolean, isCompleted: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = isUnlocked) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = when { isCompleted -> CorrectGreenBg; isUnlocked -> Color.White; else -> Color(0xFFF5F5F5) }),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 1.dp else 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(when { isCompleted -> CorrectGreen; isUnlocked -> BrandBlue; else -> LockedGray }), contentAlignment = Alignment.Center) {
                when {
                    isCompleted -> Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    isUnlocked -> Text("${stepIndex + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    else -> Icon(Icons.Filled.Lock, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(step.label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = if (isUnlocked) Color.DarkGray else LockedGray)
            }
            if (isUnlocked && !isCompleted) Icon(Icons.Filled.ChevronRight, null, tint = BrandBlue)
            if (isCompleted) Icon(Icons.Filled.Lock, null, tint = CorrectGreen, modifier = Modifier.size(18.dp))
        }
    }
}



// ── Transcript and Video data ─────────────────────────────────────────────
data class TranscriptLine(val time: Float, val timestampLabel: String, val text: String)

val moduleVideos = mapOf(
    "appropriateness_review" to "TfgJSln2V3o",
    "auxiliary_labels" to "S6VziAXfkFs",
    "checklist" to "51Ji48v6Sok",
    "iv_to_oral_switch" to "ECLUgglC7p4",
    "parts_of_prescription" to "ZXJjJA1Q8Oc",
    "narcotic_controlled_medications" to "ulQwSje6jSI",
    "high_alert_medications" to "UUc2UE8_-Pg",
    "tdm_therapeutic_dose_adjustment" to "IgfXYm6ohqg",
    "electrolyte_replacement" to "JcNWAzyGOjw",
    "pediatric_dose_adjustment" to "Z5Jqw0XBtuk",
    "drug_label" to "7IgPv0ePd4c",
    "hepatic_dose_adjustment" to "GkwggKMPwoo",
    "compounding_calculations" to "WWDn-_htHdQ",
    "chemo_dose_adjustments" to "-yJeWmmpzOY",
    "counseling" to "UoUbkqD6CWc"
)

val moduleTranscripts = mapOf(
    "appropriateness_review" to listOf(
        TranscriptLine(0f, "0:00", "Welcome to the Appropriateness Review module. In this section, we will cover the core principles of verifying medication orders."),
        TranscriptLine(10f, "0:10", "First, we must always verify the patient's identity using at least two identifiers to ensure the right medication reaches the right patient."),
        TranscriptLine(26f, "0:26", "Next, we evaluate the clinical indication. Does the prescribed medication align with the patient's diagnosed condition?"),
        TranscriptLine(42f, "0:42", "We also look for therapeutic duplication. Patients should not be on multiple drugs from the same class without a clear clinical reason."),
        TranscriptLine(60f, "1:00", "Dose and frequency are critical. We calculate the appropriate dose based on weight, renal function, and age."),
        TranscriptLine(85f, "1:25", "Finally, we check for potential drug-drug interactions and allergies that could lead to adverse events."),
        TranscriptLine(105f, "1:45", "By following this systematic review, we minimize medication errors and ensure patient safety.")
    ),
    "auxiliary_labels" to listOf(
        TranscriptLine(0f, "0:00", "Introduction to Auxiliary Labels in pharmacy practice."),
        TranscriptLine(15f, "0:15", "Understanding why these labels are crucial for patient safety."),
        TranscriptLine(30f, "0:30", "Common examples include 'Take with food' or 'May cause drowsiness'."),
        TranscriptLine(45f, "0:45", "How to apply them correctly to medication containers.")
    ),
    "checklist" to listOf(
        TranscriptLine(0f, "0:00", "Dispensing Checklist: Ensuring every step is verified."),
        TranscriptLine(20f, "0:20", "The importance of a systematic approach to dispensing."),
        TranscriptLine(40f, "0:40", "Double-checking the drug name, strength, and quantity.")
    ),
    "iv_to_oral_switch" to listOf(
        TranscriptLine(0f, "0:00", "IV to Oral Conversion: Clinical guidelines and benefits."),
        TranscriptLine(25f, "0:25", "Identifying candidates for early switch to oral therapy."),
        TranscriptLine(50f, "0:50", "Calculating bioequivalent doses for common antibiotics.")
    ),
    "parts_of_prescription" to listOf(
        TranscriptLine(0f, "0:00", "Learning the essential components of a legal prescription."),
        TranscriptLine(15f, "0:15", "Superscription, Inscription, Subscription, and Transcription."),
        TranscriptLine(35f, "0:35", "Verifying prescriber information and DEA numbers.")
    ),
    "narcotic_controlled_medications" to listOf(
        TranscriptLine(0f, "0:00", "Safe handling and dispensing of controlled substances."),
        TranscriptLine(20f, "0:20", "Legal requirements for recording and storage."),
        TranscriptLine(45f, "0:45", "Preventing diversion and ensuring patient safety.")
    ),
    "high_alert_medications" to listOf(
        TranscriptLine(0f, "0:00", "Identification and handling of high-alert medications."),
        TranscriptLine(20f, "0:20", "Special protocols for medications with high risk of causing harm."),
        TranscriptLine(40f, "0:40", "Strategies to minimize errors and enhance patient safety.")
    ),
    "tdm_therapeutic_dose_adjustment" to listOf(
        TranscriptLine(0f, "0:00", "Introduction to Therapeutic Drug Monitoring (TDM)."),
        TranscriptLine(25f, "0:25", "Interpreting drug levels and adjusting doses accordingly."),
        TranscriptLine(50f, "0:50", "Case studies on Vancomycin and Aminoglycosides.")
    )
)

// ════════════════════════════════════════════════════════════════════
//  STEP 2: LEARNING VIDEO SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun Step2LearningVideoScreen(
    navController: NavController,
    moduleId: String,
    progressVm: LessonProgressViewModel = hiltViewModel()
) {
    val module = allModules.find { it.id == moduleId }
    val transcript = moduleTranscripts[moduleId] ?: listOf(
        TranscriptLine(0f, "0:00", "Welcome to this module. Please watch the video carefully."),
        TranscriptLine(30f, "0:30", "The video covers important clinical concepts and safety protocols."),
        TranscriptLine(60f, "1:00", "Pay close attention to the concepts shown as they will be in the simulation.")
    )
    val videoId = moduleVideos[moduleId] ?: "TfgJSln2V3o"
    
    var currentTime by remember { mutableFloatStateOf(0f) }
    var youtubePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
    var isVideoLoaded by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    
    val lazyListState = rememberLazyListState()
    
    // Find active transcript index
    val activeIndex = transcript.indexOfLast { it.time <= currentTime }.coerceAtLeast(0)

    // Auto-scroll to active transcript
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && transcript.isNotEmpty()) {
            lazyListState.animateScrollToItem(activeIndex)
        }
    }

    Scaffold(
        containerColor = SurfaceBg,
        bottomBar = {
            Surface(tonalElevation = 8.dp, color = Color.White) {
                Box(Modifier.padding(16.dp)) {
                    Button(
                        onClick = { progressVm.completeStep(moduleId, 1); navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue to Simulation", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Updated Header with greeting and menu icon as requested
            Surface(tonalElevation = 2.dp, shadowElevation = 2.dp, color = Color.White) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.DarkGray) 
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Hi, Pharmacist!", 
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "Learning Module", 
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { /* Open menu if needed */ }) { 
                        Icon(Icons.Default.Menu, "Menu", tint = Color.DarkGray) 
                    }
                }
            }
            
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(16.dp))
                
                // 1. Large rounded video card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(Modifier.fillMaxSize().background(Color.Black)) {
                        if (loadError != null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(loadError!!, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                            }
                        } else {
                            AndroidView(
                                factory = { ctx ->
                                    YouTubePlayerView(ctx).apply {
                                        (ctx as? LifecycleOwner)?.lifecycle?.addObserver(this)
                                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                            override fun onReady(player: YouTubePlayer) {
                                                youtubePlayer = player
                                                player.cueVideo(videoId, 0f)
                                                isVideoLoaded = true
                                            }
                                            override fun onCurrentSecond(player: YouTubePlayer, second: Float) {
                                                currentTime = second
                                            }
                                            override fun onStateChange(player: YouTubePlayer, state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                                                isPlaying = state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING
                                            }
                                            override fun onError(player: YouTubePlayer, error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError) {
                                                loadError = "Video unavailable. Please check your internet connection."
                                            }
                                        })
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Play button overlay if not playing
                            if (isVideoLoaded && !isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.2f))
                                        .clickable { youtubePlayer?.play() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.size(64.dp),
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.9f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.PlayArrow,
                                                contentDescription = "Play",
                                                tint = BrandBlue,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // 2. Title and Like/Dislike Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = module?.title ?: "Learning Module",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {}) { Icon(Icons.Filled.ThumbUp, null, tint = Color.Gray, modifier = Modifier.size(22.dp)) }
                    IconButton(onClick = {}) { Icon(Icons.Filled.ThumbDown, null, tint = Color.Gray, modifier = Modifier.size(22.dp)) }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // 3. Upcoming Test Card (Dropdown look)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Upcoming Test 1",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color.Gray)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // 4. Interactive Transcript
                Text(
                    "Video Transcript",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(transcript.size) { index ->
                            val line = transcript[index]
                            val isActive = index == activeIndex
                            
                            TranscriptItem(
                                line = line,
                                isActive = isActive,
                                onClick = {
                                    youtubePlayer?.seekTo(line.time)
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}


@Composable
private fun TranscriptItem(line: TranscriptLine, isActive: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isActive) BrandBlueLight.copy(alpha = 0.4f) else Color.Transparent)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BrandBlue)
            )
            Spacer(Modifier.width(12.dp))
        } else {
            Spacer(Modifier.width(16.dp))
        }

        Text(
            text = line.timestampLabel,
            modifier = Modifier.width(48.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) BrandBlue else Color.Gray
            )
        )
        
        Text(
            text = line.text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) Color.Black else Color.DarkGray,
                lineHeight = 20.sp
            )
        )
    }
}

// ════════════════════════════════════════════════════════════════════
//  SIMULATION SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun SimulationScreen(navController: NavController, moduleId: String, progressVm: LessonProgressViewModel = hiltViewModel()) {
    com.azhar.dosescribe.ui.feature.simulation.SimulationMainScreen(
        navController = navController,
        moduleId = moduleId,
        lessonProgress = progressVm
    )
}

// ════════════════════════════════════════════════════════════════════
//  RESULTS SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun ResultsScreen(navController: NavController, moduleId: String, progressVm: LessonProgressViewModel = hiltViewModel()) {
    val questions = getMcqsForModule(moduleId)
    val preAns = progressVm.preAnswers[moduleId] ?: List(questions.size) { -1 }
    val postAns = progressVm.postAnswers[moduleId] ?: List(questions.size) { -1 }
    
    val preCorrect = questions.indices.count { preAns.getOrElse(it) { -1 } == questions[it].correctIndex }
    val postCorrect = questions.indices.count { postAns.getOrElse(it) { -1 } == questions[it].correctIndex }
    
    val preScorePct = if (questions.isNotEmpty()) (preCorrect.toFloat() / questions.size) * 100 else 0f
    val postScorePct = if (questions.isNotEmpty()) (postCorrect.toFloat() / questions.size) * 100 else 0f
    val improvement = postScorePct - preScorePct
    
    // Simulation result (Placeholder logic - assuming if completed it's 100% or based on some stored state)
    // In a real app, SimulationViewModel would save score to progressVm. 
    // For now, let's assume it's passed if step 2 is completed.
    val isSimCompleted = progressVm.isStepCompleted(moduleId, 2)
    val simScorePct = progressVm.simScores[moduleId] ?: 0
    val simScore = (simScorePct.toFloat() / 100f * questions.size).toInt()
    
    var expandedSection by remember { mutableIntStateOf(-1) } // -1 = none, 0 = pre, 1 = post, 2 = sim

    LaunchedEffect(Unit) { progressVm.completeStep(moduleId, 4) }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SimpleTopBar(title = "Final Results", onBackClick = { navController.popBackStack() })
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Overall Marks Summary ──
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ScoreSummaryCard(
                            title = "Questionnaire",
                            score = postCorrect,
                            total = questions.size,
                            color = CorrectGreen,
                            modifier = Modifier.weight(1f)
                        )
                        ScoreSummaryCard(
                            title = "Simulation",
                            score = simScore,
                            total = questions.size,
                            color = QuickSimColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ── Improvement Summary & Graph ──
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Learning Improvement", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(16.dp))
                            
                            ComparisonGraph(preScorePct, postScorePct)
                            
                            Spacer(Modifier.height(20.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (improvement >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                    null,
                                    tint = if (improvement >= 0) CorrectGreen else WrongRed
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (improvement >= 0) 
                                        "You improved by ${improvement.toInt()}%!" 
                                        else "Review needed (${improvement.toInt()}%)",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (improvement >= 0) CorrectGreen else WrongRed
                                )
                            }
                        }
                    }
                }

                // ── Section 1: Questionnaire Results ──
                item {
                    Text(
                        "Assessment Results",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                item {
                    ResultSectionCard(
                        title = "Pre-Questionnaire",
                        score = preCorrect,
                        total = questions.size,
                        color = BrandBlue,
                        icon = Icons.Filled.QuestionAnswer,
                        isExpanded = expandedSection == 0,
                        onClick = { expandedSection = if (expandedSection == 0) -1 else 0 }
                    )
                }
                if (expandedSection == 0) {
                    items(questions.size) { idx -> ResultMcqCard(questions[idx], preAns.getOrElse(idx) { -1 }) }
                }

                item {
                    ResultSectionCard(
                        title = "Post-Questionnaire",
                        score = postCorrect,
                        total = questions.size,
                        color = CorrectGreen,
                        icon = Icons.Filled.FactCheck,
                        isExpanded = expandedSection == 1,
                        onClick = { expandedSection = if (expandedSection == 1) -1 else 1 }
                    )
                }
                if (expandedSection == 1) {
                    items(questions.size) { idx -> ResultMcqCard(questions[idx], postAns.getOrElse(idx) { -1 }) }
                }

                // ── Section 2: Simulation Results ──
                item {
                    Text(
                        "Simulation Performance",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                item {
                    ResultSectionCard(
                        title = "Clinical Simulation",
                        score = simScore,
                        total = questions.size,
                        color = QuickSimColor,
                        icon = Icons.Filled.Science,
                        isExpanded = expandedSection == 2,
                        onClick = { expandedSection = if (expandedSection == 2) -1 else 2 }
                    )
                }
                if (expandedSection == 2) {
                    val detailedScore = progressVm.simResults[moduleId]
                    if (detailedScore != null) {
                        item {
                            SimulationResultContent(
                                score = detailedScore,
                                modifier = Modifier.fillMaxWidth().heightIn(max = 800.dp)
                            )
                        }
                    } else {
                        item {
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = QuickSimColor.copy(0.08f))) {
                                Column(Modifier.padding(16.dp)) {
                                    if (isSimCompleted) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.CheckCircle, null, tint = CorrectGreen, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Simulation Status: PASSED", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = CorrectGreen)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text("Simulation details will appear once you complete the case and return here.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Error, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Simulation Status: NOT COMPLETED", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Back to Module", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ComparisonGraph(prePct: Float, postScorePct: Float) {
    val barColorPre = BrandBlue.copy(alpha = 0.6f)
    val barColorPost = CorrectGreen
    
    Column(modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 24.dp)) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val barWidth = width * 0.35f
                val spacing = width * 0.1f
                
                // Pre bar
                val preHeight = (prePct / 100f) * height
                drawRect(
                    color = barColorPre,
                    topLeft = androidx.compose.ui.geometry.Offset(width / 2 - barWidth - spacing / 2, height - preHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, preHeight)
                )
                
                // Post bar
                val postHeight = (postScorePct / 100f) * height
                drawRect(
                    color = barColorPost,
                    topLeft = androidx.compose.ui.geometry.Offset(width / 2 + spacing / 2, height - postHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, postHeight)
                )
                
                // Baseline
                drawLine(
                    color = Color.LightGray,
                    start = androidx.compose.ui.geometry.Offset(0f, height),
                    end = androidx.compose.ui.geometry.Offset(width, height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Pre-Test", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("${prePct.toInt()}%", fontWeight = FontWeight.Bold, color = BrandBlue)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Post-Test", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("${postScorePct.toInt()}%", fontWeight = FontWeight.Bold, color = CorrectGreen)
            }
        }
    }
}

@Composable
private fun ScoreSummaryCard(title: String, score: Int, total: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.8f))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Text(
                    text = "/$total",
                    style = MaterialTheme.typography.titleMedium,
                    color = color.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ResultSectionCard(title: String, score: Int, total: Int, color: Color, icon: ImageVector, isExpanded: Boolean, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text("Score: $score/$total", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
            }
            Icon(
                if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                null, tint = Color.Gray
            )
        }
    }
}

@Composable
private fun SimDetailRow(label: String, isCorrect: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            null,
            tint = if (isCorrect) CorrectGreen else WrongRed,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = if (isCorrect) CorrectGreen else WrongRed)
    }
}

// ── Score Box ─────────────────────────────────────────────────────
@Composable
private fun ScoreBox(label: String, correct: Int, total: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$correct/$total", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

// ── MCQ Progress Bar ──────────────────────────────────────────────
@Composable
private fun McqProgressBar(answered: Int, total: Int) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Progress: $answered/$total MCQs", style = MaterialTheme.typography.bodySmall, color = if (answered == total) CorrectGreen else Color.Gray)
                Text("Total Points: $total", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(progress = { if (total > 0) answered.toFloat() / total else 0f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = if (answered == total) CorrectGreen else BrandBlue, trackColor = BrandBlueLight)
        }
    }
}

// ── MCQ Card ──────────────────────────────────────────────────────
@Composable
private fun McqCard(num: Int, q: McqQuestion, selected: Int, onSelect: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Q$num. ${q.question}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
                Text("${q.points} pt", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
            }
            Spacer(Modifier.height(10.dp))
            q.options.forEachIndexed { i, opt ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(8.dp)).background(if (selected == i) BrandBlueLight else Color.Transparent).clickable { onSelect(i) }.padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == i, onClick = { onSelect(i) }, colors = RadioButtonDefaults.colors(selectedColor = BrandBlue))
                    Spacer(Modifier.width(4.dp))
                    Text(opt, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                }
            }
        }
    }
}

// ── Result MCQ Card ────────────────────────────────────────────────
@Composable
private fun ResultMcqCard(q: McqQuestion, userAnswer: Int) {
    val isCorrect = userAnswer == q.correctIndex
    Column {
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp), color = if (isCorrect) CorrectGreenBg else WrongRedBg) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel, null, tint = if (isCorrect) CorrectGreen else WrongRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isCorrect) "Correct" else "Wrong", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = if (isCorrect) CorrectGreen else WrongRed)
            }
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text(q.question, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                Spacer(Modifier.height(8.dp))
                q.options.forEachIndexed { i, opt ->
                    val c = when { i == q.correctIndex -> CorrectGreen; i == userAnswer && !isCorrect -> WrongRed; else -> Color.DarkGray }
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = i == q.correctIndex || i == userAnswer, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = c, disabledSelectedColor = c))
                        Spacer(Modifier.width(4.dp))
                        Text(opt, style = MaterialTheme.typography.bodyMedium, color = c)
                    }
                }
                if (!isCorrect) {
                    Spacer(Modifier.height(8.dp))
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = WrongRedBg) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Explanation:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = WrongRed)
                            Spacer(Modifier.height(4.dp))
                            Text(q.explanation, style = MaterialTheme.typography.bodySmall, color = WrongRed.copy(0.85f))
                        }
                    }
                }
            }
        }
    }
}

// ── Simple Top Bar ─────────────────────────────────────────────────
@Composable
private fun SimpleTopBar(title: String, onBackClick: () -> Unit) {
    Surface(tonalElevation = 2.dp, shadowElevation = 2.dp, color = Color.White) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.Filled.ArrowBack, "Back", tint = Color.DarkGray) }
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        }
    }
}

// ── All Lessons Card ───────────────────────────────────────────────
@Composable
private fun AllLessonsCard(module: ModuleInfo, progress: Float, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(module.iconBg.copy(0.12f)), contentAlignment = Alignment.Center) {
                Icon(module.icon, null, tint = module.iconBg, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(module.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(module.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (progress > 0f) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = if (progress >= 1f) CorrectGreen else BrandBlue, trackColor = BrandBlueLight)
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = if (progress >= 1f) CorrectGreen else BrandBlue, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Filled.ChevronRight, null, tint = Color.Gray)
        }
    }
}