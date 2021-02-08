/*
 *  DicomRouterGUI.java
 *
 *  Copyright (c) 2004 by
 *
 *  IFTM Institut für Telematik in der Medizn GmbH
 *  VISUS Technology Transfer GmbH
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *
 *****************************************************************************/
package de.iftm.dcm4che.router.gui;

import de.iftm.dcm4che.router.receiver.DicomStorageSCPReceiver;
import de.iftm.dcm4che.router.receiver.GeneralFileReceiver;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import org.apache.log4j.*;
import org.apache.log4j.Logger.*;

import de.iftm.dcm4che.router.property.*;
import de.iftm.dcm4che.router.receiver.*;
import de.iftm.dcm4che.router.util.*;


/**
 * This is the main program of the DicomRouter using a GUI.<br>
 * @author Thomas Hacklaender
 * @version 2005.01.18
 */
public class DicomRouterGUI2 extends javax.swing.JFrame implements DicomRouterEventListener {
    
    /** DOCUMENT ME! */
    private static Logger log = Logger.getLogger(DicomRouterGUI.class);
    
    /** URI of the property file of the application RouterGUI */
    private final String    myProperties = "./cfg/DicomRouterGUI2.properties";
    
    /** The file containing the source file. */
    private File            sourceFile = null;
    
    /** The file containing the configuration properties. */
    private File            propertyFile = null;
    
    /** The file containing the logging properties. */
    private File            loggerFile = null;
    
    /** Vector of files to process. */
    private Vector          fVec = null;
    
    /** The new PrintStream for System.out. */
    private PrintStream     ps;
    
    /** The DataInputStream for redirecting System.out to the JTextArea. */
    private DataInputStream iis;
    
    /** Receiver currently running */
    private DicomReceiver   receiverRunning = null;
    
