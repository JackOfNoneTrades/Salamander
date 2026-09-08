# Salamander

![logo](images/logo_small.png)


1.7.10 Minecraft backport of [Geckolib](https://github.com/bernie-g/geckolib) (3, 4, 5), and model-related portions of [Citadel](https://github.com/AlexModGuy/Citadel).

<!--

[![curse](images/badges/curse.png)](https://www.curseforge.com/minecraft/mc-mods/salamander)
[![modrinth](images/badges/modrinth.png)](https://modrinth.com/mod/salamander)
[![67](images/badges/67.png)](https://67.fentanylsolutions.org/mod/salamander)
-->

[![hub](images/badges/github.png)](https://github.com/JackOfNoneTrades/Salamander/releases)
[![maven](images/badges/maven.png)](https://maven.fentanylsolutions.org/#/releases/org/fentanylsolutions/salamander/Salamander)
[![cord](images/badges/cord.png)](https://discord.gg/xAWCqGrguG)
![forge](images/badges/forge.png)

## Dependencies

* [GTNHLib](https://github.com/GTNewHorizons/GTNHLib) [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/gtnhlib) [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/gtnhlib) [![git](images/icons/git.png)](https://github.com/GTNewHorizons/GTNHLib/releases)
* [UniMixins](https://github.com/LegacyModdingMC/UniMixins) [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/unimixins) [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/unimixins/versions) [![git](images/icons/git.png)](https://github.com/LegacyModdingMC/UniMixins/releases)

## Building

`./gradlew build`

### Renderer regression checks

`build` runs the unit suite on both the build JVM and Java 8. The GL buffer tests use LWJGL 2's real
buffer-size guard without needing a display. Current-color query buffers must hold **16 floats**, even
though RGBA uses only four: legacy `glGetFloat` checks the maximum possible result size before calling
OpenGL. This applies to snapshots, entity/replaced-entity rendering and armor color queries.

For release testing, also launch a packaged client with Java 8 and LWJGL 2.9.4, render modeled items in
an actual inventory, and verify that snapshot capture/restore preserves all four color channels without
GL errors. Repeat with lwjgl3ify; a successful LWJGL 3 run alone cannot catch LWJGL 2 buffer contracts.

## Debug models

Set `debug.debugMode=true` in `config/salamander/salamander.cfg` on the client and server, then restart. The sampler cycles through the creeper, bat, render-layer, NPC, magma-spider, and jester models when right-clicked; sneak-right-click to go backward.

* `/summon salamander.debug_model ~ ~ ~`
* `/summon salamander.debug_citadel_fly ~ ~ ~`
* `/summon salamander.debug_citadel_dragon ~ ~ ~`

## Credits

* The [GeckoLib](https://github.com/bernie-g/geckolib) contributors
* Citadel compatibility code is derived from [Citadel](https://github.com/AlexModGuy/Citadel) and is licensed under LGPLv3
* Debug fixtures use assets from GeckoLib Unofficial, Alex's Mobs, and Ice and Fire under their original licenses
* The [GTNewHorizons](https://github.com/GTNewHorizons) tooling

## License

`MIT`, except the Citadel-derived compatibility code and identified debug fixtures (`LGPLv3`)

## Buy me creatine

* [ko-fi.com](https://ko-fi.com/jackisasubtlejoke)
* Monero: `893tQ56jWt7czBsqAGPq8J5BDnYVCg2tvKpvwTcMY1LS79iDabopdxoUzNLEZtRTH4ewAcKLJ4DM4V41fvrJGHgeKArxwmJ`

<br>

![license](images/license_small.png)
