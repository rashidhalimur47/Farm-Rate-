package com.aistudio.farmrate.data

import kotlinx.serialization.Serializable

@Serializable
data class Commodity(
    val id: String,
    val name: String,
    val variety: String,
    val modalPrice: Int, // ₹ per Quintal (100 kg)
    val minPrice: Int,
    val maxPrice: Int,
    val arrivalDate: String,
    val state: String = "West Bengal",
    val district: String = "Kolkata",
    val market: String = "Kolkata",
    var isFavorite: Boolean = false
)

enum class AppLanguage {
    ENGLISH, HINDI, BENGALI
}

fun getLocalizedCommodityName(name: String, lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.HINDI -> when (name) {
            "Cabbage" -> "पत्ता गोभी"
            "Green Chilli" -> "हरी मिर्च"
            "Jute" -> "जूट (पटसन)"
            "Onion" -> "प्याज"
            "Potato" -> "आलू"
            "Tomato" -> "टमाटर"
            "Rice" -> "चावल"
            "Wheat" -> "गेहूं"
            "Garlic" -> "लहसुन"
            "Ginger" -> "अदरक"
            "Mango" -> "आम"
            "Spinach" -> "पालक"
            "Amaranth" -> "चौलाई"
            "Coriander" -> "धनिया"
            "Fenugreek" -> "मेथी"
            "Cauliflower" -> "फूलगोभी"
            "Aubergine" -> "बैंगन"
            "Lady Finger" -> "भिंडी"
            "Bitter Gourd" -> "करेला"
            "Bottle Gourd" -> "लौकी"
            "Pumpkin" -> "कद्दू"
            "Cucumber" -> "खीरा"
            "Lentils" -> "मसूर दाल"
            "Banana" -> "केला"
            "Papaya" -> "पपीता"
            "Lemon" -> "नींबू"
            "Carrot" -> "गाजर"
            "Radish" -> "मूली"
            "Mint" -> "पुदीना"
            "Mustard Greens" -> "सरसों का साग"
            "Mustard Seeds" -> "सरसों"
            "Chickpeas" -> "चना"
            "Pointed Gourd" -> "परवल"
            "Ridge Gourd" -> "तोरई"
            "Snake Gourd" -> "चिचिंडा"
            "Spiny Gourd" -> "ककोड़ा"
            "Ash Gourd" -> "पेठा"
            "Yardlong Bean" -> "बरबटी"
            "Green Pea" -> "मटर"
            "Broad Beans" -> "सेम"
            "Yam" -> "जिमीकंद"
            "Taro Root" -> "अरबी"
            "Raw Banana" -> "कच्चा केला"
            "Sweet Potato" -> "शकरकंद"
            "Beetroot" -> "चुकंदर"
            "Drumstick" -> "सहजन"
            "Green Papaya" -> "कच्चा पपीता"
            "Green Mango" -> "कच्चा आम"
            "Jackfruit" -> "कटहल"
            "Watermelon" -> "तरबूज"
            "Guava" -> "अमरूद"
            "Pomegranate" -> "अनार"
            "Litchi" -> "लीची"
            "Pineapple" -> "अनानास"
            "Coconut" -> "नारियल"
            "Maize" -> "मक्का"
            "Black Gram" -> "उड़द दाल"
            "Green Gram" -> "मूँग दाल"
            "Red Lentils" -> "मसूर"
            "Pigeon Peas" -> "अरहर दाल"
            "Sesame Seeds" -> "तिल"
            "Turmeric" -> "हल्दी"
            "Black Pepper" -> "काली मिर्च"
            "Cumin Seeds" -> "जीरा"
            "Cardamom" -> "इलायची"
            "Cinnamon" -> "दालचीनी"
            "Cloves" -> "लौंग"
            "Fenugreek Seeds" -> "मेथी दाना"
            "Fennel Seeds" -> "सौंफ"
            "Bay Leaf" -> "तेजपत्ता"
            "Cashew Nut" -> "काजू"
            "Peanut" -> "मूंगफली"
            "Mustard Oil" -> "सरसों का तेल"
            "Paddy" -> "धान"
            "Barley" -> "जौ"
            "Millet" -> "बाजरा"
            "Sorghum" -> "ज्वार"
            "Jamun" -> "जामुन"
            "Apple" -> "सेब"
            "Orange" -> "संतरा"
            "Grapes" -> "अंगूर"
            "Dates" -> "खजूर"
            "Capsicum" -> "शिमला मिर्च"
            "French Beans" -> "फ्रांस बीन"
            "Turnip" -> "शलजम"
            "Broccoli" -> "ब्रोकोली"
            "Mushroom" -> "मशरूम"
            "Sweet Corn" -> "स्वीट कॉर्न"
            "Basella" -> "पोई"
            else -> name
        }
        AppLanguage.BENGALI -> when (name) {
            "Cabbage" -> "বাধাকপি"
            "Green Chilli" -> "কাঁচা লঙ্কা"
            "Jute" -> "পাট"
            "Onion" -> "পেঁয়াজ"
            "Potato" -> "আলু"
            "Tomato" -> "টমেটো"
            "Rice" -> "চাল"
            "Wheat" -> "গম"
            "Garlic" -> "রসুন"
            "Ginger" -> "আদা"
            "Mango" -> "আম"
            "Spinach" -> "পালং শাক"
            "Amaranth" -> "লাল শাক"
            "Coriander" -> "ধনে পাতা"
            "Fenugreek" -> "মেথি শাক"
            "Cauliflower" -> "ফুলকপি"
            "Aubergine" -> "বেগুন"
            "Lady Finger" -> "ঢেঁড়স"
            "Bitter Gourd" -> "উচ্ছে / করলা"
            "Bottle Gourd" -> "লাউ"
            "Pumpkin" -> "মিষ্টি কুমড়ো"
            "Cucumber" -> "শসা"
            "Lentils" -> "মসুর ডাল"
            "Banana" -> "কলা"
            "Papaya" -> "পেঁপে"
            "Lemon" -> "লেবু"
            "Carrot" -> "গাজর"
            "Radish" -> "মূলো"
            "Mint" -> "পুদিনা পাতা"
            "Mustard Greens" -> "সর্ষে শাক"
            "Mustard Seeds" -> "সর্ষে দানা"
            "Chickpeas" -> "ছোলা"
            "Pointed Gourd" -> "পটল"
            "Ridge Gourd" -> "ঝিঙে"
            "Snake Gourd" -> "চিচিঙ্গা"
            "Spiny Gourd" -> "কাঁকরোল"
            "Ash Gourd" -> "চাল কুমড়ো"
            "Yardlong Bean" -> "বরবটি"
            "Green Pea" -> "মটরশুঁটি"
            "Broad Beans" -> "শিম"
            "Yam" -> "ওল"
            "Taro Root" -> "কচু"
            "Raw Banana" -> "কাঁচকলা"
            "Sweet Potato" -> "মিষ্টি আলু"
            "Beetroot" -> "বিট"
            "Drumstick" -> "সজনে ডাঁটা"
            "Green Papaya" -> "কাঁচা পেঁপে"
            "Green Mango" -> "কাঁচা আম"
            "Jackfruit" -> "এঁচোড় / কাঁঠাল"
            "Watermelon" -> "তরমুজ"
            "Guava" -> "পেয়ারা"
            "Pomegranate" -> "বেদানা"
            "Litchi" -> "লিচু"
            "Pineapple" -> "আনারস"
            "Coconut" -> "নারকেল"
            "Maize" -> "ভুট্টা"
            "Black Gram" -> "বিউলির ডাল"
            "Green Gram" -> "মুগ ডাল"
            "Red Lentils" -> "মসুর ডাল"
            "Pigeon Peas" -> "অড়হর ডাল"
            "Sesame Seeds" -> "তিল"
            "Turmeric" -> "হলুদ"
            "Black Pepper" -> "গোলমরিচ"
            "Cumin Seeds" -> "জিরে"
            "Cardamom" -> "এলাচ"
            "Cinnamon" -> "দারুচিনি"
            "Cloves" -> "লবঙ্গ"
            "Fenugreek Seeds" -> "মেথি দানা"
            "Fennel Seeds" -> "মৌরি"
            "Bay Leaf" -> "তেজপাতা"
            "Cashew Nut" -> "কাজুবাদাম"
            "Peanut" -> "চিনাবাদাম"
            "Mustard Oil" -> "সর্ষের তেল"
            "Paddy" -> "ধান"
            "Barley" -> "যব"
            "Millet" -> "বাজরা"
            "Sorghum" -> "জোয়ার"
            "Jamun" -> "কালোজাম"
            "Apple" -> "আপেল"
            "Orange" -> "কমলালেবু"
            "Grapes" -> "আঙুর"
            "Dates" -> "খেজুরে"
            "Capsicum" -> "ক্যাপসিকাম"
            "French Beans" -> "ফ্রেঞ্চ বিন্স"
            "Turnip" -> "শালগম"
            "Broccoli" -> "ব্রকলি"
            "Mushroom" -> "মাশরুম"
            "Sweet Corn" -> "মিষ্টি ভুট্টা"
            "Basella" -> "পুই শাক"
            else -> name
        }
        AppLanguage.ENGLISH -> name
    }
}

