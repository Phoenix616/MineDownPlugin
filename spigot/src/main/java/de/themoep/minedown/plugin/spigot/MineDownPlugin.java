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

import com.google.common.collect.ImmutableSet;
import de.themoep.minedown.MineDown;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MineDownPlugin extends JavaPlugin {
    private static final Set<String> SUB_COMMANDS = ImmutableSet.of(
            "pong", "send", "broadcast"
    );
    private static final Map<String, Target> TARGETS = new LinkedHashMap<>();
    static {
        TARGETS.put("chat", (sender, receiver, message) -> {
            BaseComponent[] components = MineDown.parse(message,
                    "sender", sender.getName(),
                    "receiver", receiver.getName()
            );
            if (receiver instanceof Player) {
                ((Player) receiver).spigot().sendMessage(ChatMessageType.CHAT, components);
            } else {
                receiver.spigot().sendMessage(components);
            }
        });
        TARGETS.put("system", (sender, receiver, message) -> {
            BaseComponent[] components = MineDown.parse(message,
                    "sender", sender.getName(),
                    "receiver", receiver.getName()
            );
            if (receiver instanceof Player) {
                ((Player) receiver).spigot().sendMessage(ChatMessageType.SYSTEM, components);
            } else {
                receiver.spigot().sendMessage(components);
            }
        });
        TARGETS.put("actionbar", (sender, receiver, message) -> {
            if (receiver instanceof Player) {
                ((Player) receiver).spigot().sendMessage(ChatMessageType.ACTION_BAR, MineDown.parse(message,
                        "sender", sender.getName(),
                        "receiver", receiver.getName()
                ));
            } else {
                receiver.spigot().sendMessage(MineDown.parse("Actionbar: " + message,
                        "sender", sender.getName(),
                        "receiver", receiver.getName()
                ));
            }
        });
        TARGETS.put("title", (sender, receiver, message) -> {
            String subTitle = "";
            int subTitleIndex = message.indexOf("{SUBTITLE}");
            if (subTitleIndex > -1) {
                subTitle = message.substring(subTitleIndex + "{SUBTITLE}".length());
                message = message.substring(0, subTitleIndex);
            }
            if (receiver instanceof Player) {
                ((Player) receiver).sendTitle(
                        TextComponent.toLegacyText(MineDown.parse(message,
                                "sender", sender.getName(),
                                "receiver", receiver.getName()
                        )),
                        TextComponent.toLegacyText(MineDown.parse(subTitle,
                                "sender", sender.getName(),
                                "receiver", receiver.getName()
                        )),
                        20,
                        40,
                        20
                );
            } else {
                sender.spigot().sendMessage(MineDown.parse("Title: "+ message,
                        "sender", sender.getName(),
                        "receiver", receiver.getName()
                ));
                sender.spigot().sendMessage(MineDown.parse("Subtitle: "+ subTitle,
                        "sender", sender.getName(),
                        "receiver", receiver.getName()
                ));
            }
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || !SUB_COMMANDS.contains(args[0].toLowerCase())) {
            return false;
        } else if (!testPermission(sender, cmd, "minedown.command." + args[0].toLowerCase())) {
            return false;
        }

        int nextIndex = 1;
        Set<CommandSender> receivers = new HashSet<>();
        if ("pong".equalsIgnoreCase(args[0])) {
            receivers.add(sender);
        } else if ("send".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                return false;
            }
            Player target = getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("No player with the name " + args[1] + " online!");
                return false;
            }
            receivers.add(target);
            nextIndex = 2;
        } else if ("broadcast".equalsIgnoreCase(args[0])) {
            receivers.addAll(getServer().getOnlinePlayers());
            receivers.add(sender);
        }

        if (args.length <= nextIndex + 1) {
            return false;
        }

        Target target = TARGETS.get(args[nextIndex].toLowerCase());
        if (target == null) {
            return false;
        }
        if (testPermission(sender, cmd, "minedown.command." + args[0].toLowerCase() + "." + args[nextIndex].toLowerCase())) {
            String message = Arrays.stream(args).skip(nextIndex + 1).collect(Collectors.joining(" "));
            for (CommandSender receiver : receivers) {
                target.send(sender, receiver, message);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return new ArrayList<>(SUB_COMMANDS);
        } else if ("send".equalsIgnoreCase(args[0]) && sender.hasPermission("minedown.command.send")) {
            if (args.length == 1) {
                return getServer().getOnlinePlayers().stream()
                        .filter(p -> !(sender instanceof Player) || ((Player) sender).canSee(p))
                        .map(HumanEntity::getName)
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                return new ArrayList<>(TARGETS.keySet());
            }
        } else if (args.length == 1) {
            return new ArrayList<>(TARGETS.keySet());
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

    private interface Target {
        void send(CommandSender sender, CommandSender receiver, String message);
    }
}
