package net.lucxtv.dojoclan.commands;

import net.lucxtv.dojoclan.DojoClan;
import net.lucxtv.dojoclan.manager.ClanManager;
import net.lucxtv.dojoclan.model.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ClanMenuCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command..");
            return true;
        }

        Player player = (Player) sender;
        ClanManager clanManager = DojoClan.getInstance().getClanManager();

        if (!clanManager.isInClan(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a clan.");
            return true;
        }

        openClanMenu(player);
        return true;
    }

    public void openClanMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§dYour Clan");

        List<ClanMember> members = DojoClan.getInstance().getClanManager().getClanMembers(player.getUniqueId());

        for (ClanMember member : members) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            if (member.isOnline()) {
                meta.setOwner(member.getName());
                meta.setDisplayName("§a" + member.getName());

                List<String> lore = new ArrayList<>();
                lore.add("§7Online");
                lore.add(member.getUuid().toString());
                meta.setLore(lore);
            } else {
                meta.setOwner("MHF_Skeleton");
                meta.setDisplayName("§7" + member.getName());

                OfflinePlayer off = Bukkit.getOfflinePlayer(member.getUuid());
                long lastPlayed = off.getLastPlayed();
                String lastSeen;
                if (lastPlayed == 0L) {
                    lastSeen = "§7Never Online;";

                } else {
                    long diff = System.currentTimeMillis() - lastPlayed;
                    long minutes = diff / 1000 / 60;
                    long hours = minutes / 60;
                    long days = hours / 24;

                    if (days > 0) lastSeen = "§7Last Online: " + days + " Days";
                    else if (hours > 0) lastSeen = "§7last online: vor " + hours + " Hours";
                    else lastSeen = "§7last online: " + minutes + " Minutes";
                }

                List<String> lore = new ArrayList<>();
                lore.add(lastSeen);
                lore.add(member.getUuid().toString());
                meta.setLore(lore);
            }

            skull.setItemMeta(meta);
            inv.addItem(skull);
        }

        player.openInventory(inv);
    }
}