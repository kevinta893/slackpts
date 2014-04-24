package server;
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
import java.util.LinkedList;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import command.Command;
import command.Command.CmdResult;
import command.RequestStruct;


/**
 * The main server program.
 * @author Kevin
 *
 */
public class Server {

	public static final int MIN_PORT = 1000;
	public static final int MAX_PORT = 65535;
	
	
	public static final int INCREMENT = 1;


	private LinkedList<Command> commands = new LinkedList<Command>();

	//custom commands.
	public static final String TIP_CMD = "/tip";
	public static final String CHECK_CMD = "/check";
	public static final String REGISTER_CMD = "/register";
	public static final String SLAP_CMD = "/slap";
	


	private ServerSocket listenSock;


	//loggers
	private volatile Logger log;
	private volatile Logger errorLog;

	private static final String LOG_FOLDER = "logs";
	private static final String ERROR_LOG_FOLDER = LOG_FOLDER;
	private static final String LOG_FILENAME = "log";
	private static final String ERROR_LOG_FILENAME = "errorLog";


	//threads
	private Thread acceptThread;
	private Thread maintanenceThread;					//see Maintenence Thread for maintentence interval




	private boolean running = true;						//server running or not
	private boolean silent = false;						//silent mode prevents server from posting to slack


	//private static Date startDate = new Date(System.currentTimeMillis());

	public Server(int port){
		//check port arg
		if ((port< MIN_PORT) && (port > MAX_PORT)){
			throw new IllegalArgumentException("Invalid port given. Cannot create server with port " + port);
		}
		
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
		
	}


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
			log = new Logger(LOG_FOLDER, LOG_FILENAME);
			errorLog = new Logger(ERROR_LOG_FOLDER, ERROR_LOG_FILENAME);
			println("Log started in file: " + log.getFileName());


			println("Starting server on port " + Config.getPort() + "...");
			System.out.println("\n=====================================================");




			//create the handling thread and run it.
			acceptThread = new Thread(new SocketAccepter());
			acceptThread.start();

			//startTime = new Date(System.currentTimeMillis());							//server actually starts running here.

			maintanenceThread = new Thread(new MaintenanceThread());
			maintanenceThread.start();
			
			
			//move this thread into command line service
			println("Server now running. Enter commands to maintain.");
			commandLine();
		}
		else{
			println("Server already running.");
		}

		

		
	}


	/**
	 * Server commandline that runs commands to manage the
	 * server.
	 */
	private void commandLine(){

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
	private void messageSlack(String textPost, String channel){
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
					RequestStruct req = RequestStruct.createInstance(payload);


					//print command out
					String fullCommand = req.getCommand();
					String[] args = req.getArgs();
					for (int i =0; i < args.length; i++){
						fullCommand = fullCommand + " " + args[i];
					}
					printRecord("<SLACK_CMD> " + req.getUserName() + " issued command: \t" + fullCommand);

					
					boolean didSomething = false;
					for (Command com : commands){
						
						//go through each command and see if they apply
						if (com.isCommand(req.getCommand())){
							didSomething = true;
							CmdResult cmdResult = com.doRequest(req);
							
							if (cmdResult == CmdResult.SUCCESS){
								//request process is successful. report back
								messageSlack(com.getReturnMessage(), com.getReturnChannel());
								printRecord(com.getLogMessage());
							}
							else if (cmdResult == CmdResult.FAILED){
								//request failed, requires error to be posted
								messageSlack(com.getReturnMessage(), com.getReturnChannel());
								printError(com.getErrorMessage());
							}
							else if (cmdResult == CmdResult.SUCCESS_SILENT){
								//success, do not report back to slack
								printRecord(com.getErrorMessage());
							}
							else if (cmdResult == CmdResult.FAILED_SILENT){
								//failed, do not report back to slack
								printError(com.getErrorMessage());
							}
							else if (cmdResult == CmdResult.INVALID){ 
								//do nothing
							}
								
						}
					}
					
					if (didSomething == false){
						messageSlack("Sorry I don't understand that command. :frown:", req.getChannelName());
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
	 * Adds a command to this server's request handler.
	 * @param cmd
	 */
	public void registerCommand(Command cmd){
		commands.add(cmd);
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
		private static final int MINUTE_WINDOW_MAX = 11;


		public MaintenanceThread(){

		}


		@Override
		public void run() {


			while (running == true){
				try {
					Thread.sleep(MAINTENANCE_TIME);

					//if new day, swap out logs
					Calendar today = Calendar.getInstance();
					if ((today.get(Calendar.HOUR_OF_DAY) == HOUR_WINDOW) && 
							(today.get(Calendar.MINUTE) >= MINUTE_WINDOW_MIN) && (today.get(Calendar.MINUTE) <= MINUTE_WINDOW_MAX)){

						printRecord("--> Maintenance thread now saving new log for the day.");

						//save old logs
						log.saveLog();
						errorLog.saveLog();

						//swtich to new ones.
						log = new Logger(LOG_FOLDER, LOG_FILENAME);
						errorLog = new Logger(ERROR_LOG_FOLDER, ERROR_LOG_FILENAME);

						//save both logs
						log.saveLog();
						errorLog.saveLog();
					}


					//maintain server here
					saveAllFiles();
					printRecord("--> Maintenance Thread saved all information");
				} catch (InterruptedException e) {
					printException(e);
				}
			}
		}

	}


	/**
	 * Saves all critical files.
	 */
	private void saveAllFiles(){
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
	public void printException(Exception e){
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
	 * Prints an error to both the error log and the
	 * err print stream.
	 * @param message
	 */
	public void printError(String message){
		String stamped = timeStamp() + " " + message;
		errorLog.writeLine(stamped);
		System.err.println(stamped);
		
		log.writeLine(timeStamp() + "Error has occured. See error log.");
	}
	
	
	/**
	 * Prints a line in the server and in the log. Time stamped
	 */
	public void printRecord(String message){
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
		//Server.messageSlack("New \nLine", null);

		String derp = "f%2ff";

		String convert = derp.replaceAll("%2f", "/");

		System.out.println(convert);

		Calendar d = Calendar.getInstance();
		d.add(Calendar.HOUR_OF_DAY, 6);
		d.add(Calendar.MINUTE, -15);
		if ((d.get(Calendar.HOUR_OF_DAY) == 0) && 
				(d.get(Calendar.MINUTE) >= 5) && (d.get(Calendar.MINUTE) <= 10)){
		System.out.println(d.get(Calendar.HOUR_OF_DAY));
		}

	}
	 */
}
