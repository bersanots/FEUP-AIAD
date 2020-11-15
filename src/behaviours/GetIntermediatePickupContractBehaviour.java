package behaviours;

import java.io.IOException;

import agents.Compartment;
import agents.Container;
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

public class GetIntermediatePickupContractBehaviour extends ContractNetResponder {
	
	private Container container;
	private Compartment compartment;

	public GetIntermediatePickupContractBehaviour(Container container) {
		super(container, buildTemplate());
		this.container = container;
		this.compartment = container.getCompartment();
		// TODO Auto-generated constructor stub
	}
	
	protected static MessageTemplate buildTemplate(){
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );
		return template;
	}
	
	private void buildInformBodyMsg(ACLMessage msg) {		
		Object[] oMsg = new Object[2];
		oMsg[0] = "NEWCT";
		oMsg[1] = this.container.getPos();
		try {
			msg.setContentObject(oMsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean evaluateAction(ACLMessage cfp) {
		Object[] oMsg;
		try {
			oMsg = (Object[]) cfp.getContentObject();
			
			String msgProt = (String) oMsg[0];
			
			if (!msgProt.equals("TPROP"))
				return false;
			
			return this.compartment.getCurrentAmount() > 0;
			
		} catch (UnreadableException e) {
			return false;
		}
		
	}

	private boolean performAction() {
		return true;
	}
	
	private void buildProposal(ACLMessage msg) {
		
		container.waitForTruck();
		int currentAmount = compartment.getCurrentAmount();
		
		Object[] oMsg = new Object[3];
		oMsg[0] = "CTCOUNTERPROP";
		oMsg[1] = compartment.getType();
		oMsg[2] = currentAmount;
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
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Proposing " + compartment.getType().name() + " Intermediate Pickup");
			ACLMessage propose = cfp.createReply();
			propose.setPerformative(ACLMessage.PROPOSE);
			buildProposal(propose);
			return propose;
		} else {
			// We refuse to provide a proposal
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Refused Intermediate Pickup");
			throw new RefuseException("evaluation-failed");
		}
	}

	@Override
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
			throws FailureException {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Intermediate pickup proposal accepted");
		if (performAction()) {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": starting Intermediate pickup");
			ACLMessage inform = accept.createReply();
			inform.setPerformative(ACLMessage.INFORM);
			buildInformBodyMsg(inform);
			return inform;
		} else {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": failed to start Intermediate pickup");
			container.stopAwaitingTruck();
			throw new FailureException("unexpected-error");
		}
	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		System.out.println("Agent " + this.getAgent().getLocalName() + ": Intermediate pickup Proposal rejected");
		container.stopAwaitingTruck();
	}
}