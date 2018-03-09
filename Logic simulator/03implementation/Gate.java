// Gate.java
/**
 *  Gates process inputs from Wires and deliver outputs to Wires
 *  @see Wire
 *  @see AndGate
 *  @see OrGate
 *  @see NotGate
 *  @see ConstGate
 *  @author Douglas W. Jones
 *  @author Kaleb McKone
 *  @version 11-13-2017
 *  This solution for MP5 is based on Dr. Jones' solution to MP4.
 *  It fixes the mistake that caused the only partly low-pass filter output.
 *  It also incorporates some random numbers for causing variance in
 *  gate delays to make things more interesting. By default, a gate delay
 *  can be anywhere between 95 percent of its intial value and 105 percent
 *  of its intial value. The specification for PRNG class and randomFloat(i)
 *  (where i represents the maximum for the random float) comes directly
 *  from the interface described in problem for in HW8. Our java files
 *  for the simulation program have been broken up by class, and only
 *  Gate.java is required to turn in for MP5
 */

import java.util.LinkedList;
import java.util.Scanner;

abstract class Gate {
    // constructors may throw this when an error prevents construction
    public static class ConstructorFailure extends Exception {}

    // fields of a gate
    public final String name;            // textual name of gate, never null!
    protected final float delay;         // the delay of this gate, in seconds

    // information about gate connections and logic values is all in subclasses

    /** Constructor used only from within subclasses of class Gate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    protected Gate( String name, float delay ) {
	this.name = name;
	this.delay = delay;
    }

    /** The public use this factory to construct gates
     *  @param sc the scanner from which the gate description is read
     *  @return the newly constructed gate
     */
    public static Gate factory( Scanner sc ) throws ConstructorFailure {
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

    /** tell the gate that one of its input pins is in use
     *  @param w the wire that is connected
     *  @param pinName
     *  @return corresponding pin number
     */
    public abstract int registerInput( Wire w, String pinName );

    /** tell the gate that one of its output pins is in use
     *  @param w the wire that is connected
     *  @param pinName
     *  @return corresponding pin number
     */
    public abstract int registerOutput( Wire w, String pinName );

    /** get the name of the input pin, given its number
     *  @param pinNumber
     *  @return pinName
     */
    public abstract String inPinName( int pinNumber );

    /** get the name of the output pin, given its number
     * @param pinNumber
     * @return pinName
     */
    public abstract String outPinName( int pinNumber );

    /** check the sanity of this gate's connections
     */
    public abstract void checkSanity();

    // Simulation methods

    /** simulate the change of one of this gate's inputs
     *  @param time the time when the input changes
     *  @param dstPin the pin that changes
     *  @param v the new logic value
     */
    public abstract void inputChangeEvent( float time, int dstPin, boolean v );

} // abstract class Gate

/** Gathers all of the properties common to single-output gates
 *  Specifically, all LogicGates drive a single list of output wires
 *  with a single output value when an OutputChangeEvent occurs.
 *  @see AndGate
 *  @see OrGate
 *  @see NotGate
 */
abstract class LogicGate extends Gate {
    // set of all wires out of this gate
    private LinkedList <Wire> outgoing = new LinkedList <Wire> ();

    // this gate's output value
    protected boolean value = false;
    protected boolean lastChange = true;
    /** The constructor used only from subclasses of LogicGate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    public LogicGate( String name, float delay ) {
	super( name, delay );
    }

    /** tell the gate that one of its output pins is in use
     *  @param w the wire that is connected
     *  @param pinName
     *  @return corresponding pin number
     */
    public int registerOutput( Wire w, String pinName ) {
	if ("out".equals( pinName )) {
	    outgoing.add( w );
	    return 0;
	} else {
	    Errors.warn( "Illegal output pin: " + name + " " + pinName );
	    return -1;
	}
    }

    /** get the name of the output pin, given its number
     *  @param pinNumber
     *  @return pinName
     */
    public String outPinName( int pinNumber ) {
	if (pinNumber == 0) return "out";
	return "???";
    }

    // Simulation methods

