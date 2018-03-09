/* Logic.java
 * Program to process description of a logic circuit and simulate it
 * @author Douglas W. Jones
 * @author Kaleb McKone
 * version 2017-10-27 (adapted from MP3 solution)
 * Adapted from Logic.java Version 2017-09-27 (the MP2 solution),
 * which was adapted from RoadNetwork.java Version 2017-09-14
 *
 * Class ScanSupport added from lecture notes for Oct. 3 and 5, augmented
 * with ScanSupport.nextFloat from the homework 6 solution.
 *
 * This solution has resolved the issue of the duplicate code between
 * subclasses of Gate by utilzing the heirarchy suggested in HW8.
 *
 * This version of the program will schedule events and cause the simulation
 * to carry out. The effects of the simulation will print to standard output
 * if no errors were found in the input file.
 *
 * Bug notices in the code indicate unsolved problems.
 */

import java.util.regex.Pattern;
import java.util.LinkedList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.PriorityQueue;

/** Error reporting package
 *  provides standard prefix and behavior for messages
 */
class Errors {
    // error messages are counted.
    private static int errorCount = 0;

    /** Allow public read-only access to the count of error messages
     * @return the count
     */
    public static int count() {
	return errorCount;
    }

    /** Report nonfatal errors, output a message and return
     * @arg message the message to output
     */
    public static void warn( String message ) {
	System.err.println( "Logic: " + message );
	errorCount = errorCount + 1;
    }

    /** Report fatal errors, output a message and exit, never to return
     * @arg message the message to output
     */
    public static void fatal( String message ) {
	warn( message );
	System.exit( 1 );
    }
}

/** Support methods for scanning
 * @see Errors
 */
class ScanSupport {
    // patterns needed for scanning
    private static final Pattern name = Pattern.compile(
	"[a-zA-Z0-9_]*"
    );
    private static final Pattern number = Pattern.compile(
	"[0-9][0-9]*\\.?[0-9]*|\\.[0-9][0-9]*|"
    );
    private static final Pattern whitespace = Pattern.compile(
	"[ \t]*" // no newlines in this pattern
    );

    /** Interface used for error messages
     *  Messages are typically formulated as string-valued lambda expressions
     *  so that any concatenations they contain are only computed if the
     *  message is needed.
     */
    public static interface ErrorMessage {
        public String myString();
    }

    /** Get next name without skipping to next line (unlike sc.next())
     *  @param sc the scanner from which end of line is scanned
     *  @param message the message to output if there was no name
     *  @return the name, if there was one, or an empty string
     */
    public static String nextName( Scanner sc, ErrorMessage message ) {
        sc.skip( whitespace );
        sc.skip( name );
        String s = sc.match().group();
        if ("".equals( s )) {
            Errors.warn( "Name expected: " + message.myString() );
            sc.nextLine();
        }
        return s;
    }

    /** Get next float without skipping to next line (unlike sc.nextFloat())
     *  @param sc the scanner from which end of line is scanned
     *  @param message the message to output if there was no float
     *  @return the value, if there was one, or NaN if not
     */
    public static float nextFloat( Scanner sc, ErrorMessage message ) {
        sc.skip( whitespace );
        sc.skip( number );
        String s = sc.match().group();
        if ("".equals( s )) {
            Errors.warn( "Float expected: " + message.myString() );
            sc.nextLine();
	    return Float.NaN;
        }
	// now, s is guaranteed to hold a legal float
	return Float.parseFloat( s );
    }

    /** Advance to next line and complain if is junk at the line end
     *  @see Errors
     *  @param sc the scanner from which end of line is scanned
     *  @param message gives a prefix to give context to error messages
     *  This version supports comments starting with --
     */
    public static void lineEnd( Scanner sc, ErrorMessage message ) {
        sc.skip( whitespace );
        String lineEnd = sc.nextLine();
        if ( (!lineEnd.equals( "" ))
        &&   (!lineEnd.startsWith( "--" )) ) {
            Errors.warn(
                message.myString() +
                " followed unexpected by '" + lineEnd + "'"
            );
        }
    }
}

/** Wires join Gates
 *  @see Gate
 */
class Wire {
    // constructors may throw this when an error prevents construction
    public static class ConstructorFailure extends Exception {}

