package debug;

public final class Debug {
	protected static boolean debug_flag = true;
	
	public static boolean isDebug(){
		return debug_flag;
	}
	
	public static void printDebug(String message){
		System.out.println("[DEBUG]: "+message);
	}
}
