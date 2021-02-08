/*
 *  PropertyUtil.java
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
package de.iftm.dcm4che.router.property;

import org.apache.log4j.*;

import org.dcm4che.data.*;

import org.dcm4che.dict.*;

import java.io.*;

import java.net.*;


/**
 * This class analyses property key and value strings with special meanings.
 *
 * @author Thomas Hacklaender
 * @version 2004.02.15
 */
public class PropertyString {
    /** The logger for this plugin. */
    static Logger log = Logger.getLogger (PropertyString.class);

    /** The string given in the constructor */
    String s;

    /**
     * Creates a new PropertyString object.
     *
     * @param str the string to analyse
     */
    public PropertyString (String str) {
        s = str;
    }

    /**
     * Tests, if the PropertyString starts with a lead in character. A character
     * is lead in characeter, if the PropertyString is not null, its length is
     * graeter than 1 and the first and second character are different.
     *
     * @param c the lead in character to test
     *
     * @return true, if the PropertyString starts with the given lead in character.
     */
    protected boolean isLeadIn (char c) {
        // Wenn s nicht definiert, false
        if (s == null) {
            return false;
        }
        if (s.length () == 0) {
            return false;
        }

        // Wenn s nur ein Zeichen lang ist, kann kann nicht auf Leadin enschieden werden
        if (s.length () == 1) {
            return false;
        }

        // s ist mindestens 2 Zeichen lang
        return (s.charAt (0) == c) & (s.charAt (1) != c);
    }


    /**
     * Tests, if the PropertyString starts with the lead in character '#'.
     *
     * @return true, if the PropertyString starts with the lead in character '#'.
     */
    public boolean isCommand () {
        return isLeadIn ('#');
    }


    /**
     * Returns the command given in the PropertyString. A command is a PropertyString
     * that starts with the lead in character '#' without the '#' caharcter.
     *
     * @return the command or "" if the PropertyString does not start with the
     *         lead in character '#'.
     */
    public String getCommand () {
        if (!isCommand ()) {
            return "";
        }

        return s.substring (1);
    }


    /**
     * Gets the contents of the PropertyString. If it is a named tag or hexadecimal
     * tag the contents is the contents of the specified tag extracted from the
     * given Dataset. Else it is the String given in the constructor of the 
     * PropertyString.
     *
     * @param ds the Dataset the tag should be extracted from (if needed).
     *
     * @return the contents.
     */
    public String getContents (Dataset ds) {
        // Wenn s ein Kommando ist, kein Inhalt.
        if (isCommand ()) {
            return "";
        }

        // Named Tag
        if (isLeadIn ('$')) {
            return getNamedTag (ds);
        }

        // Hexadecimal Tag
        if (isLeadIn ('@')) {
            return getHexadecimalTag (ds);
        }

        // Der String ist selbst der Inhalt
        return s;
    }


    /**
     * Gets the contents of the named Tag specified in the PropertyString
     * from the given Dataset. A named tag starts with the lead in character
     * '$'. The name is the name of the tag specified in the class
     * org.dcm4che.dict.Tags. (Example: $PatientName)
     *
     * @param ds the Dataset the tag should be extracted from.
     *
     * @return the contens of the tag.
     */
    protected String getNamedTag (Dataset ds) {
        String value;

        try {
            value = ds.getString (Tags.forName (s.substring (1)));
            if (value == null) {
                return "";
            }

            return value;
        } catch (Exception e) {
            log.warn ("Illegal property " + s + " - Exception: " + e.getMessage ());

            return "";
        }
    }


    /**
     * Gets the contents of the hexadecimal Tag specified in the PropertyString
     * from the given Dataset. A hexadecimal tag starts with the lead in character
     * '@'. It is followed by the 4 character long hexadecimal representation of 
     * the group end the element number (Example: @00080020 for the StudyDate).
     *
     * @param ds the Dataset the tag should be extracted from.
     *
     * @return the contens of the tag.
     */
    protected String getHexadecimalTag (Dataset ds) {
        String value;

        try {
            value = ds.getString (Integer.parseInt (s.substring (1), 16));
            if (value == null) {
                return "";
            }

            return value;
        } catch (Exception e) {
            log.warn ("Illegal property " + s + " - Exception: " + e.getMessage ());

            return "";
        }
    }
}
