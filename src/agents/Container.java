package agents;

import jade.core.Agent;

public class Container extends Agent {

	public Container() {}

	public void setup() {
		System.out.println("A new Container was created!");
		//add behaviours
	}

	public void takeDown() {
        System.out.println(getLocalName() + ": done working.");
    }
	
}
