import java.io.IOException;
import java.net.ServerSocket;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * The main server program.
 * @author Kevin
 *
 */
public class Server {

	public static final String PTS_NAME = "peeg";
	public static final int INCREMENT = 1;
	
	
	private static final int SERVER_PORT = 80;
	
	private static ServerSocket listenSock;
	
	private static Logger log;
	
	
	private static boolean stop = false;
	
	public static void main(String[] args){
		System.out.println("Starting server...");
		System.out.println("Retriving user database...");
		System.out.println("Found " + UserDB.getInstance().getUserCount() + " users in database.");
		
		//if there's no users in the database, warn user to add some
		if (UserDB.getInstance().getUserCount() == 0){
			System.out.println("Warning! Server has no users registered in database. Add some users to: " + UserDB.DB_FILE_NAME);
		}
		
		//create the system log
		System.out.println("Creating system log...");
		log = new Logger("log" + logDate() + ".txt");
		
		
		//create the listen socket and start main loop
		try {
			listenSock = new ServerSocket(SERVER_PORT);
			
			
			
			listenSock.accept();

			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * The socket accepting thread that accepts all connections and attempts
	 * to service them.
	 * @author Kevin
	 *
	 */
	private class SocketAccepter implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	/**
	 * The request handler created when a new request is being made.
	 * @author Kevin
	 *
	 */
	private class RequestHandler implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private static String logDate(){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(new Date(System.currentTimeMillis()));
	}
}
