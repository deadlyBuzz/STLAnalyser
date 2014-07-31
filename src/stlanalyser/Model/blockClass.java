/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stlanalyser.Model;

/**
 * This Class represents an S7 Block.
 * each function Block is made up from
 * @author Alan Curley
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import static stlanalyser.Model.SourceEntry.*;

public class blockClass {
    private String Name;
    private String Type;
    private boolean complete;
    private Double totalTime;
    private ArrayList<String> functions;         // List of functions called from this block
    private ArrayList<String> jumpLineMarkers;
    private ArrayList<FnStruct> utilisedFunctions;  // List of Missing functions 
    private Map<Integer,lineEntry> lines;
    private ArrayList<simpleJumpLabels> jumpLabels;
    
    public blockClass(){
        Name = "";
        Type = "";
        complete = false;
        totalTime = 0.0;
        functions = new ArrayList<>();
        utilisedFunctions= new ArrayList<>();
        lines = new LinkedHashMap<>();
    }
    
    public void addLine(lineEntry line){
        lines.put(line.getLineNumber(), line);
        totalTime += line.getLineTime();
        if(line.getLineType() == lineEntry.CALLFUNCTION){
            String functionCalled = line.getLineSource().replaceAll(regExes.LABELID, "$2").trim();
            functionCalled = functionCalled.replaceAll("\\s*CALL([\\w\\s]+)[\\(\\,]{0,1}.*", "$1").replaceAll("\\s","");
            utilisedFunctions.add(new FnStruct(line.getLineNumber(), functionCalled));
        }        
        if(line.getLineType()==lineEntry.BLOCK_END)
            complete = (utilisedFunctions.isEmpty());
        
        if(jumpLineMarkers==null){
            jumpLineMarkers = new ArrayList<>();
        }
        
        String sourceLine = line.getLineSource().trim();
        if(sourceLine.replaceAll(regExes.LABELID, "$2").matches(regExes.JUMPSTATEMENT+" .*")){
            String jumpTo = sourceLine.replaceAll(regExes.LABELID, "$2");
            jumpTo = jumpTo.replaceAll(regExes.JUMPSTATEMENT+"(.*)","$2");
            jumpTo = jumpTo.replaceAll("(.+);?\\w*(//.*)?", "$1").trim();                
            jumpTo = jumpTo.replaceAll(";", "");
            jumpLineMarkers.add("Jump:"+String.valueOf(line.getLineNumber())+"|"+jumpTo);
        }

        if(sourceLine.matches(regExes.LABELID)){
            String labelMarker = sourceLine.replaceAll(regExes.LABELID, "$1");
            labelMarker = labelMarker.replaceAll(regExes.JUMPSTATEMENT+" .*", "$1").trim();
            jumpLineMarkers.add("Label:"+String.valueOf(line.getLineNumber())+"|"+labelMarker);
        }
    }
    
    public void setName(String name){ this.Name = name; }
    public void setType(String type){ this.Type = type; }
    public String getName() { return this.Name; }
    public String getType() { return this.Type; }
        
    public lineEntry getLine(int lineNo) {
        if(lines.get(lineNo)==null)
            return null;
        else
            return lines.get(lineNo);
    }
    
    /**
     * Obsolete function to get a list of functions that have been or have yet to be processed
     * and to find ones that have not been identidied.
     * Replaced with a version comparing the SDF File.
     * @return 
     */
    public String getMissingFunctions(){        
        ArrayList<String> fnList = new ArrayList<>();
        fnList.add(utilisedFunctions.get(0).fnName); // Add the first function as per default
        String placeHolder = utilisedFunctions.get(0).fnName;
        boolean found = false;
        for(FnStruct a:utilisedFunctions){
            found = false;
            for(int i=0;i<fnList.size();i++)
                if(fnList.get(i).equalsIgnoreCase(a.fnName))
                    found = true;
            if(!found){ 
                fnList.add(a.fnName); 
                placeHolder+=","+a.fnName;
            }
        }        
        return placeHolder;
    }
    
    public boolean isComplete(){ return complete; }
    public Double getExecutionTime(){ return totalTime; }
    
    /**
     * Called by the old getBlockTimes function as each block processed was
     * complete.
     * @param function
     * @param time Do
     */    
    public void addFunctionTime(String function, Double time){
        for(int i=0; i<utilisedFunctions.size();i++){
            if(utilisedFunctions.get(i).fnName.equalsIgnoreCase(function)){
                // Add the new Time to the Total Time.
                totalTime+=time;
                // Get the existing time for the Line enty identified.
                int tempTime = lines.get(utilisedFunctions.get(i).lineNo).getLineTime();
                // Add the new FB Time for the new line Entry.
                lines.get(utilisedFunctions.get(i).lineNo).setLineTime(tempTime+time.intValue());
                // Remove this function from the List.
                utilisedFunctions.remove(i);
                i--;
            }
        }
        // Once we've added a function - check if there are any more functiosn to be checked.
        // if not, this function is now complete.
        complete = utilisedFunctions.isEmpty();                
    }
    
    /**
     * Function when called will iterate through each entry in the Block
     * and output each entry to the valid PrintStream provided.
     * @param validStream PrintStream that will be used to write the Data.
     */
    public void printBlockDetails(PrintStream validStream, int printOption){
        validStream.println("\r\nPrinting Block" + this.Name + "\r\n");
        String jumpLabel = new String();
        Set<Integer> keys = lines.keySet();
        for(Integer k:keys){
            // This will iterate through each lineEntry.
            String sourceLine = lines.get(k).getLineSource();            
            validStream.println(lines.get(k).getStringDetails());
        }
    }
    
    public ArrayList<simpleJumpLabels> processJumpLabels(){
        jumpLabels = new ArrayList<>();
        
        return jumpLabels;
    }
    
    private class FnStruct{
        public int lineNo;
        public String fnName;
        public FnStruct(int lineNo,String fnName){
            this.lineNo= lineNo;
            this.fnName = fnName;
        }
    }        
        
    /**
     * Simple class to represent the Jump Labels in the program.
     */
    public class simpleJumpLabels{
        public int line;
        public String label;
        public int type;
        public final int STARTLABEL = 0;
        public final int ENDLABEL = 1;
        
        public simpleJumpLabels(){
            this.line = 0;
            this.label = "";
            this.type = 0;                        
        }
        
        public simpleJumpLabels(lineEntry line){
            if(line.getLineSource().matches(regExes.LABELID)){
                this.label = line.getLineSource().replaceAll(regExes.LABELID, "$2").trim();
                this.type = STARTLABEL;
            }
            else if(line.getLineSource().matches(regExes.JUMPSTATEMENT+"(.*)(//.*)?")){
                this.label = line.getLineSource().replaceAll(regExes.LABELID, "$2").trim();
                this.type = ENDLABEL;
            }
            this.line = line.getLineNumber();
        }
    }
    
    /**
     * This function is the function that builds the internal list of jumpLabel
     * objects.  
     * Obsolete... Pulled into addLine.
     * @return T
     */
    public ArrayList<String> getJumpLabels(){
        ArrayList<String> jumpLineMarkers = new ArrayList<>();
        Set<Integer> keys = lines.keySet();
        for(Integer k:keys){
            // This will iterate through each lineEntry.
            String sourceLine = lines.get(k).getLineSource().trim();
            if(sourceLine.replaceAll(regExes.LABELID, "$2").matches(regExes.JUMPSTATEMENT+" .*")){
                String jumpTo = sourceLine.replaceAll(regExes.LABELID, "$2");
                jumpTo = jumpTo.replaceAll(regExes.JUMPSTATEMENT+"(.*)","$2");
                jumpTo = jumpTo.replaceAll("(.+);?\\w*(//.*)?", "$1").trim();                
                jumpTo = jumpTo.replaceAll(";", "");
                jumpLineMarkers.add("Jump:"+String.valueOf(lines.get(k).getLineNumber())+"|"+jumpTo);
            }
                
            if(sourceLine.matches(regExes.LABELID)){
                String labelMarker = sourceLine.replaceAll(regExes.LABELID, "$1");
                labelMarker = labelMarker.replaceAll(regExes.JUMPSTATEMENT+" .*", "$1").trim();
                jumpLineMarkers.add("Label:"+String.valueOf(lines.get(k).getLineNumber())+"|"+labelMarker);
            }
        }
        for(String a:jumpLineMarkers)
            System.out.println(this.Name+"-"+a);
        return jumpLineMarkers;
    }
    
    /**
     * This Block is where the Map of the program path will be generated.
     * The Block will 
     * @param s 
     */
    public void generateBlockMap(PrintStream s){
        
    }
    
    /**
     * Build an arrayList containing each line of source to be included in the program.
     * @param markFunction The Function that will provide the Marking system.
     * @param blockMark The mark header for this block.
     * @return 
     */
    public ArrayList<String> markSource(String markFunction, String blockMark){
        ArrayList<String> newSource = new ArrayList<>();
        boolean markNext = false;
        boolean newNetwork = false;
        int lineCount = 0;
        Set<Integer> keys = lines.keySet();
        
        for(Integer k:keys){
            lineCount++;
            if(newNetwork){
                newSource.add(addNewNetwork());
            }
            if(markNext){
                newSource.add(markString(markFunction,blockMark+String.valueOf(lineCount)));
                if(newNetwork)
                    newSource.add(addNewNetwork());
                markNext = false;
                newNetwork = false;
            }
            String sourceString = lines.get(k).getLineSource().trim();            
            newSource.add(sourceString);//1- Add the line to the source list.
            
            if(sourceString.replaceAll(regExes.LABELID, "$2").matches(regExes.JUMPSTATEMENT+" .*"))
                markNext = true;            
//            if(sourceString.matches(regExes.JUMPSTATEMENT+"(.*)")) // If the source entry is a jump statement
//                markNext= true;
            if(lines.get(k).getLineType()==lineEntry.BEGIN){ // Mark the first line in the Block.
                newNetwork= true;
                markNext = true;
            }
            if(sourceString.matches(regExes.LABELID))
                markNext = true;
            
        }
        
        return newSource;
    }
    
    /** 
     * Convenience method for calling the mark function 
     */
    public String markString(String markFunction, String blockMark){
        return "CALL FC"+ markFunction + "(\n\t\tMarker\t:=DW#16#"+blockMark+");\n";
    }
    
    /**
     * Convenience method for when we wish to add a new network
     */
    public String addNewNetwork(){
        return "NETWORK\nTITLE = added by STLAnalyser Program \n";        
    }        
}
