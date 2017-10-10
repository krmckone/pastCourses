//Logic.java - Reading in input and reconstructing it to standard output

/** @author Douglas W. Jones
 *  @author Kaleb Robert McKone
 *  @version MP3 2017-10-09
 *
 *  This program utilizes the Errors class written for the RoadNetwork
 *  program by Dr. Jones, provided with the Sep 14 lecture notes.
 *  ScanSupport has also been provided by Dr. Jones from the Sep 26 notes.
 *  In addition, it borrows a similar main method for driving the program.
 *
 *  This program will take in a input file and attempt to
 *  reconstruct every line of input by printing them
 *  to standard output (System.out). If errors are found in the
 *  input file, the reconstruction will not be permitted and all
 *  errors will be printed to the user.
*/

import java.util.LinkedList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/** Error reporting package
 *  provides standard prefix and behavior for messages.
 *  @author Douglas W. Jones
 */
class Errors {

    public static boolean foundError = false;

    /** Report nonfatal errors, output a message and return
     * @param message the message to output
     */
    public static void warn( String message ) {
	System.err.println( "Logic: Error on input line: "
			    + Logic.getLine() + ": " +  message );
	foundError = true;
    }

    /** Report fatal errors, output a message and exit, never to return
     *  @param message the message to output
     */
    public static void fatal( String message ) {
	warn( message );
	System.exit( 1 );
    }
}


/** Support methods for scanning
 *  @author Douglas W. Jones
 *  @author Kaleb McKone
 *  @see Errors
 */
class ScanSupport {
    /* Patterns needed for scanning are provided here. To prevent
     * NoSuchElementExceptions, each pattern also matches any whitespace.
     * Each next*() method that follows handles the case where the pattern
     * matches whitespace, which means that pattern was not found on the line.
     */
    private static final Pattern name
	= Pattern.compile( "([a-zA-Z0-9_]*)|[ \t]*" );
    private static final Pattern gateType
	= Pattern.compile( "(and|or|not|const)|[ \t]*" );
    private static final Pattern whitespace
	= Pattern.compile( "[ \t]*" ); // no newlines
    private static final Pattern number
	= Pattern.compile( "([-]?[0-9]*[\\.?(0-9)*]?[0-9]*)|[ \t]*" );
    private static final Pattern pin
	= Pattern.compile( "((out[12]?)|(in[12]?))|[ \t]*" );

    /** Get next name without skipping to next line (unlike sc.next())
     *  @see Errors
     *  @param sc the scanner from which the next element is scanned
     *  @return the name, if there was one, or an empty string
     */
    public static String nextName( Scanner sc ) {
	sc.skip( whitespace );
	sc.skip( name );
	if (!"".equals( sc.match().group() )) {
	    return sc.match().group();
	}
	else {
	    Errors.warn( "Name expected." );
	    return "";
	}
    }

    /** Get next float without skipping to next line
     *  @see Errors
     *  @param sc the scanner from which the next element is scanned
     *  @return the float, if there is one, or a Float.NaN
     */
    public static float nextFloat( Scanner sc) {
	sc.skip( whitespace );
	sc.skip( number );
	if (!"".equals( sc.match().group() )) {
	    if (sc.match().group().startsWith("-")) {
		Errors.warn( "Negative float not allowed." );
	    }
	    return Float.parseFloat( sc.match().group() );
	}
	else {
	    Errors.warn( "Float expected." );
	    return Float.NaN;
	}
    }

    /** Get next pin without skipping to next line
     *  @see Errors
     *  @param sc the scanner from the next element is scanned
     *  @return the pin, if there is one, or an empty string
     */
    public static String nextPin( Scanner sc) {
	sc.skip( whitespace );
	sc.skip( pin );
	if(!"".equals( sc.match().group() )) {
	    return sc.match().group();
	}
	else {
	    Errors.warn( "Pin name expected" );
	    return "";
        }
    }
    /** Get next gate type without skipping to next line
     *  @see Errors
     *  @param sc the scanner from which the next element is scanned
     *  @return the gate type, if there is one, or an empty string
     */
    public static String nextType(Scanner sc) {
	sc.skip( whitespace );
	sc.skip( gateType );
	if (!"".equals( sc.match().group() )){
            return sc.match().group();
	}
	else {
	    Errors.warn( "Gate type expected." );
	    return "";
        }
    }
    /** Advance to next line and complain if is junk at the line end
     *  @see Errors
     *  @param sc the scanner from which end of line is scanned
     *  @param message gives a prefix to give context to error messages
     *  This version supports comments starting with --
     */
    public static void lineEnd( Scanner sc, String message ) {
        sc.skip( whitespace );
        String lineEnd = sc.nextLine();
        if ( (!lineEnd.equals( "" ))
        &&   (!lineEnd.startsWith( "--" )) ) {
            Errors.warn(
                message +
                " Unsuported element(s): '" + lineEnd + "'"
            );
        }
    }
}


