package supercoder79.meltdown;

import supercoder79.meltdown.game.MdConfig;
import supercoder79.meltdown.game.MdWaiting;
import xyz.nucleoid.plasmid.game.GameType;

import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

public class Meltdown implements ModInitializer {
	@Override
	public void onInitialize() {
		GameType.register(
				new Identifier("meltdown", "meltdown"),
				MdWaiting::open,
				MdConfig.CODEC
		);
	}
}
