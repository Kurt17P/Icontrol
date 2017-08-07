/*
 * We would appreciate your citation if the software is used: 
 * http://dx.doi.org/10.6028/jres.117.010 .
 *
 * This software can be redistributed and/or modified freely under the terms of
 * the GNU Public Licence and provided that any derivative works bear some
 * notice that they are derived from it, and any modified versions bear some
 * notice that they have been modified.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Public License for more details. http://www.fsf.org
 *
 * This software relies on other open source projects; please see the accompanying
 * _ReadMe_iC.txt for a list of included packages. Thank's very much to those
 * developers !! Without your effort, iC would not have been possible!
 *
 */
package icontrol.drivers.instruments.arduino;

import icontrol.AutoGUIAnnotation;
import static icontrol.Utilities.*;
import icontrol.drivers.Device;
import icontrol.iC_Annotation;
import icontrol.drivers.Device.CommPorts;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import javax.script.ScriptException;
import javax.swing.JOptionPane;

/**
 * This is a first implementation to support CmdMessenger on an Arduino.<p>
 *
 * Arduino "driver" class.<p>
 *
 * All device commands that the Arduino understands are
 * implemented here.<p>
 *
 * <h3>Instrument Commands:</h3>
 * <ul>
 *  <li>{@link #DemoCommand(float, String, boolean) }
 *  <li>{@link #DisplayPreviousResult() }
 *  <li>{@link #ReferenceImplementation(float, boolean, String)}
 *  <li>{@link #setTemp(float)}
 * </ul>
 *
 * @author KPP (Kurt Pernstich: pernstich@alumni.ethz.ch)
 * @version 0.1
 *
 */


// promise that this class supports RS232 communication
@iC_Annotation(CommPorts=CommPorts.RS232,
                InstrumentClassName="Arduino")
public class Arduino extends Device {


    ///////////////////
    // member variables

    /**
     * The Logger for this class. Note that the Logger Level is set in the
     * constructor of the <code>Device</code> class.
     */
    // change the Logger name to the name of your class
    private static final Logger m_Logger = Logger.getLogger("iC.Instruments.Arduino");


    


