import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CLParser{
	protected Path path, index; 
	
	
	public CLParser(String[] args) throws Exception{
		if(args.length != 0){
			parse(args);
		}
		else{
			throw new NullPointerException("error");
		}
		
	}
	
	public void parse(String[] args) throws Exception{
		if(args.length == 0){
			System.out.println("No arguments given");
			
		}
		//checks the command line arguments
		for(int i = 0; i < args.length; i++){
			//if this argument is the pathflag, check the next argument if any and check if it is a valid path or not
			if(this.isPathFlag(args[i])){
				if(i+1 < args.length && !isIndexFlag(args[i+1])){
					this.setPath(args[i+1]);
				}
				if(i+1 < args.length && isIndexFlag(args[i+1])){
					throw new Exception("No path provided");
				}
			}
			//if this argument is the indexflag, check if there is a next argument or not. Checks for the default 
			if(this.isIndexFlag(args[i])){
				if(i+1 < args.length && args[i+1].contains(".json")){
					this.setIndex(args[i+1]);
				}
				else if(i+1 >= args.length){
					this.setIndex("index.json");
				}
				else{
					this.setIndex("index.json");
				}
				System.out.println(this.getPath() + " " + this.getIndex());
			}
		}
	}
	
	public boolean isPathFlag(String arg){
		if(arg.contains("-path")){
			return true;
		}
		return false;
	}
	
	public boolean hasPathFlag(String[] arg) {
		for(int i = 0; i < arg.length; i++) {
			if(isPathFlag(arg[i])) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isIndexFlag(String arg){
		if(arg.contains("-index")){
			return true;
		}
		return false;
	}
	
	public boolean hasIndexFlag(String[] arg) {
		for(int i = 0; i < arg.length; i++) {
			if(isIndexFlag(arg[i])) {
				return true;
			}
		}
		return false;
	}
	
	public void setPath(String arg){
		this.path = Paths.get(arg);
	}
	
	public Path getPath(){
		return path;
	}
	
	public void setIndex(String arg){
		this.index = Paths.get(arg);
	}
	
	public Path getIndex(){
		return index;
	}
	
	public String toString(){
		return this.path.toString() + " " + this.index.toString();
	}
}