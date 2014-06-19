/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stlanalyser;

import java.util.Map;
import java.util.LinkedHashMap;
import stlanalyser.Model.SourceEntry;

/**
 *
 * @author Alan Curley
 */
public class stlAnalyserWindow extends javax.swing.JFrame {

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(157, 157, 157)
                        .addComponent(goButton)
                        .addGap(18, 18, 18)
                        .addComponent(debugLinesTF, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)))
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
                    .addComponent(debugLinesTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
    private javax.swing.JTextArea dataEntryTA;
    private javax.swing.JTextField debugLinesTF;
    private javax.swing.JButton goButton;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
