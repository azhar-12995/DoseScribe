package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    // ── Active case + session bookkeeping ─────────────────────────
    var case: SimulationCase? = null
        private set
    var sessionStartedAt: Long = 0L
        private set
    var lastActionAt: Long by mutableStateOf(0L)
        private set
    var locked: Boolean by mutableStateOf(false)
        private set

    // ── Right-rail state ──────────────────────────────────────────
    var activeRail: RailButton? by mutableStateOf(null)
        private set

    // ── Modals ────────────────────────────────────────────────────
    var showPrescription: Boolean by mutableStateOf(false)
        private set
    var showStorage: DrugStorage? by mutableStateOf(null)
        private set
    var showCalculator: Boolean by mutableStateOf(false)
        private set
    var showPatientFiles: Boolean by mutableStateOf(false)
        private set
    var showClinicalReference: Boolean by mutableStateOf(false)
        private set
    var showLabeling: Boolean by mutableStateOf(false)
        private set
    var showHoldForm: Boolean by mutableStateOf(false)
        private set
    var showHandOverConfirm: Boolean by mutableStateOf(false)
        private set

    /** When labeling is opened from a specific drug (Cart/Drugs/Storage), this pre-fills the form. */
    var labelingPrefillDrugId: String? by mutableStateOf(null)
        private set
    /** When editing an existing label, this id is set so the labeling form loads its values. */
    var editingLabelId: String? by mutableStateOf(null)
        private set

    // ── Session state ─────────────────────────────────────────────
    val cart = mutableStateListOf<CartItem>()
    val selectedDrugs = mutableStateListOf<CatalogDrug>()
    val completedLabels = mutableStateListOf<CompletedLabel>()
    val holds = mutableStateListOf<HoldEntry>()
    val chatLog = mutableStateListOf<ChatExchange>()
    val calculatorLog = mutableStateListOf<CalculatorUsage>()
    val storedPatients = mutableStateListOf<StoredPatient>()

    var notes: String by mutableStateOf("")
        private set

    // Patient selected from PC / patient files
    var selectedPatient: StoredPatient? by mutableStateOf(null)
        private set
    var patientJustSelectedAt: Long by mutableStateOf(0L)
        private set

    // ── Speech-bubble visibility ──────────────────────────────────
    // Shown on case start, auto-hides after a few seconds, hidden when
    // the user opens any modal/rail, re-shown when the patient sprite
    // is tapped.
    var speechBubbleVisible: Boolean by mutableStateOf(true)
        private set
    var speechBubbleShownAt: Long by mutableStateOf(System.currentTimeMillis())
        private set

    fun showSpeechBubble() {
        speechBubbleVisible = true
        speechBubbleShownAt = System.currentTimeMillis()
    }
    fun hideSpeechBubble() { speechBubbleVisible = false }

    // Action tracking (for results checklist)
    var reportsViewed: Boolean by mutableStateOf(false)
        private set
    var booksViewed: Boolean by mutableStateOf(false)
        private set
    var prescriptionViewed: Boolean by mutableStateOf(false)
        private set

    // Persisted prescription scroll position (pixels from top of LazyColumn)
    var prescriptionScrollIndex: Int by mutableStateOf(0)
        private set
    var prescriptionScrollOffset: Int by mutableStateOf(0)
        private set

    // ── Scoring result (filled after Hand Over) ───────────────────
    var lastScore: CaseScore? by mutableStateOf(null)
        private set

    // ─────────────────────────────────────────────────────────────
    fun loadCaseForModule(moduleId: String) {
        if (case == null) {
            case = SimulationCases.firstForModule(moduleId)
            sessionStartedAt = System.currentTimeMillis()
            lastActionAt = sessionStartedAt
        }
    }

    private fun touch() {
        lastActionAt = System.currentTimeMillis()
        // Any user action dismisses the speech bubble.
        speechBubbleVisible = false
    }

    // ── Rail / modal controls ─────────────────────────────────────
    fun toggleRail(button: RailButton) {
        if (locked) return
        activeRail = if (activeRail == button) null else button
        // Some rail buttons re-use modals / mark viewed
        when (button) {
            RailButton.PRESCRIPTION -> {
                showPrescription = activeRail == RailButton.PRESCRIPTION
                if (showPrescription) prescriptionViewed = true
            }
            RailButton.REPORTS -> { if (activeRail == RailButton.REPORTS) reportsViewed = true }
            else -> Unit
        }
        touch()
    }

    fun openPrescription() {
        if (locked) return
        showPrescription = true; activeRail = RailButton.PRESCRIPTION
        prescriptionViewed = true
        touch()
    }
    fun closePrescription() {
        showPrescription = false
        if (activeRail == RailButton.PRESCRIPTION) activeRail = null
    }

    fun openStorage(s: DrugStorage) { if (locked) return; showStorage = s; touch() }
    fun closeStorage() { showStorage = null }

    fun openCalculator() { if (locked) return; showCalculator = true; touch() }
    fun closeCalculator() { showCalculator = false }

    fun openPatientFiles() { if (locked) return; showPatientFiles = true; touch() }
    fun closePatientFiles() { showPatientFiles = false }

    fun openClinicalReference() { if (locked) return; showClinicalReference = true; booksViewed = true; touch() }
    fun closeClinicalReference() { showClinicalReference = false }

    /** Open the reports panel (also called from the lab_reports counter prop). */
    fun openReports() {
        if (locked) return
        activeRail = RailButton.REPORTS
        reportsViewed = true
        touch()
    }

    fun openLabeling() { if (locked) return; labelingPrefillDrugId = null; editingLabelId = null; showLabeling = true; touch() }
    /** Open labeling pre-filled with a specific drug (from Cart / Drugs panel / Storage Add). */
    fun openLabelingFor(drugId: String) {
        if (locked) return
        labelingPrefillDrugId = drugId
        editingLabelId = null
        showLabeling = true
        touch()
    }
    /** Re-open a completed label for editing. */
    fun editLabel(labelId: String) {
        if (locked) return
        editingLabelId = labelId
        labelingPrefillDrugId = completedLabels.firstOrNull { it.id == labelId }?.drugId
        showLabeling = true
        touch()
    }
    fun closeLabeling() { showLabeling = false; labelingPrefillDrugId = null; editingLabelId = null }

    fun openHoldForm() { if (locked) return; showHoldForm = true; touch() }
    fun closeHoldForm() { showHoldForm = false }

    fun openHandOverConfirm() { if (locked) return; showHandOverConfirm = true }
    fun closeHandOverConfirm() { showHandOverConfirm = false }

    // ── Prescription scroll persistence ───────────────────────────
    fun savePrescriptionScroll(index: Int, offset: Int) {
        prescriptionScrollIndex = index
        prescriptionScrollOffset = offset
    }

    // ── Cart / drug selection ─────────────────────────────────────
    fun addDrugToCart(drug: CatalogDrug) {
        if (locked) return
        val existing = cart.indexOfFirst { it.drugId == drug.id }
        if (existing >= 0) {
            cart[existing] = cart[existing].copy(quantity = cart[existing].quantity + 1)
        } else {
            cart.add(CartItem(drug.id, drug.name, drug.strength))
        }
        if (selectedDrugs.none { it.id == drug.id }) selectedDrugs.add(drug)
        touch()
    }

    fun removeFromCart(drugId: String) {
        cart.removeAll { it.drugId == drugId }
        touch()
    }

    /** Update the quantity for a cart line. If qty <= 0, the line is removed. */
    fun setCartQuantity(drugId: String, qty: Int) {
        if (locked) return
        val idx = cart.indexOfFirst { it.drugId == drugId }
        if (idx >= 0) {
            if (qty <= 0) cart.removeAt(idx)
            else cart[idx] = cart[idx].copy(quantity = qty)
            touch()
        }
    }

    // ── Labels ────────────────────────────────────────────────────
    fun addLabel(label: CompletedLabel) {
        if (locked) return
        val editing = editingLabelId
        if (editing != null) {
            val idx = completedLabels.indexOfFirst { it.id == editing }
            if (idx >= 0) {
                completedLabels[idx] = label.copy(id = editing)
            } else {
                completedLabels.add(0, label)
            }
            editingLabelId = null
        } else {
            completedLabels.add(0, label)   // newest on top
        }
        touch()
    }

    fun addHold(hold: HoldEntry) {
        if (locked) return
        holds.add(0, hold)
        touch()
    }

    // ── Chat ──────────────────────────────────────────────────────
    fun ask(question: String) {
        if (locked) return
        val answer = case?.chatQuestions?.firstOrNull { it.question == question }?.answer
            ?: "I'm not sure about that."
        chatLog.add(ChatExchange(question, answer))
        touch()
    }

    // ── Notes ─────────────────────────────────────────────────────
    fun updateNotes(text: String) { if (!locked) { notes = text; touch() } }

    // ── Calculator log ────────────────────────────────────────────
    fun logCalculator(tool: String, inputs: Map<String, String>, result: String) {
        if (locked) return
        calculatorLog.add(CalculatorUsage(tool, inputs, result))
        touch()
    }

    // ── Patients ──────────────────────────────────────────────────
    fun addPatient(p: StoredPatient) { if (!locked) { storedPatients.add(p); touch() } }
    fun selectPatient(p: StoredPatient) {
        if (locked) return
        selectedPatient = p
        patientJustSelectedAt = System.currentTimeMillis()
        touch()
        // Auto-advance the workflow: open the "Build a Label" screen so
        // the pharmacist can immediately start building labels (with the
        // full drug/dose/aux/save flow) for the selected patient.
        labelingPrefillDrugId = null
        editingLabelId = null
        showLabeling = true
    }

    // ── Ask About / chat redesign ─────────────────────────────────
    /** Add a pharmacist→patient Q/A pair from the Ask-About panel. */
    fun askAbout(q: AskQuestion) {
        if (locked) return
        // Avoid duplicates: replace existing entry if the same question was asked.
        chatLog.removeAll { it.question == q.questionText }
        chatLog.add(ChatExchange(q.questionText, q.patientAnswer))
        touch()
    }
    fun clearChat() { chatLog.clear(); touch() }

    // ─────────────────────────────────────────────────────────────
    // Hand-over → scoring → submission
    // ─────────────────────────────────────────────────────────────
    fun handOverAndScore(): CaseScore {
        locked = true
        val c = case ?: error("no case")
        val items = c.prescription.items

        val drugResults = items.map { item ->
            val matchingLabel = completedLabels.firstOrNull { it.drugId == item.expectedDrugId }
            val matchingHold = holds.firstOrNull { it.drugId == item.expectedDrugId }

            val fields = mutableListOf<FieldResult>()

            if (item.mustHold) {
                // Holding correctly is the entire correctness criterion
                val held = matchingHold != null
                val reasonOk = held && (matchingHold!!.reason.contains(
                    item.expectedHoldReason?.split(" ")?.firstOrNull() ?: "",
                    ignoreCase = true
                ) || matchingHold.reason.length >= 8)
                fields += FieldResult(
                    "Hold decision",
                    "HOLD: ${item.expectedHoldReason ?: "expected hold"}",
                    if (held) "HOLD: ${matchingHold!!.reason}" else (matchingLabel?.let { "DISPENSED" } ?: "—"),
                    held
                )
                fields += FieldResult(
                    "Hold reason quality",
                    item.expectedHoldReason ?: "—",
                    matchingHold?.reason ?: "—",
                    reasonOk
                )
            } else {
                val drugCorrect = matchingLabel != null
                fields += FieldResult(
                    "Drug selected", item.expectedDrugId,
                    matchingLabel?.drugId ?: "—", drugCorrect
                )
                fields += FieldResult(
                    "Quantity", item.expectedQuantity,
                    matchingLabel?.quantity ?: "—",
                    matchingLabel?.quantity?.equals(item.expectedQuantity, ignoreCase = true) == true
                )
                fields += FieldResult(
                    "Dose", item.expectedDose,
                    matchingLabel?.dose ?: "—",
                    matchingLabel?.dose?.equals(item.expectedDose, ignoreCase = true) == true
                )
                fields += FieldResult(
                    "Direction", item.expectedDirection,
                    matchingLabel?.direction ?: "—",
                    matchingLabel?.direction?.equals(item.expectedDirection, ignoreCase = true) == true
                )
                val expectedAux = item.expectedAuxLabels.toSet()
                val actualAux = matchingLabel?.auxLabels?.toSet().orEmpty()
                fields += FieldResult(
                    "Auxiliary labels", expectedAux.joinToString(), actualAux.joinToString(),
                    expectedAux == actualAux
                )
            }
            DrugResult(item.displayName, fields)
        }

        val correct = drugResults.sumOf { it.correctCount }
        val total = drugResults.sumOf { it.total }
        val timeSec = (System.currentTimeMillis() - sessionStartedAt) / 1000

        // ── Build the action checklist (Step 5 results) ──
        val checklist = buildActionChecklist(c)

        val score = CaseScore(
            caseId = c.id,
            moduleId = c.moduleId,
            correct = correct,
            total = total,
            drugResults = drugResults,
            timeSeconds = timeSec,
            calculatorsUsed = calculatorLog.size,
            chatQuestionsAsked = chatLog.size,
            notesLength = notes.length,
            actionChecklist = checklist
        )
        lastScore = score
        submitToAdmin(score)
        return score
    }

    private fun buildActionChecklist(c: SimulationCase): List<ChecklistItem> {
        val items = c.prescription.items
        val expectedDrugIds = items.map { it.expectedDrugId }.toSet()
        val pickedDrugIds = completedLabels.map { it.drugId }.toSet()
        val allExpectedAux = items.flatMap { it.expectedAuxLabels }.toSet()
        val pickedAux = completedLabels.flatMap { it.auxLabels }.toSet()

        return listOf(
            ChecklistItem(
                title = "Patient selected",
                userValue = selectedPatient?.name ?: "—",
                expectedValue = c.prescription.patientName,
                status = if (selectedPatient != null) ChecklistStatus.DONE else ChecklistStatus.MISSED
            ),
            ChecklistItem(
                title = "Prescription reviewed",
                userValue = if (prescriptionViewed) "Yes" else "No",
                expectedValue = "Required",
                status = if (prescriptionViewed) ChecklistStatus.DONE else ChecklistStatus.MISSED
            ),
            ChecklistItem(
                title = "Drugs selected",
                userValue = pickedDrugIds.size.toString(),
                expectedValue = expectedDrugIds.size.toString(),
                status = if (pickedDrugIds == expectedDrugIds) ChecklistStatus.DONE
                else ChecklistStatus.MISSED,
                note = if (pickedDrugIds != expectedDrugIds) "Mismatch with prescription" else ""
            ),
            ChecklistItem(
                title = "Quantities entered",
                userValue = completedLabels.count { it.quantity.isNotBlank() }.toString(),
                expectedValue = items.size.toString(),
                status = if (completedLabels.count { it.quantity.isNotBlank() } >= items.size)
                    ChecklistStatus.DONE else ChecklistStatus.MISSED
            ),
            ChecklistItem(
                title = "Labels created",
                userValue = completedLabels.size.toString(),
                expectedValue = items.count { !it.mustHold }.toString(),
                status = if (completedLabels.size >= items.count { !it.mustHold })
                    ChecklistStatus.DONE else ChecklistStatus.MISSED
            ),
            ChecklistItem(
                title = "Auxiliary labels selected",
                userValue = pickedAux.joinToString().ifBlank { "—" },
                expectedValue = allExpectedAux.joinToString().ifBlank { "none required" },
                status = when {
                    allExpectedAux.isEmpty() -> ChecklistStatus.NOT_NEEDED
                    pickedAux.containsAll(allExpectedAux) -> ChecklistStatus.DONE
                    else -> ChecklistStatus.MISSED
                }
            ),
            ChecklistItem(
                title = "Reports checked",
                userValue = if (reportsViewed) "Yes" else "No",
                expectedValue = "Recommended",
                status = if (reportsViewed) ChecklistStatus.DONE else ChecklistStatus.MISSED
            ),
            ChecklistItem(
                title = "Books / references checked",
                userValue = if (booksViewed) "Yes" else "No",
                expectedValue = "Recommended",
                status = if (booksViewed) ChecklistStatus.DONE else ChecklistStatus.NOT_NEEDED
            ),
            ChecklistItem(
                title = "Calculator used",
                userValue = "${calculatorLog.size} time(s)",
                expectedValue = "As needed",
                status = if (calculatorLog.isNotEmpty()) ChecklistStatus.DONE
                else ChecklistStatus.NOT_NEEDED
            ),
            ChecklistItem(
                title = "Patient communication",
                userValue = "${chatLog.size} question(s) asked",
                expectedValue = "${c.chatQuestions.size} available",
                status = if (chatLog.isNotEmpty()) ChecklistStatus.DONE else ChecklistStatus.MISSED
            ),
            ChecklistItem(
                title = "Holds raised",
                userValue = holds.size.toString(),
                expectedValue = items.count { it.mustHold }.toString(),
                status = if (holds.size >= items.count { it.mustHold }) ChecklistStatus.DONE
                else ChecklistStatus.MISSED
            ),
            ChecklistItem(
                title = "Handover completed",
                userValue = "Submitted",
                expectedValue = "Required",
                status = ChecklistStatus.DONE
            )
        )
    }

    // ─────────────────────────────────────────────────────────────
    // ADMIN DASHBOARD CONTRACT (Firestore)
    //
    // Collection root:
    //   simulation_results/{autoId}
    // Document shape:
    //   userId, caseId, moduleId, score{correct,total,percent,passed},
    //   drugResults[], timeSeconds, calculatorsUsed, chatQuestionsAsked,
    //   notesLength, submittedAt
    //
    // Aggregations the admin reads:
    //   - Per-user history:   simulation_results where userId == X order by submittedAt desc
    //   - Per-case stats:     simulation_results where caseId == Y → average percent
    //   - Drill-down:         simulation_results/{id}
    // ─────────────────────────────────────────────────────────────
    private fun submitToAdmin(score: CaseScore) {
        val uid = auth.currentUser?.uid ?: return
        val payload = mapOf(
            "userId" to uid,
            "caseId" to score.caseId,
            "moduleId" to score.moduleId,
            "score" to mapOf(
                "correct" to score.correct,
                "total" to score.total,
                "percent" to score.percent,
                "passed" to score.passed
            ),
            "drugResults" to score.drugResults.map { dr ->
                mapOf(
                    "drug" to dr.drugDisplay,
                    "fields" to dr.fields.map {
                        mapOf(
                            "field" to it.field,
                            "expected" to it.expected,
                            "actual" to it.actual,
                            "correct" to it.correct
                        )
                    }
                )
            },
            "timeSeconds" to score.timeSeconds,
            "calculatorsUsed" to score.calculatorsUsed,
            "chatQuestionsAsked" to score.chatQuestionsAsked,
            "notesLength" to score.notesLength,
            "submittedAt" to Timestamp.now()
        )
        firestore.collection("simulation_results").add(payload)
    }
}

