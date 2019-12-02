package cdac.in.neap;

import java.io.*;
import java.util.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Timestamp;

class Utility{

	static Map<String, TreeMap<Integer, Double>> calculatePercentile( Map<String,TreeSet<Integer>> data ){ 

		Map<String,TreeMap<Integer,Double>> percentiles = new TreeMap<String, TreeMap<Integer,Double>>();
		for(String key: data.keySet() ){
			List<Integer> all = new ArrayList<Integer>( data.get( key ) );
			Collections.sort( all );
			TreeMap<Integer, Double> percentile = new TreeMap<Integer, Double>();

			for(int i = 0; i < all.size(); i++){
				double p = Math.round( (double ) i  / (double) all.size() * (double) 100 ) / (double) 100;
				percentile.put( all.get( i ), p );
			}
			percentiles.put( key, percentile );
		}
		return percentiles;
	}

	static Map<Double,Double> calculatePercentile( Set<Double> data ){ 

		Map<Double, Double> percentiles = new TreeMap<Double, Double>();
		ArrayList<Double> all = new ArrayList<Double>( data );
		Collections.sort( all );
		for(int i = 0; i < all.size(); i++){
			double p = Math.round( (double ) i  / (double) all.size() * (double) 100 ) / (double) 100;
			percentiles.put( all.get( i ), p  );
		}
		return percentiles;
	}
}

class Question{

	String id;
	String type;

	double fifthPercentile;	

	Set<String> students;
	Set<Double> timeTakens;

	Map<String, TreeSet<Integer>> observables;
	Map<String, TreeSet<Integer>> infos;
	Map<String, TreeSet<Integer>> typeCount;

	Map<Double, Double> timeTakenPercentiles;
	Map<String, TreeMap<Integer, Double>> observablesPercentiles;
	Map<String, TreeMap<Integer, Double>> infosPercentiles;
	Map<String, TreeMap<Integer, Double>> typeCountPercentiles;

	Question(String id, String type){

		this.id = id;
		this.type = type;

		this.fifthPercentile = 0.0d;
		this.students = new TreeSet<String>();
		this.timeTakens = new TreeSet<Double>();

		this.observables = new TreeMap<String, TreeSet<Integer>>();
		this.infos = new TreeMap<String, TreeSet<Integer>>(); 
		this.typeCount = new TreeMap<String, TreeSet<Integer>>(); 

		this.timeTakenPercentiles = new TreeMap<Double, Double>();
		this.observablesPercentiles = new TreeMap<String, TreeMap<Integer, Double>>();
		this.infosPercentiles = new TreeMap<String, TreeMap<Integer, Double>>(); 
		this.typeCountPercentiles = new TreeMap<String, TreeMap<Integer, Double>>(); 
	}

	void calulatePerCentile(){

		ArrayList<Double> all = new ArrayList<Double>( timeTakens );
		Collections.sort( all );
		long index = Math.round( (  (double) 5 / (double) 100 ) *  ( double ) all.size() );
		this.fifthPercentile = all.get ( (int) index );

		this.timeTakenPercentiles = Utility.calculatePercentile( this.timeTakens ); 

		this.observablesPercentiles = Utility.calculatePercentile( this.observables );
		this.infosPercentiles = Utility.calculatePercentile( this.infos );
		this.typeCountPercentiles = Utility.calculatePercentile( this.typeCount );
	}

}

class Response{

	String id;
	String type;

	List<String> enterTime;
	List<String> exitTime;	
	
	Double totalTime;
	Double percentileTime;

	Map<String, Integer> obserableCounts;
	Map<String, Integer> infoCounts;

	Map<String, Double> obserableCountPercentiles;
	Map<String, Double> infoCountPercentiles;

	Integer attempts;

	Response(String id, String type){

		this.type = type;	
		this.id = id;

		this.enterTime = null;
		this.exitTime = null;

		this.totalTime = 0.0d;
		this.percentileTime = 0.0d;		
		this.attempts = 0;

		this.obserableCounts = new TreeMap<String, Integer>();
		this.infoCounts = new TreeMap<String, Integer>();
		this.obserableCountPercentiles = new TreeMap<String, Double>(); 
		this.infoCountPercentiles = new TreeMap<String, Double>() ;
	}
}

class Student{

	String id;
	String lable;	
	double totalTime;
	List<String> activities;

