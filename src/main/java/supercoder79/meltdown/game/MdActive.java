package supercoder79.meltdown.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import supercoder79.meltdown.entity.MeltdownZombieEntity;
import supercoder79.meltdown.map.MdMap;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.BreakBlockListener;
import xyz.nucleoid.plasmid.game.event.EntityDeathListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.PlayerRemoveListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.event.UseItemListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldConfig;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;

public class MdActive {
	public final GameWorld world;
	public final MdMap map;
	public final MdConfig config;
	private final BubbleWorldConfig worldConfig;
	private final Set<ServerPlayerEntity> participants;
	private final MdSpawnLogic spawnLogic;

	private final List<MeltdownZombieEntity> trackedZombies = new ArrayList<>();
	private final Map<MeltdownZombieEntity, BlockPos> trackedPosition = new HashMap<>();
	private final List<BlockPos> trackedWalls = new ArrayList<>(); // This'll need to be expanded with wall types at some point

	private int ticks = 0;
	public boolean isNightTime = false;

	// TODO: difficulty scaling
	public int reactorHealth = 5;

	private MdActive(GameWorld world, MdMap map, MdConfig config, BubbleWorldConfig worldConfig, Set<ServerPlayerEntity> participants) {
		this.world = world;
		this.map = map;
		this.config = config;
		this.worldConfig = worldConfig;
		this.participants = participants;

		this.spawnLogic = new MdSpawnLogic(world, config);
	}

