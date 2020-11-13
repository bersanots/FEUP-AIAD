package agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Container extends Agent {

	private Compartment compartment;
	private boolean isAwaitingTruck = false;
	private final int rate = 500;

	public Container(TRASH_TYPE type, int capacity) {
		this.compartment = new Compartment(type, capacity);
	}

	public void setup() {
		addBehaviour(new TrashGenerationBehaviour(this, rate));
		System.out.println("A new Container was created!");
		// add behaviours
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		addBehaviour(new GiveTrashBehaviour(this, template));
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
			Container container = (Container) this.getAgent();
			if (!compartment.isFull()) {				
				compartment.generateTrash();
			}
			if (compartment.isFull() && !isAwaitingTruck) {
				addBehaviour(new RequestPickupBehaviour(container));
				container.isAwaitingTruck = true;
			}
		}
	}

	public Compartment getCompartment() {
		return this.compartment;
	}
}
