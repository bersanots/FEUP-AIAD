package agents;

import behaviours.GiveTrashBehaviour;

import behaviours.RequestPickupBehaviour;
import general.App;
import general.Position;
import general.TrashType;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Container extends Agent {

	private Compartment compartment;
	private Position pos;
	private boolean isAwaitingTruck = false;
	private final int rate = 500;

	public Container(TrashType type, int capacity, Position pos) {
		this.compartment = new Compartment(type, capacity);
		this.pos = pos;
	}

	public void setup() {
		addBehaviour(new TrashGenerationBehaviour(this, rate));
		App.LOGGER.log("A new Container was created!", true);
		// add behaviours
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		addBehaviour(new GiveTrashBehaviour(this, template));
	}

	public void takeDown() {
		App.LOGGER.log(getLocalName() + ": done working.", true);
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
			}
		}
	}

	public Compartment getCompartment() {
		return this.compartment;
	}
	
	public void waitForTruck() {
		App.LOGGER.log(this.getLocalName() + " is full. Awaiting truck", true);
		this.isAwaitingTruck = true;
	}
	
	public void stopAwaitingTruck() {
		App.LOGGER.log(this.getLocalName() + " request fulfilled", true);
		this.isAwaitingTruck = false;
	}

	public Position getPos() {
		return pos;
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}
}
