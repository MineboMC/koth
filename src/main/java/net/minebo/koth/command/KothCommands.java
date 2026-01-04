package net.minebo.koth.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import net.minebo.cobalt.util.ColorUtil;
import net.minebo.koth.KoTH;
import net.minebo.koth.cobalt.timer.NextKothTimer;
import net.minebo.koth.koth.Koth;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandAlias("koth")
public class KothCommands extends BaseCommand {

    @Default
    public void defaultCommand(CommandSender sender) {
        Koth currentKoth = Koth.currentKoth;

        if (currentKoth != null) {
            sender.sendMessage(ColorUtil.translateColors("&6Active KoTH: &e" + currentKoth.getName()));
            sender.sendMessage(ColorUtil.translateColors("&eLocation: &f" +
                    currentKoth.getMidPoint().getBlockX() + ", " +
                    currentKoth.getMidPoint().getBlockY() + ", " +
                    currentKoth.getMidPoint().getBlockZ()));
            sender.sendMessage(ColorUtil.translateColors("&eTime Left: &f" + currentKoth.getRemainingInWords()));
        } else {
            // Check if there's a timer running
            NextKothTimer timer = KoTH.getInstance().getNextKothTimer();
            if (timer != null && timer.isActive()) {

                if (Koth.nextKoth != null) {
                    sender.sendMessage(ColorUtil.translateColors("&6Next KoTH: &e" + Koth.nextKoth.getName()));
                    sender.sendMessage(ColorUtil.translateColors("&eLocation: &f" +
                            Koth.nextKoth.getMidPoint().getBlockX() + ", " +
                            Koth.nextKoth.getMidPoint().getBlockY() + ", " +
                            Koth.nextKoth.getMidPoint().getBlockZ()));
                    sender.sendMessage(ColorUtil.translateColors("&eStarting in: &f" + timer.getRemaining()));
                } else {
                    sender.sendMessage(ColorUtil.translateColors("&cNo KoTH currently active."));
                }
            } else {
                sender.sendMessage(ColorUtil.translateColors("&cNo events are active or scheduled."));
            }
        }
    }

