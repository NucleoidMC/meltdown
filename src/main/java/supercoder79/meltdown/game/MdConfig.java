package supercoder79.meltdown.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public final class MdConfig {
	public static final Codec<MdConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig)
	).apply(instance, MdConfig::new));

	public final PlayerConfig playerConfig;

	public MdConfig(PlayerConfig playerConfig) {

		this.playerConfig = playerConfig;
	}
}