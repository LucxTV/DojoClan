package net.lucxtv.dojoclan.commands;

import net.lucxtv.dojoclan.DojoClan;
import net.lucxtv.dojoclan.manager.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClanCommand implements CommandExecutor {

    private final ClanManager clanManager = DojoClan.getInstance().getClanManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly Players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§e§lClan Commands:");
            player.sendMessage("§8---------------------");
            player.sendMessage("§b/clan create <name>");
            player.sendMessage("§b/clan delete");
            player.sendMessage("§b/clan invite <player>");
            player.sendMessage("§b/clan accept");
            player.sendMessage("§b/clan deny");
            player.sendMessage("§b/clan members");
            player.sendMessage("§b/clan chat <message>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: §b/clan create <name>");
                    return true;
                }
                if (clanManager.isInClan(player.getUniqueId())) {
                    player.sendMessage("§cYou are already in a clan.");
                    return true;
                }
                String clanName = args[1];
                if (clanManager.createClan(player.getUniqueId(), clanName)) {
                    player.sendMessage("§aClan '" + clanName + "' successfully created!");
                } else {
                    player.sendMessage("§cClan could not be created.");
                }
                break;

            case "delete":
                if (!clanManager.isLeader(player.getUniqueId())) {
                    player.sendMessage("§cOnly the clan leader can delete the clan.");
                    return true;
                }
                if (clanManager.deleteClan(player.getUniqueId())) {
                    player.sendMessage("§aClan successfully deleted.");
                } else {
                    player.sendMessage("§cClan could not be deleted.");
                }
                break;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: §b/clan invite <player>");
                    return true;
                }
                if (!clanManager.isLeader(player.getUniqueId())) {
                    player.sendMessage("§cOnly the clan leader can invite players.");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }
                if (clanManager.invitePlayer(player.getUniqueId(), target.getUniqueId())) {
                    player.sendMessage("§aInvitation to " + target.getName() + " was sent.");
                } else {
                    player.sendMessage("§cInvitation Error..");
                }
                break;

            case "accept":
                if (clanManager.acceptInvite(player.getUniqueId())) {
                    player.sendMessage("§aYou are now a member of the clan!");
                } else {
                    player.sendMessage("§cNo invitation found.");
                }
                break;

            case "deny":
                try {
                    UUID uuid = player.getUniqueId();
                    DojoClan.getInstance().getClanSQL().getConnection()
                            .prepareStatement("DELETE FROM clan_invites WHERE invited_uuid = '" + uuid.toString() + "'")
                            .executeUpdate();
                    player.sendMessage("§cYou declined the invitation..");
                } catch (Exception e) {
                    player.sendMessage("§cError when rejecting.");
                    e.printStackTrace();
                }
                break;

            case "members":
                if (!clanManager.isInClan(player.getUniqueId())) {
                    player.sendMessage("§cYou are not in a Clan!");
                    return true;
                }
                player.sendMessage("§eMembers of your clan:");
                clanManager.getClanMembers(player.getUniqueId()).forEach(member -> {
                    player.sendMessage((member.isOnline() ? "§a" : "§7") + member.getName());
                });
                break;

            case "chat":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: §b/clan chat <nachricht>");
                    return true;
                }
                if (!clanManager.isInClan(player.getUniqueId())) {
                    player.sendMessage("§cYou are not in a clan.");
                    return true;
                }
                String msg = String.join(" ", args).substring(5);
                clanManager.sendClanMessage(player.getUniqueId(), player.getName() + ": " + msg);
                break;

            default:
                player.sendMessage("§cUnknown command. Use §e/clan §cfor help..");
                break;
        }
        return true;
    }
}