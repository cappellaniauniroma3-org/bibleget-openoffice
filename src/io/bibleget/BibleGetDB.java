/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;


/**
 *
 * @author Lwangaman
 */
public class BibleGetDB {

    //private final String dbPath;
    private static BibleGetDB instance = null;
    private Connection conn = null;
    //private DatabaseMetaData dbMeta = null;
    //private ResultSet rs = null;
    
    private final List<String> colNames = new ArrayList<>();
    private final List<Class> colDataTypes = new ArrayList<>();
    
    
    private BibleGetDB() throws ClassNotFoundException {
        try {
            setDBSystemDir();
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");            
        } catch (Exception ex) {
            Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static BibleGetDB getInstance() throws ClassNotFoundException {
        if(instance == null)
        {            
            instance = new BibleGetDB();
            boolean dbInitialized = instance.initialize();
            if(dbInitialized){ 
                //System.out.println("Database is initialized too!"); 
            }
            else{ 
                //System.out.println("Sorry but database has not been initialized."); 
            }
        }
        return instance;        
    }

    public boolean initialize() {
    
        try {
            conn = DriverManager.getConnection(
                    "jdbc:derby:BIBLEGET;create=true",
                    "bibleget",
                    "bibleget");

            DatabaseMetaData dbMeta;
            dbMeta = conn.getMetaData();
            try (ResultSet rs1 = dbMeta.getTables(null, null, "OPTIONS", null)) {
                if(rs1.next())
                {
                    //System.out.println("Table "+rs1.getString("TABLE_NAME")+" already exists !!");
                    listColNamesTypes(dbMeta,rs1);
                }
                else
                {
                    //System.out.println("Table OPTIONS does not yet exist, now attempting to create...");
                    try ( Statement stmt = conn.createStatement()) {
                        
                        String defaultFont = "";
                        if(SystemUtils.IS_OS_WINDOWS){
                            defaultFont = "Times New Roman";
                        }
                        else if(SystemUtils.IS_OS_MAC_OSX){
                            defaultFont = "Helvetica";
                        }
                        else if(SystemUtils.IS_OS_LINUX){
                            defaultFont = "Arial";
                        }
                        
                        String tableCreate = "CREATE TABLE OPTIONS ("
                                + "PARAGRAPHALIGNMENT VARCHAR(15), "
                                + "PARAGRAPHLINESPACING INT, "
                                + "PARAGRAPHFONTFAMILY VARCHAR(50), "
                                + "PARAGRAPHLEFTINDENT INT, "
                                + "TEXTCOLORBOOKCHAPTER VARCHAR(15), "
                                + "BGCOLORBOOKCHAPTER VARCHAR(15), "
                                + "BOLDBOOKCHAPTER BOOLEAN, "
                                + "ITALICSBOOKCHAPTER BOOLEAN, "
                                + "UNDERSCOREBOOKCHAPTER BOOLEAN, "
                                + "FONTSIZEBOOKCHAPTER INT, "
                                + "VALIGNBOOKCHAPTER VARCHAR(15), "
                                + "TEXTCOLORVERSENUMBER VARCHAR(15), "
                                + "BGCOLORVERSENUMBER VARCHAR(15), "
                                + "BOLDVERSENUMBER BOOLEAN, "
                                + "ITALICSVERSENUMBER BOOLEAN, "
                                + "UNDERSCOREVERSENUMBER BOOLEAN, "
                                + "FONTSIZEVERSENUMBER INT, "
                                + "VALIGNVERSENUMBER VARCHAR(15), "
                                + "TEXTCOLORVERSETEXT VARCHAR(15), "
                                + "BGCOLORVERSETEXT VARCHAR(15), "
                                + "BOLDVERSETEXT BOOLEAN, "
                                + "ITALICSVERSETEXT BOOLEAN, "
                                + "UNDERSCOREVERSETEXT BOOLEAN, "
                                + "FONTSIZEVERSETEXT INT, "
                                + "VALIGNVERSETEXT VARCHAR(15), "
                                + "PREFERREDVERSIONS VARCHAR(50), "
                                + "NOVERSIONFORMATTING BOOLEAN"
                                + ")";
                        
                        
                        String tableInsert;
                        tableInsert = "INSERT INTO OPTIONS ("
                                + "PARAGRAPHALIGNMENT,"
                                + "PARAGRAPHLINESPACING,"
                                + "PARAGRAPHFONTFAMILY,"
                                + "PARAGRAPHLEFTINDENT,"
                                + "TEXTCOLORBOOKCHAPTER,"
                                + "BGCOLORBOOKCHAPTER,"
                                + "BOLDBOOKCHAPTER,"
                                + "ITALICSBOOKCHAPTER,"
                                + "UNDERSCOREBOOKCHAPTER,"
                                + "FONTSIZEBOOKCHAPTER,"
                                + "VALIGNBOOKCHAPTER,"
                                + "TEXTCOLORVERSENUMBER,"
                                + "BGCOLORVERSENUMBER,"
                                + "BOLDVERSENUMBER,"
                                + "ITALICSVERSENUMBER,"
                                + "UNDERSCOREVERSENUMBER,"
                                + "FONTSIZEVERSENUMBER,"
                                + "VALIGNVERSENUMBER,"
                                + "TEXTCOLORVERSETEXT,"
                                + "BGCOLORVERSETEXT,"
                                + "BOLDVERSETEXT,"
                                + "ITALICSVERSETEXT,"
                                + "UNDERSCOREVERSETEXT,"
                                + "FONTSIZEVERSETEXT,"
                                + "VALIGNVERSETEXT,"
                                + "PREFERREDVERSIONS, "
                                + "NOVERSIONFORMATTING"
                                + ") VALUES ("
                                + "'justify',100,'"+defaultFont+"',0,"
                                + "'#0000FF','#FFFFFF',true,false,false,14,'initial',"
                                + "'#AA0000','#FFFFFF',false,false,false,10,'super',"
                                + "'#696969','#FFFFFF',false,false,false,12,'initial',"
                                + "'NVBSE',"
                                + "false"
                                + ")";
                        boolean tableCreated = stmt.execute(tableCreate);
                        boolean rowsInserted;
                        int count;
                        if(tableCreated==false){
                            //is false when it's an update count!
                            count = stmt.getUpdateCount();
                            if(count==-1){
                                //System.out.println("The result is a ResultSet object or there are no more results.");
                            }
                            else{
                                //this is our expected behaviour: 0 rows affected
                                //System.out.println("The Table Creation statement produced results: "+count+" rows affected.");
                                try (Statement stmt2 = conn.createStatement()) {
                                    rowsInserted = stmt2.execute(tableInsert);
                                    if(rowsInserted==false){
                                        count = stmt2.getUpdateCount();
                                        if(count==-1){
                                            //System.out.println("The result is a ResultSet object or there are no more results.");
                                        }
                                        else{
                                            //this is our expected behaviour: n rows affected
                                            //System.out.println("The Row Insertion statement produced results: "+count+" rows affected.");
                                            dbMeta = conn.getMetaData();
                                            try (ResultSet rs2 = dbMeta.getTables(null, null, "OPTIONS", null)) {
                                                if(rs2.next())
                                                {
                                                    listColNamesTypes(dbMeta,rs2);
                                                }
                                                rs2.close();
                                            }
                                        }
                                    }
                                    else{
                                        //is true when it returns a resultset, which shouldn't be the case here
                                        try ( ResultSet rx = stmt2.getResultSet()) {
                                            while(rx.next()){
                                                //System.out.println("This isn't going to happen anyways, so...");
                                            }
                                            rx.close();
                                        }
                                    }
                                    stmt2.close();
                                }
                            }
                            
                        }
                        else{
                            //is true when it returns a resultset, which shouldn't be the case here
                            try (ResultSet rx = stmt.getResultSet()) {
                                while(rx.next()){
                                    //System.out.println("This isn't going to happen anyways, so...");
                                }
                                rx.close();
                            }
                        }
                        stmt.close();
                    }
                }
                rs1.close();
            }
            //System.out.println("Finished with first ResultSet resource, now going on to next...");
            try (ResultSet rs3 = dbMeta.getTables(null, null, "METADATA", null)) {
                if(rs3.next())
                {
                    //System.out.println("Table "+rs3.getString("TABLE_NAME")+" already exists !!");
                }
                else{
                    //System.out.println("Table METADATA does not exist, now attempting to create...");
                    try (Statement stmt = conn.createStatement()) {
                        String tableCreate = "CREATE TABLE METADATA (";
                        tableCreate += "ID INT, ";
                        for(int i=0;i<73;i++){
                            tableCreate += "BIBLEBOOKS"+Integer.toString(i)+" VARCHAR(2000), ";
                        }
                        tableCreate += "LANGUAGES VARCHAR(500), ";
                        tableCreate += "VERSIONS VARCHAR(2000)";
                        tableCreate += ")";
                        boolean tableCreated = stmt.execute(tableCreate);
                        boolean rowsInserted;
                        int count;
                        if(tableCreated==false){
                            //this is the expected result, is false when it's an update count!
                            count = stmt.getUpdateCount();
                            if(count==-1){
                                //System.out.println("The result is a ResultSet object or there are no more results.");
                            }
                            else{
                                //this is our expected behaviour: 0 rows affected
                                //System.out.println("The Table Creation statement produced results: "+count+" rows affected.");
                                //Insert a dummy row, because you cannot update what has not been inserted!                                
                                try ( Statement stmtX = conn.createStatement()) {
                                    stmtX.execute("INSERT INTO METADATA (ID) VALUES (0)");
                                    stmtX.close();
                                }
                                
                                HTTPCaller myHTTPCaller = new HTTPCaller();
                                String myResponse;
                                myResponse = myHTTPCaller.getMetaData("biblebooks");
                                if(myResponse != null){
                                    JsonReader jsonReader = Json.createReader(new StringReader(myResponse));
                                    JsonObject json = jsonReader.readObject();
                                    JsonArray arrayJson = json.getJsonArray("results");
                                    if(arrayJson != null){
                                        
                                        ListIterator pIterator = arrayJson.listIterator();
                                        while (pIterator.hasNext())
                                        {
                                            try(Statement stmt2 = conn.createStatement()) {
                                                int index = pIterator.nextIndex();
                                                JsonArray currentJson = (JsonArray) pIterator.next();
                                                String biblebooks_str = currentJson.toString(); //.replaceAll("\"", "\\\\\"");
                                                //System.out.println("BibleGetDB line 267: BIBLEBOOKS"+Integer.toString(index)+"='"+biblebooks_str+"'");
                                                String stmt_str = "UPDATE METADATA SET BIBLEBOOKS"+Integer.toString(index)+"='"+biblebooks_str+"' WHERE ID=0";
                                                try{
                                                    //System.out.println("executing update: "+stmt_str);
                                                    int update = stmt2.executeUpdate(stmt_str);
                                                    //System.out.println("executeUpdate resulted in: "+Integer.toString(update));
                                                } catch (SQLException ex){
                                                    Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                                stmt2.close();
                                            }
                                        }
                                    }
                                    
                                    arrayJson = json.getJsonArray("languages");
                                    if(arrayJson != null){
                                        try(Statement stmt2 = conn.createStatement()) {
                                            
                                            String languages_str = arrayJson.toString(); //.replaceAll("\"", "\\\\\"");
                                            String stmt_str = "UPDATE METADATA SET LANGUAGES='"+languages_str+"' WHERE ID=0";
                                            try{
                                                int update = stmt2.executeUpdate(stmt_str);
                                            } catch (SQLException ex){
                                                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                            stmt2.close();
                                        }                                    
                                    }
                                }
                                
                                myResponse = myHTTPCaller.getMetaData("bibleversions");
                                if(myResponse != null){
                                    JsonReader jsonReader = Json.createReader(new StringReader(myResponse));
                                    JsonObject json = jsonReader.readObject();
                                    JsonObject objJson = json.getJsonObject("validversions_fullname");
                                    if(objJson != null){
                                        String bibleversions_str = objJson.toString(); //.replaceAll("\"", "\\\\\"");
                                        try(Statement stmt2 = conn.createStatement()){
                                            String stmt_str = "UPDATE METADATA SET VERSIONS='"+bibleversions_str+"' WHERE ID=0";
                                            try{
                                                int update = stmt2.executeUpdate(stmt_str);
                                            } catch (SQLException ex){
                                                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                            stmt2.close();
                                        }
                                        
                                        Set<String> versionsabbrev = objJson.keySet();
                                        if(!versionsabbrev.isEmpty()){
                                            String versionsabbrev_str = "";
                                            for(String s:versionsabbrev) {
                                                versionsabbrev_str += ("".equals(versionsabbrev_str)?"":",")+s;
                                            }
                                            
                                            myResponse = myHTTPCaller.getMetaData("versionindex&versions="+versionsabbrev_str);
                                            if(myResponse != null){
                                                jsonReader = Json.createReader(new StringReader(myResponse));
                                                json = jsonReader.readObject();
                                                objJson = json.getJsonObject("indexes");
                                                if(objJson != null){
                                                    
                                                    for (String name : objJson.keySet()){
                                                        JsonObjectBuilder tempBld = Json.createObjectBuilder();
                                                        JsonObject book_num = objJson.getJsonObject(name);
                                                        tempBld.add("book_num", book_num.getJsonArray("book_num"));
                                                        tempBld.add("chapter_limit", book_num.getJsonArray("chapter_limit"));
                                                        tempBld.add("verse_limit", book_num.getJsonArray("verse_limit"));
                                                        JsonObject temp = tempBld.build();
                                                        String versionindex_str = temp.toString(); //.replaceAll("\"", "\\\\\"");
                                                        //add new column to METADATA table name+"IDX" VARCHAR(5000)
                                                        //update METADATA table SET name+"IDX" = versionindex_str
                                                        try(Statement stmt3 = conn.createStatement()){
                                                            String sql = "ALTER TABLE METADATA ADD COLUMN "+name+"IDX VARCHAR(5000)";
                                                            boolean colAdded = stmt3.execute(sql);
                                                            if(colAdded==false) {
                                                                count = stmt3.getUpdateCount();
                                                                if(count==-1){
                                                                    //System.out.println("The result is a ResultSet object or there are no more results.");
                                                                }
                                                                else if(count==0){
                                                                    //0 rows affected
                                                                    stmt3.close();
                                                                    
                                                                    try(Statement stmt4 = conn.createStatement()){
                                                                        String sql1 = "UPDATE METADATA SET "+name+"IDX='"+versionindex_str+"' WHERE ID=0";
                                                                        boolean rowsUpdated = stmt4.execute(sql1);
                                                                        if(rowsUpdated==false) {
                                                                            count = stmt4.getUpdateCount();
                                                                            if(count==-1){
                                                                                //System.out.println("The result is a ResultSet object or there are no more results.");
                                                                            }
                                                                            else{
                                                                                //should have affected only one row
                                                                                if(count==1){
                                                                                    //System.out.println(sql1+" seems to have returned true");
                                                                                    stmt4.close();
                                                                                }
                                                                            }
                                                                        }
                                                                        else{
                                                                            //returns true only when returning a resultset; should not be the case here
                                                                        }
                                                                        
                                                                    }
                                                                    
                                                                }
                                                            }
                                                            else{
                                                                //returns true only when returning a resultset; should not be the case here
                                                            }
                                                            
                                                            stmt3.close();
                                                        }
                                                    }
                                                    
                                                }
                                            }
                                            
                                        }
                                        
                                        
                                        
                                    }
                                }
                                
                            }
                        }
                        else{
                            //is true when it returns a resultset, which shouldn't be the case here
                            ResultSet rx = stmt.getResultSet();
                            while(rx.next()){
                                //System.out.println("This isn't going to happen anyways, so...");
                            }
                        }
                        stmt.close();
                    }
                }
                rs3.close();
            }
            conn.close();
            return true;
        } catch (SQLException ex) {
            if( ex.getSQLState().equals("X0Y32") ) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.INFO, null, "Table OPTIONS or Table METADATA already exists.  No need to recreate");
                return true;
            } else if (ex.getNextException().getErrorCode() ==  45000) {
                //this means we already have a connection, so this is good too
                return true;
            } else {
                //Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex.getMessage() + " : " + Arrays.toString(ex.getStackTrace()));
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }                
    }
    
    public boolean connect() {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:derby:BIBLEGET",
                    "bibleget",
                    "bibleget");
        } catch (SQLException ex) {
            if( ex.getSQLState().equals("X0Y32") ) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.INFO, null, "Table OPTIONS or Table METADATA already exists.  No need to recreate");
                return true;
            } else if (ex.getNextException().getErrorCode() ==  45000) {
                //this means we already have a connection, so this is good too
                return true;
            } else {
                //Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex.getMessage() + " : " + Arrays.toString(ex.getStackTrace()));
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    public void disconnect() {
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static void setDBSystemDir() {
        String derbyhome = "";
        if(SystemUtils.IS_OS_WINDOWS){
            derbyhome = "/AppData/Roaming/BibleGetOpenOfficePlugin";
        }
        else if(SystemUtils.IS_OS_MAC_OSX){
            derbyhome = "/Library/Application Support/BibleGetOpenOfficePlugin";
        }
        else if(SystemUtils.IS_OS_LINUX){
            derbyhome = "/.BibleGetOpenOfficePlugin";
        }
        System.setProperty("derby.system.home", System.getProperty("user.home") + derbyhome);
    }
 
        
    private void listColNamesTypes(DatabaseMetaData dbMeta, ResultSet rs) {
        //System.out.println("After Table Creation: Table "+rs.getString("TABLE_NAME")+" exists !!");
        ResultSet cols;
        try {
            cols = dbMeta.getColumns(null, null, rs.getString("TABLE_NAME"), null);
            while(cols.next()){
                //System.out.println(cols.getString("COLUMN_NAME"));
                colNames.add(cols.getString("COLUMN_NAME"));
                int dType = cols.getInt("DATA_TYPE");
                switch(dType){
                    case Types.VARCHAR:
                        colDataTypes.add(String.class); break;
                    case Types.INTEGER:
                        colDataTypes.add(Integer.class); break;
                    case Types.FLOAT:
                        colDataTypes.add(Float.class); break;
                    case Types.DOUBLE:
                    case Types.REAL:
                        colDataTypes.add(Double.class); break;
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        colDataTypes.add(java.sql.Date.class); break;
                    case Types.BOOLEAN:
                        colDataTypes.add(Boolean.class); break;
                    default:
                        colDataTypes.add(String.class); break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    
    public JsonObject getOptions() {
        if(instance.connect()){
            try {
                JsonObjectBuilder myOptionsTable;
                JsonArrayBuilder myRows;
                try (Statement stmt = conn.createStatement(); ResultSet rsOps = stmt.executeQuery("SELECT * FROM OPTIONS")) {
                    Iterator itColNames = colNames.iterator();
                    Iterator itDataTypes = colDataTypes.iterator();
                    myOptionsTable = Json.createObjectBuilder();
                    myRows = Json.createArrayBuilder();
                    while (rsOps.next()) {
                        //System.out.println("Getting a row from the table.");
                        JsonObjectBuilder thisRow = Json.createObjectBuilder();
                        while(itColNames.hasNext() && itDataTypes.hasNext()){
                            String colName = (String) itColNames.next();
                            Class dataType = (Class) itDataTypes.next();
                            if(dataType==String.class){ thisRow.add(colName, rsOps.getString(colName)); }
                            if(dataType==Integer.class){ thisRow.add(colName, rsOps.getInt(colName)); }
                            if(dataType==Boolean.class){ thisRow.add(colName, rsOps.getBoolean(colName)); }
                            //System.out.println(colName + " <" + dataType + ">");
                        }
                        thisRow.build();
                        myRows.add(thisRow);
                    }   
                    rsOps.close();
                    stmt.close();
                    instance.disconnect();
                    myRows.build();   
                    return myOptionsTable.add("rows", myRows).build();
                }
            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return null;
    }
    
    public Object getOption(String option){
        option = option.toUpperCase();
        Object returnObj = null;
        if(instance.connect()){
            try{
                if(colNames.contains(option)){
                    int idx = colNames.indexOf(option);
                    Class dataType = colDataTypes.get(idx);
                    //System.out.println(BibleGetDB.class.getName()+" [299]: dataType="+dataType.getName() );
                    try (Statement stmt = conn.createStatement()) {
                        String sqlexec = "SELECT "+option+" FROM OPTIONS";
                        try (ResultSet rsOps = stmt.executeQuery(sqlexec)) {
                            while (rsOps.next()) {
                                //System.out.println(BibleGetDB.class.getName()+" [304]: retrieved a value from DB, about to return it to calling function" );
                                if(dataType==String.class){ returnObj = rsOps.getString(option); }
                                if(dataType==Integer.class){ returnObj = rsOps.getInt(option); }
                                if(dataType==Boolean.class){ returnObj = rsOps.getBoolean(option); }
                            }
                            rsOps.close();
                        }
                        stmt.close();
                    }
                    instance.disconnect();
                    return returnObj;
                }

            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return null;
    }
    
    public String getMetaData(String dataOption){
        dataOption = StringUtils.upperCase(dataOption);
        String metaDataStr = "";
        if(dataOption.startsWith("BIBLEBOOKS") || dataOption.equals("LANGUAGES") || dataOption.equals("VERSIONS") || dataOption.endsWith("IDX")){
            //System.out.println("getMetaData received a valid request for "+dataOption);
            if(instance.connect()){
                //System.out.println("getMetaData has connected to the database...");
                String sqlexec = "SELECT "+dataOption+" FROM METADATA WHERE ID=0";
                try(Statement stmt = conn.createStatement()){
                    try (ResultSet rsOps = stmt.executeQuery(sqlexec)) {
                        //System.out.println("query seems to have been successful...");
                        //ResultSetMetaData rsMD = rsOps.getMetaData();
                        //int cols = rsMD.getColumnCount();
                        //String colnm = rsMD.getColumnName(cols);
                        //System.out.println("there are "+Integer.toString(cols)+" columns in this resultset and name is: "+colnm+"(requested "+dataOption+")");
                        while(rsOps.next()){
                            metaDataStr = rsOps.getString(dataOption);
                        }
                        rsOps.close();
                    }
                    stmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                }
                instance.disconnect();
            }
        }        
        return metaDataStr;
    }
    
    public boolean setIntOption(String colname, int value) {
        int count;
        colname = colname.toUpperCase();
        if(instance.connect()){
            if(!colNames.contains(colname)){
                boolean result = addColumn(colname,"INT");
                if(result==false){ return false; }
                //System.out.println("Added "+colname+" column of type INT to OPTIONS table");
                colNames.add(colname);
                colDataTypes.add(Integer.class);
            }
            try {
                Statement stmt = conn.createStatement();
                String sqlexec = "UPDATE OPTIONS SET "+colname+" = "+value+"";
                boolean rowsUpdated = stmt.execute(sqlexec);
                if(rowsUpdated==false) {
                    count = stmt.getUpdateCount();
                        if(count==-1){
                            //System.out.println("The result is a ResultSet object or there are no more results."); 
                        }
                        else{
                            //should have affected only one row
                            if(count==1){ 
                                stmt.close();
                                instance.disconnect();
                                return true; 
                            }
                        }
                }
                else{
                    //returns true only when returning a resultset; should not be the case here
                }

            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    public boolean setStringOption(String colname, String value) {
        int count;
        colname = colname.toUpperCase();
        if(instance.connect()){
            if(!colNames.contains(colname)){
                boolean result = addColumn(colname,"VARCHAR(50)");
                if(result==false){ return false; }
                //System.out.println("Added "+colname+" column of type VARCHAR(50) to OPTIONS table");
                colNames.add(colname);
                colDataTypes.add(String.class);
            }
            try {
                Statement stmt = conn.createStatement();
                String sqlexec = "UPDATE OPTIONS SET "+colname+" = '"+value+"'";
                boolean rowsUpdated = stmt.execute(sqlexec);
                if(rowsUpdated==false) {
                    count = stmt.getUpdateCount();
                        if(count==-1){
                            //System.out.println("The result is a ResultSet object or there are no more results."); 
                        }
                        else{
                            //should have affected only one row
                            if(count==1){ 
                                //System.out.println(sqlexec+" seems to have returned true");
                                stmt.close();
                                instance.disconnect();
                                return true; 
                            }
                        }
                }
                else{
                    //returns true only when returning a resultset; should not be the case here
                }

            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    public boolean setBooleanOption(String colname, boolean value) {
        int count;
        colname = colname.toUpperCase();
        if(instance.connect()){
            if(!colNames.contains(colname)){
                boolean result = addColumn(colname,"BOOLEAN");
                if(result==false){ return false; }
                //System.out.println("Added "+colname+" column of type BOOLEAN to OPTIONS table");
                colNames.add(colname);
                colDataTypes.add(Boolean.class);
            }
            try {
                Statement stmt = conn.createStatement();
                String sqlexec = "UPDATE OPTIONS SET "+colname+" = "+value+"";
                boolean rowsUpdated = stmt.execute(sqlexec);
                if(rowsUpdated==false) {
                    count = stmt.getUpdateCount();
                        if(count==-1){
                            //System.out.println("The result is a ResultSet object or there are no more results."); 
                        }
                        else{
                            //should have affected only one row
                            if(count==1){ 
                                stmt.close();
                                instance.disconnect();
                                return true; 
                            }
                        }
                }
                else{
                    //returns true only when returning a resultset; should not be the case here
                }

            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    public boolean addColumn(String colName,String type){
        int count;
        try {
            colName = colName.toUpperCase();
            Statement stmt = conn.createStatement();
            String sqlexec = "ALTER TABLE OPTIONS ADD COLUMN "+colName+" "+type;
            boolean colAdded = stmt.execute(sqlexec);
            
            if(colAdded==false) {
                count = stmt.getUpdateCount();
                    if(count==-1){
                        //System.out.println("The result is a ResultSet object or there are no more results."); 
                    }
                    else if(count==0){
                        //0 rows affected
                        stmt.close();
                        return true;
                    }
            }
            else{
                //returns true only when returning a resultset; should not be the case here
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean renewMetaData(){
        if(instance.connect()){
            try {
                DatabaseMetaData dbMeta;
                dbMeta = conn.getMetaData();
                try (ResultSet rs3 = dbMeta.getTables(null, null, "METADATA", null)) {
                    if(rs3.next())
                    {
                        //System.out.println("Table METADATA exists...");
                        try (Statement stmt = conn.createStatement()) {
                            HTTPCaller myHTTPCaller = new HTTPCaller();
                            String myResponse;
                            myResponse = myHTTPCaller.getMetaData("biblebooks");
                            if(myResponse != null){
                                JsonReader jsonReader = Json.createReader(new StringReader(myResponse));
                                JsonObject json = jsonReader.readObject();
                                JsonArray arrayJson = json.getJsonArray("results");
                                if(arrayJson != null){                                    
                                    ListIterator pIterator = arrayJson.listIterator();
                                    while (pIterator.hasNext())
                                    {
                                        try(Statement stmt1 = conn.createStatement()) {
                                            int index = pIterator.nextIndex();
                                            JsonArray currentJson = (JsonArray) pIterator.next();
                                            String biblebooks_str = currentJson.toString(); //.replaceAll("\"", "\\\\\"");
                                            //System.out.println("BibleGetDB line 267: BIBLEBOOKS"+Integer.toString(index)+"='"+biblebooks_str+"'");
                                            String stmt_str = "UPDATE METADATA SET BIBLEBOOKS"+Integer.toString(index)+"='"+biblebooks_str+"' WHERE ID=0";
                                            //System.out.println("executing update: "+stmt_str);
                                            int update = stmt1.executeUpdate(stmt_str);
                                            //System.out.println("executeUpdate resulted in: "+Integer.toString(update));
                                            stmt1.close();
                                        }
                                    }
                                }
                                
                                arrayJson = json.getJsonArray("languages");
                                if(arrayJson != null){
                                    try(Statement stmt2 = conn.createStatement()) {                                        
                                        String languages_str = arrayJson.toString(); //.replaceAll("\"", "\\\\\"");
                                        String stmt_str = "UPDATE METADATA SET LANGUAGES='"+languages_str+"' WHERE ID=0";
                                        int update = stmt2.executeUpdate(stmt_str);
                                        stmt2.close();
                                    }
                                }
                            }
                            
                            myResponse = myHTTPCaller.getMetaData("bibleversions");
                            if(myResponse != null){
                                JsonReader jsonReader = Json.createReader(new StringReader(myResponse));
                                JsonObject json = jsonReader.readObject();
                                JsonObject objJson = json.getJsonObject("validversions_fullname");
                                if(objJson != null){
                                    String bibleversions_str = objJson.toString(); //.replaceAll("\"", "\\\\\"");
                                    try(Statement stmt3 = conn.createStatement()){
                                        String stmt_str = "UPDATE METADATA SET VERSIONS='"+bibleversions_str+"' WHERE ID=0";
                                        int update = stmt3.executeUpdate(stmt_str);
                                        stmt3.close();
                                    }
                                    
                                    Set<String> versionsabbrev = objJson.keySet();
                                    if(!versionsabbrev.isEmpty()){
                                        String versionsabbrev_str = "";
                                        for(String s:versionsabbrev) {
                                            versionsabbrev_str += ("".equals(versionsabbrev_str)?"":",")+s;
                                        }
                                        
                                        myResponse = myHTTPCaller.getMetaData("versionindex&versions="+versionsabbrev_str);
                                        if(myResponse != null){
                                            jsonReader = Json.createReader(new StringReader(myResponse));
                                            json = jsonReader.readObject();
                                            objJson = json.getJsonObject("indexes");
                                            if(objJson != null){
                                                for (String name : objJson.keySet()){
                                                    JsonObjectBuilder tempBld = Json.createObjectBuilder();
                                                    JsonObject book_num = objJson.getJsonObject(name);
                                                    tempBld.add("book_num", book_num.getJsonArray("book_num"));
                                                    tempBld.add("chapter_limit", book_num.getJsonArray("chapter_limit"));
                                                    tempBld.add("verse_limit", book_num.getJsonArray("verse_limit"));
                                                    JsonObject temp = tempBld.build();
                                                    String versionindex_str = temp.toString(); //.replaceAll("\"", "\\\\\"");
                                                    //add new column to METADATA table name+"IDX" VARCHAR(5000)
                                                    //update METADATA table SET name+"IDX" = versionindex_str
                                                    try(ResultSet rs1 = dbMeta.getColumns(null, null, "METADATA", name+"IDX")){
                                                        boolean updateFlag = false;
                                                        if(rs1.next()){
                                                            //column already exists
                                                            updateFlag=true;
                                                        }
                                                        else{
                                                            try(Statement stmt4 = conn.createStatement()){
                                                                String sql = "ALTER TABLE METADATA ADD COLUMN "+name+"IDX VARCHAR(5000)";
                                                                boolean colAdded = stmt4.execute(sql);
                                                                if(colAdded==false) {
                                                                    int count = stmt4.getUpdateCount();
                                                                    if(count==-1){
                                                                        //System.out.println("The result is a ResultSet object or there are no more results.");
                                                                    }
                                                                    else if(count==0){
                                                                        //0 rows affected
                                                                        updateFlag=true;
                                                                    }
                                                                }
                                                                stmt4.close();
                                                            }
                                                        }
                                                        if(updateFlag){
                                                            try(Statement stmt5 = conn.createStatement()){
                                                                String sql1 = "UPDATE METADATA SET "+name+"IDX='"+versionindex_str+"' WHERE ID=0";
                                                                boolean rowsUpdated = stmt5.execute(sql1);
                                                                stmt5.close();                                                                    
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                            }
                                        }
                                        
                                    }

                                    
                                    
                                }
                            }
                            
                            stmt.close();
                        }
                    }
                    rs3.close();
                }
                instance.disconnect();
            }   catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return true;
        }
        return false;
    }
    
}
