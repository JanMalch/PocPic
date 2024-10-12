package io.github.janmalch.pocpic.widget

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.janmalch.pocpic.core.AppRepository
import kotlinx.coroutines.CancellationException

private const val TAG = "UpdateWidgetWorker"

@HiltWorker
class UpdateWidgetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: AppRepository,
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            Log.d(TAG, "Rerolling for new picture.")
            repository.reroll()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(
                TAG,
                "Failed to get random picture to update widget. Attempt #$runAttemptCount",
                e
            )
            return if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
        return Result.success()
    }
}
