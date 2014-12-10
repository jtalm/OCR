package debug;

/**
 * Class implemented for code debugging	
 * 
 */
public final class Debug {
	
	/** The debug_flag. */
	protected static boolean debug_flag = true;
	
	/**
	 * Checks if is debug.
	 *
	 * @return Debug mode status
	 */
	public static boolean isDebug(){
		return debug_flag;
		
	}
	
	/**
	 * Simple layout for better understanding of debug message.
	 *
	 * @param message message to be printed
	 */
	public static void printDebug(String message){
		if(debug_flag)
			System.out.println("[DEBUG]: "+message);
		
	}
}
