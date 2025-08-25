package net.lucxtv.dojoclan.manager;

import net.lucxtv.dojoclan.DojoClan;
import net.lucxtv.dojoclan.model.ClanMember;

import java.sql.*;
import java.util.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ClanManager {

    public boolean isInClan(UUID playerUUID) {
        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection().prepareStatement(
                    "SELECT * FROM clan_members WHERE member_uuid = ?");
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getClanId(UUID playerUUID) {
        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection().prepareStatement(
                    "SELECT clan_id FROM clan_members WHERE member_uuid = ?");
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("clan_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<ClanMember> getClanMembers(UUID playerUUID) {
        List<ClanMember> members = new ArrayList<>();
        int clanId = getClanId(playerUUID);
        if (clanId == -1) return members;

        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection().prepareStatement(
                    "SELECT member_uuid FROM clan_members WHERE clan_id = ?");
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("member_uuid"));
                Player onlinePlayer = Bukkit.getPlayer(uuid);
                String name = onlinePlayer != null ? onlinePlayer.getName() : getOfflineName(uuid);
                boolean isOnline = onlinePlayer != null;
                members.add(new ClanMember(uuid, name, isOnline));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public boolean createClan(UUID owner, String name) {
        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection()
                    .prepareStatement("INSERT INTO clans (name, owner_uuid) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);
            stmt.setString(2, owner.toString());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int clanId = keys.getInt(1);

                PreparedStatement memberStmt = DojoClan.getInstance().getClanSQL().getConnection()
                        .prepareStatement("INSERT INTO clan_members (clan_id, member_uuid) VALUES (?, ?)");
                memberStmt.setInt(1, clanId);
                memberStmt.setString(2, owner.toString());
                memberStmt.executeUpdate();

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteClan(UUID owner) {
        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection()
                    .prepareStatement("DELETE FROM clans WHERE owner_uuid = ?");
            stmt.setString(1, owner.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean invitePlayer(UUID clanOwner, UUID invited) {
        int clanId = getClanId(clanOwner);
        if (clanId == -1) return false;

        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection()
                    .prepareStatement("INSERT INTO clan_invites (clan_id, invited_uuid, invited_by, timestamp) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, clanId);
            stmt.setString(2, invited.toString());
            stmt.setString(3, clanOwner.toString());
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();

            Player target = Bukkit.getPlayer(invited);
            Player owner = Bukkit.getPlayer(clanOwner);

            if (target != null && owner != null) {
                target.sendMessage("§eYou have been invited" + owner.getName() + " §eto join the clan.");
                TextComponent accept = new TextComponent("§7[§a§lACCEPT§7]");
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan accept"));

                TextComponent deny = new TextComponent(" §7[§c§lDENY§7]");
                deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan deny"));
                target.spigot().sendMessage(accept, deny);
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void sendClanMessage(UUID sender, String message) {
        int clanId = getClanId(sender);
        if (clanId == -1) return;

        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection()
                    .prepareStatement("SELECT member_uuid FROM clan_members WHERE clan_id = ?");
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("member_uuid"));
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.sendMessage("§d[Clan] §7" + message);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean acceptInvite(UUID player) {
        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection()
                    .prepareStatement("SELECT clan_id FROM clan_invites WHERE invited_uuid = ?");
            stmt.setString(1, player.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int clanId = rs.getInt("clan_id");

                PreparedStatement add = DojoClan.getInstance().getClanSQL().getConnection()
                        .prepareStatement("INSERT INTO clan_members (clan_id, member_uuid) VALUES (?, ?)");
                add.setInt(1, clanId);
                add.setString(2, player.toString());
                add.executeUpdate();

                PreparedStatement del = DojoClan.getInstance().getClanSQL().getConnection()
                        .prepareStatement("DELETE FROM clan_invites WHERE invited_uuid = ?");
                del.setString(1, player.toString());
                del.executeUpdate();

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean renameClan(UUID owner, String newName) {
        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection()
                    .prepareStatement("UPDATE clans SET name = ? WHERE owner_uuid = ?");
            stmt.setString(1, newName);
            stmt.setString(2, owner.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean kickMember(UUID owner, UUID target) {
        int clanId = getClanId(owner);
        if (clanId == -1) return false;

        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection()
                    .prepareStatement("DELETE FROM clan_members WHERE clan_id = ? AND member_uuid = ?");
            stmt.setInt(1, clanId);
            stmt.setString(2, target.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getOfflineName(UUID uuid) {
        return "Offline-Player";
    }

    public boolean isLeader(UUID playerUUID) {
        try {
            PreparedStatement stmt = DojoClan.getInstance().getClanSQL().getConnection().prepareStatement(
                    "SELECT id FROM clans WHERE owner_uuid = ?");
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}