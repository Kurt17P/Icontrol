
"""Each time the Python Interpreter is (re-)started, this script is executed.

The Python search path includes the Project Directory specified in the GUI and
the path to the iC directory. Python files residing in those directories can 
be imported (and hence exectued) using the import command.

Instrument Control (iC) exports a Device object called '_device' to Python. It
is recommended to use the functions defined here to access the Device class'
functionality. Direct access to any method of _device should be encapsulated
in a Python function.

For convenience, the return value of the last (non-Python) command is exported
to the Python environment as '_ans' (see example in Example_Scripts/Python Demo)

"""

# --- all imports

import sys



# --- function definitions

def dispatch_command(InstrumentName, CommandName, *Parameters):
    """Send a command to the specified Instrument.

    This method is the Python-equivalent to Device.DispatchCommand(String). The
    returned value is originally (in Java) of type Object.

    """

    # build the first part of the Command String
    cmd = InstrumentName + ' ' + CommandName + ' '

    # append additional parameters
    for i in range( len(Parameters) ):
        cmd += str( Parameters[i] )
        if i+1 < len(Parameters):
            cmd += '; '

    # dispatch the iC command and return the result
    return _device.DispatchCommand(cmd)





def is_syntax_check_mode():
    """Returns true only if the script runs in Syntax-Check Mode."""

    # use global variable _device
    global _device
 
    return _device.inSyntaxCheckMode()




def is_stop_scripting():
    """Returns true if the user has pressed the Stop button.

    Tasks that may take a while should check this flag regularly to ensure that
    processing of the script can be interrupted by the user.

    """

    return _device.getStopScripting()




def is_paused(wait_until_not_paused):
    """Returns true if processing the script has been paused.

    This is the Python-equivalent of IcontrolView.isPaused(), and it returns
    true if Scripting has been paused (either by the user pressing 'Pause' 
    button or by enabling Options/AutoPause) and false if the Script is being 
    processed. The method returns immediately if Scripting has been stopped 
    (because the user pressed the 'Stop' button.
    
    When wait_until_not_paused is set to true, the method waits until the 
    Script is being processed (Scripting has been resumed if it was paused), 
    and in this case, always returns false.

    Tasks that may take a while should check this flag regularly to ensure that
    processing of the script can be interrupted by the user.

    """

    return _device.isPaused(wait_until_not_paused)


def is_no_communication_mode():
    """Returns true if the Instrument is set to No-Communication Mode.

    In No-Communication mode, no commands are sent to Instruments. If queries are
    made, the result is undefined, and might even be null.

    """

    return _device.inNoCommunicationMode()



# --- "main"

# probably not required
if __name__ == '__main__':
    
    # add path to Project Directory
    sys.path = [project_directory, iC_directory, iC_directory+'Python_Scripts/'] + sys.path
    
    print("== executing Startup.py ==")
    print("In Startup.main these names are known")
    print(dir())

