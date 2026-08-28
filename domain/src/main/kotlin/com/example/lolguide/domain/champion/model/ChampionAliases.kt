package com.example.lolguide.domain.champion.model

/**
 * Community nicknames mapped to Data Dragon champion ids.
 *
 * Players do not type Data Dragon ids. The most-searched champions are exactly
 * the ones where the id is wrong: Wukong's id is `MonkeyKing`, Nunu's is
 * `Nunu`, and nobody types `AurelionSol` when `asol` will do. Without this,
 * search fails on the queries people actually make.
 *
 * Keys are lowercase and matched after normalisation.
 */
object ChampionAliases {

    private val aliases: Map<String, String> = mapOf(
        // Ids that do not match the displayed name at all.
        "wukong" to "MonkeyKing",
        "monkey" to "MonkeyKing",
        "mundo" to "DrMundo",
        "drmundo" to "DrMundo",
        "asol" to "AurelionSol",
        "aurelion" to "AurelionSol",
        "j4" to "JarvanIV",
        "jarvan" to "JarvanIV",
        "kogmaw" to "KogMaw",
        "reksai" to "RekSai",
        "chogath" to "Chogath",
        "khazix" to "Khazix",
        "velkoz" to "Velkoz",
        "leblanc" to "Leblanc",
        "belveth" to "Belveth",
        "ksante" to "KSante",
        "renata" to "Renata",
        "nunu" to "Nunu",
        "willump" to "Nunu",
        // Common shorthand.
        "kata" to "Katarina",
        "kat" to "Katarina",
        "yi" to "MasterYi",
        "masteryi" to "MasterYi",
        "tf" to "TwistedFate",
        "ww" to "Warwick",
        "mf" to "MissFortune",
        "gp" to "Gangplank",
        "cait" to "Caitlyn",
        "eve" to "Evelynn",
        "ez" to "Ezreal",
        "heca" to "Hecarim",
        "kass" to "Kassadin",
        "kha" to "Khazix",
        "lb" to "Leblanc",
        "lee" to "LeeSin",
        "leesin" to "LeeSin",
        "liss" to "Lissandra",
        "malz" to "Malzahar",
        "morde" to "Mordekaiser",
        "morg" to "Morgana",
        "naut" to "Nautilus",
        "noc" to "Nocturne",
        "ori" to "Orianna",
        "panth" to "Pantheon",
        "sej" to "Sejuani",
        "shyv" to "Shyvana",
        "trundle" to "Trundle",
        "trynd" to "Tryndamere",
        "voli" to "Volibear",
        "xin" to "XinZhao",
        "xinzhao" to "XinZhao",
        "yorick" to "Yorick",
        "zil" to "Zilean",
        "blitz" to "Blitzcrank",
        "cass" to "Cassiopeia",
        "eli" to "Elise",
        "fiddle" to "Fiddlesticks",
        "gragas" to "Gragas",
        "j" to "JarvanIV",
        "jax" to "Jax",
        "malph" to "Malphite",
        "nid" to "Nidalee",
        "raka" to "Soraka",
        "soraka" to "Soraka",
        "sett" to "Sett",
        "tahm" to "TahmKench",
        "tahmkench" to "TahmKench",
        "yuumi" to "Yuumi",
        "zac" to "Zac",
    )

    /** The champion id for [query], or null if it is not a known nickname. */
    fun resolve(query: String): String? = aliases[query.normalise()]

    /** All aliases pointing at [championId]. Used to explain why a row matched. */
    fun aliasesFor(championId: String): List<String> =
        aliases.filterValues { it == championId }.keys.toList()

    /**
     * Lowercases and strips everything that is not a letter or digit, so
     * "Kog'Maw", "kog maw" and "kogmaw" all collapse to the same key.
     */
    fun String.normalise(): String =
        lowercase().filter { it.isLetterOrDigit() }
}
