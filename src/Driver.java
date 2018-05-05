import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Driver{
	
	
	public static void main(String[] args) throws Exception{
		
		ArgumentMap clparser = new ArgumentMap(args);
	    ThreadSafeInvertedIndex index;
		String indexPath;
		WorkQueue queue;
		try{
			if(clparser.hasFlag("-threads")){
				 index = new ThreadSafeInvertedIndex(clparser.getInteger("-threads", 5));
				 queue = new WorkQueue(clparser.getInteger("-threads", 5));
			}
			else{
				 index = new  ThreadSafeInvertedIndex(1);
				 queue = new WorkQueue(1);
				 
			}
			//takes the commandline parser. Checks if the arguments are valid.
			
			if(!clparser.hasFlag("-path") && clparser.hasFlag("-index")) {
				System.out.println("No path flag");
				InvertedIndex fail = new InvertedIndex();
				fail.toJSON("index.json");
				throw new NullPointerException();
				
			}
			if(clparser.hasValue("-path")){
				DirectoryParser stream = new DirectoryParser(Paths.get(clparser.getString("-path")), 5);
				indexPath = clparser.getString("-index", "index.json").toString();
				stream.parse(Paths.get(clparser.getString("-path")));
				
				//parses through the directorystreamer's static files that were just added through parsing
				//strips the HTML and other unneeded characters
				for(File file: stream.files){
					queue.execute(new IndexAdd(index, file));
				}
				queue.finish();
			}
			if(clparser.hasFlag("-index")){
				index.toJSON(clparser.getString("-index", "index.json"));
			}
			
			if(clparser.hasFlag("-query") && clparser.hasValue("-query")){
				FileParser fileParse = new FileParser(Paths.get(clparser.getString("-query")).toFile());
				//System.out.println(fileParse.toString());
				if(clparser.hasFlag("-exact")){
					ArrayList<String[]> queryFile = fileParse.queryParse(Paths.get(clparser.getString("-query")).toFile());
					ArrayList<QueryResult> result = index.exactSearchFull(queryFile);
					if(clparser.hasFlag("-results")){
						index.toJSONResults(clparser.getString("-results", "results.json"), result);
					}
					
				}
				else{
					ArrayList<String[]> queryFile = fileParse.queryParse(Paths.get(clparser.getString("-query")).toFile());
					ArrayList<QueryResult> result = index.partialSearchFull(queryFile);
					if(clparser.hasFlag("-results")){
						index.toJSONResults(clparser.getString("-results", "results.json"), result);
					}
					
				}
			}
			if(clparser.hasFlag("-results") && !clparser.hasFlag("-query")){
				File file = new File("results.json");
				file.createNewFile();
				
			}
			
		}catch (NullPointerException e){
			e.printStackTrace();
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		
	}
	
	private static class IndexAdd implements Runnable{
		private InvertedIndex index;
		private File file;
		public IndexAdd(InvertedIndex index, File file){
			this.index = index;
			this.file = file;
		}
		
		public void run(){
			FileParser fileParser = new FileParser(file);
			HTMLCleaner cleaner = new HTMLCleaner();
			String output = null;
			try {
				output = fileParser.parse(file);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			output = cleaner.stripHTML(output);
			//after everything is cleaned, split the currently worked on file's string output into an array of words
			//makes sure that the word is an actual word and not whitespace
			String[] words = output.split(" ");
			Arrays.asList(words);
			
			for(int i = 0; i < words.length; i++){
				if(!words[i].replace("\\s+", "").equals("")){
					
					String word = words[i];
					String fileName = file.toString();
					//initializing the inner treemap for each word if it isnt already in the outer index
					synchronized(index){
						index.addWord(word);
						//initializing the inner treeset 
						index.addInnerKey(word, fileName);
						index.addInnerValue(word, fileName, i+1); 
					}
					
				}
			}
		}
	}
}