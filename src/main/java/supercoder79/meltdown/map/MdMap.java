package supercoder79.meltdown.map;

import supercoder79.meltdown.game.MdConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class MdMap {
	private final MdConfig config;

	public MdMap(MdConfig config) {
		this.config = config;
	}

	public ChunkGenerator chunkGenerator(MinecraftServer server) {
		return new MdChunkGenerator(server);
	}
}
