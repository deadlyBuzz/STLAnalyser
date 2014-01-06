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
        lines = new HashMap<>();
    }
    
    public void addLine(lineEntry line){
        lines.put(line.getLineNumber(), line);
        totalTime += line.getLineTime();
        if(line.getLineType() == lineEntry.CALLFUNCTION){
            String functionCalled = line.getLineSource().replaceAll("\\s*CALL([\\w\\s]+)[\\(\\,]{0,1}.*", "$1").replaceAll("\\s","");
            missingFunctions.add(new MissingFnStruct(line.getLineNumber(), functionCalled));
        }        
        if(line.getLineType()==lineEntry.BLOCK_END)
            complete = (missingFunctions.isEmpty());
    }
    
    public void setName(String name){ this.Name = name; }
    public void setType(String type){ this.Type = type; }
        
    public lineEntry getLine(int lineNo) {
        if(lines.get(lineNo)==null)
            return null;
        else
            return lines.get(lineNo);
    }
    
    public String getMissingFunctions(){
        String placeHolder = "";
        ArrayList<String> fnList = new ArrayList<>();
        fnList.add(missingFunctions.get(0).fnName); // Add the first function as per default
        boolean found = false;
        for(MissingFnStruct a:missingFunctions){
            for(int i=0;i<fnList.size();i++)
                if(fnList.get(i).equalsIgnoreCase(a.fnName))
                    found = true;
            if(!found){ fnList.add(a.fnName); placeHolder+="|"+a.fnName;}
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
            }
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
