package io.github.hylkeb.retrocache

import android.annotation.SuppressLint
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.security.MessageDigest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.Okio
import okio.buffer
import okio.source

interface CacheProvider {
    // TODO use some hash value based on the request, or provide some convenience method to get a hash value or string from the request.
    fun getCachedResponse(request: Request): CachedResponse?
    fun storeResponseInCache(request: Request, response: Response, dateTimeMillis: Long)
    fun deleteCachedResponse(request: Request)

    data class CachedResponse(
        val response: Response,
        val responseDate: Long
    )

    companion object {
        val NoCache = object : CacheProvider {
            override fun getCachedResponse(request: Request): CachedResponse? = null

            override fun storeResponseInCache(
                request: Request,
                response: Response,
                dateTimeMillis: Long
            ) { }

            override fun deleteCachedResponse(request: Request) { }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
class FolderCacheProvider(
    private val folder: File
) : CacheProvider {
    companion object {
        private const val METADATA_PREFIX = "meta-"
        private const val BODY_PREFIX = "body-"
    }
    override fun getCachedResponse(request: Request): CacheProvider.CachedResponse? {
        val cacheKey = getCacheKey(request)
        val metaDataFile = File(folder, METADATA_PREFIX + cacheKey)
        if (!metaDataFile.exists()) {
            return null
        }

        val responseBodyFile = File(folder, BODY_PREFIX + cacheKey)
        if (!responseBodyFile.exists()) {
            deleteCachedResponse(request)
            return null
        }
        val metaData = Json.decodeFromStream<ResponseMetaData>(FileInputStream(metaDataFile))
        val responseBody = object : ResponseBody() {
            override fun contentType(): MediaType? {
                return metaData.responseBodyContentType.toMediaTypeOrNull()
            }

            override fun contentLength(): Long {
                return metaData.responseBodyContentLength
            }

            override fun source(): BufferedSource {
                return responseBodyFile.source().buffer()
            }

        }

        val responseHeaderNamesAndValues = metaData.responseHeaders.flatMap { header -> header.value.flatMap { listOf(header.key, it) } }
        val response = Response.Builder()
            .request(request)
            .code(metaData.code)
            .message(metaData.responseStatusMessage)
            .protocol(Protocol.get(metaData.protocol))
            .headers(headersOf(*responseHeaderNamesAndValues.toTypedArray()))
            .body(responseBody)
            .build()
        return CacheProvider.CachedResponse(response, metaData.responseDate)
    }

    override fun storeResponseInCache(request: Request, response: Response, dateTimeMillis: Long) {
        val responseBody = response.body ?: return // no need to store responses without a body
        val cacheKey = getCacheKey(request)
        val metaDataFile = File(folder, METADATA_PREFIX + cacheKey).also {
            if (it.exists()) {
                it.delete()
            }
            it.createNewFile()
        }

        val metaData = ResponseMetaData(
            url = request.url.toString(),
            method = request.method,
            requestHeaders = request.headers.toMultimap(), // todo filter stuff that shouldnt be part of the cache key (maybe by making cachekey a data class instead, with a method to extract the hash value)
            responseDate = dateTimeMillis,
            code = response.code,
            responseStatusMessage = response.message,
            protocol = response.protocol.toString(),
            responseHeaders = response.headers.toMultimap(),
            responseBodyContentType = responseBody.contentType()?.toString() ?: "",
            responseBodyContentLength = responseBody.contentLength()
        )
        FileWriter(metaDataFile).use {
            it.write(Json.encodeToString(metaData))
        }

        val responseBodyFile = File(folder, BODY_PREFIX + cacheKey).also {
            if (it.exists()) {
                it.delete()
            }
            it.createNewFile()
        }

        val source = responseBody.source().peek()
        source.request(Long.MAX_VALUE)
        val buffer = source.buffer.clone()
        FileOutputStream(responseBodyFile).use {
            buffer.writeTo(it)
        }
    }

    override fun deleteCachedResponse(request: Request) {
        val cacheKey = getCacheKey(request)
        File(folder, METADATA_PREFIX + cacheKey).delete()
        File(folder, BODY_PREFIX + cacheKey).delete()
    }

    @SuppressLint("NewApi") // todo add annotation for target api on class
    private fun getCacheKey(request: Request): String {
        val url = request.url.toString()
        val method = request.method
        val headers = request.headers.toMultimap().entries.joinToString() //  todo filter stuff that shouldnt be part of the cache key
        val requestBody = request.body
        val body = if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            buffer.readString(Charsets.UTF_8)
        } else ""
        val messageDigest = MessageDigest.getInstance("SHA-1")
        return Base64.encodeToString(
            messageDigest.digest((url + method + headers + body).toByteArray()),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
    }

}

@Serializable
internal data class ResponseMetaData(
    val url: String,
    val method: String,
    val requestHeaders: Map<String, List<String>>,
    val responseDate: Long,
    val code: Int,
    val responseStatusMessage: String,
    val protocol: String,
    val responseHeaders: Map<String, List<String>>,
    val responseBodyContentType: String,
    val responseBodyContentLength: Long,
)
