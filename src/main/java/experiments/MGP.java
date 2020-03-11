package experiments;
public class MGP {

	public static void main(String[] args) {
		double targetLattitude = 0.72902403d;

		int iterCount = 4;
		double originalAngularVelocityChunk = Constants.earthAngularVelocity / 4;
		for (int i = 0; i < iterCount; i++) {
			Constants.earthAngularVelocity = originalAngularVelocityChunk * (i + 1);

			Result result1F = totalSpeed(targetLattitude, false);
			Result result1T = totalSpeed(targetLattitude + Constants.lattitudeDiff, true);
			Result result2F = totalSpeed(targetLattitude + Constants.lattitudeDiff, false);
			Result result2T = totalSpeed(targetLattitude, true);

			double southernContractionRatio = getContractionRatio(result1F.speed, Constants.earthOrbitalSpeed);
			double northernContractionRatio = getContractionRatio(result2F.speed, Constants.earthOrbitalSpeed);

			double time1 = southernContractionRatio * Constants.eastWestDiff / result1F.speed
					+ northernContractionRatio * Constants.eastWestDiff / result1T.speed;
			double time2 = northernContractionRatio * Constants.eastWestDiff / result2F.speed
					+ southernContractionRatio * Constants.eastWestDiff / result2T.speed;

			double timeDiff = StrictMath.abs(time1 - time2);
			double fringeDiff = 2 * StrictMath.PI * MGP.Constants.c * timeDiff / MGP.Constants.lamda;

			System.out.println(fringeDiff);
		}
	}

	static double density(double r, double yRatio) {
		// data from https://www.cfa.harvard.edu/~lzeng/papers/PREM.pdf

		double raw = 0;
		// double sumMass = 0;
		if (r > 6350000) {
			raw = 1;
			// sumMass += 1 * 1000 * (volume(MGP.Constants.earthRadius) - volume(6350000));
		} else if (r > 6346000) {
			raw = 2.9;
			// sumMass += 2.9 * 1000 * (volume(6350000) - volume(6346000));
		} else if (r > 6151000) {
			raw = 2.69;
			// sumMass += 2.69 * 1000 * (volume(6346000) - volume(6151000));
		} else if (r > 5701000) {
			raw = 5.32;
			// sumMass += 5.32 * 1000 * (volume(6151000) - volume(5701000));
		} else if (r > 3480000) {
			raw = 7.96;
			// sumMass += 7.96 * 1000 * (volume(5701000) - volume(3480000));
		} else if (r > 1121000) {
			raw = 12.58;
			// sumMass += 12.58 * 1000 * (volume(3480000) - volume(1121000));
		} else {
			raw = 13.09;
			// sumMass += 13.09 * 1000 * volume(1121000);
		}

		return raw * 1000 / 1.385;
	}

	static Result totalSpeed(final double targetLattitude, boolean counter) {
		IntegrationApproximator.Function fn = new IntegrationApproximator.Function(targetLattitude, counter);
		Result retval = IntegrationApproximator.integrate(Constants.iterCount, fn);
		return retval;
	}

	static double computeDragRatio(double mass, double distance) {
		double retval = mass / (sq(distance) + 10);
		return retval;
	}

	static double getContractionRatio(double combinedLightSpeed, double relSpeed) {
		double retval = StrictMath.abs(combinedLightSpeed - relSpeed) / Constants.c;
		// double retval2 = ContractionSimulation.getRatio((long) (combinedLightSpeed /
		// relSpeed));
		// System.out.println(retval / retval2);
		return retval;
	}

	static double sq(double x) {
		return x * x;
	}

	static double volume(double r) {
		return r * r * r * 4 * StrictMath.PI / 3;
	}

	static double combineSpeed(double lightComponentSpeed, double relSpeed, double drag) {
		double sunDrag = MGP.computeDragRatio(MGP.Constants.sunMass, MGP.Constants.sunEarthDistance);
		double retval = (lightComponentSpeed * sunDrag + (lightComponentSpeed + relSpeed) * drag) / (sunDrag + drag);
		return retval;
	}

	public static class Result {
		public double speed;
	}

	static class Constants {
		public static double earthRadius = 6.368e6d;
		public static double earthAngularVelocity = 7.2921159e-5d;
		public static double earthMass = 5.972e24d;
		public static double sunMass = 1.989e30;
		public static double sunEarthDistance = 148.53e9;
		public static double earthOrbitalSpeed = 29.78e3;

		public static double northSouthDiff = 339d;
		public static double eastWestDiff = 612d;

		public static double lattitudeDiff = northSouthDiff / earthRadius;

		public static double c = 299792458d;
		public static double lamda = 5.7e-7d;

		public static int iterCount = 80000000;
	}
}
