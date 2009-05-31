package net.sf.openrocket.util;

public class MathUtil {
	public static final double EPSILON = 0.00000001;  // 10mm^3 in m^3

	/**
	 * The square of x (x^2).  On Sun's JRE using this method is as fast as typing x*x. 
	 * @param x  x
	 * @return   x^2
	 */
	public static double pow2(double x) {
		return x*x;
	}
	
	/**
	 * The cube of x (x^3).
	 * @param x  x
	 * @return   x^3
	 */
	public static double pow3(double x) {
		return x*x*x;
	}
	
	public static double pow4(double x) {
		return (x*x)*(x*x);
	}
	
	/**
	 * Clamps the value x to the range min - max.  
	 * @param x    Original value.
	 * @param min  Minimum value to return.
	 * @param max  Maximum value to return.
	 * @return     The clamped value.
	 */
	public static double clamp(double x, double min, double max) {
		if (x < min)
			return min;
		if (x > max)
			return max;
		return x;
	}
	
	public static float clamp(float x, float min, float max) {
		if (x < min)
			return min;
		if (x > max)
			return max;
		return x;
	}
	
	public static int clamp(int x, int min, int max) {
		if (x < min)
			return min;
		if (x > max)
			return max;
		return x;
	}
	
	
	/**
	 * Maps a value from one value range to another.
	 * 
	 * @param value		the value to map.
	 * @param fromMin	the minimum of the starting range.
	 * @param fromMax	the maximum of the starting range.
	 * @param toMin		the minimum of the destination range.
	 * @param toMax		the maximum of the destination range.
	 * @return			the mapped value.
	 * @throws	IllegalArgumentException  if fromMin == fromMax, but toMin != toMax.
	 */
	public static double map(double value, double fromMin, double fromMax,
			double toMin, double toMax) {
		if (equals(toMin, toMax))
			return toMin;
		if (equals(fromMin, fromMax)) {
			throw new IllegalArgumentException("from range is singular an to range is not.");
		}
		return (value - fromMin)/(fromMax-fromMin) * (toMax - toMin) + toMin;
	}
	
	/**
	 * Compute the minimum of two values.  This is performed by direct comparison. 
	 * However, if one of the values is NaN and the other is not, the non-NaN value is
	 * returned.
	 */
	public static double min(double x, double y) {
		if (Double.isNaN(y))
			return x;
		return (x < y) ? x : y;
	}
	
	/**
	 * Compute the maximum of two values.  This is performed by direct comparison. 
	 * However, if one of the values is NaN and the other is not, the non-NaN value is
	 * returned.
	 */
	public static double max(double x, double y) {
		if (Double.isNaN(x))
			return y;
		return (x < y) ? y : x;
	}
	
	/**
	 * Compute the minimum of three values.  This is performed by direct comparison. 
	 * However, if one of the values is NaN and the other is not, the non-NaN value is
	 * returned.
	 */
	public static double min(double x, double y, double z) {
		if (x < y || Double.isNaN(y)) {
			return min(x,z);
		} else {
			return min(y,z);
		}
	}
	
	/**
	 * Compute the maximum of three values.  This is performed by direct comparison. 
	 * However, if one of the values is NaN and the other is not, the non-NaN value is
	 * returned.
	 */
	public static double max(double x, double y, double z) {
		if (x > y || Double.isNaN(y)) {
			return max(x,z);
		} else {
			return max(y,z);
		}
	}
	
	/**
	 * Calculates the hypotenuse <code>sqrt(x^2+y^2)</code>.  This method is SIGNIFICANTLY
	 * faster than <code>Math.hypot(x,y)</code>.
	 */
	public static double hypot(double x, double y) {
		return Math.sqrt(x*x + y*y);
	}

	/**
	 * Reduce the angle x to the range 0 - 2*PI.
	 * @param x  Original angle.
	 * @return   The equivalent angle in the range 0 ... 2*PI.
	 */
	public static double reduce360(double x) {
		double d = Math.floor(x / (2*Math.PI));
		return x - d*2*Math.PI;
	}

	/**
	 * Reduce the angle x to the range -PI - PI.
	 * 
	 * Either -PI and PI might be returned, depending on the rounding function. 
	 * 
	 * @param x  Original angle.
	 * @return   The equivalent angle in the range -PI ... PI.
	 */
	public static double reduce180(double x) {
		double d = Math.rint(x / (2*Math.PI));
		return x - d*2*Math.PI;
	}
	
	
	public static boolean equals(double a, double b) {
		double absb = Math.abs(b);
		
		if (absb < EPSILON/2) {
			// Near zero
			return Math.abs(a) < EPSILON/2;
		}
		return Math.abs(a-b) < EPSILON*absb;
	}
	
	public static double sign(double x) {
		return (x<0) ? -1.0 : 1.0;
	}

	/* Math.abs() is about 3x as fast as this:
	
	public static double abs(double x) {
		return (x<0) ? -x : x;
	}
	*/
	
	
	public static void main(String[] arg) {
		double nan = Double.NaN;
		System.out.println("min(5,6)     = " + min(5, 6));
		System.out.println("min(5,nan)   = " + min(5, nan));
		System.out.println("min(nan,6)   = " + min(nan, 6));
		System.out.println("min(nan,nan) = " + min(nan, nan));
		System.out.println();
		System.out.println("max(5,6)     = " + max(5, 6));
		System.out.println("max(5,nan)   = " + max(5, nan));
		System.out.println("max(nan,6)   = " + max(nan, 6));
		System.out.println("max(nan,nan) = " + max(nan, nan));
		System.out.println();
		System.out.println("min(5,6,7)       = " + min(5, 6, 7));
		System.out.println("min(5,6,nan)     = " + min(5, 6, nan));
		System.out.println("min(5,nan,7)     = " + min(5, nan, 7));
		System.out.println("min(5,nan,nan)   = " + min(5, nan, nan));
		System.out.println("min(nan,6,7)     = " + min(nan, 6, 7));
		System.out.println("min(nan,6,nan)   = " + min(nan, 6, nan));
		System.out.println("min(nan,nan,7)   = " + min(nan, nan, 7));
		System.out.println("min(nan,nan,nan) = " + min(nan, nan, nan));
		System.out.println();
		System.out.println("max(5,6,7)       = " + max(5, 6, 7));
		System.out.println("max(5,6,nan)     = " + max(5, 6, nan));
		System.out.println("max(5,nan,7)     = " + max(5, nan, 7));
		System.out.println("max(5,nan,nan)   = " + max(5, nan, nan));
		System.out.println("max(nan,6,7)     = " + max(nan, 6, 7));
		System.out.println("max(nan,6,nan)   = " + max(nan, 6, nan));
		System.out.println("max(nan,nan,7)   = " + max(nan, nan, 7));
		System.out.println("max(nan,nan,nan) = " + max(nan, nan, nan));
		System.out.println();
		
		
	}
	
}
