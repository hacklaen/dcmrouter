/*
 *  XSLTransformPlugin.java
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

import org.xml.sax.SAXException;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;


/**
 * This plugins transforms the given Dataset using a XSL document.
 *
 * @author Thomas Hacklaender
 * @version 2006.04.24
 */
public class XSLTransformPlugin extends DicomRouterPlugin {
    /** The version string */
    final String VERSION = "2006.11.10";
    
    /** The logger for this plugin */
    Logger log = Logger.getLogger(XSLTransformPlugin.class);
    
    /** The properties of this plugin */
    Properties props = null;
    
    // Local fields defined by properties:
    
    /** True, if the source XML file should be saved */
    boolean save_source_xml = false;
    
    /** Directory of the source XML file */
    File source_xml_directory = Util.uriToFile("./");
    
    /** Name of the source XML file */
    String source_xml_filename = "source";
    
    /** Name of the source XML file */
    String source_xml_extension = ".xml";
    
    /** Directory of the XSL file */
    File xsl_directory = Util.uriToFile("./cfg/xsl/");
    
    /** Name of the XSL file */
    String xsl_filename = "Identity.xsl";
    
    /** True, if the XML representation of the Dataset should be transformed with the XSL file. */
    boolean transform = true;
    
    /** True, if all data except the pixel data should be modified. */
    boolean untouch_pixel_data = true;
    
    /** True, if the transformed XML file should be saved */
    boolean save_transformed_xml = false;
    
    /** True, if the transformed XML file should be reconverted to a Dataset */
    boolean reconvert_transformed_xml = true;
    
    /** Directory of the transformed XML file */
    File transformed_xml_directory = Util.uriToFile("./");
    
    /** Name of the transformed XML file */
    String transformed_xml_filename = "transformed";
    
    /** Name of the transformed XML file */
    String transformed_xml_extension = ".xml";
    
    /**
     * Constructor.
     */
    public XSLTransformPlugin() {
        // Nichts zu tun.
    }
    
