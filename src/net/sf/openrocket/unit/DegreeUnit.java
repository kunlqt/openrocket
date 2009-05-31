package net.sf.openrocket.unit;

import java.text.DecimalFormat;

public class DegreeUnit extends GeneralUnit {

	public DegreeUnit() {
		super(Math.PI/180.0,"\u00b0");
	}

	@Override
	public boolean hasSpace() {
		return false;
	}

	@Override
	public double round(double v) {
		return Math.rint(v);
	}

	private final DecimalFormat decFormat = new DecimalFormat("0.#");
	@Override
	public String toString(double value) {
		double val = toUnit(value);
		return decFormat.format(val);
	}
}
