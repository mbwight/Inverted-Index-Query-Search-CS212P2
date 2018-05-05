import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
*class to extend Inverted Index with a thread safe read/write lock
*
*@see InvertedIndex.java
*@see ReadWriteLock.java
**/

public class ThreadSafeInvertedIndex extends InvertedIndex{
	private ReadWriteLock lock;
	private WorkQueue workQ;
	private int threads;
	public ThreadSafeInvertedIndex(int threads) {
		lock = new ReadWriteLock();
		workQ = new WorkQueue();
		this.threads = threads;
	}
	
	
	@Override
	public void addWord(String word){
		lock.lockReadWrite();
		try {
			 super.addWord(word);
		}
		finally {
			lock.unlockReadWrite();
		}
	}
	
	@Override
	public void addInnerKey(String word, String file){
		lock.lockReadWrite();
		try {
			super.addInnerKey(word, file);
		}
		finally {
			lock.unlockReadWrite();
		}

	}
	
	@Override
	public void addInnerValue(String word, String file, Integer value){
		lock.lockReadWrite();
		try {
			super.addInnerValue(word, file, value);
		}
		finally {
			lock.unlockReadWrite();
		}
	}
	/**
	 * uses the partial search from InvertedIndex and implements the single partialSearch 
	 * with a WorkQueue
	 * @param queries
	 * 			queries from a query file
	 * @param threads
	 * 			threads specified in the driver
	 * @return
	 */
	public ArrayList<QueryResult> partialSearchFull(ArrayList<String[]> queries, int threads){
		this.threads = threads;
		ArrayList<QueryResult> finalResult = new ArrayList<QueryResult>();
		
		for(String[] query: queries){
			workQ.execute(new partialSearch(query.toString(), finalResult, getIndex()));
		}
		workQ.finish();
		Collections.sort(finalResult);
		return finalResult;
		
	}
	/**
	 * uses a work queue to execute fullSearch exact runnable method
	 * @param queries
	 * 			queries from a file
	 * @param threads
	 * 			threads specified by the driver
	 * @return
	 * 			returns the arraylist of queryresults for all queries in a query file
	 */
	public ArrayList<QueryResult> exactSearchFull(ArrayList<String[]> queries, int threads){
		this.threads = threads;
		ArrayList<QueryResult> finalResult = new ArrayList<QueryResult>();
		
		for(String[] query: queries){
			workQ.execute(new fullSearch(query.toString(), finalResult, getIndex()));
		}
		workQ.finish();
		Collections.sort(finalResult);
		return finalResult;
		
	}
	
	private static class partialSearch implements Runnable{
		private String query1;
		private ArrayList<QueryResult> result;
		private ArrayList<QueryResult> finalResult;
		private TreeMap<String, TreeMap<String , SortedSet<Integer>>> index; 
	
		public partialSearch(String query1, ArrayList<QueryResult> finalResult, TreeMap<String, TreeMap<String ,  SortedSet<Integer>>> index){
			this.query1 = query1;
			this.index = index;
			this.finalResult = finalResult;
			result = new ArrayList<QueryResult>();
			}
		
		public boolean containsQuery(ArrayList<QueryResult> queryList, String path, String word){
			for(QueryResult queryResult: queryList){
				if(queryResult.path.equals(path) && queryResult.word.contains(word)){
					return true;
				}
			}
			return false;
		}
		
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
		
		public QueryResult getQuery(ArrayList<QueryResult> queryList, String path, String word){
			for(QueryResult queryResult: queryList){
				if(queryResult.path.equals(path) && queryResult.word.equals(word)){
					return queryResult;
				}
			}
			return null;
		}
		
		@Override
		public void run(){
			synchronized(finalResult) {
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
				finalResult.addAll(result);
				//this.printResults(result);
				
			}
		}
	}
	
	private class fullSearch implements Runnable{
		private String query1;
		private ArrayList<QueryResult> result;
		private ArrayList<QueryResult> finalResult;
		private TreeMap<String, TreeMap<String , SortedSet<Integer>>> index; 
	
		public fullSearch(String query1, ArrayList<QueryResult> finalResult, TreeMap<String, TreeMap<String ,  SortedSet<Integer>>> index){
			this.query1 = query1;
			this.index = index;
			this.finalResult = finalResult;
			result = new ArrayList<QueryResult>();
			}
		public QueryResult getQuery(ArrayList<QueryResult> queryList, String path, String word){
			for(QueryResult queryResult: queryList){
				if(queryResult.path.equals(path) && queryResult.word.equals(word)){
					return queryResult;
				}
			}
			return null;
		}
		public boolean allFalse(String[] query){
			for(String q: query){
				if(index.containsKey(q)){
					return false;
				}
			}
			return true;
		}
		public boolean containsQuery(ArrayList<QueryResult> queryList, String path, String word){
			for(QueryResult queryResult: queryList){
				if(queryResult.path.equals(path) && queryResult.word.contains(word)){
					return true;
				}
			}
			return false;
		}
		
		
		public void run(){
			synchronized(finalResult){
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
					
					Collections.sort(result);
					finalResult.addAll(result);
				}
			}
		}
	}
	
}
