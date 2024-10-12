package io.github.janmalch.pocpic.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.janmalch.pocpic.core.Logger
import io.github.janmalch.pocpic.core.PictureRepository
import kotlinx.coroutines.CancellationException

@HiltWorker
class UpdateWidgetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val pictureRepository: PictureRepository,
    private val logger: Logger,
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            logger.info("Getting new picture, triggered by interval worker.")
            pictureRepository.reroll()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logger.error(
                "Failed to get random picture to update widget. Attempt #$runAttemptCount",
                e
            )
            return if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
        return Result.success()
    }
}
