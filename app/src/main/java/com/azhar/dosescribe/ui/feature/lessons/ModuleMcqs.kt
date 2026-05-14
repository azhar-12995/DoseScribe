package com.azhar.dosescribe.ui.feature.lessons

data class McqQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val points: Int = 1
)

fun getMcqsForModule(moduleId: String): List<McqQuestion> {
    return when (moduleId) {

        "appropriateness_review" -> listOf(
            McqQuestion(
                question = "Patient B, age 15, was prescribed Methotrexate 10 mg QD and Folic Acid 5 mg QWK for Rheumatoid Arthritis. The pharmacist calls the prescriber for verification. What is the error?",
                options = listOf(
                    "Folic acid should be given daily, not weekly, to prevent toxicity",
                    "Methotrexate requires weight-based dosing in pediatric patients and 10 mg is too high",
                    "Methotrexate for RA is dosed weekly (QWK), not daily (QD) -- daily dosing can cause fatal toxicity",
                    "The prescription is correct; both drugs are appropriately dosed and scheduled"
                ),
                correctIndex = 2,
                explanation = "Methotrexate for rheumatoid arthritis is dosed weekly, not daily. Daily use can cause fatal toxicity."
            ),
            McqQuestion(
                question = "A 66-year-old diabetic and hypertensive woman is on Sitagliptin/Metformin (100/1000) XR and Empagliflozin/Metformin (12.5/1000) XR. Her HbA1c is 5.7%. She complains of occasional fainting episodes. What should the pharmacist do?",
                options = listOf(
                    "Reassure the patient -- fainting is a known postural side effect of empagliflozin due to volume depletion",
                    "HbA1c of 5.7% indicates therapy is failing; doses should be increased",
                    "Fainting is an expected GI-related side effect of high-dose metformin and will resolve over time",
                    "Inform the prescriber -- HbA1c of 5.7% on dual combination therapy suggests over-treatment and possible hypoglycemia; therapy modification is needed"
                ),
                correctIndex = 3,
                explanation = "HbA1c of 5.7% with fainting may suggest over-treatment or possible hypoglycemia, so the prescriber should be informed."
            ),
            McqQuestion(
                question = "Patient D received Dexlansoprazole 30 mg from Prescriber A and Pantoprazole 20 mg from Prescriber B. What should the pharmacist do?",
                options = listOf(
                    "Dispense both -- dexlansoprazole is a modified-release PPI and pantoprazole is conventional, so they serve different purposes",
                    "Dispense both -- they belong to different chemical subclasses of PPIs",
                    "Contact one prescriber to discontinue one -- this is therapeutic class duplication (both are PPIs) and increases adverse effect risk without added benefit",
                    "Replace pantoprazole with ranitidine to combine a PPI with an H2 blocker for better acid suppression"
                ),
                correctIndex = 2,
                explanation = "Dexlansoprazole and pantoprazole are both PPIs, so this is therapeutic duplication."
            ),
            McqQuestion(
                question = "A 78-year-old woman weighing 54 kg with non-valvular atrial fibrillation presents with serum creatinine of 1.8 mg/dL. Her prescription reads Apixaban 5 mg twice daily. What is the most appropriate action?",
                options = listOf(
                    "Continue Apixaban 5 mg BD -- dose reduction only applies if all three criteria are met simultaneously",
                    "Reduce to Apixaban 2.5 mg BD -- patient meets two or more of 3 dose-reduction criteria (age >= 80, weight <= 60 kg, serum creatinine >= 1.5 mg/dL)",
                    "Switch to Apixaban 10 mg BD -- standard NVAF dosing requires higher doses in elderly patients",
                    "Discontinue Apixaban -- it is contraindicated when serum creatinine exceeds 1.5 mg/dL"
                ),
                correctIndex = 1,
                explanation = "The patient meets at least two apixaban dose-reduction criteria: low body weight and raised serum creatinine."
            ),
            McqQuestion(
                question = "An 8-year-old boy is prescribed Montelukast 10 mg QD for 5 days along with nebulization for seasonal allergies. Is this correct therapy?",
                options = listOf(
                    "No -- montelukast is not indicated for allergies; azithromycin should be prescribed instead",
                    "Montelukast 10 mg is correct for his age, but nebulization is unnecessary for seasonal allergies",
                    "Montelukast dose should be 5 mg (6-14 years); 10 mg is the adult dose -- this is a dosing error",
                    "Both the drug choice and dose are incorrect; he should receive azithromycin and lower-dose montelukast"
                ),
                correctIndex = 2,
                explanation = "For children aged 6-14 years, montelukast dose is 5 mg. The 10 mg dose is for adults."
            ),
            McqQuestion(
                question = "A 30-year-old woman at 12 weeks gestation is suspected of having DVT. The physician asks the pharmacist to recommend an anticoagulant safe during pregnancy. Which is most appropriate?",
                options = listOf(
                    "Warfarin 5 mg daily with INR monitoring -- it is safe after the first trimester",
                    "Apixaban 2.5 mg BD -- DOACs are preferred over heparins in pregnancy for patient convenience",
                    "Unfractionated heparin IV infusion -- it is the only anticoagulant that does not cross the placenta",
                    "Enoxaparin (LMWH) subcutaneously -- preferred in pregnancy as it does not cross the placenta, has predictable pharmacokinetics, and doesn't require routine monitoring"
                ),
                correctIndex = 3,
                explanation = "LMWH such as enoxaparin is preferred in pregnancy because it does not cross the placenta and has predictable pharmacokinetics."
            ),
            McqQuestion(
                question = "A 37-year-old pregnant woman was prescribed Ciprofloxacin, Azithromycin, and Metronidazole for an infection. Should the pharmacist dispense ciprofloxacin?",
                options = listOf(
                    "Yes -- ciprofloxacin is safe in pregnancy when benefits outweigh risks and no alternative exists",
                    "No -- ciprofloxacin has an FDA Category C rating, so it requires prescriber re-evaluation but can still be dispensed",
                    "No -- ciprofloxacin is contraindicated in pregnancy due to risk of cartilage damage in the fetus; do not dispense and contact the prescriber",
                    "Yes -- fluoroquinolones are only contraindicated in the third trimester, not during earlier stages"
                ),
                correctIndex = 2,
                explanation = "Ciprofloxacin should not be dispensed in pregnancy without prescriber re-evaluation due to fetal cartilage risk."
            ),
            McqQuestion(
                question = "You receive a prescription for a patient with a documented history of severe rash with Amoxicillin one year ago: Cefixime 200 mg BID for UTI. What is the most appropriate pharmacist action?",
                options = listOf(
                    "Dispense as written -- cephalosporins and penicillins belong to entirely different drug classes with no cross-reactivity",
                    "Dispense but counsel the patient to watch for mild skin reactions as a precaution",
                    "Do not dispense -- there is a hypersensitivity cross-reactivity risk between penicillins and cephalosporins; contact the prescriber for an alternative",
                    "Dispense and co-prescribe diphenhydramine prophylactically to prevent any allergic reaction"
                ),
                correctIndex = 2,
                explanation = "There is a possible cross-reactivity risk between penicillins and cephalosporins, especially with a severe allergy history."
            ),
            McqQuestion(
                question = "You receive the following prescription: Linezolid 600 mg PO BID. Diagnosis: Mild sore throat, likely viral. Duration: 14 days. What type of error is present?",
                options = listOf(
                    "No error -- linezolid provides broad-spectrum coverage and is appropriate for throat infections",
                    "Drug-drug interaction -- linezolid interacts with common OTC cold medications the patient may be taking",
                    "Therapeutic duplication -- a macrolide antibiotic should replace linezolid for respiratory infections",
                    "Inappropriate drug use -- a reserve antibiotic (linezolid) is being prescribed for a likely viral illness where antibiotics are not indicated"
                ),
                correctIndex = 3,
                explanation = "Linezolid is a reserve antibiotic and is inappropriate for a likely viral sore throat."
            ),
            McqQuestion(
                question = "You receive a prescription for a patient currently on Levodopa/Carbidopa 100/25 mg TID. During counseling, the patient reports consuming high-protein meals (meat and dairy at every meal). What should the pharmacist identify?",
                options = listOf(
                    "No concern -- protein intake does not affect Parkinson's medications",
                    "The patient should increase the Levodopa dose to compensate for reduced absorption with food",
                    "This is an inappropriate route error -- Levodopa should be given IV to bypass dietary interference",
                    "Drug-food interaction -- dietary amino acids from high protein compete with Levodopa for absorption, reducing its effectiveness; the patient needs dietary counseling"
                ),
                correctIndex = 3,
                explanation = "High-protein meals can reduce levodopa absorption because amino acids compete with levodopa."
            )
        )

        "auxiliary_labels" -> listOf(
            McqQuestion(
                question = "A pharmacist dispenses Norepinephrine 4mg/250mL IV infusion. An intern asks why it needs a special label when it's clearly labeled by the manufacturer. What's the BEST response?",
                options = listOf(
                    "It doesn't need one -- manufacturer label is sufficient",
                    "High Alert label is required because errors with this drug can cause fatal outcomes",
                    "LASA label is required because it sounds like Epinephrine",
                    "Both B and C are correct"
                ),
                correctIndex = 3,
                explanation = "Norepinephrine requires high-alert labeling, and it is also a LASA risk with epinephrine."
            ),
            McqQuestion(
                question = "A patient returns Insulin Glargine to the pharmacy saying it looks cloudy. Which labeling instruction, if followed correctly, would have PREVENTED this complaint?",
                options = listOf(
                    "Shake well before use",
                    "Keep refrigerated, do not freeze",
                    "Store below 25C, protect from light",
                    "High alert medication"
                ),
                correctIndex = 1,
                explanation = "Proper storage labeling helps prevent insulin damage from freezing or incorrect temperature handling."
            ),
            McqQuestion(
                question = "A nurse calls saying she accidentally gave a Tiotropium Rotacap orally instead of via inhaler. Which TWO auxiliary labels should have been on this dispensing?",
                options = listOf(
                    "Not for oral use + High Alert",
                    "Not for oral use + For inhalation use only",
                    "High Alert + Store in refrigerator",
                    "For inhalation use only + Shake well before use"
                ),
                correctIndex = 1,
                explanation = "Tiotropium Rotacap should be clearly labeled as not for oral use and for inhalation use only."
            ),
            McqQuestion(
                question = "Cisatracurium is being sent to ICU. The receiving nurse is not the usual one. What is the MOST critical auxiliary label and why?",
                options = listOf(
                    "Store away from light -- it degrades rapidly",
                    "Not for IV push -- to prevent rapid bolus administration",
                    "Warning: Paralyzing Agent -- Must be used on intubated patients only",
                    "High Alert -- all ICU drugs carry this label"
                ),
                correctIndex = 2,
                explanation = "Cisatracurium is a neuromuscular blocker and must be labeled as a paralyzing agent."
            ),
            McqQuestion(
                question = "A prescription reads Amoxicillin-Clavulanate 400/57mg per 5mL -- dispense dry powder. The caregiver is a first-time parent. Which combination of labels is MOST appropriate?",
                options = listOf(
                    "Shake well before use + Refrigerate after reconstitution",
                    "Refrigerate after reconstitution + Reconstitute before use",
                    "For oral use only + Do not freeze",
                    "No auxiliary label needed"
                ),
                correctIndex = 1,
                explanation = "Dry powder antibiotics should be labeled to reconstitute before use and refrigerate after reconstitution."
            ),
            McqQuestion(
                question = "A pharmacist is dispensing both Dopamine and Dobutamine to two different ICU patients on the same shift. What is the SINGLE most important label to apply to BOTH?",
                options = listOf(
                    "High Alert",
                    "For IV infusion only",
                    "LASA -- Look-Alike Sound-Alike",
                    "Must be diluted before use"
                ),
                correctIndex = 2,
                explanation = "Dopamine and dobutamine are look-alike sound-alike medications."
            ),
            McqQuestion(
                question = "Warfarin 5mg is dispensed to a newly admitted patient. The nurse asks why it has a High Alert sticker when it's just a tablet. Your BEST explanation is:",
                options = listOf(
                    "All tablets in ICU get this label by default",
                    "High Alert is applied because dosing errors with Warfarin can cause life-threatening bleeding or clotting",
                    "It is a LASA drug that looks like other white tablets",
                    "It is required only for injectable medications"
                ),
                correctIndex = 1,
                explanation = "Warfarin is high-alert because dosing errors can cause serious bleeding or thrombosis."
            ),
            McqQuestion(
                question = "A patient with a known sensitivity to tetracyclines is prescribed Doxycycline by a gynecologist. The system fires an alert. Beyond allergy, what OTHER alert is most clinically relevant here?",
                options = listOf(
                    "Mild sedation risk",
                    "Teratogenic -- unsafe in pregnancy",
                    "LASA with Doxazosin",
                    "Requires refrigeration"
                ),
                correctIndex = 1,
                explanation = "Doxycycline is clinically important to flag in pregnancy because of teratogenic and fetal safety concerns."
            ),
            McqQuestion(
                question = "Metoprolol Succinate 100mg XR is dispensed to a patient who also receives a pill crusher with their meal tray. Which label is CRITICAL and what is the risk if ignored?",
                options = listOf(
                    "Take with food -- risk of GI upset",
                    "Do not crush or chew -- risk of dose dumping and toxicity",
                    "High Alert -- risk of cardiac arrest",
                    "Store below 25C -- risk of degradation"
                ),
                correctIndex = 1,
                explanation = "Extended-release metoprolol should not be crushed because it can cause dose dumping and toxicity."
            ),
            McqQuestion(
                question = "A clinical pharmacist reviews a chemotherapy order for Alemtuzumab. The intern asks which labels apply. What is the CORRECT answer?",
                options = listOf(
                    "High Alert only",
                    "Chemotherapeutic -- High Alert + LASA",
                    "Chemotherapeutic -- High Alert only",
                    "Chemotherapeutic -- High Alert + LASA + Not for Oral Use"
                ),
                correctIndex = 3,
                explanation = "Alemtuzumab requires chemotherapeutic, high-alert, LASA, and not-for-oral-use labeling."
            )
        )

        "chemo_dose_adjustments" -> listOf(
            McqQuestion(
                question = "Patient: 60 kg, 170 cm. Regimen: Doxorubicin 50 mg/m2 IV Day 1. What is the correct dose?",
                options = listOf("82.5 mg", "84 mg", "90 mg", "100 mg"),
                correctIndex = 1,
                explanation = "Using BSA-based dosing, the correct doxorubicin dose is 84 mg."
            ),
            McqQuestion(
                question = "Same patient (60 kg, 170 cm). Regimen: Cyclophosphamide 750 mg/m2 IV Day 1. What dose should be dispensed?",
                options = listOf("1,200 mg", "1,250 mg", "1,260 mg", "1,300 mg"),
                correctIndex = 2,
                explanation = "Using BSA-based dosing, the correct cyclophosphamide dose is 1,260 mg."
            ),
            McqQuestion(
                question = "Order: Vincristine 2 mg/m2 IV Day 1; Patient BSA = 1.50 m2. What dose should be administered?",
                options = listOf("3.0 mg", "2.5 mg", "2.0 mg", "1.5 mg"),
                correctIndex = 2,
                explanation = "Although 2 mg/m2 x 1.50 m2 equals 3 mg, vincristine is commonly capped at 2 mg to reduce neurotoxicity risk."
            ),
            McqQuestion(
                question = "Regimen: Oxaliplatin 130 mg/m2 Day 1. Prescribed: 283.4 mg. What is the implied BSA?",
                options = listOf("1.73 m2", "1.95 m2", "2.18 m2", "2.30 m2"),
                correctIndex = 2,
                explanation = "Implied BSA = 283.4 / 130 = 2.18 m2."
            ),
            McqQuestion(
                question = "Regimen: Paclitaxel 175 mg/m2 Day 1. Prescribed: 240 mg. What is the implied BSA?",
                options = listOf("1.20 m2", "1.50 m2", "1.37 m2", "1.42 m2"),
                correctIndex = 2,
                explanation = "Implied BSA = 240 / 175 = approximately 1.37 m2."
            )
        )

        "compounding_calculations" -> listOf(
            McqQuestion(
                question = "A physician prescribes Potassium phosphate oral solution 1 g/5 mL, total 50 mL. The available raw ingredient is potassium phosphate (monobasic) powder. How much powder is required to prepare the solution?",
                options = listOf("5 g", "7 g", "10 g", "12.5 g"),
                correctIndex = 2,
                explanation = "1 g/5 mL means 10 g is required for 50 mL."
            ),
            McqQuestion(
                question = "A pharmacist receives a prescription for Captopril oral solution 1 mg/mL, total 600 mL. The available tablets are 50 mg each. How many tablets are required to compound the solution?",
                options = listOf("6 tablets", "8 tablets", "12 tablets", "24 tablets"),
                correctIndex = 2,
                explanation = "Total drug required is 600 mg. Each tablet is 50 mg, so 600 / 50 = 12 tablets."
            ),
            McqQuestion(
                question = "A physician prescribes Potassium citrate oral solution 10 mEq/5 mL, total 75 mL. Potassium citrate provides 1 mEq = 1.08 g. How many grams of potassium citrate are required to prepare this solution?",
                options = listOf("36.0 g", "162.0 g", "48.4 g", "454.0 g"),
                correctIndex = 1,
                explanation = "Total mEq required = 10 mEq/5 mL x 75 mL = 150 mEq. 150 x 1.08 g = 162 g."
            ),
            McqQuestion(
                question = "A prescription requires Dexamethasone oral solution 1 mg/mL, total 16 mL. The available stock is Decadron injection 4 mg/mL. How many mL of Decadron are required?",
                options = listOf("2 mL", "4 mL", "8 mL", "16 mL"),
                correctIndex = 1,
                explanation = "Total dexamethasone required is 16 mg. Stock is 4 mg/mL, so 16 / 4 = 4 mL."
            ),
            McqQuestion(
                question = "A physician prescribes Dexamethasone oral solution 1 mg/mL, 3 mg PO q12h for 7 days. The available stock is Decadron injection 4 mg/mL. The pharmacist decides to round to convenient volumes during compounding. Which of the following is the most appropriate preparation?",
                options = listOf(
                    "10 mL Decadron + 32 mL D25% -> 42 mL final",
                    "10.5 mL Decadron + 31.5 mL D25% -> 42 mL final",
                    "11 mL Decadron + 33 mL D25% -> 44 mL final",
                    "12 mL Decadron + 30 mL D25% -> 42 mL final"
                ),
                correctIndex = 2,
                explanation = "The rounded convenient preparation is 11 mL Decadron plus 33 mL D25%, giving a 44 mL final volume."
            ),
            McqQuestion(
                question = "A physician prescribes Sodium benzoate oral solution, 360 mg PO q12h for 5 days. The pharmacist decides to prepare a 100 mg/mL syrup. How many grams of sodium benzoate are required for a 40 mL preparation?",
                options = listOf("2 g", "3.6 g", "4 g", "5 g"),
                correctIndex = 2,
                explanation = "A 100 mg/mL solution needs 100 x 40 = 4000 mg, which is 4 g."
            )
        )

        "counseling" -> listOf(
            McqQuestion(
                question = "You are verifying a vancomycin order for a 78-year-old inpatient with CKD (CrCl ~25 mL/min). The prescriber ordered 1 g IV every 12 hours. The nurse insists, They said it's urgent -- the doctor wants it right away. What is your BEST immediate action as the pharmacist?",
                options = listOf(
                    "Send the dose now to avoid delaying therapy -- renal adjustment can be made later",
                    "Reduce the frequency yourself to every 24 hours and dispense",
                    "Hold dispensing until you confirm the dosing with the prescriber",
                    "Dispense as written but flag for pharmacy review on the next shift"
                ),
                correctIndex = 2,
                explanation = "Vancomycin requires renal dose adjustment in CKD, so the pharmacist should hold dispensing and confirm with the prescriber."
            ),
            McqQuestion(
                question = "You are counseling a patient being discharged on methotrexate 10 mg once weekly for rheumatoid arthritis. On the discharge sheet, the printed directions read Take one tablet daily. The patient says, The doctor just said to take it for my joints, I didn't catch how often. What is your IMMEDIATE next step?",
                options = listOf(
                    "Dispense as written and clarify dosing at the follow-up clinic",
                    "Correct the instructions to weekly and hand it over",
                    "Stop the process and urgently clarify with the prescriber before dispensing",
                    "Give the medicine with a large Weekly Dose Only warning sticker"
                ),
                correctIndex = 2,
                explanation = "Methotrexate daily instead of weekly can be fatal, so dispensing must stop until the dose is clarified."
            ),
            McqQuestion(
                question = "A patient using a capsule-type dry powder inhaler (Rotacap) reports poor symptom control despite doing everything right. On observation, they exhale gently into the device before loading and then inhale. Which is the MOST likely explanation?",
                options = listOf(
                    "Exhaling into the device introduced moisture causing powder clumping and reduced dose.",
                    "They are holding their breath too long after inhalation.",
                    "They should prime the device by shaking before each use.",
                    "The spacer is required with a DPI -- advise using one."
                ),
                correctIndex = 0,
                explanation = "Exhaling into a dry powder inhaler can introduce moisture, causing powder clumping and reduced dose delivery."
            ),
            McqQuestion(
                question = "Which statement about the Trulicity (dulaglutide) prefilled pen is FALSE?",
                options = listOf(
                    "You should shake the pen vigorously before each injection to mix the medicine.",
                    "It is a single-use, prefilled, disposable weekly subcutaneous pen.",
                    "After pressing the injection button you must hold the pen against skin until you hear the second click.",
                    "Store refrigerated; if kept at room temperature it must be used within the manufacturer's specified days."
                ),
                correctIndex = 0,
                explanation = "Trulicity should not be shaken vigorously before use."
            ),
            McqQuestion(
                question = "A new RA patient on weekly methotrexate asks about folic acid to reduce side effects. The BEST counselling statement is:",
                options = listOf(
                    "Take folic acid daily except on the methotrexate day -- follow prescriber's exact dose.",
                    "Only take folic acid on the same day as methotrexate.",
                    "Avoid folic acid while on methotrexate because it reduces its effect.",
                    "Folic acid is unnecessary with injectable methotrexate."
                ),
                correctIndex = 0,
                explanation = "Folic acid is commonly used to reduce methotrexate side effects and is usually avoided on the methotrexate day unless prescribed otherwise."
            ),
            McqQuestion(
                question = "Which behavior MOST increases a patient's risk of life-threatening fentanyl overdose from a patch?",
                options = listOf(
                    "Applying the patch to the upper outer arm as instructed.",
                    "Using a heating pad over the patch for extra pain-relief.",
                    "Removing the old patch before applying a new one.",
                    "Not cutting the patch and keeping it sealed until use."
                ),
                correctIndex = 1,
                explanation = "Heat can increase fentanyl absorption from patches and may cause life-threatening overdose."
            ),
            McqQuestion(
                question = "In a hot climate a patient finds their glycerin suppository soft and sticky at room temperature. Best counseling?",
                options = listOf(
                    "Warm it briefly under hot water to make insertion easier.",
                    "Chill it briefly in the refrigerator or under cold running water to harden, then insert.",
                    "Break it into pieces and insert fragments.",
                    "Microwave for 10-15 seconds to firm it up."
                ),
                correctIndex = 1,
                explanation = "A softened suppository may be chilled briefly to harden before insertion."
            ),
            McqQuestion(
                question = "For maximum retention and effectiveness of a single-use antifungal vaginal pessary, the MOST practical advice is:",
                options = listOf(
                    "Insert at bedtime, remain lying down for a short while, and avoid intercourse for the treatment course.",
                    "Insert in the morning and remain active to promote distribution.",
                    "Remove it after 1-2 hours to prevent leakage.",
                    "Use vaginal douches afterward to remove residue and increase absorption."
                ),
                correctIndex = 0,
                explanation = "Using the pessary at bedtime and remaining lying down helps improve retention and effectiveness."
            ),
            McqQuestion(
                question = "A caregiver reports finding two rivastigmine patches adhered to the patient (one old, one new). The patient feels increasingly dizzy and weak. What is the BEST immediate action?",
                options = listOf(
                    "Ignore -- overlapping patches are fine for a short period.",
                    "Remove both patches immediately, wash hands, monitor patient, and contact prescriber/seek medical advice.",
                    "Peel off the older patch only and leave the newer one in place.",
                    "Cut one patch in half to reduce dose and leave both for absorption."
                ),
                correctIndex = 1,
                explanation = "Overlapping rivastigmine patches can cause overdose. Remove both patches and seek medical advice."
            )
        )

        "drug_label" -> listOf(
            McqQuestion(
                question = "You are dispensing Heparin Sodium injection. Which special instruction should be included on the label?",
                options = listOf(
                    "Use within 14 days of opening; monitor INR and hold if >80 sec",
                    "Use within 28 days of opening; monitor APTT and hold if >100 sec",
                    "Use within 28 days of opening; monitor PT and hold if >60 sec",
                    "Use within 7 days of opening; monitor APTT and hold if >120 sec"
                ),
                correctIndex = 1,
                explanation = "Heparin label instructions should include use within 28 days of opening and APTT monitoring."
            ),
            McqQuestion(
                question = "You are dispensing Ecosprin (Aspirin) 75 mg tablets. Which special instruction should be included?",
                options = listOf(
                    "Chew the tablet thoroughly before swallowing for faster onset",
                    "Crush and mix with water if difficulty swallowing",
                    "Do not chew or crush; swallow whole",
                    "Take on an empty stomach for better absorption"
                ),
                correctIndex = 2,
                explanation = "Aspirin EC tablets should not be crushed or chewed and should be swallowed whole."
            ),
            McqQuestion(
                question = "You are dispensing Hydrocortisone Sodium Succinate injection. Which special instruction is correct?",
                options = listOf(
                    "After reconstitution, use within 24 hours and refrigerate",
                    "After reconstitution, use immediately and discard any remaining solution",
                    "After reconstitution, store at room temperature for up to 7 days",
                    "After reconstitution, use within 6 hours; may store remaining at 2-8C"
                ),
                correctIndex = 1,
                explanation = "Hydrocortisone sodium succinate injection should be used immediately after reconstitution, and remaining solution should be discarded."
            ),
            McqQuestion(
                question = "You are dispensing Flagyl (Metronidazole) 400 mg tablets. Which special instruction should be included?",
                options = listOf(
                    "Take on an empty stomach for optimal absorption",
                    "Do not take on an empty stomach",
                    "Extended-release formulation; do not crush or chew",
                    "Take at bedtime only to reduce GI side effects"
                ),
                correctIndex = 1,
                explanation = "Metronidazole may cause stomach upset, so it should not be taken on an empty stomach."
            ),
            McqQuestion(
                question = "You are dispensing Gravinate (Dimenhydrinate) injection. Which combination of instructions is correct?",
                options = listOf(
                    "May cause drowsiness; give slow IV over 2-4 min; do not mix with Tramadol",
                    "May cause drowsiness; give rapid IV push; do not mix with Metronidazole",
                    "Does not cause drowsiness; give slow IV over 10-15 min; do not mix with Tramadol",
                    "May cause drowsiness; give slow IV over 2-4 min; safe to mix with Tramadol"
                ),
                correctIndex = 0,
                explanation = "Dimenhydrinate may cause drowsiness, should be given slow IV over 2-4 minutes, and should not be mixed with tramadol."
            ),
            McqQuestion(
                question = "You are dispensing Clonazepam tablets. Which special instructions should be included?",
                options = listOf(
                    "May cause drowsiness; Category C; use with caution in pregnancy",
                    "May cause drowsiness; Category D; avoid in pregnancy",
                    "Does not cause drowsiness; Category D; avoid in pregnancy",
                    "May cause drowsiness; Category B; safe in pregnancy under supervision"
                ),
                correctIndex = 1,
                explanation = "Clonazepam may cause drowsiness and should be avoided in pregnancy due to Category D risk."
            ),
            McqQuestion(
                question = "You are dispensing Valproic Acid injection. Which set of instructions is fully correct?",
                options = listOf(
                    "Dilute in NS or D5W; infuse at no more than 20 mg/min; Category D; contraindicated in pregnancy",
                    "Dilute in NS or Ringer's Lactate; infuse at no more than 40 mg/min; Category C; avoid in pregnancy",
                    "Dilute in NS or D5W; infuse at no more than 50 mg/min; Category D; contraindicated in pregnancy",
                    "Dilute in D5W only; infuse at no more than 20 mg/min; Category X; contraindicated in pregnancy"
                ),
                correctIndex = 0,
                explanation = "Valproic acid injection should be diluted in NS or D5W, infused at no more than 20 mg/min, and is contraindicated in pregnancy."
            ),
            McqQuestion(
                question = "You are dispensing Plavix (Clopidogrel) + Aspirin combination therapy. Which special instruction should be included?",
                options = listOf(
                    "May discontinue one agent if side effects occur without consulting a physician",
                    "Do not stop without doctor's advice; report any signs of bleeding to the doctor",
                    "Safe to stop if no bleeding occurs after 6 months",
                    "Take on an empty stomach; no need to report minor bruising"
                ),
                correctIndex = 1,
                explanation = "Dual antiplatelet therapy should not be stopped without medical advice, and bleeding signs should be reported."
            ),
            McqQuestion(
                question = "You are dispensing Sevelamer HCl tablets. Which set of instructions is correct?",
                options = listOf(
                    "Take on an empty stomach; monitor calcium levels; do not chew or crush",
                    "Take with meals; monitor phosphorus levels; do not chew or crush",
                    "Take with meals; monitor potassium levels; may crush if needed",
                    "Take 1 hour before meals; monitor phosphorus levels; chew thoroughly"
                ),
                correctIndex = 1,
                explanation = "Sevelamer should be taken with meals, phosphorus should be monitored, and tablets should not be chewed or crushed."
            ),
            McqQuestion(
                question = "You are dispensing Captopril 100 mL syrup. Which critical warning must be included on the label?",
                options = listOf(
                    "Avoid in renal impairment",
                    "Avoid in patients over 65 years of age",
                    "Avoid in pregnancy",
                    "Avoid in patients with diabetes"
                ),
                correctIndex = 2,
                explanation = "Captopril should be avoided in pregnancy."
            )
        )

        "hepatic_dose_adjustment" -> listOf(
            McqQuestion(
                question = "Mr. Ahmed is a 58-year-old male with hemochromatosis-related advanced liver disease who presents with progressive fatigue, increasing leg swelling, abdominal distension, and daytime confusion. He has no history of alcohol use. On examination, he has marked scleral icterus, moderate ascites, bilateral pedal edema, and asterixis is present. He is disoriented to time. Labs reveal: Albumin: 2.3 g/dL, Total Bilirubin: 3.5 mg/dL, INR: 2.1. Current medicines include Deferasirox 40 mg/kg PO daily, Paracetamol 1 g PO QID, Sertraline 100 mg PO BID, Enoxaparin 60 mg SC BID, and Metoprolol 100 mg PO TID. Q1. Should deferasirox be continued at the same dose in Child-Pugh C cirrhosis?",
                options = listOf(
                    "Continue at 40 mg/kg/day with ferritin and LFT monitoring",
                    "Reduce to 20 mg/kg/day due to impaired hepatic metabolism",
                    "Avoid use in severe hepatic impairment.",
                    "Consider switching to deferiprone or deferoxamine if chelation still required"
                ),
                correctIndex = 2,
                explanation = "Deferasirox should be avoided in severe hepatic impairment such as Child-Pugh C cirrhosis."
            ),
            McqQuestion(
                question = "Mr. Ahmed is a 58-year-old male with hemochromatosis-related advanced liver disease who presents with progressive fatigue, increasing leg swelling, abdominal distension, and daytime confusion. He has no history of alcohol use. On examination, he has marked scleral icterus, moderate ascites, bilateral pedal edema, and asterixis is present. He is disoriented to time. Labs reveal: Albumin: 2.3 g/dL, Total Bilirubin: 3.5 mg/dL, INR: 2.1. Current medicines include Deferasirox 40 mg/kg PO daily, Paracetamol 1 g PO QID, Sertraline 100 mg PO BID, Enoxaparin 60 mg SC BID, and Metoprolol 100 mg PO TID. Q2. Is paracetamol 4 g/day safe in a patient with decompensated cirrhosis?",
                options = listOf(
                    "4 g/day is acceptable with normal renal function and short duration",
                    "2 g/day maximum due to risk of hepatotoxicity in cirrhosis",
                    "Up to 3 g/day may be acceptable if no alcohol use and duration <7 days",
                    "Paracetamol should generally be avoided; NSAIDs are safer for pain control"
                ),
                correctIndex = 1,
                explanation = "In decompensated cirrhosis, paracetamol is usually limited to a maximum of 2 g/day."
            ),
            McqQuestion(
                question = "Mr. Ahmed is a 58-year-old male with hemochromatosis-related advanced liver disease who presents with progressive fatigue, increasing leg swelling, abdominal distension, and daytime confusion. He has no history of alcohol use. On examination, he has marked scleral icterus, moderate ascites, bilateral pedal edema, and asterixis is present. He is disoriented to time. Labs reveal: Albumin: 2.3 g/dL, Total Bilirubin: 3.5 mg/dL, INR: 2.1. Current medicines include Deferasirox 40 mg/kg PO daily, Paracetamol 1 g PO QID, Sertraline 100 mg PO BID, Enoxaparin 60 mg SC BID, and Metoprolol 100 mg PO TID. Q3. Does sertraline require dose adjustment in Child-Pugh C cirrhosis?",
                options = listOf(
                    "Continue at current dose; only minimal hepatic metabolism occurs",
                    "No adjustment needed unless dose exceeds 150-200 mg/day",
                    "Use not recommended; if continued, reduce by 50% with close monitoring",
                    "Consider switching to fluoxetine or mirtazapine as alternatives in cirrhosis"
                ),
                correctIndex = 2,
                explanation = "Sertraline use is not recommended in severe hepatic impairment; if continued, dose reduction and close monitoring are needed."
            ),
            McqQuestion(
                question = "Mr. Ahmed is a 58-year-old male with hemochromatosis-related advanced liver disease who presents with progressive fatigue, increasing leg swelling, abdominal distension, and daytime confusion. He has no history of alcohol use. On examination, he has marked scleral icterus, moderate ascites, bilateral pedal edema, and asterixis is present. He is disoriented to time. Labs reveal: Albumin: 2.3 g/dL, Total Bilirubin: 3.5 mg/dL, INR: 2.1. Current medicines include Deferasirox 40 mg/kg PO daily, Paracetamol 1 g PO QID, Sertraline 100 mg PO BID, Enoxaparin 60 mg SC BID, and Metoprolol 100 mg PO TID. Q4. Is enoxaparin safe and effective in advanced liver disease with coagulopathy?",
                options = listOf(
                    "Contraindicated in all patients with elevated INR or thrombocytopenia",
                    "Standard prophylactic dosing may be used cautiously, as INR is not reliable for bleeding risk",
                    "Dose escalation is often required to overcome pseudo-coagulopathy of cirrhosis"
                ),
                correctIndex = 1,
                explanation = "INR alone does not reliably predict bleeding risk in cirrhosis; prophylactic enoxaparin may be used cautiously when appropriate."
            ),
            McqQuestion(
                question = "Mr. Ahmed is a 58-year-old male with hemochromatosis-related advanced liver disease who presents with progressive fatigue, increasing leg swelling, abdominal distension, and daytime confusion. He has no history of alcohol use. On examination, he has marked scleral icterus, moderate ascites, bilateral pedal edema, and asterixis is present. He is disoriented to time. Labs reveal: Albumin: 2.3 g/dL, Total Bilirubin: 3.5 mg/dL, INR: 2.1. Current medicines include Deferasirox 40 mg/kg PO daily, Paracetamol 1 g PO QID, Sertraline 100 mg PO BID, Enoxaparin 60 mg SC BID, and Metoprolol 100 mg PO TID. Q5. Should metoprolol dosing be modified in the setting of impaired hepatic metabolism?",
                options = listOf(
                    "No adjustment required; clearance is unchanged in cirrhosis",
                    "Avoid all non-selective beta-blockers in decompensated cirrhosis",
                    "Initiate at a lower dose with gradual titration due to reduced hepatic clearance",
                    "Prefer atenolol or bisoprolol as they undergo renal elimination and less hepatic metabolism"
                ),
                correctIndex = 2,
                explanation = "Metoprolol undergoes hepatic metabolism, so lower starting doses and gradual titration are recommended in hepatic impairment."
            )
        )

        "high_alert_medications" -> listOf(
            McqQuestion(
                question = "A nurse conveys a verbal order for Angised as Ansaid. The pharmacist reviews the diagnosis (angina) and catches the error. This scenario best illustrates:",
                options = listOf(
                    "A prescribing error caused by incomplete patient history review",
                    "A transcription error due to illegible handwriting on the chart",
                    "A Look-Alike Sound-Alike (LASA) error during verbal communication",
                    "A dispensing error caused by similar packaging of the two drugs"
                ),
                correctIndex = 2,
                explanation = "This is a LASA error caused by similar-sounding drug names during verbal communication."
            ),
            McqQuestion(
                question = "A 56 kg patient in the Medical ICU is initiated on heparin at 80 units/kg for DVT prophylaxis (maximum cap: 10,000 units). What dose should the pharmacist verify?",
                options = listOf(
                    "4,800 units -- rounded to nearest hundred for pump programming",
                    "4,480 units -- exact weight-based calculation within the cap",
                    "5,600 units -- using an adjusted body weight formula",
                    "10,000 units -- the cap applies because DVT prophylaxis always requires maximum dosing"
                ),
                correctIndex = 1,
                explanation = "80 units/kg x 56 kg = 4,480 units, which is within the maximum cap."
            ),
            McqQuestion(
                question = "While verifying a chemotherapy order for vincristine, which risk most critically justifies its classification as a high-alert medication?",
                options = listOf(
                    "Its vesicant properties require central line administration to prevent tissue necrosis",
                    "Its narrow therapeutic index requires therapeutic drug monitoring after every cycle",
                    "Administration via the wrong route (e.g., intrathecal) has resulted in fatal ascending paralysis",
                    "It requires weight-adjusted dosing capped at 2 mg, making calculation errors likely in obese patients"
                ),
                correctIndex = 2,
                explanation = "Vincristine is high-alert because wrong-route intrathecal administration can be fatal."
            ),
            McqQuestion(
                question = "You receive a stat order containing ketamine, insulin, and atracurium. What makes this combination significant from an institutional safety standpoint?",
                options = listOf(
                    "All three require mandatory cold chain storage and expire rapidly at room temperature",
                    "All three have synergistic CNS depression requiring a single reversal agent",
                    "All three are classified as high-alert medications per ISMP's published list -- ketamine as a sedative/anesthetic, insulin as a hypoglycemic agent, and atracurium as a neuromuscular blocker",
                    "All three are DEA Schedule II controlled substances requiring dual-signature documentation"
                ),
                correctIndex = 2,
                explanation = "Ketamine, insulin, and atracurium are all high-alert medication categories."
            ),
            McqQuestion(
                question = "According to ISMP safeguarding principles, abbreviating heparin units as U has led to which specific type of error?",
                options = listOf(
                    "Confusion with the abbreviation IU resulting in wrong route of administration",
                    "Ten-fold overdoses because U is misread as an additional zero",
                    "Omission errors because U is mistaken for units withheld in nursing documentation",
                    "Concentration errors because U is confused with mL on infusion pump settings"
                ),
                correctIndex = 1,
                explanation = "The abbreviation U can be misread as a zero, causing ten-fold overdose errors."
            ),
            McqQuestion(
                question = "Mix-ups between hydromorphone and morphine are common. Which combination of strategies best differentiates these two drugs according to ISMP?",
                options = listOf(
                    "Prescribe both by brand name only and restrict hydromorphone to palliative care units",
                    "Use TALL-man lettering (HYDROmorphone), stock in different strengths/forms, and store in physically separate locations",
                    "Color-code the vials identically but apply different barcode labels for scanner differentiation",
                    "Replace hydromorphone entirely with fentanyl equivalents on the hospital formulary"
                ),
                correctIndex = 1,
                explanation = "Tall-man lettering, different strengths/forms, and separate storage help prevent hydromorphone-morphine mix-ups."
            ),
            McqQuestion(
                question = "The abbreviations MSO4 and MgSO4 have caused life-threatening mix-ups. What specific harm can result?",
                options = listOf(
                    "Administration of magnesium instead of morphine, causing prolonged QT interval and fatal torsades de pointes",
                    "Administration of morphine when magnesium was intended, leading to respiratory depression and potential arrest",
                    "Co-precipitation of both drugs in the IV line causing pulmonary microemboli",
                    "Magnesium-induced potentiation of morphine's analgesic effects causing irreversible CNS sedation"
                ),
                correctIndex = 1,
                explanation = "Confusing MgSO4 with MSO4 may result in morphine administration instead of magnesium, causing respiratory depression."
            ),
            McqQuestion(
                question = "A pharmacist receives a chemotherapy prescription for a pediatric patient requiring cyclophosphamide IV. Which approach best minimizes the risk of a high-alert medication error during verification and preparation?",
                options = listOf(
                    "Verify the dose against the patient's body surface area, confirm renal function, and have a second pharmacist independently recalculate before preparing under a biosafety hood with full PPE",
                    "Verify the dose against patient weight only, prepare in a laminar airflow hood, and have the oncology nurse confirm the final volume at bedside",
                    "Use a standard adult dose reduced by 50% for pediatric patients, prepare in the pharmacy clean room, and attach a Caution: Chemotherapy label before dispensing",
                    "Cross-reference the dose with the prescriber's previous chemotherapy orders for this patient and prepare immediately to avoid treatment delays"
                ),
                correctIndex = 0,
                explanation = "Chemotherapy verification should include BSA checking, renal function review, independent double-checking, and safe preparation with PPE."
            ),
            McqQuestion(
                question = "Which of the following is an example of a forcing function as described in the ISMP high-alert medication safety framework?",
                options = listOf(
                    "Placing HIGH ALERT auxiliary stickers on neuromuscular blocker vials in the pharmacy",
                    "Conducting a failure mode and effects analysis (FMEA) before adding a new high-alert drug to formulary",
                    "Designing enteral feeding tube connectors that physically cannot attach to intravenous access ports",
                    "Requiring pharmacist verification before any high-alert medication is released from automated dispensing cabinets"
                ),
                correctIndex = 2,
                explanation = "A forcing function physically prevents an incorrect action, such as incompatible enteral and IV connectors."
            ),
            McqQuestion(
                question = "Neuromuscular blocking agents such as rocuronium are dispensed to the OR. Which storage and handling strategy is recommended by ISMP to prevent accidental administration?",
                options = listOf(
                    "Store them in the general anesthesia tray alongside sedatives for rapid OR access during emergencies",
                    "Sequester them from all other medications in a separate locked container with prominent warning labels and restricted access",
                    "Refrigerate them in the pharmacy's general fridge with clear shelf separation from insulin and vaccines",
                    "Store them in the automated dispensing cabinet under the generic drug name with a standard barcode scan requirement"
                ),
                correctIndex = 1,
                explanation = "Neuromuscular blockers should be stored separately with warning labels and restricted access."
            )
        )

        "iv_to_oral_switch" -> listOf(
            McqQuestion(
                question = "A patient receiving IV metronidazole 500 mg TID is now clinically stable and tolerating oral intake. The oral bioavailability of metronidazole is 100%. What is the correct oral dose conversion?",
                options = listOf(
                    "250 mg TID -- reduce by 50% to account for first-pass hepatic metabolism",
                    "500 mg TID -- 1:1 conversion since bioavailability is 100%",
                    "750 mg TID -- increase by 50% to compensate for variable GI absorption",
                    "500 mg BID -- same total daily dose but reduce frequency for better oral compliance"
                ),
                correctIndex = 1,
                explanation = "Metronidazole has 100% oral bioavailability, so IV to oral conversion is 1:1."
            ),
            McqQuestion(
                question = "A patient meets all ABCD criteria for IV to oral switch. However, they were just started on continuous nasogastric suction 2 hours ago for a bowel obstruction. What is the most appropriate action?",
                options = listOf(
                    "Switch to oral and administer via the NG tube since the criteria are met",
                    "Continue IV therapy until GI function is restored and oral absorption can be ensured",
                    "Switch to oral and hold the NG suction for 1 hour after each dose to allow absorption",
                    "Switch to oral rectal suppository formulation to bypass the GI obstruction entirely"
                ),
                correctIndex = 1,
                explanation = "Oral therapy should be delayed when GI absorption is unreliable due to bowel obstruction or NG suction."
            ),
            McqQuestion(
                question = "A patient on IV omeprazole is now tolerating a regular diet, is afebrile, and clinically stable. The pharmacist recommends switching to oral omeprazole. The physician declines, stating IV ensures 100% drug delivery. What is the BEST pharmacist rebuttal?",
                options = listOf(
                    "The physician is correct -- IV bypasses first-pass metabolism and always delivers more active drug to target tissues",
                    "Omeprazole has 100% oral bioavailability, so the oral route achieves equivalent systemic exposure in a patient with a functioning GI tract",
                    "The oral dose should be doubled to match IV delivery since some drug is always lost during GI absorption",
                    "Oral omeprazole actually has superior bioavailability compared to IV due to enterohepatic recirculation"
                ),
                correctIndex = 1,
                explanation = "With good oral bioavailability and a functioning GI tract, oral therapy can provide equivalent systemic exposure."
            ),
            McqQuestion(
                question = "A patient with MRSA osteomyelitis is on IV vancomycin, is afebrile for 48 hours, tolerating diet, and has a normalizing WBC. The intern requests an IV to oral switch. What is the BEST response?",
                options = listOf(
                    "Switch to oral vancomycin at the same dose since the patient meets all clinical switch criteria",
                    "Switch to oral linezolid -- it has 100% bioavailability and MRSA coverage, making it a suitable step-down",
                    "Decline the switch -- osteomyelitis is a deep-seated infection requiring prolonged IV therapy per guidelines",
                    "Switch to oral clindamycin -- it has excellent bone penetration and is appropriate for step-down therapy"
                ),
                correctIndex = 2,
                explanation = "Osteomyelitis is a deep-seated infection and often requires prolonged IV therapy, so routine switch is inappropriate."
            ),
            McqQuestion(
                question = "A patient is receiving IV ondansetron 4 mg TID and is now eating well. Ondansetron has 100% oral bioavailability. The nurse argues IV should continue because it works faster. What is the MOST accurate pharmacist response?",
                options = listOf(
                    "The nurse is correct -- IV always produces faster and more reliable therapeutic effect regardless of bioavailability",
                    "With 100% bioavailability, the oral route achieves equivalent systemic exposure; speed of onset is not clinically relevant in a stable, eating patient",
                    "The oral dose should be increased to 8 mg TID to compensate for the slower onset of action compared to IV",
                    "Both routes are equivalent in speed of onset, so IV should be continued for convenience since the line is already in place"
                ),
                correctIndex = 1,
                explanation = "In a stable patient tolerating oral intake, 100% bioavailability supports oral conversion without dose change."
            ),
            McqQuestion(
                question = "A patient on IV levetiracetam for seizure prophylaxis is now stable, tolerating oral diet, and seizure-free for 72 hours. Levetiracetam has 100% oral bioavailability. The physician wants to continue IV just to be safe. Which is the STRONGEST argument for switching?",
                options = listOf(
                    "Oral levetiracetam has superior CNS penetration compared to the IV formulation",
                    "Continuing unnecessary IV access increases the risk of catheter-related bloodstream infections, phlebitis, and hospital-acquired infections like C. difficile",
                    "IV levetiracetam is pharmacologically contraindicated beyond 72 hours of continuous use",
                    "Switching to oral will achieve higher and more sustained serum drug levels due to slower absorption kinetics"
                ),
                correctIndex = 1,
                explanation = "Unnecessary IV therapy increases line-related complications and infection risk."
            ),
            McqQuestion(
                question = "According to the ABCD criteria, which specific vital sign parameters must be met under criterion C (clinically improving) before an IV to oral switch is appropriate?",
                options = listOf(
                    "HR <100/min in past 24 hours, BP stable for 12 hours, RR <24/min in past 12 hours",
                    "HR <90/min in past 12 hours, BP stable in past 24 hours, RR <20/min in past 24 hours",
                    "HR <80/min in past 48 hours, BP >120/80 consistently, RR <16/min in past 24 hours",
                    "HR <90/min in past 24 hours, BP stable in past 12 hours, RR <20/min in past 12 hours"
                ),
                correctIndex = 1,
                explanation = "Criterion C requires clinical improvement with stable vital signs such as HR <90/min, stable BP, and RR <20/min."
            ),
            McqQuestion(
                question = "A patient on IV linezolid 600 mg BID for a VRE wound infection is now afebrile 36 hours, tolerating diet, WBC normalizing, wound improving. Linezolid has 100% oral bioavailability. The physician writes the oral order as 300 mg BID, reasoning oral doses should be lower. What is the pharmacist's BEST intervention?",
                options = listOf(
                    "Accept the order -- oral doses are routinely reduced by 50% to account for differences in distribution between IV and oral routes",
                    "Correct the order to 600 mg BID -- with 100% bioavailability, the oral dose should be equivalent to the IV dose with no reduction needed",
                    "Recommend 600 mg TID orally -- the frequency should be increased to compensate for slower oral absorption and lower peak levels",
                    "Recommend 450 mg BID as a compromise -- partial dose reduction accounts for the transition period between IV and oral steady states"
                ),
                correctIndex = 1,
                explanation = "Linezolid has 100% oral bioavailability, so the oral dose should match the IV dose."
            ),
            McqQuestion(
                question = "A septic patient in the ICU has BP 78/42 mmHg on vasopressors, HR 118/min, and lactate 5.2 mmol/L. They are on IV meropenem. A colleague suggests switching to oral levofloxacin since cultures show a susceptible organism. Why is this switch inappropriate?",
                options = listOf(
                    "Levofloxacin has insufficient spectrum of activity compared to meropenem regardless of susceptibility results",
                    "The patient is hemodynamically unstable with likely compromised splanchnic perfusion, making GI absorption unpredictable and unreliable",
                    "Oral antibiotics are pharmacologically contraindicated in all ICU patients regardless of hemodynamic status",
                    "The switch is appropriate -- levofloxacin has 99% bioavailability, so culture-guided oral step-down should proceed immediately"
                ),
                correctIndex = 1,
                explanation = "Hemodynamic instability can make GI absorption unreliable, so oral switch is inappropriate."
            ),
            McqQuestion(
                question = "A patient has been afebrile for 30 hours with temperature readings between 36.2-37.8C. According to criterion A of the ABCD switch criteria, is this patient eligible?",
                options = listOf(
                    "Yes -- they have been afebrile (36-38C) for more than 24 hours, meeting criterion A",
                    "No -- afebrile status requires a minimum of 48 consecutive hours without any temperature above 37C",
                    "No -- 37.8C is considered low-grade fever and does not qualify as afebrile under the ABCD criteria",
                    "Yes -- but only if all recorded temperatures were strictly below 37.5C during the 30-hour window"
                ),
                correctIndex = 0,
                explanation = "Afebrile for more than 24 hours within 36-38C meets criterion A."
            )
        )

        "lab_interpretation" -> listOf(
            McqQuestion(
                question = "In a patient with persistent hypokalemia (K+ < 3.5 mEq/L) that is refractory to potassium supplementation, which physiological relationship must the pharmacist consider?",
                options = listOf(
                    "High sodium levels are preventing potassium absorption.",
                    "High chloride levels are causing metabolic alkalosis.",
                    "Potassium is difficult to correct while magnesium remains low.",
                    "Bicarbonate must be corrected first to stabilize the pH."
                ),
                correctIndex = 2,
                explanation = "Hypomagnesemia can make hypokalemia difficult to correct."
            ),
            McqQuestion(
                question = "When evaluating Liver Function Tests (LFTs), which AST/ALT ratio and trend is most indicative of alcoholic liver damage or cirrhosis?",
                options = listOf(
                    "AST/ALT ratio < 1 (ALT is higher than AST).",
                    "AST/ALT ratio = 1 (ALT and AST are equal).",
                    "AST/ALT ratio > 1 (AST is higher than ALT), typically >= 2:1.",
                    "A 2-3 times rise in ALT from baseline only."
                ),
                correctIndex = 2,
                explanation = "An AST/ALT ratio greater than 1, especially around 2:1, suggests alcoholic liver damage or cirrhosis."
            ),
            McqQuestion(
                question = "A patient's fasting blood glucose is 118 mg/dL. According to the diagnostic criteria for glucose tolerance, what is the pharmacist's interpretation?",
                options = listOf(
                    "The patient is within the optimal fasting range.",
                    "The patient has Impaired Fasting Glucose (IFG), suggestive of prediabetes.",
                    "The patient meets the criteria for a diagnosis of Diabetes Mellitus.",
                    "The patient is experiencing hypoglycemia and requires immediate blood sugar support."
                ),
                correctIndex = 1,
                explanation = "A fasting blood glucose of 118 mg/dL falls in the impaired fasting glucose range."
            ),
            McqQuestion(
                question = "Why is Procalcitonin (PCT) preferred over C-Reactive Protein (CRP) for the Antibiotic Stewardship role in a clinical setting?",
                options = listOf(
                    "CRP is only elevated in fungal infections.",
                    "PCT has a much shorter half-life of only 1 hour.",
                    "PCT elevations are selective for bacterial infections and do not rise significantly in viral infections.",
                    "CRP levels cannot be used to diagnose sepsis."
                ),
                correctIndex = 2,
                explanation = "PCT is more selective for bacterial infection and is useful in antibiotic stewardship."
            ),
            McqQuestion(
                question = "For a patient with a mechanical mitral valve replacement, what is the target INR range that a pharmacist must verify before dispensing Warfarin?",
                options = listOf(
                    "1.1 or below.",
                    "2.0 - 3.0.",
                    "2.5 - 3.5.",
                    "Above 3.5 to prevent thrombosis."
                ),
                correctIndex = 2,
                explanation = "Mechanical mitral valve replacement usually requires a target INR of 2.5-3.5."
            ),
            McqQuestion(
                question = "A pharmacist notes an isolated decrease in Blood Urea Nitrogen (BUN) while the creatinine remains normal. Which clinical condition is most likely?",
                options = listOf(
                    "Acute renal failure.",
                    "Gastrointestinal bleeding.",
                    "Liver failure, due to the inability of the liver to synthesize urea.",
                    "Dehydration leading to dry azotemia."
                ),
                correctIndex = 2,
                explanation = "Low BUN with normal creatinine may suggest impaired hepatic urea synthesis, such as liver failure."
            )
        )

        "narcotic_controlled_medications" -> listOf(
            McqQuestion(
                question = "A house officer prescribes Diazepam 10 mg TDS x 30 days. No consultant countersignature, no hospital stamp, no patient ID. The patient warns that abrupt stoppage causes seizures. The pharmacist should:",
                options = listOf(
                    "Dispense a 5-day emergency taper to prevent seizures, record in CD register, and demand corrected prescription within 72 hours",
                    "Refuse to dispense due to missing consultant authorization, stamp, and ID; counsel the patient on seizure risk and urgently refer back for a corrected prescription.",
                    "Verify the house officer's registration number by phone and dispense full supply, recording verbal confirmation as substitute documentation",
                    "Dispense 7 days under emergency provisions with verbal ID and fax the hospital for a corrected prescription"
                ),
                correctIndex = 1,
                explanation = "Controlled medicine prescriptions must meet legal requirements; missing authorization and patient details mean dispensing should be refused with urgent referral."
            ),
            McqQuestion(
                question = "A construction worker requests Tramadol 50 mg x 60 capsules without prescription. He shows an old empty box with his name on it, appears sweaty and trembling, and his colleague confirms 3 years of use. The pharmacist should:",
                options = listOf(
                    "Dispense 5 capsules as humanitarian dose, photograph the old box as documentation, insist on prescription within 24 hours",
                    "Refuse to dispense without a valid prescription, explain Tramadol's controlled status, counsel on withdrawal, and refer to the nearest healthcare facility.",
                    "Dispense 10 capsules with CNIC recording and colleague as witness, since observable withdrawal constitutes clinical equivalence to a prescription",
                    "Offer Tapentadol 50 mg as an unscheduled alternative dispensable at pharmacist discretion with lower dependence risk"
                ),
                correctIndex = 1,
                explanation = "Tramadol should not be dispensed without a valid prescription; withdrawal risk should be addressed by referral."
            ),
            McqQuestion(
                question = "A valid prescription states Buprenorphine 8 mg sublingual daily x 14 days -- daily supervised dispensing. The patient begs for the full 14-day supply because daily visits will cost him his job (employer letter provided). The pharmacist should:",
                options = listOf(
                    "Dispense all 14 days, noting the employer letter in the narcotic register as socioeconomic hardship justification",
                    "Dispense 3 days as a compromise, enter each day separately, and contact the specialist about modifying frequency",
                    "Dispense daily supervised doses as prescribed, record each transaction in the narcotic register, and contact the addiction specialist to formally discuss whether the regimen can be modified.",
                    "Transfer the prescription to a pharmacy near the patient's workplace for daily supervised dispensing there"
                ),
                correctIndex = 2,
                explanation = "The pharmacist must follow supervised dispensing instructions and contact the specialist if modification is needed."
            ),
            McqQuestion(
                question = "An inspector finds your Alprazolam and Clonazepam in a locked cabinet with a dedicated register and daily balances. However, the last independent audit was 6 weeks ago, and a 4-tablet Alprazolam discrepancy is noted only in pencil in the margin. This is:",
                options = listOf(
                    "Fully compliant -- 6-week gap falls within the quarterly audit cycle, and pencil notations are acceptable interim documentation",
                    "Partially compliant -- audit gap exceeds 30 days and discrepancies must be reported to regulators within 48 hours",
                    "Non-compliant -- audits must be monthly, and discrepancies must be documented in ink with investigation notes, corrective actions, and pharmacist sign-off; pencil marginal notes are unacceptable.",
                    "Non-compliant -- benzodiazepines must be stored in the same safe as narcotics under a single unified register"
                ),
                correctIndex = 2,
                explanation = "Controlled medicine discrepancies must be properly documented and investigated; pencil margin notes are not acceptable."
            ),
            McqQuestion(
                question = "A patient in anaphylactic shock needs Hydrocortisone Sodium Succinate 100 mg IV urgently. The Schedule E requisition form is incomplete. No floor stock available. SpO2 is dropping. The pharmacist should:",
                options = listOf(
                    "Withhold until the form is completed -- the physician should use epinephrine first while completing paperwork",
                    "Dispense immediately without documentation; emergencies permanently waive all Schedule E requirements",
                    "Dispense only after obtaining a verbal order witnessed by two nurses, with the form completed within 30 days",
                    "Dispense immediately, update the Schedule E register with all details within the mandated time frame (typically 24 hours), and obtain retrospective signed documentation."
                ),
                correctIndex = 3,
                explanation = "In an emergency, treatment should not be delayed; documentation must be completed retrospectively within the required time."
            ),
            McqQuestion(
                question = "A GP issues a private prescription for Oxycodone MR 20 mg BD x 28 days (Schedule 2). It is computer-generated with only the signature handwritten. Quantity states 56 tablets in figures only. Plain white letterhead is used instead of FP10PCD. The patient's daughter collects. The legal defects are:",
                options = listOf(
                    "Only one defect -- missing words-and-figures; computer body is acceptable, no standardised form needed for private CDs, and representatives may collect with ID",
                    "Two defects -- words-and-figures missing and standardised form required; but computer body is acceptable and daughter may collect after verification",
                    "Three defects -- Schedule 2 CDs require the prescriber's own handwriting (or compliant electronic prescribing), quantity in words and figures, and a standardised CD private prescription form; additionally the pharmacist must establish and record the collector's identity and relationship.",
                    "Four defects -- all of option C plus a GP cannot prescribe Schedule 2 CDs for cancer pain on private prescriptions without palliative care countersignature"
                ),
                correctIndex = 2,
                explanation = "Schedule 2 controlled drug prescriptions have strict legal requirements, including quantity in words and figures and proper documentation of collection."
            ),
            McqQuestion(
                question = "A Schedule 2 prescription for Morphine Sulfate MR 60 mg BD is dated 14 weeks ago. Prescriber address is in Switzerland. Quantity is a 90-day supply. Signature appears genuine. The legal barriers are:",
                options = listOf(
                    "One barrier only -- foreign prescriber address; the 13-week validity runs from dispensing date, and quantity limits are advisory",
                    "Two barriers -- exceeds 28-day validity and foreign address; but 90-day supply is permissible if clinically justified",
                    "Two barriers -- exceeds 13-week validity and foreign address; the 90-day quantity is not illegal but requires professional query",
                    "Three barriers -- exceeds 28-day validity for Schedule 2 CDs, prescriber address must be within the UK, and while no strict statutory maximum exists, 30 days is the recommended maximum requiring professional judgment; signature verification remains an ongoing obligation."
                ),
                correctIndex = 3,
                explanation = "The prescription exceeds Schedule 2 validity requirements, has an invalid prescriber address issue, and the long quantity requires professional concern."
            ),
            McqQuestion(
                question = "Your pharmacy must destroy expired stock: Quinalbarbitone 50 mg (Sch 2), Dihydrocodeine 30 mg (Sch 5), Phenobarbitone 30 mg (Sch 3), Temazepam 10 mg (Sch 3). The correct destruction requirements are:",
                options = listOf(
                    "All four require authorised person supervision since all are classified as controlled drugs",
                    "Only Quinalbarbitone requires authorised witness; the other three may be destroyed independently with register entries",
                    "Quinalbarbitone and Temazepam require authorised witness; Phenobarbitone is exempt as an antiepileptic; Dihydrocodeine may be destroyed without witness",
                    "Quinalbarbitone (Sch 2) requires destruction witnessed by an authorised person with CD register documentation; Temazepam and Phenobarbitone (Sch 3) should follow good practice but the legal authorised-witness requirement applies specifically to Schedule 2; Dihydrocodeine (Sch 5) has the least restrictive requirements."
                ),
                correctIndex = 3,
                explanation = "Schedule 2 CDs require witnessed destruction; Schedule 3 and 5 have less restrictive requirements, though good practice still applies."
            ),
            McqQuestion(
                question = "An NHS prescription for Phenobarbital 30 mg x 168 tablets is written for a 10-year-old epileptic child. Typed body, handwritten signature, quantity in figures only, no age stated, dated 2 months ago. The legal deficiencies are:",
                options = listOf(
                    "Three deficiencies -- missing age (under 12), quantity not in words and figures, and exceeds 28-day validity",
                    "Two deficiencies -- patient age must be stated for under-12s, and the prescription validity must be checked; however phenobarbital (Schedule 3) is exempt from handwriting and words-and-figures requirements applicable to Schedule 2.",
                    "Four deficiencies -- missing age, no words-and-figures, exceeds 28-day validity, and paediatric CDs require dual-clinician countersignature",
                    "One deficiency only -- the missing age; Schedule 3 is exempt from all other requirements and NHS prescriptions are valid for 6 months"
                ),
                correctIndex = 1,
                explanation = "For an under-12 patient, age must be stated; phenobarbital Schedule 3 has specific exemptions compared with Schedule 2 requirements."
            ),
            McqQuestion(
                question = "At 11 PM a patient calls saying she has run out of Clonazepam 2 mg, is experiencing tremors, and fears seizures. No prescription. Her regular pharmacy is closed. The pharmacist can:",
                options = listOf(
                    "Make an emergency supply for any CD if immediate need exists, treatment was previously prescribed, smallest quantity is supplied, and prescriber is contacted within 24 hours",
                    "Never make an emergency supply of any CD under any circumstance -- she must go to the emergency department",
                    "Make an emergency supply for Clonazepam (Schedule 3) -- but not Schedule 2 CDs -- provided there is immediate clinical need, treatment was previously prescribed, the smallest quantity is supplied (maximum 5 days for CDs), and full details are recorded in the POM register.",
                    "Make an emergency supply only if her own prescriber contacts the pharmacist directly -- patient-initiated emergency requests for CDs are categorically prohibited regardless of schedule"
                ),
                correctIndex = 2,
                explanation = "Emergency supply may be possible for clonazepam Schedule 3 under strict conditions and proper records."
            )
        )

        "parts_of_prescription" -> listOf(
            McqQuestion(
                question = "A prescriber writes a prescription with the following: Patient name, age, Rx symbol, drug name, dose, frequency, route, and signs it with a signature. There is NO date written anywhere on the prescription. What is the implication?",
                options = listOf(
                    "The prescription is still valid because the signature authenticates it",
                    "The prescription is invalid -- the date is essential to determine validity, ensure patient follow-up, and authorize refills",
                    "The pharmacist can add today's date and dispense it",
                    "The date is only required for controlled substance prescriptions, so this is acceptable for regular drugs"
                ),
                correctIndex = 1,
                explanation = "The date is essential for prescription validity, follow-up, and refill authorization."
            ),
            McqQuestion(
                question = "A doctor prescribes: Atenolol 100 mg QD. According to the list of dangerous abbreviations, what is the risk with QD?",
                options = listOf(
                    "QD is universally accepted and poses no risk",
                    "QD can be mistaken for QOD (every other day) or the period after Q can be mistaken for I, leading to a dosing error -- the prescriber should write daily",
                    "QD is only dangerous when used with controlled substances",
                    "QD is risky only because it is a Latin term and patients won't understand it"
                ),
                correctIndex = 1,
                explanation = "QD is unsafe because it can be confused with QOD or misread, so daily should be written clearly."
            ),
            McqQuestion(
                question = "A prescription for a pediatric patient includes the drug name, dose of 125 mg/5 mL -- one teaspoon TDS, and the prescriber's signature. Which CRITICAL element is missing that is especially important in pediatric prescriptions?",
                options = listOf(
                    "The inscription",
                    "The patient's weight for weight-based dose verification",
                    "The Rx symbol",
                    "The brand vs. generic preference"
                ),
                correctIndex = 1,
                explanation = "Pediatric prescriptions should include weight to verify weight-based dosing."
            ),
            McqQuestion(
                question = "A drug has a pregnancy category C. What does this mean for prescribing?",
                options = listOf(
                    "The drug is completely safe in pregnancy",
                    "The drug should be avoided entirely in pregnancy",
                    "The drug may be used only if the potential benefit justifies the potential risk",
                    "The drug is safe in the first trimester only"
                ),
                correctIndex = 2,
                explanation = "Pregnancy category C means use only when potential benefit justifies potential risk."
            ),
            McqQuestion(
                question = "A pharmacist receives a prescription reading Morphine SO4 5.0 mg PRN. How many dangerous practices exist in this order?",
                options = listOf(
                    "One -- the abbreviation SO4",
                    "Two -- the abbreviation SO4 and the trailing zero 5.0",
                    "Three -- SO4, the trailing zero, and PRN without a maximum dose",
                    "None -- this is standard shorthand"
                ),
                correctIndex = 2,
                explanation = "The dangerous practices are SO4 abbreviation, trailing zero, and PRN without a maximum dose."
            ),
            McqQuestion(
                question = "Which drug interaction risk rating requires the prescriber to consider modifying the therapy?",
                options = listOf(
                    "Risk Rating C",
                    "Risk Rating B",
                    "Risk Rating D",
                    "Risk Rating X"
                ),
                correctIndex = 2,
                explanation = "Risk Rating D means therapy modification should be considered."
            ),
            McqQuestion(
                question = "A prescription reads: .8 mL of Cephalexin suspension. What is the critical error?",
                options = listOf(
                    "The drug name should be abbreviated",
                    "A leading zero is missing before the decimal point, which could cause a 10-fold dosing error",
                    "Milliliters should be written as cc",
                    "Suspension volumes should only be written in teaspoons"
                ),
                correctIndex = 1,
                explanation = "A leading zero should be written before decimals to prevent 10-fold dosing errors."
            ),
            McqQuestion(
                question = "The Subscription part of a prescription contains:",
                options = listOf(
                    "The patient's demographic information and the Rx symbol",
                    "The composition and dosage of the medicine",
                    "Directions to the pharmacist on how to dispense the dosage form",
                    "The prescriber's signature and registration number"
                ),
                correctIndex = 2,
                explanation = "The subscription gives directions to the pharmacist for dispensing or compounding."
            ),
            McqQuestion(
                question = "A nurse receives a verbal order that reads: Inj. MgSO4 2 g IV stat. According to dangerous abbreviation guidelines, what should the prescriber have written instead?",
                options = listOf(
                    "Inj. MgSO4 2000 mg IV stat",
                    "Inj. Magnesium Sulfate 2 g IV stat",
                    "Inj. MgSO4 2.0 g IV stat",
                    "Inj. MS 2 g IV immediately"
                ),
                correctIndex = 1,
                explanation = "Magnesium sulfate should be written fully to avoid confusion with morphine sulfate."
            ),
            McqQuestion(
                question = "A pharmacist receives a prescription dated 14 months ago with Refill x 3 written on it. Two refills have been used. The pharmacist should:",
                options = listOf(
                    "Dispense the third refill since it was authorized by the prescriber",
                    "Refuse to dispense because refill authorizations are only valid for 1 year from the prescription date",
                    "Dispense but reduce the quantity by half",
                    "Contact the patient's insurance company for approval"
                ),
                correctIndex = 1,
                explanation = "Refill authorization is only valid for 1 year from the prescription date."
            )
        )

        "pediatric_dose_adjustment" -> listOf(
            McqQuestion(
                question = "A child weighing 28.6 kg is prescribed Amoxil. The safe dosage is 50 mg/kg/day every 8 hours. What is the safe dose per dose?",
                options = listOf("478.2 mg/dose", "472.4 mg/dose", "476.6 mg/dose", "481.3 mg/dose"),
                correctIndex = 2,
                explanation = "50 mg/kg/day x 28.6 kg = 1430 mg/day. Divided every 8 hours means 3 doses/day, so 1430 / 3 = 476.6 mg/dose."
            ),
            McqQuestion(
                question = "A child weighing 32.7 kg is prescribed 300 mg of Zmax once daily. The safe dose range is 10-12 mg/kg/day. Is this a safe dose?",
                options = listOf(
                    "No, a safe dose is 330.2-396.8 mg/day.",
                    "No, a safe dose is 327.0-392.4 mg/day.",
                    "No, a safe dose is 321.5-385.8 mg/day.",
                    "Yes, this is a safe dose."
                ),
                correctIndex = 1,
                explanation = "Safe dose range = 32.7 x 10-12 mg/kg/day = 327.0-392.4 mg/day. The prescribed 300 mg is below the safe range."
            ),
            McqQuestion(
                question = "A child weighing 14.1 kg is prescribed Cefazolin 350 mg IV every 4 hours. The safe dosage range is 25-75 mg/kg/day. Is this a safe dose?",
                options = listOf(
                    "No, a safe dose would be 56.2-172.8 mg/dose.",
                    "No, a safe dose would be 61.3-179.4 mg/dose.",
                    "No, a safe dose would be 58.75-176.25 mg/dose.",
                    "Yes, this is a safe dose."
                ),
                correctIndex = 2,
                explanation = "Safe daily dose = 14.1 x 25-75 = 352.5-1057.5 mg/day. Given every 4 hours means 6 doses/day, so safe dose is 58.75-176.25 mg/dose."
            ),
            McqQuestion(
                question = "A child weighing 35.5 kg requires a Dopamine IV drip. The safe dosage range is 5-20 mcg/kg/min. What is the safe dosage range for this child?",
                options = listOf("175.2-704.8 mcg/min", "180.1-714.6 mcg/min", "177.5-710.0 mcg/min", "173.8-706.2 mcg/min"),
                correctIndex = 2,
                explanation = "Safe range = 35.5 x 5-20 mcg/kg/min = 177.5-710.0 mcg/min."
            ),
            McqQuestion(
                question = "A child weighing 23.6 kg has a fever and is prescribed Tylenol. The safe dose range is 10-15 mg/kg every 6 hours. What is the maximum safe dose this child can receive per day?",
                options = listOf("1,420.4 mg/day", "1,410.8 mg/day", "1,416.0 mg/day", "1,422.6 mg/day"),
                correctIndex = 2,
                explanation = "Maximum dose per dose = 23.6 x 15 = 354 mg. Every 6 hours means 4 doses/day, so 354 x 4 = 1416 mg/day."
            ),
            McqQuestion(
                question = "A child weighing 7.3 kg is prescribed Digoxin 0.92 mg daily. The safe dosage is 8-12 mcg/kg/day. Is this a safe dose?",
                options = listOf(
                    "No, a safe dose is 0.0612-0.0904 mg/day.",
                    "No, a safe dose is 0.0584-0.0876 mg/day.",
                    "No, a safe dose is 0.0548-0.0832 mg/day.",
                    "Yes, this is a safe dose."
                ),
                correctIndex = 1,
                explanation = "Safe dose = 7.3 x 8-12 mcg/kg/day = 58.4-87.6 mcg/day, or 0.0584-0.0876 mg/day. The prescribed 0.92 mg is too high."
            )
        )

        "renal_dose_adjustment" -> listOf(
            McqQuestion(
                question = "Patient Information: 66-year-old male, weight 78 kg, height 172 cm, serum creatinine 2.1 mg/dL. Diagnosis: diabetic foot ulcer with cellulitis. Prescription: Piperacillin-Tazobactam 4.5 g IV q6h. A pharmacist reviews this prescription. Considering the patient's renal function, what is the most appropriate recommendation for Piperacillin-Tazobactam?",
                options = listOf(
                    "Continue Piperacillin-Tazobactam 4.5 g IV q6h without changes",
                    "Switch Piperacillin-Tazobactam to 4.5 g IV q12h",
                    "Adjust Piperacillin-Tazobactam to 3.375 g IV q6h or 4.5 g IV q8h",
                    "Discontinue Piperacillin-Tazobactam and use an alternative agent"
                ),
                correctIndex = 2,
                explanation = "In renal impairment, piperacillin-tazobactam requires dose adjustment. The appropriate adjustment is 3.375 g IV q6h or 4.5 g IV q8h."
            ),
            McqQuestion(
                question = "Patient Information: 58-year-old male, weight 90 kg, height 180 cm, serum creatinine 2.9 mg/dL. Diagnosis: HAP, possibly pseudomonal, diabetic nephropathy. Prescription: Meropenem 1 g IV q8h. A pharmacist reviews the antibiotic regimen. Considering the patient's renal function, what is the most appropriate intervention?",
                options = listOf(
                    "Continue Meropenem 1 g IV q8h",
                    "Reduce to Meropenem 500 mg IV q8h",
                    "Adjust to Meropenem 1 g IV q12h",
                    "Switch to Piperacillin-tazobactam 4.5 g IV q6h"
                ),
                correctIndex = 2,
                explanation = "Meropenem requires renal dose adjustment. In this case, 1 g IV q12h is the most appropriate intervention."
            ),
            McqQuestion(
                question = "Patient Information: 45-year-old female, weight 62 kg, height 158 cm, serum creatinine 2.0 mg/dL. Diagnosis: SSTI suspicious for MRSA. Current medication: Cefazolin 1 g IV q8h. The pharmacist reviews the antibiotic regimen. Considering the patient's renal function, what adjustment is needed for Cefazolin?",
                options = listOf(
                    "Continue Cefazolin 1 g IV q8h as prescribed",
                    "Increase Cefazolin to 2 g IV q8h",
                    "Extend Cefazolin dosing interval to 1 g IV q12h",
                    "Discontinue Cefazolin due to renal impairment"
                ),
                correctIndex = 2,
                explanation = "Cefazolin is renally cleared, so the dosing interval should be extended to 1 g IV q12h."
            ),
            McqQuestion(
                question = "Patient: 76-year-old female, 70 kg. Serum creatinine: 2.4 mg/dL. Diagnosis: complicated UTI caused by MDR Enterobacteriaceae. Current therapy: Colistin (Polymyxin E) 700,000 IU IV q12h. On review of this case, the pharmacist considers renal dosing of Colistin. Which adjustment is most appropriate?",
                options = listOf(
                    "Continue current dose: 700,000 IU IV every 12 hours",
                    "Reduce to 1.5 million IU IV once daily to avoid nephrotoxicity",
                    "Adjust to ~160 mg CBA/day (~4.8 million IU IV once daily)",
                    "Switch Colistin to 2 million IU IV q8h for aggressive MDR coverage"
                ),
                correctIndex = 2,
                explanation = "The most appropriate renal-adjusted colistin regimen is approximately 160 mg CBA/day, about 4.8 million IU IV once daily."
            ),
            McqQuestion(
                question = "What factor may alter a drug's efficacy in chronic kidney disease?",
                options = listOf(
                    "Changes in protein binding",
                    "Decreased absorption",
                    "Decreased production of active metabolites required for effect",
                    "All of the above"
                ),
                correctIndex = 3,
                explanation = "Chronic kidney disease may alter drug efficacy through changes in protein binding, absorption, and production of active metabolites."
            ),
            McqQuestion(
                question = "What is one key question to consider when adjusting doses for renal impairment?",
                options = listOf(
                    "What is the drug's half-life?",
                    "What is the risk of treatment failure and how can it be monitored?",
                    "What is the impact on steady-state concentrations?",
                    "What bioavailability changes occur?"
                ),
                correctIndex = 1,
                explanation = "When adjusting renal doses, the pharmacist must consider the risk of treatment failure and how it can be monitored."
            )
        )

        "tdm_therapeutic_dose_adjustment" -> listOf(
            McqQuestion(
                question = "A 65-year-old male with a mechanical prosthetic heart valve is on warfarin 5 mg daily. His INR has been stable at 2.8 for 3 months. He now presents with an INR of 1.2 and reports starting ciprofloxacin 500 mg BID for a urinary tract infection 5 days ago. What is the most appropriate pharmacist recommendation?",
                options = listOf(
                    "Inform the physician of the drug interaction, recommend a temporary warfarin dose increase of 10-20%, and recheck the INR in 3-5 days while the antibiotic course continues.",
                    "Recommend bridging with low-molecular-weight heparin (LMWH) until the INR returns to the therapeutic range, while maintaining the current warfarin dose.",
                    "Advise the physician to increase the warfarin dose by 50% immediately and recheck the INR the next day to ensure rapid correction.",
                    "Recommend no warfarin dose change at this time; instead, advise rechecking the INR after the antibiotic course is completed and adjusting the dose if the INR remains subtherapeutic."
                ),
                correctIndex = 0,
                explanation = "The pharmacist should inform the physician, recommend a temporary 10-20% warfarin dose increase, and recheck INR in 3-5 days."
            ),
            McqQuestion(
                question = "A 32-year-old female with a history of DVT has been on warfarin for 4 months. She informs the pharmacist that she and her partner are planning to conceive in the next few months. What is the most appropriate counseling by the pharmacist?",
                options = listOf(
                    "Warfarin must be discontinued immediately and replaced with low-molecular-weight heparin (LMWH) now, since she is planning pregnancy in the near future.",
                    "Warfarin is a known teratogen; she should use two reliable forms of contraception while on therapy and discuss transitioning to a safer anticoagulant with her physician before conception.",
                    "Warfarin can be continued safely through the first trimester, but she should switch to LMWH during the second and third trimesters when organogenesis is complete.",
                    "Advise that warfarin carries a low risk of harm if the INR remains within the therapeutic range, and recommend continuing therapy with more frequent INR monitoring once pregnant."
                ),
                correctIndex = 1,
                explanation = "Warfarin is teratogenic. The patient should use reliable contraception and discuss switching to a safer anticoagulant before conception."
            ),
            McqQuestion(
                question = "A patient on warfarin with a current INR of 3.2 is scheduled for a dental extraction in 10 days. The patient has a history of atrial fibrillation with a CHA2DS2-VASc score of 4. The physician asks the pharmacist for a recommendation. What is the most appropriate course of action?",
                options = listOf(
                    "Recommend holding warfarin 4-5 days before the procedure, obtaining a pre-procedure INR, and considering bridging therapy with LMWH given the patient's high thromboembolic risk.",
                    "Advise the physician that for minor dental procedures, warfarin can generally be continued if the INR is below 4.0; recheck the INR 1-2 days before the procedure and use local hemostatic measures.",
                    "Recommend holding warfarin for 24 hours before the procedure and administering fresh frozen plasma on the morning of surgery to rapidly normalize the INR.",
                    "Recommend administering Vitamin K 5 mg IV immediately to reduce the INR to below 2.0 before the scheduled procedure"
                ),
                correctIndex = 1,
                explanation = "For minor dental procedures, warfarin can usually be continued if INR is below 4.0, with INR recheck and local hemostatic measures."
            ),
            McqQuestion(
                question = "A 70-year-old hemodialysis patient has been on Epoetin Alfa (Epokine) 4000 IU three times weekly. The current hemoglobin is 9.2 g/dL. Four weeks ago, the hemoglobin was 8.5 g/dL. Iron studies show transferrin saturation of 25% and ferritin of 350 ng/mL. What is the most appropriate dose adjustment?",
                options = listOf(
                    "Increase the ESA dose by 25%, as the hemoglobin remains below 10 g/dL despite 4 weeks of therapy.",
                    "Maintain the current dose and recheck hemoglobin in 4 weeks, since the Hgb has risen by 0.7 g/dL (within the expected response of up to 1 g/dL per month).",
                    "Hold the ESA dose for one week and then resume at the same dose, as the rate of hemoglobin rise suggests overresponse.",
                    "Switch to darbepoetin alfa (Aranesp) for improved efficacy, as the patient has not reached the target hemoglobin of 10-11.5 g/dL."
                ),
                correctIndex = 1,
                explanation = "The hemoglobin increased by 0.7 g/dL in 4 weeks, which is within the expected response, so the current ESA dose should be maintained."
            ),
            McqQuestion(
                question = "A dialysis patient on Epokine 4000 IU three times weekly has a hemoglobin that has risen from 10.5 g/dL to 12.8 g/dL over the past 4 weeks. The patient also has uncontrolled hypertension (BP 168/98 mmHg). What is the most appropriate pharmacist recommendation?",
                options = listOf(
                    "Decrease the ESA dose by 25% and continue monitoring hemoglobin monthly, as the Hgb is approaching but has not exceeded the 13 g/dL ceiling.",
                    "Hold the ESA dose immediately, address the uncontrolled hypertension, and recheck hemoglobin in 1-2 weeks before resuming at a reduced dose.",
                    "Continue the current ESA dose but increase the frequency of hemoglobin monitoring to weekly, since the Hgb is still below the upper safety limit of 13 g/dL.",
                    "Decrease the ESA dose by 25% and add an additional antihypertensive agent, then recheck hemoglobin and blood pressure in 4 weeks."
                ),
                correctIndex = 1,
                explanation = "The ESA should be held because hemoglobin rose significantly and the patient has uncontrolled hypertension."
            )
        )

        "vancomycin_dose_adjustment" -> listOf(
            McqQuestion(
                question = "Which of the following statements is CORRECT regarding the use of AUC-based vancomycin dosing?",
                options = listOf(
                    "AUC dosing is simpler to implement clinically than traditional trough-based monitoring",
                    "AUC dosing primarily aims to maintain trough concentrations consistently between 15-20 mg/L",
                    "The AUC:MIC ratio is the pharmacokinetic/pharmacodynamic parameter that best predicts vancomycin efficacy",
                    "AUC/MIC >= 400 can often be achieved with trough concentrations below 15 mg/L"
                ),
                correctIndex = 3,
                explanation = "AUC/MIC >= 400 can often be achieved even when trough concentrations are below 15 mg/L."
            ),
            McqQuestion(
                question = "In medication administration, when should the trough level of Vancomycin be measured?",
                options = listOf(
                    "30 minutes after completion of the 3rd dose infusion",
                    "30 minutes before administration of the 1st dose",
                    "30 minutes before administration of the 3rd dose",
                    "30 minutes after completion of the 1st dose infusion"
                ),
                correctIndex = 2,
                explanation = "Vancomycin trough is usually measured 30 minutes before the next dose, commonly before the 3rd dose depending on protocol."
            ),
            McqQuestion(
                question = "A 72-year-old female (60 kg, CrCl 40 mL/min) is being treated for MRSA bacteremia. She is on vancomycin 1 g IV q12h. Measured trough = 24 mg/L. What is the most appropriate action?",
                options = listOf(
                    "Continue 1 g IV q12h -- trough is within an acceptable supratherapeutic margin for severe bacteremia",
                    "Reduce to 750 mg IV q12h -- predicted trough = 17 mg/L, AUC = 520 mg*h/L",
                    "Reduce to 1 g IV q24h -- predicted trough = 12 mg/L, AUC = 320 mg*h/L",
                    "Discontinue vancomycin and switch to daptomycin immediately due to imminent nephrotoxicity"
                ),
                correctIndex = 1,
                explanation = "A trough of 24 mg/L is high. Reducing to 750 mg IV q12h gives a predicted trough and AUC within target range."
            ),
            McqQuestion(
                question = "A 55-year-old male (100 kg, CrCl 90 mL/min) with MRSA osteomyelitis is on vancomycin 1 g IV q12h. Measured trough = 13 mg/L (goal 15-20 mg/L). What is the best adjustment?",
                options = listOf(
                    "Continue 1 g IV q12h -- a trough of 13 mg/L is clinically sufficient for bone and joint infections",
                    "Increase to 1.25 g IV q12h -- predicted trough = 16 mg/L, AUC = 471 mg*h/L",
                    "Change to 1.5 g IV q24h -- predicted trough = 12 mg/L, AUC = 536 mg*h/L",
                    "Increase to 1.5 g IV q12h -- predicted trough = 21 mg/L, AUC = 642 mg*h/L"
                ),
                correctIndex = 2,
                explanation = "Changing to 1.5 g IV q24h provides a predicted AUC within target range while avoiding excessive trough exposure."
            ),
            McqQuestion(
                question = "A 68-year-old male (70 kg, 172 cm, SCr 1.4 mg/dL, CrCl ~55 mL/min) with MRSA pneumonia requires initial vancomycin; which regimen best achieves target AUC 400-600 mg*h/L and trough 15-20 mg/L?",
                options = listOf(
                    "1 g IV q12h -- predicted trough = 18 mg/L, AUC = 585 mg*h/L",
                    "500 mg IV q12h -- predicted trough = 12 mg/L, AUC = 402 mg*h/L",
                    "750 mg IV q12h -- predicted trough = 16 mg/L, AUC = 491 mg*h/L",
                    "1.25 g IV q24h -- predicted trough = 12 mg/L, AUC = 321 mg*h/L"
                ),
                correctIndex = 1,
                explanation = "500 mg IV q12h gives a predicted AUC of approximately 402 mg*h/L, within the target AUC range."
            ),
            McqQuestion(
                question = "A 55-year-old female (60 kg, 160 cm, SCr 2.0 mg/dL, CrCl ~30 mL/min) with MRSA osteomyelitis requires vancomycin. Which regimen is most appropriate?",
                options = listOf(
                    "1 g IV q12h -- predicted trough = 26 mg/L, AUC = 710 mg*h/L",
                    "750 mg IV q24h -- predicted trough = 12.9 mg/L, AUC = 470 mg*h/L",
                    "500 mg IV q12h -- predicted trough = 21 mg/L, AUC = 650 mg*h/L",
                    "1.5 g IV q24h -- predicted trough = 31 mg/L, AUC = 810 mg*h/L"
                ),
                correctIndex = 1,
                explanation = "750 mg IV q24h gives an AUC of approximately 470 mg*h/L, which is within the target AUC range."
            )
        )

        "checklist" -> listOf(
            McqQuestion(
                question = "What is the primary objective of the dispensing checklist module?",
                options = listOf(
                    "Theoretical understanding only",
                    "Developing practical clinical competency in verification procedures",
                    "Memorization of drug names",
                    "Administrative compliance"
                ),
                correctIndex = 1,
                explanation = "The primary objective is developing practical competency in verification procedures."
            ),
            McqQuestion(
                question = "Which safety measure is most critical during the dispensing process?",
                options = listOf(
                    "Speed of execution",
                    "Patient verification and identification",
                    "Cost optimization",
                    "Brand preference"
                ),
                correctIndex = 1,
                explanation = "Patient verification is the most critical safety measure during dispensing."
            ),
            McqQuestion(
                question = "When should dispensing documentation be completed?",
                options = listOf(
                    "End of shift",
                    "Immediately after the procedure",
                    "When convenient",
                    "Weekly batch processing"
                ),
                correctIndex = 1,
                explanation = "Documentation should be completed immediately after the dispensing procedure."
            ),
            McqQuestion(
                question = "In case of a dispensing discrepancy, the pharmacist should:",
                options = listOf(
                    "Ignore if minor",
                    "Report and investigate immediately",
                    "Fix silently",
                    "Wait for review cycle"
                ),
                correctIndex = 1,
                explanation = "Discrepancies should be reported and investigated immediately."
            ),
            McqQuestion(
                question = "Communication within the pharmacy team should be:",
                options = listOf(
                    "Minimal",
                    "Clear, timely, and documented",
                    "Informal and verbal only",
                    "Through email only"
                ),
                correctIndex = 1,
                explanation = "Communication should be clear, timely, and documented for patient safety."
            )
        )

        "electrolyte_replacement" -> listOf(
            McqQuestion(
                question = "What is the primary objective of electrolyte replacement therapy?",
                options = listOf(
                    "To achieve supranormal electrolyte levels for faster recovery",
                    "To restore and maintain physiological electrolyte balance",
                    "To replace all electrolytes simultaneously regardless of lab values",
                    "To use IV replacement exclusively for all deficiencies"
                ),
                correctIndex = 1,
                explanation = "The primary objective is to restore and maintain physiological electrolyte balance."
            ),
            McqQuestion(
                question = "Which electrolyte imbalance is most commonly associated with cardiac arrhythmias?",
                options = listOf(
                    "Hypocalcemia",
                    "Hyponatremia",
                    "Hypokalemia or hyperkalemia",
                    "Hypophosphatemia"
                ),
                correctIndex = 2,
                explanation = "Both hypokalemia and hyperkalemia are most commonly associated with cardiac arrhythmias."
            ),
            McqQuestion(
                question = "The maximum recommended IV potassium infusion rate in non-emergency situations is:",
                options = listOf(
                    "40 mEq/hour via peripheral line",
                    "10 mEq/hour via peripheral line",
                    "20 mEq/hour via central line only",
                    "No rate limit exists if cardiac monitoring is in place"
                ),
                correctIndex = 1,
                explanation = "The standard maximum peripheral IV potassium infusion rate is 10 mEq/hour in non-emergency situations."
            ),
            McqQuestion(
                question = "A patient with refractory hypokalemia should also have which electrolyte checked and corrected?",
                options = listOf(
                    "Sodium",
                    "Calcium",
                    "Magnesium",
                    "Chloride"
                ),
                correctIndex = 2,
                explanation = "Magnesium must be checked and corrected, as hypomagnesemia makes hypokalemia refractory to treatment."
            ),
            McqQuestion(
                question = "Oral phosphate replacement is preferred over IV when:",
                options = listOf(
                    "Serum phosphate is below 1.0 mg/dL",
                    "The patient is asymptomatic with mild hypophosphatemia and tolerating oral intake",
                    "The patient is NPO but hemodynamically stable",
                    "IV access is difficult regardless of severity"
                ),
                correctIndex = 1,
                explanation = "Oral phosphate replacement is preferred in mild, asymptomatic hypophosphatemia when the patient can take oral medications."
            )
        )

        else -> emptyList()
    }
}