    // fields of a gate
    private final float delay;        // measured in seconds
    private final Gate source;        // where this wire comes from, never null
    private final String srcPin;      // what pin of source, never null
    private final Gate destination;   // where this wire goes, never null
    private final String dstPin;      // what pin of the destination, never null
    // name of a wire is source-srcpin-destination-dstpin

    private int state = 0; // output state of a wire,
			   // always starts as false

    /** construct a new wire by scanning its description from the source file
     */
    public Wire( Scanner sc ) throws ConstructorFailure {
	String sourceName = ScanSupport.nextName(
	    sc, ()-> "wire ???"
	);
	if ("".equals( sourceName )) throw new ConstructorFailure();

	srcPin = ScanSupport.nextName(
	    sc, ()->"wire " + sourceName + " ???"
	);
	if ("".equals( srcPin )) throw new ConstructorFailure();

	String dstName = ScanSupport.nextName(
	    sc, ()->"wire " + " " + srcPin + " ???"
	);
	if ("".equals( dstName )) throw new ConstructorFailure();

	dstPin = ScanSupport.nextName(
	    sc, ()->"wire " + " " + srcPin + " " + dstName + " ???"
	);
	if ("".equals( dstPin )) throw new ConstructorFailure();

	source = Logic.findGate( sourceName );
	destination = Logic.findGate( dstName );
	if (source == null) {
	    Errors.warn( "No such source gate: wire "
			+ sourceName + " " + srcPin + " "
			+ dstName + " " + dstPin
	    );
	    sc.nextLine();
	    throw new ConstructorFailure();
	}
	if (destination == null) {
	    Errors.warn( "No such destination gate: wire "
			+ sourceName + " " + srcPin + " "
			+ dstName + " " + dstPin
	    );
	    sc.nextLine();
	    throw new ConstructorFailure();
	}

	// take care of source and destination pins
	// Bug:  This is a start, but in the long run, it might not be right
	source.registerOutput( srcPin );
	source.getOutgoing().add(this); // add outgoing wires to the list

	destination.registerInput( dstPin );
	destination.getIncoming().add(this); // add incoming wire to the list

	delay = ScanSupport.nextFloat(
	    sc, ()->"wire "
		+ sourceName + " " + srcPin + " "
		+ dstName + " " + dstPin + " ???"
	);
	if (Float.isNaN( delay )) throw new ConstructorFailure();
	if (delay < 0.0F) Errors.warn( "Negative delay: " + this.toString() );

	ScanSupport.lineEnd( sc, ()->this.toString() );
    }

    /** Simulation methods for wires
     */
    public void inputChangeEvent(float time) {
	Simulator.schedule( time + delay,
	    (float t ) -> outputChangeEvent( t )
	);
    }
    public void outputChangeEvent( float time) {
	invert();
	Simulator.schedule( time,
	    (float t) -> getDstGate().inputChangeEvent( t )
	);
    }

    /** Inverts the state of the wire
     *  Used for when the output of a gate
     *  that a wire is connected to changes
     */
    private void invert() {
	if (state == 0) state = 1;
	else state = 0;
    }

    /** Getters for the final private
     *  fields of the wire that are used
     */
    public String getSrcPin() {
	return this.srcPin;
    }
    public float getDelay() {
	return this.delay;
    }
    public Gate getDstGate() {
	return this.destination;
    }
    public int getState() {
	return this.state;
    }

    /** output the wire in a form like that used for input
     *  @return the textual form
     */
    public String toString() {
	return  "wire "
		+ source.name + " "
		+ srcPin + " "
		+ destination.name + " "
		+ dstPin + " "
		+ delay;
    }
}

/** Gates process inputs from Wires and deliver outputs to Wires
 *  @see Wire
 */
abstract class Gate {

    // constructors may throw this when an error prevents construction
    protected static class ConstructorFailure extends Exception {}

    // fields of a gate
    protected final String name;         // textual name of gate, never null!
    protected final float delay;         // the delay of this gate, in seconds

    protected LinkedList <Wire> outgoing;  // set of all wires out of this gate
    protected LinkedList <Wire> incoming;  // set of all wires in to this gate

    protected int trueCount = 0; // integer representation of trues coming into
				 // this gate. Always starts false since
				 // wires are initially carrying false

