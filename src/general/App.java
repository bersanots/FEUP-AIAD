package general;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import agents.Central;
import agents.Container;
import agents.Truck;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Agent;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import sajas.wrapper.AgentController;
import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Multi2DGrid;

public class App extends Repast3Launcher {

	public static Logger LOGGER = new Logger();

	private static List<Truck> trucks = new ArrayList<>();
	private static List<Container> containers = new ArrayList<>();
	private static Central central;
	
	private List<Agent> agentList = new ArrayList<>();

	private static String args[];
	
	//graphics
	private Multi2DGrid space;
	private DisplaySurface dsurf;
	
	//statistics
	private OpenSequenceGraph avgContainerWaitGraph;
	private OpenSequenceGraph avgContainerOccupationGraph;
	private OpenSequenceGraph avgTruckTripTimeGraph;
	private OpenSequenceGraph containerFullTimeGraph;
	private OpenSequenceGraph avgTruckTripDistanceGraph;
	
	
	//schedule
	Schedule schedule;


	public App(String argums[]) {
		args = argums;
	}

	@Override
	public String[] getInitParam() {
		return new String[0];
	}

	@Override
	public String getName() {
		return "SmartTrashPickup";
	}

	@Override
	protected void launchJADE() {

		Runtime rt = Runtime.instance();

		Profile p1 = new ProfileImpl();
		ContainerController mainContainer = rt.createMainContainer(p1);

		Profile p2 = new ProfileImpl();
		ContainerController container = rt.createAgentContainer(p2);

		try {
			parseArgs();
			central = new Central();

			List<AgentController> acs = buildAgentControllerList(container);

			startAgents(acs);

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	private static void startAgents(List<AgentController> acs) throws StaleProxyException {
		for (AgentController ac : acs)
			ac.start();
	}

	private static List<AgentController> buildAgentControllerList(ContainerController container)
			throws StaleProxyException {

		List<AgentController> acs = new ArrayList<>();
		acs.add(container.acceptNewAgent("central", central));
		addTrucksToControllerList(acs, container);
		addContainersToControllerList(acs, container);
		return acs;
	}

	private static void addTrucksToControllerList(List<AgentController> acs, ContainerController container)
			throws StaleProxyException {

		for (int i = 0; i < trucks.size(); i++) {
			Truck truck = trucks.get(i);
			acs.add(container.acceptNewAgent("truck" + Integer.toString(i + 1), truck));
		}
	}

	private static void addContainersToControllerList(List<AgentController> acs, ContainerController container)
			throws StaleProxyException {

		for (int i = 0; i < containers.size(); i++) {
			Container containerAgent = containers.get(i);
			acs.add(container.acceptNewAgent("container" + Integer.toString(i + 1), containerAgent));
		}

	}

	private static void addTruckType(TrashType type, int capacity, boolean allowsIntermediate, int num) {
		for (int i = 0; i < num; i++) {
			addTruck(new Truck(type, capacity, allowsIntermediate));
		}
	}

	private static void addSpecialTruckType(String type, int capacity, boolean allowsIntermediate, int num) {
		for (int i = 0; i < num; i++) {
			addTruck(new Truck(type, capacity, allowsIntermediate));
		}
	}

	private static void addTruck(Truck t) {
		trucks.add(t);
	}

	private static void addContainer(TrashType t_type, int capacity, int x, int y) {
		Container c = new Container(t_type, capacity, new Position(x, y));
		containers.add(c);
	}

	private static void buildTrucks(String truckCombination, int capacity, int num, boolean allowsIntermediate) {

		List<String> specialTypes = new ArrayList<>();
		List<TrashType> regularTypes = new ArrayList<>();
		switch (truckCombination.toUpperCase()) {
			case "ALLCOMP":
				System.out.println("AllComp");
				specialTypes.add("Recycling");
				specialTypes.add("Urgent");
				specialTypes.add("EletroGreen");
				break;
			case "RECYCLING":
				System.out.println("Recycling");
				specialTypes.add("Recycling");
				regularTypes.add(TrashType.ELETRONIC);
				regularTypes.add(TrashType.ORGANIC);
				regularTypes.add(TrashType.REGULAR);
				break;
			case "URGENT":
				System.out.println("Urgent");
				specialTypes.add("Urgent");
				regularTypes.add(TrashType.BLUE);
				regularTypes.add(TrashType.GREEN);
				regularTypes.add(TrashType.YELLOW);
				regularTypes.add(TrashType.ELETRONIC);
				break;
			case "ELETROGREEN":
				System.out.println("EletroGreen");
				specialTypes.add("EletroGreen");
				regularTypes.add(TrashType.BLUE);
				regularTypes.add(TrashType.YELLOW);
				regularTypes.add(TrashType.ORGANIC);
				regularTypes.add(TrashType.REGULAR);
				break;
			case "ALLSIMPLE":
			default:// regular (1 compartment)
				System.out.println("AllSimple");
				for (TrashType type : TrashType.values())
					regularTypes.add(type);
				break;
		}

		for (String specialT : specialTypes)
			addSpecialTruckType(specialT, capacity, allowsIntermediate, num);
		for (TrashType regularT : regularTypes)
			addTruckType(regularT, capacity, allowsIntermediate, num);
	}

	private static void buildContainers(int num, int capacity) {
		for (TrashType t_type : TrashType.values())
			buildTypeContainers(num, capacity, t_type);
	}

	private static void buildTypeContainers(int num, int capacity, TrashType t_type) {

		int max = SpaceDimensions.getSize();
		int min = 0;
		Random random = new Random();

		for (int i = 0; i < num; i++) {

			int x = random.nextInt(max - min) + min;
			int y = random.nextInt(max - min) + min;

			while (x == SpaceDimensions.getCenterPos().getX())
				x = random.nextInt(max - min) + min;
			if (y == SpaceDimensions.getCenterPos().getY())
				y = random.nextInt(max - min) + min;

			App.addContainer(t_type, capacity, x, y);
		}
	}

	private static void parseArgs() {
		
		if (args.length != 6 && args.length != 2) {
			System.out.println(
					"Usage: app $truckComb(truckCombination) $truckNum(int) $truckCapacity(int) $containerNum(int) $containerCapacity(int) $allowIntermediatePickups(0/1)");
			System.out.println(
					"truckCombination = \"AllSimple\" or \"Recycling\" or \"Urgent\" or \"EletroGreen\" or \"AllComp\"");
			System.out.println("OR");
			System.out.println("Usage: app \"debug\" $allowIntermediatePickups(0/1)");
			System.exit(-1);
		}

		if (args.length == 6) {
			String truckCombination = args[0];
			int truckNum = Integer.parseInt(args[1]);
			int truckCapacity = Integer.parseInt(args[2]);
			int containerNum = Integer.parseInt(args[3]);
			SpaceDimensions.setSize(100);
			//SpaceDimensions.setUpSize(containerNum);
			int containerCapacity = Integer.parseInt(args[4]);
			int allowInterPickupsInt = Integer.parseInt(args[5]);
			boolean allowInterPickups = allowInterPickupsInt != 0 ? true : false;

			App.buildContainers(containerNum, containerCapacity);
			App.buildTrucks(truckCombination, truckCapacity, truckNum, allowInterPickups);
		} else {
			String debug = args[0];
			if (!debug.toUpperCase().equals("DEBUG")) {
				System.out.println("Usage: app \"debug\" $allowIntermediatePickups(0/1)");
				System.exit(-1);
			}

			int allowInterPickupsInt = Integer.parseInt(args[1]);
			boolean allowInterPickups = allowInterPickupsInt != 0 ? true : false;

			addTruckType(TrashType.REGULAR, 100, allowInterPickups, 1);
			addSpecialTruckType("Urgent", 100, allowInterPickups, 1);
			addContainer(TrashType.REGULAR, 50, -5, 10);
			addContainer(TrashType.ORGANIC, 50, 5, 10);

		}

	}

	@Override
	public void setup() {
		super.setup();
		// property descriptors
		// prepare simulation
		// create Schedule
		// create DisplaySurface
		dsurf = new DisplaySurface(this,
				this.getName());
		registerDisplaySurface(this.getName(), dsurf);
	}

	@Override
	public void begin() {
		super.begin();
		buildModel();
		buildDisplay();
		buildSchedule();
		// display surfaces, spaces, displays, plots, ...
		// ...
	}
	
	private void buildModel() {
		// create and store agents
		setupAgentList();
		// create space, data recorders
		this.space =  new Multi2DGrid(SpaceDimensions.getSize(), SpaceDimensions.getSize(), false);
		
		this.buildContainerAvgWaitTimeGraph();
		this.buildTruckAvgTripTimeGraph();
		this.buildContainerAvgOccupationGraph();
		this.buildContainerFullTimeGraph();
		this.buildTruckAvgDistanceGraph();
		
		drawAgents();
	}
	
	private void buildContainerFullTimeGraph()
	{
		this.containerFullTimeGraph = new OpenSequenceGraph("Average Container Full Time", this);
		this.containerFullTimeGraph.setAxisTitles("time", "time full");
		
		class AvgWaitTimeSeq implements Sequence {
			private TrashType type;
			public AvgWaitTimeSeq(TrashType type)
			{
				this.type = type;
			}
			
			public double getSValue() {
				long full_time_sum = 0;
				int n = 0;
				for(Container cont : App.containers)
				{
					TrashType contType = cont.getCompartment().getType();
					if (type == contType || type == null)
					{
						n++;
						full_time_sum += cont.getTotalFullTime();
					}
				}
				
				if (n==0)
					return 0;
				else return full_time_sum / n;
			}
		
		}
		
		this.containerFullTimeGraph.addSequence("Regular trash", new AvgWaitTimeSeq(TrashType.REGULAR));
		this.containerFullTimeGraph.addSequence("Blue trash", new AvgWaitTimeSeq(TrashType.BLUE));
		this.containerFullTimeGraph.addSequence("Green trash", new AvgWaitTimeSeq(TrashType.GREEN));
		this.containerFullTimeGraph.addSequence("Yellow trash", new AvgWaitTimeSeq(TrashType.YELLOW));
		this.containerFullTimeGraph.addSequence("Organic trash", new AvgWaitTimeSeq(TrashType.ORGANIC));
		this.containerFullTimeGraph.addSequence("Eletronic trash", new AvgWaitTimeSeq(TrashType.ELETRONIC));
		this.containerFullTimeGraph.addSequence("All trash", new AvgWaitTimeSeq(null));
		
		this.containerFullTimeGraph.setYRange(0, 20);
		this.containerFullTimeGraph.display();
	}
	

	private void buildContainerAvgWaitTimeGraph() {
		this.avgContainerWaitGraph = new OpenSequenceGraph("Average Container Waiting Time", this);
		this.avgContainerWaitGraph.setAxisTitles("time", "wait time");
		
		class AvgWaitTimeSeq implements Sequence {
			private TrashType type;
			public AvgWaitTimeSeq(TrashType type)
			{
				this.type = type;
			}
			
			public double getSValue() {
				long avg_wait_time_sum = 0;
				int n = 0;
				for(Container cont : App.containers)
				{
					TrashType contType = cont.getCompartment().getType();
					if (type == contType || type == null)
					{
						n++;
						avg_wait_time_sum += cont.getAverageWaitTime();
					}
				}
				
				if (n==0)
					return 0;
				else return avg_wait_time_sum / n;
			}
		
		}
		
		this.avgContainerWaitGraph.addSequence("Regular trash", new AvgWaitTimeSeq(TrashType.REGULAR));
		this.avgContainerWaitGraph.addSequence("Blue trash", new AvgWaitTimeSeq(TrashType.BLUE));
		this.avgContainerWaitGraph.addSequence("Green trash", new AvgWaitTimeSeq(TrashType.GREEN));
		this.avgContainerWaitGraph.addSequence("Yellow trash", new AvgWaitTimeSeq(TrashType.YELLOW));
		this.avgContainerWaitGraph.addSequence("Organic trash", new AvgWaitTimeSeq(TrashType.ORGANIC));
		this.avgContainerWaitGraph.addSequence("Eletronic trash", new AvgWaitTimeSeq(TrashType.ELETRONIC));
		this.avgContainerWaitGraph.addSequence("All trash", new AvgWaitTimeSeq(null));
		
		
		this.avgContainerWaitGraph.display();
	}
	
	private void buildContainerAvgOccupationGraph() {
		this.avgContainerOccupationGraph = new OpenSequenceGraph("Average Container Occupation", this);
		this.avgContainerOccupationGraph.setAxisTitles("time", "Occupation (%)");
		
		class AvgWaitTimeSeq implements Sequence {
			private TrashType type;
			public AvgWaitTimeSeq(TrashType type)
			{
				this.type = type;
			}
			
			public double getSValue() {
				int avg_occupation_sum = 0;
				int n = 0;
				for(Container cont : App.containers)
				{
					TrashType contType = cont.getCompartment().getType();
					if (type == contType || type == null)
					{
						n++;
						Compartment compa = cont.getCompartment();
						double occupied_percent =  compa.getCurrentAmount() / (double) compa.getCapacity() * 100;
						avg_occupation_sum += occupied_percent;
					}
				}
				
				if (n==0)
					return 0;
				else return avg_occupation_sum / n;
			}
		
		}
		
		this.avgContainerOccupationGraph.addSequence("Regular trash", new AvgWaitTimeSeq(TrashType.REGULAR));
		this.avgContainerOccupationGraph.addSequence("Blue trash", new AvgWaitTimeSeq(TrashType.BLUE));
		this.avgContainerOccupationGraph.addSequence("Green trash", new AvgWaitTimeSeq(TrashType.GREEN));
		this.avgContainerOccupationGraph.addSequence("Yellow trash", new AvgWaitTimeSeq(TrashType.YELLOW));
		this.avgContainerOccupationGraph.addSequence("Organic trash", new AvgWaitTimeSeq(TrashType.ORGANIC));
		this.avgContainerOccupationGraph.addSequence("Eletronic trash", new AvgWaitTimeSeq(TrashType.ELETRONIC));
		this.avgContainerOccupationGraph.addSequence("All trash", new AvgWaitTimeSeq(null));
		
		this.avgContainerOccupationGraph.setYRange(0, 100);
		this.avgContainerOccupationGraph.display();
	}
	
	private void buildTruckAvgTripTimeGraph() {
		this.avgTruckTripTimeGraph = new OpenSequenceGraph("Average Truck Trip Time", this);
		this.avgTruckTripTimeGraph.setAxisTitles("time", "trip time");
		
		class AvgWaitTimeSeq implements Sequence {
			private TrashType type;
			public AvgWaitTimeSeq(TrashType type)
			{
				this.type = type;
			}
			
			public double getSValue() {
				long avg_trip_time_sum = 0;
				int n = 0;
				for(Truck truck : App.trucks)
				{
					if (true)
					{
						n++;
						avg_trip_time_sum += truck.getAverageTripTime();
					}
				}
				
				return avg_trip_time_sum / n;
			}
		
		}
		
		this.avgTruckTripTimeGraph.addSequence("All Trucks", new AvgWaitTimeSeq(null));
		
		
		this.avgTruckTripTimeGraph.display();
	}
	
	private void buildTruckAvgDistanceGraph() {
		this.avgTruckTripDistanceGraph = new OpenSequenceGraph("Average Truck Distance", this);
		this.avgTruckTripDistanceGraph.setAxisTitles("time", "distance");
		
		class AvgWaitTimeSeq implements Sequence {
			private TrashType type;
			public AvgWaitTimeSeq(TrashType type)
			{
				this.type = type;
			}
			
			public double getSValue() {
				long avg_distance_sum = 0;
				int n = 0;
				for(Truck truck : App.trucks)
				{
					if (true)
					{
						n++;
						avg_distance_sum += truck.getAverageTripDistance();
					}
				}
				
				return avg_distance_sum / n;
			}
		
		}
		
		this.avgTruckTripDistanceGraph.addSequence("All Trucks", new AvgWaitTimeSeq(null));
		
		
		this.avgTruckTripDistanceGraph.display();
	}
	

	
	private void buildDisplay() {
		// create displays, charts
		Object2DDisplay agentDisplay = new Object2DDisplay(space);
		agentDisplay.setObjectList(agentList);
				
		dsurf.addDisplayableProbeable(agentDisplay, "Agents");
		addSimEventListener(dsurf);
		dsurf.display();
		dsurf.setSnapshotFileName("");
		
		this.avgContainerOccupationGraph.setSnapshotFileName("AVGOcc");
		this.avgContainerWaitGraph.setSnapshotFileName("AVGWait");
		this.avgTruckTripTimeGraph.setSnapshotFileName("AVGTripT");
		this.containerFullTimeGraph.setSnapshotFileName("AVGFullT");
		this.avgTruckTripDistanceGraph.setSnapshotFileName("AVGTripD");
	}
	
	private void buildSchedule() {
		// build the schedule
		int scheduleTime = 10000;
		this.schedule = this.getSchedule();
		schedule.scheduleActionAtInterval(scheduleTime * 3, dsurf, "updateDisplay", Schedule.LAST);
		schedule.scheduleActionAtInterval(scheduleTime * 3, this.avgContainerWaitGraph, "step", Schedule.LAST);
		schedule.scheduleActionAtInterval(scheduleTime * 3, this.avgTruckTripTimeGraph, "step", Schedule.LAST);
		schedule.scheduleActionAtInterval(scheduleTime * 3, this.avgContainerOccupationGraph, "step", Schedule.LAST);
		schedule.scheduleActionAtInterval(scheduleTime * 3, this.containerFullTimeGraph, "step", Schedule.LAST);
		schedule.scheduleActionAtInterval(scheduleTime * 3, this.avgTruckTripDistanceGraph, "step", Schedule.LAST);
		
		schedule.scheduleActionAtInterval(scheduleTime * 100, this.avgContainerWaitGraph, "takeSnapshot", Schedule.LAST);
		schedule.scheduleActionAtInterval(scheduleTime * 100, this.avgTruckTripTimeGraph, "takeSnapshot", Schedule.LAST);
		schedule.scheduleActionAtInterval(scheduleTime * 100, this.avgContainerOccupationGraph, "takeSnapshot", Schedule.LAST);
		schedule.scheduleActionAtInterval(scheduleTime * 100, this.containerFullTimeGraph, "takeSnapshot", Schedule.LAST);
		schedule.scheduleActionAtInterval(scheduleTime * 100, this.avgTruckTripDistanceGraph, "takeSnapshot", Schedule.LAST);
		
		
		class GenerateTrashAction extends BasicAction{

			@Override
			public void execute() {
				for(Container container : containers)
					container.generateTrashStep();
			}
			
		}
		
		class MoveTruckAction extends BasicAction{

			@Override
			public void execute() {
				for(Truck truck : trucks)
					truck.moveStep();
			}
			
		}
		
		schedule.scheduleActionAtInterval(scheduleTime * 3, new GenerateTrashAction(), Schedule.RANDOM);
		schedule.scheduleActionAtInterval(scheduleTime, new MoveTruckAction(), Schedule.RANDOM);
		
	}

	/**
	 * Launching Repast3
	 * 
	 * @param args
	 */
	public static void main(String[] argums) {
		boolean BATCH_MODE = true;
		SimInit init = new SimInit();
		init.setNumRuns(1); // works only in batch mode
		
		App model = new App(argums);
		
		init.loadModel(model, null, BATCH_MODE);
	}
	
	private void setupAgentList() {
		for (Container container: App.containers)
			agentList.add(container);
		for (Truck truck : App.trucks)
			agentList.add(truck);
		agentList.add(central);
	}
	
	private void drawAgents() {
		for (Container container: App.containers)
			space.putObjectAt(container.getPos().getX(), container.getPos().getY(), container);
		for (Truck truck : App.trucks)
			space.putObjectAt(truck.getPos().getX(), truck.getPos().getY(), truck);
		space.putObjectAt(central.getPos().getX(), central.getPos().getY(), central);
	}

}
