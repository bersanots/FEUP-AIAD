package general;

public final class SpaceDimensions {
	
	private static int size = 50;
	
	public static void setUpSize(int containerNum) {
		
		int matrixUnitNumber = containerNum * 100;
		size = (int) Math.sqrt(matrixUnitNumber);
		
	}
	
	public static Position getCenterPos() {
		return new Position(size/2, size/2);
	}
	
	public static int getSize() {
		return size;
	}
	
	public static void setSize(int size) {
		SpaceDimensions.size = size;
	}
}
