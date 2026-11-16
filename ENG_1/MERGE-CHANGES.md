# Merge changes

Inputs: ENG_1(5).zip and ENG_1 (2)(1).zip.

Used ENG_1(5).zip as the base, keeping its start screen (Enter), Game Over at zero health, Esc pause/resume and quit, GameEngine integration, separate updateGame method, player-coordinate display and explanatory comments.

Integrated from ENG_1 (2)(1).zip:
- test_map3.png and its Map_Test_3.aseprite source, plus the updated asset list.
- Map.java with static collision bodies for buildings, lakes, bus, bus stop and four map edges.
- Long Boi at (374, 95). Main now creates Map(world) and reads statue coordinates through Map's getters. Removed the old statue body creation from Main so there is only one statue collider.
- T debug shortcut that prints the mouse position in world coordinates.

Conflict decisions:
- Kept the base version's corrected Player health comment and GameEngine spelling. Player behaviour is identical in both inputs.
- Launcher difference was only an ending newline; kept the base file.
- NPC, UI, bread behaviour and build configuration were identical in both inputs.

Files changed relative to ENG_1(5).zip:
- Updated assets/assets.txt
- Added assets/map/test_map3.png
- Added assets/map/Map_Test_3.aseprite
- Updated core/src/main/java/com/eng1/goozers/Main.java
- Updated core/src/main/java/com/eng1/goozers/Map.java

Verification: all eight Java sources compiled successfully against the previously supplied project's bundled dependencies using Java 8 source/target settings. Referenced texture paths exist, menu and pause logic are present, and statue creation occurs in Map only. No graphical play-test was possible in this environment. This ZIP contains source and assets; build the executable with .\gradlew.bat lwjgl3:jar from ENG_1. Run using .\gradlew.bat lwjgl3:run.

Existing behaviour to check in play-testing: ducks still spawn in a circle around the player and chase directly. They can spawn outside the new walls or get blocked by obstacles; obstacle-aware spawning/pathfinding is not implemented by either input.

Generated build output, Gradle caches, Git metadata and machine-specific IDE files were omitted from this clean export. Original ZIPs were not modified.
