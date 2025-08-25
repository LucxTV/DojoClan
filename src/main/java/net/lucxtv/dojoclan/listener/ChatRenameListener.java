package net.lucxtv.dojoclan.listener;

import net.lucxtv.dojoclan.DojoClan;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;

public class ChatRenameListener implements Listener {

    private static Set<Player> renameSessions = new HashSet<>();

    public static void startRenameSession(Player player) {
        renameSessions.add(player);
        player.sendMessage("§ePlease enter the new clan name in the chat:");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (renameSessions.contains(player)) {
            e.setCancelled(true);
            String newName = e.getMessage();

            if (DojoClan.getInstance().getClanManager().renameClan(player.getUniqueId(), newName)) {
                player.sendMessage("§aClan successfully renamed to: §f" + newName);
            } else {
                player.sendMessage("§cError renaming.");
            }

            renameSessions.remove(player);
        }
    }
}