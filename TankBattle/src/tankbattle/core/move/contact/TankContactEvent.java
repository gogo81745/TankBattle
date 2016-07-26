package tankbattle.core.move.contact;

import tankbattle.core.Entity;
import tankbattle.core.Tank;

public class TankContactEvent extends ContactEntityEvent {

	public TankContactEvent(Tank mover, Entity entity) {
		super(mover, entity);
	}

	public Tank getMover() {
		return (Tank) super.getMover();
	}

	public TankContactEvent setMover(Tank mover) {
		return (TankContactEvent) super.setMover(mover);
	}

	public TankContactEvent setEntity(Entity entity) {
		return (TankContactEvent) super.setEntity(entity);
	}

}
