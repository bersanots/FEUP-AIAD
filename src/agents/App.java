package agents;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.*;

public class App{
	
	public static void main(String[] args) {
		Runtime rt = Runtime.instance();
		
		Profile p1 = new ProfileImpl();
		//p1.setParameter(...); // optional
		ContainerController mainContainer = rt.createMainContainer(p1);

		Profile p2 = new ProfileImpl();
		//p2.setParameter(...); // optional
		
		ContainerController container = rt.createAgentContainer(p2);
		
		AgentController ac1, ac2, ac3;
		try {
			Central central = new Central();
			Truck truck = new Truck("Simple", 100);
			Container c = new Container(TRASH_TYPE.REGULAR, 100);
			ac1 = container.acceptNewAgent("central", central);
			ac2 = container.acceptNewAgent("truck", truck);
			ac3 = container.acceptNewAgent("container", c);
			ac1.start();
			ac3.start();
			ac2.start();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
