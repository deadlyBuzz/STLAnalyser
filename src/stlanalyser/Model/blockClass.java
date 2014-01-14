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
import static stlanalyser.Model.SourceEntry.reLABELID;

public class blockClass {
    private String Name;
    private String Type;
    private boolean complete;
    private Double totalTime;
    private ArrayList<String> functions;         // List of functions called from this block
    private ArrayList<MissingFnStruct> missingFunctions;  // List of Missing functions 
    private Map<Integer,lineEntry> lines;
    
    public blockClass(){
        Name = "";
        Type = "";
        complete = false;
        totalTime = 0.0;
        functions = new ArrayList<>();
        missingFunctions= new ArrayList<>();
        lines = new LinkedHashMap<>();
    }
    
    public void addLine(lineEntry line){
        lines.put(line.getLineNumber(), line);
        totalTime += line.getLineTime();
        if(line.getLineType() == lineEntry.CALLFUNCTION){
            String functionCalled = line.getLineSource().replaceAll(reLABELID, "$1").trim();
            functionCalled = functionCalled.replaceAll("\\s*CALL([\\w\\s]+)[\\(\\,]{0,1}.*", "$1").replaceAll("\\s","");
            missingFunctions.add(new MissingFnStruct(line.getLineNumber(), functionCalled));
        }        
        if(line.getLineType()==lineEntry.BLOCK_END)
            complete = (missingFunctions.isEmpty());
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
    
    public String getMissingFunctions(){        
        ArrayList<String> fnList = new ArrayList<>();
        fnList.add(missingFunctions.get(0).fnName); // Add the first function as per default
        String placeHolder = missingFunctions.get(0).fnName;
        boolean found = false;
        for(MissingFnStruct a:missingFunctions){
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
    
    public void addFunctionTime(String function, Double time){
        for(int i=0; i<missingFunctions.size();i++){
            if(missingFunctions.get(i).fnName.equalsIgnoreCase(function)){
                // Add the new Time to the Total Time.
                totalTime+=time;
                // Get the existing time for the Line enty identified.
                int tempTime = lines.get(missingFunctions.get(i).lineNo).getLineTime();
                // Add the new FB Time for the new line Entry.
                lines.get(missingFunctions.get(i).lineNo).setLineTime(tempTime+time.intValue());
                // Remove this function from the List.
                missingFunctions.remove(i);
                i--;
            }
        }
        // Once we've added a function - check if there are any more functiosn to be checked.
        // if not, this function is now complete.
        complete = missingFunctions.isEmpty();                
    }
    
    /**
     * Function when called will iterate through each entry in the Block
     * and output each entry to the valid PrintStream provided.
     * @param validStream PrintStream that will be used to write the Data.
     */
    public void printBlockDetails(PrintStream validStream, int printOption){
        validStream.println("\r\nPrinting Block" + this.Name + "\r\n");
        String jumpLabel = new String();
//        Set entrySet = lines.entrySet();
//        Iterator it = entrySet.iterator();        
//        while(it.hasNext()){            
            
//       }
        Set<Integer> keys = lines.keySet();
        for(Integer k:keys){
            // This will iterate through each lineEntry.
            String sourceLine = lines.get(k).getLineSource();            
//            if(sourceLine.matches(SourceEntry.reLABELID+SourceEntry.reJUMPSTATEMENT))
 //               jumpLabel+=sourceLine.replaceAll(SourceEntry.reJUMPSTATEMENT, "$1");
            validStream.println(lines.get(k).getStringDetails());
        }
    }
    
    private class MissingFnStruct{
        public int lineNo;
        public String fnName;
        public MissingFnStruct(int lineNo,String fnName){
            this.lineNo= lineNo;
            this.fnName = fnName;
        }
    }        

}
