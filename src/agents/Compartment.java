package agents;

import jade.core.Agent;

enum TRASH_TYPE {
	  BLUE,
	  GREEN,
	  YELLOW,
	  REGULAR,
	  ORGANIC,
	  ELETRONIC
	}

public class Compartment{
	
	private final int capacity;
	private int current_amount = 0;
	private TRASH_TYPE type;
	

	public Compartment(TRASH_TYPE type, int capacity) {
		
		this.capacity = capacity;
		this.type = type;
	}
	
	@Override
	public boolean equals(Object o) {
	  if (this == o) return true;                  
	  if (o == null) return false;               
	  if (getClass() != o.getClass()) return false; 
	  Compartment compartment = (Compartment) o;                       
	  return this.type == compartment.type;    
	}
	
	public TRASH_TYPE getType() {
		return type;
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
			removedAmount = this.current_amount - amount;
		else removedAmount = this.current_amount;
		
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
	
	
}
