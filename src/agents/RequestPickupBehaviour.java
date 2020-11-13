package agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

class RequestPickupBehaviour extends AchieveREInitiator {
	
	
	private Compartment compartment;
	
	public RequestPickupBehaviour(Container container) {
		super(container, createMsg(container.getCompartment()));
		
		this.compartment = container.getCompartment();
		
	}
	
	protected static ACLMessage createMsg(Compartment compartment) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(new AID("central", AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		// msg.setContent("dummy-action");
		Object[] oMsg = new Object[3];
		oMsg[0] = "REQPIC";
		oMsg[1] = compartment.getType();
		oMsg[2] = compartment.getCurrentAmount();
		try {
			msg.setContentObject(oMsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msg;
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
		
		//try {
			//Object[] oMsg= (Object[]) inform.getContentObject();
			//String req = (String) oMsg[0];
			//TRASH_TYPE trashType = (TRASH_TYPE) oMsg[1];
			//int amount = (Integer) oMsg[2];
			//System.out.println("REPLY CONTENT: " + req + " " + trashType.name() + " " + amount);
			System.out.println("PICKUP ORDERED YAY");
			/*} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
}