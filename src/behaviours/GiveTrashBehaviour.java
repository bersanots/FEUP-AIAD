package behaviours;

import java.io.IOException;


import agents.Compartment;
import agents.Container;
import general.App;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;

public class GiveTrashBehaviour extends AchieveREResponder {

	private Container container;
	private Compartment compartment;
	private int amount = -1;
	
	public GiveTrashBehaviour(Container container, MessageTemplate mt) {
		super(container, mt);
		// TODO Auto-generated constructor stub
		this.container = container;
		this.compartment = container.getCompartment();
	}

	protected boolean checkAction(ACLMessage msg) {
		
		try {
			Object[] o = (Object[]) msg.getContentObject();
			String req = (String) o[0];
			this.amount = (int) o[1];
			return req.equals("REQ") && !this.compartment.isEmpty();
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
		
	}

	protected int giveTrashToTruck() {
		this.container.stopAwaitingTruck();
		int takenTrash;
		if (amount > 0)
			takenTrash = this.compartment.removeContents(amount);
		else 
			takenTrash = this.compartment.emptyCompartment();		
		return takenTrash;
	}

	@Override
	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {		
		App.LOGGER.log("Agent " + this.getAgent().getLocalName() + ": Trash Pickup from "+ request.getSender().getLocalName(), true);
		if (checkAction(request)) {
			// We agree to perform the action. Note that in the FIPA-Request
			// protocol the AGREE message is optional. Return null if you
			// don't want to send it.
			App.LOGGER.log("Agent " + this.getAgent().getLocalName() + ": Agrees to pickup", true);
			ACLMessage agree = request.createReply();
			agree.setPerformative(ACLMessage.AGREE);
			return agree;
		} else {
			// We refuse to perform the action
			App.LOGGER.log("Agent " + this.getAgent().getLocalName() + ": Refuses Pickup", true);
			throw new RefuseException("check-failed");
		}
	}

	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		
		int trashTaken = giveTrashToTruck();
		if ( trashTaken >= 0) {
			App.LOGGER.log("Agent " + this.getAgent().getLocalName() + ": successfully gave trash", true);
			ACLMessage inform = request.createReply();
			
			Object[] oMsg=new Object[3];
	         oMsg[0] = "REQ";
	         oMsg[1] = compartment.getType();
	         oMsg[2] = trashTaken;
			try {
				inform.setContentObject(oMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//inform.setContent(compartment.getType().name() + " " + trashTaken);
			inform.setPerformative(ACLMessage.INFORM);
			return inform;
		} else {
			App.LOGGER.log("Agent " + this.getAgent().getLocalName() + ": Failed to remove trash", true);
			throw new FailureException("unexpected-error");
		}
	}
}