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
    public static final String reFUNCTIONHEADER = "(FUNCTION|ORGANIZATION)(_BLOCK)?.*";
    public static final String reDBHEADER       = "DATA_BLOCK.*";
    public static final String reBLOCKFOOTER    = "END_(ORGANIZATION|DATA|FUNCTION)(_BLOCK)?";
    public static final String reTITLELINE      = "TITLE =.*"; // Title will always be only one line.
    public static final String reVERSIONLINE    = "VERSION : \\d+\\.\\d+";
    public static final String reBEGIN          = "BEGIN";
    public static final String reNETWORK        = "NETWORK";
    public static final String reCOMMENT        = "\\/\\/.*"; // Any line starting with "//" is a comment.
    public static final String reBLOCKDETAILS   = "(AUTHOR|FAMILY|NAME)";
    public static final String reVARBEGIN       = "VAR(_INPUT|_OUTPUT|_TEMP)?";
    public static final String reVAREND         = "END_VAR";
    public static final String reLOCALVARIABLE  = "#[\\w_]+";
    public static final String reSYMBOLICNAMES  = "\"[\\w_]+\"";
    public static final String reENDFUNCTION    = "END_FUNCTION.*";
    public static final String reNOPSTATEMENT   = "NOP\\s+\\d+.*"; // NOP any whitespace, any digit
    public static final String reLABELID        = "[a-zA-Z_]\\w{0,3}:\\s*(.*);";
    public static final String reBLDSTATEMENT   = "BLD\\s+.*";
    public static final String reJUMPSTATEMENT  = "(J[ULCOZNP]|JCN|JNB|JBI|JNBI|JOS|JPZ|JMZ|JUO|LOOP)";
    public static final String reCALLSTATEMENT  = "(CALL|UC|CC)";
    public static final String reARRAYSTATEMENT = ".*ARRAY\\s*\\[[0-9]+\\s+\\.+\\s[0-9]+\\s\\].*";
    public static final String reARRAYCLEAR     = "(\\[[ \\t0-9]+\\]\\s+)?;";
    public static final String reLOADADDRESS    = "\\s*LAR(1|2).*";
    public static final String reCLEANLINE      = "(.*);(\\s*//.*)?";
    public static final String reREGINDIRECT    = "\\[AR.*"; // Register indirect check
    public static final String reAREAINDIRECT   = "\\[[ML][DW].*"; // Area indirect addressing
    
    
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
            String stringLine = rawStringLine.replaceAll(reLABELID, "$1").trim();
            stringLine = stringLine.replaceAll("(.+)//.*", "$1");
            if(i>0) 
                sourceLineEntries.get(i-1).setParentBlock(blockName);
            /*
             * Purely for Debug purposes only.
             * Allows me to Break the processing at any line I want to.
             */
            for(int d=0; d<args.length; d++){
                if(i==Integer.parseInt(args[d])-1)
                    System.out.println("<<<<Debug: "+ stringLine +">>>>");
            }
            switch(StateMachine){                
/*----------*/  case 0 : //Unknown.
                    if(stringLine.matches(reFUNCTIONHEADER)) {
                        blockName = stringLine.replaceAll("(FUNCTION|ORGANIZATION)(_BLOCK)?.*((FC|FB|OB|DB)\\s+[0-9]+).*","$3").replaceAll(" ", "");
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_DECLARATION));
                        StateMachine = BLOCKHEADER;                        
                        break;
                    }
                    if(stringLine.matches(reDBHEADER)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_DECLARATION));
                        StateMachine = DATABLOCK;
                        break;
                    }
                    sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.EMPTY_LINE));
                    blockName = "";
                    break;
/*----------*/  case 1 : //BlockHeader
                    // 1st Line - This is Definitley a Block Declaration.
                    if(stringLine.length()<1){
                        sourceLineEntries.add(new lineEntry(" ",i,0,lineEntry.EMPTY_LINE));
                        break;                        
                    }
                    if(stringLine.matches(reTITLELINE)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_TITLE));
                        break;
                    }
                    if(stringLine.matches(reVERSIONLINE)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_VERSION));
                        break;                        
                    }
                    if(stringLine.matches(reCOMMENT)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_COMMENT));
                        break;                        
                    }
                    if(stringLine.matches(reBLOCKDETAILS)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_COMMENT));
                        break;
                    }
                    if(stringLine.matches(reBEGIN)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BEGIN));
                        StateMachine = NETWORKANALYSIS;
                        break;
                    }
                    if(stringLine.matches(reVARBEGIN)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.VAR_HEADER));
                        StateMachine = VARDECLARE;
                        break;
                    }                    