/** Class to represent a logic gate
 */
class Gate {
    private final String name;
    private final String type;
    private final float timeDelay;
    private final LinkedList<Pin> outPins;
    private final LinkedList<Pin> inPins;

    /** Exception for use within the gate constructors
     */
    class GateFailureException extends Exception {
	String message;

	GateFailureException(String message) {
	    this.message = message;
	}
	public String getMessage() {
	    return this.message;
	}// Bug: Should this be one exception that both Wire and
	 // Gate use?
    }

    /** Inner class used to represent an input/output pin
     *  to a gate
     */
    class Pin {
	private final  String name;
	private boolean inUse;

	Pin(String name) {
	    this.name = name;
	    inUse = false; // wire is not connected by default
	}
	public void setUse() {
	     this.inUse = true;
        }
	public boolean getUse() {
	    return this.inUse;
	}
	public String getName() {
	    return this.name;
	}
    }

    /** Gate constructor, throws exception if error found in
     *  relation to the rest of the logic system (duplicate gates)
     *  @param scanner scanner from the input file.
     */
    Gate(Scanner sc) throws GateFailureException {
        this.name = ScanSupport.nextName( sc );
        this.type = ScanSupport.nextType( sc );
        this.timeDelay = ScanSupport.nextFloat( sc );
	this.outPins = new LinkedList<Pin>();
	this.inPins = new LinkedList<Pin>();

        if (Logic.findGate( this.getName() ) != null) {
	    throw new GateFailureException( "Gate " + this.getName()
			                    + " already created." );
	}
	// Gate types can only have specific in pins and out pins
	if ("and".equals( this.getType() ) || "or".equals( this.getType() )) {
            inPins.add( new Pin( "in1" ) );
	    inPins.add( new Pin( "in2" ) );
	    outPins.add( new Pin( "out" ) );
	}
	else if ("not".equals( this.getType() )) {
	    inPins.add( new Pin( "in" ) );
	    outPins.add( new Pin( "out" ) );
	}
	else if ("cost".equals( this.getType() )) {
	    outPins.add( new Pin( "true" ) );
	    outPins.add( new Pin( "false" ) );
	}
        ScanSupport.lineEnd( sc, "Gate Error," );
    }

    /** Method for determining if a gate has a particular *IN* pin
     *  available for connection
     *  @param searchName name of pin
     *  @return the pin if it was found, null otherwise
     */
    public Pin findInPin(String searchName) {
	for (Pin pin : this.inPins){
	    if (pin.getName().equals( searchName )) {
		return pin;
	    }
	}
	return null;
    }
    /** Method for determining if a gate has a particular *OUT* pin
     *  available for connection
     *  @param searchName name of pin
     *  @return the pin if it was found, null otherwise
     */
    public Pin findOutPin(String searchName) {
	for (Pin pin : this.outPins) {
            if (pin.getName().equals( searchName )) {
		return pin;
	    }
	}
	return null;
    }

    /** Getter methods for name/type/delay/pins
     */
    public String getName() {
	return this.name;
    }
    public String getType() {
	return this.type;
    }
    public float getDelay() {
	return this.timeDelay;
    }
    public LinkedList<Pin> getInPins() {
	return this.inPins;
    }

    /** Resturns the string representation of the gate
     *  in the same fashion that it was read in from input
     */
    public String toString() {
            return  "gate " + name + " " + type + " " + timeDelay;
    }
}

/** Class to represent a wire
 */

class Wire {
    private final String srcGateName;
    private final String srcPinName;
    private final String destGateName;
    private final String destPinName;
    private final float timeDelay;

    class WireFailureException extends Exception {
	private String message;

	WireFailureException(String message) {
	    this.message = message;
	}
	public String getMessage() {
	    return this.message;
	}// Bug: Should I make one ConstructorFailure exception for
	 // both wires and gates?
    }

