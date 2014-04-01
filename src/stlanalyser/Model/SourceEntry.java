package stlanalyser.Model;

import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Model representing the Complete segment of STL source being analysed
 * @author Alan Curley
 */
public class SourceEntry {
    // Global Variables 
    private ArrayList<String> sourceLines;
    private ArrayList<lineEntry> sourceLineEntries;
    private ArrayList<blockClass> blockList;
    private Map<String, Integer> m;
    private Map<String, String> t;
    
    //http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
    
    
    /**
     * Default Constructor. 
     */
    public SourceEntry(){
        sourceLines = new ArrayList<>();        
        sourceLineEntries = new ArrayList<>();
        //loadHashMap(); // populate the hashmap with instructions and times.
        getDataBaseInfo();
    }
    
    /**
     * Secondary constructor where the Block string representing the STL source code
     * is passed when creating the object 
     * @param SourceCode 
     */
    public SourceEntry(String SourceCode){
        sourceLines = new ArrayList<>();        
        sourceLineEntries = new ArrayList<>();
        //loadHashMap(); // populate the hashmap with instructions and times.
        getDataBaseInfo();

        //1 - Break the Source code Entry into individual lines.
        //String sourceCodeArray[] = SourceCode.split("(\\r\\n)");
        String sourceCodeArray[] = SourceCode.split("\n");
        sourceLines.addAll(Arrays.asList(sourceCodeArray)); //(Thank you Netbeans Hints)
    }

    /**
     * Add a Line to the source code list at any time.
     * @param lineToAdd 
     */
    public void addLine(String lineToAdd){ sourceLines.add(lineToAdd); }
    
    /**
     * This is the method that performs the process of analysing the source code.
     */
    public void processSourceCode(String[] args){
        Integer StateMachine;// = new Integer(0);
        String blockName = ""; // String to remember the name of the parent block being processed.
        Map<String,String> VAR = new HashMap<>();
        final Integer UNKNOWN           = 0;
        final Integer BLOCKHEADER       = 1;             
        final Integer VARDECLARE        = 2;
        final Integer NETWORKANALYSIS   = 3;
        final Integer DATABLOCK         = 4; // Data Blocks are Not runtime executable - Data only.
        final Integer BLOCKCALL         = 5; // Block calls - Any parameter transfers do not incurr
        ArrayList<String> memData = new ArrayList<>();  // Create a new Arraylist to hold any data we're supposed to remember
                                                        // from any previousl lines (E.G - Arrays)
                
        //1 - Empty the arraylist to start with.
        sourceLineEntries.clear();
        StateMachine = UNKNOWN;        
        /**
         * This loop is where we go through each Line, one entry at a time, check what it is and then 
         * create a lineEntry object type to represent that time.
         * it is during this generation we determine the time each instruction is taking up.
         */
        for(int i=0; i<sourceLines.size(); i++){                        
            String rawStringLine = sourceLines.get(i); // get rid of any Labels
            String stringLine = rawStringLine.replaceAll(regExes.LABELID, "$2").trim();
            stringLine = stringLine.replaceAll("(.+)//.*", "$1");
            if(i>0) 
                sourceLineEntries.get(i-1).setParentBlock(blockName);

            /*
             * Purely for Debug purposes only.
             * Allows me to Break the processing at any line I want to.
             */
            if(Integer.parseInt(args[0])>0)
                System.out.println(sourceLines.get(i));
                
            for(int d=0; d<args.length; d++){
                if(i==Integer.parseInt(args[d])-1)
                    System.out.println("<<<<Debug: "+ stringLine +">>>>");
            }
            switch(StateMachine){                
/*----------*/  case 0 : //Unknown.
                    if(stringLine.matches(regExes.FUNCTIONHEADER)) {
                        blockName = stringLine.replaceAll("(FUNCTION|ORGANIZATION)(_BLOCK)?.*((FC|FB|OB|DB)\\s+[0-9]+).*","$3").replaceAll(" ", "");
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_DECLARATION));
                        StateMachine = BLOCKHEADER;                        
                        break;
                    }
                    if(stringLine.matches(regExes.DBHEADER)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_DECLARATION));
                        StateMachine = DATABLOCK;
                        break;
                    }
                    sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.EMPTY_LINE));
                    blockName = "";
                    break;
