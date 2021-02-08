/*
 *  ExportImagePlugin.java
 *
 *  Copyright (c) 2003 by
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
package de.iftm.dcm4che.router.plugin;

import de.iftm.dcm4che.router.property.*;

import de.iftm.dcm4che.router.util.*;

import org.apache.log4j.*;

import org.dcm4che.data.*;

import org.dcm4che.dict.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import java.io.*;

import java.net.*;

import java.text.*;

import java.util.*;

import javax.imageio.*;


/**
 * This plugins exports the given Dataset as a standard graphic image. It supports
 * the properties which start with the key SaveFilesystemPlugin. For description
 * of the subkeys (= properties) see the user manual.<br>
 *
 * @author Thomas Hacklaender
 * @version 2006.04.24
 */
public class ExportImagePlugin extends DicomRouterPlugin {
    /** The version string */
    final String VERSION = "2006.11.10";
    
    /** The logger for this plugin. */
    Logger log = Logger.getLogger(ExportImagePlugin.class);
    
    /** The BufferedImage of the Dataset to export. */
    BufferedImage image = null;
    
    /** The Graphic2D of image.*/
    private Graphics2D imageG2D = null;
    
    /** The Annotation of image.*/
    private Annotation annotation = null;
    
    /** True, if the size of the image to export should be the original image
     * size of the Dataset. */
    boolean originalSize = false;
    
    /** The properties of this plugin */
    Properties props = null;
    
    
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>  Local fields defined by properties: >>>>>>>
    
    
    /** Type of image annotation. */
    int annotation_type = Annotation.ANNOTATION_FULL;
    
    /** Directory to export the Dataset into. */
    File directory = Util.uriToFile("./");
    
    /** The color of the font for image annotation. */
    Color font_color = Color.orange;
    
    /**
     * The name of the font for image annotation.
     */
    String font_name = "dialog";
    
    /** The font size for image annotation. */
    int font_size = 14;
    
    /**
     * ImageIO name of the graphic format.
     */
    String image_format = "png";
    
    /** The size of the (square) image to export. */
    int image_size = 512;
    
    /** The margin for image annotations in pixel. */
    int margin_size = 4;
    
    /**
     * Name of file to save. If the String starts with the character '$' the filename
     * is set from the DICOM element named in the remaining part of the string.
     */
    String filename = "DicomRouterImage";
    
    /**
     * A string to append after the name and before the file extension.
     */
    String name_postfix = "";
    
    /** If true, write files in the subdirectory "$PatientName_$PatientBirthDate/<directory>/" */
    boolean separate_patients = false;
    
    /** Write files in the subdirectory "[$PatientName_$PatientBirthDate/]<directory>/<use_subdirectory>/". */
    String use_subdirectory = "";
    
    /**
     * If true:
     * Construct the the file ID of the file to save from the following file ID
     * components:<br>
     * 1. First letter of family name followed by first letter of given name
     * followed by date of birth, 6 character. Example: HT570522<br>
     * 2. Study date, 6 character. Example: 043012<br>
     * 3. Modality, 2 character followed by study time, 4 character. Example: MR1531<br>
     * 4. Study ID. Example: 4711<br>
     * 5. Series number. Example: 3<br>
     * 6. Instance number. Example: 54<br>
     * If false:<br>
     * Construct the the name of the file to save from directory, filename
     * name_postfix and the image_format as extension.
     */
    boolean write_dir_tree = true;
    
    
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    
    
    /**
     * Constructor.
     */
    public ExportImagePlugin() {
        // Nichts zu tun.
    }
    
