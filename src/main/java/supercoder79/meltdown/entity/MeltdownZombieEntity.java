package supercoder79.meltdown.entity;

import supercoder79.meltdown.entity.ai.PathTowardsReactorGoal;
import supercoder79.meltdown.game.MdActive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class MeltdownZombieEntity extends ZombieEntity implements GameEntity{
	private final MdActive game;

	public MeltdownZombieEntity(World world, MdActive game) {
		super(world);
		this.game = game;
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new ZombieAttackGoal(this, 1.0, false));
		this.goalSelector.add(2, new PathTowardsReactorGoal<>(this));
		this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(4, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge(ZombifiedPiglinEntity.class));
		this.targetSelector.add(2, new FollowTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	protected void convertInWater() {
	}

	@Override
	protected void convertTo(EntityType<? extends ZombieEntity> entityType) {
	}

	@Override
	public MdActive getGame() {
		return this.game;
	}
}
