/* Gate.java
 * Representations of logic gates in class Gate and its subsidiaries
 * @author Douglas W. Jones
 * @author Kaleb McKone
 * version 2017-12-4
 * Adapted from Dr. Jones' solution to MP5
 * This version is refactored from the lambda-based framework to a version
 * that utilizes anonymous subclasses of Simulator.Event
 * A large amount of javadoc comments have been extended to be more useful
 * to someone who is learning how the program works via javadoc
 *
 */

import java.util.LinkedList;
import java.util.Scanner;

/** Gates process inputs from {@link Wire Wire}s and deliver outputs to wires.
 *  All gates have input/output wires and all types
 *  of gates that are used in this program will extend this class.
 *  The type of functionality desired in a gate will be defined
 *  at the subclass level through their simulation methods and
 *  the number of input wires will be strictly defined also by
 *  the nature of the desired functionality.
 *  Any gate can have any number of wires connected to their output pin
 *  but any input pin may have only one wire connected to it for any gate.
 *  Concrete implementations of Gates are created with the
 *  {@link #factory(Scanner sc) factory} method.
 *
 *  @see Wire
 *  @see AndGate
 *  @see OrGate
 *  @see NotGate
 *  @see ConstGate
 *  @see #factory(Scanner sc)
 *
 */
public abstract class Gate {
    /** Constructors may throw this when an error prevents construction.
     *  The {@link #factory(Scanner sc) factory} method calls
     *  the constructor of the various subclasses of Gate, and they
     *  decide when this exception gets thrown. This exception getting thrown
     *  indicates that there is an error in describing that specific gate
     *  in the input file.
     *  @see #factory(Scanner sc)
     */
    public static class ConstructorFailure extends Exception {}

    /** The name of the gate is never null and should be unique. It simply
     *  acts as an identifier that the user can utilize in the output of the
     *  program to follow that gate's activity. For example, A, B, and C are
     *  unique identifiers that are easy to distinguish in the program's
     *  output.
     */
    public final String name;

    /** The delay of the gate describies how long the gate takes for a
     *  change in input to be realized at the output. For example, a delay
     *  of 1.0 means that when a gate changes from false to true, the gate
     *  will still be false until 1.0 time units occurs, and at that point
     *  it will be true.
     */
    protected final float delay;

    // information about gate connections and logic values is all in subclasses

    /** Constructor used only from within subclasses of class Gate.
     *  All subclass costructors should call super(name, delay)
     *  in order to initialize the name and delay fields during
     *  their respective construction.
     *  @param name a textual identity of the gate and is final
     *  @param delay the delay value for this gate and is final
     */
    protected Gate( String name, float delay ) {
	this.name = name;
	this.delay = delay;
    }

    /** The public use this factory to construct gates. The scanner takes
     *  care of learning which type of gate the input file specifies and
     *  that determines which concrete constructor gets called. A
     *  {@link ConstructorFailure ConstructorFailure} gets thrown when
     *  the respective constructor determines that the input file is invalid
     *  for describing that gate.
     *  @param sc the scanner from which the textual gate description is read
     *  @throws ConstructorFailure thrown when there is an error in the input
     *  @return the newly constructed gate, always one of the concrete classes
     */
    public final static Gate factory( Scanner sc ) throws ConstructorFailure {
	// tempraries used while constructing a gate
	final String name;
	final String kind;
	final float delay;
	final Gate newGate;

	// scan basic fields of input line
	try {
	    name = ScanSupport.nextName(
		sc, ()->"gate ???"
	    );
	    kind = ScanSupport.nextName(
		sc, ()->"gate " + name + " ???"
	    );
	    delay = ScanSupport.nextFloat(
		sc, ()->"gate " + name + " " + kind + " ???"
	    );
	} catch (ScanSupport.NotFound e) {
	    throw new ConstructorFailure();
	}

	// check the fields
	if (Logic.findGate( name ) != null) {
	    Errors.warn( "Redefinition: gate " + name + " " + kind );
	    sc.nextLine();
	    throw new ConstructorFailure();
	}

	if (delay < 0.0F) Errors.warn(
	    "Negative delay: " + "gate " + name + " " + kind + " " + delay
	    // don't throw a failure here, we can build a gate with this error
	);

	// now construct the right kind of gate
	if ("and".equals( kind )) {
	    newGate = new AndGate( name, delay );
	} else if ("or".equals( kind )) {
	    newGate = new OrGate( name, delay );
	} else if ("not".equals( kind )) {
	    newGate = new NotGate( name, delay );
	} else if ("const".equals( kind )) {
	    newGate = new ConstGate( name, delay );
	} else {
	    Errors.warn( "Unknown gate kind: gate " + name + " " + kind );
	    sc.nextLine();
	    throw new ConstructorFailure();
	}

	ScanSupport.lineEnd( sc, ()->newGate.toString() );
	return newGate;
    }

