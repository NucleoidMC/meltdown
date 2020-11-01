package supercoder79.meltdown.game;

import java.util.HashSet;
import java.util.Set;

import supercoder79.meltdown.map.MdMap;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.EntityDeathListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.PlayerRemoveListener;
import xyz.nucleoid.plasmid.game.event.UseItemListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

public class MdActive {
	public final GameWorld world;
	public final MdMap map;
	public final MdConfig config;
	private final Set<ServerPlayerEntity> participants;
	private final MdSpawnLogic spawnLogic;

	private MdActive(GameWorld world, MdMap map, MdConfig config, Set<ServerPlayerEntity> participants) {
		this.world = world;
		this.map = map;
		this.config = config;
		this.participants = participants;

		this.spawnLogic = new MdSpawnLogic(world, config);
	}

	public static void open(GameWorld world, MdMap map, MdConfig config) {
		MdActive active = new MdActive(world, map, config, new HashSet<>(world.getPlayers()));

		world.openGame(game -> {
			game.setRule(GameRule.CRAFTING, RuleResult.ALLOW);
			game.setRule(GameRule.PORTALS, RuleResult.DENY);
			game.setRule(GameRule.PVP, RuleResult.DENY);
			game.setRule(GameRule.BLOCK_DROPS, RuleResult.ALLOW);
			game.setRule(GameRule.FALL_DAMAGE, RuleResult.ALLOW);
			game.setRule(GameRule.HUNGER, RuleResult.ALLOW);
			game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);

			game.on(GameOpenListener.EVENT, active::open);
			game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
			game.on(PlayerAddListener.EVENT, active::addPlayer);
//			game.on(PlayerRemoveListener.EVENT, active::removePlayer);
//
//			game.on(GameTickListener.EVENT, active::tick);
//			game.on(UseItemListener.EVENT, active::onUseItem);
//
//			game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
//			game.on(EntityDeathListener.EVENT, active::onEntityDeath);
		});
	}

	private void open() {
		for (ServerPlayerEntity player : this.participants) {
			this.spawnParticipant(player);
			player.sendMessage(
					new LiteralText("§6§lCommander: §r§6Welcome Generals. Your mission today is simple: stop the meltdown."), false);
			player.sendMessage(
					new LiteralText("§6§lCommander: §r§6You have a few minutes to gather supplies and build defenses, but then the monsters will descend upon you."), false);
			player.sendMessage(
					new LiteralText("§6§lCommander: §r§6Don't let them win. Good luck."), false);
		}
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.spawnSpectator(player);
	}

	private void spawnParticipant(ServerPlayerEntity player) {
		this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
		this.spawnLogic.spawnPlayer(player);
	}

	private void spawnSpectator(ServerPlayerEntity player) {
		this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
		this.spawnLogic.spawnPlayer(player);
	}
}
