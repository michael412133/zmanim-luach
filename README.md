# Zmanim & Luach

Today's zmanim and the Hebrew date for the [Mudita Kompakt](https://mudita.com/products/phones/mudita-kompakt/), drawn for its e-ink screen: black on white, nothing animated, a page at a time instead of scrolling, and dotted lines between the rows.

## What it shows

- The month on top and the day under it; swipe up to fold the month into its week and page through the day's times, and swipe sideways to change the day or the month
- The Hebrew date, and after shkia the date that has started
- Yom Tov, fasts, Rosh Chodesh and Chanukah in a black box, and grey squares for them in the month
- This week's parsha and the daf yomi
- The day's zmanim from alos to chatzos halaila, with the next one in bold
- A star next to a time with other opinions; tap it to see every opinion with its time
- Grey lines on the days that have them: candle lighting, when Shabbos or Yom Tov ends, when a fast begins and ends, the chametz times on Erev Pesach, Kiddush Levana, the molad on Shabbos Mevarchim, and tonight's sefiras haomer (tap it for the nusach and the sefira)
- Your own yahrzeits, birthdays, anniversaries and other events, on the Hebrew or the English date, with a dot on their day
- A bar along the bottom: Month, Today, Add and the settings
- In the settings: the opinion each group of zmanim follows, which times to show, the language (English or Hebrew), the month view, your events, and how to use the app
- Your location from the phone's GPS, or one of 42 towns: Rockland and Orange, New York and New Jersey, the US and Canada, Eretz Yisroel and Europe

## How the times are worked out

- Everything is calculated on the phone with the [KosherJava zmanim library](https://github.com/KosherJava/zmanim), every opinion included. The app has no internet permission and never goes online. The only permission it can ask for is location, and only when you choose "My location".
- KosherJava has no rules for yahrzeits and birthdays, so those follow [Hebcal's](https://www.hebcal.com/yahrzeit) (from Reingold and Dershowitz's *Calendrical Calculations*), and the tests check the hard dates against it: 30 Cheshvan, 30 Kislev, Adar, Adar II and 30 Adar I. For a date in Adar the app asks which Adar to use in a year with two.
- The omer's words are the siddur's, put together for each day, and the tests check all 49 against Hebcal's.
- Netz and shkia are at sea level, the way most luchos in America print them.
- Times are rounded to the safe side: a deadline like sof zman krias shema is shown a minute earlier, and a starting time like tzeis a minute later.
- The results are checked against [Hebcal](https://www.hebcal.com) in the tests (`app/src/test`), for every opinion the settings offer.
- For halacha l'maaseh, follow your rav and your shul's luach.

## Installing

With [Obtainium](https://github.com/ImranR98/Obtainium), add this address and it will offer every new version as it comes out:

```
https://github.com/michael412133/zmanim-luach
```

Or download the newest APK and sideload it with Mudita Center or ADB:
https://github.com/michael412133/zmanim-luach/releases/latest/download/zmanim-luach.apk

## Building

GitHub Actions tests and builds every push to `main`. Changing the number in `version.txt` (say to `0.3.0`) builds a signed APK and publishes it as release `v0.3.0`, which is what Obtainium picks up.

## Credits

- [KosherJava Zmanim](https://github.com/KosherJava/zmanim), LGPL 2.1
- [Mudita Mindful Design](https://github.com/mudita/MMD), Apache 2.0
- The e-ink approach follows [wander's Kompakt apps](https://wanderthe.dev), the pop-ups included
- Town coordinates from Wikipedia, and from [GeoNames](https://www.geonames.org) for Beit Shemesh and Petach Tikva
