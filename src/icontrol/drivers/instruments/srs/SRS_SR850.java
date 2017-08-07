/*
 * This software was developed at the National Institute of Standards and
 * Technology by a guest researcher in the course of his official duties and
 * with the partial support of the Swiss National Science Foundation. Pursuant
 * to title 17 Section 105 of the United States Code this software is not
 * subject to copyright protection and is in the public domain. The
 * Instrument-Control (iC) software is an experimental system. Neither NIST, nor
 * the Swiss National Science Foundation nor any of the authors assumes any
 * responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or any
 * other characteristic. We would appreciate your citation if the software
 * is used: http://dx.doi.org/10.6028/jres.117.010 .
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
package icontrol.drivers.instruments.srs;

import static icontrol.Utilities.getFloat;
import icontrol.AutoGUIAnnotation;
import icontrol.Utilities;
import icontrol.drivers.Device;
import icontrol.iC_Annotation;
import icontrol.drivers.Device.CommPorts;
import icontrol.iC_ChartXY;
import icontrol.iC_ChartXY.SeriesIdentification;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import javax.script.ScriptException;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.jfree.chart.axis.StandardTickUnitSource;

/**
 * This class implements functionality to communicate with a 
 * Stanford Research Systems SR850 Lock-in.
 *
 * <h3>Instrument Commands:</h3>
 * <ul>
 *  <li>{@link #getTimeConstant() } (not shown in the GUI)
 *  <li>{@link #getSensitivity() } (not shown in the GUI)
 *  <li>{@link #setAmplitude(double) }
 *  <li>{@link #setFrequency(double) }
 *  <li>{@link #setPhase(double) }
 *  <li>{@link #setReferenceSource(int) }
 *  <li>{@link #setSensitivity(int) }
 *  <li>{@link #setTimeConstant(int) }
 *  <li>{@link #GainCalibration(float, double, double, double, boolean, int, String) }
 *  <li>{@link #SweepF(double, double, double, boolean, int, String) }
 *  <li> Note that more commands might be defined as generic GPIB commands
 * </ul><p>
 * 
 * @author egbittle (Emily Bittle: egbittle@gmail.com)
 * @author KPP (Kurt Pernstich: pernstich@alumni.ethz.ch)
 * @version 0.1
 */
// promise that this class supports GPIB communication
@iC_Annotation(CommPorts = CommPorts.GPIB,
               InstrumentClassName = "SRS SR850")
public class SRS_SR850 extends Device {

    ///////////////////
    // member variables
    
    /** The Logger for this class. Note that the Logger Level is set in the
     * constructor of the <code>Device</code> class. */
    private static final Logger m_Logger = Logger.getLogger("iC.Instruments.SRS_SR850");
    
    /** Used in JUnit Testing to generate random frequency values that are
     * strictly increasing */
    private static float JUnitFrequency = 1;
        
    
    /** Define a new data type to store a measurement of the LIA. */
    // <editor-fold defaultstate="collapsed" desc="MValue">
    private class MValue {

        float R = Float.NaN;
        float Phi = Float.NaN;
        float F = Float.NaN;

    }//</editor-fold>

    
    /**
     * Extra initialization of the SR850 after establishing the IO Connection 
     * via GPIB. It invokes the base class' <code>Open</code>, and ...<p>
     *
     * The description of the parameters and exceptions was copied from the
     * implementation in the super class.
     * See {@link icontrol.drivers.Device#OpenGPIB(int) } for details.<p>
     *
     * @throws IOException when an sending / receiving data caused a GPIB error
     * @throws ScriptException when the response to a *IDN? query does not
     * match the expected result; bubbles up from <code>checkIDN</code>.
     */
    // <editor-fold defaultstate="collapsed" desc="Open">
    @Override
    public void Open()
            throws IOException, ScriptException {

        // invoke the Open method of the base class
        // TODO @Emily: Not sure calling the superclass will work here. We need to 
        // test, but to do so, I would need the String returned by the SR850
        // to a *IDN? command. Could you please use NI-Max to obtain this String
        super.Open();

 
        // direct responses of SR850 to GPIB
        SendToInstrument("OUTX 1");

        
    }//</editor-fold>
    
