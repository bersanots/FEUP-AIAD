package general;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import sajas.core.Agent;
import sajas.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public final class DFUtils {

	public static void register(Agent a, ServiceDescription sd) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(a.getAID());

		try {
			DFAgentDescription previous_entries[] = DFService.search(a, dfd);
			if (previous_entries.length > 0)
				DFService.deregister(a);

			dfd.addServices(sd);
			DFService.register(a, dfd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void registerMultipleServices(Agent a, List<ServiceDescription> sd) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(a.getAID());

		try {
			DFAgentDescription previous_entries[] = DFService.search(a, dfd);
			if (previous_entries.length > 0)
				DFService.deregister(a);

			for (ServiceDescription service : sd)
				dfd.addServices(service);
			DFService.register(a, dfd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<AID> getServiceWithProperty(Agent a, String service, Property property) {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(service);
		if (property != null)
			sd.addProperties(property); // add property
		dfd.addServices(sd);

		SearchConstraints constraints = new SearchConstraints();
		constraints.setMaxResults((long) -1);

		try {
			DFAgentDescription[] result = DFService.search(a, dfd, constraints);
			List<AID> agents = new ArrayList<>();

			for (int i = 0; i < result.length; i++)
				agents.add(result[i].getName());
			return agents;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<AID> getService(Agent a, String service) {
		return getServiceWithProperty(a, service, null);
	}

}