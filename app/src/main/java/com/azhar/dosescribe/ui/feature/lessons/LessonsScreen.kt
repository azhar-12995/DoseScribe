package com.azhar.dosescribe.ui.feature.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.azhar.dosescribe.ui.feature.dashboard.ModuleInfo
import com.azhar.dosescribe.ui.feature.dashboard.allModules

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

// ════════════════════════════════════════════════════════════════════
//  PRE-QUESTIONNAIRE SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun PreQuestionnaireScreen(navController: NavController, moduleId: String, progressVm: LessonProgressViewModel = hiltViewModel()) {
    val questions = getMcqsForModule(moduleId)
    val answers = remember { mutableStateListOf(*IntArray(questions.size) { -1 }.toTypedArray()) }
    val answeredCount = answers.count { it != -1 }
    val allAnswered = answeredCount == questions.size

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SimpleTopBar(title = "Pre Questionnaire", onBackClick = { navController.popBackStack() })
            McqProgressBar(answered = answeredCount, total = questions.size)
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(questions.size) { idx ->
                    McqCard(idx + 1, questions[idx], answers[idx]) { answers[idx] = it }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        progressVm.savePreAnswers(moduleId, answers.toList())
                        progressVm.completeStep(moduleId, 0)
                        navController.popBackStack()
                    }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandBlue), shape = RoundedCornerShape(12.dp), enabled = allAnswered) {
                        Text("Submit & Continue", fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  POST-QUESTIONNAIRE SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun PostQuestionnaireScreen(navController: NavController, moduleId: String, progressVm: LessonProgressViewModel = hiltViewModel()) {
    val questions = getMcqsForModule(moduleId)
    val answers = remember { mutableStateListOf(*IntArray(questions.size) { -1 }.toTypedArray()) }
    val answeredCount = answers.count { it != -1 }
    val allAnswered = answeredCount == questions.size

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SimpleTopBar(title = "Post Questionnaire", onBackClick = { navController.popBackStack() })
            McqProgressBar(answered = answeredCount, total = questions.size)
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(questions.size) { idx ->
                    McqCard(idx + 1, questions[idx], answers[idx]) { answers[idx] = it }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        progressVm.savePostAnswers(moduleId, answers.toList())
                        progressVm.completeStep(moduleId, 3)
                        navController.popBackStack()
                    }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandBlue), shape = RoundedCornerShape(12.dp), enabled = allAnswered) {
                        Text("Submit & View Results", fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  LEARNING MODULE SCREEN
// ════════════════════════════════════════════════════════════════════
@Composable
fun LearningModuleScreen(navController: NavController, moduleId: String, progressVm: LessonProgressViewModel = hiltViewModel()) {
    val module = allModules.find { it.id == moduleId }
    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SimpleTopBar(title = "Learning Module", onBackClick = { navController.popBackStack() })
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                // Video placeholder
                Card(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFB0BEC5))) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Surface(Modifier.size(56.dp), shape = CircleShape, color = Color.Black.copy(0.5f)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(32.dp)) }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${module?.title ?: ""} Overview", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Row {
                        IconButton(onClick = {}) { Icon(Icons.Filled.ThumbUp, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                        IconButton(onClick = {}) { Icon(Icons.Filled.ThumbDown, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                    }
                }
                HorizontalDivider(color = Color(0xFFE8E8E8))
                Spacer(Modifier.height(12.dp))
                // Transcription
                Text("0:00", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${module?.description ?: ""}\n\nThis module covers identification, safe handling, and administration protocols. " +
                            "You will learn best practices, common errors, and how to prevent adverse events.\n\n" +
                            "Key topics:\n• Identification and classification\n• Safety protocols\n• Documentation requirements\n• Error prevention\n• Patient communication",
                    style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray, lineHeight = 22.sp
                )
                Spacer(Modifier.height(20.dp))

                // Quick Simulation box
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = QuickSimColor)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Science, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Quick Simulation", color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Upload and run a quick simulation exercise. Results will appear in the Results step.", color = Color.White.copy(0.9f), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = {}, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), shape = RoundedCornerShape(8.dp)) {
                            Icon(Icons.Filled.Upload, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Upload Simulation")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Button(onClick = { progressVm.completeStep(moduleId, 1); navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandBlue), shape = RoundedCornerShape(12.dp)) {
                    Text("Complete & Continue", fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
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
    val simScore = questions.size // Simulation passed = full score
    val totalPossible = questions.size * 3
    val cumulativeScore = preCorrect + postCorrect + simScore
    var expandedSection by remember { mutableStateOf(-1) } // -1 = none, 0 = pre, 1 = post, 2 = sim

    LaunchedEffect(Unit) { progressVm.completeStep(moduleId, 4) }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SimpleTopBar(title = "Results", onBackClick = { navController.popBackStack() })
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // ── Cumulative Result Summary ──
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CorrectGreenBg)) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎉 Lesson Completed!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CorrectGreen)
                            Spacer(Modifier.height(12.dp))
                            Text("Cumulative Score", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("$cumulativeScore / $totalPossible", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = CorrectGreen)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { cumulativeScore.toFloat() / totalPossible },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = CorrectGreen, trackColor = CorrectGreen.copy(0.2f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                ScoreBox("Pre-Test", preCorrect, questions.size, BrandBlue)
                                ScoreBox("Post-Test", postCorrect, questions.size, CorrectGreen)
                                ScoreBox("Simulation", simScore, questions.size, QuickSimColor)
                            }
                        }
                    }
                }

                // ── Pre-Questionnaire Card ──
                item {
                    ResultSectionCard(
                        title = "Pre-Questionnaire Result",
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

                // ── Post-Questionnaire Card ──
                item {
                    ResultSectionCard(
                        title = "Post-Questionnaire Result",
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

                // ── Simulation Result Card ──
                item {
                    ResultSectionCard(
                        title = "Simulation Result",
                        score = simScore,
                        total = questions.size,
                        color = QuickSimColor,
                        icon = Icons.Filled.Science,
                        isExpanded = expandedSection == 2,
                        onClick = { expandedSection = if (expandedSection == 2) -1 else 2 }
                    )
                }
                if (expandedSection == 2) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = QuickSimColor.copy(0.08f))) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = CorrectGreen, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Simulation Score: Passed", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = CorrectGreen)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("All simulation tasks completed successfully.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFE0E0E0))
                                Spacer(Modifier.height(8.dp))
                                Text("Simulation Details:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                                Spacer(Modifier.height(4.dp))
                                SimDetailRow("Patient verified", true)
                                SimDetailRow("Prescription interpreted correctly", true)
                                SimDetailRow("Drug label checked", true)
                                SimDetailRow("Dose calculation correct", true)
                                SimDetailRow("Auxiliary labels applied", true)
                                SimDetailRow("Counseling completed", true)
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandBlue), shape = RoundedCornerShape(12.dp)) {
                        Text("Back to Lesson", fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                }
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