	Map<String, Response> responses;
	Map<String, String> results;	
	Map<String, Integer> typeCount;

	Student(String id){
		this.id = id;
		this.totalTime = 0.0d;
		this.activities = new ArrayList<String> ();
		this.responses = new TreeMap<String, Response> ();
		this.results = new TreeMap<String, String>();
		this.typeCount = new TreeMap<String, Integer>();

		this.results.put("A", "0.0");
		this.results.put("B", "0.0");
	}
}


class Data{

	String lable;
	Map<String, Student> students;
	Map<String, Question> questions;
	Set<Double> totalTimes;

	Map<String, TreeSet<Integer>> qTypeCounts;
	Map<String, TreeMap<Integer, Double>> qTypeCountPercentile;
	Map<Double, Double> totalTimePercentile;

	Data( String lable ){

		this.lable = lable;
		this.totalTimes = new TreeSet<Double>();
		this.students = new TreeMap<String, Student> ();
		this.questions = new TreeMap<String, Question> ();

		this.qTypeCounts = new TreeMap<String, TreeSet<Integer>> ();
		this.qTypeCountPercentile = new TreeMap<String, TreeMap<Integer,Double>> ();
		this.totalTimePercentile = new TreeMap<Double, Double>();
	}

	void preprocessingStudents(){

		for(String id: students.keySet() ){

			Student student = students.get( id );

			for( String activity: student.activities ){

				String[] token = activity.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
				String qId =  token[2].trim();
				String qType =  token[3].trim();
				String action =  token[4].trim();
				String info =  token[5].trim();
				String eventTime =  token[6].trim();

				Response response = student.responses.get( qId );

				if( response == null ){
					response = new Response( qId, qType );
				}

				if( action.equals("Enter Item") ) {
					response.enterTime.add( eventTime );
				}else if( action.equals( "Exit Item" )  ){
					response.exitTime.add( eventTime );
				}else{

					if( action.length() >  0 ){

						Integer count = response.obserableCounts.get( action );
						if( count == null)
							count = 0;
						count++;
						response.obserableCounts.put(  action, count );
					}
					if( info.length() >  0){
						Integer count = response.infoCounts.get( info );
						if( count == null)
							count = 0;
						count++;
						response.infoCounts.put( info, count );
					}

				}
				student.responses.put( qId , response );
			}
			
			
			
		}
		
		/*
		System.err.println(">> "+ students.size() );

		for(String id: students.keySet() ){

			Student student = students.get( id );
			Response response = null;
			String enterTime = null;
			String exitTime = null;
			String lastTime = null;
			String qId = null;
			double timeTaken = 0.0d;

			for( String activity: student.activities ){

				try{
					String[] token = activity.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
					qId =  token[2].trim();
					String qType =  token[3].trim();
					String action =  token[4].trim();
					String info =  token[5].trim();
					String eventTime =  token[6].trim();

					if( action.equals("Enter Item") ) {

						response = student.responses.get( qId );

						if( response == null ){
							response = new Response( qId, qType );
						}

						enterTime = eventTime;

					}else if( action.equals( "Exit Item" )  ){

						exitTime = eventTime;

						try{
							Timestamp et = Timestamp.valueOf( enterTime);
							Timestamp xt = Timestamp.valueOf( exitTime );
							long milliseconds = xt.getTime() - et.getTime();
							double seconds = (double) milliseconds / 1000;
							timeTaken = (float) ( seconds % 3600 ) / (double) 60 ;

						}catch(Exception e){
							timeTaken = 0.0d;
						}
						
						response.totalTime += timeTaken;
						response.attempts++;
						
						Integer count = student.typeCount.get( response.type );

						if( count == null)
							count = 0;
						count++;
						student.typeCount.put( response.type, count );
						student.responses.put( qId, response );

					}else {

						
						if( response != null ){

							lastTime = eventTime;

							if( action.length() >  0 ){

								Integer count = response.obserableCounts.get( action );
								if( count == null)
									count = 0;
								count++;
								response.obserableCounts.put(  action, count );
							}

							if( info.length() >  0){

								Integer count = response.infoCounts.get( info );
								if( count == null)
									count = 0;
								count++;
								response.infoCounts.put( info, count );
							}
						}
					}   	
				}catch(Exception e){
					System.err.println("Activity: "+activity );
					System.exit(0);
					e.printStackTrace();
				}
			}

			if( exitTime == null && lastTime != null ){

				exitTime = lastTime;

				try{
					Timestamp et = Timestamp.valueOf( enterTime );
					Timestamp xt = Timestamp.valueOf( exitTime );
					long milliseconds = xt.getTime() - et.getTime();
					double seconds = (double) milliseconds / 1000;
					timeTaken = (float) ( seconds % 3600 ) / (double) 60 ;

                               	}catch(Exception e){
                                       	timeTaken = 0.0d;
                               	}
					
				response.totalTime += timeTaken;
				response.attempts++;

				Integer count = student.typeCount.get( response.type );

				if( count == null)
					count = 0;
				count++;
				student.typeCount.put( response.type, count );

				student.responses.put( qId, response );
			}

			students.put( id , student );
		}
		*/
	}

