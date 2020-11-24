package supercoder79.meltdown.map;

import java.util.concurrent.CompletableFuture;

import supercoder79.meltdown.game.MdConfig;

import net.minecraft.util.Util;

public final class MdMapGenerator {
    public MdMap create(MdConfig config) {
        return new MdMap(config);
    }
}