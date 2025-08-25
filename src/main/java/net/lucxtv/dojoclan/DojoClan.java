package net.lucxtv.dojoclan;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import net.lucxtv.dojoclan.commands.ClanCommand;
import net.lucxtv.dojoclan.commands.ClanMenuCommand;
import net.lucxtv.dojoclan.database.ClanSQL;
import net.lucxtv.dojoclan.manager.ClanManager;
import net.lucxtv.dojoclan.listener.GUIListener;
import net.lucxtv.dojoclan.listener.ChatRenameListener;

public class DojoClan extends JavaPlugin {

    private static DojoClan instance;
    private ClanSQL clanSQL;
    private ClanManager clanManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.clanSQL = new ClanSQL(this);
        try {
            this.clanSQL.connect();
            this.clanSQL.createTables();
            getLogger().info("MySQL verbunden und Tabellen erstellt.");
        } catch (Exception e) {
            getLogger().severe("Fehler bei der Verbindung zu MySQL:");
            e.printStackTrace();
        }

        this.clanManager = new ClanManager();

        getCommand("clanmenu").setExecutor(new ClanMenuCommand());
        getCommand("clan").setExecutor(new ClanCommand());
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatRenameListener(), this);

        getLogger().info("DojoClan v1.0 aktiviert!");
    }

    @Override
    public void onDisable() {
        if (clanSQL != null) {
            clanSQL.disconnect();
        }
        getLogger().info("DojoClan v1.0 deaktiviert!");
    }

    public static DojoClan getInstance() {
        return instance;
    }

    public ClanSQL getClanSQL() {
        return clanSQL;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }
}