package experiments;
import java.util.Random;

public class IntegrationApproximator {
	static Random r = new Random();

	public static MGP.Result integrate(int iter, Function fn) {
		double sunDrag = MGP.computeDragRatio(MGP.Constants.sunMass, MGP.Constants.sunEarthDistance);

		double[] values = new double[] { 0, 0, 0, 0 };
		int chunks = (int) Math.pow(iter, 1d / 3);
		double chunkSize = MGP.Constants.earthRadius * 2 / chunks;
		for (int i = 0; i < chunks; i++) {
			for (int j = 0; j < chunks; j++) {
				for (int k = 0; k < chunks; k++) {
					double rx = (-1 + (i / (float) (chunks - 1)) * 2) * MGP.Constants.earthRadius;
					double ry = (-1 + (j / (float) (chunks - 1)) * 2) * MGP.Constants.earthRadius;
					double rz = (-1 + (k / (float) (chunks - 1)) * 2) * MGP.Constants.earthRadius;
					fn.value(rx, ry, rz, values, chunkSize);
				}
			}
		}
		double ans = (values[0] + MGP.Constants.c * sunDrag) / (values[1] + sunDrag);

		MGP.Result retval = new MGP.Result();
		retval.speed = ans;

		return retval;
	}

	public static class Function {
		double targetX, targetY;
		boolean counter;
		double targetTangentVelocity;
		double earthOrbitalSpeed;

		static double sunDrag = MGP.computeDragRatio(MGP.Constants.sunMass, MGP.Constants.sunEarthDistance);

		Function(double targetLattitude, boolean counter) {
			targetX = StrictMath.cos(targetLattitude) * MGP.Constants.earthRadius;
			targetY = StrictMath.sin(targetLattitude) * MGP.Constants.earthRadius;
			this.counter = counter;
			this.targetTangentVelocity = MGP.Constants.earthAngularVelocity * MGP.Constants.earthRadius
					* StrictMath.cos(targetLattitude);
			earthOrbitalSpeed = MGP.Constants.earthOrbitalSpeed;
			if (counter) {
				targetTangentVelocity *= -1d;
				earthOrbitalSpeed *= -1d;
			}
		}

		void value(double x, double y, double z, double[] values, double chunkSize) {
			double radius = StrictMath.sqrt(x * x + y * y + z * z);
			if (radius > MGP.Constants.earthRadius) {
				return;
			}
			double distanceToTarget = StrictMath.sqrt(MGP.sq(x - targetX) + MGP.sq(y - targetY) + z * z);
			double chunkVolume = StrictMath.pow(chunkSize, 3);
			double mass = MGP.density(radius, y) * chunkVolume;

			double zAxisTangentVelocity = MGP.Constants.earthAngularVelocity * x;

			double drag = MGP.computeDragRatio(mass, distanceToTarget);

			double relSpeed = this.targetTangentVelocity - zAxisTangentVelocity - MGP.Constants.earthOrbitalSpeed;

			values[0] += (relSpeed + MGP.Constants.c) * drag;
			values[1] += drag;
		}
	}
}
