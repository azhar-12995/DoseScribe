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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.ripple.rememberRipple
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

@Composable
fun SimulationMainScreen(
    navController: NavController,
    moduleId: String,
    lessonProgress: LessonProgressViewModel,
    vm: SimulationViewModel = hiltViewModel()
) {
    LaunchedEffect(moduleId) { vm.loadCaseForModule(moduleId) }
    val case = vm.case ?: return

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

    var showResults by remember { mutableStateOf(false) }
    if (showResults) {
        SimulationResultsScreen(
            score = vm.lastScore!!,
            onFinish = {
                lessonProgress.completeStep(moduleId, 2)
                navController.popBackStack()
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEFEFEF))) {
        PharmacyRoom(vm = vm)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            RightRail(active = vm.activeRail, onSelect = { vm.toggleRail(it) })
        }

        if (!vm.locked) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                SimPrimaryButton(
                    label = "Hand Over",
                    icon = Icons.Filled.PanTool,
                    modifier = Modifier.padding(start = 22.dp, bottom = 22.dp),
                    onClick = { vm.openHandOverConfirm() }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 56.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            AnimatedVisibility(visible = vm.activeRail == RailButton.CART, enter = slideInHorizontally(tween(280)) { it } + fadeIn(), exit = fadeOut()) { CartPanel(vm = vm, onClose = { vm.toggleRail(RailButton.CART) }) }
            AnimatedVisibility(visible = vm.activeRail == RailButton.CHAT, enter = slideInHorizontally(tween(280)) { it } + fadeIn(), exit = fadeOut()) { ChatPanel(vm = vm, onClose = { vm.toggleRail(RailButton.CHAT) }) }
            AnimatedVisibility(visible = vm.activeRail == RailButton.DRUGS, enter = slideInHorizontally(tween(280)) { it } + fadeIn(), exit = fadeOut()) { DrugsPanel(vm = vm, onClose = { vm.toggleRail(RailButton.DRUGS) }) }
            AnimatedVisibility(visible = vm.activeRail == RailButton.LABELS, enter = slideInHorizontally(tween(280)) { it } + fadeIn(), exit = fadeOut()) { LabelsPanel(vm = vm, onClose = { vm.toggleRail(RailButton.LABELS) }) }
            AnimatedVisibility(visible = vm.activeRail == RailButton.NOTES, enter = slideInHorizontally(tween(280)) { it } + fadeIn(), exit = fadeOut()) { NotesPanel(vm = vm, onClose = { vm.toggleRail(RailButton.NOTES) }) }
            AnimatedVisibility(visible = vm.activeRail == RailButton.REPORTS, enter = slideInHorizontally(tween(280)) { it } + fadeIn(), exit = fadeOut()) { ReportsPanel(vm = vm, onClose = { vm.toggleRail(RailButton.REPORTS) }) }
        }

        if (vm.showPrescription) PrescriptionPopup(vm = vm)
        if (vm.showStorage != null) StorageScreen(
            storage = vm.showStorage!!,
            catalog = case.availableDrugs,
            onAdd = { drug, qty ->
                vm.addDrugToCart(drug, qty)
                vm.closeStorage()
                vm.openLabelingFor(drug.id)
            },
            onClose = { vm.closeStorage() }
        )
        if (vm.showCalculator) CalculatorMenuDialog(vm) { vm.closeCalculator() }
        if (vm.showPatientFiles) PatientFilesDialog(vm) { vm.closePatientFiles() }
        if (vm.showClinicalReference) ClinicalReferenceDialog { vm.closeClinicalReference() }
        if (vm.showLabeling) LabelingScreen(vm = vm, onClose = { vm.closeLabeling() })
        if (vm.showHoldForm) HoldFormDialog(vm = vm, onClose = { vm.closeHoldForm() })

        if (vm.showHandOverConfirm) {
            AlertDialog(
                onDismissRequest = { vm.closeHandOverConfirm() },
                title = { Text("Hand Over to Patient?") },
                text = { Text("This locks all inputs, scores your session, and submits the result.") },
                confirmButton = {
                    Button(onClick = { vm.closeHandOverConfirm(); vm.handOverAndScore(); showResults = true }, colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue)) { Text("Hand Over") }
                },
                dismissButton = { TextButton(onClick = { vm.closeHandOverConfirm() }) { Text("Cancel") } }
            )
        }
    }
}

