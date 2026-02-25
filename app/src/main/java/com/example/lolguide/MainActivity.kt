package com.example.lolguide

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
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

                LaunchedEffect(Unit) {
                    if (isOnline) snackbarHostState.showSnackbar("Online: Fetched latest champions")
                    else snackbarHostState.showSnackbar("Offline: Showing saved champions")
                }

                BackHandler(enabled = selectedChampion != null) {
                    selectedChampion = null
                }

                Scaffold(snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            modifier = Modifier.padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(12.dp),
                            action = {
                                TextButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                                    Text("Dismiss", color = MaterialTheme.colorScheme.primary)
                                }
                            }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                }, topBar = {
                    TopAppBar(
                        title = {
                        Text(
                            text = selectedChampion?.name ?: "LoL Champions",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }, navigationIcon = {
                        if (selectedChampion != null) {
                            IconButton(onClick = { selectedChampion = null }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                    )
                }) { paddingValues ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionListScreen(champions: List<Champion>, onChampionClick: (Champion) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredChampions = champions.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.title.contains(
            searchQuery,
            ignoreCase = true
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    "Search champions...", color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredChampions) { champ ->
                val imageFileName = champ.image.full.takeIf { it.isNotEmpty() } ?: "${champ.id}.png"
                val patchVersion = champ.version ?: "14.23.1"
                val imageUrl =
                    "https://ddragon.leagueoflegends.com/cdn/$patchVersion/img/champion/$imageFileName"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onChampionClick(champ) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(imageUrl)
                            .crossfade(true).error(R.mipmap.ic_launcher).build(),
                        contentDescription = "${champ.name} Icon",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)
                            ),
                        contentScale = ContentScale.Crop
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
}

@Composable
fun ChampionDetailScreen(champion: Champion) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // --- NEW: Fetch Individual Abilities State ---
    var championDetail by remember { mutableStateOf<ChampionDetail?>(null) }
    val patchVersion = champion.version ?: "14.23.1"

    LaunchedEffect(champion.id) {
        try {
            val response = RetrofitClient.apiService.getChampionDetail(patchVersion, champion.id)
            championDetail = response.data[champion.id]
        } catch (e: Exception) {
            // Fails silently if offline; user still sees base stats
        }
    }

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChampionImageAndTags(champion)
            }
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .verticalScroll(rememberScrollState())
            ) {
                ChampionTextAndStats(champion, championDetail, patchVersion)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ChampionImageAndTags(champion)
            Spacer(modifier = Modifier.height(16.dp))
            ChampionTextAndStats(champion, championDetail, patchVersion)
        }
    }
}

@Composable
fun ChampionImageAndTags(champion: Champion) {
    val splashUrl =
        "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${champion.id}_0.jpg"

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(splashUrl).crossfade(true).build(),
        contentDescription = "Splash Art",
        modifier = Modifier
            .fillMaxWidth()
            .height(if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) 180.dp else 220.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop
    )

    Spacer(modifier = Modifier.height(12.dp))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()
    ) {
        item {
            champion.partype?.let {
                SuggestionChip(
                    onClick = { },
                    label = { Text(it, fontWeight = FontWeight.Bold) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        labelColor = MaterialTheme.colorScheme.primary
                    ),
                    border = null
                )
            }
        }
        champion.tags?.let { tags ->
            items(tags) { tag ->
                SuggestionChip(
                    onClick = { },
                    label = { Text(tag) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = null
                )
            }
        }
    }
}

