package agents;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;

class GiveTrashBehaviour extends AchieveREResponder {

	public GiveTrashBehaviour(Agent a, MessageTemplate mt) {
		super(a, mt);
		// TODO Auto-generated constructor stub
	}

	protected boolean checkAction() {
		return true;
	}

	protected boolean performAction() {
		return true;
	}

	@Override
	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
		System.out.println("Agent " + this.getAgent().getLocalName() + ": REQUEST received from "
				+ request.getSender().getName() + ". Action is " + request.getContent());
		if (checkAction()) {
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
		if (performAction()) {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Action successfully performed");
			ACLMessage inform = request.createReply();
			inform.setContent("BROU");
			inform.setPerformative(ACLMessage.INFORM);
			return inform;
		} else {
			System.out.println("Agent " + this.getAgent().getLocalName() + ": Action failed");
			throw new FailureException("unexpected-error");
		}
	}
}