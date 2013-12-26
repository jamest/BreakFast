package io.github.jamest.breakfast;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class BreakFast extends JavaPlugin
        implements Listener
{
    private Material itemRequired;
    private HashMap<String,Boolean> bfEnabled;
    private HashMap<String,Boolean> dropEnabled;

    private final String nameTag = ChatColor.GREEN+"["+ChatColor.BLUE+"Break"+ChatColor.RED+"Fast"+ChatColor.GREEN+"]"+ChatColor.WHITE;

    public void onEnable()
    {
        if(this.dropEnabled == null) {
            this.dropEnabled = new HashMap<String, Boolean>();
        }

        if(this.bfEnabled == null) {
            this.bfEnabled = new HashMap<String, Boolean>();
        }

        String requiredItem = getConfig().getString("required_item", "AIR");
        getServer().getPluginManager().registerEvents(this, this);
        PluginDescriptionFile pdf = getDescription();
        getLogger().info("version " + pdf.getVersion() + " has been enabled.");
        saveDefaultConfig();
        this.itemRequired = Material.matchMaterial(requiredItem);
    }

    public void onDisable() {
        PluginDescriptionFile pdf = getDescription();
        getLogger().info("version " + pdf.getVersion() + " has been disabled.");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = (Player)sender;
        String itemName = this.itemRequired.name();
        if(cmd.getName().equalsIgnoreCase("breakfast") || cmd.getName().equalsIgnoreCase("bf")) {
            if(player.hasPermission("breakfast.use")) {
                Boolean enable = this.bfEnabled.containsKey(player.getName()) ? !this.bfEnabled.get(player.getName()) : true;
                this.bfEnabled.put(player.getName(), enable);
                String msg = enable ? "enabled!" : "disabled!";
                sender.sendMessage(this.nameTag + msg);

            }
            else {
                sender.sendMessage("You do not have sufficient permissions to use " + nameTag);
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("breakfastdrop") || cmd.getName().equalsIgnoreCase("bfd")) {
            Boolean drop = this.dropEnabled.containsKey(player.getName()) ? !this.dropEnabled.get(player.getName()) : true;
            this.dropEnabled.put(player.getName(),drop);
            String msg = drop ? " drop enabled!" : " drop disabled!";
            sender.sendMessage(this.nameTag + msg);
            return true;
        }

        return false;
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(!this.bfEnabled.containsKey(player.getName()) || !this.bfEnabled.get(player.getName())) {
            return;
        }

        World world = player.getWorld();
        Block block = e.getClickedBlock();
        Boolean drop = this.dropEnabled.containsKey(player.getName()) && this.dropEnabled.get(player.getName());

        if ((e.getAction() == Action.LEFT_CLICK_BLOCK)
                && block.getLocation().getY() > 0.0D
                && block.getType() != Material.BEDROCK
                && player.hasPermission("breakfast.use")) {
            if(drop) {
                world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType(),1));
            }
            block.setType(Material.AIR);
        }
    }
}