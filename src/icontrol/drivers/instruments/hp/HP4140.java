//////////////////////////////////////////////////////////////
// This is WORK IN PROGRESS and might not work as expected. //
//////////////////////////////////////////////////////////////

// TODO add to list of supported instruments in overview.html

/*
 * The Instrument-Control (iC) software is an experimental system and the
 * author(s) do not assume any responsibility whatsoever for its use by other 
 * parties, and makes no guarantees, expressed or implied, about its quality, 
 * reliability, or any other characteristic. We would appreciate your citation 
 * if the software is used: http://dx.doi.org/10.6028/jres.117.010 .
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
package icontrol.drivers.instruments.hp;

import icontrol.AutoGUIAnnotation;
import icontrol.drivers.Device;
import icontrol.iC_Annotation;
import icontrol.drivers.Device.CommPorts;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Logger;
import javax.script.ScriptException;

/**
 * Hewlett-Packard HP 4140B pA meter / DC voltage source.<p>
 *
 * All device commands that the NewInstrument understands are
 * implemented here.<p>
 *
 * <h3>Instrument Commands:</h3>
 * <ul>
 *  <li>{@link #Measure(String) }
 * </ul>
 *
 * @author KPP (Kurt Pernstich: pernstich@alumni.ethz.ch)
 * @version 0.1
 *
 */

// promise that this class supports GPIB communication
@iC_Annotation( CommPorts=CommPorts.GPIB,
                InstrumentClassName="HP 4140")
public class HP4140 extends Device {


    ///////////////////
    // member variables

    /**
     * The Logger for this class. Note that the Logger Level is set in the
     * constructor of the <code>Device</code> class.
     */
    private static final Logger m_Logger = Logger.getLogger("iC.Instruments.HP4140");
    
    


    /**
     * Override the default <code>Open</code> method in the <code>Device</code>
     * class because this Instrument does not support IEEE 488.2 calls which
     * are used in the default implementation.<p>
     * 
     * Also calls <code>Init</code>.
     * 
     * @throws IOException if the queue could not be cleared; bubbles up from
     * <code>ClearQueue</code>.
     */
    // <editor-fold defaultstate="collapsed" desc="Open">
    @Override
    public void Open()
           throws IOException {
        
        // initialize the instrument
        Init();

        // clear the queue, just in case
        //ClearQueue();
        
    }//</editor-fold>
    
    
    
    /**
     * Sets the SRQ Mask so that the Status Register shows when measurement data
     * is available (the measurement is done?). See Manual on page 3-55.
     * 
     * @throws IOException bubbles up from <code>SendToInstrument</code>.
     */
    private void Init() 
            throws IOException {
        
        // choose that bit 1-3 in the Status Byte are set according to their
        // specific meaning.
        SendToInstrument("D7");
        
        m_Logger.fine("Done initializing the SRQ Status Register (to D7).");
    }

    
    
