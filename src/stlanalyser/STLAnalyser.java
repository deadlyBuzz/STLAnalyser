/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stlanalyser;

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
        //defaultMethod();
        stlAnalyserWindow SAW = new stlAnalyserWindow();
        // Check if the Debug window is to be enabled or not?
        SAW.setDebugStatus(args.length>0);
        SAW.setVisible(true);
    }        
}
