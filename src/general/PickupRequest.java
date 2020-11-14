package general;

import jade.core.AID;

public class PickupRequest {
	
	private Position pos;
	private AID containerAID;
	
	public PickupRequest(Position pos, AID id){
		this.setPos(pos);
		this.setContainerAID(id);
	}

	public Position getPos() {
		return pos;
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}

	public AID getContainerAID() {
		return containerAID;
	}

	public void setContainerAID(AID containerAID) {
		this.containerAID = containerAID;
	}
	
}
