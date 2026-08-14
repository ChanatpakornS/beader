package com.beader.core.domain.model

/**
 * A PDF document the user picked, identified by [uri] — a content URI
 * string. Kept as a plain [String] rather than `android.net.Uri` so this
 * module stays free of Android SDK types.
 */
data class PdfDocument(
    val uri: String,
    val pageCount: Int,
)
