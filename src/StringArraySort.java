import java.util.Comparator;

public class StringArraySort implements Comparator<String[]>{
	public StringArraySort(){
		
	}

	@Override
	public int compare(String[] o1, String[] o2) {
		if(o1.length == o2.length){
			for(int i = 0; i < o1.length; i++){
				if(o1[i].compareTo(o2[i]) != 0){
					return o1[i].compareTo(o2[i]);
				}
			}
		}
		if(o1[0].compareTo(o2[0]) != 0){
			return o1[0].compareTo(o2[0]);
		}
		return 0;
	}

}
	