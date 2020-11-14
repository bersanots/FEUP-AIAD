package general;

import jade.core.AID;

public class PickupRequest {
	
	private Position pos;
	private AID containerAID;
	
	public PickupRequest(Position pos, AID id){
		this.pos = pos;
		this.containerAID = id;
	}
	
}
