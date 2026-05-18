package com.azhar.dosescribe.ui.feature.simulation

import com.azhar.dosescribe.R

// ─────────────────────────────────────────────────────────────────
// Patient sprites (6 supported variants)
// ─────────────────────────────────────────────────────────────────
enum class PatientSprite(val drawableRes: Int, val label: String) {
    OLD_MAN(R.drawable.actor_oldman, "Aged Man"),
    OLD_WOMAN(R.drawable.actor_old_women, "Aged Woman"),
    MAN(R.drawable.actor_men, "Adult Man"),
    WOMAN(R.drawable.actor_female, "Adult Woman"),
    TODDLER(R.drawable.actor_mother_child, "Child / Toddler"),
    DOCTOR(R.drawable.actor_female_doctor, "Doctor")
}

// ─────────────────────────────────────────────────────────────────
// Drug catalogue item (exists in shelf/fridge/safe)
// ─────────────────────────────────────────────────────────────────
enum class DrugStorage { SHELF, FRIDGE, SAFE }

data class CatalogDrug(
    val id: String,
    val name: String,
    val strength: String,
    val storage: DrugStorage,
    val drawableRes: Int? = null
)

// ─────────────────────────────────────────────────────────────────
// Auxiliary label options
// ─────────────────────────────────────────────────────────────────
object AuxiliaryLabels {
    val ALL = listOf(
        "Take with food",
        "Take on empty stomach",
        "Do not crush or chew",
        "Shake well before use",
        "Refrigerate; do not freeze",
        "Avoid alcohol",
        "May cause drowsiness",
        "For external use only",
        "Take at bedtime",
        "Complete the full course",
        "Avoid sun exposure",
        "Do not take with dairy"
    )
}

// ─────────────────────────────────────────────────────────────────
// Per-prescription drug + the expected (correct) answer key
// ─────────────────────────────────────────────────────────────────
data class PrescriptionItem(
    val displayName: String,             // What the prescription says (e.g. "Paracetamol 500 mg tablets")
    val instructions: String,            // What the prescription says about how to take it
    val expectedDrugId: String,          // Catalog id the user must pick
    val expectedQuantity: String,        // e.g. "20 tablets"
    val expectedDose: String,            // e.g. "500 mg"
    val expectedDirection: String,       // e.g. "1 tab PO q6h PRN fever"
    val expectedAuxLabels: List<String>, // e.g. ["Take with food", "Avoid alcohol"]
    val mustHold: Boolean = false,
    val expectedHoldReason: String? = null
)

// ─────────────────────────────────────────────────────────────────
// Prescription "paper" content (header info + items)
// ─────────────────────────────────────────────────────────────────
data class Prescription(
    val doctorName: String,
    val doctorQualification: String,
    val clinic: String,
    val phone: String,
    val date: String,
    val patientName: String,
    val patientAge: String,
    val patientGender: String,
    val patientCity: String,
    val diagnosis: String,
    val items: List<PrescriptionItem>,
    val hasError: Boolean = false,        // when true, show error layout in popup
    val errorNote: String? = null
)

// ─────────────────────────────────────────────────────────────────
// Pre-authored chat Q/A (patient panel)
// ─────────────────────────────────────────────────────────────────
data class ChatQa(val question: String, val answer: String)

// ─────────────────────────────────────────────────────────────────
// A complete authored Case
// ─────────────────────────────────────────────────────────────────
data class SimulationCase(
    val id: String,
    val moduleId: String,                 // ties back to the lesson module
    val title: String,                    // shown top-left ("Simulation S1")
    val patientSprite: PatientSprite,
    val entryStatement: String,           // first speech bubble
    val prescription: Prescription,
    val chatQuestions: List<ChatQa>,
    val availableDrugs: List<CatalogDrug> // restricted catalogue for this case
)

// ─────────────────────────────────────────────────────────────────
// Session-only data the user produces (held in ViewModel)
// ─────────────────────────────────────────────────────────────────
data class CompletedLabel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val drugId: String,
    val drugName: String,
    val quantity: String,
    val dose: String,
    val direction: String,
    val auxLabels: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)

data class HoldEntry(
    val drugId: String,
    val drugName: String,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class CartItem(
    val drugId: String,
    val drugName: String,
    val strength: String,
    val quantity: Int = 1
)

data class CalculatorUsage(
    val tool: String,
    val inputs: Map<String, String>,
    val result: String,
    val at: Long = System.currentTimeMillis()
)

data class ChatExchange(val question: String, val answer: String, val at: Long = System.currentTimeMillis())

data class StoredPatient(
    val id: String,
    val name: String,
    val age: String,
    val gender: String,
    val city: String,
    val notes: String = ""
)

// ─────────────────────────────────────────────────────────────────
// Right-rail buttons
// ─────────────────────────────────────────────────────────────────
enum class RailButton(val label: String) {
    CART("Cart"),
    CHAT("Chat"),
    PRESCRIPTION("Presc."),
    DRUGS("Drugs"),
    LABELS("Labels"),
    NOTES("Notes"),
    REPORTS("Repor.")
}

// ─────────────────────────────────────────────────────────────────
// Per-field result for one prescription item (for results screen)
// ─────────────────────────────────────────────────────────────────
data class FieldResult(val field: String, val expected: String, val actual: String, val correct: Boolean)

data class DrugResult(
    val drugDisplay: String,
    val fields: List<FieldResult>
) {
    val correctCount: Int get() = fields.count { it.correct }
    val total: Int get() = fields.size
}

data class CaseScore(
    val caseId: String,
    val moduleId: String,
    val correct: Int,
    val total: Int,
    val drugResults: List<DrugResult>,
    val timeSeconds: Long,
    val calculatorsUsed: Int,
    val chatQuestionsAsked: Int,
    val notesLength: Int
) {
    val percent: Int get() = if (total == 0) 0 else (correct * 100) / total
    val passed: Boolean get() = percent >= 70
}
