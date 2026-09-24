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

## Custom entity models (CEM)

Salamander loads OptiFine-format `.jem` models, `.jpm` parts, animations, and model-selection `.properties` from
resource packs. CEM is independent of GeckoLib and Citadel: packs replace models of existing entities; they do not
create new entities or convert arbitrary GeckoLib assets into resource-pack replacements.

The target set covers vanilla 1.7.10 and the corresponding entities in Et Futurum Requiem. Newer Minecraft entities
without a 1.7.10 implementation are deliberately left unmapped. EMF/ETF extensions and animated player models
(such as Fresh Moves) are separate, later work.

| Targets | Coverage |
| --- | --- |
| Vanilla living entities | All CEM mob targets, including giant, dragon, horse variants, and native wool, saddle, armor, collar, slime, and charge layers |
| Vanilla vehicles and projectiles | Boat, minecart variants, end crystal, arrow, wither skull, and lead knot |
| Vanilla blocks | Single/double/trapped/ender chests, signs, five skull types, enchanting books, and beds |
| Et Futurum Requiem | Fox, bee, rabbit, endermite, husk, stray and outer layer, zombie villager, shulker, shulker bullet, armor stand, mooshroom, snow golem, dragon, crystal, tipped arrow, boats/chest boats/rafts/chest rafts, wood signs, banners, shulker boxes, and dyed beds |

Install the original resource-pack ZIPs in the normal pack list. For
[Fresh Animations: Spiders 2.2](https://modrinth.com/resourcepack/fresh-animations-spiders/version/2.2.0), place it above
[Fresh Animations 1.10.5](https://modrinth.com/resourcepack/fresh-animations/version/1.10.5): the extension imports
animation parts from the base pack. Current vanilla texture names are mapped to their 1.7.10 counterparts for
existing targets, respecting resource-pack priority. Legacy villager profession clothes are retained when a modern
pack supplies only the base skin. This does not implement ETF random entity textures.

CEM and associated emissive masks work without Angelica. Settings are in the `cem` category of
`config/salamander/salamander.cfg`; reload resources after changing model or emissive settings. Removing the models
or disabling CEM restores native rendering. Malformed models fall back to the native renderer and produce a
resource-specific log message. Beds use client rendering proxies; they do not add tile entities to saved worlds.

The feature works without Salamander on the server. When both sides support its optional CEM channel, the server
supplies aggression and melee-attack signals. Otherwise the pack uses client-observable inputs; attack-specific
animation can be less accurate. Disable synchronization with `cem.serverAnimationSignals`. It does not alter AI
or combat. Animation inputs or rule facts for mechanics absent from 1.7.10, such as glowing entities or villager
trade levels, retain their unavailable/default values.

Other backports can opt into an existing mapping with `CemTargets.register(entityClassName, targetName)` when they
use a supported native model. Custom model classes need a `CemModelParts` binding and a scoped `CemRuntime.begin` /
`end` render call. Mapping is by exact class so unrelated mod models are not silently treated as vanilla models.

`./gradlew build` runs the unit tests on the build JVM and Java 8. To include the original-pack regression tests,
set `SALAMANDER_CEM_PACK_DIR` to a directory containing `FreshAnimations_v1.10.5.zip` and `FA+Spiders-v2.2.zip`.
These optional tests exercise all 114 Fresh Animations models, its model-selection rules, and the spider
extension across animation frames. The packs are downloaded separately and are not bundled with Salamander.

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