    /**
     * Sets the frequency reference source.<p>
     *
     * This method performs a Syntax-Check.
     *
     * @param Fmodvalue The FMOD command sets or queries the reference source. The parameter i selects internal (i=0), internal sweep (i=1) or external (i=2).
     *
     * @throws IOException re-thrown from <code>SendToInstrument</code>
     * @throws DataFormatException when the Syntax Check failed.
     */
    // <editor-fold defaultstate="collapsed" desc="setReferenceSource">
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Sets the frequency reference source.</html>",
        ParameterNames = {"Frequency reference: { [0, 2] }"},
        DefaultValues = {"0"},
        ToolTips = {"<html>0 ... internal<br>1 ... internal sweep<br>2 ... external</html>"})
    @iC_Annotation(  MethodChecksSyntax = true )
    public void setReferenceSource(int Fmodvalue)
           throws IOException, DataFormatException {


        ///////////////
        // Syntax-Check

        // Syntax check
        if ( Fmodvalue < 0 || Fmodvalue > 2 ) {
            String str = "The value specified in setReferenceSource must be 0, 1, or 2.\n";

            throw new DataFormatException(str);
        }

        // return if in Syntax-check mode
        if (inSyntaxCheckMode())
            return;

        // exit if in No-Communication-Mode
        if (inNoCommunicationMode())
            return;


        // build the string
        String dummy = String.format(Locale.US, "FMOD %d", Fmodvalue);

        // send to device
        SendToInstrument(dummy);
        
    }//</editor-fold>
    
    
    /**
     * Sets the amplitude of the internal reference source.<p>
     *
     * This method performs a Syntax-Check.
     *
     * @param Amplitude is the amplitude of the sine wave output
     *
     * @throws IOException re-thrown from <code>SendToInstrument</code>
     * @throws DataFormatException when the Syntax Check failed.
     */
    // <editor-fold defaultstate="collapsed" desc="setAmplitude">
    @AutoGUIAnnotation(
        DescriptionForUser = "Sets the amplitude of the sine wave output.",
        ParameterNames = {"Frequency reference: { [0.004, 5.000] }"},
        DefaultValues = {"0.01"},
        ToolTips = {""})
    @iC_Annotation(  MethodChecksSyntax = true )
    public void setAmplitude(double Amplitude)
           throws IOException, DataFormatException {


        ///////////////
        // Syntax-Check

        // Syntax check
        if ( Amplitude < 0.004 || Amplitude > 5.000 ) {
            String str = "The value of sine amplitude must be an between 0.004 V and 5 V.\n";

            throw new DataFormatException(str);
        }

        // return if in Syntax-check mode
        if (inSyntaxCheckMode())
            return;

        // exit if in No-Communication-Mode
        if (inNoCommunicationMode())
            return;


        // build the string
        String dummy = String.format(Locale.US, "SLVL %.3f", Amplitude);

        // send to device
        SendToInstrument(dummy);
        
    }//</editor-fold>
    
    
    /**
     * Sets the reference frequency.<p>
     *
     * This method performs a Syntax-Check.
     *
     * @param Frequency Sets the reference frequency.
     *
     * @throws IOException re-thrown from <code>SendToInstrument</code>
     * @throws DataFormatException when the Syntax Check failed.
     */
    // <editor-fold defaultstate="collapsed" desc="setFrequency">
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Sets the reference frequency when internal source is selected.</html>",
        ParameterNames = {"Frequency [Hz] { [.001, 102e3] }"},
        DefaultValues = {"1"},
        ToolTips = {""})
    @iC_Annotation(  MethodChecksSyntax = true )
    public void setFrequency(double Frequency)
           throws IOException, DataFormatException {


        ///////////////
        // Syntax-Check

        // Syntax check for max. frequency
        if ( Frequency < .001 || Frequency > 102e3 ) {
            String str = "The Reference Frequency must be between 1mHz and  102kHz.\n";

            throw new DataFormatException(str);
        }

        // return if in Syntax-check mode
        if (inSyntaxCheckMode())
            return;

        // exit if in No-Communication-Mode
        if (inNoCommunicationMode())
            return;


        // build the string
        String dummy = String.format(Locale.US, "FREQ %f", Frequency);

        // send to device
        SendToInstrument(dummy);
        
    }//</editor-fold>
    
    
    /**
     * Sets the phase.<p>
     *
     * This method performs a Syntax-Check.
     *
     * @param Phase Sets the reference phase.
     *
     * @throws IOException re-thrown from <code>SendToInstrument</code>
     * @throws DataFormatException when the Syntax Check failed.
     */
    // <editor-fold defaultstate="collapsed" desc="setPhase">
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Sets the reference phase.</html>",
        ParameterNames = {"Phase [degrees] { [-360.0, 719.999] }"},
        DefaultValues = {"1"},
        ToolTips = {""})
    @iC_Annotation(  MethodChecksSyntax = true )
    public void setPhase(double Phase)
           throws IOException, DataFormatException {


        ///////////////
        // Syntax-Check

        // Syntax check 
        if ( Phase < -360.000 || Phase > 719.999 ) {
            String str = "The Phase is defined between -360 and 799.99.\n";

            throw new DataFormatException(str);
        }

        // return if in Syntax-check mode
        if (inSyntaxCheckMode())
            return;

        // exit if in No-Communication-Mode
        if (inNoCommunicationMode())
            return;


        // build the string
        String dummy = String.format(Locale.US, "PHAS %f", Phase);

        // send to device
        SendToInstrument(dummy);
        
    }//</editor-fold>
    

    /**
     * Sets the sensitivity.<p>
     *
     * This method performs a Syntax-Check.
     *
     * @param Sensitivity Sets the LI sensitivity.
     *
     * @throws IOException re-thrown from <code>SendToInstrument</code>
     * @throws DataFormatException when the Syntax Check failed.
     */
    // <editor-fold defaultstate="collapsed" desc="setSensitivity">
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Sets the lock-in sensitivity.</html>",
        ParameterNames = {"Sensitivity { [0, 26] }"},
        DefaultValues = {"12"},
        ToolTips = {"<html>0 ... 2 nV/fA, 1 ... 5 nV/fA<br>2 ... 10 nV/fA, 3 ... 20 nV/fA<br>"
            + "4 ... 50 nV/fA, 5 ... 100 nV/fA<br>6 ... 200 nV/fA, 7 ... 500 nV/fA<br>"
            + "8 ... 1 μV/pA, 9 ... 2 μV/pA<br>10 ... 5 μV/pA, 11 ... 10 μV/pA<br>"
            + "12 ... 20 μV/pA, 13 ... 50 μV/pA<br>14 ... 100 μV/pA, 15 ... 200 μV/pA<br>"
            + "16 ... 500 μV/pA, 17 ... 1 mV/nA<br>18 ... 2 mV/nA, 19 ... 5 mV/nA<br>"
            + "20 ...10 mV/nA, 21 ...20 mV/nA<br>22 ... 50 mV/nA, 23 ... 100 mV/nA<br>"
            + "24 ... 200 mV/nA, 25 ... 500 mV/nA<br>26 ... 1 V/μA</html>"})
    @iC_Annotation(  MethodChecksSyntax = true )
    public void setSensitivity(int Sensitivity)
           throws IOException, DataFormatException {


        ///////////////
        // Syntax-Check

        // Syntax check 
        if ( Sensitivity < 0 || Sensitivity > 26 ) {
            String str = "The Sensitivity integer code must be between 0 and  26.\n"
                    + "Please see SRS850 manual of the tool tips for the codes.\n";

            throw new DataFormatException(str);
        }

        // return if in Syntax-check mode
        if (inSyntaxCheckMode())
            return;

        // exit if in No-Communication-Mode
        if (inNoCommunicationMode())
            return;


        // build the string
        String dummy = String.format(Locale.US, "SENS %d", Sensitivity);

        // send to device
        SendToInstrument(dummy);
        
    }//</editor-fold>
    
    
    /**
     * Sets the Time Constant.<p>
     *
     * This method performs a Syntax-Check.
     *
     * @param TimeConstant Sets the LI TimeConstant.
     *
     * @throws IOException re-thrown from <code>SendToInstrument</code>
     * @throws DataFormatException when the Syntax Check failed.
     */
    // <editor-fold defaultstate="collapsed" desc="setTimeConstant">
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Sets the lock-in TimeConstant.</html>",
        ParameterNames = {"TimeConstant { [0, 19] }"},
        DefaultValues = {"10"},
        ToolTips = {"<html>0 ... 10 μs, 10 ... 1 s<br>1 ... 30 μs, 11 ... 3 s<br>"
                + "2 ... 100 μs, 12 ... 10 s<br>3 ... 300 μs, 13 ... 30 s<br>"
            + "4 ... 1 ms, 14 ... 100 s<br>5 ... 3 ms, 15 ... 300 s<br>6 ... 10 ms, 16 ... 1 ks<br>"
            + "7 ... 30 ms, 17 ... 3 ks<br>8 ... 100 ms, 18 ... 10 ks<br>"
            + "9 ... 300 ms, 19 ... 30 ks</html>"})
    @iC_Annotation(  MethodChecksSyntax = true )
    public void setTimeConstant(int TimeConstant)
           throws IOException, DataFormatException {


        ///////////////
        // Syntax-Check

        // Syntax check
        if ( TimeConstant < 0 || TimeConstant > 19 ) {
            String str = "The TimeConstant integer code must be between 0 and  19.\n"
                    + "Please see SRS850 manual or the tool tips for the codes.\n";

            throw new DataFormatException(str);
        }

        // return if in Syntax-check mode
        if (inSyntaxCheckMode())
            return;

        // exit if in No-Communication-Mode
        if (inNoCommunicationMode())
            return;


        // build the string
        String dummy = String.format(Locale.US, "OFLT %d", TimeConstant);

        // send to device
        SendToInstrument(dummy);
        
    }//</editor-fold>
    
    
    /**
     * Gets the Time Constant.<p>
     *
     * @return The Time Constant in seconds.
     * 
     * @throws IOException re-thrown from <code>QueryFromInstrument</code>
     * @throws ScriptException When the returned answer cannot be converted to an
     * integer of an invalid integer was received.
     */
    // <editor-fold defaultstate="collapsed" desc="setTimeConstant">
    /* At present, it seems unnecessary to announce this method in the GUI.
     * It is public, so it could be called if one knew it existed.
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Gets the lock-in TimeConstant.</html>",
        ParameterNames = {},
        DefaultValues = {},
        ToolTips = {})
    @iC_Annotation(  MethodChecksSyntax = false )
     * */
    public double getTimeConstant()
           throws IOException, ScriptException {


        // exit if in No-Communication-Mode
        if (inNoCommunicationMode())
            return -1;


        // build the string
        String dummy = String.format(Locale.US, "OFLT?");

        // query the LIA
        String ans = QueryInstrument(dummy);
        
        // convert to an integer
        int TCint;
        try {
            
            TCint = Utilities.getInteger(ans);
            
        } catch (ScriptException ex) {
            String str = "The received Time Constant is invalid:\n";
            str += ex.getMessage();
            
            throw new ScriptException(str);
        }
        
        
        // convert the int to a real time
        double TC;
        switch (TCint) {
            case 0: TC = 10e-6; break;
            case 1: TC = 30e-6; break;
            case 2: TC = 100e-6; break;
            case 3: TC = 300e-6; break;
            case 4: TC = 1e-3; break;
            case 5: TC = 3e-3; break;
            case 6: TC = 10e-3; break;
            case 7: TC = 30e-3; break;
            case 8: TC = 100e-3; break;
            case 9: TC = 300e-3; break;
            case 10: TC = 1; break;
            case 11: TC = 3; break;
            case 12: TC = 10; break;
            case 13: TC = 30; break;
            case 14: TC = 100; break;
            case 15: TC = 300; break;
            case 16: TC = 1000; break;
            case 17: TC = 3000; break;
            case 18: TC = 10000; break;
            case 19: TC = 30000; break;
            default:
                String str = "The received Time Constant (" + TCint + ") is invalid.\n"
                        + "It should be between 0 and 19.\n";
            
                throw new ScriptException(str);
        }
        
        // return the Time Constant
        return TC;

        
    }//</editor-fold>
    
    
    /**
     * Reads a measurement from the Instrument. The method returns
     * with the measured R, Phi, and Frequency values or with NaN if the measurement was
     * invalid. 
     *
     * @throws IOException re-thrown from <code>SendToInstrument</code>
     * @throws ScriptException re-thrown from <code>Utilities.getFloat</code>
     * @return Returns an <code>MValue</code> object with the measured values or
     * with NaN if the measurement was invalid. During JUnit Testing, random
     * numbers are returned.
     */
    // <editor-fold defaultstate="collapsed" desc="Measure">
    private MValue Measure()
            throws IOException, ScriptException {

        // local variables
        MValue ret = new MValue();

        // return something useful while debugging
        if (inJUnitTest()) {
                       
            // just increase the previous frequency value
            JUnitFrequency *= 1.2;
            ret.F = JUnitFrequency;
            
            ret.R = -JUnitFrequency + 5*(float)Math.random();
            ret.Phi = 360 * (float)Math.random();
            
            
            //ret.F = 102e3f * (float)Math.random();
            //ret.R = -10.0f * (float)Math.random();
            //ret.Phi = 360 * (float)Math.random();
            
            
            
            return ret;
        }

        // exit if in No-Communication-Mode
        if (inNoCommunicationMode())
            return ret;


        // read R
        // TODO @Emilie: you have queried R (OUTP? 3) and in the comment you wrote X
        // not sure what you prefer. If you want X, and Y, please let me know
        // so I can rename the vairables (also in SweepF and MValue)
        String dummy = QueryInstrument("OUTP? 3");
        ret.R = getFloat(dummy);
            
        // read Phi
        dummy = QueryInstrument("OUTP? 4");
        ret.Phi = getFloat(dummy);
            
        // read frequency
        dummy = QueryInstrument("FREQ?");
        ret.F = getFloat(dummy);
            
        
        // return the answer
        return ret;
        
    }//</editor-fold>
    
    
    

    /**
     * Sweeps the frequency and takes a measurement at every frequency. The data
     * is shown in a chart and is also saved in a text file. The chart is shown 
     * only if a File Extension was specified, because if no extension was
     * specified, <code>SweepF</code> is most likey called by <code>GainCalibration</code>.
     * 
     * @param FreqStart Start Frequency
     * @param FreqStop Stop Frequency
     * @param FreqInc Increment in Frequency respectively number of points per 
     * decade. See <code>Utilities.GenerateValues</code>
     * @param FreqLog When <code>false</code> the frequency points are equally spaced;
     * if <code>true</code> they are logarithmically spaced. See <code>GenerateValues</code>
     * @param DelayFactor After setting the new frequency, a time delay is added before 
     * taking the measurement. This time delay is a multiple of the LIA's time
     * constant. <code>DelayFactor</code> specifies this multiplier. Must be greater
     * than 0.
     * @param FileExtension This String is appended to the File Name. If the
     * filename already exists, a new file with a counter is inserted between the 
     * FileName and the FileExtension. If FileExtension is empty, no data will
     * be saved.
     * @param CalibrationFile The file name for the gain/phase calibration; the
     * file can be generated with <code>GainCalibration</code>
     * @return Returns the measured data points to be used in the follwing script
     * command
     * 
     * @throws ScriptException When communication with the instrument failed, or
     * the Frequency values could not be generated, or a File IO error ocurred.
     * @throws IOException When communication with the Instrument failed (bubbled
     * up from <code>setReferenceSource</code>, <code>setFrequency</code>, or
     * <code>setReferenceSource</code>, or some more direct query commands.
     * @throws DataFormatException when the Syntax Check failed.
     */
    // <editor-fold defaultstate="collapsed" desc="SweepF">
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Measures the lock-in signal as a function of the internal reference frequency.<br>"
            + "Sets the Lock-in's Reference Signal to Internal.</html>",
        ParameterNames = {"Start Frequency [Hz] { [.001, 102e3] }", "Stop Frequency [Hz] { [.001, 102e3] }",
                          "Frequency Increment", "logarithmic Freq. spacing?", "Delay time multiplier", "File Extension",
                          "Calibration File"},
        DefaultValues = {"2000", "3000", "100", "false",
                           "250", ".fsweep.txt", ""},
        ToolTips = {"", "", "<html>Frequency difference or number of steps per decade<br>if logarithmic sweep is selected</html>", 
                    "", "<html>Specifies the multiplier of the current Time Constant set in the LIA to wait after setting<br>"
                + "the new frequency and starting the measruement.<br>"
                + "A minimum of 1 sec is always included.</html>", "",
                "The Filename containing the gain/phase calibration as taken<br>"
                + "with the script command GainCalibration"})
    @iC_Annotation(  MethodChecksSyntax = true )
    public ArrayList<MValue> SweepF(double FreqStart, double FreqStop, double FreqInc, boolean FreqLog,
                                    int DelayFactor, String FileExtension, String CalibrationFile)
           throws ScriptException, IOException, DataFormatException {

        //////////////////
        // local variables

        // store all measurements here
        ArrayList<MValue> Results = new ArrayList<>();
        
        // stores the calibration
        ArrayList<MValue> Calibration;
        
        // store the calibrated measurements
        ArrayList<MValue> ResultsCalibrated = new ArrayList<>();
        
        // use Apache Common Math for Interpolation
        UnivariateInterpolator Interpolator;
        UnivariateFunction InterpolationR = null;
        UnivariateFunction InterpolationPhi = null;

        // holds the chart
        iC_ChartXY  Chart = null;
        
        // Identification (handle) of the Data Series
        SeriesIdentification SeriesID_R = null,
                             SeriesID_Phi = null;
        
        // make new Utilities object
        Utilities util = new Utilities();



        ///////////////
        // Syntax-Check

        // Syntax check for max. bias
        if ( inSyntaxCheckMode() ) {
            // call appropriate functions that perform the Syntax check
            setFrequency(FreqStart);
            setFrequency(FreqStop);
        }
        
        // Syntax check for Delay
        if (DelayFactor < 0) {
            String str = "The delay time multiplier must be >0.\n";

            throw new DataFormatException(str);
        }
   
        // generate Frequency values
        // if you make changes to this parameter list, also make the same changes
        // in GainCalibration
        ArrayList<Double> F = util.GenerateValues(FreqStart, FreqStop, FreqInc, FreqLog, null, 0);

        
        // load gain/phase calibration file
        // <editor-fold defaultstate="collapsed" desc="load file and do the interpolation">
        if (!CalibrationFile.isEmpty()) {
            
            // initialize storage
            Calibration = new ArrayList<>();
            
            // stores one calibration point
            MValue CalPoint = new MValue();
            
            // used fro parsing the file
            String Line;
            
            // open the file for reading
            BufferedReader fr;
            String CalFileName = "";
            try {
                CalFileName = m_GUI.getProjectPath() + CalibrationFile;
                fr = new BufferedReader(new FileReader(new File(CalFileName)));

            } catch (FileNotFoundException ex) {

                // check if file is found
                String str = "Error loading gain/phase calibration data. Could not find the file\n"
                        + CalFileName + "\n" + ex.getMessage() + "\n";

                // throw Exception
                throw new DataFormatException(str);
            }

            try {
                // compile the Regex pattern
                Pattern pattern = Pattern.compile("(.*)[,\\t](.*)[,\\t](.*)");
                // http://www.regular-expressions.info/floatingpoint.html
                //Pattern pattern = Pattern.compile("([-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?)[,\\t]([-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?)");

                // process all lines in the file
                while ( (Line = fr.readLine()) != null ) {

                    // remove all characters after a comment (% or //)
                    // original Regex was ^\\s*[//[%]].*  (it's wrong)
                    // updated Regex was (//|%).+   (it's also wrong, strips off everything after % (e.g. in FR %.2f))
                    // up-updated Regex is ^\\s*(//|%).+ (also wrong, a line containing nothing but // is not recognized as comment line)
                    // up-updated Regex is ^\\s*(//|%).*
                    Line = Line.replaceAll("^\\s*(//|%).*", "");

                    // remove leading and trailing whitespaces
                    Line = Line.replaceAll("^\\s+", "").replaceAll("\\s+$", "");

                    // go to the next line if Line is empty
                    if (Line.isEmpty())
                        continue;


                    // make the Matcher
                    Matcher m = pattern.matcher(Line);

                    // Make the match
                    if (m.matches()) {

                        // convert to numbers
                        CalPoint.F = Utilities.getFloat(m.group(1));
                        CalPoint.R = Utilities.getFloat(m.group(2));
                        CalPoint.Phi = Utilities.getFloat(m.group(3));
                        
                        // store in the list
                        Calibration.add(CalPoint);

                    } else {
                        // no valid match was found so throw an Exception
                        String str = "Could not convert the line\n" + Line +
                                "\ninto three numbers. Please correct the line and try again.";
                        throw new DataFormatException(str);
                    }

                }
            } catch (IOException ex) {
                String str = "An IO error occurred during reading the file\n"
                        + CalFileName + "\n";

                throw new DataFormatException(str);
            }

            // close the file
            if ( fr != null ) {
                try { fr.close(); } catch (IOException ignore) {}
            }
            
            // make array for interpolation
            double[] Fcal = new double[Calibration.size()];
            double[] Rcal = new double[Calibration.size()];
            double[] Phical = new double[Calibration.size()];
            
            // convert ArrayList to array
            for (int i=0; i<Calibration.size(); i++) {
                Fcal[i] = Calibration.get(i).F;
                Rcal[i] = Calibration.get(i).R;
                Phical[i] = Calibration.get(i).Phi;
            }
            
            // throw away the ArrayList
            Calibration = null;
            
        
            
            ///////////////////////
            // do the interpolation
        
            int InterpolationType = m_iC_Properties.getInt("SRS_SR850.GainCalibration.InterpolationType", 1);

            double Bandwidth = m_iC_Properties.getDouble("SRS_SR850.GainCalibration.LoessBandwidth", 0.01);
            int RobustnessIters = m_iC_Properties.getInt("SRS_SR850.GainCalibration.LoessRobustnessIterations", 2);
            double Accuracy = m_iC_Properties.getDouble("SRS_SR850.GainCalibration.LoessAccuracy", 1e-12);


            try {
                // make the interpolator
                // http://commons.apache.org/math/userguide/analysis.html#a12.6_Curve_Fitting
                if (InterpolationType == 2) {
                    Interpolator = new LoessInterpolator(Bandwidth, RobustnessIters, Accuracy);
                } else {
                    Interpolator = new LinearInterpolator();
                }

                // generate the interpolating function
                InterpolationR = Interpolator.interpolate(Fcal, Rcal);
                InterpolationPhi = Interpolator.interpolate(Fcal, Phical);


                // This comment was copied from SRS_DS345.setARBtoCELIV
                // When experimenting with real measurement data, I found that the Loess
                // interpolation sometimes works with a bandwidth of 0.1, sometimes 0.01
                // is required. When the interpolation did not work, the function evaluation
                // returned NaN, so I test for NaN here
                if (InterpolationR.value(Fcal[0]) == Double.NaN ||
                    InterpolationPhi.value(Fcal[0]) == Double.NaN ) {
                    String str = "Info: The Loess interpolation seems to have failed. Try decreasing the Loess Bandwidth\n"
                            + "in the iC_User.properties using SRS_SR850.GainCalibration.LoessBandwidth = 0.1.\n";
                    throw new DataFormatException(str);
                }

                /* Catch all Exceptions as Commons.Math might throw different Exceptions
                 * that have not a common Math.Exception as their root, and the
                 * javadoc is not 100% correct. */
            } catch (Exception ex) {

                // throw Exception
                String str = "Error: Could not generate the interpolating curves.\n";
                str += ex.getMessage() + "\n";
                throw new DataFormatException(str);
            }
        }
        //</editor-fold>
        
        
        // return if in Syntax-check mode
        if (inSyntaxCheckMode())
            return Results;

        // exit if in No-Communication-Mode if not in JUnit Test
        if (inNoCommunicationMode() && !inJUnitTest())
            return Results;
        

        // plot a chart?
        if (!FileExtension.isEmpty()) {
            // make a new XYChart object
            Chart = new iC_ChartXY("Sweep f",
                                   "f [ Hz ]",
                                   "R", //  left Y axis label, e.g |Z| [ Ohm ]
                                   true  /*legend*/,
                                   1024, 768);

            // add second Y axis for Phi
            int SecondAxisIndex = Chart.newYaxis("Phi [deg]"); // right Y axis label, e.g. phi [deg]

            // change scaling of X axis to log if log-frequency sweep
            if (FreqLog) {
                Chart.LogXAxis(true);
            }

            // use nice number format
            Chart.getYAxis(0).setNumberFormatOverride(new DecimalFormat("##0.#####E0"));
            Chart.getYAxis(SecondAxisIndex).setNumberFormatOverride(new DecimalFormat("0.00"));

            // dynamically calculate tick units (to show very small numbers)
            Chart.getYAxis(0).setStandardTickUnits(new StandardTickUnitSource());
            Chart.getYAxis(SecondAxisIndex).setStandardTickUnits(new StandardTickUnitSource());

            // adjust minimum value for auto scaling
            // TODO @Emilie What would be reasonable values?
            Chart.getYAxis(0).setAutoRangeMinimumSize(1e-12);
            Chart.getYAxis(SecondAxisIndex).setAutoRangeMinimumSize(1e-12);

            // don't include 0 in autoscaling
            Chart.getYAxis(0).setAutoRangeIncludesZero(false);
            Chart.getYAxis(SecondAxisIndex).setAutoRangeIncludesZero(false);
        
        
            // add a new data series
            // First axis (left), e.g. |Z|
            SeriesID_R = Chart.AddXYSeries("R", 0, false, true,
                                             Chart.LINE_SOLID, Chart.MARKER_NONE);

            // Second axis (right), e.g. phi in degree
            SeriesID_Phi = Chart.AddXYSeries("Phi", SecondAxisIndex, false, true,
                                             Chart.LINE_DASHED, Chart.MARKER_NONE);
        }


        
        //set Lock-in to internal reference mode
        setReferenceSource(0);
        
        // get current Time Constant
        double TimeConstant = getTimeConstant();

        // calc the dalay time in ms
        int DelayTime = 1000 * (int)Math.round(TimeConstant * DelayFactor);

        // ensure that VDelay is greater than 0
        // Always wait at least 1000 ms
        if (DelayTime < 1000)
            DelayTime = 1000;
        
        // display a message
        if (DelayTime > 5000) {
            String str = String.format("Info: The delay time in SRS_SR850.SweepF is %.1f sec\n", DelayTime/1000);
            m_GUI.DisplayStatusMessage(str);
        }

        // sweep through all frequencies
        for (double f : F) {
            
            // check Stop button
            if (m_StopScripting)
                break;

            // check for Pause button
            isPaused(true);

            // set the spot frequency
            setFrequency(f);

        
            // wait for the specified time
            try { Thread.sleep(DelayTime); } catch (InterruptedException ignore) {}



            // measure
            MValue Data = Measure();
            
            // store the measurement in the list
            Results.add(Data);
            
            
            // do the gain / phase correction
            if (!CalibrationFile.isEmpty()) {
                
                double R;
                double Phi;
                
                try {
                
                    // get interpolation values
                    R = InterpolationR.value(Data.F);
                    Phi = InterpolationPhi.value(Data.F);

                } catch (Exception ex) {
                        String str = "Could not interpolate the gain / phase at F = " + Data.F + "\n"
                                + ex.getMessage() + "\n";
                        throw new ScriptException(str);
                }
                
                // do the calibration
                MValue DataCalibrated = new MValue();
                DataCalibrated.F = Data.F;
                DataCalibrated.R = (float)(Data.R * R);
                DataCalibrated.Phi = (float)(Data.Phi - Phi);
                
                // store the result
                ResultsCalibrated.add(DataCalibrated);
                
            }            


            // add the datapoint to the graph
            // TODO 4* also plot the corrected amplitude/phase once GainCalibration works
            if (!FileExtension.isEmpty()) {
                Chart.AddXYDataPoint(SeriesID_R, Data.F, Data.R);
                Chart.AddXYDataPoint(SeriesID_Phi, Data.F, Data.Phi);
            }

        }
        

        // done measuring, save the data
        if (!FileExtension.isEmpty()) {
            

            // get the filename
            String FileName = m_GUI.getFileName(FileExtension);


            // open the file
            BufferedWriter FWriter;
            try {
                FWriter = new BufferedWriter(new FileWriter(FileName));

                // write header lines

                // Time constant
                FWriter.write("% Lock-in Time Constant:" + TimeConstant);
                FWriter.newLine();

                // AC voltage amplitude
                String str = QueryInstrument("SLVL?");
                FWriter.write("% AC Amplitude: " + str + "V");
                FWriter.newLine();

                // Sensitivity
                double Sensitivity = getSensitivity();
                FWriter.write("% Sensitivity: " + Sensitivity);
                FWriter.newLine();

                // column headers
                if (CalibrationFile.isEmpty()) {
                    FWriter.write("f [Hz]\tR\tPhi [deg]");
                } else {
                    FWriter.write("f [Hz]\tR\tPhi [deg]\tR_calibrated\tPhi_calibrated");
                }
                FWriter.newLine();

                // save data
                for (int i=0; i<Results.size(); i++) {
                    
                    // get measured data
                    MValue Data = Results.get(i);

                    // build one line
                    String dummy;
                    if (CalibrationFile.isEmpty()) {
                        dummy = String.format(Locale.US, "%E\t%E\t%E",
                                Data.F, Data.R, Data.Phi);
                    } else {
                        // get calibrated data
                        MValue DataCalibrated = ResultsCalibrated.get(i);
                        
                        dummy = String.format(Locale.US, "%E\t%E\t%E\t%E\t%E",
                                Data.F, Data.R, Data.Phi, DataCalibrated.R, DataCalibrated.Phi);
                    }

                    // store to file
                    FWriter.write(dummy);
                    FWriter.newLine();
                }

                // close the file
                FWriter.close();

            } catch (IOException ex) {
                m_Logger.log(Level.SEVERE, "Error writing to the file in SweepF", ex);

                // show a dialog
                String str = "Could not open/write/close the file\n";
                str += FileName + "\n\n";
                str += ex.getMessage();

                throw new ScriptException(str);
            }

            ////////////////////////
            // save the chart as png

            // make the file
            File file = new File( m_GUI.getFileName(FileExtension + ".png") );

            // save the chart
            try {
                Chart.SaveAsPNG(file, 0, 0);
            } catch (IOException ignore) {
                m_GUI.DisplayStatusMessage("Could not save the chart.\n");
            }
        }
        
        // return measuremtns for the next script command
        return Results;
 
    }//</editor-fold>
    
    
    /**
     * Performes the gain/phase calibration. It sweeps the frequency and takes a 
     * measurement at every frequency. A gain/phase calibration factor is 
     * calculated based on the specified nominal resistanance value and exported
     * in a file. This file can be used in <code>SweepF</code>. The data
     * is shown in a chart together with an interolation curve for inspection. The
     * same interpolation curve will be used in <code>SweepF</code> (although it
     * will be re-calculated there with the same settings).
     * 
     * @param NominalResistance The value of the calibration resistor used to 
     * perform the gain/phase calibration.
     * @param FreqStart Start Frequency
     * @param FreqStop Stop Frequency
     * @param FreqInc Increment in Frequency respectively number of points per 
     * decade. See <code>Utilities.GenerateValues</code>
     * @param FreqLog When <code>false</code> the frequency points are equally spaced;
     * if <code>true</code> they are logarithmically spaced. See <code>GenerateValues</code>
     * @param DelayFactor After setting the new frequency, a time delay is added before 
     * taking the measurement. This time delay is a multiple of the LIA's time
     * constant. <code>DelayFactor</code> specifies this multiplier. Must be greater
     * than 0.
     * @param FileExtension This String is appended to the File Name. If the
     * filename already exists, a new file with a counter is inserted between the 
     * FileName and the FileExtension.
     * 
     * @throws ScriptException When generating or calculating the gain / phase 
     * interpolation curve failed, or when a File IO error ocurred
     * @throws IOException When communication with the Instrument failed (bubbled
     * up from <code>getTimeConstant</code> or some more direct query commands.
     * @throws DataFormatException when the Syntax Check failed.
     */
    // <editor-fold defaultstate="collapsed" desc="GainCalibration">
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Performes a gain/phase calibration.<br>"
            + "Uses 'SweepF' to measure the real gain/phase at different frequencies.<br>"
            + "Sets the Lock-in's Reference Signal to Internal.</html>",
        ParameterNames = {"Nominal Resistance","Start Frequency [Hz] { [.001, 102e3] }", "Stop Frequency [Hz] { [.001, 102e3] }",
                          "Frequency Increment", "logarithmic Freq. spacing?", "Delay time factor", "File Extension"},
        DefaultValues = {"50","2000", "3000", "100", "false",
                           "250", ".gcal.txt"},
        ToolTips = {"Specifies the resistance value of the DUT when making the gain/phase calibration.",
                    "", "", "<html>Frequency difference or number of steps per decade<br>if logarithmic sweep is selected</html>", 
                    "", "<html>Specifies the multiplier of the current Time Constant set in the LIA to wait after setting<br>"
                + "the new frequency and starting the measruement.</html>", "" })
    @iC_Annotation(  MethodChecksSyntax = true )
    public void GainCalibration(float NominalResistance,
                                double FreqStart, double FreqStop, double FreqInc, boolean FreqLog,
                                int DelayFactor, String FileExtension)
           throws ScriptException, IOException, DataFormatException {
        
        //////////////////
        // local variables
        
        // make new Utilities object
        Utilities util = new Utilities();
        
        
        
        ///////////////
        // do the sweep
        ArrayList<MValue> Measured = SweepF(FreqStart, FreqStop, FreqInc, FreqLog, DelayFactor, "", "");
        
        
        // return if in Syntax-check mode
        if (inSyntaxCheckMode())
            return;        
        
        
        /////////////////////////////
        // calc gain/phase correction
                
        // array for data interpolation and intermediate storage
        double[] F = new double[Measured.size()];
        double[] R = new double[Measured.size()];
        double[] Phi = new double[Measured.size()];
        
        // iterate through all frequencies
        for (int i=0; i<Measured.size(); i++) {
            
            // get current measuremend
            MValue DataPoint = Measured.get(i);
            
            // calc gain correction
            R[i] = NominalResistance / DataPoint.R;
            
            // calc phase correction
            Phi[i] = 0 - DataPoint.Phi;
            
            // store frequency
            F[i] = DataPoint.F;
        }
        
        
        ///////////////////////
        // do the interpolation
        // <editor-fold defaultstate="collapsed" desc="interpolate">              
        
        // use Apache Common Math
        UnivariateInterpolator Interpolator;
        UnivariateFunction InterpolationR = null;
        UnivariateFunction InterpolationPhi = null;
        
        int InterpolationType = m_iC_Properties.getInt("SRS_SR850.GainCalibration.InterpolationType", 1);

        double Bandwidth = m_iC_Properties.getDouble("SRS_SR850.GainCalibration.LoessBandwidth", 0.01);
        int RobustnessIters = m_iC_Properties.getInt("SRS_SR850.GainCalibration.LoessRobustnessIterations", 2);
        double Accuracy = m_iC_Properties.getDouble("SRS_SR850.GainCalibration.LoessAccuracy", 1e-12);


        try {
            // make the interpolator
            // http://commons.apache.org/math/userguide/analysis.html#a12.6_Curve_Fitting
            if (InterpolationType == 2) {
                Interpolator = new LoessInterpolator(Bandwidth, RobustnessIters, Accuracy);
            } else {
                Interpolator = new LinearInterpolator();
            }

            // generate the interpolating function
            InterpolationR = Interpolator.interpolate(F, R);
            InterpolationPhi = Interpolator.interpolate(F, Phi);


            // This comment was copied from SRS_DS345.setARBtoCELIV
            // When experimenting with real measurement data, I found that the Loess
            // interpolation sometimes works with a bandwidth of 0.1, sometimes 0.01
            // is required. When the interpolation did not work, the function evaluation
            // returned NaN, so I test for NaN here
            if (InterpolationR.value(F[0]) == Double.NaN ||
                InterpolationPhi.value(F[0]) == Double.NaN ) {
                String str = "Info: The Loess interpolation seems to have failed. Try decreasing the Loess Bandwidth\n"
                        + "in the iC_User.properties using SRS_SR850.GainCalibration.LoessBandwidth = 0.1.\n";
                throw new DataFormatException(str);
            }

            /* Catch all Exceptions as Commons.Math might throw different Exceptions
             * that have not a common Math.Exception as their root, and the
             * javadoc is not 100% correct. */
        } catch (Exception ex) {

            // throw Exception
            String str = "Error: Could not generate the interpolating curves.\n";
            str += ex.getMessage() + "\n";
            throw new DataFormatException(str);
        }
        
        
        ////////////////////////////////////
        // do the interpolation for plotting
        
        // array for data interpolation and intermediate storage
        double[] Fint = new double[10 * Measured.size()];
        double[] Rint = new double[10 * Measured.size()];
        double[] Phiint = new double[10 * Measured.size()];
        
        // calc new FreqInc parameter
        double FreqIncNew;
        if (FreqLog) {
            // log sweep, so multiply
            FreqIncNew = FreqInc * 10;
        } else {
            // linear sweep, so divide
            FreqIncNew = FreqInc / 10;
        }
        
        // generate the frequencies
        ArrayList<Double> FintList = util.GenerateValues(FreqStart, FreqStop, FreqIncNew, FreqLog, null, 0);
        
        // iterate through all frequencies
        for (int i=0; i<FintList.size(); i++) {
            
            // set frequency
            Fint[i] = FintList.get(i);
            
            try {
                
                // get interpolation values
                Rint[i] = InterpolationR.value(Fint[i]);
                Phiint[i] = InterpolationPhi.value(Fint[i]);
                
            } catch (Exception ex) {
                    String str = "Could not interpolate the gain / phase at F = " + Fint[i] + "\n"
                            + ex.getMessage() + "\n";
                    throw new ScriptException(str);
            }
        }
        //</editor-fold>
        
        
        
        ////////////////
        // plot the data
        
        // make a new XYChart
        iC_ChartXY Chart = new iC_ChartXY("Gain / Phase Calibration",
                                "Frequency [Hz]", "Gain Calibration [1]",
                                false  /*legend*/,
                                640, 480);
        
        // add second Y axis for Phi
        int SecondAxisIndex = Chart.newYaxis("Phi Calibration [deg]"); // right Y axis label, e.g. phi
        
        // change scaling of X axis to log if log-frequency sweep
        if (FreqLog) {
            Chart.LogXAxis(true);
        }
        
        // use nice number format
        // TODO @Emily you probably want to use a different number format for your real data
        Chart.getYAxis(0).setNumberFormatOverride(new DecimalFormat("##0.#####E0"));
        Chart.getYAxis(SecondAxisIndex).setNumberFormatOverride(new DecimalFormat("0.00"));
        
        // dynamically calculate tick units (to show very small numbers)
        // TODO @Emily uncomment if zooming into the data does not update the axis ticks satisfacttory
        //Chart.getYAxis(0).setStandardTickUnits(new StandardTickUnitSource());
        //Chart.getYAxis(SecondAxisIndex).setStandardTickUnits(new StandardTickUnitSource());
        
        // adjust minimum value for auto scaling
        // TODO @Emilie What would be reasonable values? Does it work without explicit settings?
        //Chart.getYAxis(0).setAutoRangeMinimumSize(1e-12);
        //Chart.getYAxis(SecondAxisIndex).setAutoRangeMinimumSize(1e-12);
        
        // don't include 0 in autoscaling
        //Chart.getYAxis(0).setAutoRangeIncludesZero(false);
        //Chart.getYAxis(SecondAxisIndex).setAutoRangeIncludesZero(false);
        
               

        // add a new data series
        // interpolated Gain / Phase
        SeriesIdentification SeriesID_Rcalint = Chart.AddXYSeries("Rcal_int", 0, false, true,
                                                        Chart.LINE_SOLID, Chart.MARKER_NONE);

        // Second axis (right), e.g. phi in degree
        SeriesIdentification SeriesID_Phicalint = Chart.AddXYSeries("Phical_int", SecondAxisIndex, false, true,
                                                        Chart.LINE_DASHED, Chart.MARKER_NONE);
        
        // measured Gain / Phase values
        SeriesIdentification SeriesID_Rcal = Chart.AddXYSeries("Rcal_meas", 0, false, true,
                                                        Chart.LINE_NONE, Chart.MARKER_CIRCLE);

        // Second axis (right), e.g. phi in degree
        SeriesIdentification SeriesID_Phical = Chart.AddXYSeries("Phical_meas", SecondAxisIndex, false, true,
                                                        Chart.LINE_NONE, Chart.MARKER_CIRCLE);
        
        
        
        // plot measured gain / phase calibration points
        Chart.AddXYDataPoints(SeriesID_Rcal, F, R);
        Chart.AddXYDataPoints(SeriesID_Phical, F, Phi);
        
        // plot interpolated gain / phase calibration points
        Chart.AddXYDataPoints(SeriesID_Rcalint, Fint, Rint);
        Chart.AddXYDataPoints(SeriesID_Phicalint, Fint, Phiint);

        
        
        ////////////////////////
        // save the chart as png
        
        // make the file
        File file = new File( m_GUI.getFileName(FileExtension + ".png") );
        
        // save the chart
        try {
            Chart.SaveAsPNG(file, 0, 0);
        } catch (IOException ignore) {
            m_GUI.DisplayStatusMessage("Could not save the chart.\n");
        }
            
        
        
        
        /////////////////
        // export to file
        
        // get the filename
        String FileName = m_GUI.getFileName(FileExtension);


        // open the file
        BufferedWriter FWriter;
        try {
            FWriter = new BufferedWriter(new FileWriter(FileName));
            
            // write header lines
            
            FWriter.write("% Gain / Phase calibration for R = " + NominalResistance);
            
            // Time constant
            FWriter.write("% Lock-in Time Constant:" + getTimeConstant());
            FWriter.newLine();
            
            // AC voltage amplitude
            String str = QueryInstrument("SLVL?");
            FWriter.write("% AC Amplitude: " + str + "V");
            FWriter.newLine();
            
            // Sensitivity
            str = QueryInstrument("SENS?");
            FWriter.write("% Sensitivity code, SRS manual page 6-7: " + str);
            FWriter.newLine();

            // column headers
            FWriter.write("% f [Hz]\t" + "R_cal [1]" + "\t" + "Phi_cal [deg]");
            FWriter.newLine();

            // save data
            for (int i=0; i<F.length; i++) {

                // build one line
                String dummy = String.format(Locale.US, "%E\t%E\t%E",
                        F[i], R[i], Phi[i]);

                // store to file
                FWriter.write(dummy);
                FWriter.newLine();
            }

            // close the file
            FWriter.close();

        } catch (IOException ex) {
            m_Logger.log(Level.SEVERE, "Error writing to the file in GainCalibration", ex);

            // show a dialog
            String str = "Could not open/write/close the file\n";
            str += FileName + "\n\n";
            str += ex.getMessage();

            throw new ScriptException(str);
        }
        
    }//</editor-fold>

    
    // TODO delme