fun getLocalizedVarietyName(variety: String, lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.HINDI -> when (variety) {
            "Regular" -> "सामान्य"
            "Local" -> "स्थानीय"
            "TD-5" -> "टीडी-5"
            "Nasik" -> "नासिक"
            "Jyoti" -> "ज्योति"
            "Deshi" -> "देशी"
            "Common" -> "सामान्य"
            "Dara" -> "दारा"
            "Red" -> "लाल"
            "Medium" -> "मध्यम"
            "Agria" -> "एग्रिया"
            "Dussehri" -> "दशहरी"
            "Fresh" -> "ताजा"
            "Long Green" -> "लंबा हरा"
            "Hybrid" -> "हाइब्रिड"
            "Bold" -> "बोल्ड"
            "Pusa" -> "पूसा"
            else -> variety
        }
        AppLanguage.BENGALI -> when (variety) {
            "Regular" -> "সাধারণ"
            "Local" -> "স্থানীয়"
            "TD-5" -> "টিডি-৫"
            "Nasik" -> "নাসিক"
            "Jyoti" -> "জ্যোতি"
            "Deshi" -> "দেশী"
            "Common" -> "সাধারণ"
            "Dara" -> "দারা"
            "Red" -> "লাল"
            "Medium" -> "মাঝারি"
            "Agria" -> "এগ্রিয়া"
            "Dussehri" -> "দশহরি"
            "Fresh" -> "তাজা"
            "Long Green" -> "লম্বা বেগুনী"
            "Hybrid" -> "হাইব্রিড"
            "Bold" -> "বোল্ড"
            "Pusa" -> "পুসা"
            else -> variety
        }
        AppLanguage.ENGLISH -> variety
    }
}

