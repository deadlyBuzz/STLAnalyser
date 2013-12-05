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
    public static final String reFUNCTIONHEADER = "FUNCTION(_BLOCK).*";
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
        String sourceCodeArray[] = SourceCode.split("\r\n");
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
    public void processSourceCode(){
        Integer StateMachine = new Integer(0);
        Map<String,String> VAR = new HashMap<>();
        final Integer BLOCKHEADER       = 0;             
        final Integer VARDECLARE        = 1;
        final Integer NETWORKANALYSIS   = 2;
        
        int[] debugLines = {26};
        
        //1 - Empty the arraylist to start with.
        sourceLineEntries.clear();
        StateMachine = BLOCKHEADER;        
        for(int i=0; i<sourceLines.size(); i++){
            String stringLine = sourceLines.get(i).replaceAll(reLABELID, "$1").trim(); // get rid of any Labels
            
            /*
             * Purely for Debug purposes only.
             * Allows me to Break the processing at any line I want to.
             */
            for(int d=0; d<debugLines.length; d++){
                if(i==debugLines[d])
                    System.out.println("<<<<Debug: "+ stringLine +">>>>");
            }
            switch(StateMachine){
/*----------*/  case 0 : //BlockHeader
                    // 1st Line - This is Definitley a Block Declaration.
                    if(i == 0) {
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.BLOCK_DECLARATION));
                        break;
                    }                    
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
/*----------*/  case 1 : //VARDECLARE   
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
                    else{
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.VAR_DECLARE));
                        String[] placeHolder =  stringLine.split(":");
                        String put = VAR.put(placeHolder[0], resolveMemory(placeHolder[1]));                                            
                    }
                    break;
/*----------*/  case 2 : // NETWORKANALYSIS
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
                        break;
                    }                    
                    
                    // NOP Function - No Operation, Used for Constructing Calls etc.
                    if((stringLine.matches(reNOPSTATEMENT))|(stringLine.matches(reBLDSTATEMENT))){
                    //if(stringLine.matches(reNOPSTATEMENT)){
                        sourceLineEntries.add(new lineEntry(stringLine,i,0,lineEntry.CODE_COMMENT));
                        break;
                    }
                    //<<<< Still to do...                                                                              
                    // 1 - See how to accomodate Timers, counters and "A(" into the Memory ID as they don't
                    //      divide too well into placeholder[0] & [1]
                    // 2 - See how to encode the Direct and Indirect memory access into the encoding.
                    // 3 - Update method to ID The memory typoe from the text passed to the functions
                    // 4 - Good ol' Indirect Addressing.
                    
                    
                    // If we're here - This is a Valid Line to process.
                    placeHolder = stringLine.trim().split("\\s+"); // split via any whitespace characters.

                    try{
                    if (placeHolder[1].matches(reLOCALVARIABLE)){
                        dataPlaceHolder = VAR.get(placeHolder[1]);
                        if (dataPlaceHolder == null){
                            JOptionPane.showMessageDialog(null, "Error Matching Variables- Ensure correct script details");
                            System.err.println("Error Matching Variables- Ensure correct script details");
                            System.exit(0);
                        }
                        else
                            placeHolder[1] = dataPlaceHolder;                                                    
                    }
                    else 
                        placeHolder[1] = IDmemoryType(placeHolder[1]);
                    }
                    catch(ArrayIndexOutOfBoundsException e){
                        System.err.println("<<<< Error processing entry:"+String.valueOf(i)+" " + placeHolder);
                    }
                    if(placeHolder[0].matches(reJUMPSTATEMENT))
                        sourceLineEntries.add(new lineEntry(stringLine,i,mGet(placeHolder[0]),lineEntry.CODE_SOURCE));
                    else
                        sourceLineEntries.add(new lineEntry(stringLine,i,mGet(placeHolder[0]+","+placeHolder[1]),lineEntry.CODE_SOURCE));

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
        
        placeHolder = memoryTypes.get(memory);     
        if(placeHolder == null) {
            JOptionPane.showMessageDialog(null, "Memory Type " + memory + " Not found --> Please adddess.");
            System.out.println("Memory Type " + memory + " Not found --> Please adddess.");
            placeHolder = "b";
        }        
        return placeHolder;
    }
    
    private void loadHashMap(){
        // Time is to be recorded in nanoseconds.
        // This is to accmmodate time of .11us for certain instructions.
        // until i come up with a better way of doing this, this is how it;s done.
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
        m.put("<>I,b",200);
        m.put("),b", 120);
        //m.put("BLD", 0); // Transferred to Comment
        m.put("JNB,b",160);
        m.put("T,B",80); // S7300_Instruction_list.pdf P36
        m.put("T,W",90);
        m.put("T,D",110);
        m.put("CALL",130);
        m.put("R,b", 80);
        m.put("S,b", 80);        
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
            return "W"; // Stored as a Word ???
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
