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
    private Map<Integer, String> missingFunctions;  // List of Missing functions 
    private Map<Integer,lineEntry> lines;
    
    public blockClass(){
        Name = "";
        Type = "";
        complete = false;
        totalTime = 0.0;
        functions = new ArrayList<>();
        missingFunctions= new HashMap<>();
        lines = new HashMap<>();
    }
    
    public void addLine(lineEntry line){
        lines.put(line.getLineNumber(), line);
        totalTime += line.getLineTime();
        if(line.getLineType() == lineEntry.CALLFUNCTION){
            String functionCalled = line.getLineSource().replaceAll("\\s*CALL([\\w\\s]+)[\\(\\,]{0,1}.*", "$1").replaceAll("\\s","");
            missingFunctions.put(line.getLineNumber(), functionCalled);
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
    
    public boolean isComplete(){ return complete; }
    public Double getExecutionTime(){ return totalTime; }

}
