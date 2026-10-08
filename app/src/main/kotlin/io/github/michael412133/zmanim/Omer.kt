package io.github.michael412133.zmanim

import java.text.Normalizer

/**
 * The words of the count, the way the siddur prints them. KosherJava gives the day of the omer
 * as a number; the sentence and the sefira are put together here from the siddur's words, and
 * the tests check all 49 days.
 */
object Omer {

    private const val HAYOM = "הַיּוֹם"
    private const val YOM = "יוֹם"
    private const val YAMIM = "יָמִים"
    private const val SHEHEM = "שֶׁהֵם"
    private const val ASAR = "עָשָׂר"

    /** One to nine, as they are said with "יוֹם" after them (אֶחָד וְעֶשְׂרִים יוֹם). */
    private val ones = listOf(
        "",
        "אֶחָד",
        "שְׁנַיִם",
        "שְׁלשָׁה",
        "אַרְבָּעָה",
        "חֲמִשָּׁה",
        "שִׁשָּׁה",
        "שִׁבְעָה",
        "שְׁמוֹנָה",
        "תִּשְׁעָה",
    )

    private val tens = listOf("", "עֲשָׂרָה", "עֶשְׂרִים", "שְׁלשִׁים", "אַרְבָּעִים")

    private val weeks = listOf(
        "",
        "שָׁבוּעַ אֶחָד",
        "שְׁנֵי שָׁבוּעוֹת",
        "שְׁלשָׁה שָׁבוּעוֹת",
        "אַרְבָּעָה שָׁבוּעוֹת",
        "חֲמִשָּׁה שָׁבוּעוֹת",
        "שִׁשָּׁה שָׁבוּעוֹת",
        "שִׁבְעָה שָׁבוּעוֹת",
    )

    /** The days left over after the weeks, with the "and" in front of them. */
    private val andDays = listOf(
        "",
        "וְיוֹם אֶחָד",
        "וּשְׁנֵי יָמִים",
        "וּשְׁלשָׁה יָמִים",
        "וְאַרְבָּעָה יָמִים",
        "וַחֲמִשָּׁה יָמִים",
        "וְשִׁשָּׁה יָמִים",
    )

    private val sefiros = listOf("חֶסֶד", "גְּבוּרָה", "תִּפְאֶרֶת", "נֶצַח", "הוֹד", "יְסוֹד", "מַלְכוּת")

    private val inSefira = listOf(
        "שֶׁבְּחֶסֶד",
        "שֶׁבִּגְבוּרָה",
        "שֶׁבְּתִפְאֶרֶת",
        "שֶׁבְּנֶצַח",
        "שֶׁבְּהוֹד",
        "שֶׁבִּיסוֹד",
        "שֶׁבְּמַלְכוּת",
    )

    /** The count for one day, 1 to 49, like "הַיּוֹם שְׁמוֹנָה יָמִים, שֶׁהֵם שָׁבוּעַ אֶחָד וְיוֹם אֶחָד לָעֹמֶר". */
    fun count(day: Int, wording: OmerWording): String {
        require(day in 1..49) { "The omer has 49 days, not $day" }
        val number = when (day) {
            1 -> "$YOM ${ones[1]}"
            2 -> "שְׁנֵי $YAMIM"
            in 3..9 -> "${ones[day]} $YAMIM"
            10 -> "${tens[1]} $YAMIM"
            11 -> "אַחַד $ASAR $YOM"
            12 -> "שְׁנֵים $ASAR $YOM"
            in 13..19 -> "${ones[day - 10]} $ASAR $YOM"
            else -> {
                val ten = tens[day / 10]
                val one = day % 10
                when {
                    one == 0 -> "$ten $YOM"
                    // "And" before a sheva is a shuruk: וּשְׁלשִׁים.
                    day / 10 == 3 -> "${ones[one]} וּ$ten $YOM"
                    else -> "${ones[one]} וְ$ten $YOM"
                }
            }
        }
        val sentence = StringBuilder("$HAYOM $number")
        if (day >= 7) {
            sentence.append(", $SHEHEM ").append(weeks[day / 7])
            if (day % 7 != 0) sentence.append(' ').append(andDays[day % 7])
        }
        sentence.append(' ').append(if (wording == OmerWording.Ba) "בָּעֹמֶר" else "לָעֹמֶר")
        return Normalizer.normalize(sentence.toString(), Normalizer.Form.NFC)
    }

    /** The sefira of the day, like "גְּבוּרָה שֶׁבְּחֶסֶד" on the second day. */
    fun sefira(day: Int): String {
        require(day in 1..49) { "The omer has 49 days, not $day" }
        val text = sefiros[(day - 1) % 7] + " " + inSefira[(day - 1) / 7]
        return Normalizer.normalize(text, Normalizer.Form.NFC)
    }
}