    /** Registers with the gate that its input is in use, meaning it has
     *  now one wire connected to it. Input pins may only have one wire.
     *  @param w the wire that is connected to this gate's input
     *  @param pinName the textual representation of the input pin
     *  @return a pin number usable as a parameter to
     *  {@link #inPinName inPinName}
     *  @see #inPinName
     */
    public abstract int registerInput( Wire w, String pinName );

    /** Registers with the gate that its output is in use, meaning it has
     *  at least one wire connected to it. Output pins can have any
     *  number of output wires connected.
     *  @param w the wire that is connected to this gate's output
     *  @param pinName the textual representation of the output pin
     *  @return a pin number usable as a parameter to
     *  {@link #outPinName outPinName}.
     *  @see #outPinName
     */
    public abstract int registerOutput( Wire w, String pinName );

    /** Gets the name of the input pin, given its number. The number can
     *  easily be recorded from the return value of
     *  {@link #registerInput registerInput}
     *  @param pinNumber a pin number previously returned by registerInput
     *  @return pinName the textual name of an input pin
     *  @see registerInput
     */
    public abstract String inPinName( int pinNumber );

    /** Gets the name of the output pin, given its number. The number can
     *  easily be recorded from the return value of
     *  {@link #registerOutput registerOutput}
     *  @param pinNumber a pin number previously returned by registerOutput
     *  @return pinName the textual name of an output pin
     *  @see registerOutput
     */
    public abstract String outPinName( int pinNumber );

    /** Checks the sanity of this gate's connections. Input pins should
     *  only have one input wire each and all
     *  {@link LogicGate LogicGate}s need all of their input pins used.
     *  {@link ConstGate ConstGate}s do not have any input pins.
     */
    public abstract void checkSanity();

    // Simulation methods

    /** Represents the event where a change of value has occured at an
     *  input pin for a gate. It should handle the determination of whether
     *  or not a new output will occur based on this change in input.
     *  This method will handle this event differently
     *  depending on the nature of the gate, and ConstGate will have
     *  a basic implementation as it has no input pins.
     *  @param time the time when the input changes
     *  @param dstPin the pin that changes
     *  @param v the new logic value
     */
    public abstract void inputChangeEvent( float time, int dstPin, boolean v );

} // abstract class Gate

/** Gathers all of the properties common to single-output gates.
 *  Specifically, all LogicGates drive a single list of output wires
 *  with a single output value when an OutputChangeEvent occurs.
 *  This class only provides an outputChangeEvent method and extenders
 *  of this class further down the heirarchy will need to implement
 *  their respective inputChangeEvent methods.
 *
 *  @see AndGate
 *  @see OrGate
 *  @see NotGate
 */
abstract class LogicGate extends Gate {
    // set of all wires out of this gate
    private LinkedList <Wire> outgoing = new LinkedList <Wire> ();

    /** This gate's current value, computed by input change events
     *  which are defined in {@link TwoInputGate TwoInputGate} and
     *  {@link NotGate NotGate}.
     */
    protected boolean value = false;

    // this gate's most recent actual output value
    private boolean outValue = false;

    /** The constructor used only from subclasses of LogicGate
     *  All subclass costructors should call super(name, delay)
     *  in order to initialize the name and delay fields during
     *  their respective construction.
     *  @param name a textual identity of the gate and is final
     *  @param delay the delay value for this gate and is final
     */
    public LogicGate( String name, float delay ) {
	super( name, delay );
    }

