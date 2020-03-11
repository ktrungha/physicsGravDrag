package experiments;
public class MM {

	public static void main(String[] args) {
		double earthDrag = MGP.computeDragRatio(MGP.Constants.earthMass, MGP.Constants.earthRadius);

		double combineSpeed = MGP.combineSpeed(MGP.Constants.c, MGP.Constants.earthOrbitalSpeed, earthDrag);
		double contractionRatio = MGP.getContractionRatio(combineSpeed, MGP.Constants.earthOrbitalSpeed);

		double horizontalTime = 20 * contractionRatio / (combineSpeed - MGP.Constants.earthOrbitalSpeed);

		double verticalTime = 20 / MGP.Constants.c;

		double timeDiff = verticalTime - horizontalTime;
		double fringeDiff = 2 * StrictMath.PI * MGP.Constants.c * timeDiff / MGP.Constants.lamda;

		System.out.println(fringeDiff);
	}

}
