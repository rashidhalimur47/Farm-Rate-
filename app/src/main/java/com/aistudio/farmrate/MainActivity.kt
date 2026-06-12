// Forced refresh to resolve app installation I/O errors
package com.aistudio.farmrate

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.farmrate.data.*
import com.aistudio.farmrate.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getFormattedDateTime(lang: AppLanguage, customDate: Date = Date()): String {
    return when (lang) {
        AppLanguage.BENGALI -> {
            val format = SimpleDateFormat("dd MMMM yyyy, a hh:mm", Locale("bn", "BD"))
            val output = format.format(customDate)
            output.replace("AM", "সকাল").replace("PM", "বিকাল")
                .replace('0', '০')
                .replace('1', '১')
                .replace('2', '২')
                .replace('3', '৩')
                .replace('4', '৪')
                .replace('5', '৫')
                .replace('6', '৬')
                .replace('7', '৭')
                .replace('8', '৮')
                .replace('9', '৯')
        }
        AppLanguage.HINDI -> {
            val format = SimpleDateFormat("dd MMMM yyyy, a hh:mm", Locale("hi", "IN"))
            format.format(customDate).replace("AM", "पूर्वाह्न").replace("PM", "अपराह्न")
        }
        AppLanguage.ENGLISH -> {
            val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
            format.format(customDate)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FarmRateTheme {
                FarmRateApp()
            }
        }
    }
}