    /**
     * Inits the DicomRouterPlugin with the specified Properties.
     * @param p  Properties containing the configuration of the plugin
     */
    public void init(Properties p) {
        String s;
        File newDirectory;
        
        // Properties lokal speichern
        props = p;
        
        if (props == null) {
            // nichts zu tun
            return;
        }
        
        // Bildannotationen
        s = props.getProperty("annotation-type");
        
        if (s != null) {
            if (s.equals("off")) {
                annotation_type = Annotation.ANNOTATION_OFF;
            } else if (s.equals("full")) {
                annotation_type = Annotation.ANNOTATION_FULL;
            } else if (s.equals("minimal")) {
                annotation_type = Annotation.ANNOTATION_MINIMAL;
            } else if (s.equals("anonymous")) {
                annotation_type = Annotation.ANNOTATION_ANONYMOUS;
            } else if (s.equals("pseudonym")) {
                annotation_type = Annotation.ANNOTATION_PSEUDONYM;
            } else {
                log.warn(
                        "Format error in Property \"annotation\". Annotation type set to \"full\"");
                annotation_type = Annotation.ANNOTATION_FULL;
            }
        }
        
        // Directory
        s = props.getProperty("directory");
        
        if (s != null) {
            if ((newDirectory = Util.uriToFile(s)) != null) {
                directory = newDirectory;
            }
        }
        
        // Zeichensatz Farbe
        s = props.getProperty("font-color");
        
        if (s != null) {
            if (s.equals("darkGray")) {
                font_color = Color.darkGray;
            } else if (s.equals("lightGray")) {
                font_color = Color.lightGray;
            } else if (s.equals("white")) {
                font_color = Color.white;
            } else if (s.equals("yellow")) {
                font_color = Color.yellow;
            } else if (s.equals("orange")) {
                font_color = Color.orange;
            } else if (s.equals("green")) {
                font_color = Color.green;
            } else {
                log.warn(
                        "Format error in Property \"font_color\". Color size set to Color.orange");
                font_color = Color.orange;
            }
        }
        
        // Zeichensatz Name
        s = props.getProperty("font-name");
        
        if (s != null) {
            if (s.equals("serif")) {
                font_name = "Serif";
            } else if (s.equals("sansserif")) {
                font_name = "SansSerif";
            } else if (s.equals("monospaced")) {
                font_name = "Monospaced";
            } else if (s.equals("dialog")) {
                font_name = "Dialog";
            } else {
                log.warn(
                        "Format error in Property \"font_name\". Font name set to \"dialog\"");
                font_name = "Dialog";
            }
        }
        
        // Zeichensatz Groesse
        s = props.getProperty("font-size");
        
        if (s != null) {
            // Value ist eine ganze Zahl
            try {
                font_size = (new Integer(s)).intValue();
            } catch (NumberFormatException e) {
                log.warn(
                        "Format error in Property \"font_size\". Font size set to 12");
                font_size = 12;
            }
        }
        
        // Graphik Format
        s = props.getProperty("image-format");
        
        if (s != null) {
            image_format = s;
        }
        
        // Bildgroesse entspechend der Property aendern
        s = props.getProperty("image-size");
        
        if (s != null) {
            if (s.equals("original")) {
                originalSize = true;
            } else {
                // Value ist eine ganze Zahl
                originalSize = false;
                
                try {
                    image_size = (new Integer(s)).intValue();
                } catch (NumberFormatException e) {
                    log.warn(
                            "Format error in Property \"image_size\". Image size set to 512*512");
                    image_size = 512;
                }
            }
        }
        
        // Rahmen Breite
        s = props.getProperty("margin-size");
        
        if (s != null) {
            // Value ist eine ganze Zahl
            try {
                margin_size = (new Integer(s)).intValue();
            } catch (NumberFormatException e) {
                log.warn(
                        "Format error in Property \"frame_size\". Margin size set to 4");
                margin_size = 4;
            }
        }
        
        // File Name
        // Wird in setDynamicProperties gesetzt, da der Wert vom aktuellen
        // Datset abhaengig ist.
        
        // File Postfix
        s = props.getProperty("name-postfix");
        
        if (s != null) {
            name_postfix = s;
        }
        
        // Separate_patients-Option nach Properties aendern
        s = props.getProperty("separate-patients");
        
        if (s != null) {
            if (s.toLowerCase().charAt(0) == 't') {
                separate_patients = true;
            } else {
                separate_patients = false;
            }
        }
        
        // Use_subdirectory-Option nach Properties aendern
        s = props.getProperty("use-subdirectory");
        
        if (s != null) {
            use_subdirectory = s;
        }
        
        // Tree-Option entspechend der Property aendern
        s = props.getProperty("write-dir-tree");
        
        if (s != null) {
            if (s.toLowerCase().charAt(0) == 't') {
                write_dir_tree = true;
            } else {
                write_dir_tree = false;
            }
        }
    }
    
    /**
     * Sets dynamic properties, which depend on the actual Dataset to process.
     * @param ds  The Dataset to process.
     * @return True, if dynamic property could be set.
     */
    protected boolean setDynamicProperties(Dataset ds) {
        String s;
        
        if (props == null) {
            // nichts zu tun
            return true;
        }
        
        // File Name
        s = props.getProperty("filename");
        
        if (s != null) {
            if (s.charAt(0) == '$') {
                // Name ist ein DICOM Element
                try {
                    filename = ds.getString(Tags.forName(s.substring(1)));
                } catch (Exception e) {
                    log.error("Can't find Element" + s.substring(1) +
                            " in Dataset.");
                    
                    return false;
                }
            } else {
                // Name ist direkt angegeben
                filename = s;
            }
        }
        
        // Kein Fehler aufgetreten
        return true;
    }
    
    /**
     * Returns a version string.
     *
     * @return The version string
     */
    public String getVersion() {
        return VERSION;
    }

    
    /**
     * Closes the Plugin.
     */
    public void close() {
        // Nichts zu tun.
    }
    
