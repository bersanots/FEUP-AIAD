package behaviours;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import agents.Central;
import general.App;
import general.PickupRequestInfo;
import general.Position;
import general.TrashType;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

public class SetPickupContractBehaviour extends ContractNetInitiator {
	
	private Central central;
	private PickupRequestInfo reqInfo;

	public SetPickupContractBehaviour(Central central, PickupRequestInfo reqInfo, List<AID> truckAIDs) {
		super(central, buildMsg(reqInfo, truckAIDs));
		// TODO Auto-generated constructor stub
		this.central = central;
		this.reqInfo = reqInfo;
	}
	
	private boolean evaluateProposal(Object o) {
		Object[] oMsg = (Object[]) o;
		String msgProt = (String) oMsg[0];
		TrashType t_type = (TrashType)oMsg[1];
		int capacity = (int)oMsg[2];
		
		if (!msgProt.equals("TPROP"))
			return false;
		
		return (t_type== reqInfo.getTrashType() && reqInfo.getAmount() <= capacity);
		
	}

	protected static ACLMessage buildMsg(PickupRequestInfo reqInfo,
			List<AID> truckAIDs) {
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
  		for (AID truckAID : truckAIDs) {
  			msg.addReceiver(truckAID);
  		}
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			// We want to receive a reply in 10 secs
			//msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
			Object[] oMsg = new Object[2];
			oMsg[0] = "CPROP";
			oMsg[1] = reqInfo;
			
			try {
				msg.setContentObject(oMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return msg;
	}

	protected void handlePropose(ACLMessage propose, Vector v) {
		App.LOGGER.log("Agent "+propose.getSender().getLocalName()+" proposed pickup", true);
	}
	
	protected void handleRefuse(ACLMessage refuse) {
		App.LOGGER.log("Agent "+refuse.getSender().getLocalName()+" refused pickup", true);
	}
	
	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			App.LOGGER.log("Responder does not exist", true);
		}
		else {
			App.LOGGER.log("Agent "+failure.getSender().getLocalName()+" failed pickup", true);
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
			App.LOGGER.log("Pickup has been handed to " + bestProposer.getLocalName(), true);
			accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
		}
		else 
			central.insertRequest(reqInfo);
	}
	
	protected void handleInform(ACLMessage inform) {
		App.LOGGER.log("Agent "+inform.getSender().getLocalName()+" started the pickup", true);
	}
}