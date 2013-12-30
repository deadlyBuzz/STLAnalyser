package stlanalyser.Model;

import java.io.PrintStream;
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
        loadHashMap(); // populate the hashmap with instructions and times.
    }
    
    /**
     * Secondary constructor where the Block string representing the STL source code
     * is passed when creating the object 
     * @param SourceCode 
     */
    public SourceEntry(String SourceCode){
        sourceLines = new ArrayList<>();        
        sourceLineEntries = new ArrayList<>();
        loadHashMap(); // populate the hashmap with instructions and times.

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
        for(int i=0; i<sourceLines.size(); i++){
            String stringLine = sourceLines.get(i).replaceAll(reLABELID, "$1").trim(); // get rid of any Labels
            
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
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_DECLARATION));
                        StateMachine = BLOCKHEADER;
                        break;
                    }
                    if(stringLine.matches(reDBHEADER)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_DECLARATION));
                        StateMachine = DATABLOCK;
                        break;
                    }
                    sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.EMPTY_LINE));
                    break;
/*----------*/  case 1 : //BlockHeader
                    // 1st Line - This is Definitley a Block Declaration.
                    if(stringLine.length()<1){
                        sourceLineEntries.add(new lineEntry(" ",i,0,lineEntry.EMPTY_LINE));
                        break;                        
                    }
                    if(stringLine.matches(reTITLELINE)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_TITLE));
                        break;
                    }
                    if(stringLine.matches(reVERSIONLINE)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_VERSION));
                        break;                        
                    }
                    if(stringLine.matches(reCOMMENT)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_COMMENT));
                        break;                        
                    }
                    if(stringLine.matches(reBLOCKDETAILS)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_COMMENT));
                        break;
                    }
                    if(stringLine.matches(reBEGIN)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BEGIN));
                        StateMachine = NETWORKANALYSIS;
                        break;
                    }
                    if(stringLine.matches(reVARBEGIN)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.VAR_HEADER));
                        StateMachine = VARDECLARE;
                        break;
                    }                    