fun getLocalizedLocationName(location: String, lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.HINDI -> when (location) {
            "West Bengal" -> "पश्चिम बंगाल"
            "Maharashtra" -> "महाराष्ट्र"
            "Uttar Pradesh" -> "उत्तर प्रदेश"
            "Punjab" -> "पंजाब"
            "Delhi" -> "दिल्ली"
            "Kolkata" -> "कोलकाता"
            "Howrah" -> "हावड़ा"
            "Nadia" -> "नदिया"
            "Hooghly" -> "हुगली"
            "Mumbai" -> "मुंबई"
            "Pune" -> "पुणे"
            "Nashik" -> "नासिक"
            "Nagpur" -> "नागपुर"
            "Lucknow" -> "लखनऊ"
            "Agra" -> "आगरा"
            "Kanpur" -> "कानपुर"
            "Varanasi" -> "वाराणसी"
            "Amritsar" -> "अमृतसर"
            "Ludhiana" -> "लुधियाना"
            "Jalandhar" -> "जालंधर"
            "North Delhi" -> "उत्तरी दिल्ली"
            "South Delhi" -> "दक्षिणी दिल्ली"
            "Bara Bazar" -> "बड़ा बाजार"
            "Sealdah" -> "शियालदह"
            "Howrah Mandi" -> "हावड़ा मंडी"
            "Ramrajatala" -> "रामराजतला"
            "Krishnanagar" -> "कृष्णनगर"
            "Ranaghat" -> "रानाघाट"
            "Sheoraphuli" -> "श्योराफुली"
            "Chinsurah" -> "चिन्सुराह"
            "Vashi APMC" -> "वाशी एपीएमसी"
            "Kalyan" -> "कल्याण"
            "Pune Fruit & Veg" -> "पुणे फल और सब्जी"
            "Hadapsar" -> "हडपसर"
            "Nashik APMC" -> "नासिक एपीएमसी"
            "Lasalgaon" -> "लासलगांव"
            "Nagpur APMC" -> "नागपुर एपीएमसी"
            "Cotton Market" -> "कॉटन मार्केट"
            "Lucknow APMC" -> "लखनऊ एपीएमसी"
            "Naveen Mandi" -> "नवीन मंडी"
            "Sikandra" -> "सिकंदरा"
            "Fatehabad" -> "फतेहाबाद"
            "Azadpur APMC" -> "आजादपुर एपीएमसी"
            "Keshopur" -> "केशोपुर"
            "Okhla Mandi" -> "ओखला मंडी"
            "Kanpur Grain Market" -> "कानपुर अनाज बाजार"
            "Naveen Galla Mandi" -> "नवीन गल्ला मंडी"
            "Varanasi APMC" -> "वाराणसी एपीएमसी"
            "Choubepur Mandi" -> "चौबेपुर मंडी"
            "Amritsar Grain Market" -> "अमृतसर अनाज बाजार"
            "Mandi Bhagtanwala" -> "मंडी भगतानवाला"
            "Ludhiana New Sabzi Mandi" -> "लुधियाना नई सब्जी मंडी"
            "Gill Road Mandi" -> "गिल रोड मंडी"
            "Jalandhar Maqsudan Mandi" -> "जलंधर मकसूदन मंडी"
            "New Grain Market" -> "नया अनाज बाजार"
            else -> location
        }
        AppLanguage.BENGALI -> when (location) {
            "West Bengal" -> "পশ্চিমবঙ্গ"
            "Maharashtra" -> "মহারাষ্ট্র"
            "Uttar Pradesh" -> "উত্তর প্রদেশ"
            "Punjab" -> "পাঞ্জাব"
            "Delhi" -> "দিল্লি"
            "Kolkata" -> "কলকাতা"
            "Howrah" -> "হাওড়া"
            "Nadia" -> "নদীয়ার"
            "Hooghly" -> "হুগলী"
            "Mumbai" -> "মুম্বই"
            "Pune" -> "পুনে"
            "Nashik" -> "নাসিক"
            "Nagpur" -> "নাগপুর"
            "Lucknow" -> "লখনউ"
            "Agra" -> "আগ্রা"
            "Kanpur" -> "কানপুর"
            "Varanasi" -> "বারাণসী"
            "Amritsar" -> "অমৃতসর"
            "Ludhiana" -> "লুধিয়ানা"
            "Jalandhar" -> "জলন্দর"
            "North Delhi" -> "উত্তর দিল্লি"
            "South Delhi" -> "দক্ষিণ দিল্লি"
            "Bara Bazar" -> "বড়া বাজার"
            "Sealdah" -> "শিয়ালদহ"
            "Howrah Mandi" -> "হাওড়া মান্ডি"
            "Ramrajatala" -> "রামরাজাতলা"
            "Krishnanagar" -> "কৃষ্ণনগর"
            "Ranaghat" -> "রানাঘাট"
            "Sheoraphuli" -> "শেওড়াফুলি"
            "Chinsurah" -> "চুঁচুড়া"
            "Vashi APMC" -> "ভাশি এপিএমসি"
            "Kalyan" -> "কল্যাণ"
            "Pune Fruit & Veg" -> "পুনে ফল ও সবজি"
            "Hadapsar" -> "হাদাপসার"
            "Nashik APMC" -> "নাসিক এপিএমসি"
            "Lasalgaon" -> "লাসালগাঁও"
            "Nagpur APMC" -> "নাগপুর এপিএমসি"
            "Cotton Market" -> "তুলা বাজার"
            "Lucknow APMC" -> "লখনউ এপিএমসি"
            "Naveen Mandi" -> "নবীন মান্ডি"
            "Sikandra" -> "সিকান্দ্রা"
            "Fatehabad" -> "ফাতেহাবাদ"
            "Azadpur APMC" -> "আজাদপুর এপিএমসি"
            "Keshopur" -> "কেশপুর"
            "Okhla Mandi" -> "ওখলা মান্ডি"
            "Kanpur Grain Market" -> "কানপুর শস্য বাজার"
            "Naveen Galla Mandi" -> "নবীন গাল্লা মান্ডি"
            "Varanasi APMC" -> "বারাণসী এপিএমসি"
            "Choubepur Mandi" -> "চৌবেপুর মান্ডি"
            "Amritsar Grain Market" -> "অমৃতসর শস্য বাজার"
            "Mandi Bhagtanwala" -> "মান্ডি ভগতানওয়ালা"
            "Ludhiana New Sabzi Mandi" -> "লুধিয়ানা নতুন সবজি মান্ডি"
            "Gill Road Mandi" -> "গিল রোড মান্ডি"
            "Jalandhar Maqsudan Mandi" -> "জলন্দর মকসুদান মান্ডি"
            "New Grain Market" -> "নতুন শস্য বাজার"
            else -> location
        }
        AppLanguage.ENGLISH -> location
    }
}

