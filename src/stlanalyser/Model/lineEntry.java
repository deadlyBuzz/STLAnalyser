/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stlanalyser.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a Data Model for a Line of STL source
 * @author Alan Curley
 */
public class lineEntry {
    private Integer lineNumber;
    private String lineSource;
    private Integer lineTime;
    private Integer lineType;
    private String parentBlock;
    
    /**
     * Define Static integers for Line entry Types
     */
    public static final int BLOCK_DECLARATION  = 0;
    public static final int BLOCK_TITLE        = 1;
    public static final int BLOCK_VERSION      = 2;
    public static final int BLOCK_COMMENT      = 3;
    public static final int BLOCK_END          = 4;
    public static final int VAR_HEADER         = 5;
    public static final int VAR_DECLARE        = 6;
    public static final int VAR_FOOTER         = 7;
    public static final int BEGIN              = 8;
    public static final int NETWORK_DECLARE    = 9;
    public static final int NETWORK_TITLE      = 10;
    public static final int CODE_COMMENT       = 11;
    public static final int CODE_SOURCE        = 12;
    public static final int DB_ENTRY           = 13;
    public static final int CALLFUNCTION       = 14;
    public static final int INVALID_ENTRY      = -1;
    public static final int INCOMPLETE_ENTRY   = -2;
    public static final int EMPTY_LINE         = -3;
    
    private Map<Integer, String> typeTable;

    /**
     * Default Constructor
     */
    public lineEntry(){
        this.lineNumber = 0;
        this.lineSource = "";
        this.lineTime = 0;
        this.lineType = INVALID_ENTRY;        
        this.parentBlock = "";
        initHashMap();
    }
    
    public lineEntry(String sourceLine){
        this.lineNumber = 0;
        this.lineSource = sourceLine;
        this.lineTime = 0;
        this.lineType = INCOMPLETE_ENTRY;
        this.parentBlock = "";
        initHashMap();
    }
    
    public lineEntry(String sourceLine, Integer lineNumber){
        this.lineNumber = lineNumber;
        this.lineSource = sourceLine;
        this.lineTime = 0;
        this.lineType = INCOMPLETE_ENTRY;
        this.parentBlock = "";
        initHashMap();
    }
    
    public lineEntry(String sourceLine, Integer lineNumber, Integer lineTime){
        this.lineTime = lineTime;
        this.lineNumber = lineNumber;
        this.lineSource = sourceLine;
        this.lineType = INCOMPLETE_ENTRY;
        this.parentBlock = "";
        initHashMap();
    }
    
    public lineEntry(String sourceLine, Integer lineNumber, Integer lineTime, Integer lineType){
        this.lineNumber = lineNumber;
        this.lineTime = lineTime;
        this.lineSource = sourceLine;
        this.lineType = lineType;
        this.parentBlock = "";
        initHashMap();
    }
    
    public lineEntry(String sourceLine, Integer lineNumber, Integer lineTime, Integer lineType, String parentBlock){
        this.lineNumber = lineNumber;
        this.lineTime = lineTime;
        this.lineSource = sourceLine;
        this.lineType = lineType;
        this.parentBlock = parentBlock;
        initHashMap();
        }

    
    private void initHashMap(){
        typeTable = new HashMap<>();
        typeTable.put(0,"BLOCK_DECLARATION");
        typeTable.put(1,"BLOCK_TITLE");
        typeTable.put(2,"BLOCK_VERSION");
        typeTable.put(3,"BLOCK_COMMENT");
        typeTable.put(4,"BLOCK_END");
        typeTable.put(5,"VAR_HEADER");
        typeTable.put(6,"VAR_DECLARE");
        typeTable.put(7,"VAR_FOOTER");
        typeTable.put(8,"BEGIN");
        typeTable.put(9,"NETWORK_DECLARE");
        typeTable.put(10,"NETWORK_TITLE");
        typeTable.put(11,"CODE_COMMENT");
        typeTable.put(12,"CODE_SOURCE");
        typeTable.put(13,"DB_ENTRY");
        typeTable.put(14,"CALL FUNCTION");
        typeTable.put(-1,"INVALID_ENTRY");
        typeTable.put(-2,"INCOMPLETE_ENTRY");
        typeTable.put(-3,"EMPTY_LINE");
    }
    
    /** Getters **/
    public int getLineNumber() { return this.lineNumber; }
    public int getLineTime() { return this.lineTime; }
    public int getLineType() { return this.lineType; }   
    public String getLineSource() { return this.lineSource; }
    public String getParentBlock() { return this.parentBlock;}    
    
    /** Setters **/
    public void setLineNumber(int lineNumber){ this.lineNumber = lineNumber; }
    public void setLineTime(int lineTime){ this.lineTime = lineTime; }
    public void setLinetype (int lineType){ this.lineType = lineType; }
    public void setLineSource(String lineSource){ this.lineSource = lineSource; }
    public void setParentBlock(String parentBlock) { this.parentBlock = parentBlock; }
    
    /** Convenience Methods **/
    public String getStringDetails(){
        return String.valueOf(this.lineNumber)+ "|" +
                this.lineSource               + "|" +
                String.valueOf(this.lineTime) + "|" +
                typeTable.get(this.lineType) + "|" +
                this.parentBlock;
                
    }
}