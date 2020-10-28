package agents;

import jade.core.Agent;

public class Truck extends Agent {

	public Truck() {}

	public void setup() {
		System.out.println("A new Truck was created!");
		//add behaviours
	}

	public void takeDown() {
        System.out.println(getLocalName() + ": done working.");
    }
	
}
