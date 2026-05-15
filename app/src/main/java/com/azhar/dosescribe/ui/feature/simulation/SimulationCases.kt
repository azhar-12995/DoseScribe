package com.azhar.dosescribe.ui.feature.simulation

// ─────────────────────────────────────────────────────────────────
// Sample case repository. Add a new case = add a new entry here.
// The simulation engine renders any case purely from this data.
// ─────────────────────────────────────────────────────────────────
object SimulationCases {

    // A small starter drug catalogue. Add more as needed.
    private val DEFAULT_DRUGS: List<CatalogDrug> = listOf(
        CatalogDrug("paracetamol_500", "Paracetamol", "500 mg tablet", DrugStorage.SHELF),
        CatalogDrug("ibuprofen_400", "Ibuprofen", "400 mg tablet", DrugStorage.SHELF),
        CatalogDrug("amoxicillin_500", "Amoxicillin", "500 mg capsule", DrugStorage.SHELF),
        CatalogDrug("azithromycin_500", "Azithromycin", "500 mg tablet", DrugStorage.SHELF),
        CatalogDrug("cetirizine_10", "Cetirizine", "10 mg tablet", DrugStorage.SHELF),
        CatalogDrug("dextromethorphan_15", "Dextromethorphan", "15 mg syrup", DrugStorage.SHELF),
        CatalogDrug("salbutamol_100", "Salbutamol", "100 mcg inhaler", DrugStorage.SHELF),
        CatalogDrug("metformin_500", "Metformin", "500 mg tablet", DrugStorage.SHELF),
        CatalogDrug("omeprazole_20", "Omeprazole", "20 mg capsule", DrugStorage.SHELF),
        CatalogDrug("loratadine_10", "Loratadine", "10 mg tablet", DrugStorage.SHELF),
        CatalogDrug("ranitidine_150", "Ranitidine", "150 mg tablet", DrugStorage.SHELF),
        CatalogDrug("warfarin_5", "Warfarin", "5 mg tablet", DrugStorage.SHELF),

        CatalogDrug("insulin_glargine", "Insulin Glargine", "100 IU/mL", DrugStorage.FRIDGE),
        CatalogDrug("insulin_regular", "Insulin Regular", "100 IU/mL", DrugStorage.FRIDGE),
        CatalogDrug("ceftriaxone_1g", "Ceftriaxone", "1 g vial", DrugStorage.FRIDGE),
        CatalogDrug("influenza_vaccine", "Influenza Vaccine", "0.5 mL", DrugStorage.FRIDGE),

        CatalogDrug("morphine_10", "Morphine", "10 mg tablet", DrugStorage.SAFE),
        CatalogDrug("diazepam_5", "Diazepam", "5 mg tablet", DrugStorage.SAFE),
        CatalogDrug("alprazolam_0_5", "Alprazolam", "0.5 mg tablet", DrugStorage.SAFE),
        CatalogDrug("tramadol_50", "Tramadol", "50 mg capsule", DrugStorage.SAFE)
    )