/*----------*/  case 2 : //VARDECLARE   
                    if(stringLine.matches(reVAREND)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.VAR_FOOTER));
                        break;
                    }
                    else if(stringLine.matches(reVARBEGIN)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.VAR_HEADER));
                        break;
                    }
                    else if(stringLine.matches(reBEGIN)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BEGIN));
                        StateMachine = NETWORKANALYSIS;
                        break;
                    }
                    else if(stringLine.matches(reARRAYSTATEMENT)){
                        memData.add(stringLine);
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.VAR_DECLARE));
                        break;
                    }
                    else{
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.VAR_DECLARE));                        
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
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.CODE_COMMENT));
                        break;
                    }
                    // NETWORK identifer signifying the start of a network
                    if(stringLine.matches(reNETWORK)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.NETWORK_DECLARE));
                        break;
                    }
                    // TITLE = defining the title of the network - always here though sometimes blank
                    if(stringLine.matches(reTITLELINE)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.NETWORK_TITLE));
                        break;                        
                    }
                    // Anything less than 1 character long is an empty line.
                    if(stringLine.length()<1){
                        sourceLineEntries.add(new lineEntry(" ",i,0,lineEntry.EMPTY_LINE));
                        break;                        
                    }
                    // END_FUNCTION Declaration indicating the end if a function block or function call.
                    if(stringLine.matches(reENDFUNCTION)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_END));
                        StateMachine = UNKNOWN;
                        break;
                    }                    
                    
                    // NOP Function - No Operation, Used for Constructing Calls etc.
                    if((stringLine.matches(reNOPSTATEMENT))|(stringLine.matches(reBLDSTATEMENT))){
                    //if(stringLine.matches(reNOPSTATEMENT)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.CODE_COMMENT));
                        break;
                    }
                    
                    // If this is a Footer - Push back to "unknown" to search for another title.
                    if(stringLine.matches(reBLOCKFOOTER)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_END));
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
                           sourceLineEntries.add(new lineEntry(stringLine,i,mGet(placeHolder[0]+","+placeHolder[1]),lineEntry.CODE_SOURCE));
                       }
                       catch(ArrayIndexOutOfBoundsException e){
                           if(placeHolder.length<2) {// this is done as the statement is a standalone LAR1
                               sourceLineEntries.add(new lineEntry(stringLine,i,mGet(placeHolder[0]+",m"),lineEntry.CODE_SOURCE));
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
                                sourceLineEntries.add(new lineEntry(stringLine,i,mGet(placeHolder[0]+","+placeHolder[1]),lineEntry.CODE_SOURCE));
                            }

                            // -------- We're here this is a genuine sourcecode entry, - Now check what the statement has consisted of. --------------
                            // Check that the entry is a Jump statement in which the key part of the statement is the Jump function.
                            else if(placeHolder[0].matches(reJUMPSTATEMENT))
                                sourceLineEntries.add(new lineEntry(stringLine,i,mGet(placeHolder[0]),lineEntry.CODE_SOURCE));
                            // Check that the entry is calling an additional Function Block or DB Call.
                            else if(placeHolder[0].toUpperCase().matches(reCALLSTATEMENT)){
                                sourceLineEntries.add(new lineEntry(stringLine,i,mGet(placeHolder[0]+","+placeHolder[1]),lineEntry.CODE_SOURCE));
                                StateMachine = BLOCKCALL; // S7300_instruction_list.PDF P59 
                                                          // AWL_e.PDF P265
                            }
                            else{
                                // Now check if there is a third item in placeholder representing an indirect address access.                                
                                if(placeHolder.length==3){ // If there is a third level in placeholder - possibly an Area Internal register.
                                    if(placeHolder[2].matches(reREGINDIRECT))
                                        placeHolder[1] = placeHolder[1] + ";airi"; // annotate this with "Address internal, Register Indirect" addressing
                                    if(placeHolder[2].matches(reAREAINDIRECT))
                                        placeHolder[1] = placeHolder[1] + ";ai";   // annotate this with "Area Indirect" addressing
                                }
                                else
                                    if(placeHolder[1].matches(reREGINDIRECT))
                                        placeHolder[1] = "b;acri";    // Annotate this for Area Crossing, Register indirect
                                placeHolder[1] = IDmemoryType(placeHolder[1]);
                                sourceLineEntries.add(new lineEntry(stringLine,i,mGet(placeHolder[0]+","+placeHolder[1]),lineEntry.CODE_SOURCE));
                            }
                        }
                        else
                            sourceLineEntries.add(new lineEntry(stringLine,i,mGet(placeHolder[0]+",b"),lineEntry.CODE_SOURCE));                       
                    }
                    catch(ArrayIndexOutOfBoundsException e){
                        System.err.println("<<<< Error processing entry:"+String.valueOf(i)+" " + stringLine);
                    }

                    break;
