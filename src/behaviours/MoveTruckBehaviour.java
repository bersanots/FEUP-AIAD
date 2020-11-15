package behaviours;

import agents.Container;
import agents.Truck;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class MoveTruckBehaviour extends TickerBehaviour {
	
	private int amount;
	
	public MoveTruckBehaviour(Agent a, long period, int amount) {
		super(a, period);
		
		this.amount = amount;
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
			if(truck.reachedContainer()) {
				truck.requestTrashFullPickup();
			}
			else truck.moveTowardsPickup();
		}
	}
}