package com.example.lolguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.example.lolguide.ui.theme.LOLGuideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a One Time Work Request
        val championWorkRequest = OneTimeWorkRequestBuilder<ChampionWorker>().build()

        // Enqueue the work
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(championWorkRequest)

        // Observe WorkManager Status
        val workInfoLiveData = workManager.getWorkInfoByIdLiveData(championWorkRequest.id)

        setContent {
            LOLGuideTheme {
                val workInfo by workInfoLiveData.observeAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (workInfo?.state) {
                        WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED -> LoadingScreen()
                        WorkInfo.State.SUCCEEDED -> ChampionListScreen(champions = ChampionRepository.championsList)
                        WorkInfo.State.FAILED -> ErrorScreen()
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorScreen() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text("Failed to load champions.", color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun ChampionListScreen(champions: List<Champion>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(champions) { champ ->
            ChampionItem(champ)
        }
    }
}

@Composable
fun ChampionItem(champion: Champion) {
    val imageUrl = "https://ddragon.leagueoflegends.com/cdn/16.1.1/img/champion/${champion.image.full}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "${champion.name} icon",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = champion.name,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = champion.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}