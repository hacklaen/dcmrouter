/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The initial developer of the original code is
 * Dr. Thomas Hacklaender
 * Copyright (C) 2006. All rights reserved.
 *
 * Contributor(s):
 * Dr. Thomas Hacklaender <thomas.hacklaender@iftm.de>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 * ***** END LICENSE BLOCK ***** */

package de.iftm.dcm4che.router.plugin;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.apache.log4j.*;


/**
 * This class draws annotations on an Graphics2D Object.
 *
 * @author Thomas Hacklaender
 * @version 2006.11.11
 */
public class Annotation {
    
    /** Initialize logger */
    private static Logger log = Logger.getLogger(Annotation.class);
    
    
    /** Value of field type: Don't draw any annotations. */
    public static final int ANNOTATION_OFF = 0;
    
    /** Value of field type: Draw all annotations. */
    public static final int ANNOTATION_FULL = 1;
    
    /** Value of field type: Draw minimal set of annotations. */
    public static final int ANNOTATION_MINIMAL = 2;
    
    /** Value of field type: Draw all annotation besides patient name,
     * patient birth date and patient ID. */
    public static final int ANNOTATION_ANONYMOUS = 3;
    
    /** Value of field type: Draw all annotation besides patient birth
     * date and patient ID. Replace the patient name with a pseudonym. */
    public static final int ANNOTATION_PSEUDONYM = 4;
    
    /** Value of the row-parameter of method drawString. */
    public static final int TOP = 0;
    
    /** Value of the row-parameter of method drawString. */
    public static final int CENTER = 1;
    
    /** Value of the row-parameter of method drawString. */
    public static final int BOTTOM = 2;
    
    /** Value of the column-parameter of method drawString. */
    public static final int LEFT = 0;
    
    /** Value of the column-parameter of method drawString. */
    public static final int MIDDLE = 1;
    
    /** Value of the column-parameter of method drawString. */
    public static final int RIGHT = 2;
    
    /** Type of image annotation. */
    private int type = ANNOTATION_FULL;
    
    /** The margin for image the in pixel. */
    private int marginSize = 4;
    
    /** The font size for the annotation. */
    private int fontSize = 11;
    
    /** The name of the font for the annotation. */
    private String fontName = "Dialog";
    
    /** The color of the font for the annotation. */
    private Color fontColor = Color.orange;
    
    /** The Graphic2D.*/
    private Graphics2D g2D = null;
    
    /** The width of the Graphic2D.*/
    private int g2dWidth;
    
    /**  The height of the Graphic2D.*/
    private int g2dHeight;
    
    /** The Dataset. */
    private Dataset ds = null;
    
    
    /**
     * Creates a new Annotation object.
     *
     * @param theImage the image to draw the annotation onto.
     * @param theDataset the Dataset containing the metainforamtion of the annotations.
     */
    public Annotation(BufferedImage theImage, Dataset theDataset) {
        this(theImage.createGraphics(), theImage.getWidth(), theImage.getHeight(), theDataset);
    }
    
    
    /**
     * Creates a new Annotation object.
     *
     * @param theG2D the graphic context to draw the annotation onto.
     * @param width the width of the graphic context.
     * @param height the height of the graphic context.
     * @param theDataset the Dataset containing the metainforamtion of the annotations.
     */
    public Annotation(Graphics2D theG2D, int width, int height, Dataset theDataset) {
        g2D = theG2D;
        g2dWidth = width;
        g2dHeight = height;
        ds = theDataset;
    }
    