@Composable
private fun PharmacyRoom(vm: SimulationViewModel) {
    val case = vm.case ?: return
    var caseStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(450); caseStarted = true }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.72f).background(Brush.verticalGradient(listOf(Color(0xFFF4F6F8), Color(0xFFE2E6EA)))))
        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.28f).background(Brush.verticalGradient(listOf(Color(0xFFE0DFDA), Color(0xFFCFCDC6)))))

        Row(modifier = Modifier.align(Alignment.TopStart).padding(start = 4.dp, top = 6.dp, end = 6.dp).fillMaxWidth(0.94f).fillMaxHeight(0.66f)) {
            Row(modifier = Modifier.weight(0.66f).fillMaxHeight().clickable { vm.openStorage(DrugStorage.SHELF) }) {
                repeat(3) { Image(painter = painterResource(R.drawable.shelf), contentDescription = null, modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.FillBounds) }
            }
            Spacer(Modifier.width(6.dp))
            Box(modifier = Modifier.weight(0.13f).fillMaxHeight().clickable { vm.openStorage(DrugStorage.FRIDGE) }, contentAlignment = Alignment.BottomCenter) {
                Image(painter = painterResource(R.drawable.fridge_1), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.width(4.dp))
            Box(modifier = Modifier.weight(0.13f).fillMaxHeight().clickable { vm.openStorage(DrugStorage.SAFE) }, contentAlignment = Alignment.BottomCenter) {
                Image(painter = painterResource(R.drawable.locker), contentDescription = null, modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f), contentScale = ContentScale.Fit)
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.32f).background(Brush.verticalGradient(listOf(Color(0xFFE3DFD7), Color(0xFFC8C2B6)))))

        Image(
            painter = painterResource(case.patientSprite.drawableRes),
            contentDescription = null,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 80.dp, bottom = 80.dp).fillMaxHeight(0.74f).width(210.dp).zIndex(1f).clickable { vm.showSpeechBubble() },
            contentScale = ContentScale.Fit
        )

        LaunchedEffect(vm.speechBubbleShownAt, caseStarted) { if (caseStarted && vm.speechBubbleVisible) { delay(4500); vm.hideSpeechBubble() } }
        AnimatedVisibility(visible = caseStarted && vm.speechBubbleVisible, enter = fadeIn(tween(350)) + scaleIn(spring(Spring.DampingRatioMediumBouncy), initialScale = 0.7f), exit = fadeOut(tween(220)), modifier = Modifier.align(Alignment.TopStart).padding(start = 280.dp, top = 28.dp).fillMaxWidth(0.40f).zIndex(5f)) { SpeechBubble(case.entryStatement) }

        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.32f).zIndex(2f)) {

            // 1. Hand Over Button (Front-Left)
            SimPrimaryButton(
                label = "Hand Over",
                icon = Icons.Filled.PanTool,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 20.dp).zIndex(10f),
                onClick = { vm.openHandOverConfirm() }
            )

            // 2. Basket (Behind Hand Over)
            DeskItem(
                resId = R.drawable.basket,
                label = "Basket",
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 45.dp).size(130.dp, 95.dp),
                onClick = { vm.toggleRail(RailButton.CART) }
            )

            // 3. Prescription (Between Hands - Center Left)
            DeskItem(
                resId = R.drawable.prescription,
                label = "Prescription",
                modifier = Modifier.align(Alignment.BottomCenter).offset(x = (-130).dp, y = (-20).dp).size(110.dp, 140.dp),
                onClick = { vm.openPrescription() }
            )

            // 4. Calculator (Centered)
            DeskItem(
                resId = R.drawable.calculator,
                label = "Calculator",
                modifier = Modifier.align(Alignment.BottomCenter).offset(x = 10.dp, y = (-25).dp).size(85.dp, 85.dp),
                onClick = { vm.openCalculator() }
            )

            // 5. Telephone (Right of Calculator)
            DeskItem(
                resId = R.drawable.telephone,
                label = "Telephone",
                modifier = Modifier.align(Alignment.BottomCenter).offset(x = 110.dp, y = (-30).dp).size(95.dp, 75.dp),
                onClick = { vm.openTelephone() }
            )

            // 6. Reports (Center Right)
            DeskItem(
                resId = R.drawable.lab_reports,
                label = "Reports",
                modifier = Modifier.align(Alignment.BottomCenter).offset(x = 220.dp, y = (-15).dp).size(110.dp, 130.dp),
                onClick = { vm.openReports() }
            )

            // 7. Reference (Right Side)
            DeskItem(
                resId = R.drawable.books,
                label = "Reference",
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 280.dp, bottom = 20.dp).size(130.dp, 100.dp),
                onClick = { vm.openClinicalReference() }
            )

            // 8. Computer (Far Right)
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 50.dp, bottom = 25.dp).fillMaxWidth(0.20f).fillMaxHeight(0.95f).clickable { vm.openPatientFiles() }
            ) {
                DesktopComputerModern(showTime = caseStarted)
            }
        }
    }
}

@Composable
private fun DeskItem(
    resId: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false, radius = 48.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = label,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun DesktopComputerModern(showTime: Boolean) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.82f),
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, Color(0xFF333333))
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(4.dp).background(Color.Black, RoundedCornerShape(6.dp))) {
                if (showTime) {
                    Text("10:30 AM", color = Color(0xFF00FF00), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
                }
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(R.drawable.logo_on_computer), contentDescription = null, modifier = Modifier.size(60.dp), contentScale = ContentScale.Fit)
                    Text("DOSESCRIBE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
                Surface(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp), color = Color(0xFF333333), shape = CircleShape) {
                    Box(Modifier.size(6.dp))
                }
            }
        }
        Box(modifier = Modifier.width(35.dp).height(12.dp).background(Color(0xFF222222)))
        Surface(modifier = Modifier.width(80.dp).height(5.dp), color = Color(0xFF1A1A1A), shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)) {}
        Spacer(Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.width(90.dp).height(12.dp), color = Color(0xFFE0E0E0), shape = RoundedCornerShape(2.dp), shadowElevation = 2.dp) {
                Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    repeat(10) { Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFBDBDBD))) }
                }
            }
            Spacer(Modifier.width(10.dp))
            Surface(modifier = Modifier.size(14.dp), color = Color(0xFFE0E0E0), shape = CircleShape, shadowElevation = 2.dp) {}
        }
    }
}
