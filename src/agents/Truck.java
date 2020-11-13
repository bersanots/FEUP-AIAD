package agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public class Truck extends Agent {

	private List<Compartment> compartments;

	public Truck(String type, int total_capacity) {

		compartments = new ArrayList<>();
		switch (type) {

		case "Recycling":
			compartments.add(new Compartment(TRASH_TYPE.BLUE, total_capacity / 3));
			compartments.add(new Compartment(TRASH_TYPE.YELLOW, total_capacity / 3));
			compartments.add(new Compartment(TRASH_TYPE.GREEN, total_capacity / 3));
			break;
		case "Urgent":
			compartments.add(new Compartment(TRASH_TYPE.REGULAR, total_capacity / 2));
			compartments.add(new Compartment(TRASH_TYPE.ORGANIC, total_capacity / 2));
			break;
		case "Simple":// simple (1 compartment)
			compartments.add(new Compartment(TRASH_TYPE.REGULAR, total_capacity));
			break;
		}
	}

	private ACLMessage buildPickupGarbageMsg(int amount) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(new AID("container", AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		// msg.setContent("dummy-action");
		Object[] oMsg = new Object[3];
		oMsg[0] = "REQ";
		oMsg[1] = amount;
		try {
			msg.setContentObject(oMsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msg;
	}

	private void requestTrashPickup(int amount) {

		ACLMessage msg = this.buildPickupGarbageMsg(amount);
		addBehaviour(new PickupTrashBehaviour(this, msg));
	}

	public void setup() {
		System.out.println("A new Truck was created!");
		// add behaviours

		requestTrashPickup(10);
		requestTrashPickup(15);
		this.setAvailable();
	}

	public void takeDown() {
		System.out.println(getLocalName() + ": done working.");
	}

	private Compartment getTypeCompartment(TRASH_TYPE type) {

		for (Compartment compartment : compartments) {
			if (compartment.getType() == type)
				return compartment;
		}
		return null;
	}

	public boolean hasType(TRASH_TYPE type) {

		for (Compartment compartment : compartments) {
			if (compartment.getType() == type)
				return true;
		}
		return false;
	}

	public void pickupGarbage(TRASH_TYPE type, int amount) {

		Compartment compartment = this.getTypeCompartment(type);

		if (compartment != null) {
			compartment.addContents(amount);
		}
	}

	public void emptyCompartments() {
		for (Compartment compartment : compartments) {
			compartment.emptyCompartment();
		}
	}

	public List<Compartment> getCompartments() {
		return this.compartments;
	}

	public String showContents() {
		String oi = "";
		for (Compartment compartment : compartments) {
			oi = oi + "Type: " + compartment.getType() + " | Amount: " + compartment.getCurrentAmount() + "\n";
		}
		return oi;
	}

	public void setAvailable() {

		for (Compartment compartment : compartments) {
			ServiceDescription sd = new ServiceDescription();
			sd.setType("truck" + compartment.getType().name());
			sd.setName(getLocalName());
			DFUtils.register(this, sd);
		}
	}

	public void setOccupied() {

		for (Compartment compartment : compartments) {
			try {
				DFService.deregister(this);
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
