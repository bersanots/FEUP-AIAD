package general;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import agents.Central;
import agents.Compartment;
import agents.Container;
import agents.Truck;
import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.*;

public class App {

	public static Logger LOGGER = new Logger();

	private static List<Truck> trucks = new ArrayList<>();
	private static List<Container> containers = new ArrayList<>();
	private static Central central = new Central();

	public static void main(String[] args) {
		Runtime rt = Runtime.instance();

		Profile p1 = new ProfileImpl();
		// p1.setParameter(...); // optional
		ContainerController mainContainer = rt.createMainContainer(p1);

		Profile p2 = new ProfileImpl();
		// p2.setParameter(...); // optional

		ContainerController container = rt.createAgentContainer(p2);

		try {
			central = new Central();
			parseArgs(args);

			List<AgentController> acs = buildAgentControllerList(container);
			

			startAgents(acs);

		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
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
		
		List <String> specialTypes = new ArrayList<>();
		List <TrashType> regularTypes = new ArrayList<>();
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
		case "SIMPLE":
		default:// regular (1 compartment)		
			System.out.println("Simple");
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
		
		for(TrashType t_type : TrashType.values())	
			buildTypeContainers(num, capacity, t_type);
		
	}

	private static void buildTypeContainers(int num, int capacity, TrashType t_type) {
		
		int max = 30;
		int min = -30;
		Random random = new Random();
		
		for (int i = 0; i < num; i++) {
			
			int x = random.nextInt(max - min) + min;
			int y = random.nextInt(max - min) + min;
			
			if (x == 0)
				x = 1;
			if (y == 0)
				y = 1;
			
			App.addContainer(t_type, capacity, x, y);
		}
	}
	
	private static void parseArgs(String args[]) {
		
		if (args.length != 5)
			System.exit(-1);
		
		String truckCombination = args[0];
		int truckNum = Integer.parseInt(args[1]);
		int truckCapacity = Integer.parseInt(args[2]);
		int containerNum = Integer.parseInt(args[3]);
		int containerCapacity = Integer.parseInt(args[4]);

		App.buildContainers(containerNum, containerCapacity);			
		App.buildTrucks(truckCombination, truckCapacity, truckNum, true);
	}
}
