package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Kits.KitManager;
import hu.tryharddood.advancedkits.Listeners.InventoryListener;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static hu.tryharddood.advancedkits.Phrases.phrase;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class UseCommand extends Subcommand
{
    @Override
    public String getPermission()
    {
        return Variables.KIT_PERMISSION;
    }

    @Override
    public String getUsage()
    {
        return "/kit use <kit>";
    }

    @Override
    public String getDescription()
    {
        return "Uses a kit";
    }

    @Override
    public int getArgs()
    {
        return 2;
    }

    @Override
    public boolean playerOnly()
    {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = (Player) sender;
        Kit kit = KitManager.getKit(args[1]);
        if (kit == null)
        {
            sendMessage(player, phrase("error_kit_not_found"), ChatColor.RED);
            return;
        }
        if (kit.getUses() > 0 && (kit.getUses() - KitManager.getUses(kit, player)) <= 0 && !player.hasPermission(Variables.KITADMIN_PERMISSION))
        {
            sendMessage(player, phrase("cant_use_anymore"), ChatColor.RED);
            closeGUI(player, "Details");

            return;
        }

        if (AdvancedKits.getInstance().getConfiguration().isEconomy() && !KitManager.getUnlocked(kit, player.getName()))
        {
            sendMessage(player, phrase("kituse_error_notunlocked"), ChatColor.RED);
            closeGUI(player, "Details");

            return;
        }

        if (kit.isPermonly() && !player.hasPermission(kit.getPermission()))
        {
            sendMessage(player, phrase("error_no_permission"), ChatColor.RED);
            closeGUI(player, "Details");

            return;
        }

        if (kit.getDelay() > 0)
        {
            if (!player.hasPermission(Variables.KITDELAY_BYPASS))
            {
                if (!KitManager.CheckCooldown(player, kit))
                {
                    closeGUI(player, "Details");
                    sendMessage(player, phrase("kituse_wait", KitManager.getDelay(player, kit)), ChatColor.RED);
                    return;
                }
            }
        }

        if (kit.getWorlds().contains(player.getWorld().getName()))
        {
            sendMessage(player, phrase("kitadmin_flag_world"), ChatColor.RED);
            return;
        }

        GiveItems(player, kit);
    }

    private void GiveItems(Player player, Kit kit)
    {
        if (kit.getUses() > 0)
        {
            KitManager.setUses(kit, player, (KitManager.getUses(kit, player) + 1));
        }

        PlayerInventory inv = player.getInventory();
        if (kit.isClearinv())
        {
            AdvancedKits.clearInventory.clearArmor(player);
            AdvancedKits.clearInventory.clearInventory(player);
        }

        ItemMeta itemMeta;
        for (ItemStack item : kit.getItemStacks())
        {
            if (item.hasItemMeta())
            {
                itemMeta = item.getItemMeta();
                if (itemMeta.hasDisplayName() && itemMeta.getDisplayName().contains("%player%"))
                {
                    itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%player%", player.getName()));
                }

                if (itemMeta.getLore() != null)
                {
                    List<String> lore = itemMeta.getLore();
                    for (int i = 0; i < lore.size(); i++)
                    {
                        lore.set(i, lore.get(i).replaceAll("%player%", player.getName()));
                    }
                    itemMeta.setLore(lore);
                }

                item.setItemMeta(itemMeta);
            }
            inv.addItem(item);
        }

        for (ItemStack item : kit.getArmor())
        {
            if (item.hasItemMeta())
            {
                itemMeta = item.getItemMeta();
                if (itemMeta.hasDisplayName() && itemMeta.getDisplayName().contains("%player%"))
                {
                    itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%player%", player.getName()));
                }

                if (itemMeta.getLore() != null)
                {
                    List<String> lore = itemMeta.getLore();
                    for (int i = 0; i < lore.size(); i++)
                    {
                        lore.set(i, lore.get(i).replaceAll("%player%", player.getName()));
                    }
                    itemMeta.setLore(lore);
                }

                item.setItemMeta(itemMeta);
            }

            if (InventoryListener.isHelmet(item.getType()))
            {
                player.getInventory().setHelmet(item);
            }
            else if (InventoryListener.isChestplate(item.getType()))
            {
                player.getInventory().setChestplate(item);
            }
            else if (InventoryListener.isLeggings(item.getType()))
            {
                player.getInventory().setLeggings(item);
            }
            else if (InventoryListener.isBoots(item.getType())) player.getInventory().setBoots(item);
        }

        player.updateInventory();

        KitManager.setDelay(player, kit.getDelay(), kit);
        closeGUI(player, "Details");
        sendMessage(player, phrase("kituse_success"), ChatColor.GREEN);

        for (String command : kit.getCommands())
        {
            Bukkit.dispatchCommand(AdvancedKits.getInstance().getServer().getConsoleSender(), command.replaceAll("%player%", player.getName()));
        }
    }
}