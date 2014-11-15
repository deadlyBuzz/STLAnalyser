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
    //private ArrayList<String> functions;         // List of functions called from this block
    private ArrayList<String> jumpLineMarkers;
    private ArrayList<FnStruct> utilisedFunctions;  // List of Missing functions 
    private Map<Integer,lineEntry> lines;
    private Map<String,Integer> labels;
    private Map<String,Integer> segments; // map of the different segments and corresponding execution time.
    private Map<String,String> labelsToMarkers; // Somewhere the Different Labels to Markers can exist.
    private ArrayList<segment> segmentList; // List of all the segments available to the block.
    private boolean haveSegmentDetails;
    
    public blockClass(){
        Name = "";
        Type = "";
        complete = false;
        totalTime = 0.0;
        utilisedFunctions= new ArrayList<>();
        lines = new LinkedHashMap<>();
        labels = new LinkedHashMap<>();
        haveSegmentDetails = false;
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
            //jumpTo = jumpTo.replaceAll("(.+);?\\W*(//.*)?", "$1").trim();                
            jumpTo = jumpTo.replaceAll("\\W*(\\w+)(.*);?\\W*(//.*)?", "$1").trim();                
            jumpTo = jumpTo.replaceAll(";", "");
            if(labels.get(jumpTo)!=null)
                jumpTo += ".loop";            
            jumpLineMarkers.add("Jump:"+String.valueOf(line.getLineNumber())+"|"+jumpTo);
        }

        if(sourceLine.matches(regExes.LABELID)){
            String labelMarker = sourceLine.replaceAll(regExes.LABELID, "$1");
            labelMarker = labelMarker.replaceAll(regExes.JUMPSTATEMENT+" .*", "$1").trim();
            jumpLineMarkers.add("Label:"+String.valueOf(line.getLineNumber())+"|"+labelMarker);
            labels.put(labelMarker, line.getLineNumber()); // remember this label.
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
        for(String a:jumpLineMarkers)
            System.out.println(this.Name+"-"+a);
        return jumpLineMarkers;
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
        segments = new HashMap<>();
        segmentList = new ArrayList<>();
        segment newSegment;  // Declare newSegment as a 
        Integer segmentDest = 0;
        
        String jumpTo = "";
        String segmentName = "";
        String oldSegmentName = "";
        Integer segmentTime = 0;
        
        for(Integer k:keys){
            lineCount++;
            if(newNetwork){
                newSource.add(addNewNetwork());
            }
            // add up the executiont time of each line.            
            
            if((markNext)&(lines.get(k).getLineType()!=lineEntry.CODE_COMMENT)){                
                oldSegmentName = segmentName;
                segmentName = blockMark+String.valueOf(lineCount);                
                newSource.add(markString(markFunction,segmentName));                
                if(newNetwork){
                    newSource.add(addNewNetwork()); // probably not needed.
                }
                else{
                    segments.put(oldSegmentName, segmentTime);
                    newSegment = new segment(this.Name, oldSegmentName);                    
                    newSegment.setTime(segmentTime);                                        
                    if((segmentDest == segment.JUMPUNCONDITIONAL)|(segmentDest == segment.JUMPCONDITIONAL)){ // if the segment includes a label
                        newSegment.setJumpDest(jumpTo,false); // until all the jump destinations are used.
                        newSegment.setVertexType(segmentDest);
                        jumpTo = ""; // Clear out the JumpTo
                        segmentList.add(newSegment);
                        if(segmentDest == segment.JUMPCONDITIONAL){ // implies that there is a second path if the conditionds are not met
                            newSegment = new segment(this.Name, oldSegmentName);
                            newSegment.setTime(segmentTime);
                            newSegment.setJumpDest(segmentName, true);
                            newSegment.setVertexType(segmentDest);
                            segmentList.add(newSegment);
                        }
                    }
                    else{ // This is a label
                        newSegment = new segment(this.Name, oldSegmentName);
                        newSegment.setTime(segmentTime);
                        newSegment.setJumpDest(segmentName, true);
                        newSegment.setVertexType(segmentDest);
                        segmentList.add(newSegment);
                        if(labelsToMarkers == null)
                            labelsToMarkers = new HashMap<>();
                        labelsToMarkers.put(jumpTo, segmentName);
                        jumpTo = "";
                    }
                    
                    segmentTime = 0;
                }
                markNext = false;
                newNetwork = false;
            }
            String sourceString = lines.get(k).getLineSource().trim();            
            segmentTime += lines.get(k).getLineTime();
            newSource.add(sourceString);//1- Add the line to the source list.
            
            if(sourceString.replaceAll(regExes.LABELID, "$2").matches(regExes.JUMPSTATEMENT+" .*")){
                String commandString = sourceString.replaceAll(regExes.LABELID, "$2");
                jumpTo = sourceString.replaceAll(regExes.LABELID, "$2");
                jumpTo = jumpTo.replaceAll(regExes.JUMPSTATEMENT+"(.*)","$2");
                //jumpTo = jumpTo.replaceAll("(.+);?\\W*(//.*)?", "$1").trim();                
                jumpTo = jumpTo.replaceAll("\\W*(\\w+)(.*);?\\W*(//.*)?", "$1").trim();                
                jumpTo = jumpTo.replaceAll(";", "");
    //            if(labels.get(jumpTo)!=null)
    //                jumpTo += ".loop";            
    //            jumpLineMarkers.add("Jump:"+String.valueOf(line.getLineNumber())+"|"+jumpTo);
                markNext = true;            
                if(commandString.matches("\\W+JU"+" .*"))
                    segmentDest = segment.JUMPUNCONDITIONAL;
                else
                    segmentDest = segment.JUMPCONDITIONAL;
                
            }
//            if(sourceString.matches(regExes.JUMPSTATEMENT+"(.*)")) // If the source entry is a jump statement
//                markNext= true;
            if(lines.get(k).getLineType()==lineEntry.BEGIN){ // Mark the first line in the Block.
                segmentDest = segment.INIT;
                newNetwork= true;
                markNext = true;
            }
            if(sourceString.matches(regExes.LABELID)){
                markNext = true;
                segmentDest = segment.LABEL;
                jumpTo = sourceString.replaceAll(regExes.LABELID, "$1");
            }
            
        }
        segments.put(segmentName, segmentTime); // last thing, record what the last segment time was.
        newSegment = new segment(this.Name, segmentName);
        newSegment.setJumpDest("end", true);
        newSegment.setTime(segmentTime);
        newSegment.setVertexType(segment.END);
        segmentList.add(newSegment);
        haveSegmentDetails = true;        
        
        // AT the end of the List, all labels and markers should have been identified.        
        for(segment s:segmentList){
            if(!s.destFinalised){
                String finalJumpDest = labelsToMarkers.get(s.getJumpDest());
                if(finalJumpDest!=null)
                    s.setJumpDest(finalJumpDest, true);                
            }
            System.out.println(s.toString());
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
    
    /**
     * public accessors class to determine if the segments are complete
     */
    public boolean classHasSegmentDetails(){return haveSegmentDetails;}
    
    /**
     * Return the block map in ArrayList&gt;String&lt; Format
     */
    public ArrayList<String> getBlockMap(){
        ArrayList<String> blockMap = new ArrayList<>();
        Set<String> keys = segments.keySet();
        for(String k:keys){
            blockMap.add(k+","+segments.get(k));                    
        }        
        return blockMap;
    }
    
    /**
     * Private class to represent each segment.
     */
    private static class segment{
        String mark;
        Integer executionTime;
        String parentBlock;
        String jumpDest;
        Integer vertexType;
        boolean destFinalised = false;
        public static final Integer INVALID = 0; // invalid condition
        public static final Integer JUMPCONDITIONAL = 1; // Jump Conditional
        public static final Integer JUMPUNCONDITIONAL = 2; // Jump unconditional
        public static final Integer INIT = 3;  // Start of block mark
        public static final Integer LABEL = 4;  // label - jump destination.
        public static final Integer END = 5;
        
        public segment(String newParentBlock){
            setParentBlock(newParentBlock);
            destFinalised = false;
        }
        public segment(String newParentBlock, String name){
            setParentBlock(newParentBlock);
            setName(name);
        }
        
        // Setters
        public void setName(String name){ this.mark = name;}
        public void setParentBlock(String parentBlock){ this.parentBlock = parentBlock;}
        public void setTime(Integer time){ this.executionTime = time;}
        public void setJumpDest(String jumpDest,boolean finalDest){ 
            this.jumpDest = jumpDest;
            this.destFinalised = finalDest;
        }
        public void setVertexType(Integer vType){ this.vertexType = vType;}
        
        // Getters
        public String getName(){ return this.mark;}
        public String getParentBlock(){ return this.parentBlock;}
        public Integer getTime(){ return this.executionTime;}
        public String getJumpDest(){ return this.jumpDest;}
        public Integer getVertexType(){ return this.vertexType;}
        
        // To String method
        @Override
        public String toString(){
            return parentBlock+"|"
                    +mark+"|"
                    +String.valueOf(executionTime)+"|"
                    +String.valueOf(vertexType)+"|"
                    +jumpDest;
        }
        
        public boolean hasFinalDest(){return destFinalised; }
    }
}
