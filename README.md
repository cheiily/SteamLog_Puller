# SteamLog_Puller

This is part 1 of a two-part app that will actively query Steam's API and store your session history locally to then offer an on-demand analysis and presentation of statistics like "most played game", cake charts of onlinetime/playtime per hour, graph of playtime in a selectable timeframe, etc.

This part is a CLI application that should be run on pc startup and be left to run in the background in order to poll the history.
The app is capable of tracking multiple users at once, however an important fact to have in mind is that each steam api key has a daily query limit of 100000.
In default mode of operation and a single-user scenario, 17280 calls are used in a 24-hour period. The exact behavior can however be configured in order to maximize history resolution or cut down on issued requests.
The exact behavior is configurable via config.ini. Specific instructions on that will be contained in how_to_configure.txt but for now a glance at the AppConfig class should suffice.

The app currently does not have a JAR build as it is not yet finished.
The remaining work is listed below.
#### TODO:
- [ ] Fix debug appender not saving messages below INFO.
- [ ] Fill out how_to_configure.txt with proper instruction.
- [ ] (maybe) split per-user output into different files (config entry)
