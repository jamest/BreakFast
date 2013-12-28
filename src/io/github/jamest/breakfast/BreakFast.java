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

import java.util.Collection;
import java.util.HashMap;

public class BreakFast extends JavaPlugin
        implements Listener
{
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

        getServer().getPluginManager().registerEvents(this, this);
        PluginDescriptionFile pdf = getDescription();
        getLogger().info("version " + pdf.getVersion() + " has been enabled.");
        saveDefaultConfig();
    }

    public void onDisable() {
        PluginDescriptionFile pdf = getDescription();
        getLogger().info("version " + pdf.getVersion() + " has been disabled.");
    }

    private boolean getDropEnabled(String playerName) {
        return this.dropEnabled != null
                && this.dropEnabled.containsKey(playerName)
                && this.dropEnabled.get(playerName);
    }

    private String getEnabledString(Boolean enabled) {
        String msg = enabled
                ? ChatColor.GREEN + "enabled" + ChatColor.WHITE
                : ChatColor.RED + "disabled" + ChatColor.WHITE;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = (Player)sender;
        if(cmd.getName().equalsIgnoreCase("breakfast") || cmd.getName().equalsIgnoreCase("bf")) {
            if(player.hasPermission("breakfast.use")) {
                Boolean enable = !this.bfEnabled.containsKey(player.getName()) || !this.bfEnabled.get(player.getName());
                this.bfEnabled.put(player.getName(), enable);
                String msgEnable = getEnabledString(enable);
                String msgDrop = getEnabledString(getDropEnabled(player.getName()))
                sender.sendMessage(this.nameTag + msgEnable + " with drop " + msgDrop);

            }
            else {
                sender.sendMessage("You do not have sufficient permissions to use " + nameTag);
            }
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("breakfastdrop") || cmd.getName().equalsIgnoreCase("bfd")) {
            Boolean drop = !getDropEnabled(player.getName());
            this.dropEnabled.put(player.getName(),drop);
            String msg = getEnabledString(drop);
            sender.sendMessage(this.nameTag + " drop " + msg);
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
        Boolean drop = getDropEnabled(player.getName());

        if ((e.getAction() == Action.LEFT_CLICK_BLOCK)
                && block.getLocation().getY() > 0.0D
                && block.getType() != Material.BEDROCK
                && player.hasPermission("breakfast.use")) {
            if(drop) {
                block.breakNaturally();
            } else {
                block.setType(Material.AIR);
            }
        }
    }
}