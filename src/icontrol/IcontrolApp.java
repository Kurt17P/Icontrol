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

package icontrol;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class IcontrolApp extends SingleFrameApplication {

    // Arguments passed to the application
    private static String[] m_Args;
    
    // the view
    private IcontrolView m_View;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        
        // manually instantiate the View to be able to return it in getView()
        m_View = new IcontrolView(this, m_Args);
        
        // do more initializations (some require access to the GUI via
        // Utilities.getView, so it can't be done in IcontrolView's constructor anymore)
        m_View.myInit();
        
        show(m_View);
    }
    
    /**
     * @return Returns the <code>GUI_Interface</code> (aka, the GUI) when iC
     * is run as a "regular" application and not as a Unit Test
     */
    // <editor-fold defaultstate="collapsed" desc="getView">
    public GUI_Interface getView() {
        return m_View;
    }//</editor-fold>

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of IcontrolApp
     */
    public static IcontrolApp getApplication() {
        return Application.getInstance(IcontrolApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {

        // store Arguments passed to the application
        m_Args = args;
        
        launch(IcontrolApp.class, args);
    }
}
