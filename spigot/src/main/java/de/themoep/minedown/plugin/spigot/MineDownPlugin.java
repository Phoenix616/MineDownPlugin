package de.themoep.minedown.plugin.spigot;

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
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class MineDownPlugin extends JavaPlugin {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if ("pong".equalsIgnoreCase(args[0])) {
            if (testPermission(sender, cmd, "minedown.command.pong")) {
                if (args.length < 2) {
                    return false;
                }
                sender.spigot().sendMessage(MineDown.parse(
                        Arrays.stream(args).skip(1).collect(Collectors.joining(" ")),
                        "sender", sender.getName()
                ));
            }
        } else if ("send".equalsIgnoreCase(args[0])) {
            if (testPermission(sender, cmd, "minedown.command.send")) {
                if (args.length < 3) {
                    return false;
                }
                Player target = getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("No player with the name " + args[1] + " online!");
                    return false;
                }
                BaseComponent[] message = MineDown.parse(
                        Arrays.stream(args).skip(1).collect(Collectors.joining(" ")),
                        "sender", sender.getName(),
                        "player", target.getName()
                );
                target.spigot().sendMessage(message);

                sender.sendMessage("Sent the following message to " + target.getName() + ":");
                sender.spigot().sendMessage(message);
            }
        } else if ("broadcast".equalsIgnoreCase(args[0])) {
            if (testPermission(sender, cmd, "minedown.command.broadcast")) {
                if (args.length < 2) {
                    return false;
                }
                for (Player player : getServer().getOnlinePlayers()) {
                    sender.spigot().sendMessage(MineDown.parse(
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return Arrays.asList("send", "pong", "broadcast");
        } else if ("send".equalsIgnoreCase(args[0]) && sender.hasPermission("minedown.command.send")) {
            return getServer().getOnlinePlayers().stream()
                    .filter(p -> !(sender instanceof Player) || ((Player) sender).canSee(p))
                    .map(HumanEntity::getName)
                    .collect(Collectors.toList());
        }
        return super.onTabComplete(sender, command, alias, args);
    }

    private boolean testPermission(CommandSender sender, Command cmd, String permission) {
        if (!sender.hasPermission(permission)) {
            for (String line : cmd.getPermissionMessage().replace("<permission>", permission).split("\n")) {
                sender.sendMessage(line);
            }
            return false;
        }
        return true;
    }
}