    @CatchUnknown
    @HelpCommand
    public void helpCommand(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("create")
    @CommandPermission("koth.admin")
    @Description("Create a new KoTH at your location.")
    @Syntax("<name>")
    public void createCommand(Player player, String name) {
        // Check if KoTH with that name already exists
        for (Koth koth : Koth.koths) {
            if (koth.getName().equalsIgnoreCase(name)) {
                player.sendMessage(ColorUtil.translateColors("&cA KoTH with that name already exists!"));
                return;
            }
        }

        Koth newKoth = new Koth();
        newKoth.setName(name);
        newKoth.setMidPoint(player.getLocation());
        newKoth.setCapSize(3); // Default cap size
        newKoth.setCapTime(KoTH.getInstance().getConfig().getInt("koth.default-cap-time", 500));

        // Add to list (need to make it mutable)
        if (Koth.koths.isEmpty()) {
            Koth.koths = new ArrayList<>();
        }
        Koth.koths.add(newKoth);
        Koth.saveAll();

        player.sendMessage(ColorUtil.translateColors("&aCreated KoTH &e" + name + " &aat your location!"));
        player.sendMessage(ColorUtil.translateColors("&7Cap Size: &e" + newKoth.getCapSize() + " &7Cap Time: &e" + newKoth.getCapTime() + "s"));
    }

    @Subcommand("setmidpoint")
    @CommandPermission("koth.admin")
    @Description("Set the midpoint of a KoTH to your location.")
    @Syntax("<name>")
    @CommandCompletion("@koths")
    public void setMidpointCommand(Player player, String name) {
        Koth koth = findKoth(name);

        if (koth == null) {
            player.sendMessage(ColorUtil.translateColors("&cKoTH not found!"));
            return;
        }

        koth.setMidPoint(player.getLocation());
        Koth.saveAll();

        player.sendMessage(ColorUtil.translateColors("&aSet midpoint of &e" + koth.getName() + " &ato your location!"));
    }

    @Subcommand("setcapsize")
    @CommandPermission("koth.admin")
    @Description("Set the capture size of a KoTH.")
    @Syntax("<name> <size>")
    @CommandCompletion("@koths")
    public void setCapSizeCommand(CommandSender sender, String name, Integer size) {
        Koth koth = findKoth(name);

        if (koth == null) {
            sender.sendMessage(ColorUtil.translateColors("&cKoTH not found!"));
            return;
        }

        if (size <= 0) {
            sender.sendMessage(ColorUtil.translateColors("&cCap size must be greater than 0!"));
            return;
        }

        koth.setCapSize(size);
        Koth.saveAll();

        sender.sendMessage(ColorUtil.translateColors("&aSet cap size of &e" + koth.getName() + " &ato &e" + size + " blocks&a! "));
    }

    @Subcommand("setcaptime")
    @CommandPermission("koth.admin")
    @Description("Set the capture time of a KoTH.")
    @Syntax("<name> <time>")
    @CommandCompletion("@koths")
    public void setCapTimeCommand(CommandSender sender, String name, Integer time) {
        Koth koth = findKoth(name);

        if (koth == null) {
            sender.sendMessage(ColorUtil.translateColors("&cKoTH not found!"));
            return;
        }

        if (time <= 0) {
            sender.sendMessage(ColorUtil.translateColors("&cCap time must be greater than 0!"));
            return;
        }

        koth.setCapTime(time);
        Koth.saveAll();

        sender.sendMessage(ColorUtil.translateColors("&aSet cap time of &e" + koth.getName() + " &ato &e" + time + " seconds&a!"));
    }

    @Subcommand("setname")
    @CommandPermission("koth.admin")
    @Description("Set the name of a KoTH.")
    @Syntax("<oldName> <newName>")
    @CommandCompletion("@koths")
    public void setNameCommand(CommandSender sender, String oldName, String newName) {
        Koth koth = findKoth(oldName);

        if (koth == null) {
            sender.sendMessage(ColorUtil.translateColors("&cKoTH not found!"));
            return;
        }

        // Check if new name is already taken
        for (Koth k : Koth.koths) {
            if (k.getName().equalsIgnoreCase(newName) && k != koth) {
                sender.sendMessage(ColorUtil.translateColors("&cA KoTH with that name already exists!"));
                return;
            }
        }

        koth.setName(newName);
        Koth.saveAll();

        sender.sendMessage(ColorUtil.translateColors("&aRenamed KoTH from &e" + oldName + " &ato &e" + newName + "&a!"));
    }

    @Subcommand("list")
    @CommandPermission("koth.admin")
    @Description("List all KoTHs with their data.")
    public void listCommand(CommandSender sender) {
        if (Koth.koths.isEmpty()) {
            sender.sendMessage(ColorUtil.translateColors("&cNo KoTHs exist!"));
            return;
        }

        sender.sendMessage("");
        sender.sendMessage(ColorUtil.translateColors("&6Koth List:"));

        for (Koth koth : Koth.koths) {
            sender.sendMessage(ColorUtil.translateColors("&e" + koth.getName()));
            sender.sendMessage(ColorUtil.translateColors("  &7Location: &f" +
                    koth.getMidPoint().getBlockX() + ", " +
                    koth.getMidPoint().getBlockY() + ", " +
                    koth.getMidPoint().getBlockZ()));
            sender.sendMessage(ColorUtil.translateColors("  &7World: &f" + koth.getMidPoint().getWorld().getName()));
            sender.sendMessage(ColorUtil.translateColors("  &7Cap Size: &f" + koth.getCapSize() + " blocks"));
            sender.sendMessage(ColorUtil.translateColors("  &7Cap Time: &f" + formatTime(koth.getCapTime())));
            sender.sendMessage("");
        }
    }

    @Subcommand("delete")
    @CommandPermission("koth.admin")
    @Description("Delete a KoTH.")
    @Syntax("<name>")
    @CommandCompletion("@koths")
    public void deleteCommand(CommandSender sender, String name) {
        Koth koth = findKoth(name);

        if (koth == null) {
            sender.sendMessage(ColorUtil.translateColors("&cKoTH not found!"));
            return;
        }

        // Stop if it's the current koth
        if (Koth.currentKoth == koth) {
            koth.end(null);
        }

        Koth.koths.remove(koth);
        Koth.saveAll();

        sender.sendMessage(ColorUtil.translateColors("&aDeleted KoTH &e" + name + "&a!"));
    }

    @Subcommand("start")
    @CommandPermission("koth.admin")
    @Description("Manually start a KoTH.")
    @Syntax("<name>")
    @CommandCompletion("@koths")
    public void startCommand(CommandSender sender, String name) {
        if (Koth.currentKoth != null) {
            sender.sendMessage(ColorUtil.translateColors("&cA KoTH is already active!  Use /koth stop first."));
            return;
        }

        Koth koth = findKoth(name);

        if (koth == null) {
            sender.sendMessage(ColorUtil.translateColors("&cKoTH not found!"));
            return;
        }

        koth.start();
        sender.sendMessage(ColorUtil.translateColors("&aStarted KoTH &e" + koth.getName() + "&a!"));
    }

    @Subcommand("stop")
    @CommandPermission("koth.admin")
    @Description("Manually stop the active KoTH.")
    public void stopCommand(CommandSender sender) {
        Koth currentKoth = Koth.currentKoth;

        if (currentKoth == null) {
            sender.sendMessage(ColorUtil.translateColors("&cNo KoTH is currently active!"));
            return;
        }

        String kothName = currentKoth.getName();
        currentKoth.end(null);
        sender.sendMessage(ColorUtil.translateColors("&aStopped KoTH &e" + kothName + "&a!"));
    }

    @Subcommand("toggle")
    @CommandPermission("koth.admin")
    @Description("Toggle automatic KoTH starting.")
    public void toggleCommand(CommandSender sender) {
        boolean current = KoTH.getInstance().getConfig().getBoolean("auto-koth.enabled", true);
        KoTH.getInstance().getConfig().set("auto-koth.enabled", !current);
        KoTH.getInstance().saveConfig();

        if (! current) {
            sender.sendMessage(ColorUtil.translateColors("&aAuto KoTH &eenabled&a!"));
            // Start the auto koth timer if not already running
            if(Koth.koths.isEmpty()) {
                sender.sendMessage(ColorUtil.translateColors("&cAuto KoTH cannot be enabled while there are no Koths!"));
            }

            KoTH.getInstance().scheduleNextAutoKoth();
        } else {
            sender.sendMessage(ColorUtil.translateColors("&aAuto KoTH &cdisabled&a!"));
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