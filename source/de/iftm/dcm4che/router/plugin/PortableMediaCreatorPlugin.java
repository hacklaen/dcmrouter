/*
 *  PortableMediaCreatorPlugin.java
 *
 *  Copyright (c) 2005 by
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
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;


/**
 * This plugins creates a IHE (Integrating the Healthcare Enterprise) PDI
 * (Portable Data for Imaging) compatible file structure
 * (see http://www.rsna.org/IHE/tf/ihe_tf_index.shtml). The plugin acts as
 * a Portable Media Creator. It supports the properties which start with the key
 * PortableMediaCreatorPlugin. For description of the subkeys (= properties)
 * see the user manual. The following shows an example file structure. Text in []
 * is example text, which is differnt in other use cases.<br>
 * <br>
 *  [Mustermann^Hans_19570522]/             : Patient Name '_' Date of Birth<br>
 *      INDEX.HTM<br>
 *      README.TXT<br>
 *      DICOM/<br>
 *          [DICOM files in the same sub-directory structur as in IHE_PDI/]<br>
 *      IHE_PDI/<br>
 *          INDEX.HTM                       : List of studies<br>
 *          COMPARE.HTM                     : A framneset with two links to INDEX.HTM<br>
 *          [MH570522]/                     : First letter of family name followed by first letter of given name followed by date of birth<br>
 *              [043012]/                   : Study date (maximum of 8 characters)<br>
 *                  [MR1531]/               : Modality, 2 character followed by study time, 4 character<br>
 *                      [4711]/             : Study ID (maximum of 8 characters)<br>
 *                          INDEX.HTM       : List of series<br>
 *                          [3]/            : Series Number (maximum of 8 characters)<br>
 *                              INDEX.HTM   : List of images<br>
 *                              [54.jpg]    : JPG image.Name is the Instance Number<br>
 * <br>
 * All INDEX.HTM files are XHTML conform. They are divided into three sections -
 * Title, Info and List - which are separated by a horizontal line. The List section
 * has links to the next INDEX.HTM file.
 * All information concerning an image is organized in tables. There are up to
 * three tables in a file, each with an individaul "id" attribute. Information
 * about the patient are in a table with id="patient", inforamtion about the studies
 * in id="study", inforamtion about the series in id="series" and the images in a
 * table with id="image". All rows in the tables have a unique "id" attribute, derived
 * from the PatientID, StudyInstanceUID, SeriesInstanceUID and InstanceUID for the image.
 * To allow ascending sorting of the table rows each row has a "title" attribute,
 * which contains the sorting key.<br>
 * <br>
 * This class uses the inner classes HTMLTable, HTMLTableRow and WriteXMLToFileThread.
 *
 * @author Thomas Hacklaender
 * @version 2005.01.17
 */
public class PortableMediaCreatorPlugin extends DicomRouterPlugin {
    /** The version string */
    final String VERSION = "2006.11.10";
    
    /** The logger for this plugin */
    Logger log = Logger.getLogger(PortableMediaCreatorPlugin.class);
    
    /** The properties of this plugin */
    Properties props = null;
    
    /** If true, write files in the subdirectory "[$PatientName_$PatientBirthDate]/<directory>/" */
    boolean separate_patients = true;
    
    /** Writes DICOM files in the subdirectory "[$PatientName_$PatientBirthDate/]<directory>/<DICOM>/". */
    String use_subdirectory_dicom = "DICOM";
    
    /** Writes web files in the subdirectory "[$PatientName_$PatientBirthDate/]<directory>/<IHE_PDI>/". */
    String use_subdirectory_web = "IHE_PDI";
    
    /** Array of strings containing the subdirectories extracted from the given Dataset
     * [0]: First letter of family name followed by first letter of given name
     *      followed by date of birth, 6 character. Example: MH570522<br>
     * [1]: Study date, 6 character. Example: 043012<br>
     * [2]: Modality, 2 character followed by study time, 4 character. Example: MR1531<br>
     * [3]: Study ID. Example: 4711<br>
     * [4]: Series number. Example: 3<br>
     * [5]: Instance number. Example: 54
     */
    String[] fileIDComponents = null;
    
    /** If true:
     * Construct the the file ID of the file to save from the following file ID
     * components given by the array in field fileIDComponents.<br>
     * If false:
     * Construct the the filename of the file to save from directory, name and extension.
     */
    boolean write_dir_tree = true;
    
    /** The ImagIO name of the graphic format to export the Dataset. */
    String image_format = "jpg";
    
    /** HTML file "INDEX.HTM" containing the list of studies for the given Dataset */
    File listOfStudiesHTMLFile = null;
    
    /** HTML file "INDEX.HTM" containing the list of series for the given Dataset */
    File listOfSeriesHTMLFile = null;
    
    /** HTML file "INDEX.HTM" containing the list of images for the given Dataset */
    File listOfImagesHTMLFile = null;
    
    /** The JPG file derived from the given Dataset */
    File currentJPGFile = null;
    
    // >>>>>>>>>>>>>>>>>>>>>>>>>>> Fields derived from properties: >>>>>>>>>>>>
    
    /** True, if the default (build in) start file of the web-contend should be used */
    boolean defaultIndexFile = true;
    
    /** True, if the default (build in) README.TXT file of the web-contend should be used */
    boolean defaultReadmeFile = true;
    
    /** True, if a README.TXT file of the web-contend should be used */
    boolean useReadmeFile = true;
    
    /** True, if the size of the image to export should be the original image size */
    boolean originalSize = false;
    
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>  Local fields defined by properties: >>>>>>>
    
    /** Type of image annotation. */
    int annotation_type = Annotation.ANNOTATION_FULL;
    
    /** Directory to export the Dataset into. */
    File directory = Util.uriToFile("./");
    
    /** The color of the font for image annotation. */
    Color font_color = Color.orange;
    
    /** The name of the font for image annotation. */
    String font_name = "dialog";
    
    /** The font size for image annotation. */
    int font_size = 14;
    
    /** The size of the (square) image to export. */
    int image_size = 512;
    
    /** Reference to the start file of the web-contend. */
    File index_file = Util.uriToFile("./cfg/html/INDEX.HTM");
    
    /** The margin for image annotations in pixel. */
    int margin_size = 4;
    
