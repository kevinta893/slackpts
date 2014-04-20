import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;


/**
 * Simple database class that takes in a user list file.
 * The file has lines in the form: name,value
 * Which is separated by the comma delimiter (see DELIM field).
 * The name is used to refer to the user at any time.
 * Any malformed lines are ignored and the parsing continues through
 * the file. name or value must have at least one character to be valid.
 * @author Kevin
 *
 */
public class UserDB {


	private static final String DB_FILE_NAME = "users";

	private static final String DELIM = ",";										//delimiter of each username and points

	private static HashMap<String, User> masterList = new HashMap<String, User>();

	private static UserDB instance;

	public static UserDB getInstance(){
		if (instance == null){
			instance = new UserDB();
		}
		return instance;
	}

	private UserDB(){

		//find the user database and read values by comma separation
		File dbfile = new File(DB_FILE_NAME);
		if (dbfile.exists() == false){
			//create the file
			try {
				dbfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Can not create user database file! Quitting...");
				System.exit(-1);
			}
		}


		//database file found, read the file
		try {
			FileReader fr = new FileReader(dbfile);
			BufferedReader br = new BufferedReader(fr);


			//now read each user line
			String nextLine = br.readLine();
			String[] tokens;
			while (nextLine != null){

				tokens = nextLine.split(DELIM);

				if (tokens.length == 2){
					//exactly two values, then 
					tokens[0] = tokens[0].trim();
					tokens[1] = tokens[1].trim();

					if ((tokens[0] != null) && (tokens[1] != null) && (tokens[0].length() >= 1) && (tokens[1].length() >= 1)){

						//parse the integer if possible
						try {
							long pts = Long.parseLong(tokens[1]);

							User parsed = new User(tokens[0], pts);

							//add to master list
							masterList.put(parsed.getName(), parsed);
						} catch (NumberFormatException e) {}  //ignore line if cannot parse
					}

				}
			

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
		System.err.println("Warning! No users read from the file.");
	}

}


/**
 * Saves all current user values to the file.
 */
public void saveAll(){
	File dbfile = new File(DB_FILE_NAME);

	if (dbfile.exists() == false){
		//cannot file file, cannot save
		System.err.println("Error! Cannot save user file, cannot be found");
	}
	else{

		//open file for overwriting
		try {
			BufferedWriter userFile = new BufferedWriter(new FileWriter(dbfile, false));

			for (User user : masterList.values()){
				userFile.write(user.getName() + "," + user.getPts() + "\n");
			}

			userFile.flush();
			userFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


public int getUserCount(){
	return masterList.size();
}

}
