package agents;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import behaviours.OrderPickupBehaviour;
import behaviours.SetPickupContractBehaviour;
import general.App;
import general.DFUtils;
import general.PickupRequestInfo;
import general.Position;
import general.TrashType;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

public class Central extends Agent {
	
	private Position pos = new Position(0, 0);
	private ConcurrentLinkedQueue<PickupRequestInfo> requestQueue;

	public Central() {
		this.requestQueue = new ConcurrentLinkedQueue<>();
	}

	public void setup() {
		App.LOGGER.log("A new Central was created!", true);
		// add behaviours
		addBehaviour(new OrderPickupBehaviour(this));

		DFAgentDescription template = new DFAgentDescription();
		// ServiceDescription sd = new ServiceDescription();
		// template.addServices(sd);

		addBehaviour(new SubscriptionInitiator(this,
				DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
					
					Central central = (Central) myAgent;
					
					central.requestPendingPickups();
					
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		});
	}
	
	public void requestPendingPickups() {
		int queueStartingSize = requestQueue.size();
		// App.LOGGER.log("Pending Requests: " + queueStartingSize, true);
		for (int i = 0; i < queueStartingSize; i++) {

			PickupRequestInfo reqInfo = popRequest();
			requestPickup(reqInfo);
		}
	}
	
	synchronized public void requestPickup(PickupRequestInfo reqInfo) {
		TrashType t_type = reqInfo.getTrashType();
		List<AID> truckIds = DFUtils.getService(this, "truck" + t_type.name());
		for (AID truckId : truckIds) {
			App.LOGGER.log("Available for pickup: " + truckId.getLocalName(), true);
		}
		if (truckIds.size() != 0) {
			this.addBehaviour(new SetPickupContractBehaviour(this, reqInfo, truckIds));
		}
		else {
			insertRequest(reqInfo);
		}
	}

	synchronized public void insertRequest(PickupRequestInfo req) {		
		this.requestQueue.add(req);
		App.LOGGER.log("Request added. Pending: " + requestQueue.size(), true);
	}

	synchronized public PickupRequestInfo peekRequest() {
		return requestQueue.peek();
	}

	synchronized public PickupRequestInfo popRequest() {
		return requestQueue.poll();
	}
}
