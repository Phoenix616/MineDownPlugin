# MineDownAdventurePlugin
A plugin that adds a command to send [MineDown](https://github.com/Phoenix616/MineDown/) formatted
messages on Bukkit or BungeeCord using kyori-adventure adapters!

MineDown is a custom mark up syntax which is loosely based on MarkDown that adds the ability to use the full power of
component messages with the same simplicity as legacy formatting codes. (Which it can still support!)
It also includes a way to directly replace placeholders in the messages, both string based and component based ones!

See this overview of the [MineDown syntax](https://wiki.phoenix616.dev/library/minedown/syntax) to get some idea what's
possible with the commands!

## Commands

The main plugin command is `/minedown` or short `/md`.

The messages can also contain placeholders with the sender or target names.

Here are the possible sub commands:

| Command                                   | Description                                       | Placeholders          |
|-------------------------------------------|---------------------------------------------------|-----------------------|
| `/md pong <target> <message>`             | Send a MineDown formatted message to the sender   | %sender%              |
| `/md send <player>  <target> <message>`   | Send a MineDown formatted message to a player     | %sender% %received%   |
| `/md broadcast  <target> <message>`       | Send a MineDown formatted message to every player | %sender% %received%   |

`<target>` can be chat, system, actionbar or title. Title will use {SUBTITLE} in the message as the indicator that the 
text after it will be shown as the subtitle.

## Permissions

All commands have permissions:

| Permission                            | Description                   |
|---------------------------------------|-------------------------------|
| minedown.command                      | Command permission            |
| minedown.command.pong                 | Pong action permission        |
| minedown.command.send                 | Send action permission        |
| minedown.command.broadcast            | Broadcast action permission   |
| minedown.command.<action>.chat        | Chat target permission        |
| minedown.command.<action>.system      | System target permission      |
| minedown.command.<action>.actionbar   | Actionbar target permission   |
| minedown.command.<action>.title       | Title target permission       |

Both commands and permissions are the same on Bungee or Spigot

## Downloads

Downloads are currently available on the [Minebench.de jenkins](https://ci.minebench.de/job/MineDownAdventurePlugin).

## License

Licensed under the following, MIT license (same as MineDown).

```
Copyright (c) 2019 Max Lee (https://github.com/Phoenix616)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