    /** Wire constructor
    *   @param scanner the input scanner that should be properly
    *   incremented to provide the correct arguments in the correct
    *   order.
    */
    Wire(Scanner sc) throws WireFailureException {
	this.srcGateName = ScanSupport.nextName( sc );
	this.srcPinName = ScanSupport.nextPin( sc );
	this.destGateName = ScanSupport.nextName( sc );
	this.destPinName = ScanSupport.nextPin( sc );
        this.timeDelay = ScanSupport.nextFloat( sc );

	// Check to see if the proper gates have been created
        if (Logic.findGate( srcGateName ) == null) {
	    throw new WireFailureException( "Gate " + srcGateName
			                    + " has not been created." );
	}
	if (Logic.findGate( destGateName ) == null) {
	    throw new WireFailureException( "Gate " + destGateName
			                    + " has not been created.");
	}
	// We check if a gate actually has the output indicted in the
	// input file.
	Gate.Pin outPin
	    = Logic.findGate( srcGateName ).findOutPin( srcPinName );

	if (outPin == null) {
	    throw new WireFailureException( "Gate " + srcGateName
			                    + " does not have support"
					    + " for given source pin" );
	}
	// We don't have to check to see if an output in is already in use
	// because each output may be connected to zero or more inputs, with
	// each connection made by a distinct wire. For completeness, each
	// new wire will cause inUse to get set to true here, although it makes
	// no difference after the first wire.
	else {
            outPin.setUse();
	}
        // We do the same here for in pins as we did above for out pins.
	// We make sure that the indicated input pin is actually on the gate.
	Gate.Pin inPin
	    = Logic.findGate( destGateName ).findInPin( destPinName );

	if (inPin == null) {
	    throw  new WireFailureException ( "Gate " + destGateName
			                      + " does not have support"
					      + " for given destination pin" );
	}
	// Make sure the specific input pin is not aleady in use since
	// exactly one wire must connect to each input only.
	else if (inPin.getUse()) {
            throw new WireFailureException( "Gate " + destGateName
			                    + " input pin " + destPinName
					    + " already in use." );
	}
	// After this call, the input is in use and will only have that one
	// wire connect to it.
	else {
            inPin.setUse();
	}

        ScanSupport.lineEnd( sc, "Wire Error," );
    }

    /** Returns the string representation of a wire in the same fashion as
     *  it was read in from input
     */
    public String toString() {
	return "wire " + srcGateName + " " + srcPinName + " "
	       + destGateName + " " + destPinName + " " + timeDelay;
    }
}

/** Main class
 */
public class Logic {

    private static LinkedList<Gate> gateList
        = new LinkedList<Gate>();                // List to hold all gates
    private static LinkedList<Wire> wireList
        = new LinkedList<Wire>();                // List to hold all wires

    private static int lineNumber; // keeps track input file line

    /** Public method to give the current line number in the
     *  input file
     *  @return lineNumber current line number in file
     */
    public static int getLine() {
        return Logic.lineNumber;
    }

    /** Utilizes the scanner and builds the logic system
     */
    private static void buildLogic( Scanner scanner ) {
	while (scanner.hasNext()) {
	    String keyword = scanner.next();
	    lineNumber++;
	    if ("gate".equals( keyword )) {
		try {
		    gateList.add( new Gate( scanner ) );
		}
		catch ( Gate.GateFailureException e ) {
		    Errors.warn( e.getMessage() );
		}
	    }
	    else if ("wire".equals( keyword )) {
		try {
		    wireList.add( new Wire( scanner ) );
		}
		catch ( Wire.WireFailureException e ) {
		    Errors.warn( e.getMessage() );
		}
	    }
	    else if ("--".equals( keyword )) {
		scanner.nextLine();
	    }
	    else {
		Errors.warn( "Unknown command: " + keyword );
		scanner.nextLine();
            }
	}
	checkInputs();
    }

    /** Checks to make sure each input pin has at been given
     *  a wire since each input needs to be given one for the logic
     *  system to be legal
     */
    private static void checkInputs() {
	for (Gate gate : gateList) {
	    for(Gate.Pin pin : gate.getInPins()) {
		if(!pin.getUse()) {
		    Errors.warn( "Gate " + gate.getName()
				 + " needs wire to " + pin.getName() );
		}
	    }
        }
    }

    /** After a logic system has been constructed, prints it back out
    *   in the same nature as the input file
    */
    private static void printLogic() {
	for (Gate gate : gateList) {
	   System.out.println( gate.toString() );
	}
	for (Wire wire: wireList) {
	    System.out.println( wire.toString() );
	}
    }

    /** Used to see if a gate has already been created of the same name
     *  @param searchName the gate name to search for
     */
    public static Gate findGate(String searchName) {
	for (Gate gate: gateList) {
	    if (gate.getName().equals( searchName)) {
	        return gate;
	    }
	}
	return null;
    }

    /** Gives the current list of wires in the logic system
     *  @return current list of wires
     */
    public static LinkedList<Wire> getWires() {
	return wireList;
    }

    public static void main(String[] args) {
	if (args.length < 1) {
            Errors.fatal( "No file argument provided" );
	}
	else if (args.length > 1) {
            Errors.fatal( "Too many arguments provided" );
	}
	else try {
            buildLogic( new Scanner( new File( args[0] ) ) );
	    if (!Errors.foundError){
	        printLogic();
	    }
	}
	catch (FileNotFoundException e) {
            Errors.fatal( "Unable to open the file provided in argument" );
	}
    }
}
