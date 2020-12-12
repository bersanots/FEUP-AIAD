package agents;

import java.awt.Color;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import general.App;
import general.ColorAssigner;
import general.Compartment;
import general.DFUtils;
import general.PickupRequest;
import general.Position;
import general.TrashType;
import behaviours.GetPickupContractBehaviour;
import behaviours.PickupTrashBehaviour;
import behaviours.SetIntermediatePickupContractBehaviour;
import jade.core.AID;
import sajas.core.Agent;
import sajas.domain.DFService;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Truck extends Agent implements Drawable{

	private List<Compartment> compartments;
	private Position centralPos = new Position(25,25);
	private Position pos = new Position(centralPos);
	private PickupRequest pickupRequest = null;
	private boolean isScanning = false;
	private boolean allowsIntermediatePickups;
	private boolean isMoving = false;
	// logging
	private Date pickup_start_time = null;
	private Position distance = null;
	private long average_time = 0;
	private double average_distance = 0;
	private int n_collections = 0;
	
	//drawable
	List<Color> colors = new ArrayList<>();
	

	public Truck(String type, int total_capacity) {
		this(type, total_capacity, true);
		
	}

	public Truck(TrashType type, int total_capacity, boolean allowsIntermediatePickups) {

		compartments = new ArrayList<>();
		this.allowsIntermediatePickups = allowsIntermediatePickups;
		compartments.add(new Compartment(type, total_capacity));
		//drawable
		setupColors();
		

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
			case "EletroGreen":
				compartments.add(new Compartment(TrashType.GREEN, total_capacity / 2));
				compartments.add(new Compartment(TrashType.ELETRONIC, total_capacity / 2));
				break;
			default:// regular (1 compartment)
				compartments.add(new Compartment(TrashType.REGULAR, total_capacity));
				break;
		}
		//drawable
		setupColors();
	}
	
	public Position getPos() {
		return this.pos;
	}
	
	private void setupColors() {
		Color currColor = new Color(0,0,0);
		for (int i = 0; i < 4; i++) {
			
			if (i < this.compartments.size()) {
				TrashType type = compartments.get(i).getType();
				currColor = ColorAssigner.assignColor(type);
			}
			
			if (i == 3) {
				currColor = new Color(0,0,0);
			}
			
			this.colors.add(currColor);
			
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
		App.LOGGER.createLogFile(this.getLocalName());
		String compartment_types = "";
		int individual_capacity = 0;

		for (Compartment c : this.compartments) {
			compartment_types += c.getType().name() + " ";
			individual_capacity = c.getCapacity();
		}

		App.LOGGER.log(this.getLocalName(), this.getLocalName() + " TYPES: " + compartment_types + " - COMPARTMENT CAPACITY: " + individual_capacity);
		// add behaviours

		// requestTrashPickup(new sajas.core.AID("container", AID.ISLOCALNAME), 10);
		// requestTrashPickup(new sajas.core.AID("container", AID.ISLOCALNAME), 15);
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
		return this.pickupRequest == null && pos.equals(this.centralPos);
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
			return this.pos.getDistance(this.centralPos) == 0;
		else
			return false;
	}

	public void moveTowardsPickup() {
		App.LOGGER.log(this.getLocalName() + " ==> " + this.pickupRequest.getContainerAID().getLocalName() + " : " + this.pos.toString(), true);
		Position step = this.pos.getUnitaryStep(pickupRequest.getPos());
		this.distance.sum(step.abs());
		this.pos.sum(step);
	}

	public void moveTowardsCentral() {
		App.LOGGER.log(this.getLocalName() + " ==> central : " + this.pos.toString(), true);
		Position step = this.pos.getUnitaryStep(this.centralPos);
		this.distance.sum(step.abs());
		this.pos.sum(step);
	}

	public void startPickup(Position pos, AID id) {
		this.pickupRequest = new PickupRequest(pos, id);
		this.setLoggingVars();
		App.LOGGER.log(this.getLocalName(), "0 - LEFT");
		App.LOGGER.log(this.getLocalName() + " started pickup", true);
		this.setOccupied();
	}

	public void startIntermediatePickup(Position pos, AID id) {
		this.pickupRequest = new PickupRequest(pos, id);
		App.LOGGER.log(this.getLocalName() + " started intermediate pickup", true);
	}

	public void returnToCentral() {
		this.pickupRequest = null;
	}

	public void endPickup() {
		App.LOGGER.log(this.getLocalName(), "1 - RETURNED");
		App.LOGGER.log(this.getLocalName(), "2 - DEPOSITED: " + this.showContents());

		long previous_time_sum = this.average_time * n_collections;
		double previous_distance_sum = this.average_distance * n_collections;
		this.n_collections++;

		Date curr_time = new Date(System.currentTimeMillis());
		long round_trip_time = App.LOGGER.getTimeDifference(this.pickup_start_time, curr_time, TimeUnit.SECONDS);
		App.LOGGER.log(this.getLocalName(), "3 - ROUND TRIP TIME: " + round_trip_time + " SECONDS");

		double trip_distance = this.distance.getDistance(this.centralPos);
		App.LOGGER.log(this.getLocalName(), "4 - TRIP DISTANCE: " + trip_distance);

		this.average_time = (previous_time_sum + round_trip_time) / this.n_collections;
		this.average_distance = (previous_distance_sum + trip_distance) / this.n_collections;
		App.LOGGER.log(this.getLocalName(), "5 - AVERAGE ROUND TRIP TIME: " + this.average_time + " SECONDS");
		App.LOGGER.log(this.getLocalName(), "6 - AVERAGE TRIP DISTANCE: " + this.average_distance);

		App.LOGGER.log(this.getLocalName() + " ended pickup", true);
		this.clearLoggingVars();
		this.pickupRequest = null;
		this.emptyCompartments();
		this.setAvailable();
	}

	private void setLoggingVars() {
		this.pickup_start_time = new Date(System.currentTimeMillis());
		this.distance = new Position(0, 0);
	}

	private void clearLoggingVars() {
		this.pickup_start_time = null;
		this.distance = null;
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
			List<AID> typeContainerIds = DFUtils.getService(this, "container" + t_type.name());
			containerIds.addAll(typeContainerIds);
		}
		for (AID containerId : containerIds) {
			App.LOGGER.log(containerId.getLocalName() + " is a candidate for intermediate pickup", true);
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
	
	public long getAverageTripTime()
	{
		return this.average_time;
	}
	
	public double getAverageTripDistance()
	{
		return this.average_distance;
	}
	
	@Override
	public void draw(SimGraphics g) {
		
		g.drawRect(colors.get(3));
		g.draw4ColorHollowRect(colors.get(0), colors.get(1), colors.get(2), colors.get(3));
	}

	@Override
	public int getX() {
		return this.pos.getX();
	}

	@Override
	public int getY() {
		return this.pos.getY();
	}
	
	public void moveStep() {
		
		if (!isMoving)
			return;
		
		if (isAvailable()) {
			endPickup();
			stopMoving();
		}
		else if (isReturning()) {
			if (!isScanning())
				moveTowardsCentral();
		}
		else {
			if (reachedContainer()) {
				requestTrashFullPickup();
				App.LOGGER.log(getLocalName() + " reached destination");
			}
			else moveTowardsPickup();
		}
	}

	private void stopMoving() {
		isMoving = false;
	}
	
	public void startMoving() {
		isMoving = true;
	}
}