/*----------*/  case 1 : //BlockHeader
                    // 1st Line - This is Definitley a Block Declaration.
                    if(stringLine.replaceAll("(.*)\\{.*\\}(.*)", "$1$2").length()<1){ // Attributes may be associated with the Block, that once removed will throw AIOOBE.
                        sourceLineEntries.add(new lineEntry(" ",i,0,lineEntry.EMPTY_LINE));
                        break;                        
                    }
                    if(stringLine.matches(regExes.TITLELINE)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_TITLE));
                        break;
                    }
                    if(stringLine.matches(regExes.VERSIONLINE)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_VERSION));
                        break;                        
                    }
                    if(stringLine.matches(regExes.COMMENT)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_COMMENT));
                        break;                        
                    }
                    if(stringLine.matches(regExes.BLOCKDETAILS)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_COMMENT));
                        break;
                    }
                    if(stringLine.matches(regExes.BEGIN)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BEGIN));
                        StateMachine = NETWORKANALYSIS;
                        break;
                    }
                    if(stringLine.matches(regExes.VARBEGIN)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.VAR_HEADER));
                        StateMachine = VARDECLARE;
                        break;
                    }                    
/*----------*/  case 2 : //VARDECLARE   
                    if(stringLine.matches(regExes.VAREND)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.VAR_FOOTER));
                        break;
                    }
                    else if(stringLine.matches(regExes.VARBEGIN)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.VAR_HEADER));
                        break;
                    }
                    else if(stringLine.matches(regExes.BEGIN)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BEGIN));
                        StateMachine = NETWORKANALYSIS;
                        break;
                    }
                    else if(stringLine.matches(regExes.ARRAYSTATEMENT)){
                        memData.add(stringLine);
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.VAR_DECLARE));
                        break;
                    }
                    else{
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.VAR_DECLARE));                        
                        // Remove any attributes from the entries until we know what is happening?
                        stringLine = stringLine.replaceAll("(.*)\\{.*\\}(.*)", "$1$2"); // Turn something like   "WORD_1_REPLY { S7_dynamic := 'true' }: WORD ;" into "WORD_1_REPLY : WORD ;"
                        String[] placeHolder =  stringLine.split(":");                        
                        if(placeHolder.length<2){ // if there is no Colon in the entry... why?
                            try{    
                                if(memData.get(memData.size()-1).matches(regExes.ARRAYSTATEMENT)){ // if the last entry was an Array Declare                                
                                    String[] datPlaceHolder = new String[]{"",""};                                        
                                    datPlaceHolder[1] = placeHolder[0].replace(";", "").trim(); // build a new "PlaceHolder"
                                    datPlaceHolder[0] = memData.get(memData.size()-1);
                                    datPlaceHolder[0] = datPlaceHolder[0].substring(0, datPlaceHolder[0].indexOf(":"));
                                    //datPlaceHolder[0] = datPlaceHolder[0].replace("(.*):(.*)", "$1:");
                                    placeHolder = datPlaceHolder;
                                    memData.remove(memData.size()-1); //When finished - Remove the last string item.
                                }
                            }
                            catch(ArrayIndexOutOfBoundsException AIOOBE){
                                System.err.println(AIOOBE);
                                System.err.println("Vardeclare error: " + stringLine);
                            }
                        }                                                            
                        try{
                            String put = VAR.put(placeHolder[0].trim(), resolveMemory(placeHolder[1].replaceAll("(\\[[ \\t0-9]+\\]\\s+)?;", "").trim())); // Clear out any trailing colons.                                           
                        }
                        catch(ArrayIndexOutOfBoundsException AIOOBE){
                            System.err.println(AIOOBE.getMessage());
                            System.err.println("<<<< procHeader error@"+"I-"+ String.valueOf(i)+":" + String.valueOf(placeHolder.length)+";"+stringLine + ".");
                        }
                    }
                    break;
