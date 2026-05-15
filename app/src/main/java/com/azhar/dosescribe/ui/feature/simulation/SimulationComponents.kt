package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Brand palette
val SimDeepBlue = Color(0xFF0982BA)
val SimDeepBlueLight = Color(0xFF1592CE)
val SimSurface = Color(0xFFF7F9FC)
val SimWhite = Color(0xFFFFFFFF)
val SimSuccess = Color(0xFF2E7D32)
val SimDanger = Color(0xFFC62828)
val SimMuted = Color(0xFF666666)

// Tail-shape used by the patient speech bubble
fun triangleLeftShape(): Shape = GenericShape { size, _ ->
    moveTo(0f, size.height / 2f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    close()
}

// ─────────────────────────────────────────────────────────────────
// Right-side option rail — SLIM PEEK-OUT tabs.
// Each tab sticks to the right edge with most of its body hidden;
// only an icon + tiny label peek out. On tap, it slides left into
// an expanded blue state. Only one tab can be active at a time.
// ─────────────────────────────────────────────────────────────────
@Composable
fun RightRail(
    active: RailButton?,
    onSelect: (RailButton) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    // Default peek width ~6-8% of screen, expanded ~14%
    val peekW = (configuration.screenWidthDp * 0.07f).dp.coerceIn(54.dp, 78.dp)
    val expandedW = (configuration.screenWidthDp * 0.13f).dp.coerceIn(96.dp, 132.dp)

    Column(
        modifier = modifier
            .width(expandedW)            // reserve max width so expanded tabs don't clip
            .fillMaxHeight()
            .padding(top = 10.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Top group: Cart / Chat
        RailTab("Cart", Icons.Filled.ShoppingCart, active == RailButton.CART, peekW, expandedW) { onSelect(RailButton.CART) }
        RailTab("Chat", Icons.Filled.QuestionAnswer, active == RailButton.CHAT, peekW, expandedW) { onSelect(RailButton.CHAT) }

        Spacer(Modifier.height(10.dp))

        // Bottom group: Prescription / Drugs / Labels / Notes / Reports
        RailTab("Presc.", Icons.Filled.Receipt, active == RailButton.PRESCRIPTION, peekW, expandedW) { onSelect(RailButton.PRESCRIPTION) }
        RailTab("Drugs", Icons.Filled.Medication, active == RailButton.DRUGS, peekW, expandedW) { onSelect(RailButton.DRUGS) }
        RailTab("Labels", Icons.Filled.LocalOffer, active == RailButton.LABELS, peekW, expandedW) { onSelect(RailButton.LABELS) }
        RailTab("Notes", Icons.Filled.NoteAlt, active == RailButton.NOTES, peekW, expandedW) { onSelect(RailButton.NOTES) }
        RailTab("Repor.", Icons.Filled.Analytics, active == RailButton.REPORTS, peekW, expandedW) { onSelect(RailButton.REPORTS) }
    }
}

@Composable
private fun RailTab(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    peekW: androidx.compose.ui.unit.Dp,
    expandedW: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val targetW by animateDpAsState(
        targetValue = if (isActive) expandedW else peekW,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "rail-tab-w"
    )
    val elevation by animateDpAsState(
        targetValue = if (isActive) 8.dp else 3.dp,
        animationSpec = spring(),
        label = "rail-tab-elev"
    )
    val bg = if (isActive)
        Brush.verticalGradient(listOf(SimDeepBlueLight, SimDeepBlue))
    else
        Brush.verticalGradient(listOf(SimWhite, Color(0xFFF7F7F7)))
    val fg = if (isActive) SimWhite else Color(0xFF2A2A2A)

    // Tab shape: rounded only on the LEFT side (right is flush with screen edge)
    val tabShape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp, topEnd = 0.dp, bottomEnd = 0.dp)

    Box(
        modifier = Modifier
            .width(targetW)
            .height(36.dp)
            .shadow(elevation, tabShape, clip = false)
            .clip(tabShape)
            .background(bg)
            .border(
                width = if (isActive) 0.dp else 0.5.dp,
                color = Color(0xFFE2E2E2),
                shape = tabShape
            )
            .clickable { onClick() }
            .padding(start = 10.dp, end = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                color = fg,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Speech bubble with a left-pointing tail toward the patient
// ─────────────────────────────────────────────────────────────────
@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 22.dp)
                .size(width = 10.dp, height = 14.dp)
                .clip(triangleLeftShape())
                .background(SimWhite)
        )
        Box(
            modifier = Modifier
                .offset(x = (-1).dp)
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(SimWhite)
        ) {
            Text(
                text = text,
                color = Color(0xFF222222),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Primary blue 3D pill button
// ─────────────────────────────────────────────────────────────────
@Composable
fun SimPrimaryButton(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(SimDeepBlueLight, SimDeepBlue)))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = SimWhite, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(label, color = SimWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Slide-in panel surface (used for cart/chat/drugs/labels/notes/reports)
// ─────────────────────────────────────────────────────────────────
@Composable
fun SlideInPanel(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    width: Float = 0.32f,
    content: @Composable ColumnScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val w = (configuration.screenWidthDp * width).dp.coerceAtLeast(260.dp)
    Surface(
        modifier = modifier
            .width(w)
            .fillMaxHeight()
            .shadow(10.dp),
        color = SimWhite,
        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF222222))
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, "close", tint = SimMuted)
                }
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Drawable helper
// ─────────────────────────────────────────────────────────────────
@Composable
fun DrawableImage(resId: Int, modifier: Modifier = Modifier) {
    Image(painter = painterResource(resId), contentDescription = null, modifier = modifier)
}

