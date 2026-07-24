package net.minebo.koth.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minebo.cobalt.util.ColorUtil;
import net.minebo.koth.KoTH;
import net.minebo.koth.cobalt.timer.NextKothTimer;
import net.minebo.koth.koth.Koth;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandAlias("koth")
public class KothCommands extends BaseCommand {

    @CatchUnknown
    @HelpCommand
    public void helpCommand(CommandHelp help) {
        help.showHelp();
    }

    @Default
    public void defaultCommand(CommandSender sender) {
        Koth currentKoth = Koth.currentKoth;

        if (currentKoth != null) {
            sender.sendMessage(ColorUtil.translateColors("<gold>Active KoTH: <yellow>" + currentKoth.getName()));
            sender.sendMessage(ColorUtil.translateColors("<yellow>Location: <white>" +
                    currentKoth.getMidPoint().getBlockX() + ", " +
                    currentKoth.getMidPoint().getBlockY() + ", " +
                    currentKoth.getMidPoint().getBlockZ()));
            sender.sendMessage(ColorUtil.translateColors("<yellow>Time Left: <white>" + currentKoth.getRemainingInWords()));
        } else {
            // Check if there's a timer running
            NextKothTimer timer = KoTH.getInstance().getNextKothTimer();
            if (timer != null && timer.isActive()) {

                if (Koth.nextKoth != null) {
                    sender.sendMessage(ColorUtil.translateColors("<gold>Next KoTH: <yellow>" + Koth.nextKoth.getName()));
                    sender.sendMessage(ColorUtil.translateColors("<yellow>Location: <white>" +
                            Koth.nextKoth.getMidPoint().getBlockX() + ", " +
                            Koth.nextKoth.getMidPoint().getBlockY() + ", " +
                            Koth.nextKoth.getMidPoint().getBlockZ()));
                    sender.sendMessage(ColorUtil.translateColors("<yellow>Starting in: <white>" + timer.getRemaining()));
                } else {
                    sender.sendMessage(ColorUtil.translateColors("<red>No KoTH currently active."));
                }
            } else {
                sender.sendMessage(ColorUtil.translateColors("<red>No events are active or scheduled."));
            }
        }
    }

    @Subcommand("create")
    @CommandPermission("koth.admin")
    @Description("Create a new KoTH at your location.")
    @Syntax("<koth>")
    public void createCommand(Player player, String name) {
        // Check if KoTH with that name already exists
        for (Koth koth : Koth.koths) {
            if (koth.getName().equalsIgnoreCase(name)) {
                player.sendMessage(ColorUtil.translateColors("<red>A KoTH with that name already exists!"));
                return;
            }
        }

        Koth newKoth = new Koth();
        newKoth.setName(name);
        newKoth.setMidPoint(player.getLocation());
        newKoth.setCapSize(3); // Default cap size
        newKoth.setCapTime(KoTH.getInstance().getConfig().getInt("koth.default-cap-time", 500));

        if (Koth.koths.isEmpty()) {
            Koth.koths = new ArrayList<>();
        }
        Koth.koths.add(newKoth);
        Koth.saveAll();

        player.sendMessage(ColorUtil.translateColors("<green>Created KoTH <yellow>" + name + " <green>at your location!"));
        player.sendMessage(ColorUtil.translateColors("<gray>Cap Size: <yellow>" + newKoth.getCapSize() + " <gray>Cap Time: <yellow>" + newKoth.getCapTime() + "s"));
    }

    @Subcommand("setmidpoint")
    @CommandPermission("koth.admin")
    @Description("Set the midpoint of a KoTH to your location.")
    @Syntax("<koth>")
    @CommandCompletion("@koths")
    public void setMidpointCommand(Player player, Koth koth) {
        koth.setMidPoint(player.getLocation());
        Koth.saveAll();

        player.sendMessage(ColorUtil.translateColors("<green>Set midpoint of <yellow>" + koth.getName() + " <green>to your location!"));
    }