    /** Registers with the gate that its output is in use, meaning it has
     *  at least one wire connected to it. Output pins can have any
     *  number of output wires connected.
     *  @param w the wire that is connected to this gate's output
     *  @param pinName the textual representation of the output pin
     *  @return a pin number usable as a parameter to
     *  {@link #outPinName outPinName}.
     *  @see #outPinName
     */
    public final int registerOutput( Wire w, String pinName ) {
	if ("out".equals( pinName )) {
	    outgoing.add( w );
	    return 0;
	} else {
	    Errors.warn( "Illegal output pin: " + name + " " + pinName );
	    return -1;
	}
    }

    /** Gets the name of the output pin, given its number. The number can
     *  easily be recorded from the return value of
     *  {@link #registerOutput registerOutput}
     *  @param pinNumber a pin number previously returned by registerOutput
     *  @return pinName the textual name of an output pin
     *  @see registerOutput
     */
    public final String outPinName( int pinNumber ) {
	if (pinNumber == 0) return "out";
	return "???";
    }

    // Simulation methods

    /** Handles the case where an output has been confirmed to change.
     *  After updating the new output value, the value of every wire
     *  attached to the output pin of this gate is also updated and
     *  {@link Wire#inputChangeEvent(float, boolean) Wire.inputChangeEvent}
     *  for every wire is invoked.
     *  @param time tells when this wire's input changes
     *  @see Wire#inputChangeEvent
     */
    protected final void outputChangeEvent( float time ) {
	if (value != outValue) { // only if the output actually changes
	    outValue = value;
	    System.out.println(
		"At " + time + " " + toString() +
		" out " + " changes to " + value
	    );
	    for (Wire w: outgoing) {
		w.inputChangeEvent( time, value );
	    }
	}
    }

} // abstract class LogicGate

/** Handles the properties common to logic gates with two inputs.
 *  Specifically, all two-input gates have two input wires, in1 an in2.
 *  Extenders of this class (AndGate and OrGate) implement the abstract
 *  method updateValue and this class provides inputChangeEvent which
 *  is specific to gates with two inputs.
 *
 *  @see AndGate
 *  @see OrGate
 *  @see LogicGate
 */
abstract class TwoInputGate extends LogicGate {
    // usage records for inputs
    /** Represents the usage of the first input pin of the gate,
     *  always starts false.
     */
    protected boolean in1used = false;

    /** Represents the usage of the second input pin of the gate,
     *  always starts false.
     */
    protected boolean in2used = false;

    // Boolean values of inputs
    /** Represents the current value of input for the first input pin,
     *  always starts as false.
     */
    protected boolean in1 = false;

    /** Represents the current value of input for the second input pin,
     *  always starts as false.
     */
    protected boolean in2 = false;

    /** The constructor used only from subclasses of TwoInputGate
     *  All subclass costructors should call super(name, delay)
     *  in order to initialize the name and delay fields during
     *  their respective construction.
     *  @param name a textual identity of the gate and is final
     *  @param delay the delay value for this gate and is final
     */
    protected TwoInputGate( String name, float delay ) {
	super( name, delay );
    }

    /** Update the gate's inUse fields that keep track of whether
     *  or not a certain input pin is in use. All gates must have their
     *  input pins used and only one wire can be connected to an input pin.
     *  @param w the wire that is connected to this gate's input
     *  @param pinName the textual representation of the input pin
     *  @return a pin number usable as a parameter to
     *  {@link #inPinName inPinName}
     *  @see #inPinName
     */
    public final int registerInput( Wire w, String pinName ) {
	if ("in1".equals( pinName )) {
	    if (in1used) Errors.warn(
		"Multiple uses of input pin: " + name + " in1"
	    );
	    in1used = true;
	    return 1;
	} else if ("in2".equals( pinName )) {
	    if (in2used) Errors.warn(
		"Multiple uses of input pin: " + name + " in2"
	    );
	    in2used = true;
	    return 2;
	} else {
	    Errors.warn( "Illegal input pin: " + name + " " + pinName );
	    return -1;
	}
    }

    /** Gets the name of the input pin, given its number. The number can
     *  easily be recorded from the return value of
     *  {@link #registerInput registerInput}
     *  @param pinNumber a pin number previously returned by registerInput
     *  @return pinName the textual name of an input pin
     *  @see registerInput
     */
    public final String inPinName( int pinNumber ) {
	if (pinNumber == 1) return "in1";
	if (pinNumber == 2) return "in2";
	return "???";
    }