/*----------*/  case 3 : // NETWORKANALYSIS

                    /*<<<< Decision Engine >>>>*/
                    String[] placeHolder;
                    String dataPlaceHolder;

                    // Comments (Just like this one)
                    if(stringLine.matches(regExes.COMMENT)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.CODE_COMMENT));
                        break;
                    }
                    // NETWORK identifer signifying the start of a network
                    if(stringLine.matches(regExes.NETWORK)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.NETWORK_DECLARE));
                        break;
                    }
                    // TITLE = defining the title of the network - always here though sometimes blank
                    if(stringLine.matches(regExes.TITLELINE)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.NETWORK_TITLE));
                        break;                        
                    }
                    // Anything less than 1 character long is an empty line.
                    if(stringLine.length()<1){
                        sourceLineEntries.add(new lineEntry(" ",i,0,lineEntry.EMPTY_LINE));
                        break;                        
                    }
                    // END_FUNCTION Declaration indicating the end if a function block or function call.
                    if(stringLine.matches(regExes.ENDFUNCTION)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_END));
                        StateMachine = UNKNOWN;
                        break;
                    }                    
                    
                    // NOP Function - No Operation, Used for Constructing Calls etc.
                    if((stringLine.matches(regExes.NOPSTATEMENT))|(stringLine.matches(regExes.BLDSTATEMENT))){
                    //if(stringLine.matches(reNOPSTATEMENT)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.CODE_SOURCE));
                        break;
                    }
                    
                    // If this is a Footer - Push back to "unknown" to search for another title.
                    if(stringLine.matches(regExes.BLOCKFOOTER)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_END));
                        StateMachine = UNKNOWN;
                        break;
                    }
                    
                                        
                    // If we're here - This is a Valid Line to process.
                    placeHolder = stringLine.trim().replaceAll(regExes.CLEANLINE, "$1").split("\\s+"); // split via any whitespace characters and remove any trailing comments.
                    
                    S7Statement statment = new S7Statement();
                    // ---- The Entry has now been divided into it's constituent parts.
                    // This may be 1, 2, 3 or more parts.
                    // Typically, this is limited to 3, a Statement, a memory type and an address.
                    // AN M  0.0 which is broken down into [0]AN [1]M  [2]0.0
                    // This can also be a single command such as an "AW" command
                    // AW which is broken into [0]AW
                    // !!!Labels have been removed from stringLine which is used to generate placeHolder!!!
                    // This Block will build each statement from the data required and then use this
                    // statement to build the lineEntry.
                   
                    //Start by checking the first item.... this should be a command variable.                    
                    try{
                        if(placeHolder[0].toUpperCase().matches(regExes.JUMPSTATEMENT)){ // This is a Jump Statement.
                            statment.command= placeHolder[0];   
                            statment.arg1 = "b"; // for all jumps, assume a boolean.
                        }
                        else if(placeHolder[0].toUpperCase().matches(regExes.CALLSTATEMENT)){
                            statment.command = placeHolder[0];
                            statment.arg1 = placeHolder[1];
                            if(!(statment.arg1.toUpperCase().matches(regExes.LOCALVARIABLE))){                                
                                statment.arg2 = placeHolder[2];                     // Keep in memory what SFC/SFB this is, we can address this later if we need.                             
                            }
                            else{
                                
                                statment.arg1 += ";_pa";                            // Tag to accomodate the additional delay for parameter access.                                
                            }                                
                            statment.blockType = lineEntry.CALLFUNCTION;                            
                            if(stringLine.matches(".*\\(.*")) // If the "CALL" function does not have an open bracket, then then there won't be a close bracket.
                                StateMachine = BLOCKCALL; // S7300_instruction_list.PDF P59                                                       // AWL_e.PDF P265
                        }
                        // for everything else the same format applies...
                        else{                             // Default command.
                            statment.command = placeHolder[0];
                            if(placeHolder.length>1)        // If this only contains a single command - Declare operation on a "b"oolean
                                statment.arg1 = IDMemoryType(placeHolder,statment.arg1,VAR);
                            else
                                statment.arg1 = "b";
                        }
                        if(statment.blockType<0)
                            statment.blockType = lineEntry.CODE_SOURCE;
                        
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,mGet(statment.command+","+statment.arg1),statment.blockType));
                                                
                            
                    } catch (ArrayIndexOutOfBoundsException AIOOBE){
                        System.err.println("<<<< Decision Engine: Possible misconstructed statement.");
                        System.err.println("\t"+ stringLine);
                        System.err.println(AIOOBE.getMessage());
                    }                                            

                    break;
