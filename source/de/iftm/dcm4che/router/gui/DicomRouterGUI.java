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
 * @version 2004.01.27
 */
public class DicomRouterGUI extends javax.swing.JFrame implements DicomRouterEventListener {
    
    /** DOCUMENT ME! */
    private static Logger log = Logger.getLogger(DicomRouterGUI.class);
    
    /** URI of the property file of the application RouterGUI */
    private final String    routerGUIPropertiesURI = "./cfg/routerGUI.properties";
    
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
    public DicomRouterGUI() {
        
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
        
        // Hauptframe
        props.setProperty("frameWidth", String.valueOf(this.getSize().width));
        props.setProperty("frameHeight", String.valueOf(this.getSize().height));
        // Die Methode splitPane#getLastDividerLocation() liefert nicht immer
        // die korrekte Position zurueck. Wenn der Anwender den Devider nicht
        // bewegt hat wird der Wert -1 zurueck gegeben
        int div = splitPane.getDividerSize();
        int left = leftPanel.getSize().width;
        props.setProperty("dividerLocation", String.valueOf(left + div / 2));
        
        // Tabbed Pane
        if (sourceTabbedPane.getSelectedComponent().equals(fileTab)) {
            props.setProperty("sourceTabbedPane", "file");
        } else {
            props.setProperty("sourceTabbedPane", "network");
        }
        
        // Source: File
        if (selectFilesBtn.isSelected()) {
            props.setProperty("selectFilesBtn", "true");
            props.setProperty("selectDirectoryBtn", "false");
        } else {
            props.setProperty("selectFilesBtn", "false");
            props.setProperty("selectDirectoryBtn", "true");
        }
        
        if (includeSubdirectoriesBox.isSelected()) {
            props.setProperty("includeSubdirectoriesBox", "true");
        } else {
            props.setProperty("includeSubdirectoriesBox", "false");
        }
        
        props.setProperty("sourceNameField", sourceNameField.getText());
        
        // Property File
        props.setProperty("propertyNameField", propertyNameField.getText());
        
        // Logger File
        if (loggerDefaultBtn.isSelected()) {
            props.setProperty("loggerDefaultBtn", "true");
            props.setProperty("loggerSelectBtn", "false");
        } else {
            props.setProperty("loggerDefaultBtn", "false");
            props.setProperty("loggerSelectBtn", "true");
        }
        
        props.setProperty("loggerNameField", loggerNameField.getText());
        
        // Eigenschaften in Datei speichern
        try {
            File f = Util.uriToFile(routerGUIPropertiesURI);
            OutputStream out = new FileOutputStream(f);
            props.store(out, "");
        } catch (Exception e) {
            log.warn("Could not writie properties to file " + routerGUIPropertiesURI + ": " + e.getMessage());
        } finally {
        }
    }
    
    
    /**
     * Loads the GUI properties from the file ./cfg/DicomRouter.properties.
     */
    private void loadProperties() {
        Properties  props = new Properties();
        String      value;
        
        // Properties aus Datei laden
        try {
            File f = Util.uriToFile(routerGUIPropertiesURI);
            InputStream in = new FileInputStream(f);
            props.load(in);
        } catch (Exception e) {
            return;
        }
        
        // Hauptframe
        try {
            if ((value = props.getProperty("frameWidth")) != null) {
                int width = Integer.parseInt(value);
                if ((value = props.getProperty("frameHeight")) != null) {
                    int height = Integer.parseInt(value);
                    this.setSize(width, height);
                }
            }
            // Divider der JSplitPane setzen
            if ((value = props.getProperty("dividerLocation")) != null) {
                splitPane.setDividerLocation(Integer.parseInt(value));
            }
        } catch (Exception e) {}
        
        // Tabbed Pane
        if ((value = props.getProperty("sourceTabbedPane")) != null) {
            if (value.equals("file")) {
                sourceTabbedPane.setSelectedComponent(fileTab);
            } else {
                sourceTabbedPane.setSelectedComponent(networkTab);
            }
        }
        
        // Source: File
        if ((value = props.getProperty("selectDirectoryBtn")) != null) {
            selectDirectoryBtn.setSelected(value.equals("true"));
        }
        if ((value = props.getProperty("selectFilesBtn")) != null) {
            selectFilesBtn.setSelected(value.equals("true"));
        }
        
        if ((value = props.getProperty("includeSubdirectoriesBox")) != null) {
            includeSubdirectoriesBox.setSelected(value.equals("true"));
        }
        
        if ((value = props.getProperty("sourceNameField")) != null) {
            sourceNameField.setText(value);
            // Lokale Variablen updaten
            sourceNameToVector();
        }
        
        // Property File
        if ((value = props.getProperty("propertyNameField")) != null) {
            propertyNameField.setText(value);
            // Lokale Variable updaten
            propertyFile = new File(value);
        }
        
        // Logger
        if ((value = props.getProperty("loggerDefaultBtn")) != null) {
            loggerDefaultBtn.setSelected(value.equals("true"));
        }
        if ((value = props.getProperty("loggerSelectBtn")) != null) {
            loggerSelectBtn.setSelected(value.equals("true"));
        }
        if ((value = props.getProperty("loggerNameField")) != null) {
            loggerNameField.setText(value);
        }
        // Lokale Variable fuer Logging setzten
        if (loggerDefaultBtn.isSelected()) {
            loggerFile = null;
        } else {
            loggerFile = new File(loggerNameField.getText());
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
     */
    private void sourceNameToVector() {
        File    f;
        
        // Neuen Path in Textfeld darstellen
        f = new File( sourceNameField.getText());
        
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
                if (includeSubdirectoriesBox.isSelected()) {
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
        splitPane = new javax.swing.JSplitPane();
        leftPanel = new javax.swing.JPanel();
        sourcePanel = new javax.swing.JPanel();
        sourceTabbedPane = new javax.swing.JTabbedPane();
        fileTab = new javax.swing.JPanel();
        fileSourcePanel = new javax.swing.JPanel();
        selectSourceBtn = new javax.swing.JButton();
        sourceNameField = new javax.swing.JTextField();
        includeSubdirectoriesBox = new javax.swing.JCheckBox();
        selectFilesBtn = new javax.swing.JRadioButton();
        selectDirectoryBtn = new javax.swing.JRadioButton();
        textLabel1 = new javax.swing.JLabel();
        numberOfFilesLabel = new javax.swing.JLabel();
        networkTab = new javax.swing.JPanel();
        networkLabel = new javax.swing.JLabel();
        propertyPanel = new javax.swing.JPanel();
        selectPropertyBtn = new javax.swing.JButton();
        propertyNameField = new javax.swing.JTextField();
        loggerPanel = new javax.swing.JPanel();
        selectLoggerBtn = new javax.swing.JButton();
        loggerNameField = new javax.swing.JTextField();
        loggerDefaultBtn = new javax.swing.JRadioButton();
        loggerSelectBtn = new javax.swing.JRadioButton();
        rightPanel = new javax.swing.JPanel();
        processPanel = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        startBtn = new javax.swing.JButton();
        stopBtn = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();
        scrollMessageArea = new javax.swing.JScrollPane();
        messageArea = new javax.swing.JTextArea();
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

        guiPanel.setLayout(new java.awt.BorderLayout());

        guiPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        guiPanel.setMinimumSize(new java.awt.Dimension(780, 520));
        guiPanel.setPreferredSize(new java.awt.Dimension(780, 520));
        leftPanel.setLayout(new java.awt.GridBagLayout());

        leftPanel.setMinimumSize(new java.awt.Dimension(32, 256));
        leftPanel.setPreferredSize(new java.awt.Dimension(32, 256));
        sourcePanel.setLayout(new java.awt.GridBagLayout());

        sourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Source", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12)));
        sourcePanel.setMinimumSize(new java.awt.Dimension(256, 128));
        sourcePanel.setPreferredSize(new java.awt.Dimension(256, 128));
        fileTab.setLayout(new java.awt.BorderLayout());

        fileSourcePanel.setLayout(new java.awt.GridBagLayout());

        fileSourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Source"));
        selectSourceBtn.setText("Select...");
        selectSourceBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSourceBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        fileSourcePanel.add(selectSourceBtn, gridBagConstraints);

        sourceNameField.setEditable(false);
        sourceNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceNameFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 5.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 10);
        fileSourcePanel.add(sourceNameField, gridBagConstraints);

        includeSubdirectoriesBox.setText("Include subdirectories");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        fileSourcePanel.add(includeSubdirectoriesBox, gridBagConstraints);

        sourceBtnGroup.add(selectFilesBtn);
        selectFilesBtn.setSelected(true);
        selectFilesBtn.setText("Select files");
        selectFilesBtn.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                selectFilesBtnStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        fileSourcePanel.add(selectFilesBtn, gridBagConstraints);

        sourceBtnGroup.add(selectDirectoryBtn);
        selectDirectoryBtn.setText("Select directory");
        selectDirectoryBtn.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                selectDirectoryBtnStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        fileSourcePanel.add(selectDirectoryBtn, gridBagConstraints);

        textLabel1.setFont(new java.awt.Font("Dialog", 0, 12));
        textLabel1.setText("Number of files to process:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        fileSourcePanel.add(textLabel1, gridBagConstraints);

        numberOfFilesLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        numberOfFilesLabel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        fileSourcePanel.add(numberOfFilesLabel, gridBagConstraints);

        fileTab.add(fileSourcePanel, java.awt.BorderLayout.CENTER);

        sourceTabbedPane.addTab("File", fileTab);

        networkTab.setLayout(new java.awt.GridBagLayout());

        networkLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        networkLabel.setText("All configuration is done in the property file.");
        networkTab.add(networkLabel, new java.awt.GridBagConstraints());

        sourceTabbedPane.addTab("Network", networkTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        sourcePanel.add(sourceTabbedPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 5.0;
        leftPanel.add(sourcePanel, gridBagConstraints);

        propertyPanel.setLayout(new java.awt.GridBagLayout());

        propertyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Property file", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12)));
        propertyPanel.setMinimumSize(new java.awt.Dimension(256, 128));
        propertyPanel.setPreferredSize(new java.awt.Dimension(256, 128));
        selectPropertyBtn.setText("Select...");
        selectPropertyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPropertyBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        propertyPanel.add(selectPropertyBtn, gridBagConstraints);

        propertyNameField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        propertyPanel.add(propertyNameField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        leftPanel.add(propertyPanel, gridBagConstraints);

        loggerPanel.setLayout(new java.awt.GridBagLayout());

        loggerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Logger", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12)));
        selectLoggerBtn.setText("Select...");
        selectLoggerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectLoggerBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        loggerPanel.add(selectLoggerBtn, gridBagConstraints);

        loggerNameField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        loggerPanel.add(loggerNameField, gridBagConstraints);

        loggerBtnGroup.add(loggerDefaultBtn);
        loggerDefaultBtn.setSelected(true);
        loggerDefaultBtn.setText("Use default logger");
        loggerDefaultBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggerDefaultBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        loggerPanel.add(loggerSelectBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        leftPanel.add(loggerPanel, gridBagConstraints);

        splitPane.setLeftComponent(leftPanel);

        rightPanel.setLayout(new java.awt.GridBagLayout());

        rightPanel.setMinimumSize(new java.awt.Dimension(32, 256));
        rightPanel.setPreferredSize(new java.awt.Dimension(32, 256));
        processPanel.setLayout(new java.awt.GridBagLayout());

        processPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Process", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12)));
        processPanel.setMinimumSize(new java.awt.Dimension(384, 256));
        processPanel.setPreferredSize(new java.awt.Dimension(384, 256));
        progressBar.setMinimumSize(new java.awt.Dimension(10, 20));
        progressBar.setPreferredSize(new java.awt.Dimension(148, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 10);
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
        stopBtn.setAutoscrolls(true);
        stopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        processPanel.add(stopBtn, gridBagConstraints);

        messageLabel.setText("Messages");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        processPanel.add(messageLabel, gridBagConstraints);

        messageArea.setEditable(false);
        scrollMessageArea.setViewportView(messageArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        processPanel.add(scrollMessageArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        rightPanel.add(processPanel, gridBagConstraints);

        splitPane.setRightComponent(rightPanel);

        guiPanel.add(splitPane, java.awt.BorderLayout.CENTER);

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
    
    private void sourceNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceNameFieldActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_sourceNameFieldActionPerformed
    
    
    /**
     * Action method of the radio button to select a logger file.
     * @param evt the event.
     */
    private void loggerSelectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggerSelectBtnActionPerformed
        loggerFile = new File(loggerNameField.getText());
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
        jfc.setCurrentDirectory(new File(loggerNameField.getText()));
        
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
        
        // Neuen Path in Textfeld darstellen
        loggerNameField.setText(loggerFile.toString());
    }//GEN-LAST:event_selectLoggerBtnActionPerformed
    
    
    /**
     * Action method of the radio button to select a source directory.
     * @param evt the event.
     */
    private void selectDirectoryBtnStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_selectDirectoryBtnStateChanged
        sourceNameField.setText("");
        numberOfFilesLabel.setText("0");
    }//GEN-LAST:event_selectDirectoryBtnStateChanged
    
    
    /**
     * Action method of the radio button to select source files.
     * @param evt the event.
     */
    private void selectFilesBtnStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_selectFilesBtnStateChanged
        sourceNameField.setText("");
        numberOfFilesLabel.setText("0");
    }//GEN-LAST:event_selectFilesBtnStateChanged
    
    
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
        jfc.setCurrentDirectory(new File(propertyNameField.getText()));
        
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
        
        // Neuen Path in Textfeld darstellen
        propertyNameField.setText(propertyFile.getPath());
        
    }//GEN-LAST:event_selectPropertyBtnActionPerformed
    
    
    /**
     * Action method of the button to select source files or a source directory.
     * @param evt the event.
     */
    private void selectSourceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectSourceBtnActionPerformed
        if (selectDirectoryBtn.isSelected()) {
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
        File            dir;
        
        // Klasse fuer den Dialog generieren
        jfc = new JFileChooser();
        
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        // Bestehenden Namen als Default eintragen
        jfc.setCurrentDirectory(new File(sourceNameField.getText()));
        
        if (jfc.showOpenDialog(this) != 0) {
            // Cancel oder Dialog ohne Auswahl geschlossen
            return;
        }
        
        // Directory holen
        dir = jfc.getSelectedFile();
        
        if (dir == null) {
            // Kein gueltiges Directory selektiert
            return;
        }
        
        // Neuen Path in Textfeld darstellen
        sourceNameField.setText(dir.getPath());
        
        // Vector der Files erstellen
        sourceNameToVector();
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
        jfc.setCurrentDirectory(new File(sourceNameField.getText()));
        
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
        
        // Path von erstem File in Textfeld darstellen
        sourceNameField.setText(fArray[0].getPath());
        
        // Array in Vector kopieren
        fVec = new Vector();
        for (int i = 0; i < fArray.length; i++) {
            fVec.add(fArray[i]);
        }
        
        // Anzahl der Files darstellen
        numberOfFilesLabel.setText(String.valueOf(fVec.size()));
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
        new DicomRouterGUI().setVisible(true);
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
    private javax.swing.JCheckBox includeSubdirectoriesBox;
    private javax.swing.JPanel leftPanel;
    private javax.swing.ButtonGroup loggerBtnGroup;
    private javax.swing.JRadioButton loggerDefaultBtn;
    private javax.swing.JTextField loggerNameField;
    private javax.swing.JPanel loggerPanel;
    private javax.swing.JRadioButton loggerSelectBtn;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTextArea messageArea;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel networkLabel;
    private javax.swing.JPanel networkTab;
    private javax.swing.JLabel numberOfFilesLabel;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JPanel processPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextField propertyNameField;
    private javax.swing.JPanel propertyPanel;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JScrollPane scrollMessageArea;
    private javax.swing.JRadioButton selectDirectoryBtn;
    private javax.swing.JRadioButton selectFilesBtn;
    private javax.swing.JButton selectLoggerBtn;
    private javax.swing.JButton selectPropertyBtn;
    private javax.swing.JButton selectSourceBtn;
    private javax.swing.ButtonGroup sourceBtnGroup;
    private javax.swing.JTextField sourceNameField;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JTabbedPane sourceTabbedPane;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JButton startBtn;
    private javax.swing.JButton stopBtn;
    private javax.swing.JLabel textLabel1;
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
