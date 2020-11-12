package agents;


import java.util.ArrayList;
import java.util.List;

import jade.core.Agent;

public class Truck extends Agent {

	private List<Compartment> compartments;
	public Truck(String type, int total_capacity) {
		
		compartments = new ArrayList<>();
		switch (type) {
		
		
		case "Recycling":
			compartments.add(new Compartment(TRASH_TYPE.BLUE, total_capacity/3));
			compartments.add(new Compartment(TRASH_TYPE.YELLOW, total_capacity/3));
			compartments.add(new Compartment(TRASH_TYPE.GREEN, total_capacity/3));
			break;
		case "Urgent":
			compartments.add(new Compartment(TRASH_TYPE.REGULAR, total_capacity/2));
			compartments.add(new Compartment(TRASH_TYPE.ORGANIC, total_capacity/2));
			break;
		case "Simple"://simple (1 compartment)
			compartments.add(new Compartment(TRASH_TYPE.REGULAR, total_capacity));
			break;
		}
	}

	public void setup() {
		System.out.println("A new Truck was created!");
		//add behaviours
	}

	public void takeDown() {
        System.out.println(getLocalName() + ": done working.");
    }
	
	private Compartment getTypeCompartment(TRASH_TYPE type) { 
		
		for (Compartment compartment: compartments) {			
			if (compartment.getType() == type) 
				return compartment;
		}
		return null;
	}
	
	public boolean hasType(TRASH_TYPE type){
		
		for (Compartment compartment: compartments) {			
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
		for (Compartment compartment: compartments) {			
			compartment.emptyCompartment();
		}
	}
}
