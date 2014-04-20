import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
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
	
	
	private static final int DEFAULT_PORT = 48567;
	private static ServerSocket listenSock;
	
	private static volatile Logger log;
	
	private static Thread acceptThread;
	private static boolean running = true;
	
	
	private static Server instance;
	
	public static Server getInstance(){
		if (instance == null){
			instance = new Server();
		}
		return instance;
	}
	
	
	private Server(){}
	
	/**
	 * Starts the server on the DEFAULT_PORT port.
	 */
	public void startServer(){
		startServer(DEFAULT_PORT);
	}
	
	/**
	 * Starts the server, binds all resourc. If an instance has already is or has been
	 * running, then nothing happens.
	 * @param port The server point to use while running
	 */
	public void startServer(int port){
		
		//run only if the current thread is not created. Single instance
		if (acceptThread == null){
			System.out.println("Starting server on port " + port + "...");
			System.out.println("Retriving user database...");
			System.out.println("Found " + UserDB.getInstance().getUserCount() + " users in database.");
			
			//if there's no users in the database, warn user to add some
			if (UserDB.getInstance().getUserCount() == 0){
				System.out.println("Warning! Server has no users registered in database. Add some users to: " + UserDB.DB_FILE_NAME);
			}
			
			//create the system log
			System.out.println("Creating system log...");
			log = new Logger("log" + logDate() + ".txt");
			
			
			//create the listen socket
			try {
				listenSock = new ServerSocket(port);
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//create the handling thread and run it.
			acceptThread = new Thread(new SocketAccepter());
			acceptThread.run();
		}
	}
	
	
	/**
	 * Gets the current log date and returns a string.
	 * These strings only differ by day.
	 * @return
	 */
	private static String logDate(){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(new Date(System.currentTimeMillis()));
	}
	
	
	
	/**
	 * The request handler created when a new request is being made.
	 * @author Kevin
	 *
	 */
	private final class RequestHandler implements Runnable{

		Socket client;
		
		public RequestHandler(Socket client){
			this.client = client;
		}
		
		@Override
		public void run() {
			try {
				BufferedReader buff = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
				
				String nextLine = buff.readLine();
				while (nextLine != null){
					System.out.println(nextLine);
					nextLine = buff.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	
	/**
	 * The socket accepting thread that accepts all connections and attempts
	 * to service them.
	 * @author Kevin
	 *
	 */
	private final class SocketAccepter implements Runnable{

		
		@Override
		public void run() {
			
			while( running == true ){
				try {
					Socket client = listenSock.accept();
					
					//got a connection
					System.out.println("Recieved connection from: " + client.getInetAddress().toString() + "@ port: " + client.getPort());
					
					//handle request in new thread
					Thread clientHandler = new Thread(new RequestHandler(client));
					clientHandler.run();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
}
