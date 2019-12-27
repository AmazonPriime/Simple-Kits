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

    private String[][] placeholders = {{"{kit}", ""}, {"{material}", ""}, {"{quantity}", ""}, {"{kit_list}", ""}};
    private final String prefix = getConfig().getString("messages.prefix");

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            ConfigurationSection kits = getConfig().getConfigurationSection("kits");
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("kit")) {
                // if the user does not provide any arguments - send them a message about usage
                if (args.length == 0) { return infoMessage("messages.usage", player); }

                // if there are no kits defined in the config - send them a message saying no kits are defined
                if (kits == null) { return infoMessage("messages.no_kits", player); }

                // if the user uses the lists argument
                if (args[0].equalsIgnoreCase("list")) { return listArgument(kits, player, placeholders); }

                // if the kit the user specifies in their argument exists then give them the items
                if (kits.contains(args[0])) {
                    placeholders[0][1] = args[0];
                    return redeemKit(args[0], player, placeholders);
                } else {
                    return infoMessage("messages.does_not_exist", player);
                }
            }
        }
        return false;
    }

    // helper methods
    private boolean infoMessage(String path, Player player) {
        String message = prefix + " " + getConfig().getString(path);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return false;
    }

    private boolean infoMessage(String path, Player player, String[][] placeholders) {
        String message = prefix + " " + getConfig().getString(path);
        for (String[] placeholder_pair : placeholders) {
            message = message.replace(placeholder_pair[0], placeholder_pair[1]);
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return false;
    }

    private boolean listArgument(ConfigurationSection kits, Player player, String[][] placeholders) {
        StringBuilder availKits = new StringBuilder();
        player.sendMessage(kits.getKeys(false).toString());
        for (String kitName : kits.getKeys(false)) {
            if (availKits.length() == 0) { availKits.append(kitName); continue; }
            availKits.append(", ").append(kitName);
        }
        placeholders[3][1] = availKits.toString();
        return !infoMessage("messages.kits_list", player, placeholders);
    }

    private boolean redeemKit(String kit, Player player, String[][] placeholders) {
        List<String> kitItems = getConfig().getStringList("kits." + kit + ".items");
        for (String itemDetails : kitItems) {
            String[] itemArray = itemDetails.split(", ");
            Material item = Material.getMaterial(itemArray[0]);
            placeholders[1][1] = itemArray[0];
            placeholders[2][1] = itemArray[1];
            if (item == null) {
                return infoMessage("messages.invalid_item", player, placeholders);
            } else {
                player.getInventory().addItem(new ItemStack(item, Integer.parseInt(itemArray[1])));
                return !infoMessage("messages.redeemed", player, placeholders);
            }
        }
        return true;
    }
}
