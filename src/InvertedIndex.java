import java.io.*;
import java.io.ObjectInputStream.GetField;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
public class InvertedIndex{
	protected TreeMap<String, TreeMap<String , SortedSet<Integer>>> index; 
	private static QueryResult check;
	/**
	 * inverted index constructor
	 */
	public InvertedIndex(){
		index =  new TreeMap<String, TreeMap<String , SortedSet<Integer>>>();
	}
	
	public TreeMap<String, TreeMap<String , SortedSet<Integer>>> getIndex(){
		return index;
	}
	
	
	/**
	 * adds a word to the invertedindex if not already in the index
	 * @param word
	 * 			word input to be added to index
	 */
	public void addWord(String word){
		if(index.get(word) == null){
			index.put(word, new TreeMap<String, SortedSet<Integer>>());
		}
	}
	/**
	 * attributes file names to the words in an inner tree structure
	 * @param word
	 * 			word input
	 * @param file
	 * 			file to attribute to the word
	 */
	public void addInnerKey(String word, String file){
		if(index.get(word).containsKey(file) == false){
			index.get(word).put(file, new TreeSet<>());
		}
	}
	/**
	 * adds the index of any given word in a file
	 * @param word
	 * @param file
	 * @param value
	 * 			the index location of the word in an html file being parsed
	 */
	public void addInnerValue(String word, String file, Integer value){
		index.get(word).get(file).add(value);
	}
	/**
	 * takes a query string to check the index. Cleans the string in the proper format. For each word
	 * in the query string, search the index in partial search format. Makes a new QueryResult for the first
	 * time a query matches and if it isnt new it will compare the values and update data for sorting purposes
	 * @param query1
	 * 			the query match starts off as a string and gets turned into a string[] for iterative search
	 * @return
	 * 			returns an arraylist of query results, which are matches found in QueryResult object format 
	 */
	public ArrayList<QueryResult> partialSearch(String query1){
		ArrayList<QueryResult> result = new ArrayList<QueryResult>();
		HTMLCleaner clean = new HTMLCleaner();
		String[] query = clean.stripHTML(query1).trim().split(" ");
		StringBuilder queryString = new StringBuilder();
		for(int i = 0; i < query.length; i++){
			if(!query[i].equals("")){
				queryString.append(query[i] + " ");
			}
		}
		queryString = queryString.deleteCharAt(queryString.lastIndexOf(" "));
		
		for(int i = 0; i < query.length; i++){
			for(String key: index.keySet()){
				if(key.startsWith(query[i])){
					for(String path: index.get(key).keySet()){
						if(!containsQuery(result, path, queryString.toString())){
							result.add(new QueryResult(queryString.toString(), 0, index.get(key).get(path).first(), path));
						}
						QueryResult queryCompare = this.getQuery(result, path, queryString.toString());
						queryCompare.addFrequency(index.get(key).get(path).size());
						if(index.get(key).get(path).first() < queryCompare.getInitial()){
							queryCompare.setInitial(index.get(key).get(path).first());
						}
						//QueryResult test = this.getQuery(result, path, queryString.toString());
						//System.out.println(test.toString());
						}
				}
				if(query[i].compareTo(key) == 1){
					continue;
				}
			}
			if(this.allFalsePartial(query) ){
				result.add(new QueryResult(queryString.toString(), ""));
			}
		}
		
		Collections.sort(result);
		
		//this.printResults(result);
		return result;
	}
	/**
	 * runs the partial search method on the whole set of query matches from a given file
	 * @param queries
	 * 			an arraylist of all the queries in a given query text file
	 * @return
	 */
	public ArrayList<QueryResult> partialSearchFull(ArrayList<String[]> queries){
		
		ArrayList<QueryResult> result = new ArrayList<QueryResult>();
		ArrayList<QueryResult> queryMatch;
		
		for(String[] query: queries){
			//System.out.println(Arrays.toString(query));
			queryMatch = partialSearch(Arrays.toString(query));
			result.addAll(queryMatch);
		}
		Collections.sort(result);
		//this.printResults(result);
		return result;
	}
	/**
	 * method to execute exact search function given a query of words
	 * 
	 * @param String[] query
	 * 			the query of words needed to search for matches
	 */
	
