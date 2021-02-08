/*
 *  CommandLineOpt.java
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
package de.iftm.dcm4che.router.util;

import de.iftm.dcm4che.router.property.*;

import org.apache.log4j.Logger;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * This class manages the commandolineparsing.
 *
 * 2004.02.17 hacklaender: 
 * Name of default configuration file changed from framework.properties to 
 * router.properties.<br>
 *
 * @version 2004.02.17
 */
public class CommandLineOpt {
    /**
     * Zugriff auf alle benötigten Sprachresourcen
     */
    public static ResourceBundle message = ResourceBundle.getBundle ("de/iftm/dcm4che/router/util/resources/CommandLineOpt", Locale.getDefault ());

    /**
     * The LongOpt[] contains instances of LongOpt, they allowed to use longoptions
     * in the commandoline
     */
    public static final LongOpt[] LO_OPT = new LongOpt[] {
        new LongOpt("config", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
        new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
        new LongOpt("logger", LongOpt.REQUIRED_ARGUMENT, null, 'l')
    };

    /** DOCUMENT ME! */
    Getopt getOpt;

    /**
     * Name of the property file
     */
    String configName = "router.properties";

    /**
     * Defaulte URI of the property file
     */
    String configURI = "./cfg/" + configName;

    /**
     * Defaulte Name of the logger file
     */
    String loggerName = "log4j.properties";

    /**
     * Defaulte URI of the logger file
     */
    String loggerURI = "./cfg/" + loggerName;

    /**
     * Defaulte path of the file/directory to process
     */
    String fileURI = null;

    /**
     *
     */
    public static boolean configFileAvailable = true;

    /** DOCUMENT ME! */
    Logger log = Logger.getLogger (CommandLineOpt.class);

    /** DOCUMENT ME! */
    boolean readyToRun = false;

    /**
     * Creates a new instance of CommandLineOpt
     * @param argv
     */
    public CommandLineOpt (String[] argv) {
        if (argv == null) {
            readyToRun = this.checkDefaultPropertyFile ();

            return;
        }

        if (argv.length == 0) {
            readyToRun = this.checkDefaultPropertyFile ();
        } else {
            getOpt = new Getopt("dcmrouter", argv, "c:hl:", LO_OPT);
            getArgument (argv);
        }
    }

    /**
     * Selects the right action for the typed argument
     */
    public void getArgument (String[] argv) {
        int i;

        while ((i = getOpt.getopt ()) != -1) {
            switch (i) {
            case 'c':
                System.out.println ("Configuration is load from : " + getOpt.getOptarg ());
                setPropertyURI (getOpt.getOptarg ());
                readyToRun = true;

                break;

            case 'h':
                System.out.println (CommandLineOpt.message.getString ("default"));
                System.out.println (CommandLineOpt.message.getString ("help"));
                readyToRun = false;

                break;

            case 'l':
                System.out.println ("Loggerconfiguration will be loaded from : " + getOpt.getOptarg ());
                setLoggerURI (getOpt.getOptarg ());
                readyToRun = true;

                break;

            default:
                System.err.println (CommandLineOpt.message.getString ("try"));
                readyToRun = false;

                break;
            }
        }

        // i ist der Index in argv, der nicht mehr zu den Parametern/Argumenten gehoert
        i = getOpt.getOptind ();

        // Nach der Aufrufsyntax entspricht dies der URI des Files/Directorys
        if (i >= argv.length) {
            // Keine URI angegeben. Das Programm wird dann die Angabe aus der Property Datei verwenden
            return;
        }

        // File URI setzen
        setFileURI (argv[i]);
    }


    /**
     * Setter for field configURI.
     * @param uri Path of the configurationfile
     */
    public void setPropertyURI (String uri) {
        this.configURI = uri;
    }


    /**
     * Getter for field configURI
     * @return Value of configURI
     */
    public String getPropertyURI () {
        return configURI;
    }


    /**
     * Setter for field loggerURI.
     * @param uri Path of the configurationfile
     */
    private void setLoggerURI (String uri) {
        this.loggerURI = uri;
    }


    /**
     * Getter for field loggerURI
     * @return Value of loggerURI
     */
    public String getLoggerURI () {
        return loggerURI;
    }


    /**
     * Setter for field fileURI.
     * @param uri Path of the configurationfile
     */
    public void setFileURI (String uri) {
        this.fileURI = uri;
    }


    /**
     * Getter for field fileURI
     * @return Value of fileURI
     */
    public String getFileURI () {
        return fileURI;
    }


    /**
     * Getter for the status of boolean field readyToRun
     * @return Value of readyToRun
     */
    public boolean isRunnable () {
        return readyToRun;
    }


    /**
     * Checks wether the config file exists in the ./cfg dir
     * or directly in ./
     */
    private boolean checkDefaultPropertyFile () {
        File file = Util.uriToFile (configURI);

        if (file.exists ()) {
            this.configFileAvailable = true;

            return true;
        } else {
            // checks if file exists at the directory the main method is started from 
            file = new File(configName);

            if (file.exists ()) {
                // new path to the configURI
                this.setPropertyURI ("./" + configName);
                this.configFileAvailable = true;

                return true;
            }
            this.configFileAvailable = false;

            return false;
        }
    }


    /**
     * Checks wether a propertyfile for the Log4j-Logger exists in
     * the home- or cfg-directory. If not "false" will be returned,
     * else "true"
     */
    public boolean checkLoggerProp () {
        File file = Util.uriToFile (this.loggerURI);

        if (file.exists ()) {
            return true;
        } else {
            // checks if file exists at the directory the main method is started from 
            file = new File(loggerName);
            if (file.exists ()) {
                // new path to the configURI
                this.setLoggerURI ("./" + loggerName);

                return true;
            }

            //after checking both directories for log.prop, the basicKonfigurator
            //will be setted.
            return false;
        }
    }
}