    /** The constructor used only from within subclasses
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    protected Gate( String name, float delay ) {
	this.name = name;
	this.delay = delay;
	this.outgoing = new LinkedList<Wire>();
	this.incoming = new LinkedList<Wire>();
    }

    /** The public use this factory to construct gates
     *  @param sc the scanner from which the gate description is read
     *  @return the newly constructed gate
     */
    public static Gate factory( Scanner sc ) throws ConstructorFailure {
	String name = ScanSupport.nextName( sc, ()->"gate ???" );
	if ("".equals( name )) throw new ConstructorFailure();
	String kind = ScanSupport.nextName( sc, ()->"gate " + name + " ???" );
	if ("".equals( kind )) throw new ConstructorFailure();

	if (Logic.findGate( name ) != null) {
	    Errors.warn( "Redefinition: gate " + name + " " + kind );
	    sc.nextLine();
	    throw new ConstructorFailure();
	}

	final float delay = ScanSupport.nextFloat(
	    sc, ()->"gate " + name + " " + kind + " ???"
	);
	if (Float.isNaN( delay )) throw new ConstructorFailure();
	if (delay < 0.0F) Errors.warn(
	    "Negative delay: " + "gate " + name + " " + kind + " " + delay
	);

	final Gate newGate; // initialized by one of the alternatives below
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

    /** Schedules the change of state for every
     *  wire going out a gate, if this gets scheduled
     *  then it has been determined that the output
     *  of a gate has changed and occured at the param time
     *  @param time the time the output change occured
    */
    protected void outputChangeEvent(float time) {
	for (Wire wire : outgoing) {
	    Simulator.schedule( time,
		(float t) -> wire.inputChangeEvent( t )
	    );
	}
    }

    /** Updates the recorded number of
     *  inputs to the gate that are true
     */
    public void countTrueInputs() {
	trueCount = 0;
	for (Wire wire: incoming) {
	    if (wire.getState() == 1) {
		trueCount++;
	    }
	}
    }

    /** Getters for private fields of
     *  gates
     */
    public int getTrueCount() {
	return this.trueCount;
    }
    public LinkedList<Wire> getOutgoing() {
	return this.outgoing;
    }
    public LinkedList<Wire> getIncoming() {
	return this.incoming;
    }
    public float getDelay() {
	return this.delay;
    }

    /** tell the gate that one of its input pins is in use
     * @param pinName
     */
    public abstract void registerInput( String pinName );

    /** tell the gate that one of its output pins is in use
     * @param pinName
     */
    public abstract void registerOutput( String pinName );

    /** check the sanity of this gate's connections
     */
    public abstract void checkSanity();

    /** schedules an output change if the input change
     *  causes a change in input
     *  @param time the time at which this event occured
     */
    protected abstract void inputChangeEvent(float time);

} // class Gate

abstract class LogicGate extends Gate {

    /** The constructor used only from within class LogicGate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    protected LogicGate(String name, float delay) {
	super(name, delay);
    }

    /** tell the gate that one of its output pins is in use
     *  @param pinName
     */
    public void registerOutput( String pinName ) {
        if ("out".equals( pinName )) {
	    // Bug:  Do we do anything?
	} else {
	    Errors.warn( "Illegal output pin: " + name + " " + pinName );
	}
    }

    /** schedules an output change if the input change causes
     *  a change in output
     *  @param time the time at which this event occured
     */
    protected void inputChangeEvent(float time) {
	if (newOutput( time )) {
	    Simulator.schedule( time + delay,
		(float t) -> outputChangeEvent( t )
	    );
        }
    }

    /** Unique to each logic gate since different work must
     *  be done to determine if an output change occurs due to
     *  a change in input
     *  @param time the time at which the output is being checked
     */
    public abstract boolean newOutput(float time);
}

abstract class TwoInGate extends LogicGate {

    // usage records for inputs
    private boolean in1used = false;
    private boolean in2used = false;

