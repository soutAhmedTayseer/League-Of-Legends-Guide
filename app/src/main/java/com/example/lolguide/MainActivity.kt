package com.example.lolguide

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.lolguide.ui.theme.LOLGuideTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private var championsState = mutableStateOf<List<Champion>>(emptyList())
    private var isLoadingState = mutableStateOf(true)
    private var isNetworkErrorState = mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = ChampionDatabase.getInstance(this).championDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val isOnline = isNetworkAvailable(this@MainActivity)
            try {
                if (isOnline) {
                    val response = RetrofitClient.apiService.getChampions()
                    val championsList = response.data.values.toList()
                    dao.clearAll()
                    dao.insertAll(championsList)
                }

                val localData = dao.getAllChampions()

                withContext(Dispatchers.Main) {
                    championsState.value = localData
                    isLoadingState.value = false
                    if (localData.isEmpty()) isNetworkErrorState.value = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingState.value = false
                    isNetworkErrorState.value = true
                }
            }
        }

        setContent {
            LOLGuideTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                var selectedChampion by remember { mutableStateOf<Champion?>(null) }
                val isOnline = isNetworkAvailable(this)

                // SnackBar Notification
                LaunchedEffect(Unit) {
                    if (isOnline) {
                        snackbarHostState.showSnackbar("Online: Fetched latest champions")
                    } else {
                        snackbarHostState.showSnackbar("Offline: Showing saved champions")
                    }
                }

                BackHandler(enabled = selectedChampion != null) {
                    selectedChampion = null
                }

                Scaffold(
                    snackbarHost = {
                        // --- UPGRADED CUSTOM SNACKBAR ---
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                modifier = Modifier.padding(16.dp),
                                containerColor = MaterialTheme.colorScheme.surface, // LolCardBg
                                contentColor = MaterialTheme.colorScheme.onSurface, // LolTextLight
                                shape = RoundedCornerShape(12.dp),
                                action = {
                                    TextButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                                        Text("Dismiss", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // App Logo in Snackbar
                                    AsyncImage(
                                        model = R.mipmap.ic_launcher,
                                        contentDescription = "App Logo",
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = data.visuals.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = selectedChampion?.name ?: "LoL Champions",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                if (selectedChampion != null) {
                                    IconButton(onClick = { selectedChampion = null }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when {
                            isLoadingState.value -> LoadingScreen()
                            isNetworkErrorState.value && championsState.value.isEmpty() -> ErrorScreen()
                            else -> {
                                if (selectedChampion == null) {
                                    ChampionListScreen(championsState.value) { clickedChamp ->
                                        selectedChampion = clickedChamp
                                    }
                                } else {
                                    ChampionDetailScreen(selectedChampion!!)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}

// --- UI COMPONENTS ---

@Composable
fun ChampionListScreen(champions: List<Champion>, onChampionClick: (Champion) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(champions) { champ ->
            val imageUrl = "https://ddragon.leagueoflegends.com/cdn/12.6.1/img/champion/${champ.image.full}"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onChampionClick(champ) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = champ.name,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = champ.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ChampionDetailScreen(champion: Champion) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                ChampionImageAndTags(champion)
            }
            Column(modifier = Modifier.weight(0.6f).verticalScroll(rememberScrollState())) {
                ChampionTextAndStats(champion)
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            ChampionImageAndTags(champion)
            Spacer(modifier = Modifier.height(16.dp))
            ChampionTextAndStats(champion)
        }
    }
}

@Composable
fun ChampionImageAndTags(champion: Champion) {
    val splashUrl = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${champion.id}_0.jpg"

    AsyncImage(
        model = splashUrl,
        contentDescription = "Splash Art",
        modifier = Modifier
            .fillMaxWidth()
            .height(if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) 180.dp else 220.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        champion.tags?.forEach { tag ->
            SuggestionChip(
                onClick = { },
                label = { Text(tag) },
                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        champion.partype?.let {
            SuggestionChip(
                onClick = { },
                label = { Text(it) },
                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun ChampionTextAndStats(champion: Champion) {
    Text(
        text = champion.blurb,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text("Combat Info", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            champion.info?.let {
                CombatInfoBar("Attack", it.attack, Color(0xFFE53935))   // Red
                CombatInfoBar("Defense", it.defense, Color(0xFF43A047)) // Green
                CombatInfoBar("Magic", it.magic, Color(0xFF1E88E5))     // Blue
                CombatInfoBar("Difficulty", it.difficulty, Color(0xFF8E24AA)) // Purple
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // --- EXACT 20-STAT REQUIREMENT FULLY MAPPED ---
    Text("Detailed Stats", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        champion.stats?.let { stats ->
            Column(modifier = Modifier.padding(8.dp)) {
                // 1. HP & HP Regen
                StatGridRow(
                    label1 = "Health", base1 = stats.hp, perLvl1 = stats.hpperlevel,
                    label2 = "HP Regen", base2 = stats.hpregen, perLvl2 = stats.hpregenperlevel
                )
                // 2. MP & MP Regen
                StatGridRow(
                    label1 = "Mana / Energy", base1 = stats.mp, perLvl1 = stats.mpperlevel,
                    label2 = "Mana Regen", base2 = stats.mpregen, perLvl2 = stats.mpregenperlevel
                )
                // 3. Attack Damage & Attack Speed
                StatGridRow(
                    label1 = "Attack Damage", base1 = stats.attackdamage, perLvl1 = stats.attackdamageperlevel,
                    label2 = "Attack Speed", base2 = stats.attackspeed, perLvl2 = stats.attackspeedperlevel
                )
                // 4. Armor & Magic Resist
                StatGridRow(
                    label1 = "Armor", base1 = stats.armor, perLvl1 = stats.armorperlevel,
                    label2 = "Magic Resist", base2 = stats.spellblock, perLvl2 = stats.spellblockperlevel
                )
                // 5. Ranges & Speed
                StatGridRow(
                    label1 = "Move Speed", base1 = stats.movespeed, perLvl1 = 0.0,
                    label2 = "Attack Range", base2 = stats.attackrange, perLvl2 = 0.0
                )
                // 6. Crit
                StatGridRow(
                    label1 = "Crit Chance", base1 = stats.crit, perLvl1 = stats.critperlevel,
                    label2 = "", base2 = 0.0, perLvl2 = 0.0
                )
            }
        }
    }
}

// Custom Progress Bar for Combat Info
@Composable
fun CombatInfoBar(label: String, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(80.dp), fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = value / 10f,
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.weight(1f).height(10.dp).clip(CircleShape)
        )
        Text(text = "$value/10", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp), textAlign = TextAlign.End, fontSize = 12.sp)
    }
}

// Custom Grid Row for Detailed Stats
@Composable
fun StatGridRow(label1: String, base1: Double, perLvl1: Double, label2: String, base2: Double, perLvl2: Double) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f).padding(4.dp)) {
            StatCard(label1, base1, perLvl1)
        }
        Box(modifier = Modifier.weight(1f).padding(4.dp)) {
            if (label2.isNotEmpty()) {
                StatCard(label2, base2, perLvl2)
            }
        }
    }
}

@Composable
fun StatCard(label: String, base: Double, perLvl: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp) // Slightly increased padding for readability
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        // Properly formats strings so if perLvl is 0, it won't show the "(+0.0)" text
        val valueText = if (perLvl > 0.0) "$base (+${perLvl}/lvl)" else "$base"
        Text(text = valueText, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
        Text("No internet and no cached data.", color = MaterialTheme.colorScheme.onBackground)
    }
}