package supercoder79.meltdown.entity.ai;

import java.util.EnumSet;

import supercoder79.meltdown.entity.GameEntity;
import supercoder79.meltdown.game.MdActive;

import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;

public class PathTowardsReactorGoal<T extends PathAwareEntity & GameEntity> extends Goal {
	private final T entity;

	public PathTowardsReactorGoal(T entity) {
		this.entity = entity;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP, Control.TARGET));
	}

	@Override
	public void start() {
		MdActive game = this.entity.getGame();
		Vec3d center = new Vec3d(game.map.reactorX, game.map.reactorY, game.map.reactorZ);
		System.out.println("Reactor: " + center);
		Vec3d target = TargetFinder.findTargetTowards(this.entity, 31, 15, center);

		if (target != null) {
			boolean didStart = this.entity.getNavigation().startMovingTo(target.x, target.y, target.z, 1.0);
			System.out.println("Moving towards: " + target + " and returned " + didStart);
		}
	}

	@Override
	public boolean canStart() {
		return true;
	}
}
