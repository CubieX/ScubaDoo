package com.github.CubieX.ScubaDoo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SDEntityListener implements Listener
{
   private ScubaDoo plugin = null;

   public SDEntityListener(ScubaDoo plugin)
   {        
      this.plugin = plugin;

      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
   void onEquipHelmet(InventoryClickEvent e)
   {
      Player p = null;

      if(e.getWhoClicked() instanceof Player)
      {
         p = (Player)e.getWhoClicked();
      }

      if((ScubaDoo.maxAir > 0) &&
            (p.hasPermission("scubadoo.use"))) // leave handler if maxAir is set to 0. ScubaDoo is deactivated.
      {
         Inventory inv = e.getInventory();

         if (inv.getType() == InventoryType.CRAFTING)
         {         
            if (e.getSlot() == ScubaDoo.HELMET_SLOT)
            {
               if (inv.getSize() == ScubaDoo.TARGET_SLOT_COUNT)
               {
                  ItemStack holdItem = e.getCursor();

                  if (holdItem.getType().equals(ScubaDoo.DIVE_HELMET))
                  {
                     int holdCount = holdItem.getAmount();

                     if (e.getView().getBottomInventory().getType() == InventoryType.PLAYER)
                     {        
                        PlayerInventory pInv = (PlayerInventory) e.getView().getBottomInventory();
                        ItemStack headItem = pInv.getHelmet();
                        int headCount;

                        if (null == headItem )
                        {
                           if(ScubaDoo.debug){ScubaDoo.log.info("Nothing on Head");}
                           headCount = 0;                                      // if nothing on head
                           headItem = holdItem.clone();                        // prep for move
                        }
                        else
                        {
                           headCount = headItem.getAmount();
                        }

                        if (headItem.getType().equals(holdItem.getType()))
                        { // if scuba helmet on head
                           int moveCount = 0;

                           if(e.isRightClick()) // move one item at right click. Else move the whole stack to the slot
                           {
                              moveCount = 1;
                           }
                           else
                           {
                              moveCount = holdCount;
                           }

                           moveCount = Math.min(64 - headCount, moveCount); // move the smaller stack to cap this at 64 items

                           if (moveCount > 0 )
                           {
                              if(ScubaDoo.debug){ScubaDoo.log.info("Adding Dive Helmet " + moveCount + " blocks to head's " + headCount);}
                              headItem.setAmount(headCount + moveCount);               

                              if (holdCount <= moveCount)
                              {
                                 if(ScubaDoo.debug){ScubaDoo.log.info("Removing item in hand");}
                                 holdItem = null;
                              }
                              else
                              {
                                 if(ScubaDoo.debug){ScubaDoo.log.info("Reducing hold count (" + holdCount + ") by " + moveCount);}
                                 holdItem.setAmount(holdCount - moveCount);
                              }

                              pInv.setHelmet(headItem);                           // update head count
                              e.setCursor(holdItem);                              // update hold count
                           }
                        }
                        else
                        { 
                           pInv.setHelmet(holdItem);                             // swap hand and head items
                           e.setCursor(headItem); 
                        }

                        e.setResult(Result.ALLOW);
                        e.setCancelled(false);                                  // no more actions
                        if(ScubaDoo.debug){ScubaDoo.log.info("Should be on Head Now");}
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
   void onScubaBreathe(PlayerToggleSneakEvent e)
   {
      if((ScubaDoo.maxAirTicks == 0) || !(plugin.checkIfDivingInWater(e.getPlayer()))) // leave handler if maxAir is set to 0 or player is not diving
      {
         return;
      }

      Player p = e.getPlayer();

      if (e.isSneaking())
      {
         if (p.getRemainingAir() < ScubaDoo.maxAirTicks) // only refresh if remaining air is not full
         {
            ItemStack onHead = p.getInventory().getHelmet();

            if (null != onHead)
            {
               if (onHead.getType().equals(ScubaDoo.DIVE_HELMET))
               {
                  int refills = onHead.getAmount(); // refills left

                  if (refills > 0) // are still helmets left?
                  {
                     refills--;                            // remove one exhausted helmet
                     onHead.setAmount(refills);
                     p.setRemainingAir(ScubaDoo.maxAirTicks); // replenish air reservoir

                     if (refills <= ScubaDoo.WARN_LEVEL)
                     {
                        if(refills >= 1)
                        {
                           p.sendMessage(ChatColor.GRAY + "Noch fuer " + ChatColor.GOLD + refills + ChatColor.GRAY + " Atemzuege (" + ChatColor.GOLD + (refills + 1) * ScubaDoo.maxAir + ChatColor.GRAY + " Sekunden) Luft.");
                        }
                        else // the last helmet is now exhausted. Player has taken his last breath.
                        {
                           p.sendMessage(ChatColor.GRAY + "Das war dein " + ChatColor.RED + "LETZTER " + ChatColor.GRAY + "Atemzug!");
                           p.getInventory().setHelmet(null);          // remove helmet
                        }
                     }
                  }

                  p.getInventory().setHelmet(onHead);        // update dive helmet slot

               }
            }
         }
      }
   }
}