    /** Checks the sanity of this gate's connections. Input pins should
     *  only have one input wire each and all
     *  {@link LogicGate LogicGate}s need all of their input pins used.
     */
    public final void checkSanity() {
	if (!in1used) Errors.warn( "Unused input pin: " + name + " in1" );
	if (!in2used) Errors.warn( "Unused input pin: " + name + " in2" );
    }

    // Simulation methods

    /** Update the output value of a gate based on its input values
     *  This is called from {@link #inputChangeEvent inputChangeEvent}
     *  to delegate the response of the logic gate to the input change
     *  to the concrete gate class instead of this abstract class.
     *  @param time the value is updated
     *  @see inputChangeEvent
     */
    abstract void updateValue( float time );

    /** Handles the updating of one of the gate's inputs based on a change
     *  in value to one of the input pins.
     *  This method calls {@link #updateValue updateValue} in order
     *  to determine if an outputChangeEvent should be scheduled.
     *  @param time the time when the input changes
     *  @param dstPin the pin that changes
     *  @param v the new logic value
     *  @see updateValue
     */
    public void inputChangeEvent( float time, int dstPin, boolean v ) {
	if (dstPin == 1) {
	    in1 = v;
	} else if (dstPin == 2) {
	    in2 = v;
	}
	updateValue( time );
    }

} // abstract class TwoInputGate

/** Handles the properties specific to and gates.
 *  This class implements updateValue, which compares the current
 *  inputs carried by its two input wires by the AND operator.
 *  updateValue is called by inputChangeEvent which is defined in
 *  TwoInputGate, and updateValue will schedule an outputChangeEvent
 *  if it has determined that a change in ouput has occured.
 *  @see TwoInputGate
 *  @see LogicGate
 */
final class AndGate extends TwoInputGate {

    /** Constructor for AndGate, utilizes the class heirarchy to
     *  initialize final variables name and delay.
     *  This class is final and is the concrete class that the
     *  simulator will utilize.
     *  @param name a textual identity of the gate and is final
     *  @param delay the delay value for this gate and is final
     */
    public AndGate( String name, float delay ) {
	super( name, delay );
    }

    /** Reconstruct the textual description of this gate. This is
     *  used for bug testing and viewing the outcome of the simulation.
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " and " + delay;
    }

    // Simulation methods

    /** Update the output value of a gate based on its input values.
     *  This implementation is specific to AndGate as it uses the AND
     *  operator to determine a new value based on its in1 and in2 pins
     *  @param time the value is updated
     */
    void updateValue( float time ) {
	boolean newVal = in1 & in2;
	if (newVal != value) {
	    value = newVal;
	    Simulator.schedule(
	        new Simulator.Event(
		time + (delay * 0.95f)
		+ PRNG.randomFloat( delay * 0.1f ) ) {
		    void trigger() {
			outputChangeEvent( this.time );
		    }
		}
	    );
	}
    }

} // class AndGate

/** Handles the properties specific to or gates.
 *  This class implements updateValue, which compares the current
 *  inputs carried by its two input wires by the OR operator.
 *  updateValue is called by inputChangeEvent which is defined in
 *  TwoInputGate, and updateValue will schedule an outputChangeEvent
 *  if it has determined that a change in ouput has occured.
 *  @see TwoInputGate
 *  @see LogicGate
 */
final class OrGate extends TwoInputGate {

    /** Constructor for OrGate, utilizes the class heirarchy to
     *  initialize final variables name and delay.
     *  This class is final and is the concrete class that the
     *  simulator will utilize.
     *  @param name a textual identity of the gate and is final
     *  @param delay the delay value for this gate and is final
     */
    public OrGate( String name, float delay ) {
	super( name, delay );
    }

    /** Reconstruct the textual description of this gate. This is
     *  used for bug testing and viewing the outcome of the simulation.
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " or " + delay;
    }

    // Simulation methods

    /** Update the output value of a gate based on its input values.
     *  This implementation of updateValue is specific to OrGate as it
     *  uses the OR operator to determine its output value based on
     *  in1 and in2 pins
     *  @param time the value is updated
     */
    void updateValue( float time ) {
	boolean newVal = in1 | in2;
	if (newVal != value) {
	    value = newVal;
	    Simulator.schedule(
	        new Simulator.Event( time + (delay * 0.95f)
		+ PRNG.randomFloat( delay * 0.1f ) ) {
		    void trigger() {
			outputChangeEvent( this.time );
		    }
		}
	    );
	}
    }

} // class OrGate

