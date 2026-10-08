# Zmanim & Luach

Today's zmanim and the Hebrew date for the [Mudita Kompakt](https://mudita.com/products/phones/mudita-kompakt/), drawn for its e-ink screen: black on white, nothing animated, a page at a time instead of scrolling, and dotted lines between the rows.

## What it shows

- The Hebrew date, and after shkia the date that has started
- Yom tov, fast days, Rosh Chodesh, Chanukah and sefiras ha'omer
- This week's parsha and the daf yomi
- Fourteen zmanim from alos to Rabbeinu Tam, with candle lighting on Erev Shabbos and Erev Yom Tov
- The next zman to come in bold, and the list opens on its page
- Any day before or after, with the arrows next to the date
- A month view like a wall calendar, by English months or Hebrew months, with both dates in every square
- The whole app in English or in Hebrew, right to left
- Your location from the phone's GPS, or a town in Rockland County, Kiryas Joel, Lakewood or Brooklyn

## How the times are worked out

- Everything is calculated on the phone with the [KosherJava zmanim library](https://github.com/KosherJava/zmanim). The app has no internet permission and never goes online. The only permission it can ask for is location, and only when you choose "My location".
- Netz and shkia are at sea level, the way most luchos in America print them.
- Times are rounded to the safe side: a deadline like sof zman krias shema is shown a minute earlier, and a starting time like tzeis a minute later.
- The results are checked against [Hebcal](https://www.hebcal.com) in the tests (`app/src/test`).
- For halacha l'maaseh, follow your rav and your shul's luach.

## Installing

With [Obtainium](https://github.com/ImranR98/Obtainium), add this address and it will offer every new version as it comes out:

```
https://github.com/michael412133/zmanim-luach
```

Or download the newest APK and sideload it with Mudita Center or ADB:
https://github.com/michael412133/zmanim-luach/releases/latest/download/zmanim-luach.apk

## Building

GitHub Actions tests and builds every push to `main`. Changing the number in `version.txt` (say to `0.2.0`) builds a signed APK and publishes it as release `v0.2.0`, which is what Obtainium picks up.

## Credits

- [KosherJava Zmanim](https://github.com/KosherJava/zmanim), LGPL 2.1
- [Mudita Mindful Design](https://github.com/mudita/MMD), Apache 2.0
- The e-ink approach follows [wander's Kompakt apps](https://wanderthe.dev)
