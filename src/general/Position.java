package general;

import jade.util.leap.Serializable;

public class Position implements Serializable{
	
	private int x;
	private int y;
	
	public Position(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public double getDistance(Position pos)
	{       
	    return Math.sqrt((pos.y - this.y) * (pos.y - this.y) + (pos.x - this.x) * (pos.x - this.x));
	}

	public Position getStep(Position destination, int step_size)
	{
	  int diff_x = this.diffX(destination);
	  int diff_y = this.diffY(destination);
	  int step_x = (diff_x != 0 ? Math.abs(diff_x) / diff_x : 0) * step_size;
	  int step_y = (diff_y != 0 ? Math.abs(diff_y) / diff_y : 0) * step_size;

	  if ( (step_x > 0 && this.x + step_x > destination.x) || (step_x < 0 && this.x + step_x < destination.x) )
	    step_x = destination.x - this.x;
	 
	  if ( (step_y > 0 && this.y + step_y > destination.y) || (step_y < 0 && this.y + step_y < destination.y) )
	    step_y = destination.y - this.y;
	    
	  Position step = new Position(step_x, step_y);
	  return step;
	}

	public Position getUnitaryStep(Position destination)
	{
	  return this.getStep(destination, 1);
	}
	
	public void sum(Position pos) {
		this.x += pos.x;
		this.y += pos.y;
	}

	private int diffY(Position pos)
	{
	  return pos.y - this.y;
	}

	private int diffX(Position pos)
	{
	  return pos.x - this.x;
	}

	@Override
	public boolean equals(Object o) 
	{
	  if (this == o)
	    return true;
	  if (o == null)
	    return false;
	  if (getClass() != o.getClass())
	    return false;
	  Position pos = (Position) o;
	  return this.x == pos.x && this.y == pos.y;
	}
	
	@Override
    public String toString() {
        return "Position [x=" + x + ", y=" + y
                + "]";
    }
}