    /**
     * For Emily to test saving data of a long measurement.
     * Starts a measurement and waits until the measurement is done. Then
     * saves the data.<p>
     * 
     * Note that the first few measuremtn point might be lost becuase the
     * 4140 drops them after a serial poll used to get the SRQ Status Byte (see
     * Manual page 3-52, Note 2).
     * 
     * @param FileExtension 
     * @throws IOException re-thrown from <code>SendToInstrument</code>
     * @throws ScriptException When the data could not be saved to a file. 
     */
    @AutoGUIAnnotation(
        DescriptionForUser = "<html>TODO</html>",
        ParameterNames = {"File Extension"},
        DefaultValues = {".ui"},
        ToolTips = {""})
    @iC_Annotation(  MethodChecksSyntax = false )
    public void Measure(String FileExtension) 
           throws IOException, ScriptException {
        
        // send the command to start the measurement
        // W1 also addresses the instrument to send back data
        SendToInstrument("W1");
        
        
        ///////////////////////////////////
        // wait until measurements are done
        
        // get status byte
        // Important: After Serial Polling the first few measurement data points 
        // are lost according to the manual on page 3-52, Note 2
        // Not sure which bits need to be checked. Candidates are bits 1, 3, and
        // maybe 8.
        while ( (m_GPIB_Driver.ReadStatusByte() & 0x04) == 0  ) {
            
            // data not ready, so wait a bit
            try {Thread.sleep(250);} catch (InterruptedException ignore) {}
        }
        
        // data should now be ready, so get it
        String Data = QueryInstrument("");
        
        
        
        // TODO @Emily: not sure if saving works. Just copied some other code
        // Maybe you want to look at HP4192.SweepFthenV to see how one can
        // split the data and save it in a nicer format. Maybe the format
        // as it comes from the 4140 is okay.
        
        ////////////
        // save data
        
        // get the File Name
        String FileName = m_GUI.getFileName(FileExtension);

        // open the file for writing
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(FileName));

        } catch (IOException ex) {
            String str = "Could not open the file " + FileName + "\n"
                + "Make sure it is not a directory and that it can be opened or created.\n";
            throw new ScriptException(str);
        }

        try {
            // write to file
            bw.write(Data);

        } catch (IOException ex) {
            String str = "Could not save the data in file" + FileName + "\n"
                    + ex.getMessage();

            throw new ScriptException(str);
        }

        try {
            // close the file
            if (bw != null)
                bw.close();

        } catch (IOException ex) {
            m_GUI.DisplayStatusMessage("Could not close the file " + FileName + "\n");
        }
        
        
        
    }


    // copied from HP3561. might be useful later
    /**
     * Checks if the Instrument requests service, and if it does, the Instrument
     * is addressed to talk so that the un-fetched data is read to empty the
     * output buffer of the 3561 and end the service request.<p>
     *
     * NOTE: Receiving the Status Byte is only implemented when using driver classes
     * derived from <code>GPIB_Driver</code> and an Exception is thrown if some other
     * driver is used (at the time of writing, no such driver exists).
     *
     * @throws IOException Bubbles up from <code>QueryInstrument</code>.
     */
    // TODO @HP3561 ClearQueue() was just copied from an other HP Instrument and needs to be adapted if at all necessary
    // <editor-fold defaultstate="collapsed" desc="Clear Queue">
//    private void ClearQueue()
//            throws IOException {
//
//        // return if in No-Communication Mode
//        if (inNoCommunicationMode())
//            return;
//
//        // check if a driver class derived from GPIB_Driver is used
//        if (m_GPIB_Driver != null) {
//
//            // debugging log
//            m_Logger.finest("Entering ClearQueue.\n");
//
//            // get the Status Byte
//            int Status = m_GPIB_Driver.ReadStatusByte();
//
//            // debugging log
//            m_Logger.log(Level.FINER, "Status Byte = {0}\n", Integer.toString(Status));
//
//            // check for an error
//            if ( (Status & 0x40) > 0) {
//                // log event
//                m_Logger.fine("The 3561 requested service. Will now read from buffer.\n");
//
//                // Instrument is requesting service, so read from the Instrument
//                //m_GPIB_Driver.Receive(false);
//                QueryInstrument("");
//            } else {
//                // log event
//                m_Logger.fine("The 3561 did not request service\n");
//            }
//
//            // get the Status Byte again
//            Status = m_GPIB_Driver.ReadStatusByte();
//
//            // debugging log
//            m_Logger.log(Level.FINER, "Status Byte = {0}\n", Integer.toString(Status));
//
//            // check for errors
//            if ( (Status & 0x40) > 0) {
//                // just log the event
//                m_Logger.warning("The 3561 is still requesting service\n");
//            }
//        } else {
//            String str = "It seems a new driver has been implemented but not been\n"
//                    + "considered in HP3561.ClearQueue(). Plese tell the developer.\n";
//
//            // log event
//            m_Logger.severe(str);
//
//            // show to the user
//            //icontrol.m_GUI.DisplayStatusMessage(str);
//            throw new IOException(str);
//        }
//    }//</editor-fold>

}
