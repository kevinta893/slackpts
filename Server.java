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
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

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
	public static final String TIP_CMD = "/tip";
	public static final String CHECK_CMD = "/check";
	public static final String REGISTER_CMD = "/register";



	private static ServerSocket listenSock;

	
	
	
	//loggers
	private static volatile Logger log;
	private static volatile Logger errorLog;
	private static final String LOG_FILENAME = "log";
	private static final String ERROR_LOG_FILENAME = "errorLog";
	

	//threads
	private static Thread acceptThread;
	private static Thread maintanenceThread;					//see Maintenence Thread for maintentence interval

	


	private static boolean running = true;						//server running or not
	private static boolean silent = false;						//silent mode prevents server from posting to slack


	//private static Date startDate = new Date(System.currentTimeMillis());

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

			println("Retriving user database...");
			println("Found " + UserDB.getUserCount() + " users in database.");
			UserMapping.getCount();				//Initialize the mapping

			//if there's no users in the database, warn user to add some
			if (UserDB.getUserCount() == 0){
				println("Warning! Server has no users registered in database.");
			}

			//create the system log
			println("Creating system logs...");
			log = new Logger(LOG_FILENAME);
			errorLog = new Logger(ERROR_LOG_FILENAME);
			println("Log started in file: " + log.getFileName());


			println("Starting server on port " + Config.getPort() + "...");
			System.out.println("\n=====================================================");



			//create the listen socket
			try {
				listenSock = new ServerSocket(Config.getPort());

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
			acceptThread.start();

			//startTime = new Date(System.currentTimeMillis());							//server actually starts running here.
			
			maintanenceThread = new Thread(new MaintenanceThread());
			maintanenceThread.start();
		}


		//move this thread into command line service
		
		println("Server now running. Enter commands to maintain.");
		commandLine();
	}


	/**
	 * Server commandline that runs commands to manage the
	 * server.
	 */
	private static void commandLine(){

		Scanner in = new Scanner(System.in);

		String nextInput;
		while (running == true){
			nextInput = in.nextLine();

			String[] commandArgs = nextInput.split(" ", 2);

			String command = commandArgs[0].trim();
			String args = (commandArgs.length == 2)? commandArgs[1] : "";


			if (command.equals("/stop")){
				//stop server command
				println("Saving all information and stopping server...");
				saveAllFiles();
				running = false;

			}
			else if(command.equals("/save")){
				//save all current information immediately
				println("Saving all logs and user information...");
				saveAllFiles();
				println("All files have been saved.");

			}
			else if(command.equals("/message")){
				//sends a message onto slack given the channel and message respectively
				String[] split = args.split(" ", 2);

				//must be exactly two args.
				if(split.length == 2){
					println("Sending message...");
					messageSlack(split[1], split[0]);
				}
				else{
					println("To use /message, enter channel name then the message to send.");
				}

			}
			else if (command.equals("/silent")){
				//toggle silent mode

				if (silent == false){
					//turn silent on
					silent = true;
					printRecord("Slient mode is ON, no messages will be posted to Slack.");
				}
				else if (silent == true){
					//turn silent of
					silent = false;
					printRecord("Slient mode is OFF, messages will be posted to Slack.");
				}
			}
			else{
				println("Invalid command.");
			}
		}


		//shutdown server.
		in.close();
		System.exit(0);
	}


	


	/**
	 * Posts a message on slack on the specified channel
	 * @param message
	 * @param channel
	 */
	private static void messageSlack(String textPost, String channel){
		//System.out.println(textPost);

		//convert all new lines into proper characters
		textPost = textPost.replaceAll("\n", "`\\\\n`");
		
		if(silent == false){
			String message;
			//construct the JSON message
			if (channel != null){
				message = "payload={\"text\":\"`" + textPost + "`\", \"channel\":\"#" + channel + "\", \"username\": \"" + Config.getBotName() + "\"}";
			}
			else{
				message = "payload={\"text\":\"`" + textPost + "`\", \"username\": \"" + Config.getBotName() + "\"}";
			}


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
				println("<Slack Server>: " + (new String(baos.toByteArray())));

				slackServer.close();
			} catch (UnknownHostException e){
				printException(e);
			} catch (IOException e) {
				printException(e);
			}
		}
	}


	//====================================================================================================
	//The request handler


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

				if (complete.indexOf("token=") >= 0){
					//proper request.

					String payload = complete.substring(complete.indexOf("token="));

					//convert payload into proper text
					
					//with complete request, find the command sent
					String channelName = getTagArg(payload, CHANNEL_NAME_TAG);
					//String channelID = getTagArg(payload, CHANNEL_ID_TAG);
					String userID = getTagArg(payload, USER_ID_TAG);
					String userName = getTagArg(payload, USER_NAME_TAG);
					String command = getTagArg(payload, CMD_TAG).replaceAll("%2F", "/");			//replace "%2f" with forward slash
					String[] args = getTextArgs(payload);								//arguements of the command


					//print command out
					String fullCommand = command;
					for (int i =0; i < args.length; i++){
						fullCommand = fullCommand + " " + args[i];
					}
					printRecord("<SLACK_CMD> " + userName + " issued command: \t"+fullCommand );
					


					if (command.equals(TIP_CMD)){
						//increment command sent, increment points


						//increment only if there is a user that exists.
						if (args.length == 1){

							String targetID = UserMapping.getID(args[0]);

							if (targetID != null){
								if (UserDB.hasUser(targetID)){


									if (targetID.equals(userID) == false){
										//not self, do tipping

										UserDB.increment(targetID, INCREMENT);
										log.writeLine(userName + " gave " + args[0] + INCREMENT + Config.getCurrencyName());
										messageSlack(userName + " gave " + args[0] + INCREMENT + Config.getCurrencyName(), channelName);
									}
									else{
										//error, cannot tip self.
										messageSlack("You cannot tip yourself " + userName + "!", channelName);
									}
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
							String targetID = UserMapping.getID(args[0]);

							if(targetID != null){
								if (UserDB.hasUser(targetID)){
									//user exists, return their count.
									User check = UserDB.getUser(targetID);
									String humanName = UserMapping.getName(targetID);
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
							String targetID = UserMapping.getID(userName);

							if(targetID != null){
								if (UserDB.hasUser(targetID)){
									//user exists, return their count.
									User check = UserDB.getUser(targetID);
									String humanName = UserMapping.getName(targetID);
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

						if (UserMapping.registerPair(userName, userID)){
							UserMapping.saveAll();

							log.writeLine("Added " + userName + " as new ID: " + userID);

							//create new user in database
							UserDB.registerUser(userID);
							UserDB.saveAll();


							messageSlack("Welcome "+ userName + "! You have " + UserDB.getUser(userID).getPts() 
									+ Config.getCurrencyName() + ". Earn more by getting tips from friends.", channelName);
						}
						else{
							String oldName = UserMapping.getName(userID);
							if (UserMapping.updateName(oldName, userName)){
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
				}
			} catch (IOException e) {
				printException(e);
			} catch(Exception e){
				printException(e);
			}




			//always close the client
			try {
				client.close();
			} catch (IOException e) {
				printException(e);
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


	//===========================================================================================================


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
					clientHandler.start();
				} catch (IOException e) {
					printException(e);
				}
			}


			try {
				listenSock.close();
			} catch (IOException e) {
				printException(e);
			}

		}

	}


	private final class MaintenanceThread implements Runnable{
		
		private static final int MAINTENANCE_TIME = 300000; 		//every 5 minutes, run maintenance thread.
		
		
		//the time to reset the logs.  Write new log at about 12:05 am. (aka 0:05)
		private static final int HOUR_WINDOW = 0;
		private static final int MINUTE_WINDOW_MIN = 5;
		private static final int MINUTE_WINDOW_MAX = 10;
		
		
		public MaintenanceThread(){
			
		}
		
		
		@Override
		public void run() {

			try {
				Thread.sleep(MAINTENANCE_TIME);
				
				//if new day, swap out logs
				Calendar today = Calendar.getInstance();
				if ((today.get(Calendar.HOUR_OF_DAY) == HOUR_WINDOW) && 
						(today.get(Calendar.MINUTE) >= MINUTE_WINDOW_MIN) && (today.get(Calendar.MINUTE) <= MINUTE_WINDOW_MAX)){
					
					printRecord("--> Maintenance thread now saving new log for the day.");
					
					
					log = new Logger(LOG_FILENAME);
					errorLog = new Logger(ERROR_LOG_FILENAME);
				}
				
				
				//maintain server here
				saveAllFiles();
				printRecord("--> Maintenance Thread saved all information");
			} catch (InterruptedException e) {
				printException(e);
			}
		}

	}


	/**
	 * Saves all critical files.
	 */
	private static void saveAllFiles(){
		log.saveLog();
		errorLog.saveLog();
		UserDB.saveAll();
		UserMapping.saveAll();
	}



	//================================================================
	//Reporting methods and console print

	private static SimpleDateFormat consoleDate = new SimpleDateFormat("HH:mm:ss");

	private static String timeStamp(){
		return "[" + (consoleDate.format(new Date(System.currentTimeMillis()))) + "]: ";
	}

	/**
	 * Prints out an exception when it occurs. Only the stack
	 * trace is printed to the error log. But an occurance is shown
	 * in both the console and the log.
	 * 
	 * @param e
	 */
	public static void printException(Exception e){
		String message = timeStamp() + "Exception occurred. " + UnknownHostException.class.getName() + ": " + e.getMessage();
		printRecord(message + " --Please see error log for stack trace--");

		//Convert stack trace into string and print to error log
		StringWriter error = new StringWriter();
		e.printStackTrace(new PrintWriter(error));
		String stackTrace = error.toString();

		errorLog.writeLine(message + "\n" + stackTrace);

		messageSlack("Whoops! I ran into an exception. See my log.", null);
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
	public static void println(String message){
		System.out.println(timeStamp() + message);
	}



	
	/*
	public static void main(String[] args){
		Server.messageSlack("New \nLine", null);
		
		String derp = "f%2ff";
		
		String convert = derp.replaceAll("%2f", "/");
		
		System.out.print(convert);

	}
	 */
}