/*----------*/  case 4 : //DATA BLOCK
                    if(stringLine.matches(reBLOCKFOOTER)){ // All Entries are Data points only until the end of the Block.
                        StateMachine = UNKNOWN;
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_END));
                        break;
                    }
                    sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.DB_ENTRY));
                    break;    
                case 5: // BLOCK CALL
                    // ignore all the entries while in block call until we see a ")"                    
                    sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.CODE_COMMENT));
                    //If this is a closing bracket - go back to calcuating execution times.
                    if(stringLine.matches(".*\\);?.*"))
                        StateMachine = NETWORKANALYSIS;
                    break;
                default :
                    System.out.println("<<<< How the Hell did I get here? Line" + String.valueOf(i) + ": " + stringLine);
                    break;
            }
        }
        
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
    
    private void loadHashMap(){
        // Time is to be recorded in nanoseconds.
        // This is to accmmodate time of .11us for certain instructions.
        // until i come up with a better way of doing this, this is how it;s done.
        // NOTE:  The instruction times here are all derived for the S7-315 controller.
        //      The page numbers are included in the comments in groups such that in 
        //      the event that the details of another PLC are required, it is easier
        //      reference.
        m = new HashMap<>();
        t = new HashMap<>();
        m.put("L,W", 110); //S7300_Instruction_list.pdf p34
        m.put("L,B", 90);
        m.put("L,D", 120);
        m.put("L,K", 90);  // Constant Values, all the same regardless of type
        m.put("T", 800);
        m.put("L,C", 450); // C= Counter
        m.put("LC,C", 1090); // Loading a counter value with a BCD Value
        m.put("LC,T", 1090); // Loading a Timer value with BCD
        m.put("=,b", 80);
        m.put("A,b",50); //S7300_Instruction_list.pdf p22<sup>1</sup>
        m.put("O,b", 50);
        m.put("AN,b",50); //S7300_Instruction_list.pdf p22<sup>1</sup>
        m.put("ON,b", 50);
        m.put("A,T",230);
        m.put("A,C",100);
        m.put("A(,b", 120);
        m.put("AN,T",230);
        m.put("AN,C",100);
        m.put("AN(,b", 120);
        m.put("O(,b",120);
        m.put("ON(,b",120);
        m.put("<>I,b",200); // S7300_instruction_list.PDF P49
        m.put(">=I,b", 200);
        m.put(">I,b", 200);
        m.put("<=I,b", 200);
        m.put("<I,b", 200);
        m.put("==I,b", 200);
        m.put("<>D,b",180); // S7300_instruction_list.PDF P49
        m.put(">=D,b", 180);
        m.put(">D,b", 180);
        m.put("<=D,b", 180);
        m.put("<D,b", 180);
        m.put("==D,b", 180);
        m.put("<>R,b",670); // S7300_instruction_list.PDF P49
        m.put(">=R,b", 670);
        m.put(">R,b", 670);
        m.put("<=R,b", 670);
        m.put("<R,b", 670);
        m.put("==R,b", 670);

        m.put("+I,b", 100); // S7300_instruction_list.PDF P43
        m.put("+D,b", 90); 
        m.put("+R,b", 440); 
        m.put("-I,b", 100); // S7300_instruction_list.PDF P43
        m.put("-D,b", 90); 
        m.put("-R,b", 440); 
        m.put("*I,b", 120); // S7300_instruction_list.PDF P43
        m.put("*D,b", 90); 
        m.put("*R,b", 440); 
        m.put("/I,b", 220); // S7300_instruction_list.PDF P44
        m.put("/D,b", 210); 
        m.put("/R,b", 1930);         
        m.put("MOD,b", 180);
        m.put("),b", 120);
        //m.put("BLD", 0); // Transferred to Comment
        m.put("JNB,b",160); // S7300_instruction_list.PDF P64
        m.put("T,B",80); // S7300_Instruction_list.pdf P36
        m.put("T,W",90);
        m.put("T,D",110);
        m.put("CALL,FB",2050); // S7300_instruction_list.PDF P60
        m.put("CALL,FC",2030);
        m.put("UC,FB",1590);
        m.put("UC,FC",1770);
        m.put("CC,FB",1590);
        m.put("CC,FC",1770);
        m.put("R,b", 80);
        m.put("S,b", 80);        
        m.put("SET,b",40); // SS German pnemonic, S7300_Instruction_list.pdf P31
        m.put("CLR,b",40);
        m.put("NOT,b",50);
        m.put("SD,T", 510); // SS German pnemonic, S7300_Instruction_list.pdf P32
        
        // Jump entries - different scenario.
        m.put("JC", 160); //s7300_instruction_list.pdf P64
        m.put("JCN", 160);
        m.put("JCB", 160);
        m.put("JNB", 160);
        m.put("JBI", 160);
        m.put("JBIN", 160);
        m.put("JO", 160);
        m.put("JOS", 160);
        m.put("JUO", 160);
        m.put("JZ", 160);
        m.put("JP", 210);
        m.put("JM", 210);
        m.put("JN", 160);
        m.put("JMZ", 160);
        m.put("JPZ", 160);
        m.put("JU", 160);
        m.put("JL", 160);
        m.put("LOOP", 150);
        
        m.put("BE,b",680); // S7300_instruction_list.pdf P61
        m.put("BEU,b",680);   
        m.put("BEC,b",680); 
        
        m.put("POP,b",80); // S7300_instruction_list.pdf P54
        
        m.put("+AR1,b",100); // S7300_instruction_list.pdf P48
        m.put("+AR1,w",120); // S7300_instruction_list.pdf P48
        m.put("+AR2,b",100); // S7300_instruction_list.pdf P48
        m.put("+AR2,w",120); // S7300_instruction_list.pdf P48
        
        m.put("LAR1,AR2",100); // S7300_instruction_list.pdf P37
        m.put("LAR1,DBD",210); // S7300_instruction_list.pdf P37
        m.put("LAR1,DID",400); // S7300_instruction_list.pdf P37
        m.put("LAR1,LD",210); // S7300_instruction_list.pdf P37
        m.put("LAR1,MD",210); // S7300_instruction_list.pdf P37
        m.put("LAR1,ACCU",100); // S7300_instruction_list.pdf P37
        m.put("LAR1,m",120); // S7300_instruction_list.pdf P37        
        m.put("LAR2,DBD",210); // S7300_instruction_list.pdf P37
        m.put("LAR2,DID",400); // S7300_instruction_list.pdf P37
        m.put("LAR2,LD",210); // S7300_instruction_list.pdf P37
        m.put("LAR2,MD",210); // S7300_instruction_list.pdf P37
        m.put("LAR2,ACCU",100); // S7300_instruction_list.pdf P37
        m.put("LAR2,m",120); // S7300_instruction_list.pdf P37
        
       m.put("airi", 100); // Area internal, address indirect - S7300_instruction_list.pdf P76
       m.put("ai", 100); // Area internal, address indirect - S7300_instruction_list.pdf P76
       m.put("acri",330); // Area crossing, address indirect - assume a Boolean?

        t.put("BOOL", "b");
        t.put("BYTE", "B");
        t.put("INT","W");
        t.put("WORD","W");
        t.put("DWORD","D");
        t.put("DINT","D");
        t.put("REAL","D");
        t.put("S5TIME","W");
        t.put("TIME","D");
        t.put("DATE","W");
        t.put("CHAR","B");
        t.put("DATE_AND_TIME","X"); // X = Complex.
        t.put("ANY", "X");
        t.put("STRING", "B"); // Complex but individual access treated as a Byte (Char)       
        t.put("ARRAY", "X");        
        t.put("STRUCT", "X");
        t.put("TIMER", "P"); // P = Parameter.
        t.put("COUNTER", "P"); 
        t.put("POINTER", "P"); 
        t.put("ANY", "P"); // P = Parameter.        


    /*
     * <sup>1</sup>: Different calculation when evaluating an A Etc. with a signal
     * compared with specified conditions. (Compare P22 with P27>
     */
    }
    
    public void printDetails(PrintStream s){
        for(lineEntry a:sourceLineEntries){
            s.println(a.getStringDetails());
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
        
        if(var.length>1)
            retVal = var[0].substring(var[0].length()-1, var[0].length());
        
        if(retVal.length()==0){//If we've got this far, then this is an unknown address.  Treat it as such.
            System.out.println("<<<< IDMemoryType:" +Var+"> Unknown Var - Please Address");
            return Var;
        }
        if(var.length>1)
            return retVal + ";" + var[1];
        else
            return retVal;
    }
    
    private int mGet(String getM){
        String[] getMString = getM.split(";");
        int retVal;
        int i = 0;
        if(m.get(getMString[0])==null){
            System.out.println("<<<< mGet 1: Could not find "+getM);            
            return i;
        }
        retVal = m.get(getMString[0]);
        if(getMString.length>1){
            if(m.get(getMString[1])==null){
                System.out.println("<<<< mGet 2: Could not find "+getM);            
                return i;
            }        
            retVal = retVal + m.get(getMString[1]);
        }        
        return retVal;                                
    }
}
