package behaviours;

import java.io.IOException;

import agents.Container;
import general.App;
import general.Compartment;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

public class RequestPickupBehaviour extends AchieveREInitiator {

	private Container container;
	private Compartment compartment;

	public RequestPickupBehaviour(Container container) {
		super(container, createMsg(container));
		container.waitForTruck();
		this.container = container;
		this.compartment = container.getCompartment();
		

	}

	protected static ACLMessage createMsg(Container container) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(new AID("central", AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		// msg.setContent("dummy-action");
		Object[] oMsg = new Object[4];
		oMsg[0] = "REQPIC";
		oMsg[1] = container.getCompartment().getType();
		oMsg[2] = container.getCompartment().getCurrentAmount();
		oMsg[3] = container.getPos();
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
		App.LOGGER.log("Agent " + refuse.getSender().getLocalName() + " refused to order pickup", true);
		this.container.stopAwaitingTruck();
	}

	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			App.LOGGER.log("Responder does not exist", true);
		} else {
			App.LOGGER.log("Agent " + failure.getSender().getLocalName() + " failed to order pickup", true);
		}
		this.container.stopAwaitingTruck();
	}

	@Override
	protected void handleAgree(ACLMessage agree) {
		App.LOGGER.log("Agent " + agree.getSender().getLocalName() + " agreed to order pickup", true);
	}

	@Override
	protected void handleInform(ACLMessage inform) {
		App.LOGGER.log("Agent " + inform.getSender().getLocalName() + " successfully ordered pickup", true);

		// try {
		Object[] oMsg;
		try {
			oMsg = (Object[]) inform.getContentObject();
			String req = (String) oMsg[0];
			String status = (String) oMsg[1];
			if (!status.equals("OK")) {
				App.LOGGER.log("NO TRUCK COMING :'(", true);
				this.container.stopAwaitingTruck();
			}
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.container.stopAwaitingTruck();
		}

		// int amount = (Integer) oMsg[2];
		// System.out.println("REPLY CONTENT: " + req + " " + trashType.name() + " " +
		// amount);

		/*
		 * } catch (UnreadableException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

	}
}