package supercoder79.meltdown.map;

import java.util.Random;

import supercoder79.meltdown.game.MdConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class MdMap {
	private final MdConfig config;
	public final int reactorX;
	public final int reactorZ;
	public int reactorY;

	public MdMap(MdConfig config) {
		this.config = config;

		Random random = new Random();

		this.reactorX = random.nextInt(80) - random.nextInt(80);
		this.reactorZ = random.nextInt(80) - random.nextInt(80);
	}

	public ChunkGenerator chunkGenerator(MinecraftServer server) {
		return new MdChunkGenerator(server, this);
	}
}