	public ArrayList<QueryResult> exactSearch(String query1){
		ArrayList<QueryResult> result = new ArrayList<QueryResult>();
		HTMLCleaner clean = new HTMLCleaner();
		//System.out.println(clean.stripHTML(query1));
		String[] query = clean.stripHTML(query1).split(" ");
		StringBuilder queryString = new StringBuilder();
		for(int i = 0; i < query.length; i++){
			queryString.append(query[i] + " "); 
		}
		queryString = queryString.deleteCharAt(queryString.lastIndexOf(" "));
		
		for(int i = 0; i < query.length; i++){
			if(index.containsKey(query[i])){
				for(String path: index.get(query[i]).keySet()){
					if(!containsQuery(result, path, query[i])){
						result.add(new QueryResult(queryString.toString(), 0, index.get(query[i]).get(path).first(), path));
					}
					QueryResult queryCompare = this.getQuery(result, path, queryString.toString());
					queryCompare.addFrequency(index.get(query[i]).get(path).size());
					if(index.get(query[i]).get(path).first() < queryCompare.getInitial()){
						queryCompare.setInitial(index.get(query[i]).get(path).first());
					}
					//QueryResult test = this.getQuery(result, path, queryString.toString());
					//System.out.println(test.toString());
				}
			}
			if(this.allFalse(query) ){
				result.add(new QueryResult(queryString.toString(), ""));
			}
		}
		Collections.sort(result);
		//this.printResults(result);
		return result;
	}
	/**
	 * runs exact search on all queries of a given query text file
	 * @param queries
	 * 			array list of queries in a given file
	 * @return
	 */
	public ArrayList<QueryResult> exactSearchFull(ArrayList<String[]> queries){
		
		ArrayList<QueryResult> result = new ArrayList<QueryResult>();
		ArrayList<QueryResult> queryMatch;
		
		for(String[] query: queries){
			//System.out.println(Arrays.toString(query));
			queryMatch = exactSearch(Arrays.toString(query));
			result.addAll(queryMatch);
		}
		Collections.sort(result);
		//this.printResults(result);
		return result;
	}
	/**
	 * checks if a given array list of search results already contains a certain word/path combo. 
	 * prevents duplicate objects.
	 * @param queryList
	 * 			the query results list of search results using partial or exact search
	 * @param path
	 * 			the path to compare if there already exists a path/word combo
	 * @param word
	 * @return
	 */
	public boolean containsQuery(ArrayList<QueryResult> queryList, String path, String word){
		for(QueryResult queryResult: queryList){
			if(queryResult.path.equals(path) && queryResult.word.contains(word)){
				return true;
			}
		}
		return false;
	}
	/**
	 * if a query[] has no matches, will return false in order to prevent creating empty objects which
	 * will mess up the JSON writer
	 * @param query
	 * 			one query set from a given query file
	 * @return
	 */
	public boolean allFalse(String[] query){
		for(String q: query){
			if(index.containsKey(q)){
				return false;
			}
		}
		return true;
	}
	/**
	 * same as all false but with partial search
	 * @param query
	 * @return
	 */
	public boolean allFalsePartial(String[] query){
		for(String q: query){
			for(String key: index.keySet()){
				if(key.startsWith(q)){
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * gets a query result from the query result list. 
	 * @param queryList
	 * @param path
	 * @param word
	 * @return
	 */
	public QueryResult getQuery(ArrayList<QueryResult> queryList, String path, String word){
		for(QueryResult queryResult: queryList){
			if(queryResult.path.equals(path) && queryResult.word.equals(word)){
				return queryResult;
			}
		}
		return null;
	}
	/**
	 * takes the list of matching results from a search and prints all of the matches from exact or full 
	 * @param queryList
	 * 			the query list containing all matches from a search
	 */
	public void printResults(ArrayList<QueryResult> queryList){
		for(QueryResult queryResult: queryList){
			System.out.println(queryResult.toString());
		}
	}
	/**
	 * the JSON writer for the index JSON Pretty Print format
	 * @param path
	 * 			the output file path where the json will be written to
	 */
	public void toJSON(String path){
		BufferedWriter bw = null;
		try{
			//checks if file exists, if not it will create it
			File file = new File( path );
			if(!file.exists()) {
				System.out.println("creating file");
				file.createNewFile();
			}
			//creates a bufferedwriter in the UTF8 format
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
			
			bw.write("{\n");
			Set<String> i = index.keySet();
			Iterator<String> it = i.iterator();
			//nested while loops to iterate through the inverted index and write to the output file 
			while(it.hasNext()) {
				String current = it.next();
				bw.write("\t" + '"' + current + '"' + ": {\n" );
				Set<String> fileName = index.get(current).keySet();
				Iterator<String> fileIt = fileName.iterator();
				while(fileIt.hasNext()) {
					String currentFileName = fileIt.next();
					String osFormat = currentFileName.replaceAll("\\\\", "/");
					bw.write("\t\t" + '"' + osFormat + '"' + ": [\n");
					Iterator<Integer> indexPosition = index.get(current).get(currentFileName).iterator();
					while(indexPosition.hasNext()) {
						int currentPos = indexPosition.next();
						if(indexPosition.hasNext()) {
							bw.write("\t\t\t" +currentPos + ",\n");
						}
						else {
							bw.write("\t\t\t" + currentPos + "\n");
						}
						
					}
					if(fileIt.hasNext()) {
						bw.write("\t\t],\n");
					}
					else {
						bw.write("\t\t]\n");
					}
				}
				if(it.hasNext()) {
					bw.write("\t},\n");
				}
				else {
					bw.write("\t}\n");
				}
			}
			bw.write("}");
			
		}catch (IOException e){
			System.err.println(e.getMessage());
		}finally{
			try{
				if(bw != null)
					bw.close();
			}catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	/**
	 * the JSON writer for the results path for the query search
	 * @param resultsPath
	 * 			the path indicating where the results JSON file will be written
	 * @param queryArray
	 * 			the array of results 
	 */
	public void toJSONResults(String resultsPath, ArrayList<QueryResult> queryArray){
		File file = new File( resultsPath );
		if(!file.exists()) {
			System.out.println("creating file");
			try {
				file.createNewFile();
				file.setReadable(true);
				System.out.println(Files.isReadable(file.toPath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsPath), StandardCharsets.UTF_8));){
			Iterator<QueryResult> it = queryArray.iterator();
			bw.write("[\n");
			int count = 0;
			QueryResult curr = null;
			QueryResult prev = curr;
			if(it.hasNext()){
				prev = it.next();
			}
			while(it.hasNext()){
					curr = it.next();
//					if(prev.word != null){
//						
//						System.out.println(prev.word + curr.word);
//					}
					
					if(count == 0){
						bw.write("\t{\n\t\t" + "\"queries\": " + "\"" + prev.word + "\",\n");
						
					}
					if(prev.getFrequency() != 0){
						if(count == 0){
							bw.write("\t\t\"results\": [\n");
						}
						bw.write("\t\t\t{\n\t\t\t\t\"where\": ");
						bw.write("\"" + prev.path.replaceAll("\\\\", "/") + "\",\n");
						bw.write("\t\t\t\t\"count\": " + prev.getFrequency() + ",\n");
						bw.write("\t\t\t\t\"index\": " + prev.getInitial() + "\n");
						bw.write("\t\t\t}");
					}
					else if(prev.getFrequency() == 0 && count == 0 ){
						bw.write("\t\t\"results\": [");
						count++;
						
					}
					if(prev.word.equals(curr.word) && prev.getFrequency() != 0){
						bw.write(",\n");
						count++;
					}
					else if(!prev.word.equals(curr.word)){
						bw.write("\n\t\t]\n\t}");
						count = 0;
						if(it.hasNext()){
							bw.write(",\n");
						}
						
					}
					if(it.hasNext()){
						prev = curr;
						
					}
					
					check = prev;
			}
			System.out.println(check.getInitial());
			QueryResult last = queryArray.get(queryArray.size()-1);
			
			if(check.word.equals(last.word)){
				if(last.getFrequency() != 0){
				bw.write("\t\t\t{\n\t\t\t\t\"where\": ");
				bw.write("\"" + last.path.replaceAll("\\\\", "/") + "\",\n");
				bw.write("\t\t\t\t\"count\": " + last.getFrequency() + ",\n");
				bw.write("\t\t\t\t\"index\": " + last.getInitial() + "\n");
				bw.write("\t\t\t}\n");
				bw.write("\t\t]\n\t}\n]");
				}
				else{
					bw.write("\t\t\t\n");
					bw.write("\t\t]\n\t}\n]");
				}
			}
			else{
				if(last.getFrequency() != 0){
					bw.write(",\n\t{\n\t\t" + "\"queries\": " + "\"" + last.word + "\",\n");
					bw.write("\t\t\"results\": [\n");
					bw.write("\t\t\t{\n\t\t\t\t\"where\": ");
					bw.write("\"" + last.path.replaceAll("\\\\", "/") + "\",\n");
					bw.write("\t\t\t\t\"count\": " + last.getFrequency() + ",\n");
					bw.write("\t\t\t\t\"index\": " + last.getInitial() + "\n");
					bw.write("\t\t\t}\n");
					bw.write("\t\t]\n\t}\n]");
				}
				else{
					bw.write("\t{\n\t\t" + "\"queries\": " + "\"" + prev.word + "\",\n");
					bw.write("\t\t\"results\": [\n");
					bw.write("\t\t]\n\t}\n]");
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch(NullPointerException e){
			e.printStackTrace();
		}
	
	}
}