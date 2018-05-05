import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DirectoryParser{
	private Path path;
	private WorkQueue queue;
	protected ArrayList<File> files = new ArrayList<>();
	//takes an arraylist of files 
	public DirectoryParser(Path path, int threads){
		this.path = path;
		queue = new WorkQueue(threads);
	}
	/**
	 * takes a path. If the path location is just a single file that ends with .htm extension, 
	 * adds it to the file array
	 * @param path
	 * 			path for directory or html file to be parsed
	 * @return
	 * 			
	 */
	//if it is a directory, recursively go through the directories and add .htm files to the array
	public File parse(Path path){
		
		queue.execute(new DirectoryTask(path));
		queue.finish();
		
		
		
		return null;
	}
	
	private class DirectoryTask implements Runnable{
		private final Path path;
		
		public DirectoryTask(Path path) {
			this.path = path;
			
		}
		
		public void run() {
			File file = new File(path.normalize().toString());
			if(file.toString().toLowerCase().endsWith(".htm") || file.toString().toLowerCase().endsWith(".html")){
				synchronized(files){
					files.add(file);
				}
				
			}
			if(file.isDirectory()) {
				for(File f: file.listFiles()){
					if(f.isDirectory()){
						queue.execute(new DirectoryTask(f.toPath()));
					}
					else{
						if(f.toString().toLowerCase().endsWith(".htm") || f.toString().toLowerCase().endsWith(".html")){
							synchronized(files){
								files.add(f);
							}
							
						}
					}
				}
			}
		}
	}
}