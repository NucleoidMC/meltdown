package supercoder79.meltdown.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(LeavesBlock.class)
public class MixinLeavesBlock {

	/**
	 * Don't drop items when decaying
	 *
	 * @author SuperCoder79
	 */
	@Overwrite
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (!state.get(LeavesBlock.PERSISTENT) && state.get(LeavesBlock.DISTANCE) == 7) {
			world.breakBlock(pos, false);
		}
	}
}