    /**
     * Inits the TransformXSLPlugin with the specified Properties.
     * @param p  Properties containing the configuration of the plugin
     */
    public void init(Properties p) {
        String key;
        String value;
        File newDirectory;
        
        // Properties lokal speichern
        props = p;
        
        // Alle Properties bearbeiten
        for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
            // Key/Value Paar holen
            key = (String) en.nextElement();
            value = props.getProperty(key);
            
            // save_source_xml
            if (key.equals("save-source-xml")) {
                if (value.toLowerCase().charAt(0) == 't') {
                    save_source_xml = true;
                } else {
                    save_source_xml = false;
                }
            }
            
            // source_xml_directory
            if (key.equals("source-xml-directory")) {
                if ((newDirectory = Util.uriToFile(value)) != null) {
                    source_xml_directory = newDirectory;
                }
            }
            
            // source_xml_extension
            if (key.equals("source-xml-extension")) {
                source_xml_extension = value;
            }
            
            // untouch_pixel_data
            if (key.equals("untouch-pixel-data")) {
                if (value.toLowerCase().charAt(0) == 't') {
                    untouch_pixel_data = true;
                } else {
                    untouch_pixel_data = false;
                }
            }
            
            // transform
            if (key.equals("transform")) {
                if (value.toLowerCase().charAt(0) == 't') {
                    transform = true;
                } else {
                    transform = false;
                }
            }
            
            // xsl_directory
            if (key.equals("xsl-directory")) {
                if ((newDirectory = Util.uriToFile(value)) != null) {
                    xsl_directory = newDirectory;
                }
            }
            
            // xsl_filename
            if (key.equals("xsl-filename")) {
                xsl_filename = value;
            }
            
            // save_transformed_xml
            if (key.equals("save-transformed-xml")) {
                if (value.toLowerCase().charAt(0) == 't') {
                    save_transformed_xml = true;
                } else {
                    save_transformed_xml = false;
                }
            }
            
            // reconvert_transformed_xml
            if (key.equals("reconvert-transformed-xml")) {
                if (value.toLowerCase().charAt(0) == 't') {
                    reconvert_transformed_xml = true;
                } else {
                    reconvert_transformed_xml = false;
                }
            }
            
            // transformed_xml_directory
            if (key.equals("transformed-xml-directory")) {
                if ((newDirectory = Util.uriToFile(value)) != null) {
                    transformed_xml_directory = newDirectory;
                }
            }
            
            // transformed_xml_extension
            if (key.equals("transformed-xml-extension")) {
                transformed_xml_extension = value;
            }
        }
    }
    
    /**
     * Sets dynamic properties, which depend on the actual Dataset to process.
     * @param ds  The Dataset to process.
     * @return True, if dynamic property could be set.
     */
    protected boolean setDynamicProperties(Dataset ds) {
        DcmElement element;
        int tag;
        String tagName;
        String key;
        String value;
        
        // Nur dann weitermachen, wenn ein Datset vorhanden ist
        if (ds == null) {
            log.warn("No Dataset given.");
            
            return false;
        }
        
        // Alle Properties bearbeiten
        for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
            // Key/Value Paar holen
            key = (String) en.nextElement();
            value = props.getProperty(key);
            
            // Source Filename
            if (key.equals("source-xml-filename")) {
                source_xml_filename = Util.getTagStringOrValue(value, ds);
                if (source_xml_filename == null) {
                    log.error("Unknown tag in property: " + value);
                    return false;
                }
            }
            
            // Transformed Filename
            if (key.equals("transformed-xml-filename")) {
                transformed_xml_filename = Util.getTagStringOrValue(value, ds);
                if (transformed_xml_filename == null) {
                    log.error("Unknown tag in property: " + value);
                    return false;
                }
            }
        }
        
        // Kein schwerwiegender Fehler aufgetreten
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
    protected int process(Dataset dataset) {
        File sourceXmlFile = null;
        ByteArrayInputStream sourceXmlIS = null;
        ByteArrayOutputStream sourceXmlOS = null;
        FileOutputStream sourceXmlFOS = null;
        File transformedXmlFile = null;
        ByteArrayInputStream transformedXmlIS = null;
        ByteArrayOutputStream transformedXmlOS = null;
        FileOutputStream transformedXmlFOS = null;
        Dataset sourceDS = null;
        Dataset transformedDS = null;
        DcmElement pixelData = null;
        
        // Nur dann weitermachen, wenn ein Datset vorhanden ist
        if (dataset == null) {
            log.warn("No Dataset given.");
            
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Dynamische Properties setzen
        if (!setDynamicProperties(dataset)) {
            log.error("Dataset not processed.");
            
            return REQUEST_ABORT_RECEIVER;
        }
        
        // Zunaechst eine Kopie des Datset anlegen
        sourceDS = DcmObjectFactory.getInstance().newDataset();
        sourceDS.putAll(dataset);
        
        // Elemente und zugehoerige Gruppenlaengenelemente von solchen Elementen
        // loeschen, die nur zur Struktiur des gespeicherten Dataset dienen
        sourceDS.remove(Tags.DataSetTrailingPadding);
        sourceDS.remove(Tags.DataSetTrailingPadding & 0xFFFF0000);
        
        sourceDS.remove(Tags.Item);
        sourceDS.remove(Tags.Item & 0xFFFF0000);
        sourceDS.remove(Tags.ItemDelimitationItem);
        sourceDS.remove(Tags.SeqDelimitationItem);
        
        // Ggf. Pixeldaten nicht bearbeiten. Pixeldaten sichern
        if (untouch_pixel_data) {
            pixelData = sourceDS.get(Tags.PixelData);
            sourceDS.remove(Tags.PixelData);
        }
        
        try {
            // Dataset in XML konvertieren
            sourceXmlOS = datasetToXML(sourceDS);
            
            // Bei Fehler Abbruch
            if (sourceXmlOS == null) {
                return REQUEST_ABORT_RECEIVER;
            }
            
            // Die XML Darstellung des unbearbeiteten Dataset als File abspeichern.
            if (save_source_xml) {
                try {
                    // File erzeugen
                    sourceXmlFile = new File(source_xml_directory,
                            source_xml_filename + source_xml_extension);
                    
                    // FileOutputStream erzeugen
                    sourceXmlFOS = new FileOutputStream(sourceXmlFile);
                    
                    // XML file schreiben
                    sourceXmlFOS.write(sourceXmlOS.toByteArray());
                    
                    // Info-Meldung
                    if (log.isInfoEnabled()) {
                        log.info("Source XML saved as file: " + sourceXmlFile);
                    }
                } catch (IOException ioEx) {
                    log.error("Can't write to source_xml_file : " +
                            ioEx.getMessage());
                    
                    return REQUEST_ABORT_RECEIVER;
                }
            }
            
            // Falls keine Transformation gewuenscht wird beenden.
            if (!transform) {
                // Ohne Fehler beendet, Dataset  n i c h t  ersetzt
                return CONTINUE;
            }
            
            // XML Output Stream in einen Input Stream konvertieren
            sourceXmlIS = new ByteArrayInputStream(sourceXmlOS.toByteArray());
            
            // XML mit XSL transformieren
            transformedXmlOS = transformXML(sourceXmlIS);
            
            // Bei Fehler Abbruch
            if (transformedXmlOS == null) {
                return REQUEST_ABORT_RECEIVER;
            }
            
            // Die XML Darstellung der transformierten XML Darstellung als File abspeichern.
            if (save_transformed_xml) {
                try {
                    // File erzeugen
                    transformedXmlFile = new File(transformed_xml_directory,
                            transformed_xml_filename + transformed_xml_extension);
                    
                    // FileOutputStream erzeugen
                    transformedXmlFOS = new FileOutputStream(transformedXmlFile);
                    
                    // XML file schreiben
                    transformedXmlFOS.write(transformedXmlOS.toByteArray());
                    
                    // Info-Meldung
                    if (log.isInfoEnabled()) {
                        log.info("Transformed XML saved as file: " +
                                transformedXmlFile);
                    }
                } catch (IOException ioEx) {
                    log.error("Can't write to transformed_xml_file : " +
                            ioEx.getMessage());
                    
                    return REQUEST_ABORT_RECEIVER;
                }
            }
            
            //
            if (reconvert_transformed_xml) {
                // XML Output Stream in einen Input Stream konvertieren
                transformedXmlIS = new ByteArrayInputStream(transformedXmlOS.toByteArray());
                
                // XML in Dataset transformieren
                transformedDS = xmlToDataset(transformedXmlIS);
                
                if (transformedDS != null) {
                    
                    // Source Dataset mit neuem Inhalt fuellen
                    dataset.clear();
                    dataset.putAll(transformedDS);
                    
                    // Falls die Pixeldaten nicht bearbeitet wurden, diese hinzufuegen
                    if (pixelData != null) {
                        dataset.putXX(pixelData.tag(), pixelData.vr(), pixelData.getByteBuffer());
                    }
                    
                    // Info-Meldung
                    if (log.isInfoEnabled()) {
                        log.info("Transformed XML reconverted to dataset.");
                    }
                    
                    // Ohne Fehler beendet, Dataset ersetzt
                    return CONTINUE;
                } else {
                    // Konvertierungsfehler, Dataset  n i c h t  ersetzt
                    // Fehlerlog wird in xmlToDataset erzeugt
                    
                    return REQUEST_ABORT_RECEIVER;
                }
            }
            
            // Ohne Fehler beendet, Dataset  n i c h t  ersetzt
            return CONTINUE;
        } finally {
            // Alle Streams schliessen
            try {
                if (sourceXmlIS != null) {
                    sourceXmlIS.close();
                }
            } catch (IOException ignore) {
            }
            
            try {
                if (sourceXmlOS != null) {
                    sourceXmlOS.close();
                }
            } catch (IOException ignore) {
            }
            
            try {
                if (sourceXmlFOS != null) {
                    sourceXmlFOS.close();
                }
            } catch (IOException ignore) {
            }
            
            try {
                if (transformedXmlIS != null) {
                    transformedXmlIS.close();
                }
            } catch (IOException ignore) {
            }
            
            try {
                if (transformedXmlOS != null) {
                    transformedXmlOS.close();
                }
            } catch (IOException ignore) {
            }
            
            try {
                if (transformedXmlFOS != null) {
                    transformedXmlFOS.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
    
    /**
     * Transforms a given Dataset to a XML byte stream.
     *
     * @param ds The Dataset to convert.
     *
     * @return The XML byte stream. null, if an error occured.
     */
    protected ByteArrayOutputStream datasetToXML(Dataset ds) {
        ByteArrayOutputStream dicomByteArrayOS = null;
        ByteArrayInputStream dicomByteArrayIS = null;
        ByteArrayOutputStream sourceXmlOS = null;
        DcmEncodeParam param = null;
        DcmParser    parser;
        TagDictionary     dict;
        
        try {
            // Encode Parameter fuer das Dataset entsprechend der
            // ImplicitVRLittleEndian Transfersyntax
            param = DcmEncodeParam.valueOf(UIDs.ImplicitVRLittleEndian);
            
            // Datset als ByteArray in einen Input und Output Stream schreiben
            dicomByteArrayOS = new ByteArrayOutputStream();
            ds.writeDataset(dicomByteArrayOS, param);
            dicomByteArrayIS = new ByteArrayInputStream(dicomByteArrayOS.toByteArray());
            
            // Eine konkrete Instance eines DcmParser generieren
            parser = DcmParserFactory.getInstance().newDcmParser(dicomByteArrayIS);
            
            // Den Output Stream fuer das unbearbeitete XML Dokument erzeugen
            sourceXmlOS = new ByteArrayOutputStream();
            
            // Das Default Dictionary fuer Tags verwenden
            dict = DictionaryFactory.getInstance().getDefaultTagDictionary();
            
            // Der DcmParser leitet aus dem SAX-ContentHandler einen DcmHandlerAdapter
            // ab. Dieser hat den Aufbau eines ContentHandlers. Die Methoden werden
            // aber nicht von einem SAX-Parser aufgerufen, sondern von der Methode
            // DcmParser.doParse(). Damit verhaelt sich der DcmParser im XML-Umfeld
            // wie ein Parser, der ein XML-Dokument parst.
            // getTransformerHandler(out) liefert den eigentlichen ContentHandler
            // dict gibt das zu verwendende Dictionary vor
            // excludeTags null keine Tags ausschliessen
            // basedir null keine Tags zwischenspeichern
            
            // In dcm4che 1.3.22 API change:
            // DcmParser::setSAXHandler2 has additional parameter int excludeValueLengthLimit
            //   - Exclude values which length exceeds the specified limit from XML output.
            // We do not exclude values, therefore excludeValueLengthLimit = Integer.MAX_VALUE
            // for example usage see sample Dcm2Xml.
            // parser.setSAXHandler2(getTransformerHandler(sourceXmlOS), dict, null, null);
            parser.setSAXHandler2(getTransformerHandler(sourceXmlOS), dict, null, Integer.MAX_VALUE, null);
            
            // Alle Elemente bearbeiten
            int stop = -1;
            
            parser.parseDataset(param, stop);
            
        } catch (TransformerConfigurationException transConfigEx) {
            log.error("Error configuring the parser: " +
                    transConfigEx.getMessage());
            
            return null;
        } catch (TransformerException transEx) {
            log.error("Parsing error: " + transEx.getMessage());
            
            return null;
        } catch (FileNotFoundException fnfEx) {
            log.error("File not found error: " + fnfEx.getMessage());
            
            return null;
        } catch (IOException ioEx) {
            log.error("IO Exception: " + ioEx.getMessage());
            
            return null;
        } finally {
            try {
                if (sourceXmlOS != null) {
                    sourceXmlOS.close();
                }
            } catch (IOException ignore) {
            }
            
            try {
                if (dicomByteArrayIS != null) {
                    dicomByteArrayIS.close();
                }
            } catch (IOException ignore) {
            }
            
            try {
                if (dicomByteArrayOS != null) {
                    dicomByteArrayOS.close();
                }
            } catch (IOException ignore) {
            }
        }
        
        // Ohne Fehler beendet
        return sourceXmlOS;
    }
    
    /**
     * Transforms a given XML document using a XSL file.
     *
     * @param sourceXmlIS The byte stream of the XML document to transform.
     *
     * @return The transformed XML byte stream. null, if an error occured.
     */
    protected ByteArrayOutputStream transformXML(
            ByteArrayInputStream sourceXmlIS) {
        File xslFile = null;
        Source xslSource = null;
        Source xmlSource = null;
        Result xmlTransformed = null;
        ByteArrayOutputStream transformedXmlOS = null;
        Transformer transformer;
        
        try {
            // XSL File als Source definieren
            xslFile = new File(xsl_directory, xsl_filename);
            xslSource = new StreamSource(xslFile);
            
            // Transformer festlegen
            transformer = TransformerFactory.newInstance().newTransformer(xslSource);
            
            // Source definieren
            xmlSource = new StreamSource(sourceXmlIS);
            
            // Result definieren
            transformedXmlOS = new ByteArrayOutputStream();
            xmlTransformed = new StreamResult(transformedXmlOS);
            
            // Set an output property that will be in effect for the transformation:
            // indent specifies whether the Transformer may add additional whitespace
            // when outputting the result tree; the value must be yes or no.
            // transformer.setOutputProperty(OutputKeys.INDENT,"yes");
            // The method attribute identifies the overall method that should be used
            // for outputting the result tree; the value must be "xml" or "html" or
            // "text" or expanded name.
            // transformer.setOutputProperty(OutputKeys.METHOD,"xml");
            // Mit XSLT transformieren
            transformer.transform(xmlSource, xmlTransformed);
            
            // Info-Meldung
            if (log.isInfoEnabled()) {
                log.info("Source XML successful transformed with XSL file: " +
                        xslFile);
            }
        } catch (TransformerConfigurationException tcEx) {
            log.error("Can't find XSL file: " + tcEx.getMessage());
            
            return null;
        } catch (TransformerException transEx) {
            log.error("XSL transformation error: " + transEx.getMessage());
            
            return null;
        } finally {
            try {
                if (transformedXmlOS != null) {
                    transformedXmlOS.close();
                }
            } catch (IOException ignore) {
            }
        }
        
        // Ohne Fehler beendet
        return transformedXmlOS;
    }
    
    /**
     * Converts a XML Stream to a Dataset.
     *
     * @param xmlIS The XML Stream to convert.
     *
     * @return The Dataset. null, if an error occured.
     */
    protected Dataset xmlToDataset(ByteArrayInputStream xmlIS) {
        SAXParser p = null;
        Dataset ds;
        
        // Leeres Dataset erzeugen
        ds = DcmObjectFactory.getInstance().newDataset();
        
        // Character Set für Westeuropa ISO-8859-1 festlegen
        // Anderenfalls wuerden deutsche Umlaute zu einer IllegalArgumentException
        // beim Befuellen des Dataset fuehren
        ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
        
        try {
            // Parser Objekt erzeugen
            p = SAXParserFactory.newInstance().newSAXParser();
            
            // Den XML Stream mit dem Parser des Dataset parsen
            p.parse(xmlIS, ds.getSAXHandler2(null));
        } catch (FactoryConfigurationError fcEx) {
            log.error("Can't convert XML to Dataset" + fcEx.getMessage());
            
            return null;
        } catch (ParserConfigurationException pcEx) {
            log.error("Can't convert XML to Dataset" + pcEx.getMessage());
            
            return null;
        } catch (DcmValueException dvEx) {
            log.error("Can't convert XML to Dataset" + dvEx.getMessage());
            
            return null;
        } catch (SAXException sEx) {
            log.error("Can't convert XML to Dataset" + sEx.getMessage());
            
            return null;
        } catch (IOException ioEx) {
            log.error("Can't convert XML to Dataset" + ioEx.getMessage());
            
            return null;
        } finally {
            try {
                if (xmlIS != null) {
                    xmlIS.close();
                }
            } catch (IOException ignore) {
            }
        }
        
        // Ohne Fehler beendet
        return ds;
    }
    
    /**
     * This method returns a non-validating ContentHandler. It converts the
     * XML input into a well-formed XML document in the outStream. Each element
     * starts in a new line.
     * A TransformerHandler listens for SAX ContentHandler parse events and
     * transforms them to a Result. [javax.xml.transformer.sax.TransformerHandler
     * extends org.xml.sax.ContentHandler]
     *
     * @param xmlStream The XML stream.
     * @return The content handler.
     */
    private TransformerHandler getTransformerHandler(OutputStream xmlStream)
    throws TransformerConfigurationException {
        SAXTransformerFactory saxTF;
        TransformerHandler transformerHandler;
        Transformer transformer;
        Source xsltSource = null;
        Result result;
        
        // Ergebnis in den Stream outStream schreiben
        result = new StreamResult(xmlStream);
        
        // Eine konkrete Instance einer SAXTransformerFactory erzeugen
        // [javax.xml.transform.TransformerFactory]
        // [javax.xml.transform.sax.SAXTransformerFactory]
        saxTF = (SAXTransformerFactory) TransformerFactory.newInstance();
        
        if (xsltSource == null) {
            // Get a TransformerHandler object that can process SAX ContentHandler
            // events into a Result. The transformation is defined as an identity
            // (or copy) transformation, for example to copy a series of SAX parse
            // events into a DOM tree. [javax.xml.transform.sax.SAXTransformerFactory]
            transformerHandler = saxTF.newTransformerHandler();
        } else {
            // Get a TransformerHandler object that can process SAX ContentHandler
            // events into a Result, based on the transformation instructions
            // specified by the argument, e.g. a XSLT styleshet.
            // [javax.xml.transform.sax.SAXTransformerFactory]
            transformerHandler = saxTF.newTransformerHandler(xsltSource);
        }
        
        // Der transformerHandler ist eine Instanz der Default-Implementation
        // des org.xml.sax.ContentHandler
        // Get the Transformer associated with this handler, which is needed in
        // order to set parameters and output properties.
        // [javax.xml.transform.Transformer]
        transformer = transformerHandler.getTransformer();
        
        // Set an output property that will be in effect for the transformation:
        // indent specifies whether the Transformer may add additional whitespace
        // when outputting the result tree; the value must be yes or no.
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        
        // The method attribute identifies the overall method that should be used
        // for outputting the result tree; the value must be "xml" or "html" or
        // "text" or expanded name.
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        
        // Enables the user of the TransformerHandler to set the to set the result
        // Result result) for the transformation.
        transformerHandler.setResult(result);
        
        return transformerHandler;
    }
}

/**
 * Revisions:
 *
 * 2006.04.24, hacklaender: Property source_xml_name replaced by source_xml_filename
 * 2006.04.24, hacklaender: Property transformed_xml_name replaced by transformed_xml_filename
 * 2006.04.24, hacklaender: Property xsl_name replaced by xsl_filename
 * 2004.11.17, hacklaender: Adapted to the new XML schema with attribute values as text nodes.
 */
