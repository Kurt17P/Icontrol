package icontrol.scriptsincolour.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Brian Remedios
 */
public interface Keywords {

	Set<String> IControl = new HashSet<String>(Arrays.asList(
			"MAKE", "GPIB", "URL", "TMCTL"
			));
	
	Set<String> Python = new HashSet<String>(Arrays.asList(
			"and","del","from","not","while","as","elif","global","or",
			"with","assert","else","if","pass","yield","break",
			"except","import","print","class","exec","in","raise",
			"continue","finally","is","return", "def","for","lambda","try"
			));
}
