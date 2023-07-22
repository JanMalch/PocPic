package io.github.janmalch.pocpic.domain

import android.net.Uri
import android.util.Log
import io.github.janmalch.pocpic.data.SourceEntity
import io.github.janmalch.pocpic.shared.IoDispatcher
import io.github.janmalch.pocpic.shared.toUriOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject


class GetRedirectLocation @Inject constructor(
    okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val noRedirectsClient = okHttpClient.newBuilder().followRedirects(false).build()

    suspend operator fun invoke(source: Source): Uri? {
        if (source.type != SourceEntity.Type.REMOTE) return null
        return withContext(ioDispatcher) {
            val req = Request.Builder()
                .url(source.uri.toString())
                .head()
                .addHeader("Cache-Control", "no-cache")
                .build()

            var res: Response? = null
            try {
                res = noRedirectsClient.newCall(req).execute()
                res.headers["Location"]?.takeIf { res.isRedirect && it.isNotEmpty() }?.toUriOrNull()
            } catch (e: Exception) {
                Log.e("GetRedirectLocation", "Error while getting redirect location.", e)
                null
            } finally {
                res?.close()
            }
        }
    }
}