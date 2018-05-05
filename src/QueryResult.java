public class QueryResult implements Comparable<QueryResult>{
	/** word result */
	public final String word;
	/** frequency of result */
	private int frequency;
	/** initial position of any word in query search result */
	private int initial;
	/** path where result was found */
	public final String path;
	
	/**
	 * initializes a QueryResult from parameters above
	 * @param word
	 * 			The word that was found using query matching
	 * @param frequency
	 * 			The amount of times a match was found in a specific file
	 * @param initial
	 * 			Index location of where the match appears first
	 * @param path
	 * 			Path where search result was found
	 */
	
	public QueryResult(String word, int frequency, Integer initial, String path){
		this.word = word;
		this.frequency = frequency;
		this.initial = initial;
		this.path = path;
	}
	/**
	 * Query Result initializer overload
	 * @param word
	 * @param path
	 */
	public QueryResult(String word, String path){
		this.word = word;
		this.path = path;
	}
	
	public int getInitial(){
		return initial;
	}
	
	public void setInitial(int initial){
		this.initial = initial;
	}
	
	public int getFrequency(){
		return frequency;
	}
	
	public void addFrequency(int size){
		frequency = frequency + size;
	}
	
	/**
	 * Compares QueryResults by frequency in descending order, if the same
	 * Compares QueryResults by initial index in ascending order, if the same
	 * Compares QueryResults by page path in alphabetical order
	 * 
	 * @param other
	 * 		the other QueryResult object being compared to this
	 */
	public int compareTo(QueryResult other){
		if(this.word.compareTo(other.word) != 0){
			return this.word.compareTo(other.word);
		}
		else if(this.frequency > other.frequency){
			return -1;
		}
		else if(this.frequency < other.frequency){
			return 1;
		}
		else if(this.initial < other.initial){
			return -1;
		}
		else if(this.initial > other.initial){
			return 1;
		}
		else if(this.path.compareTo(other.path) != 0){
			return this.path.compareTo(other.path);
		}
		else{
			return 0;
		}
	}
	
	@Override
	public String toString() {
		return String.format("Word: %s Frequency: %d Initial Index: %d Path: %s.", this.word, this.frequency, this.initial, this.path);
	}
}