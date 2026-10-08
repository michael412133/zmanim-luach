# Notes for Claude (from Michael)

Hi, I'm Michael. I'm continuing work on my custom apps for my Mudita Kompakt phone, which I started in regular Claude chats. Here's everything you need to know. I'm not a programmer, so explain things in plain words, give me exact steps for anything I have to do, and keep answers short and casual (commas instead of lots of short sentences, never use em dashes, never use the word "overall", always include working links, and English is my second language so read my typos generously).

FIRST TASK (done Oct 8 2026): save this whole message as CLAUDE.md in the root of the repo (if the repo already has a CLAUDE.md, merge this into it), commit it to main, and confirm. That way every future session reads it automatically. Then check git log and the latest release so you know exactly where things stand, and wait for my next message.

## THE PHONE
- Mudita Kompakt (North America), MuditaOS K, Android 12 era (AOSP), fully de-Googled: no Google Play Services, no Firebase push, no Google location
- 4.3 inch e-ink screen, about 480x800, black and white with 16 grays
- I sideload apps and keep them updated with Obtainium from GitHub releases
- DuraSpeed (MediaTek background killer) is disabled on my phone, but customers' phones may still have it, so anything with alarms or background work needs a plan for it
- MuditaOS K 2.0 is expected soon and may change things (on-phone app installs, launcher choice)

## HOW MY APPS ARE BUILT
- Kotlin + Jetpack Compose + Mudita's MMD design library (com.mudita:MMD), same approach as the community developer wander (github.com/wanderwildwood)
- Each app has its own public GitHub repo on my account (michael412133). GitHub Actions builds a signed APK on each release, one APK per release with a fixed file name so Obtainium never asks which file to pick
- Design preferences for every app:
  - e-ink first: black on white, no animations, no splash screens, no sliding or fading, pages that turn one at a time instead of smooth scrolling
  - thin dotted dividers between list items, and all text must fit the 4.3 inch screen without getting cut off (English and Hebrew)
  - open straight to the most useful screen, main actions in a bar that's always visible, always a one tap way back to the main screen
  - show which page I'm on (segmented bar on the side, current segment filled black)
  - gray backgrounds and bold text for special or important things instead of colors, small icons or legends
  - main screen simple, extra details one tap away in a pop-up or separate screen
  - every setting opens a pop-up with Cancel and Save, settings include a short How to use guide and an About page
  - choices where people do things differently, with sensible defaults, and let me hide what I don't use
  - swipe sideways to move between items, up and down to change pages
  - full Hebrew option with proper right to left layout when it makes sense
  - only the permissions the app really needs, no internet unless truly needed, data stays on the phone
- How I like to work:
  - use trusted, established libraries and sources for data and calculations, never make anything up, tell me which parts you wrote by hand, and check results against a trusted source
  - before building, give me a clear breakdown of the plan, ask about anything unclear, and wait for my OK
  - after building, tell me in plain words what changed
  - I test every build on my phone and send screenshots
  - you can't see my phone or my Mac from here, so if something needs them, give me exact steps

## RELEASE RULES (learned the hard way, follow them)
- Every push to main runs tests and an unsigned build (Build workflow)
- Changing the number in version.txt on main publishes a signed release tagged v<number> (Release workflow). Version code = major*10000 + minor*100 + patch (0.3.0 = 300). Releases come from version.txt because the session's git proxy has refused tag pushes before
- Push the code first and wait for the Build to pass, then bump version.txt in its own commit, so a failed build never leaves a half-made release
- The Release workflow checks the APK is signed with the app's key before publishing. Never change the signing key or the package id, or updates won't install over the old version and people would lose their saved data
- Releases should go on main. If pushing to main is refused in this session, push your branch, open a pull request and merge it yourself with the GitHub REST API (gh api), then tell me. If that's also refused, tell me exactly which button to click on GitHub
- In past sessions gh only worked through the REST API (gh api ...), not commands like gh release view
- If a session gets cut off mid-work, check git status and git log before starting anything over, never redo finished work
- After a release, download the APK from the release and confirm the version, the permissions and that it's signed with the right key, then give me the direct APK link