/*----------*/  case 2 : //VARDECLARE   
                    if(stringLine.matches(reVAREND)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.VAR_FOOTER));
                        break;
                    }
                    else if(stringLine.matches(reVARBEGIN)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.VAR_HEADER));
                        break;
                    }
                    else if(stringLine.matches(reBEGIN)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BEGIN));
                        StateMachine = NETWORKANALYSIS;
                        break;
                    }
                    else if(stringLine.matches(reARRAYSTATEMENT)){
                        memData.add(stringLine);
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.VAR_DECLARE));
                        break;
                    }
                    else{
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.VAR_DECLARE));                        
                        String[] placeHolder =  stringLine.split(":");                        
                        if(placeHolder.length<2){ // if there is no Colon in the entry... why?
                                if(memData.get(memData.size()-1).matches(reARRAYSTATEMENT)){ // if the last entry was an Array Declare
                                        String[] datPlaceHolder = new String[]{"",""};                                        
                                        datPlaceHolder[1] = placeHolder[0].replace(";", "").trim(); // build a new "PlaceHolder"
                                        datPlaceHolder[0] = memData.get(memData.size()-1);
                                        datPlaceHolder[0] = datPlaceHolder[0].substring(0, datPlaceHolder[0].indexOf(":"));
                                        //datPlaceHolder[0] = datPlaceHolder[0].replace("(.*):(.*)", "$1:");
                                        placeHolder = datPlaceHolder;
                                        memData.remove(memData.size()-1); //When finished - Remove the last string item.
                                }
                            }
                                                            
                        String put = VAR.put(placeHolder[0].trim(), resolveMemory(placeHolder[1].replaceAll("(\\[[ \\t0-9]+\\]\\s+)?;", "").trim())); // Clear out any trailing colons.                                           
                    }
                    break;
