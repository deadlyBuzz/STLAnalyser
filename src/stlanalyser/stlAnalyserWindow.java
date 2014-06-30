/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stlanalyser;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import stlanalyser.Model.SourceEntry;

/**
 *
 * @author Alan Curley
 */
public class stlAnalyserWindow extends javax.swing.JFrame
                                        implements PropertyChangeListener{

    ProgressMonitor sdbPM;
    File sdfFile;
    SDBFileIterator sdbfI;
    Map<String,String> sdfBlockList;
    boolean sdfFileAvailable = false; // <-- Flag to indicate that an sDF File is available
    
    /**
     * Creates new form stlAnalyserWindow
     */
    public stlAnalyserWindow() {
        initComponents();
        this.setTitle("STL Analyser Window.");        
    }
    
    public void setDebugStatus(boolean enabled){
        if(!enabled)
            debugLinesTF.setText("-1");
        debugLinesTF.setVisible(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        dataEntryTA = new javax.swing.JTextArea();
        goButton = new javax.swing.JButton();
        debugLinesTF = new javax.swing.JTextField();
        IHaveSDFFileButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        dataEntryTA.setColumns(20);
        dataEntryTA.setRows(5);
        dataEntryTA.setText("Paste or type the code to be analysed here.");
        jScrollPane1.setViewportView(dataEntryTA);

        goButton.setText("GO");
        goButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goButtonActionPerformed(evt);
            }
        });

        debugLinesTF.setText("-1");
        debugLinesTF.setToolTipText("");

        IHaveSDFFileButton.setText("I have SDF File");
        IHaveSDFFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IHaveSDFFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(IHaveSDFFileButton)
                        .addGap(60, 60, 60)
                        .addComponent(goButton)
                        .addGap(32, 32, 32)
                        .addComponent(debugLinesTF, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(goButton)
                    .addComponent(debugLinesTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(IHaveSDFFileButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
        // TODO add your handling code here:        
        SourceEntry source = new SourceEntry(dataEntryTA.getText());
        String debugLines[] = debugLinesTF.getText().split(",");        
        source.processSourceCode(debugLines);        
        //source.printDetails(System.out); // This is referencinbg the output of the SourceEntry object
        LinkedHashMap tempMap = new LinkedHashMap(source.getBlockTimes());        
        source.printBlockDetails(System.out); //this is referencing the output of each BlockList object
        source.testMethod(); // Test Method- Comment out when not using... or don't... I dont care. ;-)
        System.out.println("Map:"+tempMap.toString());
        System.out.println("Done.");
    }//GEN-LAST:event_goButtonActionPerformed

    private void IHaveSDFFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IHaveSDFFileButtonActionPerformed
        // TODO add your handling code here:
        String message = "This Option is for providing an exported symbol table \n"
                + "for inport into the program to aid analysis.\n"
                + "The symbol table must be exported in \".SDF\" format\n"
                + "If a symbol table is available in this format, please press \"OK\"\n"
                + "This will prompt you to select the file for import.\n"
                + "Press \"Cancel\" if you no longer wish to load a symbol table";
        int result = JOptionPane.showConfirmDialog(null, message,"Load Symbol Table",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(result == JOptionPane.OK_OPTION){
            JFileChooser fc = new JFileChooser("c:\\temp\\Siemens");
            int fileChooserRetVal = fc.showOpenDialog(this);
            if (fileChooserRetVal == JFileChooser.APPROVE_OPTION){ // User accepted a valid file
                sdfFile = fc.getSelectedFile();            
                sdfFileAvailable = true;
            }
            else
                sdfFileAvailable = false;
        }
        if(sdfFileAvailable){
            sdfBlockList = new LinkedHashMap();
            sdbPM = new ProgressMonitor(stlAnalyserWindow.this,
                    "Processing the SDB file",
                    "Run",0,100);
            sdbPM.setProgress(0);
            sdbfI = new SDBFileIterator();
            sdbfI.addPropertyChangeListener(this);
            goButton.setEnabled(false);
            IHaveSDFFileButton.setEnabled(false);
            sdbfI.execute();
        }
            
        // JOptionPane.showMessageDialog(null, "Work in Progress");        
    }//GEN-LAST:event_IHaveSDFFileButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                //if ("Metal".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(stlAnalyserWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(stlAnalyserWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(stlAnalyserWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(stlAnalyserWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new stlAnalyserWindow().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton IHaveSDFFileButton;
    private javax.swing.JTextArea dataEntryTA;
    private javax.swing.JTextField debugLinesTF;
    private javax.swing.JButton goButton;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    /**
     * Abstract Method required to implement StringWorker.
     * This method will update once the swingworker class 
     * updates the progress.
     * @param e 
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {        
        if ("progress" == evt.getPropertyName() ) {
            int progress = (Integer) evt.getNewValue();
            System.out.println("Debug - "+String.valueOf(progress));
            sdbPM.setProgress(progress);
            String message =
                String.format("Completed %d%%.\n", progress);
            sdbPM.setNote(message);
            //taskOutput.append(message);
            if (sdbPM.isCanceled() || sdbfI.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (sdbPM.isCanceled()) {
                    sdbfI.cancel(true);
                    //taskOutput.append("Task canceled.\n");
                } else {
                    sdbPM.setNote("Complete");
                }
                goButton.setEnabled(true);
                IHaveSDFFileButton.setEnabled(true);
            }
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Swingworker Class to represent iteration through the SDB File.
     */
    private class SDBFileIterator extends SwingWorker<Void, Integer> {
        int state = 0;
        int loopCount = 0;
        int result = 0;
        /**
         * The worker method for the thread.
         * @return
         * @throws Exception 
         */
        @Override
        protected Void doInBackground() throws Exception {
            // It's in here we've to take the file outlined, open it and iterate through it
            // to determine what functions are used.
            String lineRead;
            setProgress(0);
            state = 1;
            
            
            //** NOTE! **//
            // It is important to note how this thread opens and gains access to the file.
            // this resource needs to be locked and also needs to be notified in case of deadlock.
            // The ArrayList / Hashmap associated with the function block list also needs to be locked
            // to make it thread safe.
            // This may also be expanded to include the iteration of the source code
            // to derermine what blocks are missing from the symbol table.
            
            //synchronized(sdfFile){ // Eugh, American spelling :-P
            //synchronized(sdfBlockList){
                state = 2;
                BufferedReader inStream = getReader(sdfFile);  
                long totalSize = sdfFile.length();
                long currentSize = 0;
                int currentPerc = 0;
                
                try{
                    lineRead = inStream.readLine();
                    do{
                        loopCount++;
                        state = (state>3)?state:3;                        
                        state = (state>4)?state:4;
                        currentSize += lineRead.getBytes().length;
                        state = (state>5)?state:5;
                        currentPerc = (int) ((currentSize/totalSize)*100); //Hints.
                        result = currentPerc;
                        state = (state>6)?state:6;
                        lineRead = lineRead.replace('"', ' ').trim();
                        //System.out.println(lineRead);
                        if((lineRead!=null)&(lineRead.split(",")[2].matches("\\W*s?F[BC]\\W+\\d+.*"))){ // SFC, SFB, FC or FB                            
                            state = (state>7)?state:7;
                            sdfBlockList.put(lineRead.split(",")[1],lineRead.split(",")[2]);
                            state = (state>8)?state:8;
                            System.out.println(lineRead.split(",")[1]+"-"+lineRead.split(",")[2]);
                        }                       
                        setProgress(currentPerc);
                        lineRead = inStream.readLine();
                    }while(lineRead != null);
                    state = (state>9)?state:9;
                }
                catch(IOException e){
                    JOptionPane.showMessageDialog(null, "<<<< IOException encountered in SDBFileIterator.doInBackground()", "oops!", JOptionPane.ERROR_MESSAGE);
                    //state = -1;
                }
                catch(NullPointerException e){
                    JOptionPane.showMessageDialog(null, "<<<< Null pointer encountered in SDBFileIterator.doInBackground()", "oops!", JOptionPane.ERROR_MESSAGE);
                    //state = -2;
                }
            //}
            //}
            return null;
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        /**
         * This method will be called once the "doInBackground()" method has completed
         */
        @Override
        public void done(){
            System.out.println("Debug: "+String.valueOf(state)+","+String.valueOf(loopCount)+","+String.valueOf(result));
            setProgress(100);
        }
        
        
        /** 
        * Taken from the "JAVA For Dummies" Book by Bob Lowe and Barry Burd<br/>
        * This function gives back a BufferedReader object in which points
        * to the file provided in the @Name parameter.
        * 
        * Updated to have passed a File rather than a String.
        * 
        * @param name
        * @return Bufferedreader
        */
       private BufferedReader getReader(File passedFile){
           BufferedReader in = null;
           try{               
               in = new BufferedReader(
               new FileReader(passedFile));
           }
           catch(FileNotFoundException e){
               System.out.println("The File does not exist");
               JOptionPane.showMessageDialog(null, "The File does not exist");
               System.exit(0);
           }
           catch(IOException e){
               System.out.println("I/O Error");
               JOptionPane.showMessageDialog(null, "I/O Error");
               System.exit(0);
           }
           return in;
       }

        
    }
    
}
