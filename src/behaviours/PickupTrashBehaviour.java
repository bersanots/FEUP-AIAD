package behaviours;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import agents.Truck;
import general.App;
import general.TrashType;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

public class PickupTrashBehaviour extends AchieveREInitiator {
	
	
	private Truck truck;
	
	public PickupTrashBehaviour(Truck truckAgent, ACLMessage msg) {
		super(truckAgent, msg);
		
		this.truck = truckAgent;
		
	}
	
	
		@Override
	protected void handleRefuse(ACLMessage refuse) {
		App.LOGGER.log("Agent "+refuse.getSender().getName()+" refused to perform the requested action", true);
	}
	
	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			App.LOGGER.log("Responder does not exist", true);
		}
		else {
			App.LOGGER.log("Agent "+failure.getSender().getName()+" failed to perform the requested action", true);
		}
	}
	
	@Override
	protected void handleAgree(ACLMessage agree) {
		App.LOGGER.log("Agent "+agree.getSender().getName()+"agreed to perform the requested action", true);
	}
	
	@Override
	protected void handleInform(ACLMessage inform) {
		
		try {
			Object[] oMsg= (Object[]) inform.getContentObject();
			String req = (String) oMsg[0];
			TrashType trashType = (TrashType) oMsg[1];
			int amount = (Integer) oMsg[2];
			//App.LOGGER.log("REPLY CONTENT: " + req + " " + trashType.name() + " " + amount, true);
			this.truck.pickupGarbage(trashType, amount);
			this.truck.returnToCentral();
			App.LOGGER.log("Agent "+inform.getSender().getName()+" successfully performed the requested action: " + req, true);
			App.LOGGER.log(this.truck.showContents(), true);
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}