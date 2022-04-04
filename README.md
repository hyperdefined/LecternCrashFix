# LecternCrashFix
This fixes the new [lectern crash/exploit](https://github.com/Coderx-Gamer/lectern-crash). This bug is fixed on Paper build 276 and above.

This fix is jank but it works. The client isn't going to "click" on the lectern inventory normally, so this just detects any clicks sent and cancels them.

Requires [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/).