@Composable
fun ChampionTextAndStats(champion: Champion, detail: ChampionDetail?, patchVersion: String) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.5f
            )
        ), shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "\"${champion.blurb}\"",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // --- NEW: ABILITIES SECTION ---
    if (detail != null) {
        AbilitiesSection(detail, patchVersion)
        Spacer(modifier = Modifier.height(24.dp))
    }

    Text(
        "Combat Potential",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            champion.info?.let {
                CombatInfoBar("Attack", it.attack, Color(0xFFE53935))
                CombatInfoBar("Defense", it.defense, Color(0xFF43A047))
                CombatInfoBar("Magic", it.magic, Color(0xFF1E88E5))
                CombatInfoBar("Difficulty", it.difficulty, Color(0xFF8E24AA))
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        "Base Stats",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        champion.stats?.let { stats ->
            Column(modifier = Modifier.padding(8.dp)) {
                StatGridRow(
                    "Health",
                    stats.hp,
                    stats.hpperlevel,
                    "HP Regen",
                    stats.hpregen,
                    stats.hpregenperlevel
                )
                StatGridRow(
                    "Mana/Energy",
                    stats.mp,
                    stats.mpperlevel,
                    "Mana Regen",
                    stats.mpregen,
                    stats.mpregenperlevel
                )
                StatGridRow(
                    "Atk Damage",
                    stats.attackdamage,
                    stats.attackdamageperlevel,
                    "Atk Speed",
                    stats.attackspeed,
                    stats.attackspeedperlevel
                )
                StatGridRow(
                    "Armor",
                    stats.armor,
                    stats.armorperlevel,
                    "Magic Resist",
                    stats.spellblock,
                    stats.spellblockperlevel
                )
                StatGridRow("Move Speed", stats.movespeed, 0.0, "Atk Range", stats.attackrange, 0.0)
                StatGridRow("Crit Chance", stats.crit, stats.critperlevel, "", 0.0, 0.0)
            }
        }
    }
}

// --- NEW FEATURE 1: ABILITIES UI COMPONENT ---

// Helper class to unify Passives and Spells into one list
data class AbilityUiModel(
    val name: String,
    val description: String,
    val cost: String,
    val cooldown: String,
    val imageUrl: String,
    val hotkey: String
)

@Composable
fun AbilitiesSection(detail: ChampionDetail, patchVersion: String) {
    // 1. Map API data to our unified UI models
    val abilities = mutableListOf<AbilityUiModel>()

    // Add Passive
    val passiveImage =
        "https://ddragon.leagueoflegends.com/cdn/$patchVersion/img/passive/${detail.passive.image.full}"
    abilities.add(
        AbilityUiModel(
            detail.passive.name, detail.passive.description, "None", "0", passiveImage, "Passive"
        )
    )

    // Add Spells (Q, W, E, R)
    val hotkeys = listOf("Q", "W", "E", "R")
    detail.spells.forEachIndexed { index, spell ->
        val spellImage =
            "https://ddragon.leagueoflegends.com/cdn/$patchVersion/img/spell/${spell.image.full}"
        abilities.add(
            AbilityUiModel(
                spell.name,
                spell.description,
                spell.costBurn,
                spell.cooldownBurn,
                spellImage,
                hotkeys.getOrElse(index) { "?" })
        )
    }

    // State to track which ability is clicked (defaults to Passive)
    var selectedAbility by remember { mutableStateOf(abilities[0]) }

    Text(
        "Abilities",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )

    // Row of clickable Icons
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        abilities.forEach { ability ->
            val isSelected = selectedAbility.name == ability.name

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { selectedAbility = ability }) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(ability.imageUrl)
                        .crossfade(true).build(),
                    contentDescription = ability.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(if (ability.hotkey == "Passive") CircleShape else RoundedCornerShape(8.dp))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                            shape = if (ability.hotkey == "Passive") CircleShape else RoundedCornerShape(
                                8.dp
                            )
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ability.hotkey,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Detail Card for the selected ability
    Crossfade(targetState = selectedAbility, label = "ability_crossfade") { ability ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = ability.name,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (ability.hotkey != "Passive") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cooldown: ${ability.cooldown}s",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Cost: ${ability.cost}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Riot's API returns HTML tags in descriptions. This cleans them out.
                val cleanDescription = ability.description.replace(Regex("<.*?>"), "")
                Text(
                    text = cleanDescription,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// --- EXISTING HELPER UI COMPONENTS (Keep exact same as before) ---
@Composable
fun CombatInfoBar(label: String, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(70.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        LinearProgressIndicator(
            progress = value / 10f,
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(CircleShape)
        )
        Text(
            text = "$value/10",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End,
            fontSize = 12.sp
        )
    }
}

@Composable
fun StatGridRow(
    label1: String, base1: Double, perLvl1: Double, label2: String, base2: Double, perLvl2: Double
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
        ) { StatCard(label1, base1, perLvl1) }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
        ) {
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
            .padding(8.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        val valueText = if (perLvl > 0.0) "$base (+${perLvl})" else "$base"
        Text(
            text = valueText,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
    ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
}

@Composable
fun ErrorScreen() {
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
    ) { Text("No internet and no cached data.", color = MaterialTheme.colorScheme.onBackground) }
}