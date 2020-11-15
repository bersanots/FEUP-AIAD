package general;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import agents.Central;

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
			addTruckType(TrashType.REGULAR, 100, true);
			addSpecialTruckType("Urgent", 100, true);
			addContainer(TrashType.REGULAR, 50, -5, 10);
			addContainer(TrashType.ORGANIC, 50, 5, 10);
			
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

	private static void addTruckType(TrashType type, int capacity, boolean allowsIntermediate) {
		addTruck(new Truck(type, capacity, allowsIntermediate));
	}

	private static void addSpecialTruckType(String type, int capacity, boolean allowsIntermediate) {
		addTruck(new Truck(type, capacity, allowsIntermediate));
	}

	private static void addTruck(Truck t) {
		trucks.add(t);
	}

	private static void addContainer(TrashType t_type, int capacity, int x, int y) {
		Container c = new Container(t_type, capacity, new Position(x, y));
		containers.add(c);
	}
}
