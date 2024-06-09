package me.mod108.crawlingplugin;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Queue;

public class CrawlingPlayer {
    private final Player player;
    private final Queue<Block> fakeBlocks = new LinkedList<>();
    private Block floorBlock = null;

    public CrawlingPlayer(final Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Block removeLastBlock() {
        return fakeBlocks.poll();
    }

    public int getFakeBlockNumber() {
        return fakeBlocks.size();
    }

    public void addFakeBlock(final Block block) {
        fakeBlocks.add(block);
    }

    public Block getFloorBlock() {
        return floorBlock;
    }

    public void setFloorBlock(final Block block) {
        floorBlock = block;
    }
}