// Sample Data to simulate local caching from Agmarknet API (data.gov.in)
object SampleMandiData {
    val states = listOf("West Bengal", "Maharashtra", "Uttar Pradesh", "Punjab", "Delhi")
    
    val districts = mapOf(
        "West Bengal" to listOf("Kolkata", "Howrah", "Nadia", "Hooghly"),
        "Maharashtra" to listOf("Mumbai", "Pune", "Nashik", "Nagpur"),
        "Uttar Pradesh" to listOf("Lucknow", "Agra", "Kanpur", "Varanasi"),
        "Punjab" to listOf("Amritsar", "Ludhiana", "Jalandhar"),
        "Delhi" to listOf("North Delhi", "South Delhi")
    )

    val mandis = mapOf(
        "Kolkata" to listOf("Kolkata", "Bara Bazar", "Sealdah"),
        "Howrah" to listOf("Howrah Mandi", "Ramrajatala"),
        "Nadia" to listOf("Krishnanagar", "Ranaghat"),
        "Hooghly" to listOf("Sheoraphuli", "Chinsurah"),
        "Mumbai" to listOf("Vashi APMC", "Kalyan"),
        "Pune" to listOf("Pune Fruit & Veg", "Hadapsar"),
        "Nashik" to listOf("Nashik APMC", "Lasalgaon"),
        "Nagpur" to listOf("Nagpur APMC", "Cotton Market"),
        "Lucknow" to listOf("Lucknow APMC", "Naveen Mandi"),
        "Agra" to listOf("Sikandra", "Fatehabad"),
        "Kanpur" to listOf("Kanpur Grain Market", "Naveen Galla Mandi"),
        "Varanasi" to listOf("Varanasi APMC", "Choubepur Mandi"),
        "Amritsar" to listOf("Amritsar Grain Market", "Mandi Bhagtanwala"),
        "Ludhiana" to listOf("Ludhiana New Sabzi Mandi", "Gill Road Mandi"),
        "Jalandhar" to listOf("Jalandhar Maqsudan Mandi", "New Grain Market"),
        "North Delhi" to listOf("Azadpur APMC", "Keshopur"),
        "South Delhi" to listOf("Okhla Mandi")
    )

