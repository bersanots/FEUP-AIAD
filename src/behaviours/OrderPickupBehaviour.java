package behaviours;

import general.DFUtils;
import general.TrashType;

import java.io.IOException;
import java.util.List;
import agents.Central;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;

public class OrderPickupBehaviour extends AchieveREResponder {

	private Central central;
	private int amount = 0;
	private TrashType trashType = TrashType.REGULAR;

	public OrderPickupBehaviour(Central central) {
		
		super(central, buildTemplate());
		// TODO Auto-generated constructor stub
		this.central = central;
	}
	
	protected static MessageTemplate buildTemplate(){
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		return template;
	}

	protected boolean checkAction(ACLMessage request) {
		boolean ret = true;
		try {
			Object[] oMsg = (Object[]) request.getContentObject();
			String req = (String) oMsg[0];
			if (req.equals("REQPIC")) {
			this.trashType = (TrashType) oMsg[1];
			this.amount = (Integer) oMsg[2];
			System.out.println("REQUEST CONTENT: " + req + " " + trashType.name() + " " + amount);
			}
			else 
				ret = false;
			
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	protected boolean performAction(TrashType t_type, int amount, AID containerAID) {
		
		List<AID> truckIds = DFUtils.getService(central, "truck" + t_type.name());
		System.out.println("ACTIONI");
		for (AID truckId : truckIds) {
			System.out.println("OI " + truckId.getName());
		}
		this.central.addBehaviour(new SetPickupContractBehaviour(this.central, amount, t_type, truckIds, containerAID));
		return true;
	}

	@Override
	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
		System.out.println(
				"Agent " + this.getAgent().getLocalName() + ": REQUEST received from " + request.getSender().getName()); // +
																															// ".
																															// Action
																															// is
																															// "
																															// +
																															// request.getContent());
		if (checkAction(request)) {
			// We agree to perform the action. Note that in the FIPA-Request
			// protocol the AGREE message is optional. Return null if you
			// don't want to send it.
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Agree");
			ACLMessage agree = request.createReply();
			agree.setPerformative(ACLMessage.AGREE);
			return agree;
		} else {
			// We refuse to perform the action
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Refuse");
			throw new RefuseException("check-failed");
		}
	}

	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		if (performAction(this.trashType, this.amount, request.getSender())) {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Action successfully performed");
			ACLMessage inform = request.createReply();

			Object[] oMsg = new Object[3];
			oMsg[0] = "REQ";
			oMsg[1] = this.trashType;
			oMsg[2] = this.amount;
			try {
				inform.setContentObject(oMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// inform.setContent(compartment.getType().name() + " " + trashTaken);
			inform.setPerformative(ACLMessage.INFORM);
			return inform;
		} else {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Action failed");
			throw new FailureException("unexpected-error");
		}
	}
}