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
data class AuxLabel(val text: String, val iconRes: Int? = null)

object AuxiliaryLabels {
    // Stored as strings on CompletedLabel for backward compatibility with scoring.
    val WITH_ICONS: List<AuxLabel> = listOf(
        AuxLabel("Take with food", R.drawable.take_with_food),
        AuxLabel("Take on empty stomach", R.drawable.take_on_empty_stomach),
        AuxLabel("Do not crush or chew", R.drawable.dont_chew_or_crush),
        AuxLabel("Shake well before use", R.drawable.shake_well_before_use),
        AuxLabel("Refrigerate; do not freeze", R.drawable.keep_in_fridge),
        AuxLabel("May cause drowsiness", R.drawable.may_cause_drowsiness),
        AuxLabel("For external use only", R.drawable.for_external_use_only),
        AuxLabel("Avoid sun exposure", R.drawable.avoid_prolong_exposure_to_sunlight),
        AuxLabel("Do not eat grapefruit", R.drawable.dont_eat_grapefruit),
        AuxLabel("Oral use only", R.drawable.oral_use_only),
        AuxLabel("Not for injection", R.drawable.not_for_inj),
        AuxLabel("High alert medication", R.drawable.high_alert_medications),
        AuxLabel("Contraindicated in pregnancy", R.drawable.contraindicated_in_preg),
        AuxLabel("Dilute before administration", R.drawable.dilute_before_administration),
        AuxLabel("Look alike sound alike (LASA)", R.drawable.look_a_like_lasa),
        AuxLabel("Paralyzing agent", R.drawable.paralyzing_agent),
        AuxLabel("Chemotherapy", R.drawable.chemotherapy),
        AuxLabel("For IV use only", R.drawable.for_iv_use_only),
        AuxLabel("For intrathecal use only", R.drawable.for_intrathecal_use),
        AuxLabel("Avoid alcohol"),
        AuxLabel("Take at bedtime"),
        AuxLabel("Complete the full course"),
        AuxLabel("Do not take with dairy")
    )
    val ALL: List<String> = WITH_ICONS.map { it.text }
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
// "Ask About" — predefined counselling / history question chips.
// Used by the redesigned chat screen.
// ─────────────────────────────────────────────────────────────────
data class AskQuestion(
    val id: String,
    val title: String,            // chip label, e.g. "Allergies"
    val questionText: String,     // pharmacist bubble text
    val patientAnswer: String     // patient bubble text
)

object AskAboutCatalog {
    val DEFAULT: List<AskQuestion> = listOf(
        AskQuestion("age", "Age",
            "What is your age?", "I'd rather not say."),
        AskQuestion("alcohol", "Alcohol Consumption",
            "Do you consume alcohol?", "No, I do not drink alcohol."),
        AskQuestion("allergies", "Allergies",
            "Do you have any allergies?", "I'm not sure."),
        AskQuestion("breast_feeding", "Breast Feeding",
            "Are you breastfeeding?", "No."),
        AskQuestion("previous_disease", "Previous disease",
            "Do you have any previous disease or medical condition?",
            "I have high blood pressure."),
        AskQuestion("previous_med", "Previous use of medication",
            "Have you used this medicine before?", "No, this is my first time."),
        AskQuestion("current_meds", "Current medications",
            "What medications are you currently taking?",
            "I take amlodipine for blood pressure."),
        AskQuestion("pregnancy", "Pregnancy",
            "Are you currently pregnant or planning to be?", "No."),
        AskQuestion("smoking", "Smoking",
            "Do you smoke tobacco?", "No, I don't smoke."),
        AskQuestion("symptoms", "Symptoms",
            "What symptoms are you experiencing right now?",
            "I have a mild headache and feel tired."),
        AskQuestion("medical_history", "Medical history",
            "Could you share your medical history?",
            "Hypertension diagnosed five years ago."),
        AskQuestion("diet", "Diet",
            "Can you describe your typical diet?",
            "Mostly home-cooked meals, low salt.")
    )
}

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
    val duration: String = "",
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
    val notesLength: Int,
    val actionChecklist: List<ChecklistItem> = emptyList()
) {
    val percent: Int get() = if (total == 0) 0 else (correct * 100) / total
    val passed: Boolean get() = percent >= 70
}

// ─────────────────────────────────────────────────────────────────
// Action checklist (Step 5 results screen). Not persisted to Firestore
// (the admin contract is unchanged) — used purely for the UI summary.
// ─────────────────────────────────────────────────────────────────
enum class ChecklistStatus { DONE, MISSED, NOT_NEEDED }

data class ChecklistItem(
    val title: String,
    val userValue: String,
    val expectedValue: String,
    val status: ChecklistStatus,
    val note: String = ""
)