    private data class Template(val name: String, val variety: String, val basePrice: Int)

    private val templates = listOf(
        Template("Cabbage", "Regular", 1100),
        Template("Green Chilli", "Local", 4800),
        Template("Onion", "Nasik", 2200),
        Template("Potato", "Jyoti", 1600),
        Template("Tomato", "Deshi", 2800),
        Template("Rice", "Common", 3800),
        Template("Wheat", "Dara", 2400),
        Template("Garlic", "Medium", 12000),
        Template("Ginger", "Local", 9500),
        Template("Mango", "Dussehri", 4500),
        Template("Spinach", "Local", 1500),
        Template("Amaranth", "Red", 1800),
        Template("Coriander", "Fresh", 2500),
        Template("Fenugreek", "Regular", 2200),
        Template("Cauliflower", "Desi", 2000),
        Template("Aubergine", "Long Green", 1900),
        Template("Lady Finger", "Medium", 2500),
        Template("Bitter Gourd", "Common", 3200),
        Template("Bottle Gourd", "Regular", 1400),
        Template("Pumpkin", "Common", 1200),
        Template("Cucumber", "Hybrid", 1800),
        Template("Lentils", "Bold", 7500),
        Template("Banana", "Regular", 3000),
        Template("Papaya", "Local", 2600),
        Template("Lemon", "Common", 5500),
        Template("Carrot", "Pusa", 2200),
        Template("Radish", "Local", 1500),
        Template("Mint", "Fresh", 3000),
        Template("Mustard Greens", "Regular", 1600),
        Template("Mustard Seeds", "Bold", 6500),
        Template("Chickpeas", "Pusa", 5800),
        Template("Pointed Gourd", "Local", 2400),
        Template("Ridge Gourd", "Local", 2000),
        Template("Snake Gourd", "Common", 1800),
        Template("Spiny Gourd", "Fresh", 3500),
        Template("Ash Gourd", "Common", 1500),
        Template("Yardlong Bean", "Local", 2200),
        Template("Green Pea", "Fresh", 4000),
        Template("Broad Beans", "Medium", 2600),
        Template("Yam", "Deshi", 3200),
        Template("Taro Root", "Local", 2500),
        Template("Raw Banana", "Regular", 1400),
        Template("Sweet Potato", "Deshi", 1800),
        Template("Beetroot", "Common", 2200),
        Template("Drumstick", "Local", 4500),
        Template("Green Papaya", "Regular", 1200),
        Template("Green Mango", "Local", 2000),
        Template("Jackfruit", "Desi", 2600),
        Template("Watermelon", "Regular", 1500),
        Template("Guava", "Common", 3200),
        Template("Pomegranate", "Kabul", 8500),
        Template("Litchi", "Fresh", 6000),
        Template("Pineapple", "Local", 3500),
        Template("Coconut", "Common", 4500),
        Template("Maize", "Hybrid", 1900),
        Template("Black Gram", "Bold", 8200),
        Template("Green Gram", "Regular", 7800),
        Template("Red Lentils", "Medium", 7200),
        Template("Pigeon Peas", "Local", 8500),
        Template("Sesame Seeds", "Common", 9800),
        Template("Turmeric", "Raw", 5200),
        Template("Black Pepper", "Bold", 24000),
        Template("Cumin Seeds", "Regular", 18000),
        Template("Cardamom", "Medium", 140000),
        Template("Cinnamon", "Local", 32000),
        Template("Cloves", "Bold", 65000),
        Template("Fenugreek Seeds", "Regular", 6200),
        Template("Fennel Seeds", "Common", 12000),
        Template("Bay Leaf", "Fresh", 4500),
        Template("Cashew Nut", "W240", 72000),
        Template("Peanut", "Bold", 6800),
        Template("Mustard Oil", "Regular", 14000),
        Template("Paddy", "Common", 2183),
        Template("Barley", "Local", 1950),
        Template("Millet", "Regular", 2350),
        Template("Sorghum", "Common", 2700),
        Template("Jamun", "Local", 4500),
        Template("Apple", "Golden", 9000),
        Template("Orange", "Nagpur", 5500),
        Template("Grapes", "Green", 7000),
        Template("Dates", "Fresh", 12500),
        Template("Capsicum", "Hybrid", 4200),
        Template("French Beans", "Local", 3200),
        Template("Turnip", "Common", 1600),
        Template("Broccoli", "Fresh", 8000),
        Template("Mushroom", "Button", 15000),
        Template("Sweet Corn", "Regular", 2500),
        Template("Basella", "Local", 1200)
    )

