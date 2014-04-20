import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;


/**
 * The main server program.
 * @author Kevin
 *
 */
public class Server {

	public static final String PTS_NAME = "peeg";
	public static final int INCREMENT = 1;

	//payload tags in Slack POST
	public static final String PAYLOAD_START = "token=";
	public static final String CMD_TAG = "command=";
	public static final String TEXT_TAG = "text=";
	public static final String CHANNEL_NAME_TAG = "channel_name=";
	public static final String CHANNEL_ID_TAG = "channel_id=";
	public static final String USER_NAME_TAG = "user_name=";
	public static final String USER_ID_TAG = "user_id=";


	//custom commands.
	public static final String TIP_CMD = "%2Ftip";
	public static final String CHECK_CMD = "%2Fcheck";
	public static final String REGISTER_CMD = "%2Fregister";

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

			System.out.println("Retriving user database...");
			System.out.println("Found " + UserDB.getInstance().getUserCount() + " users in database.");

			//if there's no users in the database, warn user to add some
			if (UserDB.getInstance().getUserCount() == 0){
				System.out.println("Warning! Server has no users registered in database. Add some users to: " + UserDB.DB_FILE_NAME);
			}

			//create the system log
			System.out.println("Creating system log...");
			log = new Logger("log" + logDate() + ".txt");
			System.out.println("Log started in file: " + log.getFileName());

			System.out.println("\n=====================================================");
			System.out.println("Starting server on port " + port + "...");

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
	 * Posts a message on slack on the specified channel
	 * @param message
	 * @param channel
	 */
	private static void messageSlack(String sendURL, String message, String channel){
		try {
			URL url = new URL(sendURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			//construct the packet
			connection.setDoOutput(true); 
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/plain");
			connection.setRequestProperty("charset", "utf-8");
			
			
			
			
			//OutputStream outPayload = new OutputStream();
			
			connection.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
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


				//find the command field of the POST request

				//gather all lines
				String complete = "";

				String nextLine = buff.readLine();
				while (nextLine != null){
					complete = complete + nextLine;
					nextLine = buff.readLine();
				}

				String payload = complete.substring(complete.indexOf("token="));

				//with complete request, find the command sent
				String channelName = getTagArg(payload, CHANNEL_NAME_TAG);
				String channelID = getTagArg(payload, CHANNEL_ID_TAG);
				String userID = getTagArg(payload, USER_ID_TAG);
				String userName = getTagArg(payload, USER_NAME_TAG);
				String command = getTagArg(payload, CMD_TAG);
				String[] args = getTextArgs(payload);


				System.out.println(payload);

				if (command.equals(TIP_CMD)){
					//increment command sent, increment points


					//increment only if there is a user that exists.
					if (args.length == 1){
						if (UserDB.hasUser(userName)){
							UserDB.increment(userName, INCREMENT);
						}
					}



				}
				else if (command.equals(TIP_CMD)){
					//check command sent, return current points.
					

				}
				else{
					//invalid command
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			finally{
				//always close the client
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	/**
	 * Gets the command argument of the Slack slash command of the
	 * specified tag in the request.
	 * Returned string is pre-trimmed.
	 * @param postRequest The request to parse
	 * @param tag The tag to extract the value of
	 * @return Empty string if tag is not found, Value of the tag otherwise.
	 */
	private static String getTagArg(String payload, String tag){

		int index = payload.indexOf(tag);

		if (index >= 0){
			String arg = payload.substring(index + tag.length(),payload.indexOf("&", index));
			return arg.trim();
		}

		return "";
	}

	/**
	 * Gets all the arguments seperated by spaces (the '+' symbol).
	 * Returns a String array of each argument in the order they are found in.
	 * 
	 * If there are no arguments, an empty array is returned.
	 * If the string does not contain the "
	 * @param payload
	 * @return
	 */
	private static String[] getTextArgs(String payload){
		int index = payload.indexOf(TEXT_TAG);

		if (index >=0){
			String raw = payload.substring(index + TEXT_TAG.length());

			return raw.split("\\+");
		}

		return (new String[0]);
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
	/*
	public static void main(String[] args){
		String[] derp= Server.getTextArgs("");
		int i =0;
		i++;
	}
	 */
}
