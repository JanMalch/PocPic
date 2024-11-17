package io.github.janmalch.pocpic.widget

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.janmalch.pocpic.core.AppRepository
import kotlinx.coroutines.CancellationException
import java.time.Duration

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

    companion object Work {
        private const val UNIQUE_WORK_NAME = "PocPicWidget_PeriodicWorker"

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }

        fun enqueue(context: Context, repeatInterval: Duration) {
            val workManager = WorkManager.getInstance(context)
            Log.d(TAG, "Re-enqueuing periodic work with repeat interval of $repeatInterval.")
            val periodicWorkRequest =
                PeriodicWorkRequest.Builder(UpdateWidgetWorker::class.java, repeatInterval)
                    .addTag("PocPic")
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        Duration.ofMinutes(1L),
                    )
                    .build()
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                periodicWorkRequest
            )
        }
    }
}
