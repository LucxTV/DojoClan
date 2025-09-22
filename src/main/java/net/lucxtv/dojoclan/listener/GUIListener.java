package net.lucxtv.dojoclan.listener;

import net.lucxtv.dojoclan.DojoClan;
import net.lucxtv.dojoclan.commands.ClanMenuCommand;
import net.lucxtv.dojoclan.manager.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class GUIListener implements Listener {

    private final ClanManager clanManager = DojoClan.getInstance().getClanManager();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        ItemStack clicked = e.getCurrentItem();

        String title = e.getView().getTitle();
        e.setCancelled(true);

        if (title.equals("§dYour Clan")) {
            if (clicked.getType() == Material.SKULL_ITEM) {
                List<String> lore = clicked.getItemMeta().getLore();
                if (lore == null || lore.isEmpty()) return;
                String uuidString = lore.get(lore.size() - 1);
                try {
                    UUID targetUUID = UUID.fromString(uuidString);
                    openMemberOptions(player, targetUUID);
                } catch (IllegalArgumentException ex) {
                    player.sendMessage("§cError reading the UUID.");
                }
            }
        }

        if (title.equals("§dClan Member Options")) {
            ItemStack skullItem = e.getInventory().getItem(13);
            if (skullItem == null || !skullItem.hasItemMeta() || skullItem.getItemMeta().getLore() == null) return;

            String uuidString = skullItem.getItemMeta().getLore().get(0);
            UUID targetUUID = UUID.fromString(uuidString);

            if (clicked.getType() == Material.BARRIER) {
                if (clanManager.isLeader(player.getUniqueId())) {
                    if (clanManager.kickMember(player.getUniqueId(), targetUUID)) {
                        player.sendMessage("§aPlayer was kicked from the clan.");
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cError while clicking.");
                    }
                } else {
                    player.sendMessage("§cOnly the leader can kick members.");
                }
            }

            if (clicked.getType() == Material.GOLD_HELMET) {
                if (clanManager.isLeader(player.getUniqueId())) {
                    try {
                        DojoClan.getInstance().getClanSQL().getConnection().prepareStatement(
                                "UPDATE clans SET owner_uuid = '" + targetUUID.toString() +
                                        "' WHERE owner_uuid = '" + player.getUniqueId().toString() + "'"
                        ).executeUpdate();
                        player.sendMessage("§a" + Bukkit.getOfflinePlayer(targetUUID).getName() + " is now the leader of the clan!");
                        player.closeInventory();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        player.sendMessage("§cErorror..");
                    }
                } else {
                    player.sendMessage("§cOnly the leader can transfer the leader role.");
                }
            }

            if (clicked.getType() == Material.REDSTONE) {
                new ClanMenuCommand().openClanMenu(player);
            }
        }
    }

    private void openMemberOptions(Player player, UUID targetUUID) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dClan Member Options");

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName("§b" + Bukkit.getOfflinePlayer(targetUUID).getName());
        meta.setLore(java.util.Collections.singletonList(targetUUID.toString()));
        skull.setItemMeta(meta);
        inv.setItem(13, skull);

        ItemStack kick = new ItemStack(Material.BARRIER);
        ItemMeta kickMeta = kick.getItemMeta();
        kickMeta.setDisplayName("§cMember Kick");
        kick.setItemMeta(kickMeta);
        inv.setItem(11, kick);

        ItemStack makeLeader = new ItemStack(Material.GOLD_HELMET);
        ItemMeta leaderMeta = makeLeader.getItemMeta();
        leaderMeta.setDisplayName("§6Promote to Leader");
        makeLeader.setItemMeta(leaderMeta);
        inv.setItem(15, makeLeader);

        ItemStack back = new ItemStack(Material.REDSTONE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cBack");
        back.setItemMeta(backMeta);
        inv.setItem(22, back);

        player.openInventory(inv);
    }
}