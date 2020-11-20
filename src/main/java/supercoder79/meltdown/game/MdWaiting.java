package supercoder79.meltdown.game;

import java.util.concurrent.CompletableFuture;

import supercoder79.meltdown.map.MdMap;
import supercoder79.meltdown.map.MdMapGenerator;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.event.AttackEntityListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.event.UseItemListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldConfig;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldSpawner;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public final class MdWaiting {
	private final MdSpawnLogic spawnLogic;
	private final GameWorld world;
	private final MdMap map;
	private final MdConfig config;
	private final BubbleWorldConfig worldConfig;

	private MdWaiting(GameWorld world, MdMap map, MdConfig config, BubbleWorldConfig worldConfig) {
		this.world = world;
		this.map = map;
		this.config = config;
		this.worldConfig = worldConfig;
		this.spawnLogic = new MdSpawnLogic(world, config);
	}

	public static CompletableFuture<GameWorld> open(GameOpenContext<MdConfig> context) {
		MdMapGenerator generator = new MdMapGenerator();
		MdConfig config = context.getConfig();

		return generator.create(config)
				.thenCompose(map -> {
					BubbleWorldConfig worldConfig = new BubbleWorldConfig()
							.setGenerator(map.chunkGenerator(context.getServer()))
							.setDefaultGameMode(GameMode.SPECTATOR)
							.setSpawner(BubbleWorldSpawner.atSurface(0, 0))
							.setTimeOfDay(6000)
							.setDifficulty(Difficulty.NORMAL);

					return context.openWorld(worldConfig).thenApply(gameWorld -> {
						MdWaiting waiting = new MdWaiting(gameWorld, map, config, worldConfig);

						return GameWaitingLobby.open(gameWorld, config.playerConfig, game -> {
							game.setRule(GameRule.CRAFTING, RuleResult.DENY);
							game.setRule(GameRule.PORTALS, RuleResult.DENY);
							game.setRule(GameRule.PVP, RuleResult.DENY);
							game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
							game.setRule(GameRule.HUNGER, RuleResult.DENY);

							game.on(RequestStartListener.EVENT, waiting::requestStart);

							game.on(PlayerAddListener.EVENT, waiting::addPlayer);
							game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
//							game.on(AttackEntityListener.EVENT, waiting::onAttackEntity);
//							game.on(UseBlockListener.EVENT, waiting::onUseBlock);
//							game.on(UseItemListener.EVENT, waiting::onUseItem);
						});
					});
				});
	}

	private StartResult requestStart() {
		MdActive.open(this.world, this.map, this.config, this.worldConfig);
		return StartResult.OK;
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.spawnPlayer(player);
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		this.spawnPlayer(player);
		return ActionResult.FAIL;
	}

	private void spawnPlayer(ServerPlayerEntity player) {
		this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
		this.spawnLogic.spawnPlayer(player);
	}
}
