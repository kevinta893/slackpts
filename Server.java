import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.BindException;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


/**
 * The main server program.
 * @author Kevin
 *
 */
public class Server {

	public static final String CURRENCY_NAME = "peeg";
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

			println("Retriving user database...");
			println("Found " + UserDB.getInstance().getUserCount() + " users in database.");
			UserMapping.getInstance().getCount();				//intialize the mapping

			//if there's no users in the database, warn user to add some
			if (UserDB.getInstance().getUserCount() == 0){
				println("Warning! Server has no users registered in database.");
			}

			//create the system log
			println("Creating system log...");
			log = new Logger("log" + logDate() + ".txt");
			println("Log started in file: " + log.getFileName());

			System.out.println("\n=====================================================");
			println("Starting server on port " + port + "...");




			//create the listen socket
			try {
				listenSock = new ServerSocket(port);

			} catch (BindException e){
				System.err.println(e.getMessage());
				System.err.println("Cannot setup server! Quitting...");
				System.exit(-1);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.err.println("Cannot setup server! Quitting...");
				System.exit(-1);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Cannot setup server! Quitting...");
				System.exit(-1);
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
	private static void messageSlack(String sendURL, String textPost, String channel){
		
		//construct the JSON message
		String message = "payload={\"text\":\"" + textPost + "\", \"channel\":\"#" + channel + "\"}";
		
		//System.out.println(message);
		try {
			
			CloseableHttpClient slackServer = HttpClients.createDefault();
			
			HttpPost slackMessage = new HttpPost(sendURL);
			
			slackMessage.setHeader("User-Agent", "Slack Points Server");
			slackMessage.setHeader("content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			slackMessage.setEntity(new StringEntity(message));

			HttpResponse response  = slackServer.execute(slackMessage);

			
			//print reply from slack server if any.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			response.getEntity().writeTo(baos);
			Server.getInstance().println("<Slack Server>: " + (new String(baos.toByteArray())));
			
			slackServer.close();
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
				System.out.println(complete);
				String payload = complete.substring(complete.indexOf("token="));

				//with complete request, find the command sent
				String channelName = getTagArg(payload, CHANNEL_NAME_TAG);
				String channelID = getTagArg(payload, CHANNEL_ID_TAG);
				String userID = getTagArg(payload, USER_ID_TAG);
				String userName = getTagArg(payload, USER_NAME_TAG);
				String command = getTagArg(payload, CMD_TAG);
				String[] args = getTextArgs(payload);								//arguements of the command


				//print command out
				String fullCommand = command;
				for (int i =0; i < args.length; i++){
					fullCommand = fullCommand + " " + args[i];
				}
				println(userName + " issued command: \t"+fullCommand + "\n");
				
				

				if (command.equals(TIP_CMD)){
					//increment command sent, increment points


					//increment only if there is a user that exists.
					if (args.length == 1){

						String targetID = UserMapping.getInstance().getID(args[0]);
						if (targetID != null){
							if (UserDB.hasUser(targetID)){
								UserDB.increment(targetID, INCREMENT);
								log.writeLine(userName + " gave " + args[0] + " " + INCREMENT + CURRENCY_NAME);
							}
						}
						else{
							//TODO no mapping found, return error
							//"User not recognized! Did that user get an account with the bank of Slack? Get that user to enter the command /register to sign up. If you already have an account, but changed your name please enter the command /register ASAP."
						}
					}



				}
				else if (command.equals(CHECK_CMD)){
					//check command sent, return current points.

					if (args.length == 1){

						String targetID = UserMapping.getInstance().getID(args[0]);

						if(targetID != null){
							if (UserDB.hasUser(targetID)){
								User check = UserDB.getUser(args[0]);
								String humanName = UserMapping.getInstance().getName(targetID);
								//messageSlack("", humanName + " has " + check.getPts() + CURRENCY_NAME + ".", channelName);
							}
						}
						else{
							//TODO no such user exists, report back
							//messageSlack("","No such user exits", channelName);
						}

					}

				}
				else if (command.equals(REGISTER_CMD)){
					//register command sent, update id of new user.

					if (UserMapping.getInstance().registerPair(userName, userID)){
						UserMapping.saveAll();
						log.writeLine("Added " + userName + " as new ID: " + userID);
						
						//create new user in database
						UserDB.registerUser(userID);
					}
					else{
						String oldName = UserMapping.getInstance().getName(userID);
						if (UserMapping.getInstance().updateName(oldName, userName)){
							//successful name update.
							
							UserMapping.saveAll();
							log.writeLine("Updated " + oldName + " -> " + userName);
							//TODO
						}
					}
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
					println("Recieved connection from: " + client.getInetAddress().toString() + ":" + client.getPort());

					//handle request in new thread
					Thread clientHandler = new Thread(new RequestHandler(client));
					clientHandler.run();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	
	private static SimpleDateFormat consoleDate = new SimpleDateFormat("HH:mm:ss");
	
	/**
	 * Prints a line in the server and in the log. Time stamped
	 */
	public void printRecord(String message){
		 System.out.println("[" + (consoleDate.format(new Date(System.currentTimeMillis()))) + "]: " + message);
		 log.writeLine(message);
	}
	
	/**
	 * Prints a line in the server. Time stamped
	 */
	public void println(String message){
		 System.out.println("[" + (consoleDate.format(new Date(System.currentTimeMillis()))) + "]: " + message);
	}
	
	
	
	
	public static void main(String[] args){
		Server.messageSlack("https://awktocreations.slack.com/services/hooks/incoming-webhook?token=sr9pEgsE2mZpvQlSMtMmcOXv", "I wonder if I can use emoticons like everyone else... :grin:", "general");

	}

}