/*----------*/  case 4 : //DATA BLOCK
                    if(stringLine.matches(regExes.BLOCKFOOTER)){ // All Entries are Data points only until the end of the Block.
                        StateMachine = UNKNOWN;
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_END));
                        break;
                    }
                    sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.DB_ENTRY));
                    break;    
                case 5: // BLOCK CALL
                    // ignore all the entries while in block call until we see a ")"                    
                    sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.CODE_COMMENT));
                    //If this is a closing bracket - go back to calcuating execution times.
                    if(stringLine.matches(".*\\);?.*"))
                        StateMachine = NETWORKANALYSIS;
                    break;
                default :
                    System.out.println("<<<< How the Hell did I get here? Line" + String.valueOf(i) + ": " + stringLine);
                    break;
            }
        }
        sourceLineEntries.get(sourceLineEntries.size()-1).setParentBlock(blockName); // Update the Block name of the last entry
        
    }
    
     private String resolveMemory(String memory){
        String placeHolder;
        /*
         * E.G
         * B = Byte = 8 bits, any data type that takes up 8 bits is accessed as a Byte.
         * S5Time takes up 16 Bits thus is encoded as a Word.
         */        
        if(memory.matches(regExes.FBIdBlock))
            placeHolder = memory.replaceAll(regExes.FBIdBlock, "$1");
        else
            placeHolder = t.get(memory);     // T populated once in loadHashMap
        
        if(placeHolder == null) {
            //JOptionPane.showMessageDialog(null, "Memory Type " + memory + " Not found --> Please adddess.");
            System.out.println("<<<< resolveMemory: Memory Type " + memory + " Not found --> Please adddess.");
            placeHolder = "b";
        }        
        return placeHolder;
    }

    /**
     * A method that when called will print out all the details of each object in sourceLineEntries
     * 
     * @param s the @PrintStream to be used for writing
     */
    public void printDetails(PrintStream s){ //<<<< Updated by Java tips
        for(lineEntry a:sourceLineEntries){
            s.println(a.getStringDetails());
        }
    } //>>>>
    
    public void printBlockDetails(PrintStream s){
        try{
            for(blockClass a:blockList){
                a.printBlockDetails(s, 0);
            }
        }
        catch(NullPointerException e){
            s.println("ERR: Printing BlockList - Complete?");
        }
                

    }
    
    /**
     * This function takes in a Variable and returns the memory type identifier
     * for the variable.
     * E.G
     * If W#16#0FFF is passed, then the System will return a K
     * If IW120 is passed, the System will return a W
     * if S5T10S is passed, the System will return a W.
     * @param Var The Variable passed for evaluation
     * @return The Memory Identifier.
     */
    private String IDmemoryType(String Var){
        String retVal = "";
        String[] var = Var.split(";");
        // Status word operation
        if(var[0].toUpperCase().matches("(OV|RLO|BR|OS|CC)"))
            retVal = "b";
        
        // Boolean Local Variable or Memory Flag
        if(var[0].toUpperCase().matches("[LMXIQE]"))
            retVal = "b";
        // Constant Timer Variable
        if(var[0].toUpperCase().matches("(S5)?T#.*"))
            retVal = "K"; // Stored as a Word ???
        // If there is a ";", this is a single command and should be treated as a "b"
        if(var[0].toUpperCase().matches("(;|JNB|JU|JC).*"))
            retVal = "b";
        // Timers - A Quick and easy (and somewhat Lazy) solution.
        if(var[0].toUpperCase().matches("T"))
            retVal = "T";
        if(var[0].toUpperCase().matches("a[ci](ri)?"))
            retVal = var[0];
        //Read is this is a constant value being passed.
        if(var[0].toUpperCase().matches("-?[0-9\\.]+\\s*;?"))
            retVal = "K";
        if(var[0].toUpperCase().matches("(B|W|2)#[0-9]+#?[A-Fa-f0-9]+\\s*;?"))
            retVal = "K";        
        if(var[0].toUpperCase().matches("'.+'"))
            retVal = "K";        
         // instance or not Data block access - additional delay
        if(var[0].toUpperCase().matches("D[IB]\\w")){
            retVal = var[0].substring(var[0].length()-1, var[0].length());
            if(retVal.toUpperCase().matches("[LMXIQE]"))
                retVal = "b";                    
            retVal = retVal + ";_pqdb"; // Partially qualified DB(I) access
        }
        if(var[0].toUpperCase().matches("D[IB][0-9]+\\.DB\\w")){
            retVal = var[0].substring(var[0].length()-1);
            if(retVal.matches("[LMXIQE]"))
                retVal = "b";
        }
        // To be enabled afterwards - I/O requires a different delay set.
        //if(var[0].toUpperCase().matches("P?(IQ)\\w?"))
        //    retVal = var[0].substring(var[0].length()-1, var[0].length()) + ";ioacc"; // Partially qualified DB(I) access        
        
        //if(var.length>1) // Leave this alone for now - it seems to work
        //    retVal = var[0].substring(var[0].length()-1, var[0].length());
        
        if(retVal.length()==0){//If we've got this far, then this is an unknown address.  Treat it as such.
            //System.out.println("<<<< IDMemoryType:" +Var+"> Unknown Var - Please Address");
            retVal = var[0].substring(var[0].length()-1, var[0].length());
        }
        if(var.length>1){            
            for(int i=1;i<var.length; i++){ // There may be multiple appendages - append each to the end of the identifier.
                retVal += ";" + var[i];
            }
            return retVal;
        }
        else
            return retVal;
    }
    
    /**
     *  This is another version of IDMemoryType that takes in the entire 
     * "PlaceHolder" array and the current string for the statement.
     * @param placeHolder The Array of statements from the Sourceline
     * @param Statement The Statement that contains already defined variables
     * @param VAR A Map that contains the List of Local Variables
     * @return The MemoryType and additional Appendages.
     */
    private String IDMemoryType(String[] placeHolder, String Statement,Map VAR){
        //Go through each item in placeHolder.
        for(int i=1;i<placeHolder.length;i++){            
            if(placeHolder[i].matches(regExes.DIRECTADDRESSING)){ // E.G "L MW 5.0"
                Statement = placeHolder[i].replaceAll(regExes.DIRECTADDRESSING, "$1");
                if(Statement.equalsIgnoreCase(""))// If there is no preceding item - E.G M 5.1, this is Boolean.
                    Statement = "b";
            }
            else if(placeHolder[i].matches(regExes.TIMERSANDCOUNTERS)) // If this is a timer or a counter.
                Statement = placeHolder[i];
            
            else if(placeHolder[i].matches(regExes.POINTERCONSTANT))
                if(placeHolder[0].matches(regExes.LOADADDRESS))
                    Statement = "m";
                else
                    Statement = "d";
            else if(placeHolder[i].matches(regExes.REGISTERINDIRECTAREACROSSING)){
                if(i==1)
                    Statement = "b;_riac";
                else if((placeHolder[i-1].matches(regExes.DIRECTADDRESSING))|
                        (placeHolder[i-1].matches(regExes.PARTIALLYQUALIFIEDDBACCESS))|
                        (placeHolder[i-1].matches(regExes.FULLYQUALIFIEDDBACCESS))) // If a memory area has already been specified
                    Statement += ";_riai";                       // Tag Register Indirect, Area Internal.
                else                                            // Otherwise
                    Statement += ";_riac";                       // Tag Register Indirect, Area Crossing.
            }
            else if(placeHolder[i].matches(regExes.MEMORYINDIRECT)){
                if(Statement.matches(regExes.DIRECTADDRESSING))
                    Statement += ";_mi";
            }
            else if(placeHolder[i].matches(regExes.FULLYQUALIFIEDDBACCESS)){
                Statement = placeHolder[i].replaceAll(regExes.FULLYQUALIFIEDDBACCESS, "$2");
                if(Statement.equalsIgnoreCase("X"))
                        Statement = "b";
            }
            else if(placeHolder[i].matches(regExes.PARTIALLYQUALIFIEDDBACCESS)){
                Statement = placeHolder[i].replaceAll(regExes.PARTIALLYQUALIFIEDDBACCESS, "$1");
                if(Statement.equalsIgnoreCase("X"))
                    Statement = "b";
                Statement += ";_pqdb";
            }
            else if(placeHolder[i].matches(regExes.LOCALVARIABLE)){ // Check if this is a Local variable
                placeHolder[i] = placeHolder[i].replaceAll(regExes.LOCALCLEAN, "$1"); // Clean out any array brackets etc. if required.
                Statement = varGet(VAR, placeHolder[i]);          // if so, Get the Memory type from the parameter list
                Statement += ";_pa";                                // and Tag this for Parameter access.
            }
            else if((placeHolder[i].matches(regExes.TIMERCONSTANT))|
                    (placeHolder[i].matches(regExes.RADIXCONSTANT))|
                    (placeHolder[i].matches(regExes.STRINGCONSTANT))|
                    ((placeHolder[i].matches(regExes.VALUECONSTANT))&(i==1))){
                Statement = "K";
            }
            else if((placeHolder[i].matches(regExes.VALUECONSTANT))&(i==2))
                Statement = Statement; // Do nothing.            
            else{ // Error.  Stay here.  We can remove this if we need to.
                String messageString = "<<<< IDMemoryType(s[],s):"+Statement + ":"+String.valueOf(i)+">";
                for(String s:placeHolder)
                    messageString+=","+s;
                JOptionPane.showMessageDialog(null, messageString, "oops", JOptionPane.ERROR_MESSAGE);
                System.err.println(messageString);
                System.exit(0);
            }
        }
        return Statement;
    }
    
    /**
     * This function is used to return the delay time for each instruction.
     * Where multiple delays are separated with a ";", they are all added in this function
     * Example: L,W;_pqdb;_airi - <B>L</B>oad <B>W</B>ord from a <B>P</B>artially <B>Q</B>ualified <B>D</B>ata<B>B</B>ase using 
     * <B>A</B>rea <B>C</B>rossing <B>R</B>egister <B>I</B>ndirect addressing.
     * @param getM This is a String that contains the instruction and addional delay identifers
     * @return integer containing the delays for the identified parameters
     */
    private int mGet(String getM){
        String[] getMString = getM.split(";");
        int retVal = 0;        
        for(int i=0; i<getMString.length; i++){
                if(m.get(getMString[i].trim())==null){
                    System.out.println("<<<< mGet ["+String.valueOf(i)+"]: Could not find \""+getM+"\", Please update Database. ");            
                    return i;
                }
            retVal += m.get(getMString[i].trim());
        }
        return retVal;                                
    }
    
    /**
     * Convenience function to call the methods that call the data from the database.
     */
    private void getDataBaseInfo(){
        m = new HashMap<>();
        t = new HashMap<>();
        doMConnection(m,"SELECT * from 315Instructions");
        doTConnection(t,"SELECT * from dataTypes");
    }
    
    /**
     * Pull the instruction times from the SQL Database defined.
     * @param m the HashTable in which to place the Data.
     * @param SQLString The SQL String used to read the Database.
     */

    public static void doMConnection(Map m,String SQLString){
        ResultSet rs = null;
        String path = new java.io.File("c:\\temp\\STLExecTimes.mdb").getAbsolutePath();
        String db ="jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ="+path;
        try{
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Statement st; //<<<< Updated by Tips.
            try (Connection con = DriverManager.getConnection(db)) {
                st = con.createStatement();
                rs = st.executeQuery(SQLString);
                while(rs.next()){
                    m.put(rs.getString("Syntax"), rs.getInt("Time"));
                }    
            }
            st.close();
        }
        catch(SQLException | ClassNotFoundException ex){
            System.err.println("t:"+ex.toString());            
        }    
    }

    /**
     * Pull the data <u>T</u>ypes from the SQL Database defined.
     * @param t the HashTable in which to place the Data.
     * @param SQLString The SQL String used to read the Database.
     */
    public static void doTConnection(Map t,String SQLString){
        ResultSet rs = null;
        String path = new java.io.File("c:\\temp\\STLExecTimes.mdb").getAbsolutePath();
        String db ="jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ="+path;
        try{
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Statement st; //<<<< Updated by Tips.
            try (Connection con = DriverManager.getConnection(db)) {
                st = con.createStatement();
                rs = st.executeQuery(SQLString);
                while(rs.next()){
                    t.put(rs.getString("Name"), rs.getString("type"));
                }    
            }
            st.close();
        }
        catch(SQLException | ClassNotFoundException ex){
            System.err.println("m:"+ex.toString());            
        }    
    }    
    
    /**
     * Gets the Block times from the Database and returns a Hashmap
     * of Strings representing Functions versus times.
     * @return 
     */
    public Map getBlockTimes(){
        Map<String,Double> func = new HashMap<>();                
        Map<String,Integer> vRef = new HashMap<>();
        ArrayList<String> blockNames = new ArrayList<>(); // Represents the complete list of Block Names used in the program.        
        blockList = new ArrayList<>();
        String currentBlock;
        String previousBlock = "";        
        boolean incompleteDetected = false;
        Double currentMax = 0.0;
        
        // Go through the entire source code and build a list of blocks using the lineEntries.        
        //(This could have been done as sourceLineEntries was being built but thsi would have complicated the program) AC
        for(lineEntry a:sourceLineEntries){            
            currentBlock = a.getParentBlock();
            if(a.getParentBlock().length()>1){ // Ignore All unknown or blank entries.
                if(!currentBlock.equalsIgnoreCase(previousBlock)){ // This is a new Block
                    blockNames.add(currentBlock);
                    blockList.add(new blockClass());                
                    blockList.get(blockList.size()-1).setName(currentBlock);
                    vRef.put(currentBlock, blockList.size()-1); // A vertical reference of where to find each Function.
                    previousBlock = currentBlock;
                }
                blockList.get(blockList.size()-1).addLine(a);            
            }
        }
        
        // we now have a list of BlockCall Objects.  Now we have to check which ones are missing functions
        int noLoops = 1;        
        boolean allComplete = true;
        do{
           System.out.println("Completing Blocks - Iteration "+String.valueOf(noLoops)+".");
           noLoops++;   
           allComplete = true; // Assume they are all true by default.
            for(blockClass b:blockList){
                if(!b.isComplete()){ // if it's already complete = we don't really Care.
                    String[] missingList = b.getMissingFunctions().split(",");
                    for(String s:missingList){ // go through each Function missing in the Block.
                        if(vRef.get(s)==null){ // The Function being called is not available                        
                            JOptionPane.showMessageDialog(null, "Function '"+s+"' Not available", "Function Not Available", JOptionPane.WARNING_MESSAGE, null);
                            System.out.println("getBlockTimes: Function '"+s+"' Not available> Substituting for Zero.");
                            b.addFunctionTime(s, 0.0); // For now - Add an empty function.
                        }
                        else{
                            if(blockList.get(vRef.get(s)).isComplete())
                                b.addFunctionTime(s, blockList.get(vRef.get(s)).getExecutionTime());                        
                            else
                                System.out.println("getBlockTimes: Function '"+s+" Still registering incomplete");
                        }
                    }                        
                }
                if(b.isComplete())
                    func.put(b.getName(), b.getExecutionTime()); // only if this is complete will it be added.               
                else
                    allComplete = false; // Check if this is still incomplete - if so - will need another Pass.
            }
        }while((noLoops<blockList.size())&!allComplete);
        return func;
    }
    
    /**
     * Performs the Function to "Get" the local string type from the VAR map.
     * @param key The name of the variable being sought.
     * @return The Type for this variable
     */
    public static String varGet(Map VAR, String keyString){
        String tempString = keyString.replaceAll(regExes.ARRAYCLEAN, "$1"); // remove any "#" marks that identify the variable.                
        tempString = (String)VAR.get(tempString);
        if(tempString==null){
            JOptionPane.showMessageDialog(null, "<<<<varGet: Error Matching Variables: "+keyString,"oops",JOptionPane.ERROR_MESSAGE);
            System.err.println("<<<<varGet: Error Matching Variables: "+keyString);
            System.exit(0);
        }
        return tempString;       
    }
    /**
     * Let this be a test method to call other WIP Methods for calling.
     * Can be removed afterwards.
     */
    public void testMethod(){
        for(blockClass b:blockList)
            b.getJumpLabels();
    }
    
    private class S7Statement{
        public String command;
        public String arg1;
        public String arg2;
        public int blockType;
        public S7Statement(){
            command = "";
            arg1 = "";
            arg2 = "";
            blockType = -1;
        }
        
        public String getDetails(){
            return command + "," + arg1 + "," + arg2 + ";";
        }
    }
}
