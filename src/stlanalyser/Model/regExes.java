/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stlanalyser.Model;

/**
 * Somewhere to place all the regular expression constants used in the program.
 * //http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
 * @author Alan Curley
 */
public abstract class regExes {
    /** Regex String: "(FUNCTION|ORGANIZATION)(_BLOCK)?.*" **/
    public static final String FUNCTIONHEADER = "(FUNCTION|ORGANIZATION)(_BLOCK)?.*"; 
    /**  Regex String: <br/> "DATA_BLOCK.*"; **/
public static final String DBHEADER        =  "DATA_BLOCK.*";
/**  Regex String: <br/>"END_(ORGANIZATION|DATA|FUNCTION)(_BLOCK)?"; **/
    public static final String BLOCKFOOTER     =  "END_(ORGANIZATION|DATA|FUNCTION)(_BLOCK)?";
/**  Regex String: <br/>"TITLE =.*"; // Title will always be only one line. **/
    public static final String TITLELINE       =  "TITLE =.*"; // Title will always be only one line.
/**  Regex String: <br/>"VERSION : \\d+\\.\\d+"; **/
    public static final String VERSIONLINE     =  "VERSION : \\d+\\.\\d+";
/**  Regex String: <br/>"BEGIN"; **/
    public static final String BEGIN           =  "BEGIN";
/**  Regex String: <br/>"NETWORK"; **/
    public static final String NETWORK         =  "NETWORK";
/**  Regex String: <br/>"\\/\\/.*"; // Any line starting with "//" is a comment. **/
    public static final String COMMENT         =  "\\/\\/.*"; // Any line starting with "//" is a comment.
/**  Regex String: <br/>"(AUTHOR|FAMILY|NAME)\\s*:.*"; **/
    public static final String BLOCKDETAILS    =  "(AUTHOR|FAMILY|NAME)\\s*:.*";
/**  Regex String: <br/>"VAR(_INPUT|_OUTPUT|_TEMP|_IN_OUT)?"; **/
    public static final String VARBEGIN        =  "VAR(_INPUT|_OUTPUT|_TEMP|_IN_OUT)?";
/**  Regex String: <br/>"END_VAR"; **/
    public static final String VAREND          =  "END_VAR";
/**  Defines a Local Variable that starts with a "#" 
 *   Regex String: <br/>"#[\\w_]+"; 
 */
    public static final String LOCALVARIABLE   =  "#[\\w_\\[\\]]+;?";
/**  Regex String: <br/>"\"[\\w_]+\""; **/
    public static final String SYMBOLICNAMES   =  "\"[\\w_]+\"";
/**  Regex String: <br/>"END_FUNCTION.*"; **/
    public static final String ENDFUNCTION     =  "END_FUNCTION.*";
/**  Regex String: <br/>"NOP\\s+\\d+.*"; // NOP any whitespace, any digit **/
    public static final String NOPSTATEMENT    =  "NOP\\s+\\d+.*"; // NOP any whitespace, any digit
/**  Regex String: <br/>"([a-zA-Z_]\\w{0,3}):\\s*(.*);"; **/
    public static final String LABELID         =  "([a-zA-Z_]\\w{0,3}):\\s*(.*)(;|\\()";
/**  Regex String: <br/>"BLD\\s+.*"; **/
    public static final String BLDSTATEMENT    =  "BLD\\s+.*";
/**  Regex String: <br/>"(JCN|JNB|JBI|JNBI|JOS|JPZ|JMZ|JUO|LOOP|J[ULCOZNP])"; **/
    public static final String JUMPSTATEMENT   =  "(JCN|JCB|JNB|JBI|JNBI|JOS|JPZ|JMZ|JUO|LOOP|J[ULCOZNP])";
/**  Regex String: <br/>"(CALL|UC|CC)"; **/
    public static final String CALLSTATEMENT   =  "(CALL|UC|CC)";
/**  Regex String: <br/>".*ARRAY\\s*\\[[0-9]+\\s+\\.+\\s[0-9]+\\s\\].*"; **/
    public static final String ARRAYSTATEMENT  =  ".*ARRAY\\s*\\[[0-9]+\\s+\\.+\\s[0-9]+\\s\\].*";
    
