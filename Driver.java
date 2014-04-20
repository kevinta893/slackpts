import java.util.Scanner;


public class Driver {

	private static final int MIN_PORT = 1000;
	private static final int MAX_PORT = 65535;
	
	public static void main(String[] args){
		
		if (args.length == 1){
			
			int port = -1;
			
			//scan input
			Scanner in = new Scanner(args[0]);
			if (in.hasNextInt()){
				int temp = in.nextInt();
				in.close();
				if ((temp >= MIN_PORT ) && (temp <= MAX_PORT)){
					//valid port, use it
					port = temp;
					Server.getInstance().startServer(port);
				}
				else{
					System.out.printf("Specified port must be between %d and %d inclusive.\n", MIN_PORT, MAX_PORT);
				}
			}
			else{
				//invalid port number, fail.
				System.out.printf("Specified port must be between %d and %d inclusive.\n", MIN_PORT, MAX_PORT);
			}
			in.close();
		}
		else{
			Server.getInstance().startServer();
		}
		
	}
}
