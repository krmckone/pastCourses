//MP2.java - Reading in input and reconstructing it to standard output

/** @author Douglas W. Jones
 *  @author Kaleb Robert McKone
 *  @version MP2 2017-09-24
 *
 *  This program utilizes the Errors class written for the RoadNetwork
 *  program by Dr. Jones, provided with the Sep 14 lecture notes. It also 
 *  borrows a similar main class for driving the program.
 *
 *  This program will take in a input file and attempt to 
 *  reconstruct every line of input by printing them
 *  to standard output (System.out).
*/

import java.util.LinkedList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/** Error reporting package
 *  provides standard prefix and behavior for messages
 */
class Errors {

    /** Report nonfatal errors, output a message and return
     * @param message the message to output
     */
    public static void warn( String message ) {
	System.err.println( "Logic: " + message );
    }

    /** Report fatal errors, output a message and exit, never to return
     *  @param message the message to output
     */
    public static void fatal( String message ) {
	warn( message );
	System.exit( 1 );
    }
}


/** Class to represent a logic gate
 */
class Gate {
    String name;
    String type;
    float timeDelay;

    /** Gate constructor
     *  @param scanner the input scanner that should be properly
     *  incremented to provide the correct arguments in the correct 
     *  order 
     */
    Gate(String[] inputLine) {
	if (legalArgs( inputLine )) {
            this.name = inputLine[1];
            this.type = inputLine[2];
            this.timeDelay = Float.valueOf( inputLine[3] );
	}
    }
    //Bug: prevent the construction of illegal gates
    //Throw exception if argument lengths is wrong
   

    /** Finds whether or not the arguments to the gate are legal
     */
    private static boolean legalArgs(String[] args) {
	if (args.length != 4) {
	       throw new IllegalArgumentException( "Error: line "
                                                   + Logic.getLineNumber()
                                                   + ". Improper number"
                                                   + " of arguments provided"
                                                   + ". Missing "
                                                   + (4 - args.length)
                                                   + " arguments" );
        }
	
        if (Logic.findGate( args[1] ) != null) {
            throw new IllegalArgumentException( "Error: line "
                                                + Logic.getLineNumber()
                                                + ". Gate "
                                                + args[1]
                                                + " already exists." );
	}
        if (!args[1].matches( "[a-zA-Z0-9]+" )) {
	    throw new IllegalArgumentException( "Error: line " 
						+ Logic.getLineNumber() 
						+ ". Gate names can"
						+ " have letters or numbers" );
                
	}
	if (!args[2].matches( "[a-zA-Z]+" )){
	    throw new IllegalArgumentException( "Error: line "
						+ Logic.getLineNumber()
						+ ". Gate types can be"
						+ "textual words only." );
	}
	if (!args[3].matches( "[+]?[0-9]*\\.?[0-9]+" )) {	
	    throw new IllegalArgumentException( "Erorr: line "
						+ Logic.getLineNumber()
						+ ". Provide a floating"
						+ " point number as a"
						+ " delay value."
				                + " Given: "
				                + args[3] );
            
	}
	return true;
    }

    /** Resturns the string representation of the gate
     *  in the same fashion that it was read in from input
     */
    public String toString(){ 
            return  "gate " 
		    + name + " "
		    + type + " "
		    + timeDelay;
    }
}

/** Class to represent a wire
 */

class Wire {
    String srcGateName;
    String srcPinName;
    String destGateName;
    String destPinName;
    float timeDelay;
     /** Wire constructor
     *  @param scanner the input scanner that should be properly 
     *  incremented to provide the correct arguments in the correct 
     *  order
     */
    Wire(String[] inputLine) { 
	if (legalArgs( inputLine )){
	    this.srcGateName = inputLine[1];
	    this.srcPinName = inputLine[2];
	    this.destGateName = inputLine[3];
	    this.destPinName = inputLine[4];
	    this.timeDelay = Float.valueOf( inputLine[5] );
	}
    }
    //Bug: Use the proper arguments to construct
    //Bug: Prevent the construction of illegal wires
    