	void preprocessingQuestions(){

		for( String id: students.keySet() ){
			Student student = students.get( id );
			for(String qId: student.responses.keySet() ){
				Response response = student.responses.get( qId );

			
				Question question =  questions.get( qId );
				if( question  == null ){
					question = new Question( qId, response.type );
				}
				question.students.add( id );
				question.timeTakens.add( response.totalTime );
				for(String key: response.obserableCounts.keySet() ){
					TreeSet<Integer> counts = question.observables.get( key );
					if( counts == null ){
						counts = new TreeSet<Integer>();
					} 
					counts.add( response.obserableCounts.get( key )  );
					question.observables.put( key, counts );
				}
				for(String key: response.infoCounts.keySet() ){
					TreeSet<Integer> counts = question.infos.get( key );
					if( counts == null ){
						counts = new TreeSet<Integer>();
					} 
					counts.add( response.infoCounts.get( key )  );
					question.infos.put( key, counts );
				}
				questions.put( qId, question );	
			}	

			for( String type: student.typeCount.keySet() ){

				TreeSet<Integer> counts = qTypeCounts.get( type );
				if( counts == null){
					counts = new TreeSet<Integer>();
				}
				counts.add( student.typeCount.get( type ) );	
				qTypeCounts.put( type, counts );
			}
			
		}
		
	}

	void calculatePercentile(){
		this.qTypeCountPercentile = Utility.calculatePercentile( qTypeCounts );
		this.totalTimePercentile = Utility.calculatePercentile( totalTimes );
		for(String qid: this.questions.keySet() ){
			this.questions.get( qid ).calulatePerCentile();
		}
	}

	void preprocessing(){
		preprocessingStudents();		
		preprocessingQuestions();
		for(String qid: questions.keySet() ){
			questions.get( qid ).calulatePerCentile();
		}
		calculatePercentile();	
	}

	void printHeader(){

		System.out.println("@relation neap-training-data-set");
		System.out.println("@attribute time {0.3 , 0.2, 0.1}");

		for(String type: qTypeCounts.keySet() ){
			System.out.println("@attribute "+type+" numeric");	
		}

		for(String key: questions.keySet() ){

			Question question = questions.get( key );	

			System.out.println("@attribute Q-"+question.id+"-time-taken-percentile numeric");	
			System.out.println("@attribute Q-"+question.id+"-attempt numeric");	



			for(String key1: questions.get( key ).observables.keySet() ){
				System.out.println("@attribute Q-"+question.id+"-"+key1+"-percentile numeric");	
			}

			for(String key1: questions.get( key ).infos.keySet() ){
				System.out.println("@attribute Q-"+question.id+"-"+key1+"-percentile numeric");	
			}
		}

		System.out.println("@attribute class {1.0, 0.0}");	
	}

	void printData(){

		for( String id: students.keySet() ){

			Student student = students.get( id );
			System.out.print( student.lable );

			for(String qid: questions.keySet() ){

				Question question = questions.get( qid );
				Response response = student.responses.get( qid );

				System.out.print(", "+ question.timeTakenPercentiles.get( response.totalTime ) );
				System.out.print(", "+ response.attempts );

				for(String type: qTypeCounts.keySet() ){

				}
			}
		}
	}

	void print(){

		printHeader();
		printData();
	}
}

class Training{

	Data data30;
	Data data20;
	Data data10;

	Map<String, Student> students;
	Map<String, Question> questions;

