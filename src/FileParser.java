import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class FileParser{
	private File file;
	public FileParser(File file){
		this.file = file;
		
	}
	/**
	 * reads all of a file with readAllBytes, uses UTF8. Returns a string
	 * @param file
	 * 			file to be parsed for text
	 * @return
	 * 			returns the String output of the entire file
	 * @throws Exception
	 * 			throws exception if file I/O error
	 */
	public String parse(File file) throws Exception{
		try{
			String stringFile = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			return stringFile;
		}catch(IOException e){
			System.out.println("Input for file is wrong or missing");
		}
		return null;
	}
	
	/**
	 * parses a query text into a string[] format that will later be used to query search
	 * @param file
	 * 			the query file parsed for queries
	 * @return
	 * 			returns an ArrayList<String[]> of queries where each entry in the list represents a line
	 * 			of query matches in the query file
	 */
	public ArrayList<String[]> queryParse(File file){
		ArrayList<String[]> queries = new ArrayList<String[]>();
		HTMLCleaner cleaner = new HTMLCleaner();
		String line;
		try(BufferedReader br = new BufferedReader(new FileReader(file))){
			while((line = br.readLine()) != null  ){
				
				//System.out.println(line);
				line = cleaner.stripHTML(line);
				if(!line.trim().equals("")){
					//System.out.println(line);
					String split[] = line.split(" ");
					//System.out.println(Arrays.toString(split));
					Arrays.sort(split);
					if(!containsQuery(queries, split)){
						queries.add(split);
					}
				}
				
				
			}
			
			Collections.sort(queries, new StringArraySort());
				
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return queries;
	}
	/**
	 * checks to see if the ArrayList<String[]> of queries already contains the match to make sure there arent
	 * duplicate matching strings
	 * @param queries
	 * 			the arraylist of queries taken from queryparse method
	 * @param match
	 * 			String[] match is compared to see if it is already in the arraylist or not
	 * @return
	 * 			true or false
	 */
	public boolean containsQuery(ArrayList<String[]> queries, String[] match){
		for(String[] query: queries){
			if(Arrays.toString(query).equals(Arrays.toString(match))){
				return true;
			}
		}
		return false;
	}
	
	public String toString(){
		try {
			String file = parse(this.file);
			return file.toString();
		} catch (Exception e) {
			return null;
		}
	}
	
}