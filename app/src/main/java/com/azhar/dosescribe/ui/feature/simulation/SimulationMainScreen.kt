package com.azhar.dosescribe.ui.feature.simulation

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.azhar.dosescribe.R
import com.azhar.dosescribe.ui.feature.lessons.LessonProgressViewModel
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────
// Public entry point — call this from the lesson nav step 3.
// Layout strategy:
//   Scene takes the FULL screen (fillMaxSize). The slim peek-out
//   right-rail floats over the scene at the right edge so the
//   pharmacy art keeps ~92-94% effective width.
// ─────────────────────────────────────────────────────────────────
@Composable
fun SimulationMainScreen(
    navController: NavController,
    moduleId: String,
    lessonProgress: LessonProgressViewModel,
    vm: SimulationViewModel = hiltViewModel()
) {
    LaunchedEffect(moduleId) { vm.loadCaseForModule(moduleId) }
    val case = vm.case ?: return

    // Force landscape + immersive
    val context = LocalContext.current
    val view = LocalView.current
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val orig = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val window = activity?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, view)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            activity?.requestedOrientation = orig ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                WindowInsetsControllerCompat(window, view)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEFEFEF))) {
        // Full-bleed pharmacy scene
        PharmacyRoom(vm = vm)

        // Slim peek-out right rail (overlay, anchored right)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            RightRail(active = vm.activeRail, onSelect = { vm.toggleRail(it) })
        }

        // Hand-Over primary button (bottom-left) — replaces basket + Exit
        // Basket image sits BEHIND the button as a separate decorative element.
        if (!vm.locked) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 14.dp, bottom = 14.dp)
                        .size(width = 150.dp, height = 110.dp)
                ) {
                    // Decorative basket (NOT clickable) — drawn first so it sits behind
                    Image(
                        painter = painterResource(R.drawable.basket),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-6).dp, y = (-2).dp)
                            .size(width = 120.dp, height = 90.dp)
                            .zIndex(0f),
                        contentScale = ContentScale.Fit
                    )
                    // Hand-Over button drawn ON TOP — only this is clickable
                    SimPrimaryButton(
                        label = "Hand Over",
                        icon = Icons.Filled.PanTool,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .zIndex(2f),
                        onClick = { vm.openHandOverConfirm() }
                    )
                }
            }
        }

        // Slide-in side panels — anchored to the right edge, sitting just left of the rail
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 56.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            AnimatedVisibility(
                visible = vm.activeRail == RailButton.CART,
                enter = slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(),
                exit = fadeOut()
            ) { CartPanel(vm = vm, onClose = { vm.toggleRail(RailButton.CART) }) }

            AnimatedVisibility(
                visible = vm.activeRail == RailButton.CHAT,
                enter = slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(),
                exit = fadeOut()
            ) { ChatPanel(vm = vm, onClose = { vm.toggleRail(RailButton.CHAT) }) }

            AnimatedVisibility(
                visible = vm.activeRail == RailButton.DRUGS,
                enter = slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(),
                exit = fadeOut()
            ) { DrugsPanel(vm = vm, onClose = { vm.toggleRail(RailButton.DRUGS) }) }

            AnimatedVisibility(
                visible = vm.activeRail == RailButton.LABELS,
                enter = slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(),
                exit = fadeOut()
            ) { LabelsPanel(vm = vm, onClose = { vm.toggleRail(RailButton.LABELS) }) }

            AnimatedVisibility(
                visible = vm.activeRail == RailButton.NOTES,
                enter = slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(),
                exit = fadeOut()
            ) { NotesPanel(vm = vm, onClose = { vm.toggleRail(RailButton.NOTES) }) }

            AnimatedVisibility(
                visible = vm.activeRail == RailButton.REPORTS,
                enter = slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(),
                exit = fadeOut()
            ) { ReportsPanel(vm = vm, onClose = { vm.toggleRail(RailButton.REPORTS) }) }
        }

        // ── Patient-selected confirmation toast (auto-dismiss after 2.2s) ──
        val justSelectedAt = vm.patientJustSelectedAt
        var toastVisible by remember(justSelectedAt) { mutableStateOf(justSelectedAt > 0L) }
        LaunchedEffect(justSelectedAt) {
            if (justSelectedAt > 0L) {
                toastVisible = true
                delay(2200)
                toastVisible = false
            }
        }
        AnimatedVisibility(
            visible = toastVisible && vm.selectedPatient != null,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize().padding(top = 18.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Surface(
                    color = SimSuccess,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Patient selected: ${vm.selectedPatient?.name ?: ""}",
                            color = Color.White, fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Modals
        if (vm.showPrescription) PrescriptionPopup(vm = vm)
        if (vm.showStorage != null) StorageScreen(
            storage = vm.showStorage!!,
            catalog = case.availableDrugs,
            onAdd = { drug, qty ->
                repeat(qty) { vm.addDrugToCart(drug) }
                vm.closeStorage()
                // NOTE: do NOT auto-open labeling — labeling is a separate step.
            },
            onClose = { vm.closeStorage() }
        )
        if (vm.showCalculator) CalculatorMenuDialog(vm) { vm.closeCalculator() }
        if (vm.showPatientFiles) PatientFilesDialog(vm) { vm.closePatientFiles() }
        if (vm.showClinicalReference) ClinicalReferenceDialog { vm.closeClinicalReference() }
        if (vm.showLabeling) LabelingScreen(vm = vm, onClose = { vm.closeLabeling() })
        if (vm.showHoldForm) HoldFormDialog(vm = vm, onClose = { vm.closeHoldForm() })

        // Hand-Over confirm dialog
        if (vm.showHandOverConfirm) {
            AlertDialog(
                onDismissRequest = { vm.closeHandOverConfirm() },
                title = { Text("Hand Over to Patient?") },
                text = {
                    Text(
                        "This locks all inputs, scores your session, and submits the result. " +
                                "Make sure all labels and holds are complete."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.closeHandOverConfirm()
                            val score = vm.handOverAndScore()
                            // Save simulation detailed score and complete step
                            lessonProgress.saveSimScore(moduleId, score)
                            lessonProgress.completeStep(moduleId, 2)
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue)
                    ) { Text("Hand Over") }
                },
                dismissButton = {
                    TextButton(onClick = { vm.closeHandOverConfirm() }) { Text("Cancel") }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Pharmacy room — full-bleed scene with clickable hotspots.
// All sizes are %-based so the scene scales on any landscape phone.
// ─────────────────────────────────────────────────────────────────
@Composable
private fun PharmacyRoom(vm: SimulationViewModel) {
    val case = vm.case ?: return
    var caseStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(450); caseStarted = true }

    Box(modifier = Modifier.fillMaxSize()) {
        // Walls (back + floor)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .background(Brush.verticalGradient(listOf(Color(0xFFF4F6F8), Color(0xFFE2E6EA))))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.28f)
                .background(Brush.verticalGradient(listOf(Color(0xFFE0DFDA), Color(0xFFCFCDC6))))
        )

        // Continuous shelf system + fridge + safe across the back wall
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 6.dp, end = 6.dp)
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.66f)
        ) {
            // Continuous shelves — tile drawable horizontally so it
            // reads as a busy back wall, not one isolated cabinet.
            Row(
                modifier = Modifier
                    .weight(0.66f)
                    .fillMaxHeight()
                    .clickable { vm.openStorage(DrugStorage.SHELF) }
            ) {
                repeat(3) {
                    Image(
                        painter = painterResource(R.drawable.shelf),
                        contentDescription = if (it == 0) "Shelves" else null,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            // Fridge — full-height
            Box(
                modifier = Modifier
                    .weight(0.13f)
                    .fillMaxHeight()
                    .clickable { vm.openStorage(DrugStorage.FRIDGE) },
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(R.drawable.fridge_1),
                    contentDescription = "Medication fridge",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.width(4.dp))
            // Safe locker — controlled-substance safe
            Box(
                modifier = Modifier
                    .weight(0.13f)
                    .fillMaxHeight()
                    .clickable { vm.openStorage(DrugStorage.SAFE) },
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(R.drawable.locker),
                    contentDescription = "Controlled substance safe",
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Wall art (decorative, far top-right corner)
        Image(
            painter = painterResource(R.drawable.bg_on_wall_1),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 70.dp, top = 14.dp)
                .size(width = 42.dp, height = 56.dp)
        )

        // Counter top (in front of patient)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.30f)
                .background(Brush.verticalGradient(listOf(Color(0xFFE3DFD7), Color(0xFFC8C2B6))))
        )

        // Patient sprite (centered behind counter) — anchored slightly left.
        // Tappable: tapping the patient re-shows the speech bubble.
        Image(
            painter = painterResource(case.patientSprite.drawableRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 80.dp, bottom = 70.dp)
                .fillMaxHeight(0.74f)
                .width(210.dp)
                .zIndex(1f)
                .clickable { vm.showSpeechBubble() },
            contentScale = ContentScale.Fit
        )

        // Speech bubble — anchored to the TOP-LEFT, sitting just to the
        // RIGHT of the patient's head. Anchored from TopStart (so it
        // grows downward from the top) — this keeps it cleanly above the
        // counter and avoids overlapping the busy shelf-item drawables
        // (the bubble's white opaque background occludes the shelf
        // texture behind it). Auto-hides a few seconds after appearing;
        // re-appears when the patient sprite is tapped.
        LaunchedEffect(vm.speechBubbleShownAt, caseStarted) {
            if (caseStarted && vm.speechBubbleVisible) {
                delay(4500)
                vm.hideSpeechBubble()
            }
        }
        AnimatedVisibility(
            visible = caseStarted && vm.speechBubbleVisible,
            enter = fadeIn(tween(350)) +
                    scaleIn(animationSpec = spring(Spring.DampingRatioMediumBouncy), initialScale = 0.7f),
            exit = fadeOut(tween(220)),
            modifier = Modifier
                .align(Alignment.TopStart)
                // Sprite head is roughly at start = 80 + width=210 = 290.dp
                // Anchor bubble just to the RIGHT of the head, near the top.
                .padding(start = 280.dp, top = 28.dp)
                .fillMaxWidth(0.40f)
                .zIndex(5f)
        ) { SpeechBubble(case.entryStatement) }

        // ─── Counter props row ──────────────────────────────────────
        // All 4 props live in a single Row with EQUAL WEIGHTED zones,
        // so they CANNOT overlap and each gets its own click target.
        // Left padding (180.dp) keeps a safe gap from the Hand-Over
        // button zone; right padding (230.dp) keeps clear of the desktop
        // computer. Each prop fills its weighted slot — overlap is
        // geometrically impossible.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 180.dp, end = 230.dp, bottom = 26.dp)
                .height(86.dp)
                .zIndex(2f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            CounterProp(R.drawable.prescription, "Prescription",
                modifier = Modifier.weight(1f).fillMaxHeight()) { vm.openPrescription() }
            CounterProp(R.drawable.calculator, "Calculator",
                modifier = Modifier.weight(1f).fillMaxHeight()) { vm.openCalculator() }
            CounterProp(R.drawable.lab_reports, "Reports",
                modifier = Modifier.weight(1f).fillMaxHeight()) { vm.openReports() }
            CounterProp(R.drawable.books, "Books",
                modifier = Modifier.weight(1f).fillMaxHeight()) { vm.openClinicalReference() }
        }

        // Desktop computer (right side of counter)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 110.dp, bottom = 22.dp)
                .fillMaxWidth(0.20f)
                .fillMaxHeight(0.46f)
                .zIndex(2f)
                .clickable { vm.openPatientFiles() }
        ) {
            DesktopComputer(showTime = caseStarted)
        }
    }
}

@Composable
private fun CounterProp(
    drawableRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Each prop owns its full weighted slot (modifier from caller).
    // The whole slot is clickable so the hit target equals the visible area.
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Desktop computer — proper monitor on stand + keyboard + mouse.
// Shows a small "10:30 AM" clock on the screen and the brand logo.
// ─────────────────────────────────────────────────────────────────
@Composable
private fun DesktopComputer(showTime: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Monitor body (narrow bezels)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.74f)
                .shadow(8.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF2A2A2A), Color(0xFF111111))))
                .padding(4.dp)
        ) {
            // Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF1B1B1B))))
            ) {
                if (showTime) {
                    Text(
                        text = "10:30 AM",
                        color = Color(0xFFE8E8E8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 8.dp, bottom = 6.dp)
                    )
                }
                Image(
                    painter = painterResource(R.drawable.logo_on_computer),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.7f)
                        .fillMaxHeight(0.55f),
                    contentScale = ContentScale.Fit
                )
            }
        }
        // Stand neck
        Box(modifier = Modifier.width(20.dp).height(8.dp).background(Color(0xFF2A2A2A)))
        // Stand base
        Box(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF1F1F1F))
        )
        Spacer(Modifier.height(4.dp))
        // Keyboard + mouse
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Keyboard
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(14.dp)
                    .shadow(2.dp, RoundedCornerShape(3.dp))
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFEFEFEF))
                    .border(0.5.dp, Color(0xFFCCCCCC), RoundedCornerShape(3.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                ) {
                    repeat(14) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFFD9D9D9), RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
            Spacer(Modifier.width(6.dp))
            // Mouse
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 14.dp)
                    .shadow(2.dp, RoundedCornerShape(percent = 50))
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFFEFEFEF))
                    .border(0.5.dp, Color(0xFFCCCCCC), RoundedCornerShape(percent = 50))
            )
        }
    }
}

