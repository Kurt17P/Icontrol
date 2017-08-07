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

import icontrol.IcontrolTests;
import icontrol.drivers.instruments.srs.SRS_DS345;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.DataFormatException;
import javax.script.ScriptException;
import junit.framework.Assert;
import org.junit.Test;


/**
 * Tests for the SRS_DS345 class.
 *
 * @author KPP (Kurt Pernstich: pernstich@alumni.ethz.ch)
 * @version 0.1
 *
 */
public class SRS_DS345Test extends IcontrolTests {
    
    
    /**
     * Test if the calculation routine is somewhat correct. Uses positive and negative
     * sign of TPV data, uses Tramp such that max. output voltage of DS345 is hit
     * or not.
     */
    @Test
    public void setARBtoCELIV_sign_Vmax() 
           throws DataFormatException, IOException, ScriptException, URISyntaxException {

        // do not do this test
        if (false) {
            m_GUI.DisplayStatusMessage("Warning: Skipping setARBtoCELIV_sign_Vmax tests!");
            return;
        } else {
            m_GUI.DisplayStatusMessage("in setARBtoCELIV_sign_Vmax tests\n");
        }
        
        
        // instantiate class
        SRS_DS345 dev = new SRS_DS345();
        
        // disable Syntx-Check Mode
        setSyntaxCheckMode(false);
        
        // disable No-Communication-Mode
        setSimulationMode(false);
        
             
        /////////////////////////
        // T/V file with +Voffset
        String FileName = "setARBtoCELIV/OPV1_Device4_ResTest_merged proper t=0 resampled.txt";

              
        // -Slope, Tramp long enough to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, -300000, 20, FileName);
        m_GUI.DisplayStatusMessage("Done with test 1.\n");
              
        // -Slope, Tramp short enough not to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, -300000, 10, FileName);
        m_GUI.DisplayStatusMessage("Done with test 2.\n");
        
        // +Slope, Tramp long enough to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, 300000, 30, FileName);
        m_GUI.DisplayStatusMessage("Done with test 3.\n");
        
        // +Slope, Tramp short enough not to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, 300000, 10, FileName);
        m_GUI.DisplayStatusMessage("Done with test 4.\n");        
        
        
        /////////////////////////
        // T/V file with -Voffset
        FileName = "setARBtoCELIV/OPV1_Device4_ResTest_merged proper t=0 resampled negativ.txt";
        
        // -Slope, Tramp long enough to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, -300000, 20, FileName);
        m_GUI.DisplayStatusMessage("Done with test 5.\n");
        
        // -Slope, Tramp short enough not to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, -300000, 10, FileName);
        m_GUI.DisplayStatusMessage("Done with test 6.\n");
        
        // +Slope, Tramp long enough to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, 300000, 30, FileName);
        m_GUI.DisplayStatusMessage("Done with test 7.\n");
        
        // +Slope, Tramp short enough not to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, 300000, 10, FileName);
        m_GUI.DisplayStatusMessage("Done with test 8.\n");
        
        
        
        //////////////
        // no TPV file
        
        // -Slope, Tramp long enough to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, -300000, 20, "");
        m_GUI.DisplayStatusMessage("Done with test 9.\n");
        
        // -Slope, Tramp short enough not to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, -300000, 10, "");
        m_GUI.DisplayStatusMessage("Done with test 10.\n");
        
        // +Slope, Tramp long enough to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, 300000, 30, "");
        m_GUI.DisplayStatusMessage("Done with test 11.\n");
        
        // +Slope, Tramp short enough not to reach max. output voltage of DS345
        dev.setARBtoCELIV(10, 300000, 10, "");
        m_GUI.DisplayStatusMessage("Done with test 12.\n");
        

        
        WaitForUser();
    }
    
    
    @Test
    public void setARBtoCELIV_basics() 
           throws DataFormatException, IOException, ScriptException, URISyntaxException {

        // do not do this test
        if (true) {
            m_GUI.DisplayStatusMessage("Warning: Skipping setARBtoCELIV_basics tests!");
            return;
        } else {
            m_GUI.DisplayStatusMessage("in setARBtoCELIV_basics tests\n");
        }
        
        
        // instantiate class
        SRS_DS345 dev = new SRS_DS345();
        
        // disable Syntx-Check Mode
        setSyntaxCheckMode(false);
        
        // disable No-Communication-Mode
        setSimulationMode(false);
        
        
        
        /////////////////////////
        // T/V file with +Voffset
        String FileName = "setARBtoCELIV/OPV1_Device4_ResTest_merged proper t=0 resampled.txt";
        
        
        // -Slope, longer than 40 MHz (407 us)
        dev.setARBtoCELIV(10, -5000, 400, FileName);
        m_GUI.DisplayStatusMessage("Done with test 1.\n");
        
        
        // 0 delay
        dev.setARBtoCELIV(0, -5000, 400, FileName);
        m_GUI.DisplayStatusMessage("Done with test 2.\n");

        
        // test where Tdelay is larger than measured photovoltage time span
        try {
            dev.setARBtoCELIV(2e6, 1000, 10, FileName);
            
            // fail the test if no Exception is thrown
            Assert.fail("Expected an Exception as Tdelay is too large");
        } catch (DataFormatException ignore) {}
        m_GUI.DisplayStatusMessage("Done with test 3.\n");
        
        
        
        
        // test comments in file
        dev.setARBtoCELIV(1, -10000, 10, "setARBtoCELIV/OPV1_Device4_ResTest_merged proper t=0 resampled comments.txt");
        m_GUI.DisplayStatusMessage("Done with test 4.\n");

            
        // test File Not Found
        try {
            dev.setARBtoCELIV(10, 1000, 10, "no file there.txt");
            
            // fail the test if no Exception is thrown
            Assert.fail("Expected an Exception for not finding the file");
        } catch (DataFormatException ignore) {}
        m_GUI.DisplayStatusMessage("Done with test 5.\n");

        
        

        
        WaitForUser();

    }
}