    val ALL_CASES: List<SimulationCase> = listOf(
        SimulationCase(
            id = "S1",
            moduleId = "appropriateness_review",
            title = "Simulation S1",
            patientSprite = PatientSprite.OLD_MAN,
            entryStatement = "Hi, I came in to get this prescription filled for my son. " +
                    "He has been coughing for the last two days and has a sore throat.",
            prescription = Prescription(
                doctorName = "DR. Ahmed Khan",
                doctorQualification = "MBBS, FCPS (Medicine)",
                clinic = "Healthcare Clinic, Islamabad",
                phone = "051-1234567",
                date = "21 February 2026",
                patientName = "Muhammad Ali",
                patientAge = "32 years",
                patientGender = "Male",
                patientCity = "F-8, Islamabad",
                diagnosis = "Acute Viral Upper Respiratory Tract Infection (Fever, Cough, Sore Throat)",
                items = listOf(
                    PrescriptionItem(
                        displayName = "Paracetamol 500 mg tablets",
                        instructions = "Take 1 tablet every 6 hours as needed for fever or pain. Max 4 tablets/day. 20 tablets. Refills: 0.",
                        expectedDrugId = "paracetamol_500",
                        expectedQuantity = "20 tablets",
                        expectedDose = "500 mg",
                        expectedDirection = "1 tab PO q6h PRN fever or pain",
                        expectedAuxLabels = listOf("Take with food", "Avoid alcohol")
                    ),
                    PrescriptionItem(
                        displayName = "Dextromethorphan 15 mg syrup",
                        instructions = "Take 10 mL three times a day for cough. Bottle of 60 mL.",
                        expectedDrugId = "dextromethorphan_15",
                        expectedQuantity = "60 mL",
                        expectedDose = "15 mg / 5 mL",
                        expectedDirection = "10 mL PO TID",
                        expectedAuxLabels = listOf("May cause drowsiness", "Shake well before use")
                    ),
                    PrescriptionItem(
                        displayName = "Cetirizine 10 mg tablets",
                        instructions = "Take 1 tablet at bedtime daily. Quantity: 10 tablets.",
                        expectedDrugId = "cetirizine_10",
                        expectedQuantity = "10 tablets",
                        expectedDose = "10 mg",
                        expectedDirection = "1 tab PO HS",
                        expectedAuxLabels = listOf("May cause drowsiness", "Take at bedtime")
                    )
                )
            ),
            chatQuestions = listOf(
                ChatQa("Are you allergic to any medication?", "No, I am not allergic to any medication."),
                ChatQa("Do you have any chronic illness?", "No chronic illness; I am otherwise healthy."),
                ChatQa("Are you taking any other medications right now?", "No, nothing at the moment."),
                ChatQa("How long have these symptoms been going on?", "About two days now."),
                ChatQa("Is the patient pregnant or breastfeeding?", "Not applicable; the patient is male.")
            ),
            availableDrugs = DEFAULT_DRUGS
        ),

        // A second sample case showing a different patient sprite + an error in the prescription
        SimulationCase(
            id = "S2",
            moduleId = "high_alert_medications",
            title = "Simulation S2",
            patientSprite = PatientSprite.OLD_WOMAN,
            entryStatement = "I came to refill my warfarin. The doctor changed my dose recently.",
            prescription = Prescription(
                doctorName = "DR. Sara Iqbal",
                doctorQualification = "MBBS, FCPS (Cardiology)",
                clinic = "Heart Care Clinic, Lahore",
                phone = "042-9988776",
                date = "1 March 2026",
                patientName = "Ayesha Bano",
                patientAge = "72 years",
                patientGender = "Female",
                patientCity = "DHA, Lahore",
                diagnosis = "Atrial Fibrillation; on chronic anticoagulation",
                items = listOf(
                    PrescriptionItem(
                        displayName = "Warfarin 5 mg tablets",
                        instructions = "Take 1 tablet daily at the same time. 30 tablets.",
                        expectedDrugId = "warfarin_5",
                        expectedQuantity = "30 tablets",
                        expectedDose = "5 mg",
                        expectedDirection = "1 tab PO daily, same time each day",
                        expectedAuxLabels = listOf("Avoid alcohol", "Do not crush or chew")
                    ),
                    PrescriptionItem(
                        displayName = "Tramadol 50 mg capsules (combined with warfarin)",
                        instructions = "Take 1 capsule every 6 hours as needed for pain. 20 capsules.",
                        // This drug should be HELD (interaction with warfarin → bleeding risk)
                        expectedDrugId = "tramadol_50",
                        expectedQuantity = "20 capsules",
                        expectedDose = "50 mg",
                        expectedDirection = "Hold pending prescriber review",
                        expectedAuxLabels = emptyList(),
                        mustHold = true,
                        expectedHoldReason = "Tramadol increases warfarin's anticoagulant effect; bleeding risk."
                    )
                ),
                hasError = true,
                errorNote = "Tramadol + Warfarin combination has a clinically significant interaction; a hold is expected."
            ),
            chatQuestions = listOf(
                ChatQa("When was your last INR check?", "Last week, it was 2.4."),
                ChatQa("Are you on any other medications?", "Just my blood-pressure pill in the morning."),
                ChatQa("Any recent bleeding or bruising?", "I noticed small bruises on my arms.")
            ),
            availableDrugs = DEFAULT_DRUGS
        )
    )

    fun firstForModule(moduleId: String): SimulationCase {
        return ALL_CASES.firstOrNull { it.moduleId == moduleId } ?: ALL_CASES.first()
    }
}