/** Handles the properties specific to not gates.
 *  NotGates are inverters; their output will always have
 *  the opposite value of their input after the delay of the
 *  gate has occured.
 *  Important to note is that this class directly extends LogicGate and
 *  is fully functional at this level in the heirarchy based on its
 *  definition, this is different than the case for the AndGate and
 *  OrGate which extend TwoInputGate which is an extender of LogicGate.
 *  Its inputChangeEvent handles updating its input since it is simple
 *  to just negate its output when a new input has been detected.
 *  Since all wires start false, the initial scheduling of a not gate
 *  changing to TRUE output is handled in checkSanity and this is scheduled
 *  to occur after the gate delay.
 *  @see LogicGate
 */
final class NotGate extends LogicGate {
    // usage records for inputs
    private boolean inUsed = false;

    /** Constructor for NotGate, utilizes the class heirarchy to
     *  initialize final variables name and delay.
     *  This class is final and is the concrete class that the
     *  simulator will utilize.
     *  @param name a textual identity of the gate and is final
     *  @param delay the delay value for this gate and is final
     */
    public NotGate( String name, float delay ) {
	super( name, delay );
    }

    /** Update the gate's inUse fields that keep track of whether
     *  or not a certain input pin is in use. All gates must have their
     *  input pins used and only one wire can be connected to an input pin.
     *  @param w the wire that is connected to this gate's input
     *  @param pinName the textual representation of the input pin
     *  @return a pin number usable as a parameter to
     *  {@link #inPinName inPinName}
     *  @see #inPinName
     */
    public int registerInput( Wire w, String pinName ) {
	if ("in".equals( pinName )) {
	    if (inUsed) Errors.warn(
		"Multiple uses of input pin: " + name + " in"
	    );
	    inUsed = true;
	    return 0;
	} else {
	    Errors.warn( "Illegal input pin: " + name + " " + pinName );
	    return -1;
	}
    }

    /** Gets the name of the input pin, given its number. The number can
     *  easily be recorded from the return value of
     *  {@link #registerInput registerInput}
     *  @param pinNumber a pin number previously returned by registerInput
     *  @return pinName the textual name of an input pin
     *  @see registerInput
     */
    public String inPinName( int pinNumber ) {
	if (pinNumber == 0) return "in";
	return "???";
    }

    /** Checks the sanity of this gate's connections. Input pins should
     *  only have one input wire each and all
     *  {@link LogicGate LogicGate}s need all of their input pins used.
     */
    public void checkSanity() {
	if (!inUsed) Errors.warn( "Unused input pin: " + name + " in" );

	// this is a good time to launch the simulation
	value = true;
	Simulator.schedule(
	    new Simulator.Event( delay ) {
	        void trigger() {
		    outputChangeEvent( this.time );
	        }
	    }
	);
    }

    /** Reconstruct the textual description of this gate. This is
     *  used for bug testing and viewing the outcome of the simulation.
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " not " + delay;
    }

    // Simulation methods

    /** simulate the change of one of this gate's inputs
     *  This implementation of inputChangeEvent is unique to the NotGate.
     *  Every time a new input occurs, it is implicit that the new output
     *  is just a negation of that new input. Therefore, NotGate does not
     *  require an updateValue like AndGate and OrGate do.
     *  @param time the time when the input changes
     *  @param dstPin the pin that changes
     *  @param v the new logic value
     */
    public void inputChangeEvent( float time, int dstPin, boolean v ) {
	value = !v;
	    Simulator.schedule(
	        new Simulator.Event( time
		    + (delay * 0.95f)
		    + PRNG.randomFloat( delay * 0.1f ) ) {
		        void trigger() {
			    outputChangeEvent( this.time );
		        }
		}
	    );
    }

} // class NotGate

/** Handles the properties specific to const gates
 *  Const gates have no input and have two very particular outputs
 *  One pin will always have a TRUE output once its delay has occured
 *  and the other pin will always be FALSE at all times of the simulation
 *  These are useful for adding a TRUE input to stimulate the simulation,
 *  although a properly placed NotGate could potentially provide the same
 *  functionality.
 *  In a similar manner to the NotGate, an outputChangeEvent is scheduled to
 *  occur after the delay of the gate in its checkSanity method
 *  ConstGate directly extends Gate, and it defines unique inputChangeEvent
 *  and outputChangeEvent methods which are based on the unqiue functionality
 *  of the ConstGate
 */