    /** The constructor used only from within class TwoInGate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    protected TwoInGate(String name, float delay) {
	super(name, delay);
    }

    /** tell the gate that one of its input pins is in use
     *  @param pinName
     */
    public void registerInput( String pinName ) {
        if ("in1".equals( pinName )) {
	    if (in1used) Errors.warn(
		"Multiple uses of input pin: " + name + " in1"
	    );
	    in1used = true;
	} else if ("in2".equals( pinName )) {
	    if (in2used) Errors.warn(
		"Multiple uses of input pin: " + name + " in2"
	    );
	    in2used = true;
	} else {
	    Errors.warn( "Illegal input pin: " + name + " " + pinName );
	}
    }

    /** check the sanity of this gate's connections
     */
    public void checkSanity() {
	if (!in1used) Errors.warn( "Unused input pin: " + name + " in1" );
	if (!in2used) Errors.warn( "Unused input pin: " + name + " in2" );
    }
}

class AndGate extends TwoInGate {

    /** The constructor used only from within class AndGate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    protected AndGate( String name, float delay ) {
	super( name, delay );
    }

    public boolean newOutput(float time) {
	int initialTrues = getTrueCount();
	countTrueInputs();
	int newTrues = getTrueCount();
	if (initialTrues == 2 && newTrues < 2) {
	    System.out.println( time + ": " + name + " out false" );
	    return true;
	}
	else if (initialTrues < 2 && newTrues == 2) {
	    System.out.println( time + ": " + name + " out true");
	    return true;
	}
	return false;
    }

    /** reconstruct the textual description of this gate
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " and " + delay;
    }

} // class AndGate

class OrGate extends TwoInGate {

    /** The constructor used only from within class OrGate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    protected OrGate( String name, float delay ) {
	super( name, delay );
    }

    public boolean newOutput(float time) {
	int initialTrues = getTrueCount();
	countTrueInputs();
	int newTrues = getTrueCount();
	if (initialTrues >= 1  && newTrues < 1) {
	    System.out.println( time + ": " + name + " out false" );
	    return true;
	}
	else if (initialTrues < 1 && newTrues >= 1) {
	    System.out.println( time + ": " + name + " out true" );
	    return true;
	}
	return false;
    }

    /** reconstruct the textual description of this gate
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " or " + delay;
    }

} // class OrGate

class NotGate extends LogicGate {

    // usage records for inputs
    private boolean inUsed = false;
    // keeps track of the output of the NotGate

    private boolean state = false;
    // used only for not gates, keeps track of the current
    // output state

    /** The constructor used only from within class NotGate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    protected NotGate( String name, float delay ) {
	super( name, delay );
	Simulator.schedule( delay, (float t) -> inputChangeEvent( t ) );
    }

    public boolean newOutput(float time) {
	countTrueInputs();
	int newTrues = getTrueCount();

	if (newTrues == 0 && state == false) {
	    System.out.println( time + ": " + name + " out true");
	    state = true;
	    return true;
	}
	else if (newTrues == 1 && state == true) {
	    System.out.println( time + ": " + name + " out false" );
	    state = false;
	    return true;
	}
	return false;
    }

    /** tell the gate that its input pin is in use
     *  @param pinName
     */
    public void registerInput( String pinName ) {
        if ("in".equals( pinName )) {
	    if (inUsed) Errors.warn(
		"Multiple uses of input pin: " + name + " in"
	    );
	    inUsed = true;
	}
    }

    /** check the sanity of this gate's connections
     */
    public void checkSanity() {
	if (!inUsed) Errors.warn( "Unused input pin: " + name + " in" );
    }

    /** reconstruct the textual description of this gate
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " not " + delay;
    }
} // class NotGate

class ConstGate extends Gate {

    /** The constructor used only from within class Gate
     *  @param name used to initialize the final field
     *  @param delay used to initialize the final field
     */
    protected ConstGate( String name, float delay ) {
	super( name, delay );
	Simulator.schedule( delay, (float t) -> outputChangeEvent( t ) );
    }

    /** tell the gate that one of its input pins is in use
     *  @param pinName
     */
    public void registerInput( String pinName ) {
	Errors.warn( "Illegal input pin: " + name + " " + pinName );
    }

    /** tell the gate that one of its output pins is in use
     *  @param pinName
     */
    public void registerOutput( String pinName ) {
        if ("true".equals( pinName )) {
	    // Bug:  Do we do anything?
        } else if ("false".equals( pinName )) {
	    // Bug:  Do we do anything?
	} else {
	    Errors.warn( "Illegal output pin: " + name + " " + pinName );
	}
    }