    /** Reference to the README.TXT file of the web-contend. */
    File readme_file = Util.uriToFile("./cfg/html/README.TXT");
    
    
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    
    
    /**
     * Constructor.
     */
    public PortableMediaCreatorPlugin() {
        // Nichts zu tun.
    }
    
    /**
     * Inits the PortableMediaCreatorPlugin with the specified Properties.
     * @param p  Properties containing the configuration of the plugin.
     */
    public void init(Properties p) {
        String s;
        File f;
        
        // Properties lokal speichern
        props = p;
        
        // Bildannotationen
        s = props.getProperty("annotation-type");
        
        if (s != null) {
            if (s.equals("no")) {
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
                log.warn("Format error in Property \"annotation\". Annotation type set to \"full\"");
                annotation_type = Annotation.ANNOTATION_FULL;
            }
        }
        
        // Directory
        s = props.getProperty("directory");
        
        if (s != null) {
            if ((f = Util.uriToFile(s)) != null) {
                directory = f;
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
                    log.warn("Format error in Property \"image_size\". Image size set to 512*512");
                    image_size = 512;
                }
            }
        }
        
        // INDEX.HTM file fuer den Web-content festlegen
        s = props.getProperty("index-file");
        
        if (s != null) {
            if (s.length() == 0) {
                defaultIndexFile = true;
            } else {
                if (s.equals("default")) {
                    defaultIndexFile = true;
                } else {
                    // Value ist eine URI
                    if ((f = Util.uriToFile(s)) != null) {
                        defaultIndexFile = false;
                        index_file = f;
                    } else {
                        defaultIndexFile = true;
                        log.warn("Format error in Property \"index_file\". Use default INDEX.HTM file");
                    }
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
        
        // README.TXT file fuer den Web-content festlegen
        s = props.getProperty("readme-file");
        
        if (s != null) {
            if ((s.length() == 0) || (s.equals("no"))) {
                useReadmeFile = false;
                defaultReadmeFile = true;
            } else {
                useReadmeFile = true;
                if (s.equals("default")) {
                    defaultReadmeFile = true;
                } else {
                    // Value ist eine URI
                    if ((f = Util.uriToFile(s)) != null) {
                        defaultReadmeFile = false;
                        readme_file = f;
                    } else {
                        defaultReadmeFile = true;
                        log.warn("Format error in Property \"readme_file\". Use default README.TXT file");
                    }
                }
            }
        }
    }
    
    /**
     * Sets dynamic properties, which depend on the actual Dataset to process.
     *
     * @param ds  The Dataset to process.
     * @return true, if dynamic property could be set.
     */
    protected boolean setDynamicProperties(Dataset ds) {
        // Nichts zu tun, kein Fehler aufgetreten
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
     * 
     * @param dataset the Dataset to process.
     * @return CONTINUE if succesfull, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int process(Dataset dataset) {
        // Basisdirectory, die Portable Mediastruktur enthaelt
        // Abhaengig von der Property "separate_patients" ist das baseDirectory
        // identisch zu der Property "directory" oder es ist zunaechst ein
        // Unterverzeichnis fuer jeden Patienten darin erzeugt und baseDirectory
        // ist das Verzeichnis des Patienten.
        File    baseDirectory = null;
        
        // Nur dann weitermachen, wenn ein Datset vorhanden ist
        if (dataset == null) {
            log.warn("No Dataset given.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Dynamische Properties setzen
        if (!setDynamicProperties(dataset)) {
            log.error("Error in extracing dynamic properties.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Das Base Directory festlegen
        baseDirectory = directory;
        if (separate_patients) {
            baseDirectory = Util.addPatientDirectory(baseDirectory, dataset);
        }
        
        // Files und Verzeichnisse für die Portable Media Struktur schreiben
        buildPortableMediaStructure(baseDirectory, dataset);
        
        // DICOM files mit SaveDicomdirPlugin schreiben
        if (runSaveDicomdirPlugin(dataset) != CONTINUE) {
            log.error("Dataset not written to portable media.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // JPG Bilder mit ExportImagePlugin schreiben
        if (runExportImagePlugin(dataset) != CONTINUE) {
            log.error("Dataset not written to portable media.");
            return REQUEST_ABORT_RECEIVER;
        }
        // HTML Dateien verlinken
        linkSeriesListToStudyList(dataset);
        linkImagListToSeriesList(dataset);
        linkImagesToImageList(dataset);
        
        // Plugin ohne Fehler beendet
        if (log.isInfoEnabled()) {
            log.info("Dataset saved to Portable Media CD structure: " + baseDirectory.toString());
        }
        
        return CONTINUE;
    }
    
    
    /**
     * Runs the SaveDicomdirPlugin.
     *
     * @param dataset the given Dataset.
     * @return the return value of the SaveDicomdirPlugin.
     */
    protected int runSaveDicomdirPlugin(Dataset dataset) {
        Properties          props= new Properties();
        SaveDicomdirPlugin  saveDicomdirPlugin = new SaveDicomdirPlugin();
        
        props.setProperty("directory", directory.toURI().toString());
        props.setProperty("separate-patients", Boolean.toString(separate_patients));
        props.setProperty("use-subdirectory", use_subdirectory_dicom);
        props.setProperty("write-dir-tree", Boolean.toString(write_dir_tree));
        props.setProperty("transfersyntax", "ImplicitVRLittleEndian");
        
        saveDicomdirPlugin.init(props);
        return saveDicomdirPlugin.process(dataset);
    }
    
    
    /**
     * Runs the ExportImagePlugin.
     *
     * @param dataset the given Dataset.
     * @return the return value of the ExportImagePlugin.
     */
    protected int runExportImagePlugin(Dataset dataset) {
        Properties          props= new Properties();
        ExportImagePlugin   exportImagePlugin = new ExportImagePlugin();
        
        switch (annotation_type) {
            case Annotation.ANNOTATION_OFF:
                props.setProperty("annotation-type", "off");
                break;
            case Annotation.ANNOTATION_FULL:
                props.setProperty("annotation-type", "full");
                break;
            case Annotation.ANNOTATION_MINIMAL:
                props.setProperty("annotation-type", "minimal");
                break;
            case Annotation.ANNOTATION_ANONYMOUS:
                props.setProperty("annotation-type", "anonymous");
                break;
            case Annotation.ANNOTATION_PSEUDONYM:
                props.setProperty("annotation-type", "pseudonym");
                break;
            default:
                log.info("Unknown annotation-type " + Integer.toString(annotation_type));
        }
        props.setProperty("directory", directory.toURI().toString());
        props.setProperty("separate-patients", Boolean.toString(separate_patients));
        if (font_color.equals(Color.darkGray)) {
            props.setProperty("font-color", "darkGray");
        } else if (font_color.equals(Color.lightGray)) {
            props.setProperty("font-color", "lightGray");
        } else if (font_color.equals(Color.white)) {
            props.setProperty("font-color", "white");
        } else if (font_color.equals(Color.yellow)) {
            props.setProperty("font-color", "yellow");
        } else if (font_color.equals(Color.orange)) {
            props.setProperty("font-color", "orange");
        } else if (font_color.equals(Color.green)) {
            props.setProperty("font-color", "green");
        }
        props.setProperty("font-name", font_name);
        props.setProperty("font-size", Integer.toString(font_size));
        props.setProperty("image-format", image_format);
        if (originalSize) {
            props.setProperty("image-size", "original");
        } else {
            props.setProperty("image-size", Integer.toString(image_size));
        }
        props.setProperty("margin-size", Integer.toString(margin_size));
        props.setProperty("use-subdirectory", use_subdirectory_web);
        props.setProperty("write-dir-tree", Boolean.toString(write_dir_tree));
        
        exportImagePlugin.init(props);
        return exportImagePlugin.process(dataset);
    }
    
    
    /**
     * Links the INDEX.HTM file containing the list of series to the INDEX.HTM
     * file containing the list of studies.
     * 
     * 
     * 
     * 
     * @param dataset the Dataset containing the image data.
     * @return CONTINUE if succesfull, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int linkSeriesListToStudyList(Dataset dataset) {
        Document    studiesDoc;
        String      link;
        
        // Leeren HTML file mit der Liste der Studies einlesen
        studiesDoc = readXMLFromFile(listOfStudiesHTMLFile);
        if (studiesDoc == null) {
            log.error("Can't find list of studies HTML file.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Tabelle mit der Liste der Patienten fuellen (ist immer nur ein Patient)
        if (fillPatientTable(studiesDoc, dataset, null) != CONTINUE) {
            log.error("Can't fill patient table.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Link zur Liste der Series erstellen
        link = linkToFile(listOfStudiesHTMLFile, listOfSeriesHTMLFile);
        
        // Tabelle mit der Liste der Studies fuellen.
        // Die SudyID mit dem Link auf die Liste der Series versehen
        if (fillStudyTable(studiesDoc, dataset, link) != CONTINUE) {
            log.error("Can't fill study table.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Den INDEX.HTM file der Studies speichern
        writeXMLToFile(studiesDoc, listOfStudiesHTMLFile);
        
        return CONTINUE;
    }
    
    
    /**
     * Links the INDEX.HTM file containing the list of images to the INDEX.HTM
     * file containing the list of series.
     * 
     * 
     * 
     * 
     * @param dataset the Dataset containing the image data.
     * @return CONTINUE if succesfull, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int linkImagListToSeriesList(Dataset dataset) {
        Document    seriesDoc;
        String      link;
        
        // Leeren HTML file mit der Liste der Series einlesen
        seriesDoc = readXMLFromFile(listOfSeriesHTMLFile);
        if (seriesDoc == null) {
            log.error("Can't find list of studies HTML file.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Tabelle mit der Liste der Patienten fuellen (ist immer nur ein Patient)
        if (fillPatientTable(seriesDoc, dataset, null) != CONTINUE) {
            log.error("Can't fill patient table.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Tabelle mit der Liste der Studies fuellen
        if (fillStudyTable(seriesDoc, dataset, null) != CONTINUE) {
            log.error("Can't fill study table.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Link zur Liste der Images erstellen
        link = linkToFile(listOfSeriesHTMLFile, listOfImagesHTMLFile);
        
        // Tabelle mit der Liste der Series fuellen.
        // Die SeriesNumber mit dem Link auf die Liste der Images versehen
        if (fillSeriesTable(seriesDoc, dataset, link) != CONTINUE) {
            log.error("Can't fill series table.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Den INDEX.HTM file der Series speichern
        writeXMLToFile(seriesDoc, listOfSeriesHTMLFile);
        
        return CONTINUE;
    }
    
    
    /**
     * Links the individual JPG images to the INDEX.HTM file containing the list of series.
     * 
     * 
     * 
     * 
     * @param dataset the Dataset containing the image data.
     * @return CONTINUE if succesfull, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int linkImagesToImageList(Dataset dataset) {
        Document    imagesDoc;
        String      link;
        
        // Leeren HTML file mit der Liste der Images einlesen
        imagesDoc = readXMLFromFile(listOfImagesHTMLFile);
        if (imagesDoc == null) {
            log.error("Can't find list of images HTML file.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Tabelle mit der Liste der Patienten fuellen (ist immer nur ein Patient)
        if (fillPatientTable(imagesDoc, dataset, null) != CONTINUE) {
            log.error("Can't fill patient table.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Tabelle mit der Liste der Studies fuellen
        if (fillStudyTable(imagesDoc, dataset, null) != CONTINUE) {
            log.error("Can't fill study table.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Tabelle mit der Liste der Series fuellen
        if (fillSeriesTable(imagesDoc, dataset, null) != CONTINUE) {
            log.error("Can't fill series table.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Tabelle mit der Liste der Images fuellen
        if (fillImageTable(imagesDoc, dataset, null) != CONTINUE) {
            log.error("Can't fill image table.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Den INDEX.HTM file der Series speichern
        writeXMLToFile(imagesDoc, listOfImagesHTMLFile);
        
        return CONTINUE;
    }
    
    
    /**
     * Inserts patient-information into the table id="patient". Does nothing, if
     * there is already a table row with the same PatientID.
     * 
     * 
     * 
     * 
     * @param document the Document containing the DOM represenation of the HTML file.
     * @param dataset the Dataset containing the image data.
     * @param link a string containing the link from the actual HTML file to the
     *             HTML file to open, when clicking at the table elemnt in the last
     *             column. null, if no link should be used.
     * @return CONTINUE if succesfull, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int fillPatientTable(Document document, Dataset dataset, String link) {
        HTMLTable   patientTab;
        String[]    patientInfo = new String[3];
        // Defaultwert fuer das Sortierkriterium
        // Der spacs-Character kann nicht verwendet werden, da in XML keine fuehrenden
        // Whitespaces in Attribut-Values erlaubt sind. '#' ist in der Sortierreihenfolge
        // ebenfalls von Ziffern und Buchstaben angesiedelt.
        String      titleToSort = "#";
        
        // Tabelle mit id="patient" finden
        patientTab = new HTMLTable(document, "patient");
        if (patientTab.tableElement == null) {
            log.error("Can't find patient table in HTML file.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Patient-Informationen extrahieren
        String patientID = dataset.getString(Tags.PatientID, "");
        String patientName = dataset.getString(Tags.PatientName, "X^X");
        String patientBirthDate = dataset.getString(Tags.PatientBirthDate, "19990101");
        String patientSex = dataset.getString(Tags.PatientSex, "O");
        
        // "patient" Tabelle fuellen
        patientInfo[0] = patientName;
        patientInfo[1] = patientBirthDate;
        patientInfo[2] = patientSex;
        
        // Nur dann in Tabelle einfuegen, wenn noch nicht vorhanden
        if (! patientTab.contains(patientID)) {
            patientTab.append(new HTMLTableRow(document, patientInfo, patientID, titleToSort, link));
            // Zeilen sortieren
            patientTab.sort();
        }
        
        return CONTINUE;
    }
    
    
    /**
     * Inserts study-information into the table id="study". Does nothing, if
     * there is already a table row with the same studyInstanceUID.
     * 
     * 
     * 
     * 
     * @param document the Document containing the DOM represenation of the HTML file.
     * @param dataset the Dataset containing the image data.
     * @param link a string containing the link from the actual HTML file to the
     *             HTML file to open, when clicking at the table elemnt in the last
     *             column. null, if no link should be used.
     * @return CONTINUE if succesfull, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int fillStudyTable(Document document, Dataset dataset, String link) {
        HTMLTable   studyTab;
        String[]    studyInfo = new String[5];
        // Defaultwert fuer das Sortierkriterium
        // Der spacs-Character kann nicht verwendet werden, da in XML keine fuehrenden
        // Whitespaces in Attribut-Values erlaubt sind. '#' ist in der Sortierreihenfolge
        // ebenfalls von Ziffern und Buchstaben angesiedelt.
        String      titleToSort = "#";
        
        // Tabelle mit id="study" finden
        studyTab = new HTMLTable(document, "study");
        if (studyTab.tableElement == null) {
            log.error("Can't find study table in HTML file.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Study-Informationen extrahieren
        String studyInstanceUID = dataset.getString(Tags.StudyInstanceUID, "");
        String studyDate = dataset.getString(Tags.StudyDate, "20050101");
        String studyTime = dataset.getString(Tags.StudyTime, "154812");
        String modality = dataset.getString(Tags.Modality, "OT");
        String studyDescription = dataset.getString(Tags.StudyDescription, "");
        String studyID = dataset.getString(Tags.StudyID, "1");
        
        // "study" Tabelle fuellen
        studyInfo[0] = studyDate;
        studyInfo[1] = studyTime.substring(0, 6);
        studyInfo[2] = modality;
        studyInfo[3] = studyDescription;
        studyInfo[4] = studyID;
        
        // Sortier-Schlüssel für die Zeilen der Study-Tabelle
        titleToSort = studyDate + studyTime.substring(0, 6);
        
        // Nur dann in Tabelle einfuegen, wenn noch nicht vorhanden
        if (! studyTab.contains(studyInstanceUID)) {
            studyTab.append(new HTMLTableRow(document, studyInfo, studyInstanceUID, titleToSort, link));
            // Zeilen sortieren
            studyTab.sort();
        }
        
        return CONTINUE;
    }
    
    
    /**
     * Inserts series-information into the table id="series". Does nothing, if
     * there is already a table row with the same seriesInstanceUID.
     * 
     * 
     * 
     * 
     * @param document the Document containing the DOM represenation of the HTML file.
     * @param dataset the Dataset containing the image data.
     * @param link a string containing the link from the actual HTML file to the
     *             HTML file to open, when clicking at the table elemnt in the last
     *             column. null, if no link should be used.
     * @return CONTINUE if succesfull, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int fillSeriesTable(Document document, Dataset dataset, String link) {
        HTMLTable   seriesTab;
        String[]    seriesInfo = new String[4];
        // Defaultwert fuer das Sortierkriterium
        // Der spacs-Character kann nicht verwendet werden, da in XML keine fuehrenden
        // Whitespaces in Attribut-Values erlaubt sind. '#' ist in der Sortierreihenfolge
        // ebenfalls von Ziffern und Buchstaben angesiedelt.
        String      titleToSort = "#";
        
        // Tabelle id="series" finden
        seriesTab = new HTMLTable(document, "series");
        if (seriesTab.tableElement == null) {
            log.error("Can't find series table in HTML file.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Series-Informationen extrahieren
        String seriesInstanceUID = dataset.getString(Tags.SeriesInstanceUID, "");
        String seriesDate = dataset.getString(Tags.SeriesDate, "20050101");
        String seriesTime = dataset.getString(Tags.SeriesTime, "154812");
        String seriesDescription = dataset.getString(Tags.SeriesDescription, "");
        String seriesNumber = dataset.getString(Tags.SeriesNumber, "1");
        
        // "series" Tabelle fuellen
        seriesInfo[0] = seriesDate;
        seriesInfo[1] = seriesTime.substring(0, 6);
        seriesInfo[2] = seriesDescription;
        seriesInfo[3] = seriesNumber;
        
        // Sortier-Schlüssel für die Zeilen der Series-Tabelle
        String s = "#####" + seriesNumber;
        titleToSort = s.substring(s.length() - 5);
        
        // Nur dann in Tabelle einfuegen, wenn noch nicht vorhanden
        if (! seriesTab.contains(seriesInstanceUID)) {
            seriesTab.append(new HTMLTableRow(document, seriesInfo, seriesInstanceUID, titleToSort, link));
            // Zeilen sortieren
            seriesTab.sort();
        }
        
        return CONTINUE;
    }
    
    
    /**
     * Inserts image-information into the table id="image". Does nothing, if
     * there is already a table row with the same sopInstanceUID.
     * 
     * 
     * 
     * 
     * @param document the Document containing the DOM represenation of the HTML file.
     * @param dataset the Dataset containing the image data.
     * @param link a string containing the link from the actual HTML file to the
     *             HTML file to open, when clicking at the table elemnt in the last
     *             column. null, if no link should be used.
     * @return CONTINUE if succesfull, REQUEST_ABORT_RECEIVER otherwise
     */
    protected int fillImageTable(Document document, Dataset dataset, String link) {
        HTMLTable       imageTab;
        HTMLTableRow    r;
        // Defaultwert fuer das Sortierkriterium
        // Der spacs-Character kann nicht verwendet werden, da in XML keine fuehrenden
        // Whitespaces in Attribut-Values erlaubt sind. '#' ist in der Sortierreihenfolge
        // ebenfalls von Ziffern und Buchstaben angesiedelt.
        String          titleToSort = "#";
        
        // Tabelle mit id="image" finden
        imageTab = new HTMLTable(document, "image");
        if (imageTab.tableElement == null) {
            log.error("Can't find image table in HTML file.");
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Image-Informationen extrahieren
        String sopInstanceUID = dataset.getString(Tags.SOPInstanceUID, "");
        String instanceNumber = dataset.getString(Tags.InstanceNumber, "1");
        
        // Sortier-Schlüssel für die Zeilen der Image-Tabelle
        String s = "#####" + instanceNumber;
        titleToSort = s.substring(s.length() - 5);
        
        // Nur dann in Tabelle einfuegen, wenn noch nicht vorhanden
        if (! imageTab.contains(sopInstanceUID)) {
            r = new HTMLTableRow();
            r.setLinkToJPGImage(document, sopInstanceUID, titleToSort, instanceNumber);
            imageTab.append(r);
            // Zeilen sortieren
            imageTab.sort();
        }
        
        return CONTINUE;
    }
    
    
    /**
     * Creates all common files and directories for the Portable Media. Setup the
     * fields fileIDComponents, listOfStudiesHTMLFile, listOfSeriesHTMLFile,
     * listOfImagesHTMLFile and currentJPGFile.
     *
     * @param baseDirectory the directory, where the Portable Media should be stored.
     * @param ds the dataset to analyse.
     */
    protected void buildPortableMediaStructure(File baseDirectory, Dataset ds) {
        String      pathString;
        File        f;
        
        // Neues Array erzeugen
        fileIDComponents = new String[6];
        
        // Das base Directory erzeugen
        if (! baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }
        
        // Allgemeine Files und Directories erzeugen/ kopieren
        f = new File(baseDirectory, "DICOM");
        if (! f.exists()) {
            f.mkdir();
        }
        
        f = new File(baseDirectory, "IHE_PDI");
        if (! f.exists()) {
            f.mkdir();
        }
        
        f = new File(baseDirectory, "INDEX.HTM");
        if (! f.exists()) {
            // Nur wenn File noch nicht existiert
            if (defaultIndexFile) {
                // Default File = Resource verwenden
                copyFileFromResource(PortableMediaCreatorPlugin.class.getResourceAsStream("resources/INDEX.HTM"), f);
            } else {
                // In Properties angegebenen File verwenden
                copyFile(index_file, f);
            }
        }
        
        if (useReadmeFile) {
            // Nur wenn ein README.TXT file geschrieben werden soll
            f = new File(baseDirectory, "README.TXT");
            if (! f.exists()) {
                // Nur wenn File noch nicht existiert
                if (defaultReadmeFile) {
                    // Default File = Resource verwenden
                    copyFileFromResource(PortableMediaCreatorPlugin.class.getResourceAsStream("resources/README.TXT"), f);
                } else {
                // In Properties angegebenen File verwenden
                copyFile(readme_file, f);
                }
            }
        }
        
        f = new File(baseDirectory, "IHE_PDI/COMPARE.HTM");
        if (! f.exists()) {
            copyFileFromResource(PortableMediaCreatorPlugin.class.getResourceAsStream("resources/COMPARE.HTM"), f);
        }
        
        // HTML Files mit den Referenzen zur Verfuegung stellen:
        
        // Komponenten der File IDs erzeugen
        fileIDComponents = Util.datasetToNameArray(ds);
        
        // Directories erstellen
        pathString = "IHE_PDI";
        for (int i = 0; i < fileIDComponents.length - 1; i++) {
            pathString += "/" + fileIDComponents[i];
        }
        f = new File(baseDirectory, pathString);
        if (! f.exists()) {
            f.mkdirs();
        }
        
        // "Study" HTML File mit der Liste der Studie
        listOfStudiesHTMLFile = new File(baseDirectory, "IHE_PDI/INDEX.HTM");
        if (! listOfStudiesHTMLFile.exists()) {
            copyFileFromResource(PortableMediaCreatorPlugin.class.getResourceAsStream("resources/study_index.htm"), listOfStudiesHTMLFile);
        }
        
        // "Series" HTML File mit der Liste der Serien
        listOfSeriesHTMLFile = new File(baseDirectory, "IHE_PDI/" + fileIDComponents[0] + "/" + fileIDComponents[1] + "/" + fileIDComponents[2] + "/" + fileIDComponents[3] + "/INDEX.HTM");
        if (! listOfSeriesHTMLFile.exists()) {
            copyFileFromResource(PortableMediaCreatorPlugin.class.getResourceAsStream("resources/series_index.htm"), listOfSeriesHTMLFile);
        }
        
        // "Images" HTML File mit der Tablle der Bilder
        listOfImagesHTMLFile = new File(baseDirectory, "IHE_PDI/" + fileIDComponents[0] + "/" + fileIDComponents[1] + "/" + fileIDComponents[2] + "/" + fileIDComponents[3]  + "/" + fileIDComponents[4]+ "/INDEX.HTM");
        if (! listOfImagesHTMLFile.exists()) {
            copyFileFromResource(PortableMediaCreatorPlugin.class.getResourceAsStream("resources/image_index.htm"), listOfImagesHTMLFile);
        }
    }
    
    
    /**
     * Calculates the relative link from the base to a destination file.
     *
     * @param base the base file.
     * @param destination the destination file.
     * @return the relative link as a string.
     */
    protected String linkToFile(File base, File destination) {
        URI baseURI;
        URI destinationURI;
        
        if (base.isDirectory()) {
            baseURI = base.toURI();
        } else {
            baseURI = base.getParentFile().toURI();
        }
        destinationURI = destination.toURI();
        
        return baseURI.relativize(destinationURI).toString();
    }
    
    
    /**
     * Read a XML (XHTML) file to a DOM document.
     *
     * @param file the file containing the XML representation.
     * @return the DOM document containing the XML file.
     */
    protected Document readXMLFromFile(File file) {
        Document        document = null;
        FileInputStream fis = null;
        
        try{
            fis = new FileInputStream(file);
            document = readXMLFromStream(fis);
        } catch (IOException e) {
            log.error("readFromFile::IOException: " + e);
        } finally {
            try {
                fis.close();
            } catch (IOException e) {};
        }
        
        return document;
    }
    
    
    /**
     * Read a XML (XHTML) resource file to a DOM document.
     *
     * @param in the InputStream of the resource file containing the XML
     *        representation. Use xxxx.class.getResourceAsStream(String name)
     *        to access the resource file.
     * @return the DOM document containing the XML file.
     */
    protected Document readXMLFromStream(InputStream in) {
        // JAVADOC NOTE: An implementation of the DocumentBuilderFactory class
        // is NOT guaranteed to be thread safe. It is up to the user application
        // to make sure about the use of the DocumentBuilderFactory from more
        // than one thread. Alternatively the application can have one instance
        // of the DocumentBuilderFactory per thread.
        ReadXMLFromStreamThread readThread = new ReadXMLFromStreamThread(in);
        // Startet den Thread
        readThread.start();
        try {
            // Wartet bis der Thread beendet ist.
            readThread.join();
        } catch (InterruptedException e) {
            log.error("writeXMLToFile::InterruptedException: " + e);
        }
        
        return readThread.document;
    }
    
    
    /**
     * Write a DOM document to a XML (XHTML) file.
     *
     * @param document  the document to write.
     * @param file the file to write to.
     */
    protected void writeXMLToFile(Document document, File file) {
        // JAVADOC NOTE: An implementation of the TransformerFactory class is
        // NOT guaranteed to be thread safe. It is up to the user application
        // from more than one thread. Alternatively the application can have one
        // instance to make sure about the use of the TransformerFactory of the
        // TransformerFactory per thread.
        WriteXMLToFileThread writeThread = new WriteXMLToFileThread(document, file);
        // Startet den Thread
        writeThread.start();
        try {
            // Wartet bis der Thread beendet ist.
            writeThread.join();
        } catch (InterruptedException e) {
            log.error("writeXMLToFile::InterruptedException: " + e);
        }
    }
    
    
    /**
     * Copy a file from source to destination. Waits until the operating system
     * has written the file.
     *
     * @param source the source file.
     * @param destination the destination file.
     */
    public void copyFile(File source, File destination) {
        FileInputStream     fis  = null;
        FileOutputStream    fos = null;
        FileDescriptor      fd = null;
        
        try {
            fis  = new FileInputStream(source);
            fos = new FileOutputStream(destination);
            fd = fos.getFD();
            
            byte[] buf = new byte[1024];
            int i = 0;
            while((i=fis.read(buf))!=-1) {
                fos.write(buf, 0, i);
                
                // Flush the data from the streams and writers into system buffers.
                // The data may or may not be written to disk.
                fos.flush();
                
                // Block until the system buffers have been written to disk.
                // After this method returns, the data is guaranteed to have
                // been written to disk.
                fd.sync();
            }
        } catch (IOException e) {
            log.error("copyFile::IOException: " + e);
        } finally {
            try {
                fis.close();
            } catch (Exception e) {}
            try {
                fos.close();
            } catch (Exception e) {}
        }
    }
    
    
    /**
     * Copy a file from a resource to destination. Waits until the operating system
     * has written the file. Use xxxx.class.getResourceAsStream(String name)
     * to access the resource file.
     *
     * @param is the stream of the source file.
     * @param destination the destination file.
     */
    public void copyFileFromResource(InputStream is, File destination) {
        FileOutputStream    fos = null;
        FileDescriptor      fd = null;
        
        try {
            fos = new FileOutputStream(destination);
            fd = fos.getFD();
            
            byte[] buf = new byte[1024];
            int i = 0;
            while((i=is.read(buf))!=-1) {
                fos.write(buf, 0, i);
            }
            
            // Flush the data from the streams and writers into system buffers.
            // The data may or may not be written to disk.
            fos.flush();
            
            // Block until the system buffers have been written to disk.
            // After this method returns, the data is guaranteed to have
            // been written to disk.
            fd.sync();
        } catch (IOException e) {
            log.error("copyFile::IOException: " + e);
        } finally {
            try {
                fos.close();
            } catch (Exception e) {}
        }
        try {
            is.close();
        } catch (Exception e) {}
    }
    
    
    /**
     * Only for Junit Test to access member class.
     */
    protected HTMLTable newInstanceOfHTMLTable(Document document, String idAttrValue) {
        return new HTMLTable(document, idAttrValue);
    }
    
    
    /**
     * Only for Junit Test to access member class.
     */
    protected HTMLTableRow newInstanceOfHTMLTableRow(Document document, String[] columnValues, String idAttrValue, String titleAttrValue, String link) {
        return new HTMLTableRow(document, columnValues, idAttrValue, titleAttrValue, link);
    }
    
    
    /**
     * Only for Junit Test to access member class.
     */
    protected HTMLTableRow newInstanceOfHTMLTableRow(Document document, Element row) {
        return new HTMLTableRow(document, row);
    }
    
    
    //>>>>>>>>>>>>>>>>>> Member Classes >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    
    
    /**
     * This class implements a thread to read a DOM document from a stream. This
     * is neccessary because the DocumentBuilderFactory is not thread save (see Javadoc).
     */
    protected class ReadXMLFromStreamThread extends Thread {
        
        /** The input stream */
        InputStream in;
        
        /** The read document */
        Document document = null;
        
        
        /**
         * The constructor only stores the parameters into local fields for
         * further processing by the "run" method.
         * @param in the input stream.
         */
        protected ReadXMLFromStreamThread(InputStream in) {
            this.in = in;
        }
        
        /**
         * The entry point of the thread.
         */
        public void run() {
            // Create a factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            // Ignore comments
            factory.setIgnoringComments(false);
            // Convert CDATA to Text nodes
            factory.setCoalescing(true);
            // No Namespace
            factory.setNamespaceAware(false);
            // Don't validate DTD
            factory.setValidating(false);
            try{
                // Create parser
                DocumentBuilder parser = factory.newDocumentBuilder();
                // Parse the file to document
                document = parser.parse(in);
            } catch (IOException e) {
                log.error("readFromFile::IOException: " + e);
            } catch (ParserConfigurationException e) {
                log.error("readFromFile::ParserConfigurationException: " + e);
            } catch (SAXException e) {
                log.error("readFromFile::SAXException: " + e);
            }
        }
    }
    
    /**
     * This class implements a thread to write a DOM document to a file. This
     * is neccessary because the Transformer is not thread save (see Javadoc).
     */
    protected class WriteXMLToFileThread extends Thread {
        
        /** The document to write */
        Document    document;
        
        /** The destination file */
        File        file;
        
        /**
         * The constructor only stores the parameters into local fields for
         * further processing by the "run" method.
         * @param document the document to write.
         * @param file the destination file.
         */
        protected WriteXMLToFileThread(Document document, File file) {
            this.document = document;
            this.file = file;
        }
        
        /**
         * The entry point of the thread. The thread waits until the operating
         * system has written the data to the destination file.<br>
         * <br>
         * To be strict XHTML konform the file should include the following
         * document type definition after the <?xml /> line.<br>
         * <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> <br>
         * The problem is, that the parser tries to download the given DTD. It
         * waits a time out period until it gives up. This time period delays
         * the execution of the plugin (4 up to 30 seconds).
         * Therefore the doctype definion is ommitted in this version.
         */
        public void run() {
            Source              source = null;
            Result              result = null;
            FileOutputStream    os = null;
            FileDescriptor      fd = null;
            
            try {
                // Prepare the DOM document for writing
                source = new DOMSource(document);
                //Prepare the output file
                os = new FileOutputStream(file);
                fd = os.getFD();
                result = new StreamResult(os);
                //Write DOM document to file
                Transformer xformer = TransformerFactory.newInstance().newTransformer();
                // Wird method="html" gewaehlt, dann fuegt der Transformer den Tag
                // <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
                // zwischen <head> und </head> ein. Dieser Tag ist nicht XML konform
                // (es fehlt </META> und er ist in Grossbuchstabengeschrieben) und
                // fuehrt beim erneuten Einlesen zu einer SAX Exception.
                xformer.setOutputProperty(OutputKeys.METHOD, "xml");
                // xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.0 Strict//EN");
                // xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
                xformer.transform(source, result);
                
                // Flush the data from the streams and writers into system buffers.
                // The data may or may not be written to disk.
                os.flush();
                
                // Block until the system buffers have been written to disk.
                // After this method returns, the data is guaranteed to have
                // been written to disk.
                fd.sync();
            } catch (SyncFailedException e){
                log.error("writeXMLToFileThread::SyncFailedException: " + e);
            } catch (FileNotFoundException e){
                log.error("writeXMLToFileThread::FileNotFoundException: " + e);
            } catch (IOException e){
                log.error("writeXMLToFileThread::IOException: " + e);
            } catch (TransformerConfigurationException e){
                log.error("writeXMLToFileThread::TransformerConfigurationException: " + e);
            } catch (TransformerException e) {
                log.error("writeXMLToFileThread::TransformerException: " + e);
            } finally {
                try {
                    os.close();
                } catch (Exception e) {}
            }
        }
    }
    
    
    /**
     * The objects of this class represent a HTML table. The objects support
     * appending new rows and sorting the rows depending of the row attribute
     * "title".
     *
     * <br>
     * This class uses the inner class TableRowsComparator.
     */
    protected class HTMLTable {
        
        /** The DOM table Element. null if no Element is defined.  */
        Element tableElement = null;
        
        /** Vector of HTMLTableRow objects building the tableElement. */
        protected Vector tableRows = null;
        
        
        /**
         * Select a table of a document with a given value of the attribute "iD"
         * as the base of this object.
         *
         * @param document the DOM Document containing the table.
         * @param idAttrValue the value of the "id" attribute used to select one
         *        table in the documen.
         */
        protected HTMLTable(Document document, String idAttrValue) {
            NodeList    tableNodes = null;
            NodeList    trNodes = null;
            Element     elm;
            String      attrValue;
            Attr        attr;
            
            // Beide Parameter muessen definiert sein
            if ((document == null) || (idAttrValue == null)) return;
            
            // "table" Elemente finden
            tableNodes = document.getElementsByTagName("table");
            
            // Return, wenn keine "table" Elemente gefunden
            if (tableNodes == null) return;
            
            // Alle Tabellen durchgehen
            for (int i = 0; i < tableNodes.getLength(); i++) {
                elm = (Element) tableNodes.item(i);
                attr = elm.getAttributeNode("id");
                if (attr != null){
                    // Tabelle hat Attribut "id"
                    if (attr.getValue().equals(idAttrValue)) {
                        // Tabelle hat den gwuenschten Wert für das Attribut "id"
                        tableElement = elm;
                    }
                }
            }
            
            // Return, wenn keine Tabelle mit korrektem Attribut "id" gefunden
            if (tableElement == null) return;
            
            // Vector fuer Zeilen anlegen
            tableRows = new Vector();
            
            // "tr" Elemente finden
            trNodes = tableElement.getElementsByTagName("tr");
            
            // Return, wenn keine "tr" Elemente gefunden
            if (trNodes == null) return;
            
            // Return, wenn 0 "tr" Elemente gefunden
            if (trNodes.getLength() == 0) return;
            
            // Fuer jedes "tr" Element ein HTMLTableRow Objekt erzeugen
            for (int i = 0; i < trNodes.getLength(); i++) {
                tableRows.add(new HTMLTableRow(document, (Element) trNodes.item(i)));
            }
        }
        
        
        /**
         * Checks, if the table represented by this object contains a row with
         * a given value of the attribute "id".
         *
         * return true, if the table contains the row.
         */
        protected boolean contains(String value) {
            for (int i = 0; i < tableRows.size(); i++) {
                if (((HTMLTableRow) tableRows.get(i)).idAttrValue.equals(value)) {
                    return true;
                }
            }
            
            return false;
        }
        
        
        /**
         * Appends a HTMLTableRow element to the end of the Vector containing all
         * rows of the table represented by this object.
         *
         * @param tableRow the HTMLTableRow to append.
         */
        protected void append(HTMLTableRow tableRow) {
            // Neue Zeile zum Vector hinzufuegen
            tableRows.add(tableRow);
            update();
        }
        
        
        /**
         * Sorts the Vector containing all rows of the table according to the
         * value of the "title" attribute.
         */
        protected void sort() {
            Collections.sort(tableRows, new TableRowsComparator());
            update();
        }
        
        
        /**
         * Rebuilds the DOM table element from the Vector containg the HTMLTableRow
         * objects.
         */
        private void update() {
            // Alle Zeilen aus Tabelle löschen
            while (tableElement.hasChildNodes()) {
                tableElement.removeChild(tableElement.getLastChild());
            }
            
            // Neue Zeilen aus Vector eintragen
            for (int i = 0; i < tableRows.size(); i++) {
                tableElement.appendChild(((HTMLTableRow) tableRows.get(i)).trElement);
            }
        }
        
        
        /**
         * This class contains the sorting algorithm for the method sort.
         */
        protected class TableRowsComparator implements Comparator {
            
            public int compare(Object o1, Object o2) {
                String s1 = ((HTMLTableRow) o1).titleAttrValue;
                String s2 = ((HTMLTableRow) o2).titleAttrValue;
                
                return s1.compareTo(s2);
            }
        }
    }
    
    
    /**
     * This class implements an object containing a DOM table row element. It
     * also has fields for the values of the attributes "id" and "title".
     * The "title" attribute is used for soring the table rows. The "id" values
     * is used as a distinct identification of the rows.
     */
    protected class HTMLTableRow {
        
        /** The DOM table row Element. null if no Element is defined. */
        Element trElement = null;
        
        /** Value of the "id" attribute */
        String idAttrValue = "";
        
        /** Value of the "title" attribute */
        String titleAttrValue = "";
        
        
        /**
         * Use the default values for the fields.
         */
        protected HTMLTableRow() {
            // Defaultwerte der fields
        }
        
        
        /**
         * Construct a new object from the given parameters.
         *
         * @param document the DOM Document containing the table to which the rows
         *                 belong.
         * @param columnValues the array of strings containing the contents of the
         *                     columns of the row.
         * @param idAttrValue the value of the "id" attribute.
         * @param titleAttrValue the value of the "title" attribute.
         * @param link an optional link to a HTML file, which should be associated
         *             to the value of the last column. null, if no link should be
         *             associated.
         */
        protected HTMLTableRow(Document document, String[] columnValues, String idAttrValue, String titleAttrValue, String link) {
            Element tdElement;
            Element aElement;
            Text    textNode;
            
            // Lokale Variablen besetzen
            if (idAttrValue != null) {
                this.idAttrValue = idAttrValue;
            }
            if (titleAttrValue != null) {
                this.titleAttrValue = titleAttrValue;
            }
            
            // tr Knoten erzeugen
            trElement = document.createElement("tr");
            trElement.setAttribute("id", this.idAttrValue);
            trElement.setAttribute("title", this.titleAttrValue);
            
            // Keine Spalten
            if (columnValues == null) return;
            if (columnValues.length == 0) return;
            
            for (int i = 0; i < columnValues.length; i++) {
                tdElement = document.createElement("td");
                textNode = document.createTextNode(columnValues[i]);
                if ((link != null) & (i == columnValues.length - 1)) {
                    // link Knoten erzeugen
                    aElement = document.createElement("a");
                    aElement.setAttribute("href", link);
                    aElement.appendChild(textNode);
                    tdElement.appendChild(aElement);
                    trElement.appendChild(tdElement);
                } else {
                    tdElement.appendChild(textNode);
                    trElement.appendChild(tdElement);
                }
            }
        }
        
        
        /**
         * Construct a new object from the given parameters.
         *
         * @param document the DOM Document containing the table to which the rows
         *                 belong.
         * @param trElement the table row element of the document, which should be
         *                  used as the base of this object.
         */
        protected HTMLTableRow(Document document, Element trElement) {
            NodeList    tdNodes = null;
            String      value;
            
            if (trElement == null) return;
            
            // Das Element lokal speichern
            this.trElement = trElement;
            
            // Value des "id" Attributes holen
            value = trElement.getAttribute("id");
            if (value != null) {
                idAttrValue = trElement.getAttribute("id");
            }
            
            // Value des "title" Attributes holen
            value = trElement.getAttribute("title");
            if (value != null) {
                titleAttrValue = trElement.getAttribute("title");
            }
        }
        
        
        /**
         * Construct a new object from the given parameters.
         *
         * @param document the DOM Document containing the table to which the rows
         *                 belong.
         * @param idAttrValue the value of the "id" attribute.
         * @param titleAttrValue the value of the "title" attribute.
         * @param instanceNumber the InstanceNumber of the image. It is the also
         *                       the name of the JPG file (without the extension
         *                       ".jpg" and the contents of the second column in the row.
         */
        protected void setLinkToJPGImage(Document document, String idAttrValue, String titleAttrValue, String instanceNumber) {
            Element tdElementLink;
            Element tdElementName;
            Element imgElement;
            Text    textNode;
            
            // Lokale Variablen besetzen
            if (idAttrValue != null) {
                this.idAttrValue = idAttrValue;
            }
            if (titleAttrValue != null) {
                this.titleAttrValue = titleAttrValue;
            }
            
            trElement = document.createElement("tr");
            trElement.setAttribute("id", this.idAttrValue);
            trElement.setAttribute("title", this.titleAttrValue);
            
            tdElementLink = document.createElement("td");
            imgElement = document.createElement("img");
            imgElement.setAttribute("src", instanceNumber + "." + image_format);
            tdElementLink.appendChild(imgElement);
            
            tdElementName = document.createElement("td");
            textNode = document.createTextNode(instanceNumber);
            tdElementName.appendChild(textNode);
            
            trElement.appendChild(tdElementLink);
            trElement.appendChild(tdElementName);
        }
        
    }
    
}
