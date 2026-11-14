package com.cmptemplate.core.model

/**
 * Who this build is, supplied by each host (Android, iOS, desktop) into DI. [id] is the Android
 * package / iOS bundle id / desktop id, exactly: the reports service files tickets under it.
 */
data class AppInfo(
    val id: String,
    val name: String,
    val version: String,
    val code: String,
    /** Store or channel plus build type, e.g. "play/release", "github/debug". */
    val flavor: String,
    /** "android", "ios" or "desktop". */
    val platform: String,
)
