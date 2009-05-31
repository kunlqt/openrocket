package net.sf.openrocket.rocketcomponent;

import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;


/**
 * Class for an axially symmetric rocket component generated by rotating
 * a function y=f(x) >= 0 around the x-axis (eg. tube, cone, etc.)
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public abstract class SymmetricComponent extends BodyComponent implements RadialParent {
	public static final double DEFAULT_RADIUS = 0.025;
	public static final double DEFAULT_THICKNESS = 0.002;
	
	private static final int DIVISIONS = 100;  // No. of divisions when integrating
	
	protected boolean filled = false;
	protected double thickness = DEFAULT_THICKNESS;
	
	
	// Cached data, default values signify not calculated
	private double wetArea = -1;
	private double planArea = -1;
	private double planCenter = -1;
	private double volume = -1;
	private double fullVolume = -1;
	private double longitudalInertia = -1;
	private double rotationalInertia = -1;
	private Coordinate cg = null;
	
	
	
	public SymmetricComponent() {
		super();
	}

	
	/**
	 * Return the component radius at position x.
	 * @param x Position on x-axis.
	 * @return  Radius of the component at the given position, or 0 if outside
	 *          the component.
	 */
	public abstract double getRadius(double x);
	public abstract double getInnerRadius(double x);

	public abstract double getForeRadius();
	public abstract boolean isForeRadiusAutomatic();
	public abstract double getAftRadius();
	public abstract boolean isAftRadiusAutomatic();
	
	
	// Implement the Radial interface:
	public final double getOuterRadius(double x) {
		return getRadius(x);
	}
	
	
	@Override
	public final double getRadius(double x, double theta) {
		return getRadius(x);
	}
	
	@Override
	public final double getInnerRadius(double x, double theta) {
		return getInnerRadius(x);
	}
	
	
	
	/**
	 * Return the component wall thickness.
	 */
	public double getThickness() {
		if (filled)
			return Math.max(getForeRadius(),getAftRadius());
		return Math.min(thickness,Math.max(getForeRadius(),getAftRadius()));
	}
	
	
	/**
	 * Set the component wall thickness.  Values greater than the maximum radius are not
	 * allowed, and will result in setting the thickness to the maximum radius.
	 */
	public void setThickness(double thickness) {
		if ((this.thickness == thickness) && !filled)
			return;
		this.thickness = MathUtil.clamp(thickness,0,Math.max(getForeRadius(),getAftRadius()));
		filled = false;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	
	/**
	 * Returns whether the component is set as filled.  If it is set filled, then the
	 * wall thickness will have no effect. 
	 */
	public boolean isFilled() {
		return filled;
	}
	
	
	/**
	 * Sets whether the component is set as filled.  If the component is filled, then
	 * the wall thickness will have no effect.
	 */
	public void setFilled(boolean filled) {
		if (this.filled == filled)
			return;
		this.filled = filled;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	

	/**
	 * Adds component bounds at a number of points between 0...length.
	 */
	@Override
	public Collection<Coordinate> getComponentBounds() {
		List<Coordinate> list = new ArrayList<Coordinate>(20);
		for (int n=0; n<=5; n++) {
			double x = n*length/5;
			double r = getRadius(x);
			addBound(list,x,r);
		}
		return list;
	}
	
	
	
	/**
	 * Calculate volume of the component by integrating over the length of the component.
	 * The method caches the result, so subsequent calls are instant.  Subclasses may
	 * override this method for simple shapes and use this method as necessary.
	 * 
	 * @return  The volume of the component.
	 */
	@Override
	public double getComponentVolume() {
		if (volume < 0)
			integrate();
		return volume;
	}
	
	
	/**
	 * Calculate full (filled) volume of the component by integrating over the length
	 * of the component.  The method caches the result, so subsequent calls are instant.  
	 * Subclasses may override this method for simple shapes and use this method as 
	 * necessary.
	 * 
	 * @return  The filled volume of the component.
	 */
	public double getFullVolume() {
		if (fullVolume < 0)
			integrate();
		return fullVolume;
	}
	
	
	/**
	 * Calculate the wetted area of the component by integrating over the length 
	 * of the component.  The method caches the result, so subsequent calls are instant. 
	 * Subclasses may override this method for simple shapes and use this method as 
	 * necessary.
	 *  
	 * @return  The wetted area of the component.
	 */
	public double getComponentWetArea() {
		if (wetArea < 0)
			integrate();
		return wetArea;
	}
	
	
	/**
	 * Calculate the planform area of the component by integrating over the length of 
	 * the component.  The method caches the result, so subsequent calls are instant.  
	 * Subclasses may override this method for simple shapes and use this method as 
	 * necessary.
	 *  
	 * @return  The planform area of the component.
	 */
	public double getComponentPlanformArea() {
		if (planArea < 0)
			integrate();
		return planArea;
	}
	
	
	/**
	 * Calculate the planform center X-coordinate of the component by integrating over 
	 * the length of the component.  The planform center is defined as 
	 * <pre>   integrate(x*2*r(x)) / planform area  </pre>
	 * The method caches the result, so subsequent calls are instant.  Subclasses may 
	 * override this method for simple shapes and use this method as necessary.
	 *  
	 * @return  The planform center of the component.
	 */
	public double getComponentPlanformCenter() {
		if (planCenter < 0)
			integrate();
		return planCenter;
	}
	
	
	/**
	 * Calculate CG of the component by integrating over the length of the component.
	 * The method caches the result, so subsequent calls are instant.  Subclasses may
	 * override this method for simple shapes and use this method as necessary.
	 * 
	 * @return  The CG+mass of the component.
	 */
	@Override
	public Coordinate getComponentCG() {
		if (cg == null)
			integrate();
		return cg;
	}
	
	
	@Override
	public double getLongitudalUnitInertia() {
		if (longitudalInertia < 0) {
			if (getComponentVolume() > 0.0000001)  // == 0.1cm^3
				integrateInertiaVolume();
			else
				integrateInertiaSurface();
		}
		return longitudalInertia;
	}
	
	
	@Override
	public double getRotationalUnitInertia() {
		if (rotationalInertia < 0) {
			if (getComponentVolume() > 0.0000001)
				integrateInertiaVolume();
			else
				integrateInertiaSurface();
		}
		return rotationalInertia;
	}
	
	

	/**
	 * Performs integration over the length of the component and updates the cached variables.
	 */
	private void integrate() {
		double x,r1,r2;
		double cgx;
		
		// Check length > 0
		if (length <= 0) {
			wetArea = 0;
			planArea = 0;
			planCenter = 0;
			volume = 0;
			cg = Coordinate.NUL;
			return;
		}
		
		
		// Integrate for volume, CG, wetted area and planform area
		
		final double l = length/DIVISIONS;
		final double pil  = Math.PI*l;    // PI * l
		final double pil3 = Math.PI*l/3;  // PI * l/3
		r1 = getRadius(0);
		x = 0;
		wetArea = 0;
		planArea = 0;
		planCenter = 0;
		fullVolume = 0;
		volume = 0;
		cgx = 0;
		
		for (int n=1; n<=DIVISIONS; n++) {
			/*
			 * r1 and r2 are the two radii
			 * x is the position of r1
			 * hyp is the length of the hypotenuse from r1 to r2
			 * height if the y-axis height of the component if not filled
			 */
			
			r2 = getRadius(x+l);
			final double hyp = MathUtil.hypot(r2-r1, l);
			
			
			// Volume differential elements
			final double dV;
			final double dFullV;
			
			dFullV = pil3*(r1*r1 + r1*r2 + r2*r2);
			if (filled || r1<thickness || r2<thickness) {
				// Filled piece
				dV = dFullV;
			} else {
				// Hollow piece
				final double height = thickness*hyp/l;
				dV = pil*height*(r1+r2-height);
			}

			// Add to the volume-related components
			volume += dV;
			fullVolume += dFullV;
			cgx += (x+l/2)*dV;
			
			// Wetted area ( * PI at the end)
			wetArea += hyp*(r1+r2);
			
			// Planform area & center
			final double p = l*(r1+r2);
			planArea += p;
			planCenter += (x+l/2)*p;
			
			// Update for next iteration
			r1 = r2;
			x += l;
		}
		
		wetArea *= Math.PI;
		
		if (planArea > 0)
			planCenter /= planArea;
		
		if (volume == 0) {
			cg = Coordinate.NUL;
		} else {
			// getComponentMass is safe now
			cg = new Coordinate(cgx/volume,0,0,getComponentMass());
		}
	}
	
	
	/**
	 * Integrate the longitudal and rotational inertia based on component volume.
	 * This method may be used only if the total volume is zero.
	 */
	private void integrateInertiaVolume() {
		double x, r1, r2;

		final double l = length/DIVISIONS;
		final double pil  = Math.PI*l;    // PI * l
		final double pil3 = Math.PI*l/3;  // PI * l/3

		r1 = getRadius(0);
		x = 0;
		longitudalInertia = 0;
		rotationalInertia = 0;
		
		double volume = 0;
		
		for (int n=1; n<=DIVISIONS; n++) {
			/*
			 * r1 and r2 are the two radii, outer is their average
			 * x is the position of r1
			 * hyp is the length of the hypotenuse from r1 to r2
			 * height if the y-axis height of the component if not filled
			 */
			r2 = getRadius(x+l);
			final double outer = (r1 + r2)/2;
			
			
			// Volume differential elements
			final double inner;
			final double dV;
			
			if (filled || r1<thickness || r2<thickness) {
				inner = 0;
				dV = pil3*(r1*r1 + r1*r2 + r2*r2);
			} else {
				final double hyp = MathUtil.hypot(r2-r1, l);
				final double height = thickness*hyp/l;
				dV = pil*height*(r1+r2-height);
				inner = Math.max(outer-height, 0);
			}
			
			rotationalInertia += dV * (pow2(outer) + pow2(inner))/2;
			longitudalInertia += dV * ((3 * (pow2(outer) + pow2(inner)) + pow2(l))/12 
					+ pow2(x+l/2));
			
			volume += dV;
			
			// Update for next iteration
			r1 = r2;
			x += l;
		}
		
		if (MathUtil.equals(volume,0)) {
			integrateInertiaSurface();
			return;
		}
		
		rotationalInertia /= volume;
		longitudalInertia /= volume;

		// Shift longitudal inertia to CG
		longitudalInertia = Math.max(longitudalInertia - pow2(getComponentCG().x), 0);
	}
	
	

	/**
	 * Integrate the longitudal and rotational inertia based on component surface area.
	 * This method may be used only if the total volume is zero.
	 */
	private void integrateInertiaSurface() {
		double x, r1, r2;

		final double l = length/DIVISIONS;

		r1 = getRadius(0);
		x = 0;
		longitudalInertia = 0;
		rotationalInertia = 0;
		
		double surface = 0;
		
		for (int n=1; n<=DIVISIONS; n++) {
			/*
			 * r1 and r2 are the two radii, outer is their average
			 * x is the position of r1
			 * hyp is the length of the hypotenuse from r1 to r2
			 * height if the y-axis height of the component if not filled
			 */
			r2 = getRadius(x+l);
			final double hyp = MathUtil.hypot(r2-r1, l);
			final double outer = (r1 + r2)/2;
			
			final double dS = hyp * (r1+r2) * Math.PI;

			rotationalInertia += dS * pow2(outer);
			longitudalInertia += dS * ((6 * pow2(outer) + pow2(l))/12 + pow2(x+l/2));
			
			surface += dS;
			
			// Update for next iteration
			r1 = r2;
			x += l;
		}

		if (MathUtil.equals(surface, 0)) {
			longitudalInertia = 0;
			rotationalInertia = 0;
			return;
		}
		
		longitudalInertia /= surface;
		rotationalInertia /= surface;
		
		// Shift longitudal inertia to CG
		longitudalInertia = Math.max(longitudalInertia - pow2(getComponentCG().x), 0);
	}
	
	
	
	
	/**
	 * Invalidates the cached volume and CG information.
	 */
	@Override
	protected void componentChanged(ComponentChangeEvent e) {
		super.componentChanged(e);
		if (!e.isOtherChange()) {
			wetArea = -1;
			planArea = -1;
			planCenter = -1;
			volume = -1;
			fullVolume = -1;
			longitudalInertia = -1;
			rotationalInertia = -1;
			cg = null;
		}
	}
	
	
	
	///////////   Auto radius helper methods
	
	
	/**
	 * Returns the automatic radius for this component towards the 
	 * front of the rocket.  The automatics will not search towards the
	 * rear of the rocket for a suitable radius.  A positive return value
	 * indicates a preferred radius, a negative value indicates that a
	 * match was not found.
	 */
	protected abstract double getFrontAutoRadius();

	/**
	 * Returns the automatic radius for this component towards the
	 * end of the rocket.  The automatics will not search towards the
	 * front of the rocket for a suitable radius.  A positive return value
	 * indicates a preferred radius, a negative value indicates that a
	 * match was not found.
	 */
	protected abstract double getRearAutoRadius();
	
	
	
	/**
	 * Return the previous symmetric component, or null if none exists.
	 * NOTE: This method currently assumes that there are no external
	 * "pods".
	 * 
	 * @return	the previous SymmetricComponent, or null.
	 */
	protected final SymmetricComponent getPreviousSymmetricComponent() {
		RocketComponent c;
		for (c = this.getPreviousComponent(); c != null; c = c.getPreviousComponent()) {
			if (c instanceof SymmetricComponent) {
				return (SymmetricComponent)c;
			}
			if (!(c instanceof Stage) &&
				 (c.relativePosition == RocketComponent.Position.AFTER))
				return null;   // Bad component type as "parent"
		}
		return null;
	}
	
	/**
	 * Return the next symmetric component, or null if none exists.
	 * NOTE: This method currently assumes that there are no external
	 * "pods".
	 * 
	 * @return	the next SymmetricComponent, or null.
	 */
	protected final SymmetricComponent getNextSymmetricComponent() {
		RocketComponent c;
		for (c = this.getNextComponent(); c != null; c = c.getNextComponent()) {
			if (c instanceof SymmetricComponent) {
				return (SymmetricComponent)c;
			}
			if (!(c instanceof Stage) &&
				 (c.relativePosition == RocketComponent.Position.AFTER))
				return null;   // Bad component type as "parent"
		}
		return null;
	}
	
}