/*----------*/  case 3 : // NETWORKANALYSIS
                    String[] placeHolder;
                    String dataPlaceHolder;

                    // Comments (Just like this one)
                    if(stringLine.matches(reCOMMENT)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.CODE_COMMENT));
                        break;
                    }
                    // NETWORK identifer signifying the start of a network
                    if(stringLine.matches(reNETWORK)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.NETWORK_DECLARE));
                        break;
                    }
                    // TITLE = defining the title of the network - always here though sometimes blank
                    if(stringLine.matches(reTITLELINE)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.NETWORK_TITLE));
                        break;                        
                    }
                    // Anything less than 1 character long is an empty line.
                    if(stringLine.length()<1){
                        sourceLineEntries.add(new lineEntry(" ",i,0,lineEntry.EMPTY_LINE));
                        break;                        
                    }
                    // END_FUNCTION Declaration indicating the end if a function block or function call.
                    if(stringLine.matches(reENDFUNCTION)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_END));
                        StateMachine = UNKNOWN;
                        break;
                    }                    
                    
                    // NOP Function - No Operation, Used for Constructing Calls etc.
                    if((stringLine.matches(reNOPSTATEMENT))|(stringLine.matches(reBLDSTATEMENT))){
                    //if(stringLine.matches(reNOPSTATEMENT)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.CODE_SOURCE));
                        break;
                    }
                    
                    // If this is a Footer - Push back to "unknown" to search for another title.
                    if(stringLine.matches(reBLOCKFOOTER)){
                        sourceLineEntries.add(new lineEntry(rawStringLine,i,0,lineEntry.BLOCK_END));
                        StateMachine = UNKNOWN;
                        break;
                    }
                    
                    // Loading address register X.
                    // This should be treated as a separate entity TBF.
                   if(stringLine.matches(reLOADADDRESS)) {
                       placeHolder = stringLine.replaceAll(reCLEANLINE, "$1").trim().split("\\s+"); // remove all comments from the line - clean source.
                       try{
                           if(placeHolder[1].matches("P#.*")){ // if we're passing a pointer to a specific variable.
                               placeHolder[1] = "m";
                           }
                           else{
                               placeHolder[1] = placeHolder[1].replace("([a-zA-Z]+)[0-9]+", "$1");
                               //throw new IllegalArgumentException();
                           }
                           sourceLineEntries.add(new lineEntry(rawStringLine,i,mGet(placeHolder[0]+","+placeHolder[1]),lineEntry.CODE_SOURCE));
                       }
                       catch(ArrayIndexOutOfBoundsException e){
                           if(placeHolder.length<2) {// this is done as the statement is a standalone LAR1
                               sourceLineEntries.add(new lineEntry(rawStringLine,i,mGet(placeHolder[0]+",m"),lineEntry.CODE_SOURCE));
                               break;
                            }
                           System.out.println("<<<< IndirectAddressing: exception accessing placeholder on entry>" + stringLine);
                       }
                       break;
                   }
                    
                                        
                    // If we're here - This is a Valid Line to process.
                    placeHolder = stringLine.trim().replaceAll(reCLEANLINE, "$1").split("\\s+"); // split via any whitespace characters and remove any trailing comments.
                    //placeHolder = stringLine.trim().replaceAll(";", "").split("\\s+"); // split via any whitespace characters and remove any trailing comments.
                    
                    try{
                        if(placeHolder.length>1){
                            //placeHolder[1] = placeHolder[1].trim().replaceAll("\\[[0-9\\s]+\\]","");                             
                            if (placeHolder[1].trim().replaceAll("\\[[0-9\\s]+\\]","").matches(reLOCALVARIABLE)){
                                dataPlaceHolder = VAR.get(placeHolder[1].replace("#", "").replaceAll("\\[[ \\t0-9]+\\].*", ""));                                
                                if (dataPlaceHolder == null){
                                    JOptionPane.showMessageDialog(null, "Error Matching Variables- Ensure correct script details");
                                    System.err.println("Error Matching Variables- Ensure correct script details");
                                    System.exit(0);
                                }
                                else
                                    placeHolder[1] = dataPlaceHolder;                                                    
                                sourceLineEntries.add(new lineEntry(rawStringLine,i,mGet(placeHolder[0]+","+placeHolder[1]),lineEntry.CODE_SOURCE));
                            }

                            // -------- We're here this is a genuine sourcecode entry, - Now check what the statement has consisted of. --------------
                            // Check that the entry is a Jump statement in which the key part of the statement is the Jump function.
                            else if(placeHolder[0].matches(reJUMPSTATEMENT))
                                sourceLineEntries.add(new lineEntry(rawStringLine,i,mGet(placeHolder[0]),lineEntry.CODE_SOURCE));
                            // Check that the entry is calling an additional Function Block or DB Call.
                            else if(placeHolder[0].toUpperCase().matches(reCALLSTATEMENT)){
                                sourceLineEntries.add(new lineEntry(rawStringLine,i,mGet(placeHolder[0]+","+placeHolder[1]),lineEntry.CALLFUNCTION));
                                if(stringLine.matches(".*\\(.*")) // If the "CALL" function does not have an open bracket, then then there won't be a close bracket.
                                StateMachine = BLOCKCALL; // S7300_instruction_list.PDF P59 
                                                          // AWL_e.PDF P265
                            }
                            else{
                                // Now check if there is a third item in placeholder representing an indirect address access.                                
                                if(placeHolder.length==3){ // If there is a third level in placeholder - possibly an Area Internal register.
                                    if(placeHolder[2].matches(reREGINDIRECT))
                                        placeHolder[1] = placeHolder[1] + ";_airi"; // annotate this with "Address internal, Register Indirect" addressing
                                    if(placeHolder[2].matches(reAREAINDIRECT))
                                        placeHolder[1] = placeHolder[1] + ";_ai";   // annotate this with "Area Indirect" addressing
                                }
                                else
                                    if(placeHolder[1].matches(reREGINDIRECT))
                                        placeHolder[1] = "b;_acri";    // Annotate this for Area Crossing, Register indirect
                                placeHolder[1] = IDmemoryType(placeHolder[1]);
                                sourceLineEntries.add(new lineEntry(rawStringLine,i,mGet(placeHolder[0]+","+placeHolder[1]),lineEntry.CODE_SOURCE));
                            }
                        }
                        else
                            sourceLineEntries.add(new lineEntry(rawStringLine,i,mGet(placeHolder[0]+",b"),lineEntry.CODE_SOURCE));                       
                    }
                    catch(ArrayIndexOutOfBoundsException e){
                        System.err.println("<<<< Error processing entry:"+String.valueOf(i)+" " + stringLine);
                    }

                    break;
/*----------*/  case 4 : //DATA BLOCK
                    if(stringLine.matches(reBLOCKFOOTER)){ // All Entries are Data points only until the end of the Block.
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
        // Boolean Local Variable or Memory Flag
        if(var[0].toUpperCase().matches("[LM]"))
            retVal = "b";
        // Constant Timer Variable
        if(var[0].toUpperCase().matches("S5T#.*"))
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
        if(var[0].toUpperCase().matches("[0-9\\.]+\\s*;?"))
            retVal = "K";
        if(var[0].toUpperCase().matches("(B|W|2)#[0-9]+#?[A-Fa-f0-9]+\\s*;?"))
            retVal = "K";        
        if(var[0].toUpperCase().matches("'.+'"))
            retVal = "K";        
         // instance or not Data block access - additional delay
        if(var[0].toUpperCase().matches("D[IB]\\w"))
            retVal = var[0].substring(var[0].length()-1, var[0].length()) + ";_pqdb"; // Partially qualified DB(I) access
         
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
                if(m.get(getMString[0].trim())==null){
                    System.out.println("<<<< mGet ["+String.valueOf(i)+"]: Could not find "+getM);            
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
            st.close();//>>>>
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
            st.close();//>>>>
        }
        catch(SQLException | ClassNotFoundException ex){
            System.err.println("m:"+ex.toString());            
        }    
    }    
    
    /**
     * <<<< Here Al.
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
    
}
