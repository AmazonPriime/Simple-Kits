package me.amazonpriime.simplekits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class SimpleKits extends JavaPlugin {

    private String[][] placeholders = {{"{kit}", ""}, {"{material}", ""}, {"{quantity}", ""}, {"{kit_list}", ""}, {"{command}", ""}};
    private String prefix;
    private ConfigurationSection kits;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        prefix = getConfig().getString("messages.prefix");
        kits = getConfig().getConfigurationSection("kits");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            StringBuilder commandString = new StringBuilder();
            commandString.append(command.getName());
            for (String arg : args) { commandString.append(" ").append(arg); }
            placeholders[4][1] = commandString.toString();
            if (command.getName().equalsIgnoreCase("kit") && player.hasPermission("simplekits.kit")) {
                // if the user does not provide any arguments - send them a message about usage
                if (args.length == 0) { return infoMessage("messages.usage", player); }

                // if there are no kits defined in the config - send them a message saying no kits are defined
                if (kits == null) { return infoMessage("messages.no_kits", player); }

                // if the kit the user specifies in their argument exists then give them the items
                if (kits.contains(args[0]) && player.hasPermission("simplekits.kit." + args[0].toLowerCase())) {
                    placeholders[0][1] = args[0];
                    return redeemKit(args[0], player);
                } else if (!kits.contains(args[0])) {
                    return infoMessage("messages.does_not_exist", player);
                }

                // if none of the other conditionals triggered a return statement it means no permission
                return infoMessage("message.no_permission", player);
            }
            // if the command used it kits then it will display a list of kits
            if (command.getName().equalsIgnoreCase("kits")) {
                if (!player.hasPermission("simplekits.kits")) { return infoMessage("messages.no_permission", player); }
                return listArgument(kits, player);
            }
        }
        return false;
    }

    // helper methods
    private boolean infoMessage(String path, Player player) {
        String message = prefix + " " + getConfig().getString(path);
        for (String[] placeholder_pair : placeholders) {
            message = message.replace(placeholder_pair[0], placeholder_pair[1]);
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return false;
    }

    private boolean listArgument(ConfigurationSection kits, Player player) {
        StringBuilder availKits = new StringBuilder();
        for (String kitName : kits.getKeys(false)) {
            if (player.hasPermission("simplekits.kit." + kitName.toLowerCase())) {
                if (availKits.length() == 0) { availKits.append(kitName); continue; }
                availKits.append(", ").append(kitName);
            }
        }
        placeholders[3][1] = availKits.toString();
        return !infoMessage("messages.kits_list", player);
    }

    private boolean redeemKit(String kit, Player player) {
        List<String> kitItems = getConfig().getStringList("kits." + kit + ".items");
        for (String itemDetails : kitItems) {
            String[] itemArray = itemDetails.split(", ");
            Material item = Material.getMaterial(itemArray[0]);
            placeholders[1][1] = itemArray[0];
            placeholders[2][1] = itemArray[1];
            if (item == null) {
                return infoMessage("messages.invalid_item", player);
            } else {
                player.getInventory().addItem(new ItemStack(item, Integer.parseInt(itemArray[1])));
            }
        }
        return !infoMessage("messages.redeemed", player);
    }
}
