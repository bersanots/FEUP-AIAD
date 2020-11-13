package agents;

import java.util.Vector;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

class PickupTrashBehaviour extends AchieveREInitiator {

	public PickupTrashBehaviour(Agent a, ACLMessage msg) {
		super(a, msg);
		// TODO Auto-generated constructor stub
	}
	
	
		@Override
	protected void handleRefuse(ACLMessage refuse) {
		System.out.println("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
	}
	
	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			System.out.println("Responder does not exist");
		}
		else {
			System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
		}
	}
	
	@Override
	protected void handleAgree(ACLMessage agree) {
		System.out.println("Agent "+agree.getSender().getName()+"agreed to perform the requested action");
	}
	
	@Override
	protected void handleInform(ACLMessage inform) {
		System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
		System.out.println("REPLY CONTENT: " + inform.getContent());
	}
}