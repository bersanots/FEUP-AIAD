package behaviours;

import agents.Truck;
import general.App;
import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;

public class MoveTruckBehaviour extends TickerBehaviour {

	public MoveTruckBehaviour(Agent a, long period) {
		super(a, period);
	}

	private void stopTruck() {
		this.getAgent().removeBehaviour(this);
	}

	protected void onTick() {
		Truck truck = (Truck) this.getAgent();

		if (truck.isAvailable()) {
			truck.endPickup();
			stopTruck();
		}
		else if (truck.isReturning()) {
			if (!truck.isScanning())
				truck.moveTowardsCentral();
		}
		else {
			if (truck.reachedContainer()) {
				truck.requestTrashFullPickup();
				App.LOGGER.log(truck.getLocalName() + " reached destination");
			}
			else truck.moveTowardsPickup();
		}
	}
}