    public static final String SINGLELINEARRAYSTATEMENT = "\\W*(.+)\\W+: ARRAY\\W+\\[\\d+ \\.\\. \\d+\\W+\\] OF (.*);\\W*";
    
/** String Declaration from the Block header<br/> Regex String: .* : STRING\\W+\\[\\d+\\W*\\]; */    
    public static final String STRINGDECLARE = "\\W*STRING\\W+\\[\\d+\\W*\\];?";
/**  Regex String: <br/>"(\\[[ \\t0-9]+\\]\\s+)?;"; **/
    public static final String ARRAYCLEAR      =  "(\\[[ \\t0-9]+\\]\\s+)?;";
/**  Regex String: <br/>"LAR(1|2).*"; **/
    public static final String LOADTRANSFERADDRESS     =  "[LT]AR(1|2).*";
/**  Cannot replicate in comments but is used to clean the line of trailing colons and comments **/
    public static final String CLEANLINE       =  "(.*);(\\s*//.*)?";
/**  Regex String: <br/>"\\[AR.*"; // Register indirect check **/
    public static final String REGINDIRECT     =  "\\[AR.*"; // Register indirect check
/**  Regex String: <br/>"\\[[ML][DW].*"; // Area indirect addressing **/
    public static final String AREAINDIRECT    =  "\\[[ML][DW].*"; // Area indirect addressing
/**  Regex String: <br/>"\\s*(S?F[BC])\\s+\\d+"; **/
    public static final String FBIdBlock  =  "\\s*(S?F[BC])\\s+(\\d+)";
    
/** 
 * Function block match for symbol table 
 * regex String: <br/>\\W&#42;(\s?F[BC])\\W+\\(d+).&#42;
 */
    public static final String ST_FBId = "\\W*(S?F[BC])\\W+(\\d+).*";
/** Pointer Constant 
 * Regex String: <br/>"P##?\\w"; 
 */
    public static final String POINTERCONSTANT  =  "P##?[\\w.]+";
    
    /**
     * Quick and dirty Regex for removing the "#" from in front of a variable name.\n
     * Regex String: "#?(.*)"
     * 
     */
    public static final String LOCALCLEAN = "#?(.*)";
    
    /** Regex used to clean Array statements = "#resultString[3]" => "#resultString" by using
     * &gt;String&lt;.ReplaceAll(regExes.ARRAYCLEAN,"$1");
     * 
     * Regex String: <br/>"(.*)\\[[0-9]+\\]"
     */
    public static final String ARRAYCLEAN = "(.*)\\[[\\W0-9]+\\]";

    /* ---- Typically used in IDMemory Function ----- */ 

    /**  Regex String: <br/>"P?[LMIQb]([^#]*)**/
    public static final String DIRECTADDRESSING  =  "P?[LMIQb]([^#]*)"; // include "b" incase the boolean has already been identified.
        
    /** Memory Indirect, E.G A M[MD 2] <br/> Regex String: <br/>"\\d+\\[[MLIQ].*" **/
    public static final String MEMORYINDIRECT = "\\[[MLIQ].*";
    /**  Regex String: "[TC]"; **/
    public static final String TIMERSANDCOUNTERS  =  "([TC])";
    
    /**  Register indirect - Area Crossing <br/> E.G L [AR1,P#0.0] <br/> Regex String: <br/>"\\[AR[12],P#\\d\\+.\\d\\]"; **/
    public static final String REGISTERINDIRECTAREACROSSING  =  "\\[AR[12],P#\\d+.\\d\\]";
    
    /**  Regex String: D[BI](\\d+)\\.DB(\\w); **/
    public static final String FULLYQUALIFIEDDBACCESS  =  "D[BI](\\d+)\\.DB(\\w)";
    /**  Regex String: "D[BI](\\d+)"; **/
    public static final String PARTIALLYQUALIFIEDDBACCESS  =  "D[BI](\\w+)";
    
    /** Timer constant.<br/> Regex String: (S5)?T#.* */
    public static final String TIMERCONSTANT = "(S5)?T#.*";
    
    /** Counter Constant <br/> Regex String: C#[\\d\\.]+ */
    public static final String COUNTERCONSTANT = "C#[\\d\\.]+";
    
    /** Byte constant <br/> regex String: L#[0-9]+;? */
    public static final String BYTECONSTANT = "L#[0-9]+;?";
    
    /** Value constant.<br/> \n Regex String: -?[0-9\\.]+\\s*;? */
    public static final String VALUECONSTANT = "-?[0-9\\.e\\+]+\\s*;?";
    
    /** Radix Constant (E.G B#16#FFFF is Base 16 constant)<br/>
     * Regex String: (B|W|2)#[0-9]+#?[A-Fa-f0-9]+\\s*;?
     */
    public static final String RADIXCONSTANT = "(B|W|2|D)W?#[0-9]+#?[A-Fa-f0-9]+\\s*;?";
    
    /** String constant <br/> Regex String: '.+' */
    public static final String STRINGCONSTANT = "'.+'";
    
    /** Status words, Such as Overflow (OV) Stored OverFlow (OS) etc.<br/>
     *  More detail can be found in s7300_instruction_list.pdf P13 <br/>
     * Regex Syntax: (OV|RLO|BR|OS|CC) 
     */
    public static final String STATUSWORDBITS = "(OV|RLO|BR|OS|CC)";
    
    /** Open DB Command <br/> Regex String: OPN*/
    public static final String OPENDBCOMMAND = "OPN";
    
    /** Where the Memory type is used, for example W for Word access etc. **/
    public static final String DATATYPE = "(B|W|DW|D)"; 
    
    /** 
     * An Access to a variable through dot extensions, such as found on <br/>
     * UDTs or Function Blocks.<br/>
     * E.G A #reset_pulse.Q Where #reset_pulse is a timer SFB 3 (a pulse timer)<br/>
     * Regex String: #?(\D[\w\[\]]+\.+).*
     */
    public static final String DOTEXTENSIONACCESS = "#?(\\D[\\w\\[\\]]+\\.+).*";
}
