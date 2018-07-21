package com.impinj.itemsense.scheduler.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.sun.rowset.CachedRowSetImpl;

/*
 * FROM: https://gist.githubusercontent.com/jewelsea/4955598/raw/ab83b0e5cdacc2c564f0ebc087a7601e1b556247/H2app.java
 */
public class H2Util {
	private static final String H2_DRIVER = "org.h2.Driver";
    private static Connection conn = null;
	private static final Logger logger = Logger.getLogger(H2Util.class.getName());
	
	//private static final String[] SAMPLE_NAME_DATA = { "John", "Jill", "Jack", "Jerry" };

	// final ListView<String> nameView = new ListView();
	// fetchNamesFromDatabaseToListView(nameView);
	// nameView.getItems().clear();
	
	
    public static void dbConnect() throws SQLException, ClassNotFoundException {

        try {
            Class.forName(H2_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your H2 JDBC Driver?");
            e.printStackTrace();
            throw e;
        }
 
        System.out.println("H2 JDBC Driver Registered!");
 
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console" + e);
            e.printStackTrace();
            throw e;
        }
    }
    
    //Close Connection
    public static void dbDisconnect() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    //DB Execute Query Operation
    public static ResultSet dbExecuteQuery(String queryStmt) throws SQLException, ClassNotFoundException {
        //Declare statement, resultSet and CachedResultSet as null
        Statement stmt = null;
        ResultSet resultSet = null;
        CachedRowSetImpl crs = null;
        try {
            //Connect to DB (Establish Connection)
            dbConnect();
            System.out.println("Select statement: " + queryStmt + "\n");
 
            //Create statement
            stmt = conn.createStatement();
 
            //Execute select (query) operation
            resultSet = stmt.executeQuery(queryStmt);
 
            //CachedRowSet Implementation
            //In order to prevent "java.sql.SQLRecoverableException: Closed Connection: next" error
            //We are using CachedRowSet
            crs = new CachedRowSetImpl();
            crs.populate(resultSet);
        } catch (SQLException e) {
            System.out.println("Problem occurred at executeQuery operation : " + e);
            throw e;
        } finally {
            if (resultSet != null) {
                //Close resultSet
                resultSet.close();
            }
            if (stmt != null) {
                //Close Statement
                stmt.close();
            }
            //Close connection
            dbDisconnect();
        }
        //Return CachedRowSet
        return crs;
    }
 
    //DB Execute Update (For Update/Insert/Delete) Operation
    public static void dbExecuteUpdate(String sqlStmt) throws SQLException, ClassNotFoundException {
        //Declare statement as null
        Statement stmt = null;
        try {
            //Connect to DB (Establish Connection)
            dbConnect();
            //Create Statement
            stmt = conn.createStatement();
            //Run executeUpdate operation with given sql statement
            stmt.executeUpdate(sqlStmt);
        } catch (SQLException e) {
            System.out.println("Problem occurred at executeUpdate operation : " + e);
            throw e;
        } finally {
            if (stmt != null) {
                //Close statement
                stmt.close();
            }
            //Close connection
            dbDisconnect();
        }
    }
    
    
/**
	private void fetchNamesFromDatabaseToListView(ListView listView) {
		try (Connection con = getConnection()) {
			if (!schemaExists(con)) {
				createSchema(con);
				populateDatabase(con);
			}
			listView.setItems(fetchNames(con));
		} catch (SQLException | ClassNotFoundException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}



	private void createSchema(Connection con) throws SQLException {
		logger.info("Creating schema");
		Statement st = con.createStatement();
		String table = "create table employee(id integer, name varchar(64))";
		st.executeUpdate(table);
		logger.info("Created schema");
	}

	private void populateDatabase(Connection con) throws SQLException {
		logger.info("Populating database");
		Statement st = con.createStatement();
		int i = 1;
		for (String name : SAMPLE_NAME_DATA) {
			st.executeUpdate("insert into employee values(i,'" + name + "')");
			i++;
		}
		logger.info("Populated database");
	}

	private boolean schemaExists(Connection con) {
		logger.info("Checking for Schema existence");
		try {
			Statement st = con.createStatement();
			st.executeQuery("select count(*) from employee");
			logger.info("Schema exists");
		} catch (SQLException ex) {
			logger.info("Existing DB not found will create a new one");
			return false;
		}

		return true;
	}

	private ObservableList<String> fetchNames(Connection con) throws SQLException {
		logger.info("Fetching names from database");
		ObservableList<String> names = FXCollections.observableArrayList();

		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery("select name from employee");
		while (rs.next()) {
			names.add(rs.getString("name"));
		}

		logger.info("Found " + names.size() + " names");

		return names;
	}
	
*/
	
}