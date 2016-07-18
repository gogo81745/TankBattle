package tankbattle.core.position.move;

import tankbattle.core.TankBattle;
import tankbattle.core.event.Listener;
import tankbattle.core.position.Point;
import tankbattle.core.position.Vector;
import tankbattle.core.time.TimeListener;

public class MoveListener implements Listener<MoveEvent> {

	public static class MoveTimer extends TimeListener {
		final public static String LID = "TankBattle:MoveTimer";

		public MoveTimer(double interval) {
			super(interval, e -> {
				TankBattle.getGame().getEntityGroup().getAll().forEach(entity -> {
					entity.move();
				});
			});
		}

	}

	public static class MoveSpeedSetter implements Listener<MoveEvent> {
		final public static String LID = "TankBattle:MoveSpeedSetter";

		@Override
		public void listen(MoveEvent event) {
			if (event.canceled() || event.executed() || event.getMover() == null) {
				return;
			}
			Vector v = new Vector(event.getMover().towards(), event.getMover().speed() / TankBattle.getGame().getFPS());
			if (event.getVector() != null) {
				event.getVector().set(v);
			} else {
				event.setVector(v);
			}

		}

	}

	final public static String LID = "TankBattle:MoveListener";

	@Override
	public void listen(MoveEvent event) {
		if (event.canceled() || event.executed() || event.getMover() == null) {
			return;
		}
		event.setExecuted(true);
		if (event.getVector() == null) {
			return;
		}
		Point p = event.getMover().position();
		p.set(p.add(event.getVector()));
		event.getMover().setPosition(p);
	}

}