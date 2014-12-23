package command;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import server.WorkStream;



/**
 * Template class. Should be used as a template only.
 * @author Kevin
 *
 */
public final class RemindCmd extends Command {

	private static final String COMMAND = "";
	
	
	private String logMessage;
	
	
	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(500);
	
	
	public RemindCmd() {
		super(COMMAND);
		
	}


	@Override
	public int doRequest(WorkStream ws, RequestStruct req) {

		
		String[] args = req.getArgs();
		
		
		
		
		return 0;
	}

	
	
	

	
	private class Reminder implements Runnable{

		@Override
		public void run() {
			
			
		}
		
	}


	@Override
	public String getLogMessage() {
		return null;
	}
}
