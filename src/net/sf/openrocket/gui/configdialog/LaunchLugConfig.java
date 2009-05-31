package net.sf.openrocket.gui.configdialog;


import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.BasicSlider;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.UnitSelector;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;

public class LaunchLugConfig extends RocketComponentConfig {

	private MotorConfig motorConfigPane = null;

	public LaunchLugConfig(RocketComponent c) {
		super(c);
		
		JPanel primary = new JPanel(new MigLayout("fill"));
		
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::][]",""));
		
		////  Body tube length
		panel.add(new JLabel("Length:"));
		
		DoubleModel m = new DoubleModel(component,"Length",UnitGroup.UNITS_LENGTH,0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.1)),"w 100lp, wrap para");
		
		
		//// Body tube diameter
		panel.add(new JLabel("Outer diameter:"));

		DoubleModel od  = new DoubleModel(component,"Radius",2,UnitGroup.UNITS_LENGTH,0);
		// Diameter = 2*Radius

		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(od),"growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)),"w 100lp, wrap rel");

		
		////  Inner diameter
		panel.add(new JLabel("Inner diameter:"));

		// Diameter = 2*Radius
		m = new DoubleModel(component,"InnerRadius",2,UnitGroup.UNITS_LENGTH,0);
		

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(new DoubleModel(0), od)),"w 100lp, wrap rel");

		
		////  Wall thickness
		panel.add(new JLabel("Thickness:"));
		
		m = new DoubleModel(component,"Thickness",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0,0.01)),"w 100lp, wrap 20lp");
		

		////  Radial direction
		panel.add(new JLabel("Radial position:"));
		
		m = new DoubleModel(component,"RadialDirection",UnitGroup.UNITS_ANGLE,
				-Math.PI, Math.PI);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)),"w 100lp, wrap");
		
		
		
		
		primary.add(panel, "grow, gapright 20lp");
		panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::][]",""));
		
		
		

		panel.add(new JLabel("Position relative to:"));

		JComboBox combo = new JComboBox(
				new EnumModel<RocketComponent.Position>(component, "RelativePosition",
						new RocketComponent.Position[] {
						RocketComponent.Position.TOP,
						RocketComponent.Position.MIDDLE,
						RocketComponent.Position.BOTTOM,
						RocketComponent.Position.ABSOLUTE
				}));
		panel.add(combo,"spanx, growx, wrap");
		
		panel.add(new JLabel("plus"),"right");

		m = new DoubleModel(component,"PositionValue",UnitGroup.UNITS_LENGTH);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap para");

		
		
		//// Material
		materialPanel(panel, Material.Type.BULK);
		
		
		primary.add(panel,"grow");
		

		tabbedPane.insertTab("General", null, primary, "General properties", 0);
		tabbedPane.setSelectedIndex(0);
	}
	
	@Override
	public void updateFields() {
		super.updateFields();
		if (motorConfigPane != null)
			motorConfigPane.updateFields();
	}
	
}
