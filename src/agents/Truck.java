package agents;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import general.App;
import general.DFUtils;
import general.PickupRequest;
import general.PickupRequestInfo;
import general.Position;
import general.TrashType;
import behaviours.GetPickupContractBehaviour;
import behaviours.MoveTruckBehaviour;
import behaviours.PickupTrashBehaviour;
import behaviours.SetIntermediatePickupContractBehaviour;
import behaviours.SetPickupContractBehaviour;
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
	private boolean isScanning = false;
	private boolean allowsIntermediatePickups;

	public Truck(String type, int total_capacity) {
		this(type, total_capacity, true);
	}
	
	public Truck(TrashType type, int total_capacity, boolean allowsIntermediatePickups) {

		compartments = new ArrayList<>();
		this.allowsIntermediatePickups = allowsIntermediatePickups;
		compartments.add(new Compartment(type, total_capacity));
			
	}
	
	public Truck(TrashType type, int total_capacity) {

		this(type, total_capacity, true);
			
	}
	
	public Truck(String type, int total_capacity, boolean allowsIntermediatePickups) {

		compartments = new ArrayList<>();
		this.allowsIntermediatePickups = allowsIntermediatePickups;
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
		default:// regular (1 compartment)
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

	public void requestTrashPickup(int amount) {

		AID containerAID = this.pickupRequest.getContainerAID();
		ACLMessage msg = this.buildPickupGarbageMsg(containerAID, amount);
		addBehaviour(new PickupTrashBehaviour(this, msg));
	}

	public void requestTrashFullPickup() {
		requestTrashPickup(-1);
	}

	public void setup() {
		App.LOGGER.log("A new Truck was created!", true);
		// add behaviours

		// requestTrashPickup(new AID("container", AID.ISLOCALNAME), 10);
		// requestTrashPickup(new AID("container", AID.ISLOCALNAME), 15);
		this.setAvailable();
		addBehaviour(new GetPickupContractBehaviour(this));
	}

	public void takeDown() {
		App.LOGGER.log(getLocalName() + ": done working.", true);
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
		String content_str = "";
		for (Compartment compartment : compartments) {
			content_str = content_str + "Type: " + compartment.getType() + " | Amount: "
					+ compartment.getCurrentAmount() + " - ";
		}
		return content_str.substring(0, content_str.length() - 3);
	}

	public boolean isAvailable() {
		return this.pickupRequest == null && pos.equals(new Position(0, 0));
	}

	public boolean isReturning() {
		return !this.isAvailable() && this.pickupRequest == null;
	}

	public boolean reachedContainer() {
		if (pickupRequest != null)
			return this.pos.getDistance(pickupRequest.getPos()) == 0;
		else
			return false;
	}

	public boolean reachedCentral() {
		if (pickupRequest != null)
			return this.pos.getDistance(new Position(0, 0)) == 0;
		else
			return false;
	}

	public void moveTowardsPickup() {
		App.LOGGER.log(this.getLocalName() + " ==> " + this.pickupRequest.getContainerAID().getLocalName() + " : "
				+ this.pos.toString(), true);
		this.pos.sum(this.pos.getUnitaryStep(pickupRequest.getPos()));
	}

	public void moveTowardsCentral() {
		App.LOGGER.log(this.getLocalName() + " ==> central : " + this.pos.toString(), true);
		this.pos.sum(this.pos.getUnitaryStep(new Position(0, 0)));
	}

	public void startPickup(Position pos, AID id) {
		this.pickupRequest = new PickupRequest(pos, id);
		App.LOGGER.log(this.getLocalName() + " started pickup", true);
		this.setOccupied();
	}

	public void startIntermediatePickup(Position pos, AID id) {
		this.pickupRequest = new PickupRequest(pos, id);
		System.out.println(this.getLocalName() + " started intermediate pickup");
	}

	public void returnToCentral() {
		this.pickupRequest = null;
	}

	public void endPickup() {
		App.LOGGER.log(this.getLocalName() + " ended pickup", true);
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

	public void searchForContainers() {
		
		if (!this.allowsIntermediatePickups)
			return;
		
		isScanning = true;

		List<AID> containerIds = new ArrayList<>();
		for (Compartment compartment : compartments) {
			TrashType t_type = compartment.getType();
			if (!this.getTypeCompartment(t_type).isFull()) {
				List<AID> typeTruckIds = DFUtils.getService(this, "container" + t_type.name());
				containerIds.addAll(typeTruckIds);
			}
		}
		for (AID containerId : containerIds) {
			System.out.println("OI " + containerId.getName());
		}
		if (containerIds.size() != 0) {
			this.addBehaviour(new SetIntermediatePickupContractBehaviour(this, containerIds));
		} else {
			isScanning = false;
		}

	}

	public boolean isScanning() {
		return isScanning;
	}

	public void setScanning(boolean isScanning) {
		this.isScanning = isScanning;
	}
}
