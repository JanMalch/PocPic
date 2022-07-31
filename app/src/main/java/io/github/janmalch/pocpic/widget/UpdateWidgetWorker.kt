package io.github.janmalch.pocpic.widget

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.janmalch.pocpic.data.SourceProvider

class UpdateWidgetWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val source = SourceProvider
            .createInstance(applicationContext)
            .yieldSource()
            ?: return Result.failure()

        val intent = Intent(applicationContext, PocPicWidget::class.java).apply {
            action = PocPicWidget.ACTION_CHANGE_IMAGE
            putExtra(PocPicWidget.EXTRA_PICTURE_SOURCE, source)
        }

        applicationContext.sendBroadcast(intent)

        return Result.success()
    }
}