## APP 1: ZMANIM & LUACH (built, in use, this repo)
- What it does: zmanim, the Hebrew date and a month calendar for e-ink, fully offline, with my own Hebrew-date events (yahrzeits, birthdays, anniversaries)
- Repo: https://github.com/michael412133/zmanim-luach
- Direct APK (always newest): https://github.com/michael412133/zmanim-luach/releases/latest/download/zmanim-luach.apk
- Package id: io.github.michael412133.zmanim (permanent, never change)
- Signing: repo secret SIGNING_KEY, backup on my Mac (Desktop > Claude Folder > Zmanim & Luach signing key). Certificate SHA-256: f050311f99c7d03079fd9699be98d3c63538192f7176de1a5ef20641890be7e7
- Current version: 0.3.0 (released Oct 8 2026), working well on my phone, GPS confirmed working
- Tech: MMD 1.0.2, KosherJava zmanim 2.5.0, AGP 8.13.1, Kotlin 2.2.21, Compose 1.9.5, Material3 1.4.0, Gradle 8.14.3, minSdk/targetSdk 31 (versions match wander's apps)
- Calculation code (Luach.kt, Opinions.kt, Omer.kt, Events.kt, Months.kt, Places.kt, Paging.kt) is plain Kotlin with 49 unit tests checked against Hebcal. All English and Hebrew wording lives in ui/Strings.kt, and text was measured with the phone's fonts (Lato, Noto Sans Hebrew) against a 320dp wide screen
- Every time and calendar fact comes from KosherJava. Written by hand: rounding, which lines show on which day, the omer nusach and sefira text, the yearly rules for yahrzeits and birthdays (Hebcal's rules), GPS handling
- Permissions: location only, no internet. GPS uses Android's own LocationManager, satellites only, listens up to 5 minutes (Android 12 caps getCurrentLocation at 30 seconds, which broke 0.2.0)
- Settings and events are saved only on the phone (SharedPreferences, backup off). Updating keeps them, uninstalling erases them
- Decisions:
  - sea level sunrise and sunset (no elevation), like most American luchos
  - round to the safe side: start times a minute later, deadlines a minute earlier
  - default town Monsey, 42 towns in sections (Rockland and Orange, NY and NJ, US and Canada, Israel, Europe), Israeli towns use Israel's holiday calendar
  - Hebrew date, parsha and daf always in Hebrew letters, Hebrew mode shows times without AM/PM
  - defaults: Alos 72 min, Misheyakir 11.5°, Shema and Tefillah Magen Avraham 72, Mincha and Plag Gra, Tzeis 8.5°, candles 18 min, Shabbos and Yom Tov end 8.5° (options 50, 60, 72), fast ends 8.5°, Kiddush Levana 3 days to halfway between moldos, Omer לעומר
  - I decided against an upcoming holidays list and a date converter
- Known small issue: on a rare day with Yom Tov, after shkia, an event and a long parsha/daf line all at once, the town line is cut off while the month is open (folding the month shows everything)
- Next: I have a few fixes I'll send you. Later ideas: reminders, a home screen widget, maybe a lock screen line through wander's Glance

## APP 2: KOMPAKT TOOLS (planned, not started, no repo yet)
- A small companion app, NOT a replacement for the Settings app (Android won't let a regular app change most core settings, so a full replacement would keep jumping back to the real Settings)
- Plan: a clean e-ink menu that opens the phone-specific hidden settings screens in one tap (battery optimization, each app's battery and notification page, default apps, developer options and wireless debugging, display and font size, Wi-Fi, mobile data, APN), simple toggles for what an app is allowed to change itself (volume and ringer mode, Do Not Disturb, Bluetooth on/off, screen timeout), and a status section at the top (like "DuraSpeed: off" and which key apps are battery whitelisted)
- Possible extra: one ADB command from my Mac to grant an extra permission so it can flip a few more things
- DuraSpeed's screen won't open on my phone because I disabled it with ADB, which is expected
- Next step: I tell you which settings I actually hunt for, you show me a first layout, then we build. For a new app I'll create an empty repo on GitHub first. Setting up its signing key needs a backup I can save on my Mac, so walk me through that step

## OTHER IDEAS (not started)
- Ring Reminders (simple reminders that ring reliably, wander's Medicine app is a model), Work Hours Switch (forward texts and calls only 8:45 to 6:45), Notification Board, adding dotted dividers to wander's Notes and Messaging apps (I have mockups), Tehillim and Brachos, a setup script for phones I set up for others
- Already solved, don't build: app switcher (I use KompaktX), hidden settings shortcuts (Activity Launcher works for now)
