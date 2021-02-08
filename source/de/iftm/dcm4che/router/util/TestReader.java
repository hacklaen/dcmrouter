/*
 * TestReader.java
 *
 * Created on 28. April 2006, 08:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.iftm.dcm4che.router.util;

import de.iftm.dcm4che.router.property.*;
import de.iftm.dcm4che.router.plugin.DicomRouterPlugin;
import de.iftm.dcm4che.router.plugin.PluginChain;
import de.iftm.dcm4che.router.util.*;

import gnu.getopt.Getopt;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;
import org.dcm4che.util.*;
import org.dcm4che.imageio.plugins.*;

import java.awt.image.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;


/**
 *
 * @author thacklaender
 */
public class TestReader {
    /** The logger for this plugin */
    Logger log = Logger.getLogger(TestReader.class);
    
    /** Creates a new instance of TestReader */
    public TestReader() {
    }
    
    
    /**
     * The main method to start the application.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new TestReader().run2();
    }
    
    
    public void run() {
        Dataset defaultMetadataDataset;
        IIOMetadata         dummyMetadata = null;
        BufferedImage       theImage = null;
        Iterator            imageWriters;
        File                f = null;
        String              fileName;
        ImageOutputStream   imageOutputStream = null;
        ImageWriter         dcmImageWriter = null;
        
        
        theImage = null;
        try {
            // Read from a file
            File file = new File("./samples/060314_286.png");
            theImage = ImageIO.read(file);
            
        } catch (IOException e) {
            System.out.println("Can't read file");
        }
        
        // DcmImageWriter holen
        imageWriters = ImageIO.getImageWritersByFormatName("DICOM");
        dcmImageWriter = (ImageWriter) imageWriters.next();
        if (dcmImageWriter == null) {
            throw new UnsupportedOperationException("No DcmImageWriter found.");
        }
        if (! (dcmImageWriter.getDefaultStreamMetadata(null) instanceof DcmMetadata)) {
            throw new UnsupportedOperationException("No DcmImageWriter found.");
        }
        
        // Filename festlegen
        fileName = "tmp/test.dcm";
        
        // Bestehenden File erst loeschen, dann neuen erstellen
        f = new File(System.getProperty("user.dir"), fileName);
        
        // Neuen ImageOutputStream fuer File oeffnen
        try {
            imageOutputStream = ImageIO.createImageOutputStream(f);
        } catch (IOException e) {
            System.out.println("Can't create file");
        }
        
        // Den OutputStream des DcmImageWriters setzen
        dcmImageWriter.setOutput(imageOutputStream);
        
        
        defaultMetadataDataset = getDefaultMetadata();
        
        // Dataset in DcmMetadata speichern
        dummyMetadata = dcmImageWriter.getDefaultStreamMetadata(null);
        ((DcmMetadata) dummyMetadata).setDataset(defaultMetadataDataset);
        
        try {
            dcmImageWriter.write(dummyMetadata, new IIOImage(theImage, null, null), null);
        } catch (Exception e) {
            System.out.println(e);
        }
        
        // Stream schliessen
        try {
            imageOutputStream.close();
        } catch (IOException e) {
            System.out.println("Can't create file");
        }
        
    }
    
    
    public void run2() {
        Dataset defaultMetadataDataset;
        IIOMetadata         dummyMetadata = null;
        BufferedImage       theImage = null;
        BufferedImage       newImage = null;
        Iterator            imageWriters;
        File                f = null;
        String              fileName;
        ImageOutputStream   imageOutputStream = null;
        ImageWriter         dcmImageWriter = null;
        Dataset              ds = null;
        ImageInputStream         iis;
        ByteArrayOutputStream       bos = null;
        ByteArrayInputStream       bis = null;
        
        
        theImage = null;
        try {
            // Read from a file
            // File file = new File("./samples/SMPTE.png");
            File file = new File("./samples/060314_286.png");
            theImage = ImageIO.read(file);
            
        } catch (IOException e) {
            System.out.println("Can't read file");
            return;
        }
        
        // newImage = new BufferedImage(theImage.getWidth(), theImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        newImage = new BufferedImage(theImage.getWidth(), theImage.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        // newImage = new BufferedImage(theImage.getWidth(), theImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        // newImage = new BufferedImage(theImage.getWidth(), theImage.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
        
        Graphics2D g = newImage.createGraphics();
        g.drawImage(theImage, null, null);
        
        
        ds = DcmObjectFactory.getInstance().newDataset();
        ds.putBufferedImage(newImage);
        
        ds.putAll(getDefaultMetadata());
        
        // Filename festlegen
        fileName = "./tmp/test2.dcm";
        f = new File(fileName);
        saveDataset(ds, f);
    }
    
    
    /**
     * Saves the Dataset to the local filesystem.
     *
     * @param ds the Dataset to save.
     * @param f the File to which the Dataset should be save.
     */
    public boolean saveDataset(Dataset ds, File f) {
        FileOutputStream fos = null;
        DcmEncodeParam param = null;
        FileMetaInfo fmi;
        /** Transfersyntax of the saved file. One of the strings  ImplicitVRLittleEndian,
         * ExplicitVRLittleEndian, ExplicitVRBigEndian. The string may start with
         * the praefix-character '$' */
        String transfersyntax = "ImplicitVRLittleEndian";
        /** Include a File Meta Information block to the saved file */
        boolean write_fmi = true;
        
        try {
            if (f.exists()) {
                // File besteht bereits. Erst loeschen, dann neu erstellen
                f.delete();
                f.createNewFile();
            } else {
                if (!f.getParentFile().exists()) {
                    // Directory Pfad existiert noch nicht
                    f.getParentFile().mkdirs();
                }
            }
            
            fos = new FileOutputStream(f);
            param = DcmEncodeParam.valueOf(UIDs.forName(transfersyntax));
            
            // File mit/ohne File Meta Information Block schreiben
            if (write_fmi) {
                fmi = DcmObjectFactory.getInstance().newFileMetaInfo(ds, UIDs.forName(transfersyntax));
                ds.setFileMetaInfo(fmi);
                
                // Die Methode Dataset#writeFile schreibt -sofern != null- einen
                // File Meta Information Block und ruft dann Dataset#writeDataset
                // mit den selben Parametern auf.
                ds.writeFile(fos, param);
                
                // File Meta Information Block wieder loeschen
                ds.setFileMetaInfo(null);
            } else {
                ds.writeDataset(fos, param);
            }
            
            return true;
        } catch (Exception e) {
            log.error("Can't save file - Exception: " + e.getMessage());
            
            return false;
        } finally {
            try {
                fos.close();
            } catch (Exception ignore) {}
        }
    }
    
    
    /**
     * Creates the default metadata for a Secondary Capture image.
     * @return the Dataset.
     */
    public static Dataset getDefaultMetadata() {
        Dataset             ds = DcmObjectFactory.getInstance().newDataset();
        UIDGenerator        uidGen = UIDGenerator.getInstance();
        Date                now = new Date();
        SimpleDateFormat    dateFormatter = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat    timeFormatter = new SimpleDateFormat("HHmmss.SSS");
        
        // Character Set für Westeuropa ISO-8859-1 festlegen
        // Anderenfalls wuerden deutsche Umlaute zu einer IllegalArgumentException
        // beim Befuellen des Dataset fuehren
        ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
        
        // Secondary Capture Image IOD: PS 3.3 - A.8
        
        // Unique Identifiers (UIDs) PS 3.5 - 9
        // Each UID is composed of two parts: UID = <org root>.<suffix>
        
        // Data Element Type:PS 3.5 -7.4
        // Type 1: Requiered data elements. Must be present. Length may not be zero
        // Type 2: Requiered data elements. Must be present. Length may be zero
        // Type 3: Optional data elements. Length may/may not be zero.
        
        // Patient IE, Patient Module, PS 3.3 - C.7.1.1, M
        ds.putPN(Tags.PatientName, "PatientName");                          // Type 2
        ds.putLO(Tags.PatientID, "PatientID");                              // Type 2
        ds.putDA(Tags.PatientBirthDate, "19501031");                        // Type 2
        ds.putCS(Tags.PatientSex, "O");                                     // Type 2
        
        // Study IE, General Study Module, PS 3.3 - C.7.2.1, M
        ds.putUI(Tags.StudyInstanceUID, uidGen.createUID());
        ds.putDA(Tags.StudyDate, dateFormatter.format(now));                // Type 2
        ds.putTM(Tags.StudyTime, timeFormatter.format(now));                // Type 2
        ds.putPN(Tags.ReferringPhysicianName, "ReferringPhysicianName");    // Type 2
        ds.putSH(Tags.StudyID, "1");                                        // Type 2
        ds.putSH(Tags.AccessionNumber, "0");                                // Type 2
        
        // Study IE, Patient Study Module, PS 3.3 - C.7.2.2, U
        
        // Series IE, General Series Module, PS 3.3 - C.7.3.1, M
        ds.putCS(Tags.Modality, "OT");
        ds.putUI(Tags.SeriesInstanceUID, uidGen.createUID());
        ds.putIS(Tags.SeriesNumber, "1");                                   // Type 2
        ds.putCS(Tags.Laterality, "");                                      // Type 2C; only if ImageLaterality not sent; enumerativ L or R
        
        // Equipment IE, General Equipment Module, PS 3.3 - C.7.5.1, U
        
        // Equipment IE, SC Equipment Module, PS 3.3 - C.8.6.1, M
        ds.putCS(Tags.ConversionType, "WSD");                               // Type 1
        ds.putCS(Tags.Modality, "OT");                                      // Type 3; enumerativ see C7.3.1.1.1
        ds.putLO(Tags.SecondaryCaptureDeviceID, "");                        // Type 3
        ds.putLO(Tags.SecondaryCaptureDeviceManufacturer, "dcmrouter");     // Type 3
        ds.putLO(Tags.SecondaryCaptureDeviceManufacturerModelName, "");     // Type 3
        ds.putLO(Tags.SecondaryCaptureDeviceSoftwareVersion, Util.VERSION); // Type 3
        ds.putSH(Tags.VideoImageFormatAcquired, "");                        // Type 3
        ds.putLO(Tags.DigitalImageFormatAcquired, "");                      // Type 3
        
        // Image IE, General Image Module, PS 3.3 - C.7.6.1, M
        ds.putIS(Tags.InstanceNumber, "1");                                 // Type 2
        String[] po = {"", ""};
        ds.putCS(Tags.PatientOrientation, po);                              // Type 2C; see PS 3.3 - C.7.6.1.1.1
        ds.putDA(Tags.ContentDate, dateFormatter.format(now));              // Type 2C; if image is part of a series. Was Image Date
        ds.putTM(Tags.ContentTime, timeFormatter.format(now));              // Type 2C; if image is part of a series. Was Image Time
        
        // Image IE, Image Pixel Module, PS 3.3 - C.7.6.3, M
        // must be set elsewhere
        
        // Image IE, SC Image Module, PS 3.3 - C.8.6.2, M
        ds.putDA(Tags.DateOfSecondaryCapture, dateFormatter.format(now));   // Type 3
        ds.putTM(Tags.TimeOfSecondaryCapture, timeFormatter.format(now));   // Type 3
        
        // Image IE, Overlay Plane Module, PS 3.3 - C.9.2, U
        
        // Image IE, Modality LUT Module, PS 3.3 - C.11.1, U
        // ds.putDS(Tags.RescaleIntercept);                                    // Type 1C; ModalityLUTSequence is not present
        // ds.putDS(Tags.RescaleSlope);                                        // Type 1C; ModalityLUTSequence is not present
        // ds.putLO(Tags.RescaleType);                                         // Type 1C; ModalityLUTSequence is not present; arbitrary text
        
        // Image IE, VOI LUT Module, PS 3.3 - C.11.2, U
        // ds.putDS(Tags.WindowCenter);                                        // Type 3
        // ds.putDS(Tags.WindowWidth);                                         // Type 1C; WindowCenter is present
        // ds.putLO(Tags.WindowCenterWidthExplanation);                        // Type 3; arbitrary text
        
        // Image IE, SOP Common Module, PS 3.3 - C.12.1, M
        ds.putUI(Tags.SOPClassUID, UIDs.SecondaryCaptureImageStorage);      // Type 1
        ds.putUI(Tags.SOPInstanceUID, uidGen.createUID());                  // Type 1
        
        return ds;
    }
    
}