	Training(){

		this.students = new TreeMap<String, Student>();
		this.questions = new TreeMap<String, Question>();

		this.data30 = new Data("0.3");
		this.data20 = new Data("0.2");
		this.data10 = new Data("0.1"); 
	}

	void createDataSet(){

		System.err.println("All "+ students.size() );	

		for(String id: students.keySet() ){

			Student student = students.get( id );

			Student student30 = new Student( id );
			Student student20 = new Student( id );
			Student student10 = new Student( id );

			student30.results.put("B", student.results.get("B"));
			student20.results.put("B", student.results.get("B"));
			student10.results.put("B", student.results.get("B"));

			String startTime = null;
			double timeTaken = 0.0d;

			for(String activity: student.activities ){

				String[] token = activity.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

				if( startTime == null ){

					startTime = token[6].trim();
					student20.activities.add( activity );
					student10.activities.add( activity );

				}else{
					try{
						Timestamp et = Timestamp.valueOf( startTime );
						Timestamp xt = Timestamp.valueOf( token[6].trim() );
						long milliseconds = xt.getTime() - et.getTime();
						double seconds = (double) milliseconds / 1000;
						timeTaken = (float) ( seconds % 3600 ) / (double) 60;

						if( timeTaken < 11 ){
							student10.activities.add( activity );
							student10.totalTime = timeTaken;
						}
						if( timeTaken < 21){
							student20.activities.add( activity );
							student20.totalTime = timeTaken;
						}		
						student30.activities.add( activity );
						student30.totalTime = timeTaken;

					}catch(Exception e){
						System.err.println("Time: "+token[6].trim() );
					}	
				}
			}

			data30.students.put( id, student30 );
			data20.students.put( id, student30 );
			data10.students.put( id, student30 );
		}
		System.err.println("30 >>"+ data30.students.size() );
		System.err.println("20 >>"+ data20.students.size() );
		System.err.println("10 >>"+ data10.students.size() );
	}
			

	void read( String studentFile, String classFile, boolean headerStudent, boolean headerClass ){

		BufferedReader br = null;
		String line = null;
		int count = 0;

		try{
			br = new BufferedReader(new FileReader( new File( studentFile ) ) );

			while( (line = br.readLine() ) != null ){

				count++;
				if( headerStudent ){
					headerStudent = false;
					continue;
				}

				String[] token =  line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
				Student student = students.get( token[0].trim() );	

				if( student == null ){
					student = new Student( token[0].trim());
				}
				student.activities.add( line.trim() );
				students.put( token[0].trim(), student );
			}

	
		}catch(Exception e){
			System.err.println("Line no:"+count+"  Line:"+line);
		}
		System.err.println("Record read: "+count );
		System.err.println("Students Record: "+students.size() );
		
		count = 0;
	
		try{
			br = new BufferedReader(new FileReader( new File( classFile ) ) );
			while( (line = br.readLine() ) != null ){
				count++;
				if( headerClass ){
					headerClass = false;
					continue;
				}
				String[] token =  line.split(",");
				Student student = students.get( token[0].trim() );	
				if( student == null ){
					System.err.println("Student: "+token[0].trim()+" not found!");
					System.exit(0);
				}

				if( token[1].trim().equals("True") )
					student.results.put("B", "1.0");
				else
					student.results.put("B", "0.0");
			}

		}catch(Exception e){
			System.err.println("Line no:"+count+"  Line:"+line);
		}
		System.err.println("Students Class read: "+count );
	}

	void preprocessing(){

		data30.preprocessing();
		data20.preprocessing();
		data10.preprocessing();
	}
	
	void printData(){

		data30.print();
		data20.print();
		data10.print();
	}

	public static void main(String[] args){

		String studentFile = null;
		String classFile = null;
		int i = 0;
		while( i < args.length ){
			if( args[i].equals("-sf"))
				studentFile = args[ ++i ];
			else if( args[i].equals("-cf"))
				classFile = args[ ++i ];
			i++;
		}
		
		if( studentFile == null || classFile == null ){
			System.err.println("Uses: -sf [student-file]  -cf [class-file]");
			System.exit(0);
		}

		Training training = new Training();
		training.read( studentFile, classFile, true, true );	 
		training.createDataSet();
		training.preprocessing();
		training.printData();
	}
}  
