package cdac.in.naep;

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

	static Map<Integer,Double> calculatePercentile_( Set<Integer> data ){ 

		Map<Integer, Double> percentiles = new TreeMap<Integer, Double>();

		ArrayList<Integer> all = new ArrayList<Integer>( data );
		Collections.sort( all );
		for(int i = 0; i < all.size(); i++){
			double p = Math.round( (double ) i  / (double) all.size() * (double) 100 ) / (double) 100;
			percentiles.put( all.get( i ), p  );
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
	Set<Integer> attempts;
	Set<Integer> observables;
	Set<Integer> infos;

	Map<Double, Double> timeTakenPercentiles;
	Map<Integer, Double> attemptsPercentile;
	Map<Integer, Double> observablesPercentiles;
	Map<Integer, Double> infosPercentiles;

	Question(String id, String type){

		this.id = id;
		this.type = type;

		this.fifthPercentile = 0.0d;
		this.students = new TreeSet<String>();
		this.timeTakens = new TreeSet<Double>();
		this.attempts = new TreeSet<Integer>();
		this.observables = new TreeSet<Integer>();
		this.infos = new TreeSet<Integer>(); 

		this.timeTakenPercentiles = new TreeMap<Double, Double>();
		this.attemptsPercentile = new TreeMap<Integer, Double>();

		this.observablesPercentiles = new TreeMap<Integer, Double>();
		this.infosPercentiles = new TreeMap<Integer, Double>(); 
	}

	void calulatePerCentile(){

		ArrayList<Double> all = new ArrayList<Double>( timeTakens );
		Collections.sort( all );

		long index = Math.round( (  (double) 5 / (double) 100 ) *  ( double ) all.size() );
		this.fifthPercentile = all.get ( (int) index );

		this.timeTakenPercentiles = Utility.calculatePercentile( this.timeTakens ); 
		this.observablesPercentiles = Utility.calculatePercentile_( this.observables );
		this.infosPercentiles = Utility.calculatePercentile_( this.infos );
		this.attemptsPercentile = Utility.calculatePercentile_( this.attempts );
	}

}

class Response{

	String id;
	String type;

	List<String> enterTime;
	List<String> exitTime;	
	
	Double totalTime;
	Double percentileTime;

	Integer obserableCounts;
	Integer infoCounts;
	Integer attempts;

	Response(String id, String type){

		this.type = type;	
		this.id = id;

		this.enterTime = null;
		this.exitTime = null;

		this.totalTime = 0.0d;
		this.percentileTime = 0.0d;		
		this.attempts = 0;

		this.obserableCounts = 0;
		this.infoCounts = 0;

		this.enterTime = new ArrayList<String>();
		this.exitTime = new ArrayList<String>();
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
	Set<Double> totalTimes;

	Map<String, Student> students;
	Map<String, Question> questions;

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
						response.obserableCounts++;
					}

					if( info.length() >  0){
						response.infoCounts++;
					}

				}
				student.responses.put( qId , response );
			}

			List<String> removeList = new ArrayList<String>();
		
			for(String qId: student.responses.keySet() ){

				Response response = student.responses.get( qId );

				if( response.enterTime.size() == response.exitTime.size() ){

					for( int i = 0; i < response.enterTime.size(); i++ ){

						Timestamp et = Timestamp.valueOf( response.enterTime.get( i ) );
						Timestamp xt = Timestamp.valueOf( response.exitTime.get( i ) );

						long milliseconds = xt.getTime() - et.getTime();
						double seconds = (double) milliseconds / 1000;
						double timeTaken = (float) ( seconds % 3600 ) / (double) 60 ;

						if( timeTaken > 0 ){	
							response.totalTime += timeTaken;
							response.attempts++;
						}
					}

				}
				if( response.totalTime > 0 ){
					Integer count = student.typeCount.get( response.type );
					if( count == null)
						count = 0;
					count++;
					student.typeCount.put( response.type, count );
				}else{
					removeList.add( qId );
				}
				student.responses.put( qId, response );
			}

			for(String qId: removeList ){
				student.responses.remove( qId );
			}
			students.put( id , student );
		}
	}

	void preprocessingQuestions(){

		for( String id: students.keySet() ){

			Student student = students.get( id );
			double totalTime = 0.0d;

			for(String qId: student.responses.keySet() ){

				Response response = student.responses.get( qId );

				if( response.totalTime > 0 ){

					Question question =  questions.get( qId );

					if( question  == null ){
						question = new Question( qId, response.type );
					}

					question.students.add( id );
					question.timeTakens.add( response.totalTime );
					question.attempts.add( response.attempts );
					question.observables.add ( response.obserableCounts );
					question.infos.add( response.infoCounts );
					questions.put( qId, question );	
					totalTime +=  response.totalTime;
				}
			}

			if( student.totalTime > 0){
				totalTimes.add ( student.totalTime );
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

		System.err.println("calculate-percentile");
		System.err.println("QtypeCounts: "+qTypeCounts.size() );
		System.err.println("totalTimes: "+totalTimes.size() );

		this.qTypeCountPercentile = Utility.calculatePercentile( qTypeCounts );
		this.totalTimePercentile = Utility.calculatePercentile( totalTimes );
		
		for(String qid: this.questions.keySet() ){
			this.questions.get( qid ).calulatePerCentile();
		}
	}

	void preprocessing(){
		preprocessingStudents();		
		preprocessingQuestions();
		calculatePercentile();	
	}

	void printHeader(){

		System.out.println("@relation neap-training-data-set");
		System.out.println("@attribute time {0.3, 0.2, 0.1}");
		System.out.println("@attribute TotalTime-percentile numeric");	

		for(String type: qTypeCounts.keySet() ){

			if( qTypeCountPercentile.get( type ).size() > 1 ){
				System.out.println("@attribute "+type+"-Type numeric");	
			}
		}

		for(String key: questions.keySet() ){

			Question question = questions.get( key );	

			if( question.timeTakenPercentiles.size() > 1){

				System.out.println("@attribute @"+key+"-TimeTaken numeric");	
				System.out.println("@attribute @"+key+"-Attempts numeric");	
				System.out.println("@attribute @"+key+"-obserable numeric");	
				//System.out.println("@attribute @"+key+"-info numeric");	
			}
		
		}

		System.out.println("@attribute class {1.0, 0.0}");	
		System.out.println("@data");	
	}

	void printData(Data master){


		for( String id: students.keySet() ){

			Student student = students.get( id );
			if( student.totalTime < 9 )
			continue;
			System.out.print( lable );
			System.out.print(", "+totalTimePercentile.get( student.totalTime ) );

			for(String type: master.qTypeCounts.keySet() ){

				if( master.qTypeCountPercentile.get( type ).size() > 1 ){

					if( student.typeCount.get( type ) != null && qTypeCountPercentile.get( type ) != null ){
						System.out.print(", "+qTypeCountPercentile.get( type ).get( student.typeCount.get( type ) ) );	
					}else{
						System.out.print(", 0.0");	
					}
				}
			} 
			for(String key: master.questions.keySet() ){

				Question question = questions.get( key );	
				Response response = student.responses.get( key );

				if( master.questions.get( key ).timeTakenPercentiles.size() > 1){

					if( response != null && question != null && question.timeTakenPercentiles.get( response.totalTime ) != null )
						System.out.print(", "+question.timeTakenPercentiles.get( response.totalTime ) );
					else
						System.out.print(", 0.0");
					
					if( response != null && question != null && question.attemptsPercentile.get( response.attempts )  != null )
						System.out.print(", "+question.attemptsPercentile.get( response.attempts ) );
					else
						System.out.print(", 0.0");
					
					if( response != null && question != null && question.observablesPercentiles.get( response.obserableCounts )  != null )
						System.out.print(", "+question.observablesPercentiles.get( response.obserableCounts ) );
					else
						System.out.print(", 0.0");

					/*

					if( response != null && question != null && question.infosPercentiles.get( response.infoCounts )  != null )
						System.out.print(", "+question.infosPercentiles.get( response.infoCounts ) );
					else
						System.out.print(", 0.0");
					*/
				}	
			}
			System.out.println(", "+student.results.get("B"));
		}
	}

	void print( Data master ){
		printData(  master );
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
			data20.students.put( id, student20 );
			data10.students.put( id, student10 );
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

		data30.printHeader();
		data30.print( data30 );
		data20.print( data30 );
		data10.print( data30 );
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