    var commoditiesList: List<Commodity> = generateAllCommodities(0, "05/06/2026")

    fun generateAllCommodities(seed: Int = 0, dateStr: String = "05/06/2026"): List<Commodity> {
        val list = mutableListOf<Commodity>()
        var counter = 1
        for ((state, districtList) in districts) {
            for (district in districtList) {
                val mandiList = mandis[district] ?: continue
                for (mandi in mandiList) {
                    for (temp in templates) {
                        val hash = Math.abs(mandi.hashCode() + temp.name.hashCode() + seed)
                        val pctShift = (hash % 21) - 10 // scale -10% to +10%
                        val modalPrice = temp.basePrice + (temp.basePrice * pctShift / 100)
                        
                        val minShift = 10 + (hash % 10) // 10% to 19% below modal
                        val maxShift = 10 + (hash % 11) // 10% to 20% above modal
                        
                        val minPrice = modalPrice - (modalPrice * minShift / 100)
                        val maxPrice = modalPrice + (modalPrice * maxShift / 100)
                        
                        list.add(
                            Commodity(
                                id = "gen_${counter++}",
                                name = temp.name,
                                variety = temp.variety,
                                modalPrice = modalPrice,
                                minPrice = minPrice,
                                maxPrice = maxPrice,
                                arrivalDate = dateStr,
                                state = state,
                                district = district,
                                market = mandi
                            )
                        )
                    }
                }
            }
        }
        return list
    }
}
