package com.hedvig.app.ui.compose.textutil

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class SwedishSSNVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Make the string XXXXXX-XXXX
        val trimmed = text.text.take(10)

        var output = ""
        for (i in trimmed.indices) {
            output += trimmed[i]
            if (i % 6 == 5 && i != 9) {
                output += "-"
            }
        }

        val swedishSSNOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 5) return offset
                if (offset <= 9) return offset + 1
                return 11
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 6) return offset
                if (offset <= 11) return offset - 1
                return 10
            }
        }

        return TransformedText(
            AnnotatedString(output),
            swedishSSNOffsetTranslator
        )
    }
}
