package supercoder79.meltdown.map;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

import kdotjpg.opensimplex.OpenSimplexNoise;
import supercoder79.meltdown.map.gen.StraightPoplarTreeGen;
import xyz.nucleoid.plasmid.game.gen.feature.GrassGen;
import xyz.nucleoid.plasmid.game.gen.feature.tree.PoplarTreeGen;
import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructuresConfig;

public class MdChunkGenerator extends GameChunkGenerator {
	private final OpenSimplexNoise noise;
	private final OpenSimplexNoise deltailNoise;
	private final MdMap map;

	public MdChunkGenerator(MinecraftServer server, MdMap map) {
		super(createBiomeSource(server, BiomeKeys.PLAINS), new StructuresConfig(Optional.empty(), Collections.emptyMap()));
		this.map = map;
		Random random = new Random();

		this.noise = new OpenSimplexNoise(random.nextLong());
		this.deltailNoise = new OpenSimplexNoise(random.nextLong());
	}

	@Override
	public void populateNoise(WorldAccess world, StructureAccessor structures, Chunk chunk) {
		int chunkX = chunk.getPos().x * 16;
		int chunkZ = chunk.getPos().z * 16;

		BlockPos.Mutable mutable = new BlockPos.Mutable();
		Random random = new Random();

		for (int x = chunkX; x < chunkX + 16; x++) {
			for (int z = chunkZ; z < chunkZ + 16; z++) {
				// TODO: prettier map gen
				double height = 68 + noise.eval(x / 80.0, z / 80.0) * 5 + deltailNoise.eval(x / 16.0, z / 16.0) * 1;

				int genHeight  = (int) height;
				for (int y = 0; y <= genHeight; y++) {
					BlockState state = Blocks.STONE.getDefaultState();

					if (y == genHeight) {
						state = Blocks.GRASS_BLOCK.getDefaultState();

						if ((x >= map.reactorX - 2 && x <= map.reactorX + 2) && (z >= map.reactorZ - 2 && z <= map.reactorZ + 2)) {
							state = Blocks.IRON_BLOCK.getDefaultState();
						}

						if (x == map.reactorX && z == map.reactorZ) {
							state = Blocks.BEDROCK.getDefaultState();
							world.setBlockState(mutable.set(x, y + 1, z), Blocks.BEACON.getDefaultState(), 3);
							this.map.reactorY = y + 1; // TODO: this is a really ugly hack
						}
					} else if (y > genHeight - 4) {
						state = Blocks.DIRT.getDefaultState();
					}

					world.setBlockState(mutable.set(x, y, z), state, 3);
				}
			}
		}
	}

	@Override
	public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		Random random = new Random();

		int chunkX = region.getCenterChunkX() * 16;
		int chunkZ = region.getCenterChunkZ() * 16;

		// TODO: biomes, maybe? could be part of the config
		for (int i = 0; i < (random.nextInt(3) == 0 ? 1 : 0); i++) {
			int x = chunkX + random.nextInt(16);
			int z = chunkZ + random.nextInt(16);
			int y = region.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z);

			StraightPoplarTreeGen.INSTANCE.generate(region, mutable.set(x, y, z).toImmutable(), random);
		}

		for (int i = 0; i < 7; i++) {
			int x = chunkX + random.nextInt(16);
			int z = chunkZ + random.nextInt(16);
			int y = region.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z);

			GrassGen.INSTANCE.generate(region, mutable.set(x, y, z).toImmutable(), random);
		}
	}

	@Override
	public void carve(long seed, BiomeAccess biomes, Chunk chunk, GenerationStep.Carver carver) {
	}

	@Override
	public void setStructureStarts(DynamicRegistryManager registryManager, StructureAccessor accessor, Chunk chunk, StructureManager manager, long seed) {
	}

	@Override
	public void addStructureReferences(StructureWorldAccess world, StructureAccessor accessor, Chunk chunk) {
	}

	@Override
	public void populateEntities(ChunkRegion region) {
	}
}
