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
    public static final String FUNCTIONHEADER = "(FUNCTION|ORGANIZATION)(_BLOCK)?.*";
    public static final String DBHEADER       = "DATA_BLOCK.*";
    public static final String BLOCKFOOTER    = "END_(ORGANIZATION|DATA|FUNCTION)(_BLOCK)?";
    public static final String TITLELINE      = "TITLE =.*"; // Title will always be only one line.
    public static final String VERSIONLINE    = "VERSION : \\d+\\.\\d+";
    public static final String BEGIN          = "BEGIN";
    public static final String NETWORK        = "NETWORK";
    public static final String COMMENT        = "\\/\\/.*"; // Any line starting with "//" is a comment.
    public static final String BLOCKDETAILS   = "(AUTHOR|FAMILY|NAME)\\s*:.*";
    public static final String VARBEGIN       = "VAR(_INPUT|_OUTPUT|_TEMP|_IN_OUT)?";
    public static final String VAREND         = "END_VAR";
    public static final String LOCALVARIABLE  = "#[\\w_]+";
    public static final String SYMBOLICNAMES  = "\"[\\w_]+\"";
    public static final String ENDFUNCTION    = "END_FUNCTION.*";
    public static final String NOPSTATEMENT   = "NOP\\s+\\d+.*"; // NOP any whitespace, any digit
    public static final String LABELID        = "([a-zA-Z_]\\w{0,3}):\\s*(.*);";
    public static final String BLDSTATEMENT   = "BLD\\s+.*";
    public static final String JUMPSTATEMENT  = "(JCN|JNB|JBI|JNBI|JOS|JPZ|JMZ|JUO|LOOP|J[ULCOZNP])";
    public static final String CALLSTATEMENT  = "(CALL|UC|CC)";
    public static final String ARRAYSTATEMENT = ".*ARRAY\\s*\\[[0-9]+\\s+\\.+\\s[0-9]+\\s\\].*";
    public static final String ARRAYCLEAR     = "(\\[[ \\t0-9]+\\]\\s+)?;";
    public static final String LOADADDRESS    = "LAR(1|2).*";
    public static final String CLEANLINE      = "(.*);(\\s*//.*)?";
    public static final String REGINDIRECT    = "\\[AR.*"; // Register indirect check
    public static final String AREAINDIRECT   = "\\[[ML][DW].*"; // Area indirect addressing
    public static final String FBIdBlock = "\\s*(S?F[BC])\\s+\\d+";
    public static final String POINTERCONSTANT = "P##?\\w";

}
