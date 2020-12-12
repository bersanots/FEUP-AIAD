package general;

import java.awt.Color;

public final class ColorAssigner {
	
	public static Color assignColor(TrashType type) {
		Color color;
		switch (type) {
		case BLUE:
			color = new Color(7, 55, 230);
			break;
		case ELETRONIC:
			color = new Color(203, 13, 217);
			break;
		case GREEN:
			color = new Color(9, 219, 40);
			break;
		case ORGANIC:
			color = new Color(191, 129, 84);
			break;
		case REGULAR:
			color = new Color(255, 255, 255);
			break;
		case YELLOW:
			color = new Color(218, 237, 2);
			break;
		default:
			color = new Color(117, 117, 117);
			break;
	}
		
		return color;
	}
	
	public static Color centralColor() {
		
		return new Color(2, 216, 227);
	}
}