    /** Finds whether or not the arguments to a wire are legal
     */
    private static boolean legalArgs(String[] args) {
	if (args.length != 6) {
            throw new IllegalArgumentException( "Error: line "
                                                + Logic.getLineNumber()
                                                + ". Improper number"
                                                + " of arguments provided"
                                                + ". Missing "
                                                + (6 - args.length)
                                                + " arguments" );
        }
	if (Logic.findGate( args[1] ) == null
                    || Logic.findGate( args[3] ) == null) {
	    throw new IllegalArgumentException ( "Error: line  "
                                                 + Logic.getLineNumber() + ": "
                                                 + "unable to make a"
						 + " wire between gates "
                                                 + args[1] + " and "
                                                 + args[3]
                                                 + " because either one or both"
                                                 + " don't exist yet" );
         }

        if (!args[1].matches( "[a-zA-Z0-9]+" )) {
            throw new IllegalArgumentException( "Error: line "
                                                + Logic.getLineNumber()
                                                + ". Give a gate"
			      			+ " source name, which may"
			                        + " only contain numbers"
			                        + " or letters" );
	}
	if (!args[2].matches( "[a-zA-Z]+" )){
            throw new IllegalArgumentException( "Error: line  "
                                                + Logic.getLineNumber()
                                                + ". Give a source pin"
						+ " name, which may"
						+ " contain only numbers"
						+ " or letters" );
	}
        if (!args[3].matches( "[a-zA-Z0-9]+" )){
            throw new IllegalArgumentException( " Error: line "
                                                + Logic.getLineNumber()
						+ ". Give a destination"
					        + " gate name, which may"
						+ " only contain numbers"
						+ " or letters" ) ;
	}
        if (!args[4].matches( "[a-zA-Z0-9]+" )) {
	    throw new IllegalArgumentException( "Error: line "
                                                + Logic.getLineNumber()
                                                + ". Give a destination" 
						+ " pin name, which may"
						+ " contain only numbers"
						+ " or letters" );
	}
	if (!args[5].matches( "[+]?[0-9]*\\.?[0-9]+" )) {	    
	    throw new IllegalArgumentException( "Error: line "
                                                + Logic.getLineNumber()
                                                + ". Give a delay number,"
			                        + " which may only be a"
			                        + " floating point value" );
			                             
	}
	return true;
    }

    /** Returns the string representation of a wire in the same fashion as 
     * it was read in from input
     */
    public String toString(){
	return "wire " 					                                       + srcGateName + " "
	       + srcPinName + " "
	       + destGateName + " "
	       + destPinName + " "
	       + timeDelay;
    }
}

public class Logic {
    private static LinkedList<Gate> gateList
        = new LinkedList<Gate>();                // List to hold all gates
    private static LinkedList<Wire> wireList
        = new LinkedList<Wire>();                // List to hold all wires

    private static int lineNumber;
    private static boolean foundError = false; 
    

    /** Public method to give the current line number in the
     *  input file
     *  @return lineNumber
     */
    public static int getLineNumber() {
        return Logic.lineNumber;
    }

    /** Utilizes the scanner and builds the logic system
     */
    private static void buildLogic( Scanner scanner ) {
	while (scanner.hasNextLine()) {
            String[] currentLine = scanner.nextLine().split(" ");
	    lineNumber++;

	    if ("gate".equals( currentLine[0] )) {	
	        try {
	            gateList.add( new Gate( currentLine ) );    
                }
		catch (IllegalArgumentException e) {
	            Errors.warn( e.getMessage() ); 
		    foundError = true;
		}
	    }
	
	    else if ("wire".equals( currentLine[0] )) {
		try {
		   wireList.add( new Wire( currentLine ) );
                }
		catch (IllegalArgumentException e) {
		    Errors.warn( e.getMessage() );
		    foundError = true;
		}
	    }
	    else if ("--".equals( currentLine[0] ) 
		     || "".equals( String.join( "", currentLine ) )) {
		continue; // ignore comments
	    }
	    else {
	        Errors.warn( "Input file error line " 
			      + getLineNumber() + ": "
			      + "Unkown command " 
			      + currentLine[0] 
			      + ". wire or gate expected at start of line. " );
	        foundError = true;	
            }
        }
    }

    /** After a logic system has been constructed, prints it back out
    *   in the same nature as the input file
    */
    private static void printLogic() { 
	for (Wire wire : wireList) {
           System.out.println( wire.toString() );
        }
	for (Gate gate : gateList) {
	   System.out.println( gate.toString() );	
	}
    }
    
    /** Used to see if a gate has already been created of the same name
     * @param searchName the gate name to search for
     */
    public static Gate findGate(String searchName) {
	for (Gate gate: gateList) {
	    if (gate.name.equals( searchName)) {
	        return gate;
	    }
	}
	return null;
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
	    if (!foundError){   
	        printLogic();
	    }
	} 
	catch (FileNotFoundException e) {
            Errors.fatal( "Unable to open the file provided in argument" );
	}
    }
}
