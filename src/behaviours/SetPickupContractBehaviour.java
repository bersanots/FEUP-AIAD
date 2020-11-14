package behaviours;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import agents.Central;
import general.Position;
import general.TrashType;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

class SetPickupContractBehaviour extends ContractNetInitiator {
	
	private Central central;
	private int amount = 0;
	private TrashType trashType = TrashType.REGULAR;

	public SetPickupContractBehaviour(Central central, int amount, TrashType trashType, Position pos, List<AID> truckAIDs, AID containerAID) {
		super(central, buildMsg(amount, trashType, pos, truckAIDs, containerAID));
		// TODO Auto-generated constructor stub
		this.central = central;
		this.amount = amount;
		this.trashType = trashType;
	}
	
	private boolean evaluateProposal(Object o) {
		Object[] oMsg = (Object[]) o;
		String msgProt = (String) oMsg[0];
		TrashType t_type = (TrashType)oMsg[1];
		int capacity = (int)oMsg[2];
		
		if (!msgProt.equals("TPROP"))
			return false;
		
		return (t_type== this.trashType && this.amount <= capacity);
		
	}

	protected static ACLMessage buildMsg(int amount, TrashType trashType, Position pos,
			List<AID> truckAIDs, AID containerAID) {
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
  		for (AID truckAID : truckAIDs) {
  			msg.addReceiver(truckAID);
  		}
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			// We want to receive a reply in 10 secs
			//msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
			Object[] oMsg = new Object[5];
			oMsg[0] = "CPROP";
			oMsg[1] = trashType;
			oMsg[2] = amount;
			oMsg[3] = pos;
			oMsg[4] = containerAID;
			
			try {
				msg.setContentObject(oMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return msg;
	}

	protected void handlePropose(ACLMessage propose, Vector v) {
		System.out.println("Agent "+propose.getSender().getName()+" proposed ");
	}
	
	protected void handleRefuse(ACLMessage refuse) {
		System.out.println("Agent "+refuse.getSender().getName()+" refused");
	}
	
	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			System.out.println("Responder does not exist");
		}
		else {
			System.out.println("Agent "+failure.getSender().getName()+" failed");
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
			System.out.println("Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
			accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
		}						
	}
	
	protected void handleInform(ACLMessage inform) {
		System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
	}
}