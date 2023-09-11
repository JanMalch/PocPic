package io.github.janmalch.pocpic.widget

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.janmalch.pocpic.domain.GetRandomPicture

@HiltWorker
class UpdateWidgetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val getRandomPicture: GetRandomPicture,
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            getRandomPicture()
        } catch (e: Exception) {
            Log.e("UpdateWidgetWorker", "Failed to get random picture to update widget.", e)
            return Result.failure()
        }
        return Result.success()
    }
}