    /**
     * This is an demo implementation of a new device command to help understanding
     * how to write new Instrument Commands. Use the <code>ReferenceImplementation</code>
     * to implement new Instrument Commands.<p>
     *
     * This method shows a message to the user when in SyntaxCheck mode and also
     * when the 'real' action is performed.<p>
     *
     * This method implements the Syntax-Check mode.
     *
     * @param Param1 is shown to the user.
     * @param Param2 is shown to the user.
     * @param Param3 when <code>true</code>, the SyntaxCheck will succeed, otherwise
     *        it will fail.
     *
     * @throws ScriptException when conversion of the Instrument's answer to a
     * float fails.
     * @throws IOException re-thrown from <code>QueryInstrument</code>
     * @throws DataFormatException when the Syntax Check failed.
     *
     */
    // <editor-fold defaultstate="collapsed" desc="DemoCommand">
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Returns the Sample Temperature.</html>",
        ParameterNames = {},
        DefaultValues = {},
        ToolTips = {})
    @iC_Annotation(  MethodChecksSyntax = false )
    public float getTsample()
                    throws ScriptException, IOException, DataFormatException {

        

        /* in a real Instrument, the method would return when in Syntax-Check
         * Mode, but for this demo, we comment these lines */
        // return if in Syntax-check mode
        if (inSyntaxCheckMode())
            return 0.0f;

        /* in a real Instrument, the method would return when in No-Communication
         * Mode, but for this demo, we comment these lines.
         * Remark: Sometimes it is convenient to return a (unrealistic) number when
         * in No-Communication-Mode to debug the rest of the program. */
        // return if in No-Communication Mode
        if (inNoCommunicationMode())
            return 0.0f;


        //////////////////
        // query a command

        // build the GPIB command
        String cmd = String.format(Locale.US, "1;");
        
        

        // query the command
        String ans = QueryInstrument(cmd);
        
        // make a pattern and matcher
        Pattern p = Pattern.compile("(\\d+),(([+-]?\\d*\\.\\d+)(?![-+0-9\\.]));");
        Matcher m = p.matcher(ans);
        
        // check if a match is found
        if ( !m.matches() ) {
            String str = "Could not interpret the answer of the Arduino upon\n";
            str += "reading Tsample: " + ans+ "\n";

            // log event
            m_Logger.severe(str);

            //throw new ScriptException(str);
        }
        
        // get CommandID
        int CommandID = getInteger( m.group(1) );

        // get Tsample
        float Tsample = getFloat(m.group(2));

        // return Tsample
        return Tsample;


    }//</editor-fold>


    /**
     * This is a reference implementation of a typical Instrument command. Use this
     * as a template for your new methods that implement a script command.<p>
     *
     * This method performs a Syntax-Check.
     *
     * @param Param1 is a placeholder.
     * @param Param2 is a placeholder.
     * @param CommandLine is interpreted as a Command Line which is executed when not
     * in SyntaxCheck-Mode and the returned value is interpreted as a
     * <code>float</code> value.
     *
     * @throws ScriptException when conversion of the Instrument's answer to a
     * float fails during processing the <code>CommandLine</code>.
     * @throws IOException re-thrown from <code>QueryInstrument</code>
     * @throws DataFormatException when the Syntax Check failed.
     */
    // <editor-fold defaultstate="collapsed" desc="ReferenceImplementation">
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>A reference implementation of<br>a Instrument Command.</html>",
        ParameterNames = {"Value 1", "Value 2", "Value 3"},
        DefaultValues = {"1.1", "true", "\"Command-Line in double quotes\""},
        ToolTips = {"TT1", "TT2", "The command to execute"})
    @iC_Annotation(  MethodChecksSyntax = true )
    public void ReferenceImplementation(float   Param1,
                                        boolean Param2,
                                        String  CommandLine)
           throws ScriptException, IOException, DataFormatException {

        // local variables
        float dummy = 0;


        // remove double-quotes from the beginning and end of the CommandLine
        CommandLine = CommandLine.replaceFirst("^\"", "").replaceFirst("\"$", "");


        //////////////////////////////////////
        // Syntax-Check + Execute Command Line

        // Execute the Command Line:
        // in Syntax-Check mode the returned object is null
        // when not in Syntax-Check mode, the object should be valid
        if ( !CommandLine.isEmpty() ) {

            // make a new Device object to call it's DispatchCommand method
            // see Remark in javadoc (How to write new Instrument-Classes)
            Device dev = new Device();

            // perform the Syntax check
            Object obj = dev.DispatchCommand(CommandLine);

            // convert to float if returned object is valid
            if (obj != null) {

                try {
                    // convert the Instrument's answer to a float
                    dummy = getFloat(obj.toString());
                    
                } catch (ScriptException ex) {
                    // returned object is no convertible into a float, so throw an Exception
                    String str = "Executing the Command Line\n" + CommandLine + "\n"
                            + "did not return an object that can be converted into a float value.\n"
                            + "Please check the Command Line.\n";
                    throw new ScriptException(str);
                }
            }
            // the returned object is not valid, so throw an Exception when
            // not in Syntax-Check mode
            else if ( !inSyntaxCheckMode() ) {

                // returned object is null, so throw an Exception
                String str = "Executing the Command Line\n" + CommandLine + "\n";
                str += "did not return a valid object. Please check the Command Line.\n";

                throw new ScriptException(str);
            }
        }


        // perform Syntax-Checks here
        if (inSyntaxCheckMode()) {
            
            // these commands are only executed during Syntax-Check mode
            
            /* if (...) {
                String str = "Descriptive Text.\n";

                throw new DataFormatException(str);
            }*/
            
            // Syntax check for a String value defined in a List
            // consider if it makes sense to define the list in iC.properties
            // see LakeshoreTC.checkInputChannel for an example
            /*final List<String> NEW_LIST =
                    Arrays.asList("A", "B", "C");   // the entries need to be in uppercase letters!
            if ( !NEW_LIST.contains(YourVariableToCheck.toUpperCase()) ) {
                String str = "YourVariableToCheck '" + YourVariableToCheck + "' is not valid.";
                str += "Please select a value from:\n " + NEW_LIST.toString() + ".\n";
                throw new DataFormatException(str);
            }*/

            // Range check for a Float value defined in a List
            // consider if it makes sense to define the list in iC.properties
            /* final List<Float> YOUR_RANGE = Arrays.asList(-100f, 100f);

            if (YourVariableToCheck < YOUR_RANGE.get(0) || YourVariableToCheck > YOUR_RANGE.get(1)) {
                String str = "The zyx value is out of range.\n"
                        + "Please select a value between " + YOUR_RANGE.toString() + ".\n";
                throw new DataFormatException(str);
            }*/
            
            // return if in Syntax-Check mode
            return;
        }
        

        
        // exit if in No-Communication-Mode
        if (inNoCommunicationMode())
            return;



        // insert code to perform the desired task
        // for instance open files, calculate intermediate values, etc.



        //////////////////
        // query a command

        // build the GPIB command
        String cmd = String.format(Locale.US, "COMMAND 1, %.3f", Param1);

        // add the boolean value
        cmd += (Param2 ? "ON" : "OFF");



        // if new code requires to wait until a certain criteria is met, the
        // following while-loop can be used
        boolean Done = false;
        while ( !Done && !m_StopScripting) {

            // check for pause button
            isPaused(true);


            // query the command
            String ans = QueryInstrument(cmd);


            /* Convert to float
             * Using the predefined conversion routine is useful, because
             * it throws a ScriptException when the conversion fails.
             */
            dummy = getFloat(ans);


            // do something useful with dummy


            // check if the Instrument's answer is what we want it to be
            // to exit the method
            if (dummy == Param1) {
                Done = true;
            }
        }


        // if new code requires to start a new task, the following anonymous
        // inner class can be used. Ensure that the task terminates itself
        // upon the Stop signal.
        // See iC_Instrument.MonitorChart() for a working example displaying
        // data in a chart.

        /**
         * Implement a new class from an anonymous inner class that extends
         * Thread and implements run(), in which the actual job is done.<p>
         *
         * The thread pauses, respectively, stops when <code>m_Paused</code>, respectively,
         * <code>m_StopScripting</code> is true.
         */
        // <editor-fold defaultstate="collapsed" desc="myThread as anonymous inner class">
        class myThread extends Thread {

            // member variables
            public String       m_dummy;

            /** Constructor  */
            public myThread(String dummy) {
                // assign the passed values
                m_dummy = dummy;
            }


            @Override
            public void run() {
                // local variables
                float dummy = 0.0f;

                // Display a status message
                m_GUI.DisplayStatusMessage("Starting the thread.\n");

                // do until the thread should be stopped
                while ( m_StopScripting == false) {

                    // check for pause button
                    isPaused(true);

                    // do something useful here


                    // wait the desired time
                    try { Thread.sleep( 500 ); } catch (InterruptedException ignore) {}
                }

                // clean up the thread


                // Display a status message
                m_GUI.DisplayStatusMessage("Thread stopped.\n");
            }
        }//</editor-fold>


        // make a new thread object
        myThread myT = new myThread("Love All, Serve All");

        // start the thread and return
        myT.start();
    }//</editor-fold>



    /**
     * This method demonstrates the use of the result of the succeeding script
     * command. If the result is of type float, it is displayed as Status Text
     * in the GUI.
     *
     * @throws ScriptException when the data type of the return value of the
     * previous script command was different from float.
     */
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Displays the result of the previous Script command<br>if it is of ype float.</html>",
        ParameterNames = {},
        DefaultValues = {},
        ToolTips = {})
    // <editor-fold defaultstate="collapsed" desc="DisplayPreviousResult">
    public void DisplayPreviousResult() 
           throws ScriptException {

        // build the string
        String str = "The result of the previous Script command was: ";

        // check if the type was float
        if (m_LastReturnValue instanceof Number) {
                str += m_LastReturnValue.toString() + "\n";
        } else {
            str = "The data type of the return value of the previous script command\n"
                    + "is not of type Number as expected.\n";
            throw new ScriptException(str);
        }


        // display the previous result as Status Text
        m_GUI.DisplayStatusMessage(str, false);
    }//</editor-fold>


    /**
     * Reference implementation of a minimal method body. This is the example
     * listed in the publication (http://dx.doi.org/10.6028/jres.117.010)
     *
     * @param SetPoint The new temperature
     *
     * @throws IOException re-thrown from <code>SendToInstrument</code> or
     * <code>QueryInstrument</code>.
     * @throws DataFormatException when the Syntax Check failed.
     */
    @AutoGUIAnnotation(
	DescriptionForUser = "Sets the SetPoint temperature.",
	ParameterNames = {"Temperature [K]"},
	DefaultValues= {"295"},
	ToolTips = {"Define tool-tips here"})
    @iC_Annotation(MethodChecksSyntax = true )
    public void setTemp(float SetPoint)
       throws IOException, DataFormatException {

	// perform Syntax-check
	if (SetPoint < 0 || SetPoint > 500)
		throw new DataFormatException("Set point out of range.");

	// exit if in Syntax-check mode
	if ( inSyntaxCheckMode() )
		return;

        // build the GPIB command string
	String str = "SETP 1," + SetPoint;

	// send to the Instrument
	SendToInstrument(str);

	// wait until setpoint is reached
        float T;
	do {
            // it is recommended to check if scripting has been paused
            isPaused(true);

            // get current temperature
            str = QueryInstrument("KRDG? A");

            // convert to a float value
            //T = getFloat(str);    // this is the recommended way of converting
            T = Float.parseFloat(str);
	} while ( Math.abs(T-SetPoint) > 0.1 &&
                  m_StopScripting == false);
    }
}
