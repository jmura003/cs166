/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *SELECT fname, lname
FROM Customer,(
                SELECT customer_id,COUNT(customer_id) as car_num
                FROM Owns
                GROUP BY customer_id
                HAVING COUNT(customer_id) > 20
        ) AS O
WHERE O.customer_id = id;

 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
//import java.sql.*;
import java.util.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.*;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddCustomer(MechanicShop esql){//1
		//int trigger = 0;
		try{
			//String test = "SELECT MAX(id) FROM Customer;";
			//esql.executeQueryAndPrintResult(test);
			//test = "SELECT nextval(
			String init = "SELECT C.id FROM Customer C;";
			List<List<String>> data = esql.executeQueryAndReturnResult(init);
			System.out.println("ID: " + data.size());
			int id = data.size();
			
			System.out.println("Enter your first name: ");
			String fname = in.readLine(); 
			while(fname.length() > 32 || fname.length() <= 0){
			  System.out.println("Error");
			  System.out.println("Enter your first name: ");
			  fname = in.readLine();
			}
			fname = fname.substring(0,1).toUpperCase() + fname.substring(1);
			System.out.println(fname);
			
			
			System.out.println("Enter your last name: ");
			String lname = in.readLine();
			while(lname.length() > 32 || lname.length() <= 0){
			  System.out.println("Error");
			  System.out.println("Enter your Last name: ");
			  lname = in.readLine();
			}	
			
			lname = lname.substring(0,1).toUpperCase() + lname.substring(1);
			
			System.out.println("Enter your phone number:(###-###-####)");
			String phone = in.readLine();
			while(phone.length() > 13 || phone.length() <= 0){
			  System.out.println("Error");
			  System.out.println("Enter your phone number: ");
			  phone = in.readLine();
			}
			
			
			System.out.println("Enter your address: ");
			String address = in.readLine();
			while(address.length() > 256 || address.length() <= 0){
			  System.out.println("Error");
			  System.out.println("Enter your address: ");
			  address = in.readLine();
			}
	
			String query = String.format("INSERT INTO Customer(id, fname, lname, phone, address) VALUES (%d, '%s', '%s', '%s', '%s');", id, fname, lname, phone, address);
			System.out.println(query);
			esql.executeQuery(query);
			//int rowCount = esql.executeQuery(query);
			//String test2 = "SELECT C.* FROM Customer C WHERE C.id = 500;";
			//esql.executeQueryAndPrintResult(test2);
			//System.out.println("total rows: " + rowCount);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			//if (trigger == 1) {
			//  System.out.println("invalid input value");
			//} 
			//AddCustomer(esql);	
		}
		
	}
	
	public static void AddMechanic(MechanicShop esql){//2
		try{
			//String test = "SELECT MAX(id) FROM Mechanic;";
			//esql.executeQueryAndPrintResult(test);
			//test = "SELECT nextval(
			String init = "SELECT M.id FROM Mechanic M;";
			List<List<String>> data = esql.executeQueryAndReturnResult(init);
			System.out.println("ID: " + data.size());
			int id = data.size();
			
			System.out.println("Enter your first name: ");
			String fname = in.readLine(); 
			while(fname.length() > 32 || fname.length() <= 0){
			  System.out.println("Error");
			  System.out.println("Enter your first name: ");
			  fname = in.readLine();
			}
			
			fname = fname.substring(0,1).toUpperCase() + fname.substring(1);
	
			System.out.println("Enter your last name: ");
			String lname = in.readLine();
			while(lname.length() > 32 || lname.length() <= 0){
			  System.out.println("Error");
			  System.out.println("Enter your Last name: ");
			  lname = in.readLine();
			}	
			
			lname = lname.substring(0,1).toUpperCase() + lname.substring(1);
			
			System.out.println("How many years of experience do you have? ");
			int experience = Integer.parseInt(in.readLine());
			while(experience < 0 || experience > 100){
			  System.out.println("Error");
			  System.out.println("How many years of experience do you have? ");
			  experience = Integer.parseInt(in.readLine());
			}
			String query = String.format("INSERT INTO Mechanic(id, fname, lname, experience) VALUES (%d, '%s', '%s', %d);", id, fname, lname, experience);
			System.out.println(query);
			esql.executeQuery(query);
			int rowCount = esql.executeQuery(query);
			System.out.println("total rows: " + rowCount);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void AddCar(MechanicShop esql){//3
		try{
			//int id = 0;
			System.out.println("What is the VIN of your car?(First 6 characters are letters, last 10 are numbers)");
			String vin = in.readLine();
			vin = vin.toUpperCase();
			//System.out.println(vin);
			String query = String.format("SELECT C.vin FROM Car C WHERE C.vin = '%s';", vin);
			
			//List<List<String>> test = esql.executeQueryAndReturnResult(query);
			//System.out.println(test.getString(0));
			while(esql.executeQuery(query) != 0 || vin.length() > 14){
				System.out.println("VIN has to be unique or vin too long");
				System.out.println("RE-Enter your VIN:");
				vin = in.readLine();
				vin = vin.toUpperCase();
				query = String.format("SELECT C.vin FROM Car C WHERE C.vin = '%s';", vin);
			}
			System.out.println("Enter the make of your car: ");
			String make = in.readLine();
			while(make.length() < 0 || make.length() > 32){
				System.out.println("Error");
				System.out.println("Enter the make of your car");
				make = in.readLine();
			}
			
			System.out.println("Enter the Model of your car");
			String model = in.readLine();
			while(model.length() < 0 || model.length() > 32){
				System.out.println("Error");
				System.out.println("Enter the model of your car");
				model = in.readLine();
			}
			
			System.out.println("what year is the model of your car");
			int year = Integer.parseInt(in.readLine());
			while(year <1970){
				System.out.println("not a valid year");
				System.out.println("what year is the model of your car");
				year = Integer.parseInt(in.readLine());
			} 
			
			System.out.println("What is your last name"); 
			String lname = in.readLine();
			lname = lname.substring(0,1).toUpperCase() + lname.substring(1);
			//lname.charAt(0) = lname.charAt(0).toUpperCase();
			
			System.out.println("What is your first name");
			String fname = in.readLine();
			fname = fname.substring(0,1).toUpperCase() + fname.substring(1);
			String findowner = "SELECT id FROM Customer WHERE lname = '" + lname + "' AND fname = '" + fname + "';";
			//String findowner = String.format("SELECT id FROM Customer WHERE lname = '%s';", lname); 
			List<List<String>> cust_id = esql.executeQueryAndReturnResult(findowner);
			System.out.println(cust_id);
			
			String insert = String.format("INSERT INTO Car(vin, make, model, year) VALUES('%s', '%s', '%s', %d);", vin, make, model, year);
			//esql.executeQuery(insert);
			//esql.executeQuery(insert);
			
			String add_owns = "SELECT ownership_id FROM Owns;";
			//String cust_id = "SELECT 
			List<List<String>> owners = esql.executeQueryAndReturnResult(add_owns);
			int ownersize = owners.size();
			System.out.println("owner_id " + ownersize);
			String add_to_owns = String.format("INSERT INTO Owns (ownership_id, customer_id, car_vin) VALUES (%d, %d, '%s');", ownersize, Integer.parseInt(cust_id.get(0).get(0)), vin);
			System.out.println(add_to_owns);
			//String tot = "SELECT vin FROM Car";
			//List<List<String>> num_cars = esql.executeQueryAndReturnResult(tot);
			
		
			
			//esql.executeQueryAndPrintResult(insert);
			esql.executeQuery(insert + "\n" + add_to_owns);
			//System.out.println("this is an error: " + error);
			//System.out.println("total cars: " + num_cars.size());
			
			//esql.executeQuery(add_to_owns);
			
			
			
			
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		try{
			String test = "SELECT rid FROM Service_Request;";
			List<List<String>> serviceRequest = esql.executeQueryAndReturnResult(test);
			int rid = serviceRequest.size();
			System.out.println("rid: " + rid);

			System.out.println("What is your last name: ");
			String lName = in.readLine();

			String customerQuery = "SELECT C.id, C.fname, C.lname FROM Customer C WHERE C.lName = '" + lName + "';";
			List<List<String>> customers = esql.executeQueryAndReturnResult(customerQuery);
			if(customers.size() == 0){
				AddCustomer(esql);
			}
			esql.executeQueryAndPrintResult(customerQuery);
			/*System.out.println("Enter some shit");
			String innn = in.readLine();
			System.out.println(customers.get(0).get(2));
			if (customers.get(0).get(0).equals(innn.trim())){
				System.out.println("congratulations buddy")
			*/
			System.out.println("What is your customer id: ");
			int cid = Integer.parseInt(in.readLine());

			String vehicleQuery = "SELECT C.vin FROM Owns O, Car C WHERE O.car_vin=C.vin AND O.customer_id="+cid + ";";
			List<List<String>> cars = esql.executeQueryAndReturnResult(vehicleQuery);
			if(cars.size() == 0){
				AddCar(esql);
			}
			//esql.executeQueryAndPrintResult(vehicleQuery);
			List<List<String>> v = esql.executeQueryAndReturnResult(vehicleQuery);
			for (int i = 0; i < v.size(); i++){
				System.out.println((i + 1) + ". " + v.get(i).get(0));
			}

			//System.out.println
			String selection = String.format("Which VIN would you like to make a service request for(%d to %d:) ", 1, v.size());
			System.out.println(selection);
			int index = Integer.parseInt(in.readLine());
			while(index < 1 || index > v.size()){
				System.out.println("not valid");
				System.out.println(selection);
				index = Integer.parseInt(in.readLine());
			}
			String vin = v.get(index - 1).get(0);
			System.out.println(vin);
			
			int old_odo = 0;
			String s = "Select * FROM Service_Request WHERE car_vin = '" + vin + "';";
			int rowcount = esql.executeQuery(s);
			//String pre_od = "SELECT MAX(odometer) FROM Service_Request WHERE car_vin = '" + vin + "';";
			//List<List<String>> prev_odometer = esql.executeQueryAndReturnResult(pre_od);
			//int rowcount = executeQuery(pre_od);
			if (rowcount > 0){
				String pre_od = "SELECT MAX(odometer) FROM Service_Request WHERE car_vin = '" + vin + "';";
				List<List<String>> prev_odometer = esql.executeQueryAndReturnResult(pre_od);
				//System.out.println("wonderful");
				if (prev_odometer.size() > 0) {
					old_odo = Integer.parseInt(prev_odometer.get(0).get(0));
				}
			}
			
			System.out.println("Your last Odometer reading was: " +  old_odo);
			
			System.out.println("What does your odometer say?");
			int odometer = Integer.parseInt(in.readLine());
			
			
			
			while(odometer < old_odo) {
				System.out.println("Odometer must be > " + old_odo);
				odometer = Integer.parseInt(in.readLine());
			}

			System.out.println("What is your complaint?");
			String complaint = in.readLine();

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String stringDate = dateFormat.format(date);

			String query = "INSERT INTO Service_Request(rid, customer_id, car_vin, date, odometer, complain) VALUES (" + rid + ","+cid +",'"+ vin + "','" + stringDate + "'," + odometer + ",'" + complaint + "');";

			esql.executeQuery(query);
			

			
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception
	{//5
		try{
			//int emp_id;
			int mid;
			int rid;
			int wid;

			String widQuery = "SELECT wid FROM Closed_Request;";
			List<List<String>> widRequest = esql.executeQueryAndReturnResult(widQuery);
			wid = widRequest.size() + 1;


			String test = "SELECT rid FROM Service_Request;";
			List<List<String>> serviceRequest = esql.executeQueryAndReturnResult(test);
			System.out.println("what is your service request number");
			rid = Integer.parseInt(in.readLine());

			while(rid < 0 || rid > serviceRequest.size()){
				System.out.println("invalid rid");
				rid = Integer.parseInt(in.readLine());
			}

			System.out.println("what is your Mechanic id");
			mid = Integer.parseInt(in.readLine());
			String emp_id = "SELECT mid FROM Closed_Request";
			List<List<String>> mid_list = esql.executeQueryAndReturnResult(emp_id);
			while(mid < 0 || mid > mid_list.size()){
				System.out.println("invalid mid");
				mid = Integer.parseInt(in.readLine());
			}

			String dateQuery = "SELECT date FROM Service_Request WHERE rid = '" + rid + "'";
			List<List<String>> dateService = esql.executeQueryAndReturnResult(dateQuery);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

			String openedDate = dateService.get(0).get(0);
			Date openDate = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(openedDate);
			
			//String stringDate = dateFormat.format(openedDate));
			
			System.out.println("What's the close date?(Format: MM-dd-yyyy)");
			String closedDate = in.readLine();
			
			Date closeDate = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(closedDate);

			/*
			Date currentDate = dateFormat.parse(closeDate);
			*/
			while(!openDate.before(closeDate)){
				System.out.println("Incorrect Date");
				CloseServiceRequest(esql);
			}
			

			System.out.println("What is the mechanic's comment?");
			String mComment = in.readLine();

			System.out.println("What's the bill?");
			int bill = Integer.parseInt(in.readLine());
			String query = "INSERT INTO Closed_Request(wid,rid,mid,date,comment, bill) VALUES (" + wid + ","+rid +","+ mid + ",'" + closeDate + "','" + mComment + "'," + bill +")";

			esql.executeQuery(query);	

			}
			catch(Exception e){
			System.out.println(e.getMessage());
			} 
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try {
			String query = "SELECT date, comment, bill FROM Closed_Request WHERE bill < 100;";
			int rowcount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowcount);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		try {
			String query = "SELECT fname, lname FROM Customer,( SELECT customer_id,COUNT(customer_id) as car_num FROM Owns GROUP BY customer_id HAVING COUNT(customer_id) > 20 ) AS O WHERE O.customer_id = id;";
			int rowcount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowcount);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}	
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try {
                        String query = "SELECT DISTINCT make,model, year FROM Car AS C, Service_Request AS S WHERE year < 1995 and S.car_vin = C.vin and S.odometer < 50000;";
                        int rowcount = esql.executeQueryAndPrintResult(query);
                        System.out.println("total row(s): " + rowcount);
                }
                catch(Exception e){
                        System.out.println(e.getMessage());
                }
		
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		try {
				System.out.println("Enter a value for k");
				int k = Integer.parseInt(in.readLine());
                String query = "SELECT make, model, R.creq FROM Car AS C, ( SELECT car_vin, COUNT(rid) AS creq FROM Service_Request GROUP BY car_vin ) AS R WHERE R.car_vin = C.vin ORDER BY R.creq DESC LIMIT " + k + ";"; // 10;
                int rowcount = esql.executeQueryAndPrintResult(query);
                System.out.println("total row(s): " + rowcount);
            }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		try {
                        String query = "SELECT C.fname , C.lname, Total FROM Customer AS C, (SELECT sr.customer_id, SUM(CR.bill) AS Total FROM Closed_Request AS CR, Service_Request AS SR WHERE CR.rid = SR.rid GROUP BY SR.customer_id) AS A WHERE C.id=A.customer_id ORDER BY A.Total DESC;";
                        int rowcount = esql.executeQueryAndPrintResult(query);
                        System.out.println("total row(s): " + rowcount);
                }
                catch(Exception e){
                        System.out.println(e.getMessage());
                }

		
	}
	
}
