package general;

import java.util.Random;

public class Compartment {

	private final int capacity;
	private int current_amount = 0;
	private TrashType type;

	public Compartment(TrashType type, int capacity) {
		this.capacity = capacity;
		this.type = type;
	}

	public boolean isEmpty() {
		return this.capacity == 0;
	}

	public boolean isFull() {
		return this.current_amount == this.capacity;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		Compartment compartment = (Compartment) o;
		return this.type == compartment.type;
	}

	public TrashType getType() {
		return type;
	}

	public int getCurrentAmount() {
		return this.current_amount;
	}

	public int getCapacity() {
		return capacity;
	}

	public boolean hasCapacity(int amount) {
		return amount + this.current_amount <= this.capacity;
	}

	public int remainingCapacity() {
		return this.capacity - this.current_amount;
	}

	public int removeContents(int amount) {

		int removedAmount;
		if (amount <= this.current_amount)
			removedAmount = amount;
		else
			removedAmount = this.current_amount;

		this.current_amount -= removedAmount;

		return removedAmount;
	}

	public int emptyCompartment() {
		return removeContents(this.current_amount);
	}

	public int addContents(int amount) {

		if (!hasCapacity(amount))
			amount = remainingCapacity();
		this.current_amount += amount;

		return amount;
	}

	private boolean isTrashGenerated(int prob) {
		Random random = new Random();
		int max = 100, min = 0;
		int result = random.nextInt(max - min) + min;
		// App.LOGGER.log("TRASH PROB: " + prob, true);
		// App.LOGGER.log("GOT THIS PROB: " + result, true);
		return result <= prob;
	}

	private int getRandomTrashAmount(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min) + min;
	}

	private int getTypeProbability() {
		int prob = 0;
		switch (type) {
			case BLUE:
				prob = 19;
				break;
			case ELETRONIC:
				prob = 11;
				break;
			case GREEN:
				prob = 17;
				break;
			case ORGANIC:
				prob = 12;
				break;
			case REGULAR:
				prob = 46;
				break;
			case YELLOW:
				prob = 22;
				break;
		}
		return prob;
	}

	public void generateTrash() {

		int max = 10, min = 1;
		int probability = getTypeProbability();
		Random random = new Random();

		if (isTrashGenerated(probability)) {
			int randomTrashAmount = getRandomTrashAmount(min, max);
			// App.LOGGER.log("ADDED THIS AMOUNTI: " + randomTrashAmount, true);
			this.addContents(randomTrashAmount);
		}
		// App.LOGGER.log("Current amount: " + this.current_amount, true);
	}

}
