package behaviours;

import general.App;
import general.DFUtils;
import general.PickupRequest;
import general.PickupRequestInfo;
import general.Position;
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
	private Position pos;

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
			this.pos = (Position) oMsg[3];
			App.LOGGER.log("REQUEST CONTENT: " + req + " " + trashType.name() + " " + amount, true);
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
		
		PickupRequest req = new PickupRequest(this.pos, containerAID);
		PickupRequestInfo reqInfo = new PickupRequestInfo(req, amount, t_type);		
		this.central.requestPickup(reqInfo);
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
			App.LOGGER.log("Agent " + this.getAgent().getLocalName() + ": Agree", true);
			ACLMessage agree = request.createReply();
			agree.setPerformative(ACLMessage.AGREE);
			return agree;
		} else {
			// We refuse to perform the action
			App.LOGGER.log("Agent " + this.getAgent().getLocalName() + ": Refuse", true);
			throw new RefuseException("check-failed");
		}
	}

	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		if (performAction(this.trashType, this.amount, request.getSender())) {
			App.LOGGER.log("Agent " + this.getAgent().getLocalName() + ": Action successfully performed", true);
			ACLMessage inform = request.createReply();

			Object[] oMsg = new Object[2];
			oMsg[0] = "REQ";
			oMsg[1] = "OK";
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
			App.LOGGER.log("Agent " + this.getAgent().getLocalName() + ": Action failed", true);
			throw new FailureException("unexpected-error");
		}
	}
}