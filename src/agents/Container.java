package agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

public class Container extends Agent {

	private Compartment compartment;

	private final int rate = 3000;
	
	
	
	public Container(TRASH_TYPE type, int capacity) {
		this.compartment = new Compartment(type, capacity);
	}

	public void setup() {
		addBehaviour(new TrashGenerationBehaviour(this, rate));	
		System.out.println("A new Container was created!");
		//add behaviours
	}

	public void takeDown() {
        System.out.println(getLocalName() + ": done working.");
    }
	
	class TrashGenerationBehaviour extends TickerBehaviour {
		
		public TrashGenerationBehaviour(Agent a, long period) {
			super(a, period);
			// TODO Auto-generated constructor stub
		}

		protected void onTick() {
			compartment.generateTrash();
		}
	}

}
