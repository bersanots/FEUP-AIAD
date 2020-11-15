package behaviours;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import agents.Truck;
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
		System.out.println("Agent "+refuse.getSender().getLocalName()+" refused to give trash");
	}
	
	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			System.out.println("Responder does not exist");
		}
		else {
			System.out.println("Agent "+failure.getSender().getLocalName()+" failed to give trash");
		}
	}
	
	@Override
	protected void handleAgree(ACLMessage agree) {
		System.out.println("Agent "+agree.getSender().getLocalName()+"agreed to give trash");
	}
	
	@Override
	protected void handleInform(ACLMessage inform) {
		
		try {
			Object[] oMsg= (Object[]) inform.getContentObject();
			String req = (String) oMsg[0];
			TrashType trashType = (TrashType) oMsg[1];
			int amount = (Integer) oMsg[2];
			//System.out.println("REPLY CONTENT: " + req + " " + trashType.name() + " " + amount);
			this.truck.pickupGarbage(trashType, amount);
			this.truck.returnToCentral();
			truck.searchForContainers();
			System.out.println("Agent "+inform.getSender().getLocalName()+" successfully picked up trash: " + req);
			System.out.println(this.truck.showContents());
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}