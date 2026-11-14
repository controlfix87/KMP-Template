package com.cmptemplate.feature.about.report

/**
 * Removes secrets and personal data before anything leaves the device (the reports service is
 * public by design). Add the product's own secret shapes here, each with a test in RedactorTest.
 */
object Redactor {
    private val rules: List<Pair<Regex, String>> = listOf(
        Regex("""(?i)bearer\s+[a-z0-9._~+/=-]+""") to "Bearer <redacted>",
        Regex("""eyJ[a-zA-Z0-9_-]{8,}\.[a-zA-Z0-9_-]{8,}\.[a-zA-Z0-9_-]{8,}""") to "<jwt>",
        Regex("""(?i)("?(access_token|refresh_token|id_token|token|api_key|apikey|password|secret|code|code_verifier|state|session)"?\s*[:=]\s*"?)[^"\s,&}]+""") to "$1<redacted>",
        Regex("""\b[0-9a-fA-F]{32,}\b""") to "<hex>",
        Regex("""\bsk-[A-Za-z0-9_-]{16,}""") to "<key>",
        Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""") to "<email>",
        Regex("""\b(?:\d{1,3}\.){3}\d{1,3}\b""") to "<ip>",
        Regex("""\b(?:[0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}\b""") to "<mac>",
    )

    fun redact(text: String): String = rules.fold(text) { t, (re, repl) -> re.replace(t, repl) }
}