    /**
     * Creates a new DicomRouterGUI application object.
     */
    public DicomRouterGUI2() {
        
        initComponents();
        postInitComponents();
        loadProperties();
        centerOnScreen();
        setStdOut();
    }
    
    
    /**
     *
     */
    protected void postInitComponents() {
        // Add a ConsoleAppender that uses PatternLayout using the 
        // PatternLayout.TTCC_CONVERSION_PATTERN and prints to System.out 
        // to the root category.
        BasicConfigurator.configure();
        
        // Set logging level to INFO
        Logger.getRootLogger().setLevel(Level.INFO);
        
        progressBar.setMinimum(0);
    }
    
    
    /**
     *
     */
    public void handleDicomRouterEvent(DicomRouterEvent evt) {
        switch (evt.getID()) {
            
            case DicomRouterEvent.NEXT_OBJECT_EVENT:
                // No visaul feedback;
                break;
                
            case DicomRouterEvent.FINISHED_EVENT:
                lastObjectSetFinished();
                break;
                
            case DicomRouterEvent.PROGRESS_EVENT:
                progressBar.setValue(evt.getActualValue());
                progressBar.setMaximum(evt.getMaxValue());
                break;
                
        }
    }
    
    
    /**
     *
     */
    protected void lastObjectSetFinished() {
        progressBar.setValue(0);
        
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
    }
    
    
    /**
     * Stores the GUI properties in the file ./cfg/DicomRouter.properties.
     */
    private void storeProperties() {
        Properties      props = new Properties();
        
        // Frame
        props.setProperty("frame.width", String.valueOf(this.getSize().width));
        props.setProperty("frame.height", String.valueOf(this.getSize().height));
        
        // taskTabbedPane
        if (taskTabbedPane.getSelectedComponent().equals(receiverTab)) {
            props.setProperty("taskTabbedPane", "receiverTab");
        } else if (taskTabbedPane.getSelectedComponent().equals(propertiesTab)) {
            props.setProperty("taskTabbedPane", "propertiesTab");
        } else if (taskTabbedPane.getSelectedComponent().equals(runTab)) {
            props.setProperty("taskTabbedPane", "runTab");
        }
        
        // sourceTabbedPane
        if (sourceTabbedPane.getSelectedComponent().equals(fileTab)) {
            props.setProperty("sourceTabbedPane", "fileTab");
        } else if (sourceTabbedPane.getSelectedComponent().equals(networkTab)) {
            props.setProperty("sourceTabbedPane", "networkTab");
        }
        
        // Source File
        if (sourceFile != null) {
            props.setProperty("source.file", sourceFile.toString());
        }
        
        if (selectFilesRadioBtn.isSelected()) {
            props.setProperty("source.filesRadioBtn", "true");
            props.setProperty("source.directoryRadioBtn", "false");
        } else {
            props.setProperty("source.filesRadioBtn", "false");
            props.setProperty("source.directoryRadioBtn", "true");
        }
        
        if (includeSubdirectoriesCheckBox.isSelected()) {
            props.setProperty("source.includeSubdirectoriesCheckBox", "true");
        } else {
            props.setProperty("source.includeSubdirectoriesCheckBox", "false");
        }
        
        // Property File
        if (propertyFile != null) {
            props.setProperty("property.file", propertyFile.toString());
        }
        
        // Logger
        if (loggerFile != null) {
            props.setProperty("logger.file", loggerFile.toString());
        }
        
        if (loggerDefaultBtn.isSelected()) {
            props.setProperty("logger.defaultBtn", "true");
            props.setProperty("logger.selectBtn", "false");
        } else {
            props.setProperty("logger.defaultBtn", "false");
            props.setProperty("logger.selectBtn", "true");
        }
        
        // Eigenschaften in Datei speichern
        try {
            File f = Util.uriToFile(myProperties);
            OutputStream out = new FileOutputStream(f);
            props.store(out, "");
        } catch (Exception e) {
            log.warn("Could not write properties to file " + myProperties + ": " + e.getMessage());
        } finally {
        }
    }
    
    
    /**
     * Loads the GUI properties from the file.
     */
    private void loadProperties() {
        Properties  props = new Properties();
        String      value;
        
        // Properties aus Datei laden
        try {
            File f = Util.uriToFile(myProperties);
            InputStream in = new FileInputStream(f);
            props.load(in);
        } catch (Exception e) {
            return;
        }
        
        // Frame
        try {
            if ((value = props.getProperty("frame.width")) != null) {
                int width = Integer.parseInt(value);
                if ((value = props.getProperty("frame.height")) != null) {
                    int height = Integer.parseInt(value);
                    this.setSize(width, height);
                }
            }
        } catch (Exception e) {}
        
        // taskTabbedPane
        if ((value = props.getProperty("taskTabbedPane")) != null) {
            if (value.equals("receiverTab")) {
                taskTabbedPane.setSelectedComponent(receiverTab);
            } else if (value.equals("propertiesTab")) {
                taskTabbedPane.setSelectedComponent(propertiesTab);
            } else if (value.equals("runTab")) {
                taskTabbedPane.setSelectedComponent(runTab);
            }
        }
        
        // sourceTabbedPane
        if ((value = props.getProperty("sourceTabbedPane")) != null) {
            if (value.equals("fileTab")) {
                sourceTabbedPane.setSelectedComponent(fileTab);
            } else if (value.equals("networkTab")) {
                sourceTabbedPane.setSelectedComponent(networkTab);
            }
        }
        
        // Source File
        if ((value = props.getProperty("source.file")) != null) {
            sourceFile = new File(value);
            if (sourceFile.isDirectory()) {
                sourceNameField.setText("");
                sourceParentField.setText(sourceFile.toString());
            } else {
                sourceNameField.setText(sourceFile.getName());
                sourceParentField.setText(sourceFile.getParent());
            }
            // Lokale Variablen updaten
            sourceNameToVector(sourceFile);
        }
        
        if ((value = props.getProperty("source.directoryRadioBtn")) != null) {
            selectDirectoryRadioBtn.setSelected(value.equals("true"));
        }
        if ((value = props.getProperty("source.filesRadioBtn")) != null) {
            selectFilesRadioBtn.setSelected(value.equals("true"));
        }
        
        if ((value = props.getProperty("source.includeSubdirectoriesCheckBox")) != null) {
            includeSubdirectoriesCheckBox.setSelected(value.equals("true"));
        }
        
        
        // Property File
        if ((value = props.getProperty("property.file")) != null) {
            // Lokale Variable updaten
            propertyFile = new File(value);
            propertyNameField.setText(propertyFile.getName());
            propertyParentField.setText(propertyFile.getParent());
        }
        
        // Logger
        if ((value = props.getProperty("logger.file")) != null) {
            // Lokale Variable updaten
            loggerFile = new File(value);
            loggerNameField.setText(loggerFile.getName());
            loggerParentField.setText(loggerFile.getParent());
        }
        
        if ((value = props.getProperty("logger.defaultBtn")) != null) {
            loggerDefaultBtn.setSelected(value.equals("true"));
        }
        if ((value = props.getProperty("logger.selectBtn")) != null) {
            loggerSelectBtn.setSelected(value.equals("true"));
        }
        
        // Lokale Variable fuer Logging setzten
        if (loggerDefaultBtn.isSelected()) {
            loggerFile = null;
        }
    }
    
    
    /**
     * Centers the application frame on the screen.
     */
    private void centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = this.getSize();
        
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        
    }
    
    
    /**
     * Redirects System.out and System.err to the JTextArea.<br>
     * see also: http://codeguru.earthweb.com/java/articles/382.shtml
     */
    private void setStdOut() {
        PrintStream ps = new PrintStream(new FilteredStream(new ByteArrayOutputStream()));
        
        // Redirect System Output und Error
        System.setOut(ps);
        System.setErr(ps);
    }
    
    
    
    /**
     * Builds the global field fVec from the filename given in the text field
     * sourceNameField.
     *
     * @param f the file to analyse.
     */
    private void sourceNameToVector(File f) {
        // Vector der files erstellen
        fVec = new Vector();
        addFile(f, fVec);
        
        // Anzahl der Files darstellen
        numberOfFilesLabel.setText(String.valueOf(fVec.size()));
    }
    
    
    /**
     * Adds one file or all files in a directory to a given Vector of files.
     * @param f The file or directory to add.
     * @param fVector The Vector of files.
     */
    private void addFile(File f, Vector fVector) {
        File[] fList;
        
        if (f == null) {
            return;
        }
        
        if (! f.exists()) {
            return;
        }
        
        if (f.isFile()) {
            // f ist ein File: nur diesen File zum Vector hinzufuegen
            fVector.add(f);
            
            return;
        } else {
            // f ist ein Directory: Alle Files aus dem Diectory hinzufuegen
            // Liste aller Files und Subdirectories erstellen
            fList = f.listFiles();
            
            // Liste abarbeiten
            for (int i = 0; i < fList.length; i++) {
                if (includeSubdirectoriesCheckBox.isSelected()) {
                    // Subdirectories mit einschliessen: Recursiver Aufruf
                    addFile(fList[i], fVector);
                } else {
                    // Subdirectories nicht einschliessen: Ignorieren
                    if (fList[i].isFile()) {
                        fVector.add(fList[i]);
                    }
                }
            }
        }
    }
    
    
    
    /** This method is called from within the constructor to initialize the form.<br>
     * WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sourceBtnGroup = new javax.swing.ButtonGroup();
        loggerBtnGroup = new javax.swing.ButtonGroup();
        fileTypeGroup = new javax.swing.ButtonGroup();
        guiPanel = new javax.swing.JPanel();
        taskTabbedPane = new javax.swing.JTabbedPane();
        receiverTab = new javax.swing.JPanel();
        sourcePanel = new javax.swing.JPanel();
        sourceTabbedPane = new javax.swing.JTabbedPane();
        fileTab = new javax.swing.JPanel();
        fileSourcePanel = new javax.swing.JPanel();
        numberOfFilesLabel = new javax.swing.JLabel();
        numberOfFilesText = new javax.swing.JLabel();
        selectDirectoryRadioBtn = new javax.swing.JRadioButton();
        selectFilesRadioBtn = new javax.swing.JRadioButton();
        includeSubdirectoriesCheckBox = new javax.swing.JCheckBox();
        sourceParentField = new javax.swing.JTextField();
        sourceNameField = new javax.swing.JTextField();
        selectSourceBtn = new javax.swing.JButton();
        networkTab = new javax.swing.JPanel();
        networkPanel = new javax.swing.JPanel();
        networkLabel = new javax.swing.JLabel();
        propertiesTab = new javax.swing.JPanel();
        propertyPanel = new javax.swing.JPanel();
        selectPropertyBtn = new javax.swing.JButton();
        propertyNameField = new javax.swing.JTextField();
        propertyParentField = new javax.swing.JTextField();
        runTab = new javax.swing.JPanel();
        processPanel = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        startBtn = new javax.swing.JButton();
        stopBtn = new javax.swing.JButton();
        messagePanel = new javax.swing.JPanel();
        scrollMessageArea = new javax.swing.JScrollPane();
        messageArea = new javax.swing.JTextArea();
        loggerPanel = new javax.swing.JPanel();
        selectLoggerBtn = new javax.swing.JButton();
        loggerNameField = new javax.swing.JTextField();
        loggerParentField = new javax.swing.JTextField();
        loggerDefaultBtn = new javax.swing.JRadioButton();
        loggerSelectBtn = new javax.swing.JRadioButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setTitle("DicomRouterGUI");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        guiPanel.setLayout(new java.awt.GridBagLayout());

        guiPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        guiPanel.setMinimumSize(new java.awt.Dimension(780, 600));
        guiPanel.setPreferredSize(new java.awt.Dimension(780, 520));
        receiverTab.setLayout(new java.awt.GridBagLayout());

        sourcePanel.setLayout(new java.awt.GridBagLayout());

        sourcePanel.setMinimumSize(new java.awt.Dimension(256, 192));
        sourcePanel.setPreferredSize(new java.awt.Dimension(256, 192));
        fileTab.setLayout(new java.awt.GridBagLayout());

        fileTab.setMinimumSize(new java.awt.Dimension(256, 192));
        fileSourcePanel.setLayout(new java.awt.GridBagLayout());

        fileSourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Source"));
        numberOfFilesLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        numberOfFilesLabel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 10);
        fileSourcePanel.add(numberOfFilesLabel, gridBagConstraints);

        numberOfFilesText.setFont(new java.awt.Font("Dialog", 0, 12));
        numberOfFilesText.setText("Number of selected files:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        fileSourcePanel.add(numberOfFilesText, gridBagConstraints);

        sourceBtnGroup.add(selectDirectoryRadioBtn);
        selectDirectoryRadioBtn.setText("Select directory");
        selectDirectoryRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectDirectoryRadioBtnActionPerformed(evt);
            }
        });
        selectDirectoryRadioBtn.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                selectDirectoryRadioBtnStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        fileSourcePanel.add(selectDirectoryRadioBtn, gridBagConstraints);

        sourceBtnGroup.add(selectFilesRadioBtn);
        selectFilesRadioBtn.setSelected(true);
        selectFilesRadioBtn.setText("Select files");
        selectFilesRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFilesRadioBtnActionPerformed(evt);
            }
        });
        selectFilesRadioBtn.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                selectFilesRadioBtnStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        fileSourcePanel.add(selectFilesRadioBtn, gridBagConstraints);

        includeSubdirectoriesCheckBox.setText("Include subdirectories");
        includeSubdirectoriesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeSubdirectoriesCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        fileSourcePanel.add(includeSubdirectoriesCheckBox, gridBagConstraints);

        sourceParentField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        fileSourcePanel.add(sourceParentField, gridBagConstraints);

        sourceNameField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(40, 10, 0, 10);
        fileSourcePanel.add(sourceNameField, gridBagConstraints);

        selectSourceBtn.setText("Select...");
        selectSourceBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSourceBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(40, 10, 0, 0);
        fileSourcePanel.add(selectSourceBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 90.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        fileTab.add(fileSourcePanel, gridBagConstraints);

        sourceTabbedPane.addTab("File", fileTab);

        networkTab.setLayout(new java.awt.GridBagLayout());

        networkTab.setMinimumSize(new java.awt.Dimension(256, 192));
        networkPanel.setLayout(new java.awt.GridBagLayout());

        networkLabel.setFont(new java.awt.Font("Dialog", 0, 14));
        networkLabel.setText("All configuration is done in the property file.");
        networkPanel.add(networkLabel, new java.awt.GridBagConstraints());

        networkTab.add(networkPanel, new java.awt.GridBagConstraints());

        sourceTabbedPane.addTab("Network", networkTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        sourcePanel.add(sourceTabbedPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        receiverTab.add(sourcePanel, gridBagConstraints);

        taskTabbedPane.addTab("Receiver", receiverTab);

        propertiesTab.setLayout(new java.awt.GridBagLayout());

        propertiesTab.setMinimumSize(new java.awt.Dimension(32, 256));
        propertiesTab.setPreferredSize(new java.awt.Dimension(32, 256));
        propertyPanel.setLayout(new java.awt.GridBagLayout());

        propertyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Property file", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11)));
        propertyPanel.setMinimumSize(new java.awt.Dimension(256, 102));
        propertyPanel.setPreferredSize(new java.awt.Dimension(256, 102));
        propertyPanel.setVerifyInputWhenFocusTarget(false);
        selectPropertyBtn.setText("Select...");
        selectPropertyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPropertyBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        propertyPanel.add(selectPropertyBtn, gridBagConstraints);

        propertyNameField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        propertyPanel.add(propertyNameField, gridBagConstraints);

        propertyParentField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        propertyPanel.add(propertyParentField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        propertiesTab.add(propertyPanel, gridBagConstraints);

        taskTabbedPane.addTab("Properties", propertiesTab);

        runTab.setLayout(new java.awt.GridBagLayout());

        runTab.setMinimumSize(new java.awt.Dimension(384, 256));
        runTab.setPreferredSize(new java.awt.Dimension(384, 256));
        runTab.setRequestFocusEnabled(false);
        processPanel.setLayout(new java.awt.GridBagLayout());

        processPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Process"));
        progressBar.setMinimumSize(new java.awt.Dimension(10, 20));
        progressBar.setPreferredSize(new java.awt.Dimension(148, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 10, 10);
        processPanel.add(progressBar, gridBagConstraints);

        startBtn.setText("Start");
        startBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        processPanel.add(startBtn, gridBagConstraints);

        stopBtn.setText("Stop");
        stopBtn.setEnabled(false);
        stopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        processPanel.add(stopBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        runTab.add(processPanel, gridBagConstraints);

        messagePanel.setLayout(new java.awt.GridBagLayout());

        messagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Messages"));
        messageArea.setEditable(false);
        scrollMessageArea.setViewportView(messageArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        messagePanel.add(scrollMessageArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        runTab.add(messagePanel, gridBagConstraints);

        loggerPanel.setLayout(new java.awt.GridBagLayout());

        loggerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Logger", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11)));
        loggerPanel.setMinimumSize(new java.awt.Dimension(256, 170));
        loggerPanel.setOpaque(false);
        loggerPanel.setPreferredSize(new java.awt.Dimension(256, 170));
        selectLoggerBtn.setText("Select...");
        selectLoggerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectLoggerBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        loggerPanel.add(selectLoggerBtn, gridBagConstraints);

        loggerNameField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        loggerPanel.add(loggerNameField, gridBagConstraints);

        loggerParentField.setEditable(false);
        loggerParentField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggerParentFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        loggerPanel.add(loggerParentField, gridBagConstraints);

        loggerBtnGroup.add(loggerDefaultBtn);
        loggerDefaultBtn.setSelected(true);
        loggerDefaultBtn.setText("Use default logger");
        loggerDefaultBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggerDefaultBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        loggerPanel.add(loggerDefaultBtn, gridBagConstraints);

        loggerBtnGroup.add(loggerSelectBtn);
        loggerSelectBtn.setText("Select logger");
        loggerSelectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggerSelectBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        loggerPanel.add(loggerSelectBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        runTab.add(loggerPanel, gridBagConstraints);

        taskTabbedPane.addTab("Run", runTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        guiPanel.add(taskTabbedPane, gridBagConstraints);

        getContentPane().add(guiPanel, java.awt.BorderLayout.CENTER);

        menuBar.setBorder(null);
        fileMenu.setText("File");
        openMenuItem.setText("Open");
        openMenuItem.setEnabled(false);
        fileMenu.add(openMenuItem);

        saveMenuItem.setText("Save");
        saveMenuItem.setEnabled(false);
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.setEnabled(false);
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");
        editMenu.setEnabled(false);
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText("Help");
        contentsMenuItem.setText("Contents");
        contentsMenuItem.setEnabled(false);
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void stopBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopBtnActionPerformed
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        
        if (receiverRunning != null) {
            receiverRunning.stop();
        }
    }//GEN-LAST:event_stopBtnActionPerformed
    
    private void includeSubdirectoriesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeSubdirectoriesCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_includeSubdirectoriesCheckBoxActionPerformed
    
    private void selectDirectoryRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectDirectoryRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_selectDirectoryRadioBtnActionPerformed
    
    private void selectFilesRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectFilesRadioBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_selectFilesRadioBtnActionPerformed
    
    private void loggerParentFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggerParentFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_loggerParentFieldActionPerformed
    
    
    /**
     * Action method of the radio button to select a logger file.
     * @param evt the event.
     */
    private void loggerSelectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggerSelectBtnActionPerformed
        loggerFile = new File(new File(loggerParentField.getText()), loggerNameField.getText());
    }//GEN-LAST:event_loggerSelectBtnActionPerformed
    
    
    /**
     * Action method of the radio button to select the default logger file.
     * @param evt the event.
     */
    private void loggerDefaultBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggerDefaultBtnActionPerformed
        loggerFile = null;
    }//GEN-LAST:event_loggerDefaultBtnActionPerformed
    
    
    /**
     * Action method of the button to select a logger file.
     * @param evt the event.
     */
    private void selectLoggerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectLoggerBtnActionPerformed
        JFileChooser    jfc;
        
        // Klasse fuer den Dialog generieren
        jfc = new JFileChooser();
        
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Immer nur einen File selektieren
        jfc.setMultiSelectionEnabled(false);
        
        // Bestehenden Namen als Default eintragen
        jfc.setCurrentDirectory(loggerFile);
        
        if (jfc.showOpenDialog(this) != 0) {
            // Cancel oder Dialog ohne Auswahl geschlossen
            return;
        }
        
        if (jfc.getSelectedFile() == null) {
            // Kein gueltiges File selektiert
            return;
        }
        
        // File holen
        loggerFile = jfc.getSelectedFile();
        
        // Textfeld updaten
        loggerNameField.setText(loggerFile.getName());
        loggerParentField.setText(loggerFile.getParent());
    }//GEN-LAST:event_selectLoggerBtnActionPerformed
    
    
    /**
     * Action method of the radio button to select a source directory.
     * @param evt the event.
     */
    private void selectDirectoryRadioBtnStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_selectDirectoryRadioBtnStateChanged
        sourceNameField.setText("");
        numberOfFilesLabel.setText("0");
    }//GEN-LAST:event_selectDirectoryRadioBtnStateChanged
    
    
    /**
     * Action method of the radio button to select source files.
     * @param evt the event.
     */
    private void selectFilesRadioBtnStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_selectFilesRadioBtnStateChanged
        sourceNameField.setText("");
        numberOfFilesLabel.setText("0");
    }//GEN-LAST:event_selectFilesRadioBtnStateChanged
    
    
    /**
     * Action method of the button to start the processing.
     * @param evt the event.
     */
    private void startBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBtnActionPerformed
        
        // Inhalt des JTextArea loeschen.
        messageArea.setText("");
        
        // File oder Network Runner starten
        if (sourceTabbedPane.getSelectedComponent().equals(fileTab)) {
            
            if ((fVec != null) & (propertyFile != null)) {
                startFile();
            } else {
                // Source und/oder Property file fehlen
                messageArea.append("<No propery or source file selected.>" + "\n");
                return;
            }
            
        } else {
            startNetwork();
        }
        
    }//GEN-LAST:event_startBtnActionPerformed
    
    
    /**
     *
     */
    public void startFile() {
        GeneralFileReceiver     fileReceiver;
        Properties              receiverProps;
        boolean                 success;
        
        fileReceiver = GeneralFileReceiver.createByName(propertyFile);
        if (fileReceiver == null) {
            return;
        }
                
        if (loggerFile == null) {
            // Kein Loggerfile angegeben. Default Logger verwenden
            fileReceiver.setLoggerProperties((Properties) null);
        } else {
            // Spezieller Loggerfile angegeben.
            try {
                fileReceiver.setLoggerProperties(loggerFile.toURI().toString());
            } catch (IOException e) {
                log.error("Can't load logger properties from: " + loggerFile.toURI().toString());
                return;
            }
        }
        
        fileReceiver.addDicomRouterEventListener(this);
        fileReceiver.setFileVectorToProcess(fVec);
        receiverRunning = fileReceiver;
        success = fileReceiver.start();
        
        if ( success) {
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        } else {
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        }
    }    

    
    /**
     * This method starts the network receiver. The method runs foreever. The START
     * button is disabled.
     */
    private void startNetwork() {
        DicomStorageSCPReceiver dsr = null;
        boolean                 success;
        
        try {
            dsr = new DicomStorageSCPReceiver(propertyFile.toURI().toString());
        } catch (IOException e) {
            log.error("Can't load DicomRouter properties from: " + propertyFile.toURI().toString());
            return;
        }
             
        if (loggerFile == null) {
            // Kein Loggerfile angegeben. Default Logger verwenden
            dsr.setLoggerProperties((Properties) null);
        } else {
            // Spezieller Loggerfile angegeben.
            try {
                dsr.setLoggerProperties(loggerFile.toURI().toString());
            } catch (IOException e) {
                log.error("Can't load logger properties from: " + loggerFile.toURI().toString());
                return;
            }
        }
        
        receiverRunning = dsr;
        success = dsr.start();
        if ( success) {
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        } else {
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        }
    }
    
    
    /**
     * Action method of the help menu. Displays a copyright notice.
     * @param evt the event.
     */
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        JOptionPane.showMessageDialog(this, Util.getCopyrightMessage());
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    
    
    /**
     * Action method of the button to select the poperty file.
     * @param evt the event.
     */
    private void selectPropertyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectPropertyBtnActionPerformed
        JFileChooser    jfc;
        
        // Klasse fuer den Dialog generieren
        jfc = new JFileChooser();
        
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Immer nur einen File selektieren
        jfc.setMultiSelectionEnabled(false);
        
        // Bestehenden Namen als Default eintragen
        jfc.setCurrentDirectory(propertyFile);
        
        if (jfc.showOpenDialog(this) != 0) {
            // Cancel oder Dialog ohne Auswahl geschlossen
            return;
        }
        
        // File holen
        propertyFile = jfc.getSelectedFile();
        
        if (propertyFile == null) {
            // Kein gueltiges File selektiert
            return;
        }
        
        // Textfelder updaten
        propertyNameField.setText(propertyFile.getName());
        propertyParentField.setText(propertyFile.getParent());
    }//GEN-LAST:event_selectPropertyBtnActionPerformed
    
    
    /**
     * Action method of the button to select source files or a source directory.
     * @param evt the event.
     */
    private void selectSourceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectSourceBtnActionPerformed
        if (selectDirectoryRadioBtn.isSelected()) {
            selectDirectoryOnly();
        } else {
            selectFilesOnly();
        }
    }//GEN-LAST:event_selectSourceBtnActionPerformed
    
    
    /**
     * Action method of the radio button to select directories only as a source.
     */
    private void selectDirectoryOnly() {
        JFileChooser    jfc;
        
        // Klasse fuer den Dialog generieren
        jfc = new JFileChooser();
        
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        // Bestehenden Namen als Default eintragen
        jfc.setCurrentDirectory(sourceFile);
        
        if (jfc.showOpenDialog(this) != 0) {
            // Cancel oder Dialog ohne Auswahl geschlossen
            return;
        }
        
        // Directory holen
        sourceFile = jfc.getSelectedFile();
        
        if (sourceFile == null) {
            // Kein gueltiges Directory selektiert
            return;
        }
        
        // Vector der Files erstellen
        sourceNameToVector(sourceFile);
        
        // Neuen Path in Textfeld darstellen
        sourceNameField.setText("");
        sourceParentField.setText(sourceFile.toString());
    }
    
    
    /**
     * Action method of the radio button to select files only as source.
     */
    private void selectFilesOnly() {
        JFileChooser    jfc;
        File[]          fArray;
        
        // Klasse fuer den Dialog generieren
        jfc = new JFileChooser();
        
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Mehrere Files selektieren erlauben
        jfc.setMultiSelectionEnabled(true);
        
        // Bestehenden Namen als Default eintragen
        jfc.setCurrentDirectory(sourceFile);
        
        if (jfc.showOpenDialog(this) != 0) {
            // Cancel oder Dialog ohne Auswahl geschlossen
            return;
        }
        
        // File holen
        fArray = jfc.getSelectedFiles();
        
        if (fArray == null) {
            // Keine gueltigen Files selektiert
            return;
        }
        
        if (fArray.length == 0) {
            // Keine gueltigen Files selektiert
            return;
        }
        
        // Array in Vector kopieren
        fVec = new Vector();
        for (int i = 0; i < fArray.length; i++) {
            fVec.add(fArray[i]);
        }
        
        // Anzahl der Files darstellen
        numberOfFilesLabel.setText(String.valueOf(fVec.size()));
        
        // Ersten File in Textfelder darstellen
        sourceFile = fArray[0];
        sourceNameField.setText(sourceFile.getName());
        sourceParentField.setText(sourceFile.getParent());
    }
    
    
    /**
     * Action method of the exit menu.
     * @param evt the event.
     */
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        exitApplication();
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    
    /**
     * Exit the GUI form.
     * @param evt the event.
     */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        exitApplication();
    }//GEN-LAST:event_exitForm
    
    
    /**
     * Exit the application.
     */
    private void exitApplication() {
        storeProperties();
        System.exit(0);
    }
    
    
    /**
     * The main method to start the application.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new DicomRouterGUI2().setVisible(true);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPanel fileSourcePanel;
    private javax.swing.JPanel fileTab;
    private javax.swing.ButtonGroup fileTypeGroup;
    private javax.swing.JPanel guiPanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JCheckBox includeSubdirectoriesCheckBox;
    private javax.swing.ButtonGroup loggerBtnGroup;
    private javax.swing.JRadioButton loggerDefaultBtn;
    private javax.swing.JTextField loggerNameField;
    private javax.swing.JPanel loggerPanel;
    private javax.swing.JTextField loggerParentField;
    private javax.swing.JRadioButton loggerSelectBtn;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTextArea messageArea;
    private javax.swing.JPanel messagePanel;
    private javax.swing.JLabel networkLabel;
    private javax.swing.JPanel networkPanel;
    private javax.swing.JPanel networkTab;
    private javax.swing.JLabel numberOfFilesLabel;
    private javax.swing.JLabel numberOfFilesText;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JPanel processPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel propertiesTab;
    private javax.swing.JTextField propertyNameField;
    private javax.swing.JPanel propertyPanel;
    private javax.swing.JTextField propertyParentField;
    private javax.swing.JPanel receiverTab;
    private javax.swing.JPanel runTab;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JScrollPane scrollMessageArea;
    private javax.swing.JRadioButton selectDirectoryRadioBtn;
    private javax.swing.JRadioButton selectFilesRadioBtn;
    private javax.swing.JButton selectLoggerBtn;
    private javax.swing.JButton selectPropertyBtn;
    private javax.swing.JButton selectSourceBtn;
    private javax.swing.ButtonGroup sourceBtnGroup;
    private javax.swing.JTextField sourceNameField;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JTextField sourceParentField;
    private javax.swing.JTabbedPane sourceTabbedPane;
    private javax.swing.JButton startBtn;
    private javax.swing.JButton stopBtn;
    private javax.swing.JTabbedPane taskTabbedPane;
    // End of variables declaration//GEN-END:variables
    
    
    /**
     * Inner member class for handling the output of System.out and System.err to the
     * JTextArea intead of standard out and err.
     */
    class FilteredStream extends FilterOutputStream {
        
        /** Maximum length of text in the text area for logging (= 65535). */
        private final int MAXIMUM_LENGTH_OF_TEXT = 65535;
        
        /**
         * The constructor. It calls its super class only.
         * @param os The OutputStream to process.
         */
        public FilteredStream(OutputStream os) {
            super(os);
        }
        
        
        /**
         * Writes b.length bytes from the specified byte array to this output stream.
         * The length of the text is limited to the MAXIMUM_LENGTH_OF_TEXT characters.
         * @param b the data.
         * @throws IOException if an I/O error occurs.
         */
        public void write(byte b[]) throws IOException {
            String s = new String(b);
            write(s);
        }
        
        
        /**
         * Writes len bytes from the specified byte array starting at offset off to this output
         * stream.
         * The length of the text is limited to the MAXIMUM_LENGTH_OF_TEXT characters.
         * @param b the data.
         * @param off the start offset in the data.
         * @param len the number of bytes to write.
         * @throws IOException if an I/O error occurs.
         */
        public void write(byte b[], int off, int len) throws IOException {
            String s = new String(b , off , len);
            write(s);
        }
        
        
        /**
         * Writes a String to this output stream.
         * The length of the text is limited to the MAXIMUM_LENGTH_OF_TEXT characters.
         * @param s the String to write.
         * @throws IOException if an I/O error occurs.
         */
        private void write(String s) throws IOException {
            String      loggingText;
            int         h;
            JViewport   vp;
            
            // Append this logging message at the end of the logging message but respect the maximal message length
            loggingText = messageArea.getText();
            loggingText += s;
            if (loggingText.length() > MAXIMUM_LENGTH_OF_TEXT) {
                loggingText = loggingText.substring(loggingText.length() - MAXIMUM_LENGTH_OF_TEXT);
            }
            messageArea.setText(loggingText);
            
            // MessageArea ans Ende scrollen
            h = messageArea.getHeight();
            vp= scrollMessageArea.getViewport();
            vp.scrollRectToVisible(new Rectangle(0, h+1, 1, 1));
        }
    }
    
}
