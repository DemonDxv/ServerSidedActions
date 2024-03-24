**A plugin aimed at making specific client actions "server side".**

What it does?:
It makes certain player actions "server side" so that the client "cannot" manipulate them as easily, certain actions some anticheats don't pick up on (for some reason)

It basically cancels and blocks quick actions in-game. The plugin is very basic at the moment.

Actions:
- Regenerating health too quickly. (regen/timer)
- Shooting arrows too quickly (fastbow/timer)
- Removing fire from the player too quickly (antifire/timer)

Required Dependency:
- PacketEvents 2.0

Tested Version(s): (The quick regeneration of hearts on 1.9+ servers will have issues)
- Spigot 1.8.8

Todo:
- Server side potion effects (Zoot/AntiPotion)
- Fix small fast eat & fast bow bypass when first using the item
- Fix AntiFire working when sending packets a high volumes


(i made this out of boredem)
