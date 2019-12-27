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
                if (args.length == 0) {
                    String message = getConfig().getString("messages.usage");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    return false;
                }
                // if there are no kits defined in the config - send them a message saying no kits are defined
                if (kits == null) {
                    String message = getConfig().getString("messages.no_kits");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    return false;
                }
                // if the user uses the lists argument
                if (args[0].equalsIgnoreCase("list")) {
                    StringBuilder availableKits = new StringBuilder();
                    for (String kitName : kits.getKeys(false)) {
                        if (availableKits.length() == 0) { availableKits.append(kitName); continue;}
                        availableKits.append(", ").append(kitName);
                    }
                    player.sendMessage(availableKits.toString());
                    return true;
                }
                // if the kit the user specifies in their argument exists then give them the items
                if (kits.contains(args[0])) {
                    List<String> items = getConfig().getStringList("kits." + args[0] + ".items");
                    for (String item : items) {
                        String[] itemArray = item.split(", ");
                        Material material = Material.getMaterial(itemArray[0]);
                        if (material == null) {
                            String message = getConfig().getString("messages.invalid_item");
                            message = message.replace("{kit}", args[0]);
                            message = message.replace("{material}", itemArray[0]);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            return false;
                        }
                        ItemStack itemStack = new ItemStack(material, Integer.parseInt(itemArray[1]));
                        player.getInventory().addItem(itemStack);
                    }
                    player.sendMessage("You have received your items for kit " + args[0] + ".");
                } else {
                    String message = getConfig().getString("messages.does_not_exist");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
                return true;
            }
        }
        return false;
    }
}
