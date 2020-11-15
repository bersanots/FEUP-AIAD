package behaviours;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import agents.Truck;
import general.App;
import general.Position;
import general.TrashType;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

public class SetIntermediatePickupContractBehaviour extends ContractNetInitiator {

	private Truck truck;

	public SetIntermediatePickupContractBehaviour(Truck t, List<AID> containerAIDs) {
		super(t, buildMsg(containerAIDs));
		this.truck = t;
		// TODO Auto-generated constructor stub
	}

	private boolean evaluateProposal(Object o) {
		Object[] oMsg = (Object[]) o;
		String msgProt = (String) oMsg[0];
		TrashType t_type = (TrashType) oMsg[1];
		int amount = (int) oMsg[2];

		if (!msgProt.equals("CTCOUNTERPROP"))
			return false;

		int capacity = truck.getTypeCapacity(t_type);

		return (amount <= capacity);

	}

	protected static ACLMessage buildMsg(List<AID> truckAIDs) {
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		for (AID truckAID : truckAIDs) {
			msg.addReceiver(truckAID);
		}
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		// We want to receive a reply in 10 secs
		// msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		Object[] oMsg = new Object[1];
		oMsg[0] = "TPROP";

		try {
			msg.setContentObject(oMsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msg;
	}

	protected void handlePropose(ACLMessage propose, Vector v) {
		App.LOGGER.log("Agent " + propose.getSender().getLocalName() + " proposed intermediate pickup", true);
	}

	protected void handleRefuse(ACLMessage refuse) {
		App.LOGGER.log("Agent " + refuse.getSender().getLocalName() + " refused intermediate pickup", true);
	}

	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			App.LOGGER.log("Responder does not exist", true);
		} else {
			App.LOGGER.log("Agent " + failure.getSender().getLocalName() + " failed intermediate pickup", true);
		}
		// Immediate failure --> we will not receive a response from this agent
	}

	protected void handleAllResponses(Vector responses, Vector acceptances) {
		// Evaluate proposals.
		Object bestProposal = null;
		AID bestProposer = null;
		ACLMessage accept = null;
		Enumeration e = responses.elements();
		while (e.hasMoreElements()) {
			ACLMessage msg = (ACLMessage) e.nextElement();
			if (msg.getPerformative() == ACLMessage.PROPOSE) {
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				acceptances.addElement(reply);
				try {
					Object proposal = msg.getContentObject();
					if (evaluateProposal(proposal)) {
						bestProposal = proposal;
						bestProposer = msg.getSender();
						accept = reply;
					}
				} catch (UnreadableException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}
		// Accept the proposal of the best proposer
		if (accept != null) {
			App.LOGGER.log("Intermediate Pickup has been handed to " + bestProposer.getLocalName(), true);
			accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
		}
		else 
			truck.setScanning(false);
	}

	protected void handleInform(ACLMessage inform) {
		App.LOGGER.log("Agent " + inform.getSender().getLocalName() + " started the intermediate pickup", true);

		try {
			Object[] oMsg = (Object[]) inform.getContentObject();
			String msgProt = (String) oMsg[0];
			if (msgProt.equals("NEWCT")) {
				Position pos = (Position) oMsg[1];
				truck.startIntermediatePickup(pos, inform.getSender());
			}
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		truck.setScanning(false);
	}

}
