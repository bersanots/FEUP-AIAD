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
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;

public class GiveTrashBehaviour extends AchieveREResponder {

	private Container container;
	private Compartment compartment;
	
	public GiveTrashBehaviour(Container container, MessageTemplate mt) {
		super(container, mt);
		// TODO Auto-generated constructor stub
		this.container = container;
		this.compartment = container.getCompartment();
	}

	protected boolean checkAction() {
		return !this.compartment.isEmpty();
	}

	protected int giveTrashToTruck() {
		this.container.stopAwaitingTruck();
		return this.compartment.emptyCompartment();		
	}

	@Override
	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
		System.out.println("Agent " + this.getAgent().getLocalName() + ": Trash Pickup from "
				+ request.getSender().getLocalName() );
		if (checkAction()) {
			// We agree to perform the action. Note that in the FIPA-Request
			// protocol the AGREE message is optional. Return null if you
			// don't want to send it.
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Agrees to pickup");
			ACLMessage agree = request.createReply();
			agree.setPerformative(ACLMessage.AGREE);
			return agree;
		} else {
			// We refuse to perform the action
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Refuses Pickup");
			throw new RefuseException("check-failed");
		}
	}

	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		
		int trashTaken = giveTrashToTruck();
		if ( trashTaken >= 0) {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Trash successfully removed");
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
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Failed to remove trash");
			throw new FailureException("unexpected-error");
		}
	}
}