package de.themoep.minedown.adventure.plugin.bungee;

/*
 * Copyright (c) 2020 Max Lee (https://github.com/Phoenix616)
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
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MineDownPlugin extends Plugin {

    private static BungeeAudiences audiences;

    private static final Set<String> SUB_COMMANDS = ImmutableSet.of(
            "pong", "send", "broadcast"
    );
    private static final Map<String, Target> TARGETS = new LinkedHashMap<>();
    static {
        TARGETS.put("chat", (sender, receiver, message) -> {
            audiences.sender(receiver).sendMessage(MineDown.parse(message,
                    "sender", sender.getName(),
                    "receiver", receiver.getName()
            ), MessageType.CHAT);
        });
        TARGETS.put("system", (sender, receiver, message) -> {
            audiences.sender(receiver).sendMessage(MineDown.parse(message,
                    "sender", sender.getName(),
                    "receiver", receiver.getName()
            ), MessageType.SYSTEM);
        });
        TARGETS.put("actionbar", (sender, receiver, message) -> {
            if (receiver instanceof ProxiedPlayer) {
                audiences.player((ProxiedPlayer) receiver).sendActionBar(MineDown.parse(message,
                        "sender", sender.getName(),
                        "receiver", receiver.getName()
                ));
            } else {
                audiences.sender(receiver).sendMessage(MineDown.parse("Actionbar: " + message,
                        "sender", sender.getName(),
                        "receiver", receiver.getName()
                ));
            }
        });
        TARGETS.put("title", (sender, receiver, message) -> {
            String subTitleMessage = "";
            int subTitleIndex = message.indexOf("{SUBTITLE}");
            if (subTitleIndex > -1) {
                subTitleMessage = message.substring(subTitleIndex + "{SUBTITLE}".length());
                message = message.substring(0, subTitleIndex);
            }
            if (receiver instanceof ProxiedPlayer) {
                Component title = TextComponent.empty();
                if (!message.isEmpty()) {
                    title = MineDown.parse(message,
                            "sender", sender.getName(),
                            "receiver", receiver.getName()
                    );
                }
                Component subTitle = TextComponent.empty();
                if (!subTitleMessage.isEmpty()) {
                    subTitle = MineDown.parse(subTitleMessage,
                            "sender", sender.getName(),
                            "receiver", receiver.getName()
                    );
                }
                audiences.player((ProxiedPlayer) receiver).showTitle(Title.of(title, subTitle));
            } else {
                audiences.sender(sender).sendMessage(MineDown.parse("Title: "+ message,
                        "sender", sender.getName(),
                        "receiver", receiver.getName()
                ));
                audiences.sender(sender).sendMessage(MineDown.parse("Subtitle: "+ subTitleMessage,
                        "sender", sender.getName(),
                        "receiver", receiver.getName()
                ));
            }
        });
    }

    @Override
    public void onEnable() {
        audiences = BungeeAudiences.create(this);
        getProxy().getPluginManager().registerCommand(this, new MineDownCommand());
    }

    private boolean onCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || !SUB_COMMANDS.contains(args[0].toLowerCase())) {
            return false;
        } else if (!testPermission(sender, "minedown.command." + args[0].toLowerCase())) {
            return false;
        }

        int nextIndex = 1;
        Set<CommandSender> receivers = new HashSet<>();
        if ("pong".equalsIgnoreCase(args[0])) {
            receivers.add(sender);
        } else if ("send".equalsIgnoreCase(args[0]) || "tell".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                return false;
            }
            ProxiedPlayer target = getProxy().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("No player with the name " + args[1] + " online!");
                return false;
            }
            receivers.add(target);
            nextIndex = 2;
        } else if ("broadcast".equalsIgnoreCase(args[0])) {
            receivers.addAll(getProxy().getPlayers());
            receivers.add(sender);
        }

        if (args.length <= nextIndex + 1) {
            return false;
        }

        Target target = TARGETS.get(args[nextIndex].toLowerCase());
        if (target == null) {
            target = TARGETS.get("system");
            nextIndex--;
        }
        if (testPermission(sender, "minedown.command." + args[0].toLowerCase() + "." + args[nextIndex].toLowerCase())) {
            String message = Arrays.stream(args).skip(nextIndex + 1).collect(Collectors.joining(" "));
            for (CommandSender receiver : receivers) {
                target.send(sender, receiver, message);
            }
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

    private class MineDownCommand extends Command implements TabExecutor {
        public MineDownCommand() {
            super("minedown", "minedown.command", "md");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!onCommand(sender, args)) {
                sender.sendMessage("/"+ getName() + " [send <player>|broadcast|pong] chat|system|actionbar|title <message>");
            }
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 0) {
                return SUB_COMMANDS;
            } else if ("send".equalsIgnoreCase(args[0]) && sender.hasPermission("minedown.command.send")) {
                if (args.length == 1) {
                    return getProxy().getPlayers().stream()
                            .map(ProxiedPlayer::getName)
                            .collect(Collectors.toList());
                } else if (args.length == 2) {
                    return TARGETS.keySet();
                }
            } else if (args.length == 1) {
                return TARGETS.keySet();
            }
            return new ArrayList<>();
        }
    }

    private interface Target {
        void send(CommandSender sender, CommandSender receiver, String message);
    }
}
