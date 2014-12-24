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
import java.util.HashMap;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import command.Command;
import command.RequestStruct;
import command.SlackMessage;


/**
 * The main server program.
 * @author Kevin
 *
 */
public class Server {

	public static final int MIN_PORT = 1000;
	public static final int MAX_PORT = 65535;


	public static final int INCREMENT = 1;


	private HashMap<String, Command> commands = new HashMap<String, Command>();

	private ServerSocket listenSock;


	//loggers
	private volatile Logger log;
	private volatile Logger errorLog;

	private static final String LOG_FOLDER = "logs";
	private static final String ERROR_LOG_FOLDER = LOG_FOLDER;
	private static final String LOG_FILENAME = "log";
	private static final String ERROR_LOG_FILENAME = "errorLog";

	private static ServerStream stream;

	//threads
	private Thread acceptThread;
	private Thread maintanenceThread;					//see Maintenence Thread for maintentence interval

	private long startTime;


	private boolean running = true;						//server running or not
	private boolean silent = false;						//silent mode prevents server from posting to slack


	//private static Date startDate = new Date(System.currentTimeMillis());

	public Server(){

		int port = Config.getPort();

		//check port arg
		if ((port< MIN_PORT) && (port > MAX_PORT)){
			throw new IllegalArgumentException("Invalid port given. Cannot create server with port " + port);
		}


		//create output stream
		stream = new ServerStream(this);
		
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

			
			/*
			SlackMessage test = new SlackMessage("http://www.kyubeypawnshop.net/slack_server_icons/lucina_icon.png", "C0291NEV5");
			test.setUsername("slackbot");
			test.setUserIcon(":frowning:", IconType.EMOJI);
			SlackAttachment a = new SlackAttachment("<http://www.kyubeypawnshop.net/slack_server_icons/lucina_icon.png>");
			a.setPretext("<http://www.kyubeypawnshop.net/slack_server_icons/lucina_icon.png>");
		
			test.setUnfurlLinks(true);
			test.setUnfurlMedia(true);
			//test.setUnfurlLinks(true);
			//test.addAttachment(a);
			//test.addAttachment(a);
			/*
			SlackField f = new SlackField("", "Hey");
			a.setColor("#FF0000");
			a.addField(f);
			
			SlackAttachment a2 = new SlackAttachment("");
			a2.setColor("#00FF00");
			a2.addField(f);
			test.addAttachment(a2);
			
			SlackAttachment a3 = new SlackAttachment("");
			a3.setColor("#0000FF");
			a3.addField(f);
			test.addAttachment(a3);
			//a.addField(f);
			 
			messageSlack(test);
			System.out.println(SlackMessage.dumpString(test.getJSON()));
			System.exit(0);
			*/
			
			//create the handling thread and run it.
			acceptThread = new Thread(new SocketAccepter(listenSock));
			acceptThread.start();

			this.startTime = System.currentTimeMillis();							//server actually starts running here.

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
					println("To use /message, enter channel name then the message to send no qoutations expected.");
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

	
	
	
	




	private static final int RESEND_COUNT = 5;

	private void messageSlack(String message, String channel){
		messageSlack(new SlackMessage(message, channel));
	}

	private static final int RETRY_WINDOW = 10;			//retry window of 10 seconds
	
	
	/**
	 * Posts a message on slack on the specified channel
	 * 
	 * Fail safe mechanism to stop messages from bot apocalypse
	 * @param message
	 * @param channel
	 */
	private void messageSlack(SlackMessage message){
		//System.out.println(textPost);



		if(silent == false){

			String send;
			//construct the JSON message
			send = "payload=" + message.getJSON();


			//System.out.println(message);


			long enlapsedSeconds = System.currentTimeMillis();

			//attempt to send the message
			boolean sendSuccess = false;
			int tryCount = 0;
			while ((tryCount < RESEND_COUNT) && (sendSuccess == false)){
				tryCount++;

				try {

					sendMessage(send);

					sendSuccess = true;
				} catch (UnknownHostException e){
					//unknown host. try again
					printRecord("Error, UnknownHostException, could not send message to Slack, retrying... (attempt #" + tryCount + ")");
					printException(e);
				} catch (IOException e) {
					printRecord("Error, could not send message to Slack, retrying... (attempt #" + tryCount + ")");
					printException(e);
				}

				//wait one second to retry
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				enlapsedSeconds = Math.abs(System.currentTimeMillis() - enlapsedSeconds);
				//if out of time, fail and break loop
				if (enlapsedSeconds > (RETRY_WINDOW * 1000)){
					sendSuccess = true;
					break;
				}
			}


			//if resending was a major failure report
			if (tryCount == RESEND_COUNT){
				printRecord("Error! Attempted to send message to Slack " + tryCount + " times and failed. See error log for exceptions.");
			}
		}
		else{
			//silent mode, should print out the reply that should have been
			println(message.getMessage());
		}
	}

	private void sendMessage(String message) throws UnknownHostException, IOException {
		CloseableHttpClient slackServer = HttpClients.createDefault();

		HttpPost slackMessage = new HttpPost(Config.getSlackWebHook());

		slackMessage.setHeader("User-Agent", "Slack Points Server");
		slackMessage.setHeader("content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		slackMessage.setEntity(new StringEntity(message));

		HttpResponse response  = slackServer.execute(slackMessage);


		//print reply from slack server if any.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		response.getEntity().writeTo(baos);
		println("<Slack Reply>: " + (new String(baos.toByteArray())));

		slackServer.close();
	}

	//====================================================================================================
	//The request handler


	/**
	 * The request handler created when a new request is being made.
	 * @author Kevin
	 *
	 */
	private final class RequestHandler implements Runnable{

		private Socket client;
		private RequestCallback caller;

		public RequestHandler(Socket client, RequestCallback caller){
			this.client = client;
			this.caller = caller;
		}

		@Override
		public void run() {
			doWork();
			caller.done();			
		}

		private void doWork(){
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

					if (req == null){
						printError("Error! Could not parse request. Payload given was:\n" + payload);
					}
					else{
						//print command out
						String fullCommand = req.getCommand();
						String[] args = req.getArgs();
						for (int i =0; i < args.length; i++){
							fullCommand = fullCommand + " " + args[i];
						}
						printRecord("<SLACK_CMD> " + req.getUserName() + " issued command: \t" + fullCommand);


						
						Command com = commands.get(req.getCommand());
						if (com != null){
							//do command
							//int runCode = com.doRequest(stream, req);
							com.doRequest(stream, req);
						}
						else{
							//unreqconized command.
							messageSlack("Sorry I don't understand that command. :frown:", req.getChannelID());
						}
						
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
		commands.put(cmd.commandName, cmd);
	}


	//===========================================================================================================


	/**
	 * The socket accepting thread that accepts all connections and attempts
	 * to service them.
	 * @author Kevin
	 *
	 */
	private final class SocketAccepter implements Runnable, RequestCallback{

		private ServerSocket serverSock;

		private volatile int serviceCount = 0;

		private static final int MAX_REQUEST_COUNT = 7;
		private static final int BAN_SECONDS = 30;						//negative number for infinite ban till server restart.


		public SocketAccepter(ServerSocket serv){
			this.serverSock = serv;
		}

		@Override
		public void run() {

			while( running == true ){




				try {
					Socket client = serverSock.accept();

					//should not request more than max
					serviceCount++;


					if (serviceCount >= MAX_REQUEST_COUNT){
						printRecord("Warning! Max request count reached. Spammer alert! Server refusing requests for " + BAN_SECONDS + " seconds.");
						Thread.sleep(BAN_SECONDS*1000);
					}



					//got a connection
					println("Recieved connection from: " + client.getInetAddress().getHostAddress() + ":" + client.getPort());

					//handle request in new thread
					Thread clientHandler = new Thread(new RequestHandler(client, this));
					clientHandler.start();



				} catch (IOException e) {
					printException(e);
				} catch (InterruptedException e) {
					printException(e);
				}


			}


			try {
				listenSock.close();
			} catch (IOException e) {
				printException(e);
			}

		}

		@Override
		public void done() {
			serviceCount--;
		}


	}
	private interface RequestCallback{
		void done();
	}




	private final class MaintenanceThread implements Runnable{

		private static final int MAINTENANCE_TIME = 300000; 		//every 5 minutes, run maintenance thread.

		private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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

					//compute running time.
					long runTime = today.getTimeInMillis() - startTime;
					long days = runTime / 86400000;
					long hours = (runTime % 86400000) / 3600000;
					long mins = ((runTime % 86400000) % 3600000) / 60000;
					String totalRunTime = days + " days, " + hours + " hours, " + mins + " mins";

					//printmaintenance record.
					printRecord("--> Maintenance Thread saved all information. Server running since " + formatter.format(startTime) + " (" + totalRunTime + ").");
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



	private class ServerStream implements WorkStream{

		private Server instance;
		
		public ServerStream(Server s){
			this.instance = s;
		}
		
		@Override
		public void outPrintln(String out) {
			System.out.println();
		}

		@Override
		public void errPrintln(String err) {
			System.err.println(err);
		}

		@Override
		public void logPrintln(String out) {
			instance.printRecord(out);
		}

		@Override
		public void errLogPrintln(String err) {	
			instance.printError(err);
		}

		@Override
		public boolean messageSlack(SlackMessage message) {
			//TODO change Message slack to boolean and report real message status (currently true regardless if sent)
			instance.messageSlack(message);
			return true;
		}

		@Override
		public void errLogPrintln(Exception e) {
			instance.printException(e);
		}
		
	}

	/*
	public static void main(String[] args){
		//Server.messageSlack("New \nLine", "D024J9MFY");

		RequestStruct derpreq = RequestStruct.createInstance("token=IwpHiALszaR2LZLLBPatrPN3&team_id=T024GLC9M&channel_id=C0291NEV5&channel_name=bot-forge&user_id=U024HATQ5&user_name=agamemnon&command=%2Fcheck&text=hiimkevin0uo");


		String derp = "i";

		String[] convert = derp.split("\\+");

		System.out.println(convert.length);
		System.out.println(new String[0].length);
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
