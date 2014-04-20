import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;


/**
 * Maintains the list of id to name mappings. File created should
 * never be modified as it is maintained by the server itself.
 * File format is name,id seperated by comma, one pair is needed.
 * 
 * Thread safe after the instance created.
 * 
 * Class Invariant: name and id mappings size must both be equal.
 * @author Kevin
 *
 */
public class UserMapping {

	private static final String MAPPING_FILE_NAME = "mappings";
	private static final String DELIM = ",";

	private static HashMap<String, String> nameMapping = new HashMap<String, String>();					//names to ids
	private static HashMap<String, String> idMapping = new HashMap<String, String>();					//ids to names
	


	private static UserMapping instance;

	public static UserMapping getInstance(){
		if (instance == null){
			instance = new UserMapping();
		}
		return instance;
	}

	private UserMapping(){
		//find the user database and read values by comma separation
		File dbfile = new File(MAPPING_FILE_NAME);
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
					tokens[0] = tokens[0].trim();		//name
					tokens[1] = tokens[1].trim();		//id

					if ((tokens[0] != null) && (tokens[1] != null) && (tokens[0].length() >= 1) && (tokens[1].length() >= 1)){


						//add to master list
						nameMapping.put(tokens[0], tokens[1]);
						idMapping.put(tokens[1], tokens[0]);
						

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

	}

	/**
	 * Saves all current user values to the file.
	 */
	public static void saveAll(){
		File dbfile = new File(MAPPING_FILE_NAME);

		if (dbfile.exists() == false){
			//cannot file file, cannot save
			System.err.println("Error! Cannot save user file, cannot be found");
		}
		else{

			String warning = "## WARNING! DO NOT MODIFY THE CONTENTS OF THIS FILE! USERS MAY HAVE TO REGISTER THEIR ID's AGAIN. ##";

			//open file for overwriting
			try {
				BufferedWriter userFile = new BufferedWriter(new FileWriter(dbfile, false));

				for (Entry<String,String> pair : nameMapping.entrySet()){
					userFile.write(pair.getKey() + "," + pair.getValue() + "\n");
				}

				userFile.flush();
				userFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets the human readable name of the user
	 * @param name
	 * @return
	 */
	public String getID(String name){
		return nameMapping.get(name);
	}

	/**
	 * Gets the id name given the human name of the user.
	 * @param id
	 * @return
	 */
	public String getName(String id){
		return idMapping.get(id);
	}
	
	/**
	 * Adds a new pairing to the list
	 * @param name
	 * @param id
	 * @return True if the pair was registered as a new pair. False otherwise
	 */
	public synchronized boolean registerPair(String name, String id){
		
		//ensure that the mapping does not contain double keys
		if (nameMapping.containsKey(name) == true){
			return false;
		}
		else if (idMapping.containsKey(id) == true){
			return false;
		}
		
		//register pair
		idMapping.put(id, name);
		nameMapping.put(name, id);
		
		return true;
	}
	
	
	/**
	 * Updates the name of a new pairing to the list.
	 * @param oldName
	 * @param newName
	 */
	public synchronized boolean updateName(String oldName, String newName){
		String id = nameMapping.remove(oldName);
		
		if (id == null){
			throw new IllegalArgumentException("No mapping of the old name " + oldName + " was found. Cannot update with new name.");
		}
		
		nameMapping.put(newName, id);
		idMapping.put(id, newName);
		
		return true;
	}
	
	public int getCount(){
		
		if (nameMapping.size() != idMapping.size()){
			throw new IllegalStateException("Class invariant invalid. Name and id maps are unequal.");
		}
		
		return nameMapping.size();
	}
}
