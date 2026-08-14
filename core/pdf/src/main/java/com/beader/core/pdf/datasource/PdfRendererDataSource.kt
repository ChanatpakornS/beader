package com.beader.core.pdf.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.beader.core.common.di.BeaderDispatchers
import com.beader.core.common.di.Dispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around the platform [PdfRenderer]. Holds at most one open
 * document at a time — opening a new URI closes whatever was open before.
 * [PdfRenderer] is not thread-safe, so every call is serialized through
 * [mutex] and run on [ioDispatcher] since rendering a page is a blocking
 * call.
 */
@Singleton
class PdfRendererDataSource
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @Dispatcher(BeaderDispatchers.Io) private val ioDispatcher: CoroutineDispatcher,
    ) {
        private val mutex = Mutex()
        private var fileDescriptor: ParcelFileDescriptor? = null
        private var renderer: PdfRenderer? = null

        /** Opens [uriString], closing any previously open document, and returns its page count. */
        suspend fun open(uriString: String): Int =
            withContext(ioDispatcher) {
                mutex.withLock {
                    closeLocked()
                    val pfd =
                        context.contentResolver.openFileDescriptor(Uri.parse(uriString), "r")
                            ?: error("Unable to open PDF at $uriString")
                    fileDescriptor = pfd
                    PdfRenderer(pfd).also { renderer = it }.pageCount
                }
            }

        /** Renders [pageIndex] of the currently open document, scaled to [widthPx] wide, as PNG bytes. */
        suspend fun renderPage(
            pageIndex: Int,
            widthPx: Int,
        ): ByteArray =
            withContext(ioDispatcher) {
                mutex.withLock {
                    val currentRenderer = checkNotNull(renderer) { "No PDF document is open" }
                    currentRenderer.openPage(pageIndex).use { page ->
                        val boundedWidth = widthPx.coerceAtLeast(MIN_DIMENSION_PX)
                        val height =
                            (boundedWidth.toFloat() * page.height / page.width)
                                .toInt()
                                .coerceAtLeast(MIN_DIMENSION_PX)
                        val bitmap = Bitmap.createBitmap(boundedWidth, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        ByteArrayOutputStream().use { stream ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
                            bitmap.recycle()
                            stream.toByteArray()
                        }
                    }
                }
            }

        /** Closes the currently open document, if any. Safe to call when nothing is open. */
        suspend fun close() =
            withContext(ioDispatcher) {
                mutex.withLock { closeLocked() }
            }

        private fun closeLocked() {
            renderer?.close()
            renderer = null
            fileDescriptor?.close()
            fileDescriptor = null
        }

        private companion object {
            const val PNG_QUALITY = 100
            const val MIN_DIMENSION_PX = 1
        }
    }
