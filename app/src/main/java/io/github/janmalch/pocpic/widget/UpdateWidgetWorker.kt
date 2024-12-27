package io.github.janmalch.pocpic.widget

import android.content.Context
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
import timber.log.Timber
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
            Timber.d("Rerolling for new picture.")
            repository.reroll()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(
                e,
                "Failed to get random picture to update widget. Attempt #%d",
                runAttemptCount
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
            Timber.d("Re-enqueuing periodic work with repeat interval of %s.", repeatInterval)
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