    /** Simulate an output change on this wire
     *  @param time tells when this wire's input changes
     *  @param newOut used for checking that any series of scheduled events
     *  does not cause no overall change in output to be reported.
     *  Passes the new value to the input of the gate to which this wire goes.
     *  Uses the this.value field to determine the new output value.
     *  Output change events are scheduled (directly or indirectly) by the
     *  input change event of the actual gate object.
     *  @see Gate.inputChangeEvent
     */
    protected void outputChangeEvent( float time, boolean newOut ) {
	if (newOut == value) {
	    System.out.println(
		"At " + time + " " + toString() + " out "
		+ " changes to " + value
	    );
	    for (Wire w: outgoing) {
	         w.inputChangeEvent( time, value );
	    }
	}
    }

} // abstract class LogicGate

/** Handles the properties common to logic gates with two inputs
 *  Specifically, all two-input gates have two input wires, in1 an in2.
 *  @see AndGate
 *  @see OrGate
 *  @see LogicGate
 */
abstract class TwoInputGate extends LogicGate {
    // usage records for inputs
    protected boolean in1used = false;
    protected boolean in2used = false;

    // Boolean values of inputs
    protected boolean in1 = false;
    protected boolean in2 = false;

    /** The constructor used only from subclasses of TwoInputGate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    protected TwoInputGate( String name, float delay ) {
	super( name, delay );
    }

    /** tell the gate that one of its input pins is in use
     *  @param w the wire that is connected
     *  @param pinName
     *  @return corresponding pin number
     */
    public int registerInput( Wire w, String pinName ) {
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

    /** get the name of the input pin, given its number
     * @param pinNumber
     * @return pinName
     */
    public String inPinName( int pinNumber ) {
	if (pinNumber == 1) return "in1";
	if (pinNumber == 2) return "in2";
	return "???";
    }

    /** check the sanity of this gate's connections
     */
    public void checkSanity() {
	if (!in1used) Errors.warn( "Unused input pin: " + name + " in1" );
	if (!in2used) Errors.warn( "Unused input pin: " + name + " in2" );
    }

    // Simulation methods

    /** update the output value of a gate based on its input values
     *  This is called from inputChangeEvent to delegate the response of the
     *  logic gate to the input change to the actual gate instead of this
     *  abstract class.
     *  @param time the value is updated
     */
    abstract void updateValue( float time );

    /** simulate the change of one of this gate's inputs
     *  @param time the time when the input changes
     *  @param dstPin the pin that changes
     *  @param v the new logic value
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
 *  @see TwoInputGate
 *  @see LogicGate
 */
class AndGate extends TwoInputGate {

    /** The constructor used only from within class Gate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    public AndGate( String name, float delay ) {
	super( name, delay );
    }

    /** reconstruct the textual description of this gate
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " and " + delay;
    }

    // Simulation methods

    /** update the output value of a gate based on its input values
     *  only update the value if it's actually a new value and will
     *  not distrupt any previously scheduled events
     *  If we try to schedule a new event before a gate's delay has
     *  even occured, it will cause more output than we are looking for
     *  Uses a 10 percent variance for gate delay
     *  @param time the value is updated
     *  @see PRNG
     */
    void updateValue( float time ) {
	boolean newVal = in1 & in2;
	if (newVal != value && time >= delay) {
	    value = newVal;
	    Simulator.schedule(
		time + delay*(0.95F) + PRNG.randomFloat( delay * 0.1F ),
		(float t) -> outputChangeEvent( t, newVal )
	    );
	}
    }

} // class AndGate

/** Handles the properties specific to or gates.
 *  @see TwoInputGate
 *  @see LogicGate
 */
class OrGate extends TwoInputGate {

    /** The constructor used only from within class Gate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    public OrGate( String name, float delay ) {
	super( name, delay );
    }

    /** reconstruct the textual description of this gate
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " or " + delay;
    }

    // Simulation methods

    /** update the output value of a gate based on its input values
     *  only update the value if it's actually a new value and will
     *  not distrupt any previously scheduled events
     *  If we try to schedule a new event before a gate's delay has
     *  even occured, it will cause more output than we are looking for
     *  Uses a 10 percent variance for gate delay
     *  @param time the value is updated
     *  @see PRNG
     */
    void updateValue( float time ) {
	boolean newVal = in1 | in2;
	if (newVal != value && time >= delay) {
	    value = newVal;
	    Simulator.schedule(
		time + delay*(0.95F) + PRNG.randomFloat( delay * 0.1F ),
		(float t) -> outputChangeEvent( t, newVal )
	    );
	}
    }

} // class OrGate

/** Handles the properties specific to not gates.
 *  @see LogicGate
 */
class NotGate extends LogicGate {
    // usage records for inputs
    private boolean inUsed = false;

    /** The constructor used only from within class Gate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    public NotGate( String name, float delay ) {
	super( name, delay );
    }

    /** tell the gate that one of its input pins is in use
     *  @param w the wire that is connected
     *  @param pinName
     *  @return corresponding pin number
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

    /** get the name of the input pin, given its number
     * @param pinNumber
     * @return pinName
     */
    public String inPinName( int pinNumber ) {
	if (pinNumber == 0) return "in";
	return "???";
    }

    /** check the sanity of this gate's connections
     */
    public void checkSanity() {
	if (!inUsed) Errors.warn( "Unused input pin: " + name + " in" );

	// this is a good time to launch the simulation
	value = true;
	Simulator.schedule( delay, (float t) -> outputChangeEvent( t, true ) );
    }

    /** reconstruct the textual description of this gate
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " not " + delay;
    }

    // Simulation methods

    /** simulate the change of one of this gate's inputs
     *  only schedules a new output if the time is greater
     *  than or equal to the gate delay so it does not distrupt
     *  the initial scheduled event for the not gate
     *  Uses 10 percent variance to add randomness to the delay
     *  @param time the time when the input changes
     *  @param dstPin the pin that changes
     *  @param v the new logic value
     *  @see PRNG
     */
    public void inputChangeEvent( float time, int dstPin, boolean v ) {
	value = !value;
	if (time >= delay) {
	Simulator.schedule(
	    time + delay*(0.95F) + PRNG.randomFloat( delay * 0.1F ),
	    (float t) -> outputChangeEvent(t, !v)
	);
	}
    }

} // class NotGate

/** Handles the properties specific to const gates.
 */
class ConstGate extends Gate {
    // set of all wires out of this gate
    private LinkedList <Wire> outgoingTrue = new LinkedList <Wire> ();
    private LinkedList <Wire> outgoingFalse = new LinkedList <Wire> ();

    /** The constructor used only from within class Gate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    public ConstGate( String name, float delay ) {
	super( name, delay );
    }

    /** tell the gate that one of its input pins is in use
     *  @param w the wire that is connected
     *  @param pinName
     *  @return corresponding pin number
     */
    public int registerInput( Wire w, String pinName ) {
	Errors.warn( "Illegal input pin: " + name + " " + pinName );
	return -1;
    }

    /** tell the gate that one of its output pins is in use
     *  @param w the wire that is connected
     *  @param pinName
     *  @return corresponding pin number
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

    /** get the name of the input pin, given its number
     * @param pinNumber
     * @return pinName
     */
    public String inPinName( int pinNumber ) {
	return "???";
    }

    /** get the name of the output pin, given its number
     * @param pinNumber
     * @return pinName
     */
    public String outPinName( int pinNumber ) {
	if (pinNumber == 0) return "false";
	if (pinNumber == 1) return "true";
	return "???";
    }

    /** check the sanity of this gate's connections
     */
    public void checkSanity() {
	// no sanity check; there are no input pins to check

	// this is a good time to launch the simulation
	Simulator.schedule( delay*(0.95F) + PRNG.randomFloat( 0.1F ),
	    (float t) -> outputChangeEvent( t )
	);
    }

    /** reconstruct the textual description of this gate
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " const " + delay;
    }

    // Simulation methods

    /** simulate the change of one of this gate's inputs
     *  @param time the time when the input changes
     *  @param dstPin the pin that changes
     *  @param v the new logic value
     */
    public void inputChangeEvent( float time, int dstPin, boolean v ) {
	Errors.fatal( "Input should never change: " + toString() );
    }

    private void outputChangeEvent( float time ) {
	System.out.println(
	    "At " + time + " " + toString() + " true " + " changes to true"
	);
	for (Wire w: outgoingTrue) {
	    w.inputChangeEvent( time, true );
	}
    }

} // class ConstGate