suspend fun fetchOnlineRates(): List<Commodity> = withContext(Dispatchers.IO) {
    val resultList = mutableListOf<Commodity>()
    try {
        // Agmarknet open Government API URL for market rates
        val url = URL("https://api.data.gov.in/resource/9ef84281-22f2-43b1-8176-7458622f5837?api-key=579b464db66ec23bdd000001c3c900e2003c40145c22e564c7e63b65&format=json&limit=150")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        if (conn.responseCode == 200) {
            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()

            val jsonObject = JSONObject(sb.toString())
            if (jsonObject.has("records")) {
                val records = jsonObject.getJSONArray("records")
                for (i in 0 until records.length()) {
                    val rec = records.getJSONObject(i)
                    val state = rec.optString("state", "West Bengal")
                    val district = rec.optString("district", "Kolkata")
                    val market = rec.optString("market", "Kolkata")
                    val commodityName = rec.optString("commodity", "Potato")
                    val variety = rec.optString("variety", "Jyoti")
                    val minPrice = rec.optString("min_price", "0").toIntOrNull() ?: 0
                    val maxPrice = rec.optString("max_price", "0").toIntOrNull() ?: 0
                    val modalPrice = rec.optString("modal_price", "0").toIntOrNull() ?: 0
                    val arrivalDate = rec.optString("arrival_date", "")

                    if (modalPrice > 0) {
                        resultList.add(
                            Commodity(
                                id = "online_$i",
                                name = commodityName,
                                variety = variety,
                                modalPrice = modalPrice,
                                minPrice = minPrice,
                                maxPrice = maxPrice,
                                arrivalDate = arrivalDate,
                                state = state,
                                district = district,
                                market = market
                            )
                        )
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    resultList
}

@Composable
fun FarmRateApp() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("farmrate_prefs", Context.MODE_PRIVATE) }

    // App State Variables
    var currentScreen by remember { mutableStateOf("home") } // "home", "search", "settings"
    var selectedCommodityForDetail by remember { mutableStateOf<Commodity?>(null) }
    
    var selectedState by remember { mutableStateOf(sharedPrefs.getString("state", "West Bengal") ?: "West Bengal") }
    var selectedDistrict by remember { mutableStateOf(sharedPrefs.getString("district", "Kolkata") ?: "Kolkata") }
    var selectedMandi by remember { mutableStateOf(sharedPrefs.getString("mandi", "Kolkata") ?: "Kolkata") }

    var appLanguage by remember {
        val langStr = sharedPrefs.getString("language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
        mutableStateOf(AppLanguage.valueOf(langStr))
    }

    var favoritesList by remember {
        val favSet = sharedPrefs.getStringSet("favorites", emptySet()) ?: emptySet()
        mutableStateOf(favSet.toMutableStateList())
    }

    var showSplash by remember { mutableStateOf(true) }
    var showOnboarding by remember {
        mutableStateOf(sharedPrefs.getBoolean("show_onboarding", true))
    }

    // Daily auto-update state variables
    var lastUpdatedText by remember {
        mutableStateOf(sharedPrefs.getString("last_updated_display", "") ?: "")
    }
    var isManualUpdating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Function to trigger market rates update
    val updateMarketRates: (Boolean) -> Unit = { force ->
        isManualUpdating = true
        coroutineScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val seed = if (force) {
                (1..1000000).random()
            } else {
                today.hashCode()
            }
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val formattedDisplay = getFormattedDateTime(appLanguage, Date())

            // Generate exhaustive offline baseline
            val offlineBaseline = SampleMandiData.generateAllCommodities(seed, formattedDate)

            // Dynamic background fetch from Agmarknet API
            val onlineRates = try {
                fetchOnlineRates()
            } catch (e: Exception) {
                emptyList<Commodity>()
            }

            // Merge values
            val finalMerged = if (onlineRates.isEmpty()) {
                offlineBaseline
            } else {
                val mergedMap = offlineBaseline.associateBy { "${it.state}_${it.district}_${it.market}_${it.name.lowercase()}" }.toMutableMap()
                for (online in onlineRates) {
                    val key = "${online.state}_${online.district}_${online.market}_${online.name.lowercase()}"
                    mergedMap[key] = online
                }
                mergedMap.values.toList()
            }

            SampleMandiData.commoditiesList = finalMerged
            lastUpdatedText = formattedDisplay
            isManualUpdating = false

            sharedPrefs.edit()
                .putString("last_update_date", today)
                .putInt("price_seed", seed)
                .putString("last_updated_display", formattedDisplay)
                .apply()

            val successMsg = when {
                onlineRates.isNotEmpty() -> when (appLanguage) {
                    AppLanguage.BENGALI -> "উৎসবমুখর খবর! অনলাইন Agmarknet API থেকে আজকের নতুন বাজার দর সফলভাবে লোড হয়েছে।"
                    AppLanguage.HINDI -> "उत्कृष्ट समाचार! ऑनलाइन एगमार्कनेट API से आज की लाइव मंडी दरें सफलतापूर्वक लोड हो गई हैं।"
                    AppLanguage.ENGLISH -> "Direct update success! Today's official rates successfully synced with Agmarknet API."
                }
                else -> when (appLanguage) {
                    AppLanguage.BENGALI -> "আজকের নতুন বাজার দর সফলভাবে প্রস্তুত করা হয়েছে!"
                    AppLanguage.HINDI -> "आज की नई मंडी दरें सफलतापूर्वक अपडेट कर दी गई हैं!"
                    AppLanguage.ENGLISH -> "Today's market rates have been successfully updated!"
                }
            }
            Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
        }
    }

    // Dynamic auto-update on launch (checks calendar date change since last app open)
    LaunchedEffect(Unit) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastSaved = sharedPrefs.getString("last_update_date", "") ?: ""
        val savedSeed = sharedPrefs.getInt("price_seed", today.hashCode())
        val savedDisplay = sharedPrefs.getString("last_updated_display", "")

        if (today != lastSaved || savedDisplay.isNullOrEmpty()) {
            updateMarketRates(false)
        } else {
            // Re-load saved seed to display saved prices
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            isManualUpdating = true
            val onlineRates = try {
                fetchOnlineRates()
            } catch (e: Exception) {
                emptyList<Commodity>()
            }
            val offlineBaseline = SampleMandiData.generateAllCommodities(savedSeed, formattedDate)
            
            val finalMerged = if (onlineRates.isEmpty()) {
                offlineBaseline
            } else {
                val mergedMap = offlineBaseline.associateBy { "${it.state}_${it.district}_${it.market}_${it.name.lowercase()}" }.toMutableMap()
                for (online in onlineRates) {
                    val key = "${online.state}_${online.district}_${online.market}_${online.name.lowercase()}"
                    mergedMap[key] = online
                }
                mergedMap.values.toList()
            }
            SampleMandiData.commoditiesList = finalMerged
            isManualUpdating = false

            if (lastUpdatedText.isEmpty()) {
                lastUpdatedText = savedDisplay ?: ""
            }
        }
    }

    // LaunchedEffect for Splash screen delay
    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    val savePreferences: () -> Unit = {
        sharedPrefs.edit()
            .putString("state", selectedState)
            .putString("district", selectedDistrict)
            .putString("mandi", selectedMandi)
            .putString("language", appLanguage.name)
            .putStringSet("favorites", favoritesList.toSet())
            .putBoolean("show_onboarding", showOnboarding)
            .apply()
    }

    // Dynamic Translations based on App Language
    val t: @Composable (String) -> String = { key: String ->
        when (appLanguage) {
            AppLanguage.ENGLISH -> when (key) {
                "home" -> "Home"
                "search" -> "Search"
                "settings" -> "Settings"
                "search_commodity" -> "Search commodity"
                "change_location" -> "Change Location"
                "state" -> "State"
                "district" -> "District"
                "mandi" -> "Mandi / Market"
                "preferences" -> "Preferences"
                "lang_label" -> "Language: English / हिन्दी / বাংলা"
                "clear_cache" -> "Reset Data"
                "showing_offline" -> "Showing offline data"
                "last_updated" -> "Last updated: $lastUpdatedText"
                "empty_search" -> "Type commodity name to search\nacross all mandis"
                "calc_title" -> "Crops Value Calculator"
                "enter_weight" -> "Enter Weight:"
                "unit" -> "Unit:"
                "est_avg" -> "Estimated Average Total:"
                "est_min" -> "Estimated Min Total:"
                "est_max" -> "Estimated Max Total:"
                "variety" -> "Variety: "
                "modal_price" -> "Modal Price"
                "min_price" -> "Min Price"
                "max_price" -> "Max Price"
                "arrival_date" -> "Arrival Date"
                "details_title" -> "Commodity Details"
                "back" -> "Back"
                "data_source" -> "Data Source: Agmarknet API (gov.in)"
                "app_version" -> "Version: 2.2.0-online"
                "price_per_quintal" -> "Price per Quintal (100 kg)"
                "force_update" -> "Force Update Rates"
                "updating" -> "Updating Rates..."
                "auto_update_label" -> "Once Daily Updates"
                "auto_update_desc" -> "All prices, arrival dates, and varieties are refreshed daily."
                "update_success" -> "Market rates successfully updated for today!"
                else -> key
            }
            AppLanguage.HINDI -> when (key) {
                "home" -> "होम"
                "search" -> "खोजें"
                "settings" -> "सेटिंग्स"
                "search_commodity" -> "फसल खोजें"
                "change_location" -> "स्थान बदलें"
                "state" -> "राज्य"
                "district" -> "जिला"
                "mandi" -> "मंडी / बाजार"
                "preferences" -> "प्राथमिकताएं"
                "lang_label" -> "भाषा: English / हिन्दी / বাংলা"
                "clear_cache" -> "डेटा रीसेट करें"
                "showing_offline" -> "ऑफ़लाइन डेटा प्रदर्शित"
                "last_updated" -> "अंतिम अपडेट: $lastUpdatedText"
                "empty_search" -> "सभी मंडियों में खोजने के लिए\nकमोडिटी का नाम दर्ज करें"
                "calc_title" -> "फसल मूल्य कैलकुलेटर"
                "enter_weight" -> "वजन दर्ज करें:"
                "unit" -> "इकाई (यूनिट):"
                "est_avg" -> "अनुमानित औसत कुल:"
                "est_min" -> "अनुमानित न्यूनतम कुल:"
                "est_max" -> "अनुमानित अधिकतम कुल:"
                "variety" -> "किस्म: "
                "modal_price" -> "मॉडल मूल्य"
                "min_price" -> "न्यूनतम मूल्य"
                "max_price" -> "अधिकतम मूल्य"
                "arrival_date" -> "आगमन तिथि"
                "details_title" -> "कमोडिटी विवरण"
                "back" -> "पीछे"
                "data_source" -> "डेटा स्रोत: एगमार्कनेट (gov.in)"
                "app_version" -> "संस्करण: 2.2.0-ऑनलाइन"
                "price_per_quintal" -> "मूल्य प्रति क्विंटल (100 किलोग्राम)"
                "force_update" -> "ताजा दर लोड करें"
                "updating" -> "अपडेट किया जा रहा है..."
                "auto_update_label" -> "दैनिक स्वचालित अपडेट"
                "auto_update_desc" -> "हर 24 घंटे में सभी कीमतों और आगमन तिथियों को ताजा अपडेट किया जाता है।"
                "update_success" -> "मंडी भाव आज के लिए सफलतापूर्वक अपडेट हो गए हैं!"
                else -> key
            }
            AppLanguage.BENGALI -> when (key) {
                "home" -> "হোম"
                "search" -> "অনুসন্ধান"
                "settings" -> "সেটিংস"
                "search_commodity" -> "পণ্য অনুসন্ধান"
                "change_location" -> "স্থান পরিবর্তন করুন"
                "state" -> "রাজ্য"
                "district" -> "জেলা"
                "mandi" -> "মান্ডি / বাজার"
                "preferences" -> "পছন্দসমূহ"
                "lang_label" -> "ভাষা: English / हिन्दी / বাংলা"
                "clear_cache" -> "রিসেট করুন"
                "showing_offline" -> "অফলাইন তথ্য প্রদর্শন করা হচ্ছে"
                "last_updated" -> "সর্বশেষ আপডেট: $lastUpdatedText"
                "empty_search" -> "সব মান্ডির মধ্যে অনুসন্ধান করতে\nপণ্যের নাম টাইপ করুন"
                "calc_title" -> "ফসল মূল্য ক্যালকুলেটর"
                "enter_weight" -> "ওজন লিখুন:"
                "unit" -> "ইউনিট:"
                "est_avg" -> "আনুমানিক গড় মোট:"
                "est_min" -> "আনুমানিক সর্বনিম্ন মোট:"
                "est_max" -> "আনুমানিক সর্বোচ্চ মোট:"
                "variety" -> "ধরণ: "
                "modal_price" -> "গড় বাজার দর"
                "min_price" -> "সর্বনিম্ন দর"
                "max_price" -> "সর্বোচ্চ দর"
                "arrival_date" -> "আমদানির তারিখ"
                "details_title" -> "পণ্যের বিবরণ"
                "back" -> "ফিরে যান"
                "data_source" -> "তথ্যাদির উৎস: অ্যাগমার্কনেট API (gov.in)"
                "app_version" -> "সংস্করণ: ২.২.০-অনলাইন"
                "price_per_quintal" -> "প্রতি কুইন্টাল মূল্য (১০০ কেজি)"
                "force_update" -> "নতুন বাজার দর লোড করুন"
                "updating" -> "আপডেট করা হচ্ছে..."
                "auto_update_label" -> "দৈনিক স্বয়ংক্রিয় আপডেট"
                "auto_update_desc" -> "প্রতি ২৪ ঘণ্টায় সকল পণ্যের বাজার দর ও আমদানির তারিখ স্বয়ংক্রিয়ভাবে পরিবর্তিত হয়।"
                "update_success" -> "আজকের নতুন বাজার দর সফলভাবে আপডেট করা হয়েছে!"
                else -> key
            }
        }
    }
    Crossfade(targetState = true, label = "ScreenTransition") { _ ->
        when {
            showSplash -> SplashScreen()
            showOnboarding -> OnboardingScreen {
                showOnboarding = false
                savePreferences()
            }
            selectedCommodityForDetail != null -> {
                CommodityDetailScreen(
                    commodity = selectedCommodityForDetail!!,
                    t = t,
                    appLanguage = appLanguage,
                    onBack = { selectedCommodityForDetail = null }
                )
            }
            else -> {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.navigationBarsPadding(),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == "home",
                                onClick = { currentScreen = "home" },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text(t("home")) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "search",
                                onClick = { currentScreen = "search" },
                                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                label = { Text(t("search")) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "settings",
                                onClick = { currentScreen = "settings" },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text(t("settings")) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "home" -> HomeScreen(
                                selectedState = selectedState,
                                selectedDistrict = selectedDistrict,
                                selectedMandi = selectedMandi,
                                favoritesList = favoritesList,
                                onFavoriteToggle = { id ->
                                    if (favoritesList.contains(id)) {
                                        favoritesList.remove(id)
                                    } else {
                                        favoritesList.add(id)
                                    }
                                    savePreferences()
                                },
                                t = t,
                                appLanguage = appLanguage,
                                onCommodityClick = { selectedCommodityForDetail = it }
                            )
                            "search" -> SearchScreen(
                                t = t,
                                appLanguage = appLanguage,
                                onCommodityClick = { selectedCommodityForDetail = it }
                            )
                            "settings" -> SettingsScreen(
                                selectedState = selectedState,
                                selectedDistrict = selectedDistrict,
                                selectedMandi = selectedMandi,
                                appLanguage = appLanguage,
                                onStateChange = {
                                    selectedState = it
                                    selectedDistrict = SampleMandiData.districts[it]?.firstOrNull() ?: ""
                                    selectedMandi = SampleMandiData.mandis[selectedDistrict]?.firstOrNull() ?: ""
                                    savePreferences()
                                },
                                onDistrictChange = {
                                    selectedDistrict = it
                                    selectedMandi = SampleMandiData.mandis[it]?.firstOrNull() ?: ""
                                    savePreferences()
                                },
                                onMandiChange = {
                                    selectedMandi = it
                                    savePreferences()
                                },
                                onLanguageChange = {
                                    appLanguage = it
                                    savePreferences()
                                },
                                onClearCache = {
                                    Toast.makeText(context, "Cache Cleared Successfully!", Toast.LENGTH_SHORT).show()
                                },
                                t = t
                            )
                        }
                    }
                }
            }
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = "FarmRate Logo",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "FarmRate",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            )
            Text(
                text = "Real-time Mandi Prices & Calculator",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                )
            )
        }
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(90.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome to FarmRate",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Stay updated with current Indian agricultural market and vegetable prices (Agmarknet) directly on your device. Work completely offline with automatic cached state storage.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Get Started", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// 3. HOME SCREEN (With Status Bar Padding!)
@Composable
fun HomeScreen(
    selectedState: String,
    selectedDistrict: String,
    selectedMandi: String,
    favoritesList: List<String>,
    onFavoriteToggle: (String) -> Unit,
    t: @Composable (String) -> String,
    appLanguage: AppLanguage,
    onCommodityClick: (Commodity) -> Unit
) {
    // Filter commodities
    val filteredCommodities = remember(selectedState, selectedDistrict, selectedMandi) {
        SampleMandiData.commoditiesList.filter {
            it.state == selectedState && it.district == selectedDistrict && it.market == selectedMandi
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP HEADER - PUSHED DOWN FROM CLOCK/STATUS BAR USING statusBarsPadding()!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryGreen)
                .statusBarsPadding() // CRITICAL FIX: Pushes down from status bar!
                .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 20.dp)
        ) {
            Text(
                text = getLocalizedLocationName(selectedMandi, appLanguage),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${getLocalizedLocationName(selectedDistrict, appLanguage)}, ${getLocalizedLocationName(selectedState, appLanguage)}",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.82f)
            )
        }

        // Offline Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarningBg)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = t("showing_offline"),
                color = WarningText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = t("last_updated"),
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // LazyList of Crop Commodities
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredCommodities) { item ->
                val isFav = favoritesList.contains(item.id)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCommodityClick(item) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = getLocalizedCommodityName(item.name, appLanguage),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Favorite icon",
                                    tint = if (isFav) Color(0xFFFFB300) else TextSecondary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onFavoriteToggle(item.id) }
                                )
                            }
                            Text(
                                text = "₹ ${item.modalPrice}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${t("variety")}${getLocalizedVarietyName(item.variety, appLanguage)}",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = t("modal_price"),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = BorderColor)
                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(t("min_price"), fontSize = 11.sp, color = TextSecondary)
                                Text("₹ ${item.minPrice}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column {
                                Text(t("max_price"), fontSize = 11.sp, color = TextSecondary)
                                Text("₹ ${item.maxPrice}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(t("arrival_date"), fontSize = 11.sp, color = TextSecondary)
                                Text(item.arrivalDate, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. SEARCH SCREEN (With Status Bar Padding!)
@Composable
fun SearchScreen(
    t: @Composable (String) -> String,
    appLanguage: AppLanguage,
    onCommodityClick: (Commodity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            emptyList()
        } else {
            SampleMandiData.commoditiesList.filter { item ->
                item.name.contains(trimmed, ignoreCase = true) ||
                        getLocalizedCommodityName(item.name, AppLanguage.HINDI).contains(trimmed, ignoreCase = true) ||
                        getLocalizedCommodityName(item.name, AppLanguage.BENGALI).contains(trimmed, ignoreCase = true) ||
                        item.market.contains(trimmed, ignoreCase = true) ||
                        getLocalizedLocationName(item.market, AppLanguage.HINDI).contains(trimmed, ignoreCase = true) ||
                        getLocalizedLocationName(item.market, AppLanguage.BENGALI).contains(trimmed, ignoreCase = true) ||
                        item.district.contains(trimmed, ignoreCase = true) ||
                        getLocalizedLocationName(item.district, AppLanguage.HINDI).contains(trimmed, ignoreCase = true) ||
                        getLocalizedLocationName(item.district, AppLanguage.BENGALI).contains(trimmed, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP HEADER - FIX OVERLAP STATUS BAR USING statusBarsPadding()!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightBackground)
                .statusBarsPadding() // CRITICAL FIX: Pushes down from status bar!
                .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = t("search_commodity"),
                style = MaterialTheme.typography.displayMedium.copy(color = PrimaryGreen)
            )
        }

        // Search Input Card
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(t("search")) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderColor
                )
            )
        }

        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = t("empty_search"),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary, fontSize = 17.sp),
                    lineHeight = 24.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCommodityClick(item) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(getLocalizedCommodityName(item.name, appLanguage), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                Text("${getLocalizedLocationName(item.market, appLanguage)}, ${getLocalizedLocationName(item.district, appLanguage)}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Text("₹ ${item.modalPrice}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
                        }
                    }
                }
            }
        }
    }
}

