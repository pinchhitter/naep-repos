package cdac.in.naep;

import java.util.*;
import java.io.*;

class Submission{

	static Map<String,String> map = new TreeMap<String,String>();

	public static void main(String[] args){	


		try{
			BufferedReader br = new BufferedReader( new FileReader(new File("./prediction.csv") ) );
			String line = null;
			while( (line = br.readLine() ) != null ){
				String [] token = line.split(",");
				map.put(token[0].trim(), token[1].trim() );
			}

			br = new BufferedReader( new FileReader(new File("../data/hidden_label.csv") ) );
			while( (line = br.readLine() ) != null ){
				System.out.print( map.get(line.trim() )+"," );
			}
			System.out.println();

		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
