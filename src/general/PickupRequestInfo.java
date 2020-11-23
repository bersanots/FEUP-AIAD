package general;

import jade.util.leap.Serializable;

public class PickupRequestInfo implements Serializable {

	private PickupRequest pickupRequest;
	private int amount;
	private TrashType trashType;

	public PickupRequestInfo(PickupRequest pickupRequest, int amount, TrashType trashType) {
		this.pickupRequest = pickupRequest;
		this.amount = amount;
		this.trashType = trashType;
	}

	public PickupRequest getPickupRequest() {
		return pickupRequest;
	}

	public int getAmount() {
		return amount;
	}

	public TrashType getTrashType() {
		return trashType;
	}

	@Override
	public String toString() {
		return "Position [pickupRequest=" + pickupRequest.toString() + ", amount=" + amount + ",trashType=" + trashType
				+ "]";
	}

}
