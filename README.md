# CobblemonMegas
This mod is server-side only, and adds game-accurate Mega Evolution battle mechanics to Cobblemon, as well as out-of-battle Mega Evolution.

Mega stones are required for mega evolution, and can be set up with custom textures using a resource pack.

Clicking the mega evolve button in the move select screen, or right clicking with a key stone item can be used to mega evolve in battle.

Mega form models for Pokémon can also be setup as described below.

The plan for this fork is to upgrade the Fabric version to Cobblemon 1.6.0 and fix some bugs.

This is a targeted effort to help the migration of our server to Cobblemon 1.6.0. We will discuss later if we will continue development or use Flourish. If we decide to drop the maintenance after the migration, I will archive the repo and notify accordingly.

Issues will be review in a best-effort basis, contributions are welcome.

Discord Support Channel: https://discord.com/channels/934267676354834442/1328350851747807326


Commands:

---
**/megaevolve** (Permission: "selfdot.megas.megaevolve")
- Can only be used in battle.
- Targets your active battle pokémon.
- Sets the pokémon to mega evolve that turn, if a move is used.
- Player must not have used mega evolution previously that battle.
---
**/megaevolve &lt;partySlot&gt;** (Permission: "selfdot.megas.megaevolveslot")
- Toggles mega-form of pokémon.
- Can be used in or out of battle.
- If used in battle it must target your active pokémon, and will function the same as /megaevolve
---
**/getmegastone &lt;megaStone&gt;** (Permission: "selfdot.megas.getmegastone")
- Gives the player the specified mega stone.
- Will only suggest/work on whitelisted mega stones.
---
**/givemegastone &lt;megaStone&gt; &lt;player&gt;** (Permission: "selfdot.megas.givemegastone")
- Gives the player the specified mega stone.
- Will only suggest/work on whitelisted mega stones.
---
**/givekeystone &lt;player&gt;** (Permission: "selfdot.megas.givekeystone")
- Gives the player a key stone.
---
**/megas reload** (Permission: "selfdot.megas.reload")
- Reloads config file.
---
**/migratestone**
- If the player is holding a mega stone from AaronMusk's raid mod, converts it into a mega stone for this mod.
---

Config (Located at "config/cobblemonmegas/config.json"):

- "megaStoneWhitelist" controls which mega stones are obtainable and usable (will default to all mega stones whitelisted when first starting server).
- "megaRayquazaAllowed" controls whether rayquaza can be mega-evolved (will default to true).

Resource Packs:

For custom mega stone textures, you can use this resource pack: https://www.curseforge.com/minecraft/texture-packs/sanjimegastones

To add models for mega-form pokémon, the model resolver must add the "mega" aspect (or "mega-x" or "mega-y" for Charizard and Mewtwo).