/*
 *  Implementation.java
 *
 *  Copyright (c) 2003, 2004, 2005 by
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
package de.iftm.dcm4che.router.util;

import de.iftm.dcm4che.router.property.*;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;

import org.apache.log4j.*;

import java.io.*;
import java.net.*;

/**
 * Global utilities.
 *
 * @author hacklaender
 * @version 2004.12.30
 */
public class Util {
    /** The logger for this class. */
    static Logger log = Logger.getLogger(Util.class);
    
    
    /** Project version */
    public static String VERSION = "3.3.0";
    
    
    /**
     *
     */
    public static String getCopyrightMessage() {
        String  message;
        
        message =  "Dicom Router version: " + Util.VERSION + "\n";
        message += "\n";
        message += "This program receives DICOM objects, processes them and sends them optionally\n";
        message += "to another DICOM node.\n";
        message += "\n\n";
        message += "Copyright (C) 2003 - 2006 by:\n";
        message += "Dr. Thomas Hacklaender @ IFTM Institut fuer Telematik in der Medizin GmbH\n";
        message += "VISUS Technology Transfer GmbH\n";
        message += "\n\n";
        message += "This program is free software; you can redistribute it and/or modify it under \n";
        message += "the terms of the GNU Lesser Public License as published by the Free Software \n";
        message += "Foundation; either version 2 of the License, or (at your option) any later version.\n";
        message += "\n";
        message += "This program is distributed in the hope that it will be useful, but WITHOUT ANY \n";
        message += "WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A \n";
        message += "PARTICULAR PURPOSE. See http://www.gnu.org/licenses/licenses.html for more details.\n";
        message += "\n\n";
        message += "Based on dcm4che library version: " + org.dcm4che.Implementation.getVersionName() + "\n";
        message += "\n\n";
        
        return message;
    }
    
    
    /**
     * Returns the tag described by a given value. If the value starts with the
     * '$' character it is a reference by name, if it starts with '@' it is a
     * reference by group/element.
     * @param value the property value.
     * @return the tag. 0, if value does not begin with '$' or '@'. -1, if unknown tag.
     */
    public static int getTagFromPropertyString(String value) {
        final int       NO_TAG = 0;
        final int       UNKNOWN_TAG = -1;
        int             tag;
        TagDictionary   td = DictionaryFactory.getInstance().getDefaultTagDictionary();
        
        if (value.length() == 0) return NO_TAG;
        
        if (value.charAt(0) == '$') {
            try {
                tag = Tags.forName(value.substring(1));
                return tag;
            } catch (IllegalArgumentException e) {
                return UNKNOWN_TAG;
            }
        }
        
        if (value.charAt(0) == '@') {
            // Reference by group/element
            try {
                tag = Integer.parseInt(value.substring(1), 16);
                TagDictionary.Entry e = td.lookup(tag);
                if (e != null) {
                    return tag;
                } else {
                    return UNKNOWN_TAG;
                }
            } catch (NumberFormatException e) {
                return UNKNOWN_TAG;
            }
        }
        
        return NO_TAG;
    }
    
    
    /**
     * Returns the given value or, if it is a "Tag String", the contents of the tag.
     * Tag Strings are value starts wich start with the '$' character it is a
     * reference by name or with '@' if it is a reference by group/element.
     * @param value the property value.
     * @param ds the dataset to analyse.
     * @return the value or content of tag. null, if unknown tag.
     */
    public static String getTagStringOrValue(String value, Dataset ds) {
        int tag = getTagFromPropertyString(value);
        
        // Falls kein Tag String -> value zurueckgeben
        if (tag <= 0) {
            return value;
        }
        
        // Ist ein Tag -> dessen Inhalt zurueckgeben
        try {
            return ds.getString(tag);
        } catch (Exception e) {
            return null;
        }
        
    }
    
    
    /**
     * Adds a subdirectory to a given path. The name of the subdirectory is
     * composed of the patients name followed by '_' followed by the birthdate.
     * @param parent the parent directory.
     * @param ds the dataset to analyse.
     * @return the composed directory.
     */
    public static File addPatientDirectory(File parent, Dataset ds) {
        String patientName = ds.getString(Tags.PatientName, "X^X");
        String patientBirthDate = ds.getString(Tags.PatientBirthDate, "19990101");
        return new File(parent, patientName + "_" + patientBirthDate);
    }
    
    
    /**
     * Create a File from an URI.
     * <span style="font-style: italic;">file-uri</span><br>
     * Describes a file in a operating-system independend way. See the API-Doc
     * of the URI class. For Windows-OS the absolute URI
     * "file:/c:/user/tom/foo.txt" describes the file
     * "C:\\user\\tom\\foo.txt". Relative URI's, e.g. without the "file:"
     * schema-prefix, are relativ to the user-directory, given by the system
     * property user.dir. For example: If the user.dir is "C:\\user\\tom\\"
     * and the relative URI is "/abc/foo.txt" the referenced file is
     * "C:\\user\\tom\\abc\\foo.txt". The abbreviations "." for the current
     * and ".." for the upper directory are valid to form a relative URI.<br>
     * <br>
     *
     * @param uriString The string-description of an absolute or relative URI.
     * @return The file which is described by the uriString. Returns null, if uriString is null or "". Returns null also, if a conversion error occures.
     */
    static public File uriToFile(String uriString) {
        URI baseURI;
        URI uri;
        
        if (uriString == null) {
            return null;
        }
        
        if (uriString.equals("")) {
            return null;
        }
        
        try {
            uri = new URI(uriString);
            
            // Redundante Elemente entfernen:
            // Auakommentiert, weil eine URI der  Form "./a.b" (nicht "./a/b.c") zu
            // einer ArrayIndexOutOfBoundsException fuehrt. Grund unklar. Interner Fehler?
            // uri = uri.normalize();
            // Absolute URI's sind von der Form file://de.iftm/abc/def/g.txt,
            // relative besitzen kein "schema" dh. im Beispiel "file://" fehlt.
            if (!uri.isAbsolute()) {
                // Relative URI's werden auf das user.dir bezogen.
                baseURI = (new File(System.getProperty("user.dir"))).toURI();
                uri = baseURI.resolve(uri);
            }
            
            return new File(uri);
        } catch (Exception e) {
            return null;
        }
    }
    
    
    /**
     * Builds a file ID from the given Dataset. Components are separated by
     * the '/' caharcter:<br>
     * 1. componet: First letter of famely name followed by first letter of given
     * name followed by date of birth, 6 character. Example: TH570522<br>
     * 2. componet: Study date, 6 character. Example: 043012<br>
     * 3. componet: Modality, 2 character followed by study time, 4 character.
     * Example: MR1531<br>
     * 4. componet: Study ID. Example: 4711<br>
     * 5. componet: Series number. Example: 3<br>
     * 6. componet: Instance number. Example: 54<br>
     * All components are compatible to DICOM part 12, e.g. they have a maximum
     * of 8 characters and consists of upper case caracters, numbers or '_'.
     * @param ds the dataset to analyse.
     * @return the file ID as a absolute pathString.
     */
    public static String datasetToTreeFileID(Dataset ds) {
        String[] nameArray;
        String pathString;
        
        nameArray = datasetToNameArray(ds);
        
        pathString = nameArray[0];
        for (int i = 1; i < nameArray.length; i++) {
            pathString += "/" + nameArray[i];
        }
        
        return pathString;
    }
    
    
    /**
     * Builds an array of strings from the given Dataset:<br>
     * [0]: First letter of famely name followed by first letter of given
     * name followed by date of birth, 6 character. Example: TH570522<br>
     * [1]: Study date, 6 character. Example: 043012<br>
     * [2]: Modality, 2 character followed by study time, 4 character.
     * Example: MR1531<br>
     * [3]: Study ID. Example: 4711<br>
     * [4]: Series number. Example: 3<br>
     * [5]: Instance number. Example: 54<br>
     * All components are compatible to DICOM part 12, e.g. they have a maximum
     * of 8 characters and consists of upper case caracters, numbers or '_'.
     * @param ds the dataset to analyse.
     * @return the array of strings.
     */
    public static String[] datasetToNameArray(Dataset ds) {
        String  s;
        String  pn;
        int i;
        String[] nameArray = new String[6];
        
        String patientName = ds.getString(Tags.PatientName, "X^X");
        String patientBirthDate = ds.getString(Tags.PatientBirthDate, "19990101");
        String studyDate = ds.getString(Tags.StudyDate, "20050101");
        String studyTime = ds.getString(Tags.StudyTime, "123456.789");
        String modality = ds.getString(Tags.Modality, "OT");
        String studyID = ds.getString(Tags.StudyID, "0");
        String seriesNumber = ds.getString(Tags.SeriesNumber, "0");
        String instanceNumber = ds.getString(Tags.InstanceNumber, "0");
        
        // Patientenname mindestens 2 Buchstaben lang machen
        pn = patientName + "XX";
        
        s = pn.substring(0, 1);
        i = pn.indexOf('^');
        if (i != -1) {
            // Vorname vorhanden
            s += pn.substring(i + 1, i + 2);
        } else {
            // Kein Vorname vorhanden, die erstenb zwei Buchstaben des Nachnamens verwenden
            s = pn.substring(0, 2);
        }
        
        s += patientBirthDate.substring(2);
        nameArray[0] = stringToFileIDComponentString(s);
        
        s = studyDate.substring(2);
        nameArray[1] = stringToFileIDComponentString(s);
        
        s = modality.substring(0, 2);
        s += studyTime.substring(0, 4);
        nameArray[2] = stringToFileIDComponentString(s);
        
        nameArray[3] = stringToFileIDComponentString(studyID);
        
        nameArray[4] = stringToFileIDComponentString(seriesNumber);
        
        nameArray[5] = stringToFileIDComponentString(instanceNumber);
        
        return nameArray;
    }
    
