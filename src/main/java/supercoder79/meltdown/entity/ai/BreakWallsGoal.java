package supercoder79.meltdown.entity.ai;

import java.util.EnumSet;

import supercoder79.meltdown.entity.GameEntity;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

public class BreakWallsGoal<T extends PathAwareEntity & GameEntity> extends Goal {
	private final T entity;

	public BreakWallsGoal(T entity) {
		this.entity = entity;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP, Control.TARGET));
	}

	@Override
	public void start() {
		// TODO: implement this at some point
	}

	@Override
	public boolean canStart() {
		return true;
	}
}
