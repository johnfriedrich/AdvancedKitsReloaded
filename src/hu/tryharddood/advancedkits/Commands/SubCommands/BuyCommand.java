package hu.tryharddood.advancedkits.Commands.SubCommands;

import hu.tryharddood.advancedkits.AdvancedKits;
import hu.tryharddood.advancedkits.Commands.Subcommand;
import hu.tryharddood.advancedkits.Kits.Kit;
import hu.tryharddood.advancedkits.Kits.KitManager;
import hu.tryharddood.advancedkits.Variables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static hu.tryharddood.advancedkits.Phrases.phrase;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class BuyCommand extends Subcommand
{
    @Override
    public String getPermission()
    {
        return Variables.KIT_PERMISSION;
    }

    @Override
    public String getUsage()
    {
        return "/kit buy <kit>";
    }

    @Override
    public String getDescription()
    {
        return "Buys a kit.";
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

        if (kit.isPermonly() && !player.hasPermission(kit.getPermission()))
        {
            sendMessage(player, phrase("error_no_permission"), ChatColor.RED);
            closeGUI(player, "Details");

            return;
        }

        if (KitManager.getUnlocked(kit, player.getName()))
        {
            sendMessage(player, phrase("error_kitbuy_bought_already"), ChatColor.RED);
            closeGUI(player, "Details");

            return;
        }

        if (!AdvancedKits.getInstance().getConfiguration().isEconomy())
        {
            sendMessage(player, "Economy support disabled..", ChatColor.RED);
            closeGUI(player, "Details");
            return;
        }
        OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        double balance = AdvancedKits.econ.getBalance(oPlayer);
        if ((balance - kit.getCost()) >= 0)
        {
            AdvancedKits.econ.withdrawPlayer(oPlayer, kit.getCost());
            KitManager.setUnlocked(kit, player.getName());

            sendMessage(player, phrase("kitbuy_success_message", kit.getName()), ChatColor.GREEN);
            closeGUI(player, "Details");

            return;
        }
        else
        {
            sendMessage(player, phrase("error_kitbuy_not_enough_money"), ChatColor.RED);
            closeGUI(player, "Details");
            return;
        }
    }
}