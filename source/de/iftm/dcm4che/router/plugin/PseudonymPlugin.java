/*
 *  PseudonymPlugin.java
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

import de.iftm.dcm4che.router.property.DicomRouterProperties;

import org.apache.log4j.*;

import org.dcm4che.data.*;

import org.dcm4che.dict.*;

import java.util.*;


/**
 * This plugin replaces the PatientName with a pseudonym. The plugin supports
 * the properties which start with the key PseudonymPlugin and have the subkeys:<br>
 * <br>
 * type<br>
 * The key may have one of the following predefined values:<br>
 * #Initials2_Birdat4<br>
 * Creates a new Patientname: ii_bbbbbb with<br>
 * i the initials of the patient: name surename<br>
 * b patient birth date<br>
 * <br>
 * #Mod2Acqdat8_Birdat8<br>
 * Creates a new Patientname: mmaaaaaaaa_bbbbbbbb with<br>
 * m modality<br>
 * a acquisition date<br>
 * b patient birth date<br>
 * <br>
 * Default value: #Initials2_Birdat4<br>
 *
 * @author Thomas Hacklaender
 * @version 2003.11.14
 */
public class PseudonymPlugin extends DicomRouterPlugin {
    /** Value of the type of pseudonym. */
    public static final int INITIALS2_BIRDAT6 = 0;
    
    /** Value of the type of pseudonym. */
    public static final int MOD2ACQDAT8_BIRDAT8 = 1;
    
    /** The version string */
    final String VERSION = "2006.11.10";
    
    /** The logger for this plugin */
    Logger log = Logger.getLogger(PseudonymPlugin.class);
    
    /** The properties of this plugin */
    Properties props = null;
    
    // Local fields defined by properties:
    
    /** Type of pseudonym */
    int type = INITIALS2_BIRDAT6;
    
    /**
     * Constructor.
     *
     */
    public PseudonymPlugin() {
        // Nichts zu tun.
    }
    
    /**
     * Inits the DicomRouterPlugin with the specified Properties.
     * @param p  Properties containing the configuration of the plugin
     */
    public void init(Properties p) {
        String s;
        
        // Properties lokal speichern
        props = p;
        
        // Art der Pseudonymisierung festlegen
        s = props.getProperty("type");
        
        if (s != null) {
            if (s.equals("#Mod2Acqdat8_Birdat8")) {
                type = MOD2ACQDAT8_BIRDAT8;
            } else if (s.equals("#Initials2_Birdat6")) {
                type = INITIALS2_BIRDAT6;
            } else {
                log.warn("Unknown pseudonym type in properties: " + s +
                        ". Replaced by #Initials2_Birdat6");
            }
        }
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
        String originalName;
        String newName = "Pseudonym";
        PersonName pn;
        
        // Nur dann weitermachen, wenn ein Datset vorhanden ist
        if (dataset == null) {
            log.warn("No Dataset given.");
            
            return REQUEST_ABORT_RECEIVER;
        }
        
        originalName = dataset.getString(Tags.PatientName);
        
        switch (type) {
            case INITIALS2_BIRDAT6:
                newName = initials2_Birdat6(dataset);
                
                break;
                
            case MOD2ACQDAT8_BIRDAT8:
                newName = mod2Acqdat8_Birdat8(dataset);
                
                break;
        }
        
        pn = DcmObjectFactory.getInstance().newPersonName(newName);
        dataset.putPN(Tags.PatientName, pn);
        
        if (log.isInfoEnabled()) {
            log.info("PatientName " + originalName + " replaced by: " +
                    newName);
        }
        
        // Plugin ohne Fehler beendet
        return CONTINUE;
    }
    
    /**
     * Creates a new Patientname: ii_bbbbbb with<br>
     * i the initials of the patient: name surename<br>
     * b patient birth date<br>
     * @param ds the Dataset to process
     *
     * @return the patientname
     */
    protected String initials2_Birdat6(Dataset ds) {
        int i;
        String pseudonym = "";
        String patientName;
        String patientBirthDate;
        
        // C.7.1.1 Patient Module
        if ((patientName = ds.getString(Tags.PatientName)) == null) {
            patientName = "X^X";
            log.warn("Can't find PatientName tag. Replaced by: X^X");
        }
        
        if ((patientBirthDate = ds.getString(Tags.PatientBirthDate)) == null) {
            patientBirthDate = "20000101";
            log.warn("Can't find PatientBirthDate tag. Replaced by: 20000101");
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
     * Creates a new Patientname: mmaaaaaaaa_bbbbbbbb with<br>
     * m modality<br>
     * a acquisition date<br>
     * b patient birth date<br>
     * @param ds the Dataset to process
     *
     * @return the patientname
     */
    protected String mod2Acqdat8_Birdat8(Dataset ds) {
        String modality;
        String acquisitionDate;
        String patientBirthDate;
        
        if ((modality = ds.getString(Tags.Modality)) == null) {
            modality = "XX";
            log.warn("Can't find Modality tag. Replaced by: XX");
        }
        
        if ((acquisitionDate = ds.getString(Tags.AcquisitionDate)) == null) {
            acquisitionDate = "20030101";
            log.warn("Can't find AcquisitionDate tag. Replaced by: 20030101");
        }
        
        if ((patientBirthDate = ds.getString(Tags.PatientBirthDate)) == null) {
            patientBirthDate = "20000101";
            log.warn("Can't find PatientBirthDate tag. Replaced by: 20000101");
        }
        
        return modality + acquisitionDate + "_" + patientBirthDate;
    }
}