    /**
     * Checks, if the given file ID is compatible to DICOM Part 12:
     * It may only contain the characters 'A'..'Z','0'..'9' and '_'. The
     * maximal length is 8 character.
     * This method truncates the file ID after 8 characters, converts lower case
     * to upper case characters and replaces all other not valid characters with
     * the '_' character.
     * @param s the file ID to check.
     * @return the converted file ID.
     */
    public static String stringToFileIDComponentString(String s) {
        if ((s == null) || (s.length() == 0)) {
            return "__NULL__";
        }
        
        char[] in = s.toUpperCase().toCharArray();
        char[] out = new char[Math.min(8, in.length)];
        
        for (int i = 0; i < out.length; ++i) {
            out[i] = (((in[i] >= '0') && (in[i] <= '9')) ||
                ((in[i] >= 'A') && (in[i] <= 'Z'))) ? in[i] : '_';
        }
        
        return new String(out);
    }
    
    
    /**
     * Wait for a given amount of seconds.
     *
     * @param sec amount of seconds to wait.
     */
    static public void wait(int sec) {
        long act = System.currentTimeMillis();
        while (System.currentTimeMillis() < act + 1000*sec) {}
    }
    
    
    
    /**
     * Loads a Dataset object from a URI. The URI references a file that must be 
     * conform to PS 3.10 - Media Storage and File Format for Media Interchange
     *
     * @param uri the URI referencing the Dataset.
     * @return the read in Dataset.
     * @throws IOException the exception is thrown, if 1. an input error occured
     *                     during reading or 2. if a parsing error occured (in
     *                     case of a non-DICOM file). In this case an DcmParseException
     *                     is thrown, which is a subclass of IOException.
     */
    static public Dataset uriToDataset(String uri) throws IOException {
        return fileToDataset(uriToFile(uri));
    }
    
    
    
