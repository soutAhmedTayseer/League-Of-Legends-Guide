package com.venom7t.lolguide.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.venom7t.lolguide.presentation.theme.AppTheme

/**
 * Renders Data Dragon's ability text.
 *
 * Riot ships ability descriptions as pseudo-HTML: standard tags (`<br>`,
 * `<b>`, `<i>`, `<li>`) mixed with bespoke semantic ones (`<magicDamage>`,
 * `<physicalDamage>`, `<scaleAP>`, `<status>`, `<keywordMajor>`). Rendering
 * the string raw shows the markup to the user; stripping it with a blunt
 * `replace("<", "")` destroys the `<` in expressions like "if target is < 40%
 * health" and loses the damage-type colouring that makes the text readable.
 *
 * This is a small dedicated parser instead: it walks the string once, maps the
 * tags it knows to spans, drops the ones it does not, and leaves any `<` that
 * is not part of a well-formed tag alone.
 */
object AbilityTextParser {

    /**
     * Tags that carry meaning worth showing. Everything else is structural
     * and only its content survives.
     */
    private enum class Semantic {
        PHYSICAL, MAGIC, TRUE_DAMAGE, HEALING, SHIELD, STATUS, KEYWORD, SCALING, BOLD, ITALIC
    }

    private val semanticTags: Map<String, Semantic> = mapOf(
        "physicaldamage" to Semantic.PHYSICAL,
        "attackdamage" to Semantic.PHYSICAL,
        "scalead" to Semantic.PHYSICAL,
        "magicdamage" to Semantic.MAGIC,
        "scaleap" to Semantic.MAGIC,
        "truedamage" to Semantic.TRUE_DAMAGE,
        "healing" to Semantic.HEALING,
        "lifesteal" to Semantic.HEALING,
        "shield" to Semantic.SHIELD,
        "status" to Semantic.STATUS,
        "keywordstealth" to Semantic.STATUS,
        "onhit" to Semantic.KEYWORD,
        "keywordmajor" to Semantic.KEYWORD,
        "spellname" to Semantic.KEYWORD,
        "attention" to Semantic.KEYWORD,
        "scalelevel" to Semantic.SCALING,
        "scalemana" to Semantic.SCALING,
        "scalehealth" to Semantic.SCALING,
        "scavengedmana" to Semantic.SCALING,
        "speed" to Semantic.SCALING,
        "b" to Semantic.BOLD,
        "i" to Semantic.ITALIC,
        "em" to Semantic.ITALIC,
    )

    /** Tags that produce a line break rather than styled text. */
    private val breakTags = setOf("br", "br/", "li", "/li", "p", "/p")

    private val entities = mapOf(
        "&nbsp;" to " ",
        "&amp;" to "&",
        "&lt;" to "<",
        "&gt;" to ">",
        "&quot;" to "\"",
        "&#39;" to "'",
        "&apos;" to "'",
    )

    /** One parsed run of text plus whatever styling applied to it. */
    private data class Run(val text: String, val semantics: List<Semantic>)

    private fun parse(raw: String): List<Run> {
        val runs = mutableListOf<Run>()
        val open = ArrayDeque<Semantic>()
        val buffer = StringBuilder()
        var index = 0

        fun flush() {
            if (buffer.isNotEmpty()) {
                runs += Run(buffer.toString(), open.toList())
                buffer.clear()
            }
        }

        while (index < raw.length) {
            val character = raw[index]

            if (character != '<') {
                buffer.append(character)
                index++
                continue
            }

            val close = raw.indexOf('>', startIndex = index + 1)
            // A '<' with no matching '>' is literal text -- "damage if < 40%"
            // is real ability copy, not broken markup.
            if (close == -1) {
                buffer.append(character)
                index++
                continue
            }

            val inner = raw.substring(index + 1, close).trim()
            // Same reasoning: "< 40" has a space and is not a tag name.
            if (inner.isEmpty() || inner.any { it.isWhitespace() }) {
                buffer.append(character)
                index++
                continue
            }

            val normalised = inner.lowercase().removeSuffix("/")
            val isClosing = normalised.startsWith("/")
            val name = normalised.removePrefix("/")

            when {
                name in breakTags || normalised in breakTags -> {
                    flush()
                    // Collapse runs of breaks so the text does not gain a
                    // paragraph of blank space where Riot used <br><br>.
                    if (runs.isNotEmpty() && !runs.last().text.endsWith("\n")) {
                        runs += Run("\n", emptyList())
                    }
                }

                semanticTags.containsKey(name) -> {
                    flush()
                    val semantic = semanticTags.getValue(name)
                    if (isClosing) {
                        open.remove(semantic)
                    } else {
                        open.addLast(semantic)
                    }
                }

                // A tag we do not recognise: drop the tag, keep the content.
                else -> flush()
            }

            index = close + 1
        }

        flush()
        return runs
    }

    private fun decodeEntities(text: String): String {
        var result = text
        entities.forEach { (entity, replacement) -> result = result.replace(entity, replacement) }
        return result
    }

    /**
     * Builds the styled text. Must be called from composition because the
     * colours come from [AppTheme].
     */
    @Composable
    fun annotate(raw: String): AnnotatedString {
        val colors = AppTheme.colors

        fun styleFor(semantic: Semantic): SpanStyle = when (semantic) {
            Semantic.PHYSICAL -> SpanStyle(color = colors.attack)
            Semantic.MAGIC -> SpanStyle(color = colors.magic)
            Semantic.TRUE_DAMAGE -> SpanStyle(color = colors.warning)
            Semantic.HEALING -> SpanStyle(color = colors.success)
            Semantic.SHIELD -> SpanStyle(color = colors.accent)
            Semantic.STATUS -> SpanStyle(color = colors.accent, fontWeight = FontWeight.Medium)
            Semantic.KEYWORD -> SpanStyle(color = colors.primary, fontWeight = FontWeight.SemiBold)
            Semantic.SCALING -> SpanStyle(color = colors.accent)
            Semantic.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
            Semantic.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
        }

        return buildAnnotatedString {
            parse(raw).forEach { run ->
                val text = decodeEntities(run.text)
                if (run.semantics.isEmpty()) {
                    append(text)
                    return@forEach
                }
                // Merge every open tag into one span so nested markup
                // (bold inside magicDamage) keeps both effects.
                val merged = run.semantics
                    .map(::styleFor)
                    .reduce { accumulator, style -> accumulator.merge(style) }
                withStyle(merged) { append(text) }
            }
        }.trimTrailingNewlines()
    }

    /** Plain text with all markup removed, for content descriptions. */
    fun plain(raw: String): String =
        parse(raw).joinToString(separator = "") { decodeEntities(it.text) }.trim()

    private fun AnnotatedString.trimTrailingNewlines(): AnnotatedString {
        val end = text.indexOfLast { it != '\n' } + 1
        return if (end == text.length) this else subSequence(0, end)
    }
}

/** Convenience so screens read as `abilityText(description)`. */
@Composable
fun abilityText(raw: String): AnnotatedString = AbilityTextParser.annotate(raw)
