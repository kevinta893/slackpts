package command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


/**
 * The command that returns a fortune for a paritular
 * user ID once per day. The fortune list is given by the file
 * fortunes.txt
 * @author Kevin
 *
 */
public class FortuneCmd extends Command{

	private static final String COMMAND = "/fortune";


	private String returnMessage;
	private String returnChannel;
	private String logMessage;
	private String errorMessage;


	private FortuneList list = new FortuneList();				//should only have one copy.
	
	public FortuneCmd() {
		super(COMMAND);

	}


	@Override
	public CmdResult doRequest(RequestStruct req) {
		returnChannel = req.getChannelName();

		//get today's date
		Calendar today = Calendar.getInstance();


		int seed = req.getUserID().hashCode();
		int frame = today.get(Calendar.YEAR) * today.get(Calendar.MONTH) * today.get(Calendar.DAY_OF_MONTH);


		int index = randInt(seed, frame) % list.getCount();

		returnMessage = req.getUserName() + "'s fortune today is:\n" + list.getFortune(index);

		return CmdResult.SUCCESS_NO_REPORT;
	}


	/**
	 * Simple XOR shift random number generator. Taken from:
	 * 
	 * 
	 * "To generate a seed, just put any values for x, y, z, w, except (0,0,0,0)"
	 * @param seed Seed for the RNG
	 * @param frame Frame count for the seed
	 * @return
	 */
	private static int randInt(long seed, long frame){
		return (new Random(seed* frame).nextInt(Integer.MAX_VALUE));
	}


	@Override
	public String getReturnMessage() {
		return returnMessage;
	}


	@Override
	public String getReturnChannel() {
		return returnChannel;
	}


	@Override
	public String getLogMessage() {
		return logMessage;
	}


	@Override
	public String getErrorMessage() {
		return errorMessage;
	}


	private class FortuneList{

		private static final String FORTUNE_FILE_NAME = "fortunes.txt";

		private ArrayList<String> masterList = new ArrayList<String>();
		
		
		
		public FortuneList(){
			//find the user database and read values by comma separation
			File dbfile = new File(FORTUNE_FILE_NAME);
			if (dbfile.exists() == false){
				//create the file
				try {
					dbfile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Can not create fortune list file! Quitting...");
					System.exit(-1);
				}
			}


			//database file found, read the file
			try {
				FileReader fr = new FileReader(dbfile);
				BufferedReader br = new BufferedReader(fr);


				//now read each user line
				String nextLine = br.readLine();

				while (nextLine != null){

					masterList.add(nextLine);

					//otherwise skip to next line
					nextLine = br.readLine();
				}


				br.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();

			}

			//print warning if no users have been read from the file
			if (masterList.size() <=0){
				System.err.println("Warning! No fortunes read from the file.");
				masterList.add("NO FORTUNES FOUND");
			}
			
			
		}
		
		public String getFortune(int index){
			return masterList.get(index);
		}
		
		public int getCount(){
			return masterList.size();
		}

	}
		/*
	// Random number generatar test.
	public static void main(String[] args){

		final int MAX = 100;
		final int TESTS = 10000000;
		int[] result = new int[MAX];
		int[] list = new int[TESTS];
		for (int i =0; i < TESTS; i ++){
			//int rand = randInt(12456,i) % MAX;
			int rand = new Random(123 * i).nextInt(MAX);
			result[rand]++;
			list[i] = rand;
		}

		double[] avgDist = new double[MAX];
		for (int i = 0 ; i < MAX; i ++){
			int count = 0;
			count =0;

			int first = 0;
			int next;
			for (int j =first; j < list.length; j++){
				if (list[j] == i){
					//found the occurance of number
					next = j;

					//take the difference and add count
					count++;
					avgDist[i] = (next-first);
					first = next;
				}
			}

			avgDist[i] = avgDist[i];// / ((double) count);

		}

		for (int i =0; i < MAX; i ++){
			//System.out.println(avgDist[i]);
			System.out.println(i + "=" + result[i]);
		}




	}
		 */
	}
