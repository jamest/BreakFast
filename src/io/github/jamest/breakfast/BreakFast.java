package io.github.jamest.breakfast;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftSound;
import org.bukkit.craftbukkit.v1_7_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class BreakFast extends JavaPlugin
        implements Listener
{
    private final String permissionString = "breakfast.use";

    private class PlayerSettings implements Serializable {
        public PlayerSettings(Boolean enabled, Boolean drop){
            this.setEnabled(enabled);
            this.setDrop(drop);
        }

        private Boolean drop;
        public Boolean getDrop() {
            return this.drop;
        }
        public void setDrop(Boolean enabled) {
            this.drop = enabled;
        }
        public void toggleDrop() {
            setDrop(!getDrop());
        }

        private Boolean plugin;
        public Boolean getEnabled() {
            return this.plugin;
        }
        public void setEnabled(Boolean enabled) {
            this.plugin = enabled;
        }
        public void toggleEnabled() {
            setEnabled(!getEnabled());
        }
    }

    private Boolean dropEnabledDefault;
    private String name;
    private HashMap<String,Sound> soundMap;

    private final String PluginMetadataKey = "jamest.breakfast.enable";
    private final String DropMetadataKey = "jamest.breakfast.drop";


    private final String NameTag = ChatColor.GREEN+"["+ChatColor.BLUE+"Break"+ChatColor.RED+"Fast"+ChatColor.GREEN+"]"+ChatColor.WHITE;

    public void onEnable()
    {
        if(this.soundMap == null) {
            this.soundMap = new HashMap<String, Sound>();

            try{
                for(Sound sound : Sound.values()) {
                    Field soundField = CraftSound.class.getDeclaredField("sounds");
                    soundField.setAccessible(true);
                    String[] sounds = (String[]) soundField.get(null);

                    this.soundMap.put(sounds[sound.ordinal()],sound);
                }
            } catch(Exception e) {
                getLogger().info("EXCEPTION: " + e.getMessage());
            }
        }
        this.dropEnabledDefault = (Boolean)getConfig().get("defaults.enable-drop");
        getServer().getPluginManager().registerEvents(this, this);
        PluginDescriptionFile pdf = getDescription();
        this.name = pdf.getName();

        getLogger().info("version " + pdf.getVersion() + " has been enabled.");
        saveDefaultConfig();
    }

    public void onDisable() {
        PluginDescriptionFile pdf = getDescription();
        getLogger().info("version " + pdf.getVersion() + " has been disabled.");
    }

    private PlayerSettings getMetadataForPlayer(Player player) {
        List<MetadataValue> enableMetadata = player.getMetadata(this.PluginMetadataKey);

        Boolean enabled = false;
        for(MetadataValue value: enableMetadata) {
            if(value.getOwningPlugin().getDescription().getName().equals(this.name)) {
                enabled = value.asBoolean();
            }
        }

        List<MetadataValue> dropMetadata = player.getMetadata(this.DropMetadataKey);
        Boolean drop = this.dropEnabledDefault;
        for(MetadataValue value: dropMetadata) {
            if(value.getOwningPlugin().getDescription().getName().equals(this.name)) {
                drop = value.asBoolean();
            }
        }

        PlayerSettings playerSettings = new PlayerSettings(enabled, drop);
        return playerSettings;
    }

    private void setMetadataForPlayer(Player player, PlayerSettings playerSettings) {
        player.setMetadata(this.PluginMetadataKey, new FixedMetadataValue(this, playerSettings.getEnabled()));
        player.setMetadata(this.DropMetadataKey, new FixedMetadataValue(this, playerSettings.getDrop()));
    }

    private String getEnabledString(Boolean enabled) {
        String msg = enabled
                ? ChatColor.GREEN + "enabled" + ChatColor.WHITE
                : ChatColor.RED + "disabled" + ChatColor.WHITE;
        return msg;
    }

    private Boolean hasPermission(Permissible p) {
        return p.hasPermission(permissionString);
    }

    private Boolean checkPermissionWithMessage(CommandSender sender) {
        if(hasPermission(sender)) {
            return true;
        }

        sender.sendMessage("You do not have sufficient permissions to use " + this.NameTag);

        return false;
    }

    private void handleBreakFastCommand(CommandSender sender) {
        if(!checkPermissionWithMessage(sender)) {
            return;
        }

        Player player = (Player)sender;
        PlayerSettings settings = getMetadataForPlayer(player);
        if(!settings.getEnabled()) {
            settings.setDrop(this.dropEnabledDefault);
        }
        settings.toggleEnabled();
        setMetadataForPlayer(player, settings);

        String msg = " " + getEnabledString(settings.getEnabled());
        if(settings.getEnabled()) {
            msg += " with drop " + getEnabledString(settings.getDrop());
        }
        sender.sendMessage(this.NameTag + msg);
    }

    private void handleBreakFastDropCommand(CommandSender sender) {
        if(!checkPermissionWithMessage(sender)) {
            return;
        }

        Player player = (Player)sender;
        PlayerSettings settings = getMetadataForPlayer(player);
        settings.toggleDrop();
        setMetadataForPlayer(player, settings);

        String msg = getEnabledString(settings.getDrop());
        sender.sendMessage(this.NameTag + " drop " + msg);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        // handle /BreakFast and /bf
        if(cmd.getName().equalsIgnoreCase("breakfast") || cmd.getName().equalsIgnoreCase("bf")) {
            handleBreakFastCommand(sender);
            return true;
        }

        // handle /BreakFastDrop and /bfd
        if (cmd.getName().equalsIgnoreCase("breakfastdrop") || cmd.getName().equalsIgnoreCase("bfd")) {
            handleBreakFastDropCommand(sender);
            return true;
        }

        return false;
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        PlayerSettings settings = getMetadataForPlayer(player);

        if(!settings.getEnabled()) {
            return;
        }

        Block block = e.getClickedBlock();

        if ((e.getAction() == Action.LEFT_CLICK_BLOCK)
                && block.getLocation().getY() > 0.0D
                && block.getType() != Material.BEDROCK) {

            // if player has plugin enabled but no longer has permission,
            //  disable plugin
            if(!hasPermission(player)) {
                settings.setEnabled(false);
                setMetadataForPlayer(player, settings);
            }


            // check break permissions
            BlockBreakEvent event = new BlockBreakEvent(block,player);
            getServer().getPluginManager().callEvent(event);
            if(event.isCancelled()) {
                return;
            }

            // play sound before breaking the block
            Sound sound = this.soundMap.get(CraftMagicNumbers.getBlock(block).stepSound.getBreakSound());
            block.getWorld().playSound(block.getLocation(), sound, 0.8f, 1);

            // check for drop
            if(settings.getDrop()) {
                block.breakNaturally();
            } else {
                block.setType(Material.AIR);
            }

        }
    }

    @EventHandler
    public void playerChangedWorld(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        PlayerSettings playerSettings = getMetadataForPlayer(player);
        if(playerSettings.getEnabled()) {
            playerSettings.setEnabled(false);
            setMetadataForPlayer(player, playerSettings);
            player.sendMessage(this.NameTag + " " + getEnabledString(playerSettings.getEnabled()));
        }
    }
}