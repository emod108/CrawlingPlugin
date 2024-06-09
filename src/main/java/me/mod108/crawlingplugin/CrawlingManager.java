package me.mod108.crawlingplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class CrawlingManager implements Listener, CommandExecutor {
    private static final int NOT_CRAWLING = -1;
    private static final BlockData fakeBlock = Bukkit.createBlockData(Material.BARRIER);

    private final ArrayList<CrawlingPlayer> players = new ArrayList<>();
    private BukkitRunnable task = null;

    // Necessary so you can crawl down slabs and stairs
    // It's not conceivable to add a way to crawl up
    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        final Player player = e.getPlayer();
        final int index = getCrawlingIndex(player);
        if (index == NOT_CRAWLING)
            return;

        final Location to = e.getTo();
        if (to == null)
            return;

        final Block floorBlock = to.getBlock();
        final Block oldFloorBlock = players.get(index).getFloorBlock();
        if (isSlab(floorBlock) || isStairs(floorBlock)) {
            player.sendBlockChange(floorBlock.getLocation(), fakeBlock);
            to.setY(e.getFrom().getY());
            e.setTo(to);
            players.get(index).setFloorBlock(floorBlock);
            if (oldFloorBlock != null)
                player.sendBlockChange(oldFloorBlock.getLocation(), oldFloorBlock.getBlockData());
        } else {
            if (oldFloorBlock == null)
                return;
            final Location oldFloorBlockCenter = oldFloorBlock.getLocation().clone().add(0.5, 0.5, 0.5);
            if (to.distance(oldFloorBlockCenter) > 1.5)
                player.sendBlockChange(oldFloorBlock.getLocation(), oldFloorBlock.getBlockData());
        }
    }

    // Swimming animation is also a crawling animation
    @EventHandler
    public void onToggleSwim(final EntityToggleSwimEvent e) {
        if (!(e.getEntity() instanceof final Player player))
            return;

        if (!isCrawling(player))
            return;

        if (!e.isSwimming())
            e.setCancelled(true);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label,
                             final String[] args) {
        if (!(sender instanceof final Player player))
            return true;

        if (getCrawlingIndex(player) == NOT_CRAWLING) {
            startCrawling(player);
            player.sendMessage(ChatColor.YELLOW + "You started crawling!");
        } else {
            stopCrawling(player);
            player.sendMessage(ChatColor.YELLOW + "You stopped crawling!");
        }

        return true;
    }

    private int getCrawlingIndex(final Player player) {
        for (int i = 0; i < players.size(); ++i) {
            if (player.getUniqueId().equals(players.get(i).getPlayer().getUniqueId()))
                return i;
        }
        return NOT_CRAWLING;
    }

    public boolean isCrawling(final Player player) {
        return getCrawlingIndex(player) != NOT_CRAWLING;
    }

    public void startCrawling(final Player player) {
        if (isCrawling(player))
            return;

        // Adding the player to the crawling list
        final CrawlingPlayer crawlingPlayer = new CrawlingPlayer(player);
        players.add(crawlingPlayer);

        // Making it so the player starts crawling
        player.setSwimming(true);
        if (task == null) {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (final CrawlingPlayer player : players) {
                        final Location playerLocation = player.getPlayer().getLocation();
                        final Block block = playerLocation.getBlock().getRelative(0, 1, 0);
                        removeFakeBlock(player);
                        addFakeBlock(player, block);
                    }
                }
            };
            task.runTaskTimer(CrawlingPlugin.getPlugin(), 0, 1);
        }
    }

    public void stopCrawling(final Player player) {
        if (players.size() < 1) {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }

        final int index = getCrawlingIndex(player);
        if (index == NOT_CRAWLING)
            return;

        final CrawlingPlayer crawlingPlayer = players.get(index);
        while (crawlingPlayer.getFakeBlockNumber() > 0)
            removeFakeBlock(crawlingPlayer);
        final Block floorBlock = crawlingPlayer.getFloorBlock();
        if (floorBlock != null)
            player.sendBlockChange(floorBlock.getLocation(), floorBlock.getBlockData());
        players.remove(index);
    }

    public void clearCrawlingPlayers() {
        while (players.size() > 0) {
            stopCrawling(players.get(0).getPlayer());
            players.remove(0);
        }
    }

    private void addFakeBlock(final CrawlingPlayer player, final Block block) {
        player.getPlayer().sendBlockChange(block.getLocation(), fakeBlock);
        player.addFakeBlock(block);
    }

    private void removeFakeBlock(final CrawlingPlayer player) {
        final Block block = player.removeLastBlock();
        if (block == null)
            return;
        player.getPlayer().sendBlockChange(block.getLocation(), block.getBlockData());
    }

    public static boolean isSlab(final Block block) {
        return block.getBlockData() instanceof Slab;
    }

    public static boolean isStairs(final Block block) {
        return block.getBlockData() instanceof Stairs;
    }
}
