package agents;

import java.util.Random;

import jade.core.Agent;

enum TRASH_TYPE {
	BLUE, GREEN, YELLOW, REGULAR, ORGANIC, ELETRONIC
}

public class Compartment {

	private final int capacity;
	private int current_amount = 0;
	private TRASH_TYPE type;

	public Compartment(TRASH_TYPE type, int capacity) {

		this.capacity = capacity;
		this.type = type;
	}
	
	public boolean isEmpty() {
		return this.capacity == 0;
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

	public TRASH_TYPE getType() {
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
		System.out.println("TRASH PROB: " + prob);
		System.out.println("GOT THIS PROB: " + result);
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
			prob = 9;
			break;
		case ELETRONIC:
			prob = 1;
			break;
		case GREEN:
			prob = 7;
			break;
		case ORGANIC:
			prob = 2;
			break;
		case REGULAR:
			prob = 36;
			break;
		case YELLOW:
			prob = 12;
			break;
		}
		return prob;
	}

	public void generateTrash() {
		
		int max, min;
		int probability = getTypeProbability();
		Random random = new Random();
		
		if (isTrashGenerated(probability))
		{
			int randomTrashAmount = getRandomTrashAmount(1,10);
			System.out.println("ADDED THIS AMOUNTI: " + randomTrashAmount);
			this.addContents(randomTrashAmount);
		}
		System.out.println("Current amount: " + this.current_amount);
	} 

}
