package io.github.michael412133.zmanim

/**
 * The choices in the settings, one for each group of zmanim that follow the same opinion.
 * The first opinion in each group is the one used until a different one is chosen.
 */
enum class Group {
    Alos,
    Misheyakir,
    ShemaTefillah,
    Afternoon,
    Tzeis,
    Candles,
    ShabbosEnds,
    FastEnds,
    LevanaStart,
    LevanaEnd,
    ;

    /** The opinions to choose from, in the order the settings list them. */
    val opinions: List<Opinion> get() = Opinion.entries.filter { it.group == this }

    val default: Opinion get() = opinions.first()
}

/** One way of working out a group of zmanim. Every one of them is a calculation KosherJava has. */
enum class Opinion(val group: Group) {
    Alos72(Group.Alos),
    Alos90(Group.Alos),
    Alos16Point1(Group.Alos),
    Alos19Point8(Group.Alos),

    Misheyakir11Point5(Group.Misheyakir),
    Misheyakir11(Group.Misheyakir),
    Misheyakir10Point2(Group.Misheyakir),

    // Krias shema and tefillah, and on Erev Pesach the times for chametz too.
    Mga72(Group.ShemaTefillah),
    Mga16Point1(Group.ShemaTefillah),
    Gra(Group.ShemaTefillah),
    BaalHatanya(Group.ShemaTefillah),

    // Mincha gedola, mincha ketana and plag hamincha.
    AfternoonGra(Group.Afternoon),
    AfternoonMga(Group.Afternoon),
    AfternoonBaalHatanya(Group.Afternoon),

    Tzeis8Point5(Group.Tzeis),
    Tzeis7Point083(Group.Tzeis),
    Tzeis50(Group.Tzeis),

    Candles18(Group.Candles),
    Candles20(Group.Candles),
    Candles22(Group.Candles),
    Candles30(Group.Candles),
    Candles40(Group.Candles),

    // When Shabbos and Yom Tov end, and the earliest candle lighting for a second night of Yom Tov.
    Ends8Point5(Group.ShabbosEnds),
    Ends50(Group.ShabbosEnds),
    Ends60(Group.ShabbosEnds),
    Ends72(Group.ShabbosEnds),

    FastEnds8Point5(Group.FastEnds),
    FastEnds7Point083(Group.FastEnds),
    FastEnds50(Group.FastEnds),

    Levana3Days(Group.LevanaStart),
    Levana7Days(Group.LevanaStart),

    LevanaBetweenMoldos(Group.LevanaEnd),
    Levana15Days(Group.LevanaEnd),
    ;

    /** Minutes before shkia, for the candle lighting choices. */
    val candleMinutes: Int
        get() = when (this) {
            Candles18 -> 18
            Candles20 -> 20
            Candles22 -> 22
            Candles30 -> 30
            Candles40 -> 40
            else -> 0
        }
}

/** Whether the omer is counted "לעומר" or "בעומר". */
enum class OmerWording { La, Ba }

/** The opinion chosen for every group. A group with no choice saved uses its first opinion. */
data class Opinions(private val chosen: Map<Group, Opinion> = emptyMap()) {

    operator fun get(group: Group): Opinion = chosen[group]?.takeIf { it.group == group } ?: group.default

    fun with(opinion: Opinion): Opinions = Opinions(chosen + (opinion.group to opinion))

    companion object {
        val Default = Opinions()
    }
}
