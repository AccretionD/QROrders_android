package accretiond.android.chef.helper;

import java.io.IOException;
import java.util.List;

import accretiond.android.chef.util.ArrayUtil;


/**
 * Helper functions for running processes.
 * @author nolan
 *
 */
public class RuntimeHelper {
    
	/**
	 * Exec the arguments, using root if necessary.
	 * @param args
	 */
	public static Process exec(List<String> args) throws IOException {
		// since JellyBean, sudo is required to read other apps' logs

		return Runtime.getRuntime().exec(ArrayUtil.toArray(args, String.class));
	}
	
	public static void destroy(Process process) {
	    // if we're in JellyBean, then we need to kill the process as root, which requires all this
	    // extra UnixProcess logic
	      process.destroy();

	}
	
}