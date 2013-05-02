package com.github.CubieX.ScubaDoo;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ScubaDoo extends JavaPlugin
{
   public static final Logger log = Bukkit.getServer().getLogger();
   static final String logPrefix = "[ScubaDoo] "; // Prefix to go in front of all log entries

   private ScubaDoo plugin = null;
   private SDCommandHandler comHandler = null;
   private SDConfigHandler cHandler = null;
   private SDEntityListener eListener = null;
   private SDSchedulerHandler schedHandler = null;
   
   final static int MAX_AIR_CAP = 300; // cap for maxAir in seconds
   final static int HELMET_SLOT = 39;                  // slot number in inventory
   final static int TARGET_SLOT_COUNT = 5;             // ???
   final static int WARN_LEVEL = 5;                    // when to warn the player after each consumed helmet
   final static Material DIVE_HELMET = Material.GLASS; // Glassblock is the scuba helmet
   
   static boolean debug = false;
   static int maxAir = 30;                   // maximum air in seconds. Only use this for player output! Not for operations!
   static int maxAirTicks = maxAir * 20;     // maximum air in ticks Game default is 15. (300 ticks) (use this for queries and manipulations!)
  
   //*************************************************
   static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
   //*************************************************

   @Override
   public void onEnable()
   {
      this.plugin = this;
      cHandler = new SDConfigHandler(this);

      if(!checkConfigFileVersion())
      {
         log.severe(logPrefix + "Outdated or corrupted config file(s). Please delete your config files."); 
         log.severe(logPrefix + "will generate a new config for you.");
         log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
         getServer().getPluginManager().disablePlugin(this);
         return;
      }

      if (!hookToPermissionSystem())
      {
         log.info(logPrefix + "- Disabled due to no superperms compatible permission system found!");
         getServer().getPluginManager().disablePlugin(this);
         return;
      }

      eListener = new SDEntityListener(this);      
      comHandler = new SDCommandHandler(this, cHandler);      
      getCommand("scuba").setExecutor(comHandler);

      schedHandler = new SDSchedulerHandler(this);

      readConfigValues();

      log.info(this.getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");
      
      schedHandler.startPlayerInWaterCheckScheduler_SynchRepeating();
   }

   private boolean checkConfigFileVersion()
   {      
      boolean configOK = false;     

      if(cHandler.getConfig().isSet("config_version"))
      {
         String configVersion = cHandler.getConfig().getString("config_version");

         if(configVersion.equals(usedConfigVersion))
         {
            configOK = true;
         }
      }

      return (configOK);
   }

   private boolean hookToPermissionSystem()
   {
      if ((getServer().getPluginManager().getPlugin("PermissionsEx") == null) &&
            (getServer().getPluginManager().getPlugin("bPermissions") == null) &&
            (getServer().getPluginManager().getPlugin("zPermissions") == null) &&
            (getServer().getPluginManager().getPlugin("PermissionsBukkit") == null))
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   public void readConfigValues()
   {
      debug = cHandler.getConfig().getBoolean("debug");
      
      maxAir = cHandler.getConfig().getInt("maxAir");
      if(maxAir > MAX_AIR_CAP){maxAir = MAX_AIR_CAP;}
      if(maxAir < 0){maxAir = 0;}
      maxAirTicks = maxAir * 20;
   }

   @Override
   public void onDisable()
   {
      this.getServer().getScheduler().cancelAllTasks();
      cHandler = null;
      eListener = null;
      comHandler = null;
      schedHandler = null;
      log.info(this.getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
   }
   
   // #########################################################
   
   public boolean checkIfDivingInWater(Player player)
   {
      boolean playerIsDivingInWater = false;

      if(null != player)
      {
         Location loc = player.getLocation();
         loc.add(0, 1, 0); // set location to eye level
         
         if((loc.getBlock().getType() == Material.STATIONARY_WATER) ||
               loc.getBlock().getType() == Material.WATER)
         {
            playerIsDivingInWater = true;
         }
      }

      return (playerIsDivingInWater);
   } 
}