final class ConstGate extends Gate {
    // set of all wires out of this gate
    private LinkedList <Wire> outgoingTrue = new LinkedList <Wire> ();
    private LinkedList <Wire> outgoingFalse = new LinkedList <Wire> ();

    /** Constructor for ConstGate, utilizes the class heirarchy to
     *  initialize final variables name and delay.
     *  This class is final and is the concrete class that the
     *  simulator will utilize.
     *  @param name a textual identity of the gate and is final
     *  @param delay the delay value for this gate and is final
     */
    public ConstGate( String name, float delay ) {
	super( name, delay );
    }
    /**ConstGates have no input pins so this implementation of registerInput
    *  always returns -1 and handles the error in the input file accordingly.
    *  @param w the wire that is connected to this gate's input
    *  @param pinName the textual representation of the input pin
    *  @return -1 as to indicate the ConstGate has no input pin
    */
    public int registerInput( Wire w, String pinName ) {
	Errors.warn( "Illegal input pin: " + name + " " + pinName );
	return -1;
    }

    /** Registers with the gate that its output is in use, meaning it has
     *  at least one wire connected to it. Output pins can have any
     *  number of output wires connected.
     *  @param w the wire that is connected to this gate's output
     *  @param pinName the textual representation of the output pin
     *  @return a pin number usable as a parameter to
     *  {@link #outPinName outPinName}.
     *  @see #outPinName
     */
    public int registerOutput( Wire w, String pinName ) {
	if ("true".equals( pinName )) {
	    outgoingTrue.add( w );
	    return 1;
	} else if ("false".equals( pinName )) {
	    outgoingFalse.add( w );
	    return 0;
	} else {
	    Errors.warn( "Illegal output pin: " + name + " " + pinName );
	    return -1;
	}
    }

    /** Gets the name of the input pin, given its number. The number can
     *  easily be recorded from the return value of
     *  {@link #registerInput registerInput}
     *  @param pinNumber a pin number previously returned by registerInput
     *  @return pinName the textual name of an input pin
     *  @see registerInput
     */
    public String inPinName( int pinNumber ) {
	return "???";
    }

    /** Gets the name of the output pin, given its number. The number can
     *  easily be recorded from the return value of
     *  {@link #registerOutput registerOutput}
     *  @param pinNumber a pin number previously returned by registerOutput
     *  @return pinName the textual name of an output pin
     *  @see registerOutput
     */
    public String outPinName( int pinNumber ) {
	if (pinNumber == 0) return "false";
	if (pinNumber == 1) return "true";
	return "???";
    }

    /** Checks the sanity of this gate's connections
     *  {@link #ConstGate ConstGate}s do not have any input pins.
     *  Instead, this method will initiate simulation by scheduling
     *  its outputChangeEvent.
     */
    public void checkSanity() {
	// no sanity check; there are no input pins to check

	// this is a good time to launch the simulation
	Simulator.schedule(
	    new Simulator.Event( delay ) {
	        void trigger() {
		    outputChangeEvent( this.time );
	        }
	    }
	);
    }

    /** Reconstruct the textual description of this gate. This is
     *  used for bug testing and viewing the outcome of the simulation.
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " const " + delay;
    }

    // Simulation methods

    /** ContGate has no input pins, so this method reports an error
     *  if it is scheduled and invoked.
     *  @param time the time when the input changes
     *  @param dstPin the pin that changes
     *  @param v the new logic value
     */
    public void inputChangeEvent( float time, int dstPin, boolean v ) {
	Errors.fatal( "Input should never change: " + toString() );
    }

    /** Unique output change event handler for ConstGate.
     *  The only time the output of a ConstGate occurs is when its
     *  true output changes to true after the gate delay, so it is
     *  handled in a unique way here.
     *  @param time the time at which the output change occurs
     */
    public void outputChangeEvent( float time ) {
	System.out.println(
	    "At " + time + " " + toString() + " true " + " changes to true"
	);
	for (Wire w: outgoingTrue) {
	    w.inputChangeEvent( time, true );
	}
    }

} // class ConstGate
