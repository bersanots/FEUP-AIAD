package agents;

import java.awt.Color;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import behaviours.GetIntermediatePickupContractBehaviour;
import behaviours.GiveTrashBehaviour;
import behaviours.RequestPickupBehaviour;
import general.App;
import general.ColorAssigner;
import general.Compartment;
import general.DFUtils;
import general.Position;
import general.TrashType;
import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;
import sajas.domain.DFService;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Container extends Agent implements Drawable{

	private Compartment compartment;
	private Position pos;
	private boolean isAwaitingTruck = false;
	private final int rate = 500;
	// logging
	private Date request_start_time = null;
	private long average_time = 0;
	private int n_collections = 0;
	private long total_full_time = 0;
	private long average_full_time = 0;
	private int n_full_collections = 0;
	
	//drawable
	Color color;

	public Container(TrashType type, int capacity, Position pos) {
		this.compartment = new Compartment(type, capacity);
		this.pos = pos;
		this.color = ColorAssigner.assignColor(type);
	}

	public void setup() {
		setAvailable();
		addBehaviour(new TrashGenerationBehaviour(this, rate));
		App.LOGGER.log("A new Container was created!", true);
		App.LOGGER.createLogFile(this.getLocalName());
		App.LOGGER.log(this.getLocalName(), this.getLocalName() + " TYPE: " + this.compartment.getType().name() + " - CAPACITY: " + this.compartment.getCapacity());
		// add behaviours
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		addBehaviour(new GiveTrashBehaviour(this, template));
		addBehaviour(new GetIntermediatePickupContractBehaviour(this));
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
		setOccupied();
		App.LOGGER.log(this.getLocalName(), "0 - WAITING");
		App.LOGGER.log(this.getLocalName() + " awaiting truck", true);
		this.isAwaitingTruck = true;
		this.setLoggingVars();
	}

	public void stopAwaitingTruck() {
		boolean isFull = this.getCompartment().isFull();
		
		setAvailable();
		App.LOGGER.log(this.getLocalName(), "1 - STOPPED WAITING");

		long previous_time_sum = this.average_time * n_collections;
		n_collections++;

		Date curr_time = new Date(System.currentTimeMillis());
		
		
		long time_waited = -1;
		
		if (this.request_start_time == null)
			time_waited = this.average_time;
		else 
			time_waited = App.LOGGER.getTimeDifference(this.request_start_time, curr_time, TimeUnit.SECONDS);
		
		App.LOGGER.log(this.getLocalName(), "2 - TIME WAITED: " + time_waited + " SECONDS");

		this.average_time = (previous_time_sum + time_waited) / n_collections;
		App.LOGGER.log(this.getLocalName(), "3 - AVERAGE WAITING TIME: " + this.average_time + " SECONDS");

		App.LOGGER.log(this.getLocalName() + " request fulfilled", true);
		
		
		if (isFull)
		{
			//long previous_full_time_sum = this.average_full_time * this.n_full_collections;
			//this.n_full_collections++;
			
			//this.average_full_time = (previous_full_time_sum + time_waited) / this.n_full_collections;
			this.total_full_time += time_waited;
		}
		
		
		this.isAwaitingTruck = false;
		this.clearLoggingVars();
	}

	public Position getPos() {
		return pos;
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}

	public void setAvailable() {
		ServiceDescription sd = new ServiceDescription();
		sd.setType("container" + compartment.getType().name());
		sd.setName(getLocalName());
		DFUtils.register(this, sd);
	}

	public void setOccupied() {
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setLoggingVars() {
		this.request_start_time = new Date(System.currentTimeMillis());		
	}

	private void clearLoggingVars() {
		this.request_start_time = null;
	}
	
	public long getAverageWaitTime()
	{
		return this.average_time;
	}
	
	public long getTotalFullTime()
	{
		return this.total_full_time;
	}

	@Override
	public void draw(SimGraphics g) {
		g.drawFastCircle(color);
	}

	@Override
	public int getX() {
		return this.pos.getX();
	}

	@Override
	public int getY() {
		return this.pos.getY();
	}
}