    /**
     * Loads a Dataset object from a file. The file must be conform to
     * PS 3.10 - Media Storage and File Format for Media Interchange
     *
     * @param f the file containing the Dataset.
     * @return the read in Dataset.
     * @throws IOException the exception is thrown, if 1. an input error occured
     *                     during reading or 2. if a parsing error occured (in
     *                     case of a non-DICOM file). In this case an DcmParseException
     *                     is thrown, which is a subclass of IOException.
     */
    static public Dataset fileToDataset(File f) throws IOException {
        BufferedInputStream bis = null;
        Dataset             ds = null;
        
        // DICOM files must have a length of at least 132 byte (128 byte header and 'DICM' token)
        
        if (f.length() < 132) {
            throw new DcmParseException("No DICOM object"); // DcmParseException extends IOException
        }
        
        // Create new stream and mark begin of file
        bis = new BufferedInputStream(new FileInputStream(f));
        bis.mark(512);
        
        // Test 'DICM' token
        byte[] dt = new byte[4];
        bis.skip(128);
        bis.read(dt, 0, 4);
        if (dt[0] != 'D' || dt[1] != 'I' || dt[2] != 'C' || dt[3] != 'M') {
            throw new DcmParseException("No DICOM object"); // DcmParseException extends IOException
        }
        
        // Reset to begin of file
        bis.reset();
        
        // Load Dataset from stream
        return bufferedInputStreamToDataset(bis);
    }
    
    
    /**
     * Loads a Dataset object from an input stream. The stream must be conform to
     * PS 3.10 - Media Storage and File Format for Media Interchange
     *
     * @param bis the input stream containing the Dataset.
     * @return the read in Dataset.
     * @throws IOException the exception is thrown, if 1. an input error occured
     *                     during reading or 2. if a parsing error occured (in
     *                     case of a non-DICOM file). In this case an DcmParseException
     *                     is thrown, which is a subclass of IOException.
     */
    static public Dataset bufferedInputStreamToDataset(BufferedInputStream bis) throws IOException {
        Dataset ds = null;
        
        ds = DcmObjectFactory.getInstance().newDataset();
        
        // The API requires an InputStream only. But, if this method is called
        // with a FileInputStream it does not work reliable. If called with a
        // BufferedInputStream everything works correct.
        ds.readFile(bis, null, -1);
        bis.close();
        
        return ds;
    }
    
    
    /**
     * Loads a Dataset object from an input stream. The stream must be conform to
     * PS 3.10 - Media Storage and File Format for Media Interchange
     *
     * @param is the input stream containing the Dataset.
     * @return the read in Dataset.
     * @throws IOException the exception is thrown, if 1. an input error occured
     *                     during reading or 2. if a parsing error occured (in
     *                     case of a non-DICOM file). In this case an DcmParseException
     *                     is thrown, which is a subclass of IOException.
     */
    static public Dataset inputStreamToDataset(InputStream is) throws IOException {
        return bufferedInputStreamToDataset(new BufferedInputStream(is));
    }
    
}
