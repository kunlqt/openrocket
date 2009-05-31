package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;


/**
 * Base for all rocket components.  This is the "starting point" for all rocket trees.
 * It provides the actual implementations of several methods defined in RocketComponent
 * (eg. the rocket listener lists) and the methods defined in RocketComponent call these.
 * It also defines some other methods that concern the whole rocket, and helper methods
 * that keep information about the program state.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class Rocket extends RocketComponent {
	public static final double DEFAULT_REFERENCE_LENGTH = 0.01;
	
	private static final boolean DEBUG_LISTENERS = false;

	
	/**
	 * The next modification ID to use.  This variable may only be accessed via
	 * the synchronized {@link #getNextModID()} method!
	 */
	private static int nextModID = 1;


	/**
	 * List of component change listeners.
	 */
	private EventListenerList listenerList = new EventListenerList();
	
	/**
	 * When freezeList != null, events are not dispatched but stored in the list.
	 * When the structure is thawed, a single combined event will be fired.
	 */
	private List<ComponentChangeEvent> freezeList = null;
	
	
	private int modID;
	private int massModID;
	private int aeroModID;
	private int treeModID;
	private int functionalModID;
	
	
	private ReferenceType refType = ReferenceType.MAXIMUM;  // Set in constructor
	private double customReferenceLength = DEFAULT_REFERENCE_LENGTH;
	
	
	// The default configuration used in dialogs
	private final Configuration defaultConfiguration;
	
	
	private String designer = "";
	private String revision = "";
	
	
	// Motor configuration list
	private List<String> motorConfigurationIDs = new ArrayList<String>();
	private Map<String, String> motorConfigurationNames = new HashMap<String, String>();
	{
		motorConfigurationIDs.add(null);
	}
	
	
	// Does the rocket have a perfect finish (a notable amount of laminar flow)
	private boolean perfectFinish = false;
	
	
	
	/////////////  Constructor  /////////////
	
	public Rocket() {
		super(RocketComponent.Position.AFTER);
		modID = getNextModID();
		massModID = modID;
		aeroModID = modID;
		treeModID = modID;
		functionalModID = modID;
		defaultConfiguration = new Configuration(this);
	}
	
	
	
	public String getDesigner() {
		return designer;
	}
	
	public void setDesigner(String s) {
		if (s == null)
			s = "";
		designer = s;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	

	public String getRevision() {
		return revision;
	}
	
	public void setRevision(String s) {
		if (s == null)
			s = "";
		revision = s;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	

	/**
	 * Return the number of stages in this rocket.
	 * 
	 * @return   the number of stages in this rocket.
	 */
	public int getStageCount() {
		return this.getChildCount();
	}
	
	
	
	/**
	 * Return the non-negative modification ID of this rocket.  The ID is changed
	 * every time any change occurs in the rocket.  This can be used to check 
	 * whether it is necessary to void cached data in cases where listeners can not
	 * or should not be used.
	 * <p>
	 * Three other modification IDs are also available, {@link #getMassModID()},
	 * {@link #getAerodynamicModID()} {@link #getTreeModID()}, which change every time 
	 * a mass change, aerodynamic change, or tree change occur.  Even though the values 
	 * of the different modification ID's may be equal, they should be treated totally 
	 * separate.
	 * <p>
	 * Note that undo events restore the modification IDs that were in use at the
	 * corresponding undo level.  Subsequent modifications, however, produce modIDs
	 * distinct from those already used.
	 * 
	 * @return   a unique ID number for this modification state.
	 */
	public int getModID() {
		return modID;
	}
	
	/**
	 * Return the non-negative mass modification ID of this rocket.  See
	 * {@link #getModID()} for details.
	 * 
	 * @return   a unique ID number for this mass-modification state.
	 */
	public int getMassModID() {
		return massModID;
	}
	
	/**
	 * Return the non-negative aerodynamic modification ID of this rocket.  See
	 * {@link #getModID()} for details.
	 * 
	 * @return   a unique ID number for this aerodynamic-modification state.
	 */
	public int getAerodynamicModID() {
		return aeroModID;
	}
	
	/**
	 * Return the non-negative tree modification ID of this rocket.  See
	 * {@link #getModID()} for details.
	 * 
	 * @return   a unique ID number for this tree-modification state.
	 */
	public int getTreeModID() {
		return treeModID;
	}
	
	/**
	 * Return the non-negative functional modificationID of this rocket.
	 * This changes every time a functional change occurs.
	 * 
	 * @return	a unique ID number for this functional modification state.
	 */
	public int getFunctionalModID() {
		return functionalModID;
	}
	
	
	
	
	public ReferenceType getReferenceType() {
		return refType;
	}
	
	public void setReferenceType(ReferenceType type) {
		if (refType == type)
			return;
		refType = type;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	public double getCustomReferenceLength() {
		return customReferenceLength;
	}
	
	public void setCustomReferenceLength(double length) {
		if (MathUtil.equals(customReferenceLength, length))
			return;
		
		this.customReferenceLength = Math.max(length,0.001);
		
		if (refType == ReferenceType.CUSTOM) {
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
		}
	}
	
	
	
	
	
	/**
	 * Set whether the rocket has a perfect finish.  This will affect whether the
	 * boundary layer is assumed to be fully turbulent or not.
	 * 
	 * @param perfectFinish		whether the finish is perfect.
	 */
	public void setPerfectFinish(boolean perfectFinish) {
		if (this.perfectFinish == perfectFinish)
			return;
		this.perfectFinish = perfectFinish;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}



	/**
	 * Get whether the rocket has a perfect finish.
	 * 
	 * @return the perfectFinish
	 */
	public boolean isPerfectFinish() {
		return perfectFinish;
	}



	/**
	 * Return a new unique modification ID.  This method is thread-safe.
	 * 
	 * @return  a new modification ID unique to this session.
	 */
	private synchronized int getNextModID() {
		return nextModID++;
	}
	

	/**
	 * Make a deep copy of the Rocket structure.  This is a helper method which simply 
	 * casts the result of the superclass method to a Rocket.
	 */
	@Override
	public Rocket copy() {
		Rocket copy = (Rocket)super.copy();
		copy.resetListeners();
		return copy;
	}
	
	
	

	
	
	/**
	 * Load the rocket structure from the source.  The method loads the fields of this
	 * Rocket object and copies the references to siblings from the <code>source</code>.
	 * The object <code>source</code> should not be used after this call, as it is in
	 * an illegal state!
	 * <p>
	 * This method is meant to be used in conjunction with undo/redo functionality,
	 * and therefore fires an UNDO_EVENT, masked with all applicable mass/aerodynamic/tree
	 * changes.
	 */
	public void loadFrom(Rocket r) {
		super.copyFrom(r);
		
		int type = ComponentChangeEvent.UNDO_CHANGE | ComponentChangeEvent.NONFUNCTIONAL_CHANGE;
		if (this.massModID != r.massModID)
			type |= ComponentChangeEvent.MASS_CHANGE;
		if (this.aeroModID != r.aeroModID)
			type |= ComponentChangeEvent.AERODYNAMIC_CHANGE;
		if (this.treeModID != r.treeModID)
			type |= ComponentChangeEvent.TREE_CHANGE;
		
		this.modID = r.modID;
		this.massModID = r.massModID;
		this.aeroModID = r.aeroModID;
		this.treeModID = r.treeModID;
		this.functionalModID = r.functionalModID;
		this.refType = r.refType;
		this.customReferenceLength = r.customReferenceLength;
		
		this.motorConfigurationIDs = r.motorConfigurationIDs;
		this.motorConfigurationNames = r.motorConfigurationNames;
		this.perfectFinish = r.perfectFinish;
		
		fireComponentChangeEvent(type);
	}

	
	
	
	///////  Implement the ComponentChangeListener lists
	
	/**
	 * Creates a new EventListenerList for this component.  This is necessary when cloning
	 * the structure.
	 */
	public void resetListeners() {
//		System.out.println("RESETTING LISTENER LIST of Rocket "+this);
		listenerList = new EventListenerList();
	}
	
	
	public void printListeners() {
		System.out.println(""+this+" has "+listenerList.getListenerCount()+" listeners:");
		Object[] list = listenerList.getListenerList();
		for (int i=1; i<list.length; i+=2)
			System.out.println("  "+((i+1)/2)+": "+list[i]);
	}
	
	@Override
	public void addComponentChangeListener(ComponentChangeListener l) {
		listenerList.add(ComponentChangeListener.class,l);
		if (DEBUG_LISTENERS)
			System.out.println(this+": Added listner (now "+listenerList.getListenerCount()+
					" listeners): "+l);
	}
	@Override
	public void removeComponentChangeListener(ComponentChangeListener l) {
		listenerList.remove(ComponentChangeListener.class, l);
		if (DEBUG_LISTENERS)
			System.out.println(this+": Removed listner (now "+listenerList.getListenerCount()+
					" listeners): "+l);
	}
	

	@Override
	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class,l);
		if (DEBUG_LISTENERS)
			System.out.println(this+": Added listner (now "+listenerList.getListenerCount()+
					" listeners): "+l);
	}
	@Override
	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
		if (DEBUG_LISTENERS)
			System.out.println(this+": Removed listner (now "+listenerList.getListenerCount()+
					" listeners): "+l);
	}

	
	@Override
	protected void fireComponentChangeEvent(ComponentChangeEvent e) {

		// Update modification ID's only for normal (not undo/redo) events
		if (!e.isUndoChange()) {
			modID = getNextModID();
			if (e.isMassChange())
				massModID = modID;
			if (e.isAerodynamicChange())
				aeroModID = modID;
			if (e.isTreeChange())
				treeModID = modID;
			if (e.getType() != ComponentChangeEvent.NONFUNCTIONAL_CHANGE)
				functionalModID = modID;
		}
		
		if (DEBUG_LISTENERS)
			System.out.println("FIRING "+e);
		
		// Check whether frozen
		if (freezeList != null) {
			freezeList.add(e);
			return;
		}
		
		// Notify all components first
		Iterator<RocketComponent> iterator = this.deepIterator(true);
		while (iterator.hasNext()) {
			iterator.next().componentChanged(e);
		}

		// Notify all listeners
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == ComponentChangeListener.class) {
				((ComponentChangeListener) listeners[i+1]).componentChanged(e);
			} else if (listeners[i] == ChangeListener.class) {
				((ChangeListener) listeners[i+1]).stateChanged(e);
			}
		}
	}
	
		
	/**
	 * Freezes the rocket structure from firing any events.  This may be performed to
	 * combine several actions on the structure into a single large action.
	 * <code>thaw()</code> must always be called afterwards.
	 * 
	 * NOTE:  Always use a try/finally to ensure <code>thaw()</code> is called:
	 * <pre>
	 *     Rocket r = c.getRocket();
	 *     try {
	 *         r.freeze();
	 *         // do stuff
	 *     } finally {
	 *         r.thaw();
	 *     }
	 * </pre>
	 * 
	 * @see #thaw()
	 */
	public void freeze() {
		if (freezeList == null)
			freezeList = new LinkedList<ComponentChangeEvent>();
	}
	
	/**
	 * Thaws a frozen rocket structure and fires a combination of the events fired during
	 * the freeze.  The event type is a combination of those fired and the source is the
	 * last component to have been an event source.
	 *
	 * @see #freeze()
	 */
	public void thaw() {
		if (freezeList == null)
			return;
		if (freezeList.size()==0) {
			freezeList = null;
			return;
		}
		
		int type = 0;
		Object c = null;
		for (ComponentChangeEvent e: freezeList) {
			type = type | e.getType();
			c = e.getSource();
		}
		freezeList = null;
		
		fireComponentChangeEvent(new ComponentChangeEvent((RocketComponent)c,type));
	}
	
	

	
	////////  Motor configurations  ////////
	
	
	/**
	 * Return the default configuration.  This should be used in the user interface
	 * to ensure a consistent rocket configuration between dialogs.  It should NOT
	 * be used in simulations not relating to the UI.
	 * 
	 * @return   the default {@link Configuration}.
	 */
	public Configuration getDefaultConfiguration() {
		return defaultConfiguration;
	}
	
	
	/**
	 * Return an array of the motor configuration IDs.  This array is guaranteed
	 * to contain the <code>null</code> ID as the first element.
	 * 
	 * @return  an array of the motor configuration IDs.
	 */
	public String[] getMotorConfigurationIDs() {
		return motorConfigurationIDs.toArray(new String[0]);
	}
	
	/**
	 * Add a new motor configuration ID to the motor configurations.  The new ID
	 * is returned.
	 * 
	 * @return  the new motor configuration ID.
	 */
	public String newMotorConfigurationID() {
		String id = UUID.randomUUID().toString();
		motorConfigurationIDs.add(id);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
		return id;
	}
	
	/**
	 * Add a specified motor configuration ID to the motor configurations.
	 * 
	 * @param id	the motor configuration ID.
	 * @return		true if successful, false if the ID was already used.
	 */
	public boolean addMotorConfigurationID(String id) {
		if (id == null || motorConfigurationIDs.contains(id))
			return false;
		motorConfigurationIDs.add(id);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
		return true;
	}

	/**
	 * Remove a motor configuration ID from the configuration IDs.  The <code>null</code>
	 * ID cannot be removed, and an attempt to remove it will be silently ignored.
	 * 
	 * @param id   the motor configuration ID to remove
	 */
	public void removeMotorConfigurationID(String id) {
		if (id == null)
			return;
		motorConfigurationIDs.remove(id);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
	}

	
	/**
	 * Return the user-set name of the motor configuration.  If no name has been set,
	 * returns an empty string (not null).
	 *  
	 * @param id   the motor configuration id
	 * @return	   the configuration name
	 */
	public String getMotorConfigurationName(String id) {
		String s = motorConfigurationNames.get(id);
		if (s == null)
			return "";
		return s;
	}
	
	
	/**
	 * Set the name of the motor configuration.  A name can be unset by passing
	 * <code>null</code> or an empty string.
	 * 
	 * @param id	the motor configuration id
	 * @param name	the name for the motor configuration
	 */
	public void setMotorConfigurationName(String id, String name) {
		motorConfigurationNames.put(id,name);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
	}
	
		
	/**
	 * Return a description for the motor configuration.  This is either the 
	 * name previously set by {@link #setMotorConfigurationName(String, String)} or
	 * a string generated from the motor designations of the components.
	 * 
	 * @param id  the motor configuration ID.
	 * @return    a textual representation of the configuration
	 */
	@SuppressWarnings("null")
	public String getMotorConfigurationDescription(String id) {
		String name;
		int motorCount = 0;
		
		if (!motorConfigurationIDs.contains(id)) {
			throw new IllegalArgumentException("Motor configuration ID does not exist: "+id);
		}
		
		name = motorConfigurationNames.get(id);
		if (name != null  &&  !name.equals(""))
			return name;
		
		// Generate the description
		
		// First iterate over each stage and store the designations of each motor
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> currentList = null;
		
		Iterator<RocketComponent> iterator = this.deepIterator();
		while (iterator.hasNext()) {
			RocketComponent c = iterator.next();
			
			if (c instanceof Stage) {
				
				currentList = new ArrayList<String>();
				list.add(currentList);
				
			} else if (c instanceof MotorMount) {
				
				MotorMount mount = (MotorMount) c;
				Motor motor = mount.getMotor(id);
				
				if (mount.isMotorMount() && motor != null) {
					String designation = motor.getDesignation(mount.getMotorDelay(id));
					
					for (int i=0; i < mount.getMotorCount(); i++) {
						currentList.add(designation);
						motorCount++;
					}
				}
				
			}
		}
		
		if (motorCount == 0) {
			return "[No motors]";
		}
		
		// Change multiple occurrences of a motor to n x motor 
		List<String> stages = new ArrayList<String>();
		
		for (List<String> stage: list) {
			String stageName = "";
			String previous = null;
			int count = 0;
			
			Collections.sort(stage);
			for (String current: stage) {
				if (current.equals(previous)) {
					
					count++;
					
				} else {
					
					if (previous != null) {
						String s = "";
						if (count > 1) {
							s = "" + count + "\u00d7" + previous;
						} else {
							s = previous;
						}
						
						if (stageName.equals(""))
							stageName = s;
						else
							stageName = stageName + "," + s;
					}
					
					previous = current;
					count = 1;
					
				}
			}
			if (previous != null) {
				String s = "";
				if (count > 1) {
					s = "" + count + "\u00d7" + previous;
				} else {
					s = previous;
				}
				
				if (stageName.equals(""))
					stageName = s;
				else
					stageName = stageName + "," + s;
			}
			
			stages.add(stageName);
		}
		
		name = "[";
		for (int i=0; i < stages.size(); i++) {
			String s = stages.get(i);
			if (s.equals(""))
				s = "None";
			if (i==0)
				name = name + s;
			else
				name = name + "; " + s;
		}
		name += "]";
		return name;
	}
	


	////////  Obligatory component information
	
	
	@Override
	public String getComponentName() {
		return "Rocket";
	}

	@Override
	public Coordinate getComponentCG() {
		return new Coordinate(0,0,0,0);
	}

	@Override
	public double getComponentMass() {
		return 0;
	}

	@Override
	public double getLongitudalUnitInertia() {
		return 0;
	}

	@Override
	public double getRotationalUnitInertia() {
		return 0;
	}
	
	@Override
	public Collection<Coordinate> getComponentBounds() {
		return Collections.emptyList();
	}

	@Override
	public boolean isAerodynamic() {
		return false;
	}

	@Override
	public boolean isMassive() {
		return false;
	}

	/**
	 * Allows only <code>Stage</code> components to be added to the type Rocket.
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return (Stage.class.isAssignableFrom(type));
	}
}