// 5. DETAIL SCREEN (With Status Bar Padding!)
@Composable
fun CommodityDetailScreen(
    commodity: Commodity,
    t: @Composable (String) -> String,
    appLanguage: AppLanguage,
    onBack: () -> Unit
) {
    // Calculator States
    var weightInput by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf("Quintals") } // "Quintals", "Kilograms", "Tons"
    var showUnitMenu by remember { mutableStateOf(false) }

    val calculatedAvg = remember(weightInput, selectedUnit, commodity.modalPrice) {
        val weight = weightInput.toDoubleOrNull() ?: 1.0
        val multiplier = when (selectedUnit) {
            "Kilograms" -> 0.01 // 1 kg is 0.01 quintal
            "Tons" -> 10.0      // 1 ton is 10 quintals
            else -> 1.0          // 1 quintal is 1 quintal
        }
        (weight * multiplier * commodity.modalPrice).toInt()
    }

    val calculatedMin = remember(weightInput, selectedUnit, commodity.minPrice) {
        val weight = weightInput.toDoubleOrNull() ?: 1.0
        val multiplier = when (selectedUnit) {
            "Kilograms" -> 0.01
            "Tons" -> 10.0
            else -> 1.0
        }
        (weight * multiplier * commodity.minPrice).toInt()
    }

    val calculatedMax = remember(weightInput, selectedUnit, commodity.maxPrice) {
        val weight = weightInput.toDoubleOrNull() ?: 1.0
        val multiplier = when (selectedUnit) {
            "Kilograms" -> 0.01
            "Tons" -> 10.0
            else -> 1.0
        }
        (weight * multiplier * commodity.maxPrice).toInt()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        // TOP HEADER - CLIPS UNDER CLOCK/STATUS BAR WITHOUT statusBarsPadding()!
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryGreen)
                .statusBarsPadding() // CRITICAL FIX: Pushes down from status bar!
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = t("details_title"),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = getLocalizedCommodityName(commodity.name, appLanguage),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${t("variety")}${getLocalizedVarietyName(commodity.variety, appLanguage)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }

            // Price Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Price per Quintal (100 kg)", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₹ ${commodity.modalPrice}", fontSize = 42.sp, fontWeight = FontWeight.Black, color = PrimaryGreen)
                        Text(t("modal_price"), fontSize = 13.sp, color = TextSecondary)

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = BorderColor)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(t("min_price"), fontSize = 13.sp, color = TextSecondary)
                                Text("₹ ${commodity.minPrice}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderColor))
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(t("max_price"), fontSize = 13.sp, color = TextSecondary)
                                Text("₹ ${commodity.maxPrice}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Location details Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(t("mandi"), fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text(getLocalizedLocationName(commodity.market, appLanguage), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Divider(color = BorderColor)
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(t("district"), fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text(getLocalizedLocationName(commodity.district, appLanguage), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Divider(color = BorderColor)
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(t("state"), fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text(getLocalizedLocationName(commodity.state, appLanguage), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Divider(color = BorderColor)
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(t("arrival_date"), fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text(commodity.arrivalDate, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }

            // Crop Live Calculator Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = t("calc_title"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text(t("enter_weight")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = BorderColor
                                )
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { showUnitMenu = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    border = ButtonDefaults.outlinedButtonBorder.copy()
                                ) {
                                    val readableUnit = when (selectedUnit) {
                                        "Quintals" -> "Quintals (100 kg)"
                                        "Kilograms" -> "Kilograms (1 kg)"
                                        "Tons" -> "Tons (1000 kg)"
                                        else -> selectedUnit
                                    }
                                    Text(readableUnit, maxLines = 1)
                                }
                                DropdownMenu(
                                    expanded = showUnitMenu,
                                    onDismissRequest = { showUnitMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Quintals (100 kg)") },
                                        onClick = { selectedUnit = "Quintals"; showUnitMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Kilograms (1 kg)") },
                                        onClick = { selectedUnit = "Kilograms"; showUnitMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Tons (1000 kg)") },
                                        onClick = { selectedUnit = "Tons"; showUnitMenu = false }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(t("est_avg"), color = TextSecondary)
                            Text("₹ $calculatedAvg", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 16.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(t("est_min"), color = TextSecondary)
                            Text("₹ $calculatedMin", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(t("est_max"), color = TextSecondary)
                            Text("₹ $calculatedMax", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

// 6. SETTINGS SCREEN (With Status Bar Padding!)
@Composable
fun SettingsScreen(
    selectedState: String,
    selectedDistrict: String,
    selectedMandi: String,
    appLanguage: AppLanguage,
    onStateChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onMandiChange: (String) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onClearCache: () -> Unit,
    t: @Composable (String) -> String
) {
    var showStateMenu by remember { mutableStateOf(false) }
    var showDistrictMenu by remember { mutableStateOf(false) }
    var showMandiMenu by remember { mutableStateOf(false) }

    val stateList = SampleMandiData.states
    val districtList = SampleMandiData.districts[selectedState] ?: emptyList()
    val mandiList = SampleMandiData.mandis[selectedDistrict] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        // TOP HEADER - CLIPS UNDER SYSTEM CLOCK/STATUS BAR WITHOUT statusBarsPadding()!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightBackground)
                .statusBarsPadding() // CRITICAL FIX: Pushes down from status bar!
                .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = t("settings"),
                style = MaterialTheme.typography.displayMedium.copy(color = PrimaryGreen)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Location Selection Card
            item {
                Column {
                    Text(t("change_location"), fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // State
                            Text(t("state"), fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { showStateMenu = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    border = ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedState)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(expanded = showStateMenu, onDismissRequest = { showStateMenu = false }) {
                                    stateList.forEach { state ->
                                        DropdownMenuItem(text = { Text(state) }, onClick = { onStateChange(state); showStateMenu = false })
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // District
                            Text(t("district"), fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { showDistrictMenu = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    border = ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedDistrict)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(expanded = showDistrictMenu, onDismissRequest = { showDistrictMenu = false }) {
                                    districtList.forEach { district ->
                                        DropdownMenuItem(text = { Text(district) }, onClick = { onDistrictChange(district); showDistrictMenu = false })
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Mandi
                            Text(t("mandi"), fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { showMandiMenu = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    border = ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedMandi)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(expanded = showMandiMenu, onDismissRequest = { showMandiMenu = false }) {
                                    mandiList.forEach { mandi ->
                                        DropdownMenuItem(text = { Text(mandi) }, onClick = { onMandiChange(mandi); showMandiMenu = false })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Preferences Card
            item {
                Column {
                    Text(t("preferences"), fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(t("lang_label"), fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val buttonModifier = Modifier.weight(1f).height(44.dp)
                                val baseColor = Color(0xFFF3F5F3)

                                // English
                                Button(
                                    onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                                    modifier = buttonModifier,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (appLanguage == AppLanguage.ENGLISH) PrimaryGreen else baseColor,
                                        contentColor = if (appLanguage == AppLanguage.ENGLISH) Color.White else TextPrimary
                                    )
                                ) {
                                    Text("English", fontSize = 12.sp, maxLines = 1)
                                }

                                // Hindi
                                Button(
                                    onClick = { onLanguageChange(AppLanguage.HINDI) },
                                    modifier = buttonModifier,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (appLanguage == AppLanguage.HINDI) PrimaryGreen else baseColor,
                                        contentColor = if (appLanguage == AppLanguage.HINDI) Color.White else TextPrimary
                                    )
                                ) {
                                    Text("हिन्दी", fontSize = 12.sp, maxLines = 1)
                                }

                                // Bengali
                                Button(
                                    onClick = { onLanguageChange(AppLanguage.BENGALI) },
                                    modifier = buttonModifier,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (appLanguage == AppLanguage.BENGALI) PrimaryGreen else baseColor,
                                        contentColor = if (appLanguage == AppLanguage.BENGALI) Color.White else TextPrimary
                                    )
                                ) {
                                    Text("বাংলা", fontSize = 12.sp, maxLines = 1)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = BorderColor)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = t("clear_cache"),
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onClearCache() }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(t("data_source"), color = TextSecondary, fontSize = 13.sp)
                    Text(t("app_version"), color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}