package de.themoep.minedown.plugin.bungee;

/*
 * Copyright (c) 2019 Max Lee (https://github.com/Phoenix616)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import de.themoep.minedown.MineDown;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class MineDownPlugin extends Plugin {

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerCommand(this, new Command("minedown", "minedown.command", "md") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                if (!onCommand(sender, args)) {
                    sender.sendMessage("/"+ getName() + " [send <player>|broadcast|pong] <message>");
                }
            }
        });
    }

    private boolean onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if ("pong".equals(args[0])) {
            if (testPermission(sender, "minedown.command.pong")) {
                if (args.length < 2) {
                    return false;
                }
                sender.sendMessage(MineDown.parse(
                        Arrays.stream(args).skip(1).collect(Collectors.joining(" ")),
                        "sender", sender.getName()
                ));
            }
        } else if ("send".equals(args[0])) {
            if (testPermission(sender, "minedown.command.send")) {
                if (args.length < 3) {
                    return false;
                }
                ProxiedPlayer target = getProxy().getPlayer(args[1]);
                if (target != null) {
                    target.sendMessage(MineDown.parse(
                            Arrays.stream(args).skip(1).collect(Collectors.joining(" ")),
                            "sender", sender.getName(),
                            "player", target.getName()
                    ));
                }
            }
        } else if ("broadcast".equals(args[0])) {
            if (testPermission(sender, "minedown.command.broadcast")) {
                if (args.length < 2) {
                    return false;
                }
                for (ProxiedPlayer player : getProxy().getPlayers()) {
                    sender.sendMessage(MineDown.parse(
                            Arrays.stream(args).skip(1).collect(Collectors.joining(" ")),
                            "sender", sender.getName(),
                            "player", player.getName()
                    ));
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean testPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            for (String line : getProxy().getTranslation("no_permission", permission).split("\n")) {
                sender.sendMessage(line);
            }
            return false;
        }
        return true;
    }
}
