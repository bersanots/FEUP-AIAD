package behaviours;

import java.io.IOException;

import agents.Truck;
import general.PickupRequestInfo;
import general.Position;
import general.TrashType;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;

public class GetPickupContractBehaviour extends ContractNetResponder {
	
	private Truck truck;
	private TrashType t_type = TrashType.REGULAR;
	private AID containerAID;
	private Position containerPos;

	public GetPickupContractBehaviour(Truck a) {
		super(a, buildTemplate());
		this.truck = a;
		// TODO Auto-generated constructor stub
	}
	
	protected static MessageTemplate buildTemplate(){
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );
		return template;
	}
	
	private boolean evaluateAction(ACLMessage cfp) {
		Object[] oMsg;
		try {
			oMsg = (Object[]) cfp.getContentObject();
			
			String msgProt = (String) oMsg[0];
			PickupRequestInfo reqInfo = (PickupRequestInfo) oMsg[1];
			
			this.containerAID = reqInfo.getPickupRequest().getContainerAID();
			this.containerPos = reqInfo.getPickupRequest().getPos();
			this.t_type = reqInfo.getTrashType();
			int amount = reqInfo.getAmount();
			
			if (!msgProt.equals("CPROP"))
				return false;
			
			return (truck.hasType(t_type) && truck.hasTypeCapacity(t_type, amount) && truck.isAvailable());
			
		} catch (UnreadableException e) {
			return false;
		}
		
	}

	private boolean performAction() {
		// Simulate action execution by generating a random number
		this.truck.startPickup(containerPos, containerAID);
		this.truck.addBehaviour(new MoveTruckBehaviour(this.truck, 1000, containerAID, 15));
		return true;
	}
	
	private void buildProposal(ACLMessage msg) {
		int capacity = truck.getTypeCapacity(t_type);
		
		Object[] oMsg = new Object[3];
		oMsg[0] = "TPROP";
		oMsg[1] = t_type;
		oMsg[2] = capacity;
		try {
			msg.setContentObject(oMsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
		/*System.out.println("Agent " + this.getAgent().getLocalName() + ": CFP received from " + cfp.getSender().getLocalName()
				+ ". Action is ");*/
		
		if (evaluateAction(cfp)) {
			// We provide a proposal
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Proposing " + t_type.name() + " Pickup");
			ACLMessage propose = cfp.createReply();
			propose.setPerformative(ACLMessage.PROPOSE);
			buildProposal(propose);
			return propose;
		} else {
			// We refuse to provide a proposal
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Refused Pickup");
			throw new RefuseException("evaluation-failed");
		}
	}

	@Override
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
			throws FailureException {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": pickup proposal accepted");
		if (performAction()) {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": starting pickup");
			ACLMessage inform = accept.createReply();
			inform.setPerformative(ACLMessage.INFORM);
			return inform;
		} else {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": failed to start pickup");
			throw new FailureException("unexpected-error");
		}
	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		System.out.println("Agent " + this.getAgent().getLocalName() + ": pickup Proposal rejected");
	}
}