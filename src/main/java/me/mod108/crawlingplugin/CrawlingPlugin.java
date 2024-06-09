package me.mod108.crawlingplugin;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrawlingPlugin extends JavaPlugin {
    public static CrawlingPlugin getPlugin() {
        return plugin;
    }

    public CrawlingManager getCrawlingManager() {
        return crawlingManager;
    }

    private static CrawlingPlugin plugin;
    private final CrawlingManager crawlingManager = new CrawlingManager();

    @Override
    public void onEnable() {
        plugin = this;

        getServer().getPluginManager().registerEvents(crawlingManager, this);
        registerCommand("togglecrawling", crawlingManager);
    }

    private void registerCommand(final String command, final CommandExecutor executor) {
        final PluginCommand pluginCommand = getCommand(command);
        if (pluginCommand != null)
            pluginCommand.setExecutor(executor);
        else
            System.err.println("ERROR: Couldn't find command named \"" + command + "\"!");
    }

    @Override
    public void onDisable() {
        crawlingManager.clearCrawlingPlayers();
    }
}
