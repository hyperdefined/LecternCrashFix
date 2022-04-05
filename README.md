# LecternCrashFix
This fixes the new [lectern crash/exploit](https://github.com/Coderx-Gamer/lectern-crash). This bug is fixed on Paper build 276 and above. This is also fixed on [CraftBukkit](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/commits/2542945ef49093f6e1041dd817d6f640b7fc25b5). Make sure you update your Paper or Spigot servers!

This plugin is for servers who do not want to update.

This bug occurs by sending a "quick move" click onto a lectern slot. This fix simply checks if a player sends a "quick move" click in a lectern inventory and cancels it.

Requires [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/).