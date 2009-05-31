package net.sf.openrocket.gui.figureelements;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Prefs;



/**
 * A <code>FigureElement</code> that draws text at different positions in the figure
 * with general data about the rocket.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketInfo implements FigureElement {
	
	// Margin around the figure edges, pixels
	private static final int MARGIN = 8;

	// Font to use
	private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
	private static final Font SMALLFONT = new Font(Font.SANS_SERIF, Font.PLAIN, 9);

	
	private final Caret cpCaret = new CPCaret(0,0);
	private final Caret cgCaret = new CGCaret(0,0);
	
	private final Configuration configuration;
	private final UnitGroup stabilityUnits;
	
	private double cg = 0, cp = 0;
	private double length = 0, diameter = 0;
	private double mass = 0;
	private double aoa = Double.NaN, theta = Double.NaN, mach = Prefs.getDefaultMach();
	
	private WarningSet warnings = null;
	
	private boolean calculatingData = false;
	private FlightData flightData = null;
	
	private Graphics2D g2 = null;
	private float line = 0;
	private float x1, x2, y1, y2;
	
	
	
	
	
	public RocketInfo(Configuration configuration) {
		this.configuration = configuration;
		this.stabilityUnits = UnitGroup.stabilityUnits(configuration);
	}
	
	
	@Override
	public void paint(Graphics2D g2, double scale) {
		throw new UnsupportedOperationException("paint() must be called with coordinates");
	}

	@Override
	public void paint(Graphics2D g2, double scale, Rectangle visible) {
		this.g2 = g2;
		this.line = FONT.getLineMetrics("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
				g2.getFontRenderContext()).getHeight();
		
		x1 = visible.x + MARGIN;
		x2 = visible.x + visible.width - MARGIN;
		y1 = visible.y + line ;
		y2 = visible.y + visible.height - MARGIN;

		drawMainInfo();
		drawStabilityInfo();
		drawWarnings();
		drawFlightInformation();
	}
	
	
	public void setCG(double cg) {
		this.cg = cg;
	}
	
	public void setCP(double cp) {
		this.cp = cp;
	}
	
	public void setLength(double length) {
		this.length = length;
	}
	
	public void setDiameter(double diameter) {
		this.diameter = diameter;
	}
	
	public void setMass(double mass) {
		this.mass = mass;
	}
	
	public void setWarnings(WarningSet warnings) {
		this.warnings = warnings.clone();
	}
	
	public void setAOA(double aoa) {
		this.aoa = aoa;
	}
	
	public void setTheta(double theta) {
		this.theta = theta;
	}
	
	public void setMach(double mach) {
		this.mach = mach;
	}
	
	
	public void setFlightData(FlightData data) {
		this.flightData = data;
	}
	
	public void setCalculatingData(boolean calc) {
		this.calculatingData = calc;
	}
	
	
	
	
	private void drawMainInfo() {
		GlyphVector name = createText(configuration.getRocket().getName());
		GlyphVector lengthLine = createText(
				"Length " + UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(length) +
				", max. diameter " + 
				UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(diameter));
		
		String massText;
		if (configuration.hasMotors())
			massText = "Mass with motors ";
		else
			massText = "Mass with no motors ";
		
		massText += UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(mass);
		
		GlyphVector massLine = createText(massText);

		
		g2.setColor(Color.BLACK);

		g2.drawGlyphVector(name, x1, y1);
		g2.drawGlyphVector(lengthLine, x1, y1+line);
		g2.drawGlyphVector(massLine, x1, y1+2*line);

	}
	
	
	private void drawStabilityInfo() {
		String at;
		
		at = "at M="+UnitGroup.UNITS_COEFFICIENT.getDefaultUnit().toStringUnit(mach);
		if (!Double.isNaN(aoa)) {
			at += " \u03b1=" + UnitGroup.UNITS_ANGLE.getDefaultUnit().toStringUnit(aoa);
		}
		if (!Double.isNaN(theta)) {
			at += " \u0398=" + UnitGroup.UNITS_ANGLE.getDefaultUnit().toStringUnit(theta);
		}
		
		GlyphVector cgValue = createText(
				UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(cg));
		GlyphVector cpValue = createText(
				UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(cp));
		GlyphVector stabValue = createText(
				stabilityUnits.getDefaultUnit().toStringUnit(cp-cg));
				
		GlyphVector cgText = createText("CG:  ");
		GlyphVector cpText = createText("CP:  ");
		GlyphVector stabText = createText("Stability:  ");
		GlyphVector atText = createSmallText(at);

		Rectangle2D cgRect = cgValue.getVisualBounds();
		Rectangle2D cpRect = cpValue.getVisualBounds();
		Rectangle2D cgTextRect = cgText.getVisualBounds();
		Rectangle2D cpTextRect = cpText.getVisualBounds();
		Rectangle2D stabRect = stabValue.getVisualBounds();
		Rectangle2D stabTextRect = stabText.getVisualBounds();
		Rectangle2D atTextRect = atText.getVisualBounds();
		
		double unitWidth = MathUtil.max(cpRect.getWidth(), cgRect.getWidth(),
				stabRect.getWidth());
		double textWidth = Math.max(cpTextRect.getWidth(), cgTextRect.getWidth());
		

		g2.setColor(Color.BLACK);

		g2.drawGlyphVector(stabValue, (float)(x2-stabRect.getWidth()), y1);
		g2.drawGlyphVector(cgValue, (float)(x2-cgRect.getWidth()), y1+line);
		g2.drawGlyphVector(cpValue, (float)(x2-cpRect.getWidth()), y1+2*line);

		g2.drawGlyphVector(stabText, (float)(x2-unitWidth-stabTextRect.getWidth()), y1);
		g2.drawGlyphVector(cgText, (float)(x2-unitWidth-cgTextRect.getWidth()), y1+line);
		g2.drawGlyphVector(cpText, (float)(x2-unitWidth-cpTextRect.getWidth()), y1+2*line);
				
		cgCaret.setPosition(x2 - unitWidth - textWidth - 10, y1+line-0.3*line);
		cgCaret.paint(g2, 1.7);

		cpCaret.setPosition(x2 - unitWidth - textWidth - 10, y1+2*line-0.3*line);
		cpCaret.paint(g2, 1.7);
		
		float atPos;
		if (unitWidth + textWidth + 10 > atTextRect.getWidth()) {
			atPos = (float)(x2-(unitWidth+textWidth+10+atTextRect.getWidth())/2);
		} else {
			atPos = (float)(x2 - atTextRect.getWidth());
		}
		
		g2.setColor(Color.GRAY);
		g2.drawGlyphVector(atText, atPos, y1 + 3*line);

	}

	
	private void drawWarnings() {
		if (warnings == null || warnings.isEmpty())
			return;
		
		GlyphVector[] texts = new GlyphVector[warnings.size()+1];
		double max = 0;
		
		texts[0] = createText("Warning:");
		int i=1;
		for (Warning w: warnings) {
			texts[i] = createText(w.toString());
			i++;
		}
		
		for (GlyphVector v: texts) {
			Rectangle2D rect = v.getVisualBounds();
			if (rect.getWidth() > max)
				max = rect.getWidth();
		}
		

		float y = y2 - line * warnings.size();
		g2.setColor(new Color(255,0,0,130));

		for (GlyphVector v: texts) {
			Rectangle2D rect = v.getVisualBounds();
			g2.drawGlyphVector(v, (float)(x2 - max/2 - rect.getWidth()/2), y);
			y += line;
		}
	}
	
	
	private void drawFlightInformation() {
		double height = drawFlightData();
		
		if (calculatingData) {
			GlyphVector calculating = createText("Calculating...");
			g2.setColor(Color.BLACK);
			g2.drawGlyphVector(calculating, x1, (float)(y2-height));
		}
	}
	
	
	private double drawFlightData() {
		if (flightData == null)
			return 0;
		
		double width=0;
		
		GlyphVector apogee = createText("Apogee: ");
		GlyphVector maxVelocity = createText("Max. velocity: ");
		GlyphVector maxAcceleration = createText("Max. acceleration: ");

		GlyphVector apogeeValue, velocityValue, accelerationValue;
		if (!Double.isNaN(flightData.getMaxAltitude())) {
			apogeeValue = createText(
					UnitGroup.UNITS_DISTANCE.toStringUnit(flightData.getMaxAltitude()));
		} else {
			apogeeValue = createText("N/A");
		}
		if (!Double.isNaN(flightData.getMaxVelocity())) {
			velocityValue = createText(
					UnitGroup.UNITS_VELOCITY.toStringUnit(flightData.getMaxVelocity()) +
					"  (Mach " + 
					UnitGroup.UNITS_COEFFICIENT.toString(flightData.getMaxMachNumber()) + ")");
		} else {
			velocityValue = createText("N/A");
		}
		if (!Double.isNaN(flightData.getMaxAcceleration())) {
			accelerationValue = createText(
					UnitGroup.UNITS_ACCELERATION.toStringUnit(flightData.getMaxAcceleration()));
		} else {
			accelerationValue = createText("N/A");
		}
		
		Rectangle2D rect;
		rect = apogee.getVisualBounds();
		width = MathUtil.max(width, rect.getWidth());
		
		rect = maxVelocity.getVisualBounds();
		width = MathUtil.max(width, rect.getWidth());
		
		rect = maxAcceleration.getVisualBounds();
		width = MathUtil.max(width, rect.getWidth());
		
		width += 5;

		if (!calculatingData) 
			g2.setColor(new Color(0,0,127));
		else
			g2.setColor(new Color(0,0,127,127));

		
		g2.drawGlyphVector(apogee, (float)x1, (float)(y2-2*line));
		g2.drawGlyphVector(maxVelocity, (float)x1, (float)(y2-line));
		g2.drawGlyphVector(maxAcceleration, (float)x1, (float)(y2));

		g2.drawGlyphVector(apogeeValue, (float)(x1+width), (float)(y2-2*line));
		g2.drawGlyphVector(velocityValue, (float)(x1+width), (float)(y2-line));
		g2.drawGlyphVector(accelerationValue, (float)(x1+width), (float)(y2));
		
		return 3*line;
	}
	
	
	
	private GlyphVector createText(String text) {
		return FONT.createGlyphVector(g2.getFontRenderContext(), text);
	}

	private GlyphVector createSmallText(String text) {
		return SMALLFONT.createGlyphVector(g2.getFontRenderContext(), text);
	}

}
