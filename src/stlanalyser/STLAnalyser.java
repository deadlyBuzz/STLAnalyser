/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stlanalyser;

import stlanalyser.Model.SourceEntry;

/**
 *
 * @author Alan Curley
 */
public class STLAnalyser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String dataString = "";
        dataString = dataString.concat("FUNCTION FC 1 : VOID\r\n");
        dataString = dataString.concat("TITLE =\r\n");
        dataString = dataString.concat("VERSION : 0.1\r\n");
        dataString = dataString.concat("\r\n");
        dataString = dataString.concat("BEGIN\r\n");
        dataString = dataString.concat("NETWORK\r\n");
        dataString = dataString.concat("TITLE =\r\n");
        dataString = dataString.concat("\r\n");
        dataString = dataString.concat("      AN    T      1; \r\n");       // AN
        dataString = dataString.concat("      =     L      0.0; \r\n");     // =
        dataString = dataString.concat("      A     L      0.0; \r\n");     // A
        dataString = dataString.concat("      BLD   102; \r\n");            // BLD 102;
        dataString = dataString.concat("      L     S5T#3S; \r\n");         // L,W
        dataString = dataString.concat("      SD    T      1; \r\n");       // SD,T
        dataString = dataString.concat("      A     L      0.0; \r\n");     // A
        dataString = dataString.concat("      A     M      2.1; \r\n");     // A
        dataString = dataString.concat("      A(    ; \r\n");               // A(
        dataString = dataString.concat("      L     MW     8; \r\n");       // L,W
        dataString = dataString.concat("      L     0; \r\n");              // L,C // Load Constant - Same regardless of type.
        dataString = dataString.concat("      <>I   ; \r\n");
        dataString = dataString.concat("      )     ; \r\n");
        dataString = dataString.concat("      =     L      0.1; \r\n");
        dataString = dataString.concat("      A     L      0.1; \r\n");
        dataString = dataString.concat("      BLD   102; \r\n");
        dataString = dataString.concat("      =     M      2.0; \r\n");
        dataString = dataString.concat("      A     L      0.1; \r\n");
        dataString = dataString.concat("      JNB   _001; \r\n");
        dataString = dataString.concat("      L     0; \r\n");
        dataString = dataString.concat("      T     MW     8; \r\n");
        dataString = dataString.concat("_001: NOP   0; \r\n");
        dataString = dataString.concat("      A     L      0.0; \r\n");
        dataString = dataString.concat("      A     T      1; \r\n");
        dataString = dataString.concat("      R     M      2.1; \r\n");
        dataString = dataString.concat("END_FUNCTION\r\n");

        SourceEntry source = new SourceEntry(dataString);
        source.processSourceCode();
        source.printDetails(System.out);
        System.out.println("Done.");
    }
}