    /**
     * Contains the proccesing of this plugin. This implementation handels all
     * exeptions inside the method and sends logging information about the exeption.
     * 
     * 
     * 
     * @param dataset The Dataset to process.
     * @return If succesfull CONTINUE, REQUEST_ABORT_RECEIVER otherwise
     */
    public int process(Dataset dataset) {
        File    parent = null;
        File    imageFile;
        
        // Nur dann weitermachen, wenn ein Datset vorhanden ist
        if (dataset == null) {
            log.warn("No Dataset given.");
            
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Dynamische Properties setzen
        if (!setDynamicProperties(dataset)) {
            log.error("Image not exported.");
            
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Das Working Directory festlegen
        parent = directory;
        if (separate_patients) {
            parent = Util.addPatientDirectory(parent, dataset);
        }
        
        // Image aus Dataset erzeugen
        dataset2Image(dataset);
        
        // Annotationen schreiben
        annotation = new Annotation(image, dataset);
        annotation.setType(annotation_type);
        annotation.setFontColor(font_color);
        annotation.setFontName(font_name);
        annotation.setFontSize(font_size);
        annotation.setMarginSize(margin_size);
        annotation.draw();
        
        // Ein Fileobjekt erstellen
        if ((imageFile = createFilePathToSave(parent, dataset)) == null) {
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Image File exportieren
        if (exportImage(imageFile)) {
            return CONTINUE;
        } else {
            return REQUEST_ABORT_RECEIVER;
        }
    }
    
    /**
     * Create the file path of the file to save in the directory parent
     * depending of the property use_subdirectory.
     * @param parent the working directory.
     * @param ds the dataset to analyse.
     * @return the file.
     */
    protected File createFilePathToSave(File parent, Dataset ds) {
        File path;
        
        if (use_subdirectory != null) {
            if (! use_subdirectory.equals("")) {
                parent = new File(parent, use_subdirectory);
            }
        }
        
        if (write_dir_tree) {
            path = new File(parent, Util.datasetToTreeFileID(ds) + "." + image_format);
        } else {
            // Direkt in das angegebene Verzeichnis speichern
            path = new File(parent, filename + name_postfix + "." + image_format);
        }
        
        return path;
    }
    
    /**
     * Creates a BufferedImage and Graphics2D for the given Dataset and stores
     * them in local fields.
     *
     * @param ds The Dataset to process.
     */
    protected void dataset2Image(Dataset ds) {
        BufferedImage dcmImage;
        int dcmWidth;
        int dcmHeight;
        AffineTransform at = new AffineTransform();
        double xScale;
        double yScale;
        double tx = 0.0;
        double ty = 0.0;
        
        // Nur dann etwas tun, wenn ein Dataset vorhanden ist
        if (ds == null) {
            return;
        }
        
        // Erzeugt fuer MONOCHROME Bilder wird ein BufferedImage vom Typ:
        // type = 0 IndexColorModel, #pixelBits = 16
        dcmImage = ds.toBufferedImage();
        dcmWidth = dcmImage.getWidth();
        dcmHeight = dcmImage.getHeight();
        
        // Die standard ImageIO Writer koennen keine 16Bit Graustufenbilder schreiben.
        // Deshalb muessen sie in andere Typen konvertiert werden.
        if (originalSize) {
            // RGB Bild der Originalgroesse erzeugen
            image = new BufferedImage(dcmWidth, dcmHeight,
                    BufferedImage.TYPE_INT_RGB);
            imageG2D = image.createGraphics();
            imageG2D.drawImage(dcmImage, 0, 0, null);
        } else {
            // Quadratisches RGB Bild der Groesse image_size erzeugen
            image = new BufferedImage(image_size, image_size,
                    BufferedImage.TYPE_INT_RGB);
            imageG2D = image.createGraphics();
            
            // Faktor zum Skalieren
            xScale = ((double) image_size) / ((double) dcmWidth);
            yScale = ((double) image_size) / ((double) dcmHeight);
            
            if (yScale < xScale) {
                xScale = yScale;
            }
            
            // Offset fuer Zentrieren
            tx = ((double) image_size - (xScale * dcmWidth)) / 2.0;
            ty = ((double) image_size - (xScale * dcmHeight)) / 2.0;
            
            // Reihenfolge beachten: Erst Translation, dann Skalieren!
            at.translate(tx, ty);
            at.scale(xScale, xScale);
            
            imageG2D.drawImage(dcmImage, at, null);
        }
    }
    
    /**
     * Exports the BufferedImage which is stored in local field image.
     *
     * @param f The File to which the image should be exported.
     */
    protected boolean exportImage(File f) {
        try {
            if (f.exists()) {
                // File besteht bereits. Erst loeschen, dann neu erstellen
                f.delete();
                f.createNewFile();
            } else {
                // Ggf. Directory Pfad erstellen
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
            }
            
            // Mit ImageIO schreiben
            ImageIO.write(image, image_format, f);
            
            // Vorgang ohne Fehler durchgefuehrt
            if (log.isInfoEnabled()) {
                log.info("Dataset with graphic format \"" + image_format +
                        "\" exported");
            }
            
            return true;
        } catch (IOException e) {
            if (log.isInfoEnabled()) {
                log.error("Can't export image - Exception: " + e.getMessage());
            }
            
            return false;
        }
    }
}

/**
 * Revisions:
 *
 * 2006.04.24, hacklaender: Property name replaced by filename
 */
