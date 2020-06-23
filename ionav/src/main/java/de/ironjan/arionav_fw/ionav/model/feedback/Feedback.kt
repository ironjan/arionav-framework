package de.ironjan.arionav_fw.ionav.model.feedback

/**
 * Models feedback gathered via {@link de.ironjan.arionav_fw.ionav.views.FeedbackFragment}.
 */
data class Feedback(val overAllRating:Float = 0f,
                    val mapNavRating:Float = 0f,
                    val arNavRating:Float = 0f,
                    val comments: String ="")
{
    override fun toString(): String {
        return """
Overall: $overAllRating/5.0
Map:     $mapNavRating/5.0
AR:      $arNavRating/5.0

Comments:
$comments
        """.trimIndent()
    }
}