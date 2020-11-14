package agents;

import java.util.*;

import behaviours.OrderPickupBehaviour;
import general.Position;
import jade.core.Agent;

public class Central extends Agent {

	private Set<Truck> trucks = new HashSet<>();
	private Set<Container> containers = new HashSet<>();
	private Position pos = new Position(0, 0);

	public Central() {}

	public void setup() {
		System.out.println("A new CENTRALI was created!");
		//add behaviours
		addBehaviour(new OrderPickupBehaviour(this));
	}

	public void addTruck(Truck t) {
		trucks.add(t);
	}

	public void addContainer(Container c) {
		containers.add(c);
	}

	public Set<Truck> getTrucks() {
		return trucks;
	}

	public Set<Container> getContainers() {
		return containers;
	}

}
