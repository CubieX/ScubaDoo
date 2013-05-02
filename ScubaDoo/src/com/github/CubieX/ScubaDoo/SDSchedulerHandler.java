package com.github.CubieX.ScubaDoo;

import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SDSchedulerHandler
{
   private ScubaDoo plugin = null;
   private HashSet<String> divingPlayers= new HashSet<String>();

   public SDSchedulerHandler(ScubaDoo plugin)
   {
      this.plugin = plugin;
   }

   public void startPlayerInWaterCheckScheduler_SynchRepeating()
   {
      plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable()
      {
         public void run()
         {
            for(Player p : Bukkit.getServer().getOnlinePlayers())
            {
               if(null != p)
               {
                  if((null != p.getInventory().getHelmet()) &&
                        (p.getInventory().getHelmet().getType().equals(ScubaDoo.DIVE_HELMET)))
                  {
                     if(plugin.checkIfDivingInWater(p))
                     {
                        if(!divingPlayers.contains(p.getName()))
                        {
                           divingPlayers.add(p.getName());
                           
                           int refills = p.getInventory().getHelmet().getAmount();
                           p.sendMessage(ChatColor.AQUA + "Druecke SCHLEICHEN um Luft zu holen.");
                           p.sendMessage(ChatColor.GRAY + "Du hast Zusatz-Luft fuer " + ChatColor.GREEN + refills + ChatColor.GRAY + " Atemzuege (" + ChatColor.GREEN + (refills * ScubaDoo.maxAir) + ChatColor.GRAY + " Sekunden) dabei.");
                        }
                     }
                     else
                     {
                        if(divingPlayers.contains(p.getName()))
                        {
                           divingPlayers.remove(p.getName());
                        }
                     }
                  }
               }
            }            
         }
      }, 10 * 20L, 1 * 20L); // 10 seconds delay, 1 second cycle
   }
}
