import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


/**
 * The main server program.
 * @author Kevin
 *
 */
public class Server {

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
	 * Starts the server, binds all resources. If an instance has already is or has been
	 * running, then nothing happens.
	 */
	public void startServer(){

		//run only if the current thread is not created. Single instance
		if (acceptThread == null){

			println("Getting configuration settings...");
			Config.getInstance().getCount();
			
			println("Retriving user database...");
			println("Found " + UserDB.getInstance().getUserCount() + " users in database.");
			UserMapping.getInstance().getCount();				//Initialize the mapping

			//if there's no users in the database, warn user to add some
			if (UserDB.getInstance().getUserCount() == 0){
				println("Warning! Server has no users registered in database.");
			}

			//create the system log
			println("Creating system log...");
			log = new Logger("log" + logDate() + ".txt");
			println("Log started in file: " + log.getFileName());

			
			println("Starting server on port " + Config.getPort() + "...");
			System.out.println("\n=====================================================");



			//create the listen socket
			try {
				listenSock = new ServerSocket(Config.getPort());
				println("Server now running.");
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
	private static void messageSlack(String textPost, String channel){
		System.out.println(textPost);
		
		//construct the JSON message
		String message = "payload={\"text\":\"`" + textPost + "`\", \"channel\":\"#" + channel + "\", \"username\": \"" + Config.getBotName() + "\"}";

		//System.out.println(message);
		try {

			CloseableHttpClient slackServer = HttpClients.createDefault();

			HttpPost slackMessage = new HttpPost(Config.getSlackWebHook());

			slackMessage.setHeader("User-Agent", "Slack Points Server");
			slackMessage.setHeader("content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			slackMessage.setEntity(new StringEntity(message));

			HttpResponse response  = slackServer.execute(slackMessage);


			//print reply from slack server if any.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			response.getEntity().writeTo(baos);
			Server.getInstance().println("<Slack Server>: " + (new String(baos.toByteArray())));

			slackServer.close();
		} catch (UnknownHostException e){
			printException(e);
		} catch (IOException e) {
			printException(e);
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
				//String channelID = getTagArg(payload, CHANNEL_ID_TAG);
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
								log.writeLine(userName + " gave " + args[0] + " " + INCREMENT + Config.getCurrencyName());
								messageSlack(userName + " gave " + args[0] + " " + INCREMENT + Config.getCurrencyName(), channelName);
							}
						}
						else{
							//no mapping found, return error
							messageSlack("I do not recognize who " + args[0] + " is! Did that user get an account with the bank of Slack? Get that user to enter the command /register to sign up. If you already have an account, but changed your name recently please enter the command /register ASAP.", channelName);
						}
					}



				}
				else if (command.equals(CHECK_CMD)){
					//check command sent, return current points.

					if ((args.length == 1) && (args[0].equals("") == false)){


						//get the id of the user
						String targetID = UserMapping.getInstance().getID(args[0]);

						if(targetID != null){
							if (UserDB.hasUser(targetID)){
								//user exists, return their count.
								User check = UserDB.getUser(targetID);
								String humanName = UserMapping.getInstance().getName(targetID);
								messageSlack(humanName + " has " + check.getPts() + Config.getCurrencyName() + ".", channelName);
							}
						}
						else{
							//no such user exists, report back
							messageSlack("No such user named " + userName + " exists. Have they registered yet?", channelName);
						}

					}
					else if ((args.length == 1) && (args[0].equals("") == true)){

						//get the id of the user
						String targetID = UserMapping.getInstance().getID(userName);
						
						if(targetID != null){
							if (UserDB.hasUser(targetID)){
								//user exists, return their count.
								User check = UserDB.getUser(targetID);
								String humanName = UserMapping.getInstance().getName(targetID);
								messageSlack(humanName + " has " + check.getPts() + Config.getCurrencyName() + ".", channelName);
							}
						}
						else{
							//no such user exists, report back
							messageSlack("I cannot find your record " + userName + ". Have you registered yet?", channelName);
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
						UserDB.saveAll();


						messageSlack("Welcome "+ userName + "! You have " + UserDB.getUser(userID).getPts() 
								+ Config.getCurrencyName() + ". Earn more by getting tips from friends.", channelName);
					}
					else{
						String oldName = UserMapping.getInstance().getName(userID);
						if (UserMapping.getInstance().updateName(oldName, userName)){
							//successful name update.

							UserMapping.saveAll();
							
							log.writeLine("Updated " + oldName + " -> " + userName);

							messageSlack("Gotcha! I'll remember you as " + userName + " from now on.", channelName);
						}
					}
				}
				else{
					//invalid command
					messageSlack("Sorry I don't understand that command. :frown:", channelName);
				}

			} catch (IOException e) {
				printException(e);
			}
			finally{
				//always close the client
				try {
					client.close();
				} catch (IOException e) {
					printException(e);
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
					printException(e);
				}
			}

		}

	}


	private static SimpleDateFormat consoleDate = new SimpleDateFormat("HH:mm:ss");

	private static String timeStamp(){
		return "[" + (consoleDate.format(new Date(System.currentTimeMillis()))) + "]: ";
	}
	
	/**
	 * Prints out an exception when it occurs. Only the stack
	 * trace is printed to the log. But an occurance is shown
	 * in both the console and the log.
	 * @param e
	 */
	public static void printException(Exception e){
		String message = timeStamp() + "Exception occurred. " + UnknownHostException.class.getName() + ": " + e.getMessage() +
				" --Please see log for stack trace--";
		System.err.println(message);
		
		
		//Convert stack trace into string and print to log
		StringWriter error = new StringWriter();
		e.printStackTrace(new PrintWriter(error));
		String stackTrace = error.toString();
		
		log.writeLine(message + "\n" + stackTrace);
	}
	
	/**
	 * Prints a line in the server and in the log. Time stamped
	 */
	public static void printRecord(String message){
		System.out.println(timeStamp() + message);
		log.writeLine(message);
	}

	/**
	 * Prints a line in the server. Time stamped
	 */
	public void println(String message){
		System.out.println(timeStamp() + message);
	}

	


	public static void main(String[] args){
		//Server.messageSlack("Like this?", "git-blog");

	}

}