	public static void open(GameWorld world, MdMap map, MdConfig config, BubbleWorldConfig worldConfig) {
		MdActive active = new MdActive(world, map, config, worldConfig, new HashSet<>(world.getPlayers()));

		world.openGame(game -> {
			game.setRule(GameRule.CRAFTING, RuleResult.DENY);
			game.setRule(GameRule.PORTALS, RuleResult.DENY);
			game.setRule(GameRule.PVP, RuleResult.DENY);
			game.setRule(GameRule.BLOCK_DROPS, RuleResult.ALLOW);
			game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
			game.setRule(GameRule.HUNGER, RuleResult.DENY);
			game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);

			game.on(GameOpenListener.EVENT, active::open);
			game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
			game.on(PlayerAddListener.EVENT, active::addPlayer);

			game.on(BreakBlockListener.EVENT, active::onBreak);
			game.on(UseBlockListener.EVENT, active::onUseBlock);
//			game.on(PlayerRemoveListener.EVENT, active::removePlayer);
//
			game.on(GameTickListener.EVENT, active::tick);
//			game.on(UseItemListener.EVENT, active::onUseItem);
//
//			game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
			game.on(EntityDeathListener.EVENT, active::onEntityDeath);
		});
	}

	private void open() {
		for (ServerPlayerEntity player : this.participants) {
			this.spawnParticipant(player);

			player.inventory.insertStack(0, ItemStackBuilder.of(Items.IRON_SWORD).setUnbreakable().build());
			player.inventory.insertStack(1, ItemStackBuilder.of(Items.OAK_PLANKS).setCount(32).build());

			sendMessageFromCommander(player, "Welcome Generals. Your mission today is simple: stop the meltdown.");
			sendMessageFromCommander(player, "You have a few minutes to gather supplies and build defenses, but then the monsters will descend upon you.");
			sendMessageFromCommander(player, "Don't let them win. Good luck.");
		}
	}

	private void sendMessageFromCommander(ServerPlayerEntity player, String message) {
		player.sendMessage(new LiteralText("§6§lCommander: §r§6" + message), false);
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.spawnSpectator(player);
	}

	private void spawnParticipant(ServerPlayerEntity player) {
		this.spawnLogic.resetPlayer(player, GameMode.SURVIVAL);
		this.spawnLogic.spawnPlayer(player);
	}

	private void spawnSpectator(ServerPlayerEntity player) {
		this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
		this.spawnLogic.spawnPlayer(player);
	}

	private ActionResult onBreak(ServerPlayerEntity player, BlockPos pos) {
		if (player.getServerWorld().getBlockState(pos).isOf(Blocks.OAK_PLANKS)) {
			this.trackedWalls.remove(pos);

			return ActionResult.SUCCESS;
		}

		return ActionResult.FAIL;
	}

	private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		if (player.getStackInHand(hand).getItem() == Blocks.OAK_PLANKS.asItem()) {
			// ugly hack for tall grass
			BlockPos pos;

			if (this.world.getWorld().getBlockState(hitResult.getBlockPos()).getMaterial().isReplaceable()) {
				pos = hitResult.getBlockPos();
			} else {
				pos = hitResult.getBlockPos().offset(hitResult.getSide());
			}

			this.trackedWalls.add(pos);

			return ActionResult.PASS;
		}

		return ActionResult.FAIL;
	}

	ActionResult onEntityDeath(LivingEntity entity, DamageSource damageSource) {
		if (entity instanceof MeltdownZombieEntity) {
			this.trackedZombies.remove(entity);
		}

		return ActionResult.PASS;
	}

	private void tick() {
		this.ticks++;

		// Increase the time
		this.worldConfig.setTimeOfDay(12500 + ticks);

		if ((12500 + ticks) > 13000) {
			if (!this.isNightTime) {
				Random random = new Random();
				for (int i = 0; i < 16; i++) {
					// Spawn zombies around the reactor
					double theta = random.nextDouble() * Math.PI * 2;
					double dist = random.nextDouble() * 16 + 16;
					double xOffset = Math.cos(theta) * dist;
					double zOffset = Math.sin(theta) * dist;
					// TODO: atan2 to make sure the zombies don't spawn within a 30 degree radius of players

					int topY = this.world.getWorld().getTopY(Heightmap.Type.MOTION_BLOCKING, (int) (this.map.reactorX + xOffset), (int) (this.map.reactorZ + zOffset));
					BlockPos pos = new BlockPos(this.map.reactorX + xOffset, topY, this.map.reactorZ + zOffset);

					MeltdownZombieEntity zombie = new MeltdownZombieEntity(this.world.getWorld(), this);

					zombie.refreshPositionAndAngles(pos, 0, 0);
					zombie.setCustomName(new LiteralText("Zombie"));
					zombie.setPersistent();

					this.world.getWorld().spawnEntity(zombie);

					this.trackedZombies.add(zombie);
					this.trackedPosition.put(zombie, pos);
				}
			}

			this.isNightTime = true;
		}

		if (this.ticks % 10 == 0) {
			List<ZombieEntity> entities = this.world.getWorld().getEntitiesByType(EntityType.ZOMBIE, new Box(this.map.reactorX - 1, this.map.reactorY - 1, this.map.reactorZ - 1, this.map.reactorX + 2, this.map.reactorY + 2, this.map.reactorZ + 2), (t) -> true);

			if (entities.size() > 0) {
				for (ZombieEntity entity : entities) {
					entity.kill();
				}

				this.reactorHealth--;
				for (ServerPlayerEntity participant : this.participants) {
					participant.playSound(SoundEvents.ENTITY_WITHER_HURT, SoundCategory.MASTER, 1, 1);
				}
			}
		}

		// Entities break walls every 3 seconds if they don't move.
		// This should be moved to goals but I hate ai sooooo we're using  with this for now
		if (this.ticks % 60 == 0) {
			for (MeltdownZombieEntity zombie : this.trackedZombies) {
				BlockPos oldPos = this.trackedPosition.get(zombie);
				BlockPos newPos = zombie.getBlockPos();

				if (oldPos.getSquaredDistance(newPos) <= 4) {
					for (int x = -1; x <= 1; x++) {
					    for (int z = -1; z <= 1; z++) {
							for (int y = -1; y <= 1; y++) {
								BlockPos local = newPos.add(x, y, z);

								if (this.trackedWalls.contains(local)) {
									this.world.getWorld().breakBlock(local, false);
									this.trackedWalls.remove(local);
								}
							}
					    }
					}
				}

				this.trackedPosition.put(zombie, newPos);
			}
		}

	}
}
