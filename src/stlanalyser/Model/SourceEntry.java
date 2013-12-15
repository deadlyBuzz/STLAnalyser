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
                if(i==Integer.parseInt(args[d]))
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
                                                            
                        String put = VAR.put(placeHolder[0].trim(), resolveMemory(placeHolder[1].replaceAll(";", "").trim())); // Clear out any trailing colons.                                           
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
                    //<<<< Still to do...                                                                              
                    // 1 - See how to encode the Direct and Indirect memory access into the encoding.
                                        
                    // If we're here - This is a Valid Line to process.
                    placeHolder = stringLine.trim().replaceAll(";", "").split("\\s+"); // split via any whitespace characters.

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
        Map<String, String>  memoryTypes= new HashMap<>();
        /*
         * E.G
         * B = Byte = 8 bits, any data type that takes up 8 bits is accessed as a Byte.
         * S5Time takes up 16 Bits thus is encoded as a Word.
         */
        memoryTypes.put("BOOL", "b");
        memoryTypes.put("BYTE", ",B");
        memoryTypes.put("INT",",W");
        memoryTypes.put("WORD",",W");
        memoryTypes.put("DWORD",",D");
        memoryTypes.put("DINT",",D");
        memoryTypes.put("REAL",",D");
        memoryTypes.put("S5TIME",",W");
        memoryTypes.put("TIME",",D");
        memoryTypes.put("DATE",",W");
        memoryTypes.put("CHAR",",B");
        memoryTypes.put("DATE_AND_TIME","X"); // X = Complex.
        memoryTypes.put("ANY", ",X");
        memoryTypes.put("STRING", "X");        
        memoryTypes.put("ARRAY", "X");        
        memoryTypes.put("STRUCT", "X");
        memoryTypes.put("TIMER", "P"); // P = Parameter.
        memoryTypes.put("COUNTER", "P"); 
        memoryTypes.put("POINTER", "P"); 
        memoryTypes.put("ANY", "P"); // P = Parameter.        
        
        placeHolder = memoryTypes.get(memory);     
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
        m.put("SET,b",50); // SS German pnemonic, S7300_Instruction_list.pdf P31
        m.put("CLR,b",50);
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
        
        m.put("POP,b",680);
        
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
        // Boolean Local Variable or Memory Flag
        if(Var.toUpperCase().matches("[LM]"))
            return "b";
        // Constant Timer Variable
        if(Var.toUpperCase().matches("S5T#.*"))
            return "K"; // Stored as a Word ???
        // If there is a ";", this is a single command and should be treated as a "b"
        if(Var.toUpperCase().matches("(;|JNB|JU|JC).*"))
            return "b";
        // Timers - A Quick and easy (and somewhat Lazy) solution.
        if(Var.toUpperCase().matches("T"))
            return "T";
        //Read is this is a constant value being passed.
        if(Var.toUpperCase().matches("[0-9\\.]+\\s*;?"))
            return "K";
        if(Var.length()>1)
            return Var.substring(Var.length()-1, Var.length());
        
        //If we've got this far, then this is an unknown address.  Treat it as such.
        System.out.println("<<<< IDMemoryType:" +Var+"> Unknown Var - Please Address");
        return Var;
    }
    
    private int mGet(String getM){
        int i = 0;
        if(m.get(getM)==null){
            System.out.println("<<<< mGet: Could not find "+getM);            
            return i;
        }
        return m.get(getM);
    }
}
