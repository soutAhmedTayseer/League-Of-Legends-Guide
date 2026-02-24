package com.example.lolguide

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ChampionWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Fetch data
            val response = RetrofitClient.apiService.getChampions()

            // The API returns list
            ChampionRepository.championsList = response.data.values.toList()

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}