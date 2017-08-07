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
import icontrol.drivers.instruments.srs.SRS_SR850;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import javax.script.ScriptException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the SRS_SR345 class.
 *
 * @author KPP (Kurt Pernstich: pernstich@alumni.ethz.ch)
 * @version 0.1
 *
 */
public class SRS_SR850Test extends IcontrolTests {
    

    /**
     * Test of SweepF method.
     */
    @Test
    public void testSweepF() 
           throws ScriptException, IOException, DataFormatException {
        
        // do not do this test
        if (false) {
            m_GUI.DisplayStatusMessage("Warning: Skipping SweepF tests!");
            return;
        } else {
            m_GUI.DisplayStatusMessage("in SweepF tests...\n");
        }
        
        
        // instantiate class
        SRS_SR850 dev = new SRS_SR850();
        
        // disable Syntx-Check Mode
        setSyntaxCheckMode(false);
        
        // enable No-Communication-Mode
        setSimulationMode(true);
        
        
   
        double FreqStart = 100;
        double FreqStop = 10e3;
        double FreqInc = 25;
        boolean FreqLog = true;
        int DelayFactor = 1;
        String FileExtension = ".testSweepF.txt";
        
        // do the test
        dev.SweepF(FreqStart, FreqStop, FreqInc, FreqLog, DelayFactor, FileExtension, "");
        
        // let the user inspect the chart output
        WaitForUser();
        
        
        
        // display status message
        m_GUI.DisplayStatusMessage("in SweepF tests...done.\n");   
    }
    
    
    
    /**
     * Test of GainCalibration method.
     */
    @Test
    public void testGainCalibration() 
           throws ScriptException, IOException, DataFormatException {
        
        // do not do this test
        if (false) {
            m_GUI.DisplayStatusMessage("Warning: Skipping GainCalibration tests!");
            return;
        } else {
            m_GUI.DisplayStatusMessage("in GainCalibration tests...\n");
        }
        
        // instantiate class
        SRS_SR850 dev = new SRS_SR850();
        
        // disable Syntx-Check Mode
        setSyntaxCheckMode(false);
        
        // enable No-Communication-Mode
        setSimulationMode(true);
        
        
   
        // note that the frequency range needs to fit the range returned 
        // in Measure during JUnit test
        
        // linear scale
        double FreqStart = 10;
        double FreqStop = 1000;
        double FreqInc = 10;
        boolean FreqLog = false;
        int DelayFactor = 1;
        String FileExtension = ".testgcallin.txt";
        
        // do the test
        dev.GainCalibration(50, FreqStart, FreqStop, FreqInc, FreqLog, DelayFactor, FileExtension);
        
        
        
        // log scale
//        FreqStart = 10;
//        FreqStop = 10e3;
//        FreqInc = 25;
//        FreqLog = true;
//        DelayFactor = 1;
//        FileExtension = ".testgcallog.txt";
//        
//        // do the test
//        dev.GainCalibration(50, FreqStart, FreqStop, FreqInc, FreqLog, DelayFactor, FileExtension);
        
        
        // let the user inspect the chart output
        WaitForUser();
        
        
        
        // display status message
        m_GUI.DisplayStatusMessage("in GainCalibration tests...done.\n");
        
    }

    
}