    /** overides Gate's outputChangeEvent, the only change to output
     *  for the const gate will be when its true pin changes to true
     *  after its intial delay, so the time param will essentially always
     *  be the delay of the const gate in question
     */
    public void outputChangeEvent(float time) {
	System.out.println( time +  ": " + name + " true true" );
	for (Wire wire : outgoing) {
	    if ("true".equals( wire.getSrcPin() )) {
		Simulator.schedule( time + wire.getDelay(),
		    (float t) -> wire.inputChangeEvent( t )
		);
	    }
	}
    }
    public void inputChangeEvent(float time) {
    } // No inputs so this is not needed here

    /** check the sanity of this gate's connections
     */
    public void checkSanity() {
	// no sanity check; there are no input pins to check
    }

    /** reconstruct the textual description of this gate
     *  @return the textual description
     */
    public String toString() {
	return "gate " + name + " const " + delay;
    }
} // class ConstGate


/** Framework for discrete event simulation.
 */
class Simulator {

    public interface Action {
        // actions contain the specific code of each event
        void trigger( float time );
    }

    private static class Event {
        public float time; // the time of this event
        public Action act; // what to do at that time
    }

    private static PriorityQueue <Event> eventSet
        = new PriorityQueue <Event> (
            (Event e1, Event e2) -> Float.compare( e1.time, e2.time )
        );

    /** Call schedule to make act happen at time.
     *  Users typically pass the action as a lambda expression:
     *  <PRE>
     *  Simulator.schedule( t, ( float time ) -> method( ... time ... ) )
     *  </PRE>
     */
    static void schedule( float time, Action act ) {
        Event e = new Event();
        e.time = time;
        e.act = act;
        eventSet.add( e );
    }

    /** run the simulation.
     *  Call run() after scheduling some initial events to run the simulation.
     */
    static void run() {
        while (!eventSet.isEmpty()) {
            Event e = eventSet.remove();
            e.act.trigger( e.time );
        }
    }
} // class Simulator

public class Logic {

    // the sets of all wires and all gates
    private static LinkedList <Wire> wires
	= new LinkedList <Wire> ();
    private static LinkedList <Gate> gates
	= new LinkedList <Gate> ();

    /** Find a gate by textual name in the set gates
     *  @param s name of a gate
     *  @return the gate named s or null if none
     */
    public static Gate findGate( String s ) {
	// quick and dirty implementation
	for (Gate i: gates) {
	    if (i.name.equals( s )) {
		return i;
	    }
	}
	return null;
    }

    /** Initialize this logic circuit by scanning its description
     */
    private static void readCircuit( Scanner sc ) {
        while (sc.hasNext()) {
	    String command = sc.next();
	    if ("gate".equals( command )) {
		try {
		    gates.add( Gate.factory( sc ) );
		} catch (Gate.ConstructorFailure e) {
		    // do nothing, the constructor already reported the error
		}
	    } else if ("wire".equals( command )) {
		try {
		    wires.add( new Wire( sc ) );
		} catch (Wire.ConstructorFailure e) {
		    // do nothing, the constructor already reported the error
		}
	    } else if ("--".equals( command )) {
		sc.nextLine();
	    } else {
	        Errors.warn( "unknown command: " + command );
		sc.nextLine();
	    }
	}
    }

    /** Check that a circuit is properly constructed
     */
    private static void sanityCheck() {
	for (Gate i: gates) i.checkSanity();
	// Bug: Are there any sensible sanity checks on wires?
    }

    /** Print out the wire network to system.out
     */
    private static void printCircuit() {
	for (Gate i: gates) {
	    System.out.println( i.toString() );
	}
	for (Wire r: wires) {
	    System.out.println( r.toString() );
	}
    }

    /** Main program
     */
    public static void main( String[] args ) {
	if (args.length < 1) {
	    Errors.fatal( "Missing file name argument" );
	} else if (args.length > 1) {
	    Errors.fatal( "Too many arguments" );
	} else try {
	    readCircuit( new Scanner( new File( args[0] ) ) );
	    sanityCheck();
	    if (Errors.count() == 0) {
		Simulator.run();
	    }
        } catch (FileNotFoundException e) {
	    Errors.fatal( "Can't open the file" );
	}
    }
}
