package com.cmptemplate.feature.about.report

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class RedactorTest {
    private fun gone(secret: String, line: String) {
        val out = Redactor.redact(line)
        assertFalse(secret in out, "'$secret' survived in: $out")
    }

    @Test fun `removes bearer tokens, JWTs and key-value secrets`() {
        gone("abc.def-123", "Authorization: Bearer abc.def-123")
        gone("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.c2lnbmF0dXJlLXZhbHVl", "jwt=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.c2lnbmF0dXJlLXZhbHVl")
        gone("hunter2", "{\"password\":\"hunter2\"}")
        gone("s3cr3t", "https://x.test/cb?code=s3cr3t&state=x")
        gone("sk-abcdefghijklmnopqrstu", "key sk-abcdefghijklmnopqrstu used")
        gone("0123456789abcdef0123456789abcdef", "hash 0123456789abcdef0123456789abcdef")
    }

    @Test fun `removes e-mail, IP and MAC addresses`() {
        gone("someone@example.com", "from someone@example.com")
        gone("192.168.1.20", "connect 192.168.1.20:443")
        gone("aa:bb:cc:dd:ee:ff", "wifi aa:bb:cc:dd:ee:ff")
    }

    @Test fun `keeps harmless text`() {
        val text = "expires_in=3600 scope=user screen=Home items=42"
        assertContains(Redactor.redact(text), "expires_in=3600")
        assertContains(Redactor.redact(text), "scope=user")
        assertContains(Redactor.redact(text), "items=42")
    }
}
