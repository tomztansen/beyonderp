def detailRows = getElementValue("subform_grid1", false)

if (detailRows == null || detailRows.isEmpty()) {
    showError("Validation", "Data details cannot be empty")
    return false
}

def errors = []
def usedSequences = [] as Set

// ============================================================
// 1. Validate each row
// ============================================================

for (int i = 0; i < detailRows.size(); i++) {

    def row = detailRows[i]

    def sequence = row['sequence'] as Integer
    def next = row['next'] as Integer

    // --------------------------------------------------------
    // Sequence is mandatory
    // --------------------------------------------------------
    if (sequence == null) {
        errors.add(
            "Row ${i + 1}: The sequence field is mandatory."
        )
        continue
    }

    // --------------------------------------------------------
    // Sequence must not be 999
    // --------------------------------------------------------
    if (sequence == 999) {
        errors.add(
            "Row ${i + 1}: The sequence must not be 999."
        )
    }

    // --------------------------------------------------------
    // Sequence must be unique
    // --------------------------------------------------------
    if (!usedSequences.add(sequence)) {
        errors.add(
            "Row ${i + 1}: Duplicate sequence ${sequence}."
        )
    }

    // --------------------------------------------------------
    // Next is mandatory
    // --------------------------------------------------------
    if (next == null) {
        errors.add(
            "Row ${i + 1}: The 'Next' field is mandatory."
        )
        continue
    }

    // ========================================================
    // 2. Validate NEXT
    // ========================================================

    if (i < detailRows.size() - 1) {

        // ----------------------------------------------------
        // Not the last row
        // ----------------------------------------------------

        def nextRowSequence =
            detailRows[i + 1]['sequence'] as Integer

        // 999 is only allowed on the last row
        if (next == 999) {

            errors.add(
                "Row ${i + 1}: Next 999 should only be used on the last row."
            )

        } else if (nextRowSequence == null) {

            errors.add(
                "Row ${i + 2}: The sequence field is mandatory."
            )

        } else if (next != nextRowSequence) {

            // NEXT must point to the sequence of the next row
            errors.add(
                "Row ${i + 1}: Next ${next} should be ${nextRowSequence}."
            )
        }

    } else {

        // ----------------------------------------------------
        // Last row
        // ----------------------------------------------------

        // Last NEXT must be 999
        if (next != 999) {

            errors.add(
                "Row ${i + 1}: The Next value on the last row must be 999."
            )
        }
    }
}

// ============================================================
// 3. Validation result
// ============================================================

if (!errors.isEmpty()) {

    showError(
        "Validation",
        "Found ${errors.size()} validation error(s)."
    )

    errors.each { error ->
        showError("Validation", error)
    }

    return false
}

// All validations passed
return true
