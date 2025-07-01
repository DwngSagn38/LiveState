package com.example.livestate.ui.earth3d

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.InputStream

class LocalWebServer(private val context: Context) : NanoHTTPD(8080) {
    override fun serve(session: IHTTPSession?): Response {
        Log.d("LocalWebServer", "Serve request for ${session?.uri}")
        val uri = session?.uri ?: "/"
        val assetPath = if (uri == "/") "cesium.html" else uri.substring(1)

        return try {
            val mime = when {
                assetPath.endsWith(".html") -> "text/html"
                assetPath.endsWith(".js") -> "application/javascript"
                assetPath.endsWith(".css") -> "text/css"
                else -> "text/plain"
            }

            val inputStream: InputStream = context.assets.open(assetPath)
            newChunkedResponse(Response.Status.OK, mime, inputStream)
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
        }
    }
}
