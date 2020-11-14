package behaviours;

import agents.Container;
import agents.Truck;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class MoveTruckBehaviour extends TickerBehaviour {

	private AID containerAID;
	private int amount;
	
	public MoveTruckBehaviour(Agent a, long period, AID containerAID, int amount) {
		super(a, period);
		this.containerAID = containerAID;
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
			truck.moveTowardsCentral();
		}
		else {
			if(truck.reachedContainer()) {
				truck.requestTrashFullPickup(containerAID);
				truck.returnToCentral();
			}
			else truck.moveTowardsPickup();
		}
	}
}