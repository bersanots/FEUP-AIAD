package agents;

import jade.core.Agent;

public class Container extends Agent {

	private Compartment compartment;
	
	public Container(TRASH_TYPE type, int capacity) {
		this.compartment = new Compartment(type, capacity);
	}

	public void setup() {
		System.out.println("A new Container was created!");
		//add behaviours
	}

	public void takeDown() {
        System.out.println(getLocalName() + ": done working.");
    }
	
}