    @Subcommand("setcapsize")
    @CommandPermission("koth.admin")
    @Description("Set the capture size of a KoTH.")
    @Syntax("<koth> <size>")
    @CommandCompletion("@koths")
    public void setCapSizeCommand(CommandSender sender, Koth koth, Integer size) {
        if (size <= 0) {
            sender.sendMessage(ColorUtil.translateColors("<red>Cap size must be greater than 0!"));
            return;
        }

        koth.setCapSize(size);
        Koth.saveAll();

        sender.sendMessage(ColorUtil.translateColors("<green>Set cap size of <yellow>" + koth.getName() + " <green>to <yellow>" + size + " blocks<green>! "));
    }

    @Subcommand("setcaptime")
    @CommandPermission("koth.admin")
    @Description("Set the capture time of a KoTH.")
    @Syntax("<koth> <time>")
    @CommandCompletion("@koths")
    public void setCapTimeCommand(CommandSender sender, Koth koth, Integer time) {
        if (time <= 0) {
            sender.sendMessage(ColorUtil.translateColors("<red>Cap time must be greater than 0!"));
            return;
        }

        koth.setCapTime(time);
        Koth.saveAll();

        sender.sendMessage(ColorUtil.translateColors("<green>Set cap time of <yellow>" + koth.getName() + " <green>to <yellow>" + time + " seconds<green>!"));
    }

    @Subcommand("setname")
    @CommandPermission("koth.admin")
    @Description("Set the name of a KoTH.")
    @Syntax("<oldName> <newName>")
    @CommandCompletion("@koths")
    public void setNameCommand(CommandSender sender, Koth koth, String newName) {
        String oldName = koth.getName();

        // Check if new name is already taken
        for (Koth k : Koth.koths) {
            if (k.getName().equalsIgnoreCase(newName) && k != koth) {
                sender.sendMessage(ColorUtil.translateColors("<red>A KoTH with that name already exists!"));
                return;
            }
        }

        koth.setName(newName);
        Koth.saveAll();

        sender.sendMessage(ColorUtil.translateColors("<green>Renamed KoTH from <yellow>" + oldName + " <green>to <yellow>" + newName + "<green>!"));
    }