//    private void Interpolate(double[] F, double[] R, double[] Phi,
//                             double[] Fint) 
//            throws DataFormatException, ScriptException {
//        
//        // use Apache Common Math
//        UnivariateInterpolator Interpolator;
//        UnivariateFunction InterpolationR = null;
//        UnivariateFunction InterpolationPhi = null;
//        
//        int InterpolationType = m_iC_Properties.getInt("SRS_SR850.GainCalibration.InterpolationType", 1);
//
//        double Bandwidth = m_iC_Properties.getDouble("SRS_SR850.GainCalibration.LoessBandwidth", 0.01);
//        int RobustnessIters = m_iC_Properties.getInt("SRS_SR850.GainCalibration.LoessRobustnessIterations", 2);
//        double Accuracy = m_iC_Properties.getDouble("SRS_SR850.GainCalibration.LoessAccuracy", 1e-12);
//
//
//        try {
//            // make the interpolator
//            // http://commons.apache.org/math/userguide/analysis.html#a12.6_Curve_Fitting
//            if (InterpolationType == 2) {
//                Interpolator = new LoessInterpolator(Bandwidth, RobustnessIters, Accuracy);
//            } else {
//                Interpolator = new LinearInterpolator();
//            }
//
//            // generate the interpolating function
//            InterpolationR = Interpolator.interpolate(F, R);
//            InterpolationPhi = Interpolator.interpolate(F, Phi);
//
//
//            // This comment was copied from SRS_DS345.setARBtoCELIV
//            // When experimenting with real measurement data, I found that the Loess
//            // interpolation sometimes works with a bandwidth of 0.1, sometimes 0.01
//            // is required. When the interpolation did not work, the function evaluation
//            // returned NaN, so I test for NaN here
//            if (InterpolationR.value(F[0]) == Double.NaN ||
//                InterpolationPhi.value(F[0]) == Double.NaN ) {
//                String str = "Info: The Loess interpolation seems to have failed. Try decreasing the Loess Bandwidth\n"
//                        + "in the iC_User.properties using SRS_SR850.GainCalibration.LoessBandwidth = 0.1.\n";
//                throw new DataFormatException(str);
//            }
//
//            /* Catch all Exceptions as Commons.Math might throw different Exceptions
//             * that have not a common Math.Exception as their root, and the
//             * javadoc is not 100% correct. */
//        } catch (Exception ex) {
//
//            // throw Exception
//            String str = "Error: Could not generate the interpolating curves.\n";
//            str += ex.getMessage() + "\n";
//            throw new DataFormatException(str);
//        }
//        
//        
//        ////////////////////////////////////
//        // do the interpolation for plotting
//        
//        // array for data interpolation and intermediate storage
//        double[] Rint = new double[Fint.length];
//        double[] Phiint = new double[Fint.length];
//        
//        
//        // generate the frequencies
//        //ArrayList<Double> FintList = util.GenerateValues(FreqStart, FreqStop, FreqIncNew, FreqLog, null, 0);
//        
//        // iterate through all frequencies
//        for (int i=0; i<Fint.length; i++) {
//            
//            try {
//                
//                // get interpolation values
//                Rint[i] = InterpolationR.value(Fint[i]);
//                Phiint[i] = InterpolationPhi.value(Fint[i]);
//                
//            } catch (Exception ex) {
//                    String str = "Could not interpolate the gain / phase at F = " + Fint[i] + "\n"
//                            + ex.getMessage() + "\n";
//                    throw new ScriptException(str);
//                }
//        }
//    }
    
    
    /**
     * Gets the Sensitivity.<p>
     *
     * @return The Sensitivity value.
     *
     * @throws IOException re-thrown from <code>QueryFromInstrument</code>
     * @throws ScriptException When the returned answer cannot be converted to an
     * integer of an invalid integer was received.
     */
    // <editor-fold defaultstate="collapsed" desc="getSensitivity">
    /* At present, it seems unnecessary to announce this method in the GUI.
     * It is public, so it could be called if one knew it existed.
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>Gets the lock-in Sensitivity.</html>",
        ParameterNames = {},
        DefaultValues = {},
        ToolTips = {})
    @iC_Annotation(  MethodChecksSyntax = false )
     * */
    public double getSensitivity()
           throws IOException, ScriptException {
 
 
        // exit if in No-Communication-Mode
        if (inNoCommunicationMode())
            return -1;
 
 
        // build the string
        String dummy = String.format(Locale.US, "SENS?");
 
        // query the LIA
        String ans = QueryInstrument(dummy);
       
        // convert to an integer
        int SNint;
        try {
           
            SNint = Utilities.getInteger(ans);
           
        } catch (ScriptException ex) {
            String str = "The received Sensitivity is invalid:\n";
            str += ex.getMessage();
           
            throw new ScriptException(str);
        }
       
        
        // convert the int to a real time
        double SN;
        switch (SNint) {
            case 0: SN = 2e-9; break;
            case 1: SN = 5e-9; break;
            case 2: SN = 10e-9; break;
            case 3: SN = 20e-9; break;
            case 4: SN = 50e-9; break;
            case 5: SN = 100e-9; break;
            case 6: SN = 200e-9; break;
            case 7: SN = 500e-9; break;
            case 8: SN = 1e-6; break;
            case 9: SN = 2e-6; break;
            case 10: SN = 5e-6; break;
            case 11: SN = 10e-6; break;
            case 12: SN = 20e-6; break;
            case 13: SN = 50e-6; break;
            case 14: SN = 100e-6; break;
            case 15: SN = 200e-6; break;
            case 16: SN = 500e-6; break;
            case 17: SN = 1e-3; break;
            case 18: SN = 2e-3; break;
            case 19: SN = 5e-3; break;
            case 20: SN = 10e-3; break;
            case 21: SN = 20e-3; break;                             
            case 22: SN = 50e-3; break;
            case 23: SN = 100e-3; break;
            case 24: SN = 200e-3; break;
            case 25: SN = 500e-3; break;
            case 26: SN = 1; break;         
            default:
                String str = "The received sensitivity (" + SNint + ") is invalid.\n"
                        + "It should be between 0 and 26.\n";
           
                throw new ScriptException(str);
        }
       
        // return the Sensitivity
        return SN;
 
       
    }//</editor-fold>
    
}
