package agents;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import general.DFUtils;
import general.PickupRequest;
import general.Position;
import general.TrashType;
import behaviours.GetPickupContractBehaviour;
import behaviours.MoveTruckBehaviour;
import behaviours.PickupTrashBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public class Truck extends Agent {

	private List<Compartment> compartments;
	private Position pos = new Position(0, 0);
	private PickupRequest pickupRequest = null;

	public Truck(String type, int total_capacity) {

		compartments = new ArrayList<>();
		switch (type) {

		case "Recycling":
			compartments.add(new Compartment(TrashType.BLUE, total_capacity / 3));
			compartments.add(new Compartment(TrashType.YELLOW, total_capacity / 3));
			compartments.add(new Compartment(TrashType.GREEN, total_capacity / 3));
			break;
		case "Urgent":
			compartments.add(new Compartment(TrashType.REGULAR, total_capacity / 2));
			compartments.add(new Compartment(TrashType.ORGANIC, total_capacity / 2));
			break;
		case "Simple":// simple (1 compartment)
			compartments.add(new Compartment(TrashType.REGULAR, total_capacity));
			break;
		}
	}

	private ACLMessage buildPickupGarbageMsg(AID container_AID, int amount) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(container_AID);
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

	public void requestTrashPickup(AID containerAID, int amount) {

		ACLMessage msg = this.buildPickupGarbageMsg(containerAID, amount);
		addBehaviour(new PickupTrashBehaviour(this, msg));
	}
	
	

	public void setup() {
		System.out.println("A new Truck was created!");
		// add behaviours

		// requestTrashPickup(new AID("container", AID.ISLOCALNAME), 10);
		// requestTrashPickup(new AID("container", AID.ISLOCALNAME), 15);
		this.setAvailable();
		addBehaviour(new GetPickupContractBehaviour(this));
	}

	public void takeDown() {
		System.out.println(getLocalName() + ": done working.");
	}

	private Compartment getTypeCompartment(TrashType type) {

		for (Compartment compartment : compartments) {
			if (compartment.getType() == type)
				return compartment;
		}
		return null;
	}
	
	public boolean hasTypeCapacity(TrashType type, int amount) {

		Compartment c = getTypeCompartment(type);
		return c.hasCapacity(amount);
	}
	
	public int getTypeCapacity(TrashType type) {

		Compartment c = getTypeCompartment(type);
		return c.getCapacity();
	}

	public boolean hasType(TrashType type) {

		for (Compartment compartment : compartments) {
			if (compartment.getType() == type)
				return true;
		}
		return false;
	}

	public void pickupGarbage(TrashType type, int amount) {

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
	
	public boolean isAvailable() {
		return this.pickupRequest == null && pos.equals(new Position(0,0));
	}
	
	public boolean isReturning() {
		return !this.isAvailable() && this.pickupRequest == null;
	}
	
	public boolean reachedContainer() {
		if (pickupRequest != null)
			return this.pos.getDistance(pickupRequest.getPos()) == 0;
		else return false;		
	}
	
	public boolean reachedCentral() {
		if (pickupRequest != null)
			return this.pos.getDistance( new Position(0,0) ) == 0;
		else return false;		
	}
	
	public void moveTowardsPickup() {
		System.out.println("Moving toward container: " + this.pos.toString() );
		this.pos.sum( this.pos.getUnitaryStep( pickupRequest.getPos() ) );
	}
	
	public void moveTowardsCentral() {
		System.out.println("Moving toward central: " + this.pos.toString() );
		this.pos.sum( this.pos.getUnitaryStep( new Position(0,0) ) );
	}
	
	public void startPickup(Position pos, AID id) {
		this.pickupRequest = new PickupRequest(pos, id);
		System.out.println(this.getLocalName() + " started pickup");
		this.setOccupied();
	}
	
	public void returnToCentral() {
		this.pickupRequest = null;
	}
	
	public void endPickup() {
		System.out.println(this.getLocalName() + " ended pickup");
		this.pickupRequest = null;
		this.emptyCompartments();
		this.setAvailable();
	}

	public void setAvailable() {
		
		List<ServiceDescription> services = new ArrayList<>();
		for (Compartment compartment : compartments) {
			ServiceDescription sd = new ServiceDescription();
			sd.setType("truck" + compartment.getType().name());
			sd.setName(getLocalName());
			services.add(sd);
			
		}
		DFUtils.registerMultipleServices(this, services);	
	}

	public void setOccupied() {

		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