    @Subcommand("list")
    @CommandPermission("koth.admin")
    @Description("List all KoTHs with their data.")
    public void listCommand(CommandSender sender) {
        if (Koth.koths.isEmpty()) {
            sender.sendMessage(ColorUtil.translateColors("<red>There are no KoTHs configured."));
            return;
        }

        Component message = Component.text("Koths: ", NamedTextColor.GOLD);
        boolean first = true;

        for (Koth koth : Koth.koths) {
            if (!first) {
                message = message.append(Component.text(", ", NamedTextColor.GRAY));
            }
            first = false;

            Component hoverText = Component.empty()
                    .append(Component.text(koth.getName(), NamedTextColor.YELLOW))
                    .append(Component.newline())
                    .append(Component.text("Location: ", NamedTextColor.GRAY))
                    .append(Component.text(
                            koth.getMidPoint().getBlockX() + ", " +
                                    koth.getMidPoint().getBlockY() + ", " +
                                    koth.getMidPoint().getBlockZ(),
                            NamedTextColor.WHITE
                    ))
                    .append(Component.newline())
                    .append(Component.text("World: ", NamedTextColor.GRAY))
                    .append(Component.text(koth.getMidPoint().getWorld().getName(), NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("Cap Size: ", NamedTextColor.GRAY))
                    .append(Component.text(koth.getCapSize() + " blocks", NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("Cap Time: ", NamedTextColor.GRAY))
                    .append(Component.text(formatTime(koth.getCapTime()), NamedTextColor.WHITE));

            Component kothEntry = Component.text(koth.getName(), NamedTextColor.YELLOW)
                    .hoverEvent(HoverEvent.showText(hoverText));

            message = message.append(kothEntry);
        }

        sender.sendMessage(message);
    }

    @Subcommand("delete")
    @CommandPermission("koth.admin")
    @Description("Delete a KoTH.")
    @Syntax("<koth>")
    @CommandCompletion("@koths")
    public void deleteCommand(CommandSender sender, Koth koth) {
        // Stop if it's the current koth
        if (Koth.currentKoth == koth) {
            koth.end(null);
        }

        Koth.koths.remove(koth);
        Koth.saveAll();

        sender.sendMessage(ColorUtil.translateColors("<green>Deleted KoTH <yellow>" + koth.getName() + "<green>!"));
    }

    @Subcommand("start")
    @CommandPermission("koth.admin")
    @Description("Manually start a KoTH.")
    @Syntax("<koth>")
    @CommandCompletion("@koths")
    public void startCommand(CommandSender sender, Koth koth) {
        if (Koth.currentKoth != null) {
            sender.sendMessage(ColorUtil.translateColors("<red>A KoTH is already active! Use /koth stop first."));
            return;
        }

        if(KoTH.getInstance().autoKothTimer.isActive()) {
            KoTH.getInstance().cancelAutoKoth();
        }

        koth.start();
        sender.sendMessage(ColorUtil.translateColors("<green>Started KoTH <yellow>" + koth.getName() + "<green>!"));
    }

    @Subcommand("tp|teleport")
    @CommandPermission("koth.admin")
    @Description("Teleport to a KoTH.")
    @Syntax("<koth>")
    @CommandCompletion("@koths")
    public void teleportCommand(Player sender, Koth koth) {
        sender.teleport(koth.getMidPoint());
        sender.sendMessage(ColorUtil.translateColors("<green>Teleported to <yellow>" + koth.getName() + "<green>!"));
    }

    @Subcommand("stop")
    @CommandPermission("koth.admin")
    @Description("Manually stop the active KoTH.")
    public void stopCommand(CommandSender sender) {
        Koth currentKoth = Koth.currentKoth;

        if (currentKoth == null) {
            sender.sendMessage(ColorUtil.translateColors("<red>No KoTH is currently active!"));
            return;
        }

        String kothName = currentKoth.getName();
        currentKoth.end(null);
        sender.sendMessage(ColorUtil.translateColors("<green>Stopped KoTH <yellow>" + kothName + "<green>!"));
    }

    @Subcommand("toggle|auto")
    @CommandPermission("koth.admin")
    @Description("Toggle automatic KoTH starting.")
    public void toggleCommand(CommandSender sender) {
        boolean current = KoTH.getInstance().getConfig().getBoolean("auto-koth.enabled", true);
        KoTH.getInstance().getConfig().set("auto-koth.enabled", !current);
        KoTH.getInstance().saveConfig();

        if (!current) {
            sender.sendMessage(ColorUtil.translateColors("<green>Auto KoTH enabled<green>!"));
            // Start the auto koth timer if not already running
            if(Koth.koths.isEmpty()) {
                sender.sendMessage(ColorUtil.translateColors("<red>Auto KoTH cannot be enabled while there are no KoTHs!"));
            }

            KoTH.getInstance().scheduleNextAutoKoth();
        } else {
            sender.sendMessage(ColorUtil.translateColors("<red>Auto KoTH disabled!"));
            // Cancel the auto koth timer
            KoTH.getInstance().cancelAutoKoth();
        }
    }

    private Koth findKoth(String name) {
        for (Koth koth : Koth.koths) {
            if (koth.getName().equalsIgnoreCase(name)) {
                return koth;
            }
        }
        return null;
    }

    private String formatTime(int seconds) {
        if (seconds >= 60) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;

            if (remainingSeconds == 0) {
                return minutes + (minutes == 1 ? " minute" : " minutes");
            } else {
                return minutes + (minutes == 1 ? " minute" : " minutes") +
                        " " + remainingSeconds + (remainingSeconds == 1 ? " second" : " seconds");
            }
        } else {
            return seconds + (seconds == 1 ? " second" : " seconds");
        }
    }
}