    /**
     * Set the type of annotation.
     * <p><CODE>ANNOTATION_OFF</CODE> = do not display any annotations.
     * <p><CODE>ANNOTATION_FULL</CODE> = display all possible annotation.
     * <p><CODE>ANNOTATION_MINIMAL</CODE> = displays only a minimal set of annotations.
     * <p><CODE>ANNOTATION_ANONYMOUS</CODE> = display all possible annotation, but the patients name.
     * <p><CODE>ANNOTATION_PSEUDONYM</CODE> = display all possible annotation, but uses a pseudonym for the patients name.
     *
     * @param theType the type of annotation.
     */
    public void setType(int theType) {
        type = theType;
    }
    
    
    /**
     * Gets the actual annotation type.
     *
     * @return the actual annotation type.
     */
    public int getType() {
        return type;
    }
    
    
    /**
     * Sets the margin inside the image where no inforamtion should be drawn.
     *
     * @param theSize the margin.
     */
    public void setMarginSize(int theSize) {
        marginSize = theSize;
    }
    
    
    /**
     * Gets the margin inside the image where no inforamtion should be drawn.
     *
     * @return the margin.
     */
    public int getMarginSize() {
        return marginSize;
    }
    
    
    /**
     * Set the size of the font to draw with.
     *
     * @param theSize the size of the font.
     */
    public void setFontSize(int theSize) {
        fontSize = theSize;
    }
    
    
    /**
     * Gets the size of the font to draw with.
     *
     * @return the size of the font.
     */
    public int getFontSize() {
        return fontSize;
    }
    
    
    /**
     * Set the name of the font to draw with.
     *
     * @param theName the name of the font.
     */
    public void setFontName(String theName) {
        fontName = theName;
    }
    
    
    /**
     * Gets the name of the font to draw with.
     *
     * @return the name of the font.
     */
    public String getFontName() {
        return fontName;
    }
    
    
    /**
     * Set the color of the font to draw with.
     *
     * @param theColor the color of the font.
     */
    public void setFontColor(Color theColor) {
        fontColor = theColor;
    }
    
    
    /**
     * Gets the color of the font to draw with.
     *
     * @return the color of the font.
     */
    public Color getFontColor() {
        return fontColor;
    }
    
    
    /**
     * Draws annotation onto the Graphics2D .
     */
    public void draw() {
        String[] sa;
        String   sopClassUID;
        
        //  Fuer Bilder ohne Dataset nichts tun
        if ((ds == null) || (g2D == null)) {
            return;
        }
        
        // ANNOTATION_OFF: Keine Annotationen schreiben
        if (type == ANNOTATION_OFF) {
            return;
        }
        
        // C.12.1 SOP Common Module
        sopClassUID = ds.getString(Tags.SOPClassUID, "");
        
        drawGeneralAnnotation();
        
        if (sopClassUID.equals(UIDs.CTImageStorage)) {
            drawCTAnnotation();
        }
        
        if (sopClassUID.equals(UIDs.MRImageStorage)) {
            drawMRAnnotation();
        }
        
        if (sopClassUID.equals(UIDs.ComputedRadiographyImageStorage)) {
            drawCRAnnotation();
        }
    }
    
    
    /**
     * Draws annotation common for all modalities onto the BufferedImage stored
     * in the local field image.
     */
    private void drawGeneralAnnotation() {
        String acquisitionDate = "";
        String acquisitionTime = "";
        String columns = "";
        String instanceNumber = "";
        String institutionName = "";
        String patientBirthDate = "";
        String patientID = "";
        String patientName = "";
        String rows = "";
        String seriesNumber = "";
        String studyID = "";
        
        // C.7.1.1 Patient Module
        patientName = ds.getString(Tags.PatientName, ""); // Type 2
        patientID = ds.getString(Tags.PatientID, ""); // Type 2
        patientBirthDate = formatDate(ds.getString(Tags.PatientBirthDate, "")); // Type 2
        
        // C.7.2.1 General Study Module
        studyID = ds.getString(Tags.StudyID, ""); // Type 2
        
        // C.7.3.1 General Series Module
        seriesNumber = ds.getString(Tags.SeriesNumber, ""); // Type 2
        
        // C.7.5.1 General Equipment Module
        institutionName = ds.getString(Tags.InstitutionName, ""); // Type 3
        
        // C.7.6.1 General Image Module
        instanceNumber = ds.getString(Tags.InstanceNumber, ""); // Typ 1
        acquisitionDate = formatDate(ds.getString(Tags.AcquisitionDate, "")); // Typ 3
        acquisitionTime = formatTime(ds.getString(Tags.AcquisitionTime, "")); // Typ 3
        
        // C.7.6.3 Image Pixel Module
        rows = ds.getString(Tags.Rows, ""); // Typ 1
        columns = ds.getString(Tags.Columns, ""); // Typ 1
        
        switch (type) {
            case ANNOTATION_FULL:
                drawString(patientName, TOP, LEFT, 1);
                drawString("DoB: " + patientBirthDate, TOP, LEFT, 2);
                drawString("ID: " + patientID, TOP, LEFT, 3);
                drawString(acquisitionDate, TOP, LEFT, 5);
                drawString(acquisitionTime, TOP, LEFT, 6);
                drawString("Ima: " + seriesNumber + " / " + instanceNumber, TOP, LEFT, 7);
                
                drawString("FoV: " + rows + "/" + columns, BOTTOM, RIGHT, 1);
                
                break;
                
            case ANNOTATION_MINIMAL:
                drawString("Ima: " + seriesNumber + " / " + instanceNumber, BOTTOM, LEFT, 1);
                
                break;
                
            case ANNOTATION_ANONYMOUS:
                drawString(acquisitionDate, TOP, LEFT, 1);
                drawString(acquisitionTime, TOP, LEFT, 2);
                drawString("Ima: " + seriesNumber + " / " + instanceNumber, TOP, LEFT, 3);
                
                drawString("FoV: " + rows + "/" + columns, BOTTOM, RIGHT, 1);
                
                break;
                
            case ANNOTATION_PSEUDONYM:
                drawString(createPseudonym(ds), TOP, LEFT, 1);
                drawString(acquisitionDate, TOP, LEFT, 3);
                drawString(acquisitionTime, TOP, LEFT, 4);
                drawString("Ima: " + seriesNumber + " / " + instanceNumber, TOP, LEFT, 5);
                
                drawString("FoV: " + rows + "/" + columns, BOTTOM, RIGHT, 1);
                
                break;
        }
    }
    
    
    /**
     * Draws annotation specific for the CT modality onto the BufferedImage
     * stored in the local field image.
     */
    private void drawCTAnnotation() {
        String[] sa;
        String   convolutionKernel = "";
        String   exposure = "";
        String   gantryDetectorTilt = "";
        String   kvp = "";
        String   patientPosition = "";
        String   pixelSpacing = "";
        String   sliceLocation = "";
        String   sliceThickness = "";
        String   windowCenter = "";
        String   windowWidth = "";
        
        // Es handelt sich um ein CT Bild
        // In Common Series IE Module/ C.7.3.1 General Series Module
        // Mit Type 2C fuer CT und MR vorgeschrieben
        patientPosition = ds.getString(Tags.PatientPosition, "");
        
        // In A.3 und A.4 vorgeschrieben: C.7.6.2 Image Plane Module
        sa = ds.getStrings(Tags.PixelSpacing); // Typ 1
        
        if ((sa != null) && (sa.length == 2)) {
            pixelSpacing = formatDouble(sa[0], 2) + "/" + formatDouble(sa[1], 2);
        }
        
        sliceThickness = formatDouble(ds.getString(Tags.SliceThickness, ""), 1); // Typ 2
        sliceLocation = formatDouble(ds.getString(Tags.SliceLocation, ""), 1); // Typ 3
        
        kvp = ds.getString(Tags.KVP, "");
        exposure = ds.getString(Tags.Exposure);
        convolutionKernel = ds.getString(Tags.ConvolutionKernel, "");
        gantryDetectorTilt = ds.getString(Tags.GantryDetectorTilt, "");
        
        // In A.3 und A.4 User Option: C.11.2 VOI LUT Module
        windowCenter = formatDouble(ds.getString(Tags.WindowCenter, ""), 0); // Typ 3
        windowWidth = formatDouble(ds.getString(Tags.WindowWidth, ""), 0); // Typ 3
        
        switch (type) {
            case ANNOTATION_FULL:
            case ANNOTATION_ANONYMOUS:
            case ANNOTATION_PSEUDONYM:
                drawString("C: " + windowCenter, BOTTOM, RIGHT, 6);
                drawString("W: " + windowWidth, BOTTOM, RIGHT, 5);
                drawString("SL: " + sliceThickness, BOTTOM, RIGHT, 4);
                drawString("SP: " + sliceLocation, BOTTOM, RIGHT, 3);
                drawString("Pix: " + pixelSpacing, BOTTOM, RIGHT, 2);
                
                drawString("GT: " + gantryDetectorTilt, BOTTOM, LEFT, 4);
                drawString("kV: " + kvp, BOTTOM, LEFT, 3);
                drawString("mAs: " + exposure, BOTTOM, LEFT, 2);
                drawString(convolutionKernel, BOTTOM, LEFT, 1);
                
                drawString(patientPosition, BOTTOM, MIDDLE, 1);
                
                break;
                
            case ANNOTATION_MINIMAL:
                drawString(patientPosition, BOTTOM, RIGHT, 1);
                
                break;
        }
    }
    
    
    /**
     * Draws annotation specific for the MR modality onto the BufferedImage
     * stored in the local field image.
     */
    private void drawMRAnnotation() {
        String[] sa;
        String   echoTime = "";
        String   echoTrainLength = "";
        String   flipAngle = "";
        String   inversionTime = "";
        String   mrAcquisitionType = "";
        String   patientPosition = "";
        String   pixelSpacing = "";
        String   repetitionTime = "";
        String   scanningSeq = "";
        String   sequenceName = "";
        String   seqVariant = "";
        String   sliceLocation = "";
        String   sliceThickness = "";
        String   windowCenter = "";
        String   windowWidth = "";
        
        // Es handelt sich um ein CMR Bild
        // In Common Series IE Module/ C.7.3.1 General Series Module
        // Mit Type 2C fuer CT und MR vorgeschrieben
        patientPosition = ds.getString(Tags.PatientPosition, "");
        
        // In A.3 und A.4 vorgeschrieben: C.7.6.2 Image Plane Module
        sa = ds.getStrings(Tags.PixelSpacing); // Typ 1
        
        if ((sa != null) && (sa.length == 2)) {
            pixelSpacing = formatDouble(sa[0], 2) + "/" + formatDouble(sa[1], 2);
        }
        
        sliceThickness = formatDouble(ds.getString(Tags.SliceThickness, ""), 1); // Typ 2
        sliceLocation = formatDouble(ds.getString(Tags.SliceLocation, ""), 1); // Typ 3
        
        // C.8.3.1 MR Imager Module
        scanningSeq = ds.getString(Tags.ScanningSeq, "");
        seqVariant = ds.getString(Tags.SeqVariant, "");
        mrAcquisitionType = ds.getString(Tags.MRAcquisitionType, "");
        flipAngle = formatDouble(ds.getString(Tags.FlipAngle, ""), 0);
        repetitionTime = formatDouble(ds.getString(Tags.RepetitionTime, ""), 0);
        echoTime = formatDouble(ds.getString(Tags.EchoTime, ""), 0);
        inversionTime = formatDouble(ds.getString(Tags.InversionTime, ""), 0);
        sequenceName = ds.getString(Tags.SequenceName, "");
        echoTrainLength = ds.getString(Tags.EchoTrainLength, "");
        
        // In A.3 und A.4 User Option: C.11.2 VOI LUT Module
        windowCenter = formatDouble(ds.getString(Tags.WindowCenter, ""), 0); // Typ 3
        windowWidth = formatDouble(ds.getString(Tags.WindowWidth, ""), 0); // Typ 3
        
        switch (type) {
            case ANNOTATION_FULL:
            case ANNOTATION_ANONYMOUS:
            case ANNOTATION_PSEUDONYM:
                drawString("C: " + windowCenter, BOTTOM, RIGHT, 6);
                drawString("W: " + windowWidth, BOTTOM, RIGHT, 5);
                drawString("SL: " + sliceThickness, BOTTOM, RIGHT, 4);
                drawString("SP: " + sliceLocation, BOTTOM, RIGHT, 3);
                drawString("Pix: " + pixelSpacing, BOTTOM, RIGHT, 2);
                
                drawString("TR: " + repetitionTime, BOTTOM, LEFT, 7);
                drawString("TE: " + echoTime, BOTTOM, LEFT, 6);
                drawString("ETL: " + echoTrainLength, BOTTOM, LEFT, 5);
                drawString("TI: " + inversionTime, BOTTOM, LEFT, 4);
                drawString("Flp: " + flipAngle, BOTTOM, LEFT, 3);
                drawString("Seq: " + scanningSeq + "/" + seqVariant + "/" + mrAcquisitionType, BOTTOM, LEFT, 2);
                drawString(sequenceName, BOTTOM, LEFT, 1);
                
                drawString(patientPosition, BOTTOM, MIDDLE, 1);
                
                break;
                
            case ANNOTATION_MINIMAL:
                drawString(patientPosition, BOTTOM, RIGHT, 1);
                
                break;
        }
    }
    
    
    /**
     * Draws annotation specific for the CR modality onto the BufferedImage
     * stored in the local field image.
     */
    private void drawCRAnnotation() {
        String[] sa;
        String   imagerPixelSpacing = "";
        
        // Es handelt sich um ein CR Bild
        // C.8.3.1 CR Image Module
        sa = ds.getStrings(Tags.ImagerPixelSpacing); // Typ 3
        
        if ((sa != null) && (sa.length == 2)) {
            imagerPixelSpacing = formatDouble(sa[0], 2) + "/" + formatDouble(sa[1], 2);
        }
        
        switch (type) {
            case ANNOTATION_FULL:
            case ANNOTATION_ANONYMOUS:
            case ANNOTATION_PSEUDONYM:
                drawString("Pix: " + imagerPixelSpacing, BOTTOM, RIGHT, 2);
                
                break;
                
            case ANNOTATION_MINIMAL:
                break;
        }
    }
    
    
    /**
     * Draws a string as annotation onto the BufferedImage stored in the local
     * field image. The image is devided in 9 areas (3 rows, 3 columns). The
     * string is placed at the position row/column at the given text line,
     * starting with 1. The text is left/center/right justified depending on the
     * position it is placed.
     *
     * @param s The string to draw.
     * @param row The row of the area where the string should be drawn.
     * @param column The column of the area where the string should be drawn.
     * @param line The text line in the area where the string should be drawn.
     */
    private void drawString(String s, int row, int column, int line) {
        int         strWidth;
        int         fontAscent;
        int         fontDescent;
        int         fontHeight;
        FontMetrics metrics;
        
        // Nur dann etwas tun, wenn ein Graphics2D Kontext vorhanden ist
        if (g2D == null) {
            return;
        }
        
        if (s == null) {
            return;
        }
        
        if (s.equals("")) {
            return;
        }
        
        g2D.setFont(new Font(fontName, Font.PLAIN, fontSize));
        g2D.setColor(fontColor);
        
        metrics = g2D.getFontMetrics();
        strWidth = metrics.stringWidth(s);
        fontAscent = metrics.getAscent();
        fontDescent = metrics.getDescent();
        fontHeight = metrics.getHeight();
        
        switch (row) {
            case TOP:
                switch (column) {
                    case LEFT:
                        g2D.drawString(s, marginSize, marginSize + fontAscent + ((line - 1) * fontHeight));
                        
                        break;
                        
                    case MIDDLE:
                        g2D.drawString(s, (g2dWidth - strWidth) / 2, marginSize + fontAscent + ((line - 1) * fontHeight));
                        
                        break;
                        
                    case RIGHT:
                        g2D.drawString(s, g2dWidth - marginSize - strWidth, marginSize + fontAscent + ((line - 1) * fontHeight));
                        
                        break;
                }
                
                break;
                
            case CENTER:
                switch (column) {
                    case LEFT:
                        g2D.drawString(s, marginSize, (g2dHeight / 2) + ((line - 1) * fontHeight));
                        
                        break;
                        
                    case MIDDLE:
                        g2D.drawString(s, (g2dWidth - strWidth) / 2, (g2dHeight / 2) + ((line - 1) * fontHeight));
                        
                        break;
                        
                    case RIGHT:
                        g2D.drawString(s, g2dWidth - marginSize - strWidth, (g2dHeight / 2) + ((line - 1) * fontHeight));
                        
                        break;
                }
                
                break;
                
            case BOTTOM:
                switch (column) {
                    case LEFT:
                        g2D.drawString(s, marginSize, g2dHeight - marginSize - fontDescent - ((line - 1) * fontHeight));
                        
                        break;
                        
                    case MIDDLE:
                        g2D.drawString(s, (g2dWidth - strWidth) / 2, g2dHeight - marginSize - fontDescent - ((line - 1) * fontHeight));
                        
                        break;
                        
                    case RIGHT:
                        g2D.drawString(s, g2dWidth - marginSize - strWidth, g2dHeight - marginSize - fontDescent - ((line - 1) * fontHeight));
                        
                        break;
                }
                
                break;
        }
    }
    
    
    /**
     * Creates a pseudonym for the patient: It is a 9 character long sting. The
     * first character is the first character of the last name. The second is
     * the first character of the given name or '_', if not present. The third
     * character is allways '_'. The next 6 characters are the birth date of the
     * patient in the foramt yymmdd.
     *
     * @param ds The Dataset to process.
     *
     * @return The pseudonym.
     */
    protected String createPseudonym(Dataset ds) {
        String s;
        int    i;
        String pseudonym = "";
        String patientName;
        String patientBirthDate;
        
        // C.7.1.1 Patient Module
        if ((patientName = ds.getString(Tags.PatientName)) == null) {
            patientName = "Pseudonym";
        }
        
        if ((patientBirthDate = ds.getString(Tags.PatientBirthDate)) == null) {
            patientBirthDate = "20000101";
        }
        
        pseudonym += patientName.substring(0, 1);
        i = patientName.indexOf('^');
        
        if ((i != -1) & (patientName.length() > i)) {
            pseudonym += patientName.substring(i + 1, i + 2);
        } else {
            pseudonym += "_";
        }
        
        pseudonym += ("_" + patientBirthDate.substring(2, 8));
        
        return pseudonym;
    }
    
    
    /**
     * Formats a string representing a integer number. The resulting string
     * has the given number of digits with leading '0'.
     *
     * @param in The string to process.
     * @param n The number of digits.
     *
     * @return The formatted string.
     */
    protected String formatInteger(String in, int n) {
        int i;
        int end;
        
        if (in == null) {
            return "";
        }
        
        if (in.length() >= n) {
            return in.substring(in.length() - n, in.length());
        }
        
        while (in.length() < n) {
            in = "0" + in;
        }
        
        return in;
    }
    
    
    /**
     * Formats a string representing a floating point number. The resulting string
     * has the given number of decimal places. The floating point number is
     * roundet to that decimal places. If this decimal places is 0, the decimal
     * point is also ommitted. If the input string is null or empty the returned
     * string is the empty string.
     *
     * @param in The string to process.
     * @param n The number of decimal places.
     *
     * @return The formatted string.
     */
    protected String formatDouble(String in, int n) {
        double               d;
        DecimalFormat        df;
        DecimalFormatSymbols dfs;
        String               out;
        
        if (in == null) {
            return "";
        }
        
        try {
            d = (new Double(in)).doubleValue();
        } catch (NumberFormatException e) {
            return "";
        }
        
        // Keine Exponentialdarstellung
        df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(n);
        df.setMinimumFractionDigits(n);
        dfs = df.getDecimalFormatSymbols();
        
        // Dezimalseparator soll immer '.' sein
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        
        out = df.format(d);
        
        if (out.charAt(out.length() - 1) == '.') {
            return out.substring(0, out.length() - 1);
        } else {
            return out;
        }
    }
    
    
    /**
     * Formats a string representing a date of the format yymmdd or yyyymmdd. 
     * The resulting string has the format yy-mm-dd or yyyy-mm-dd.
     *
     * @param in The string to process.
     *
     * @return The formatted string.
     */
    static public String formatDate(String in) {
        if (in == null) {
            return "";
        }
        
        switch (in.length()) {
            case 6:
                return in.substring(0, 2) + "-" + in.substring(2, 4) + "-" + in.substring(4, 6);
                
            case 8:
                return in.substring(0, 4) + "-" + in.substring(4, 6) + "-" + in.substring(6, 8);
                
            default:
                return "";
        }
        
    }
    
    
    /**
     * Formats a string representing a time of the format hhmmss.sss. The resulting
     * string has the format hh:mm:ss.
     *
     * @param in The string to process.
     *
     * @return The formatted string.
     */
    static public String formatTime(String in) {
        if (in == null) {
            return "";
        }
        
        if (in.length() < 6) {
            return null;
        }
        
        return in.substring(0, 2) + ":" + in.substring(2, 4) + ":" + in.substring(4, 6);
    }
    
    
    /**
     * Formats a given string, that the resulting string only includes the characters
     * 0..9, A..Z and a..z. Other characters are replaced by the '_'character.
     *
     * @param s The stringto foramt.
     *
     * @return The resulting string.
     */
    protected String string2LetterDigit(String s) {
        int    n;
        char[] ca = s.toCharArray();
        
        for (int i = 0; i < ca.length; i++) {
            n = ca[i];
            
            if (((n >= 0x30) & (n <= 0x39)) | ((n >= 0x41) & (n <= 0x5A)) | ((n >= 0x61) & (n <= 0x7A))) {} else {
                ca[i] = '_';
            }
        }
        
        return new String(ca);
    }
}

// Change log:
// 2006.11.11 tha: Added empty string as default value to the methods Dataset.getString() 
// 2006.11.09 tha: Fixed a bug in drawXXAnnotation methods for sa == null.

