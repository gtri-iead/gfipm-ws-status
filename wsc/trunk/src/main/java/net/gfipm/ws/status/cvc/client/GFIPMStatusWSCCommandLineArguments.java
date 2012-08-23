/*
 * Copyright (c) 2012, Georgia Institute of Technology. All Rights Reserved.
 * This code was developed by Georgia Tech Research Institute (GTRI) under
 * a grant from the U.S. Dept. of Justice, Bureau of Justice Assistance.
 *
 * Please see LICENSE.txt for details on reuse.
 */

package net.gfipm.ws.status.cvc.client;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.OptionException;

import java.io.PrintStream;
import java.util.List;

/** Command line arguments for the {@link GFIPMStatusWebServiceClient} command line tool. */
public class  GFIPMStatusWSCCommandLineArguments {

    // Default Strings
    private static String DEFAULT_METADATA_URL = "http://ref.gfipm.net/gfipm-signed-ws-metadata.xml";
    private static String DEFAULT_TRUSTSTORE   = "/trust/statuswsc-cacerts.jks";
    private static String DEFAULT_KEYSTORE     = "/trust/statuswsc-keystore.jks";
    private static String DEFAULT_PW           = "changeit";
    

    // Actions
    // None for now, always Getting Status

    // Input
    private String inUrl;

    private CmdLineParser.Option IN_URL_ARG;

    private String metaUrl;

    private CmdLineParser.Option IN_META_ARG;

    private String entId;

    private CmdLineParser.Option IN_ID_ARG;


    // Output
    // None for now.


    // Key/Cert Data
    private String trustStore;

    private CmdLineParser.Option TRUST_ARG;

    private String trustPW;

    private CmdLineParser.Option TRUST_PW_ARG;

    private String keyStore;

    private CmdLineParser.Option KEYSTORE_ARG;

    private String keyPW;

    private CmdLineParser.Option KEYSTORE_PW_ARG;

    // Logging
    private boolean verbose;

    private CmdLineParser.Option VERBOSE_ARG;

    private boolean quiet;

    private CmdLineParser.Option QUIET_ARG;

    private String logConfig;

    private CmdLineParser.Option LOG_CONFIG_ARG;

    // Help
    private boolean help;

    private CmdLineParser.Option HELP_ARG;

    private CmdLineParser cliParser;

    public GFIPMStatusWSCCommandLineArguments(String[] args) {
        cliParser = new CmdLineParser();
         
        IN_URL_ARG      = cliParser.addStringOption("wspUrl");
        IN_META_ARG     = cliParser.addStringOption("fedMetadataUrl");
        IN_ID_ARG       = cliParser.addStringOption("wspEntityId");
        TRUST_ARG       = cliParser.addStringOption("TrustStore");
        TRUST_PW_ARG    = cliParser.addStringOption("TrustStorePassword");
        KEYSTORE_ARG    = cliParser.addStringOption("KeyStore");
        KEYSTORE_PW_ARG = cliParser.addStringOption("KeyStorePassword");
        HELP_ARG        = cliParser.addBooleanOption("help");
    }

    public void parseCommandLineArguments(String[] args) {
        try {
            cliParser.parse(args);

            inUrl      = (String) cliParser.getOptionValue(IN_URL_ARG);
            metaUrl    = (String) cliParser.getOptionValue(IN_META_ARG);
            entId      = (String) cliParser.getOptionValue(IN_ID_ARG);

            keyPW      = (String) cliParser.getOptionValue(KEYSTORE_PW_ARG);
            keyStore   = (String) cliParser.getOptionValue(KEYSTORE_ARG);

            trustStore = (String) cliParser.getOptionValue(TRUST_ARG);
            trustPW    = (String) cliParser.getOptionValue(TRUST_PW_ARG);

            // Defaults for null being returned
            if ( metaUrl == null )
              metaUrl = DEFAULT_METADATA_URL;

            if ( keyPW == null )
              keyPW = DEFAULT_PW;

            if ( trustPW == null )
              trustPW = DEFAULT_PW;

            if ( keyStore == null )
              keyStore = DEFAULT_KEYSTORE;

            if ( trustStore == null )
              trustStore = DEFAULT_TRUSTSTORE;

            help      = (Boolean) cliParser.getOptionValue(HELP_ARG, false);

            validateCommandLineArguments();
        } catch (OptionException e) {
            errorAndExit(e.getMessage());
        }
    }

    public String getMetadataUrl() {
      return metaUrl;
    }

    public String getWspUrl() {
      return inUrl;
    }

    public String getEntityId() {
      return entId;
    }

    public String getTruststore() {
        return trustStore;
    }

    public String getTruststorePassword() {
        return trustPW;
    }

    public String getKeystore() {
        return keyStore;
    }

    public String getKeystorePassword() {
        return keyPW;
    }

    public boolean doHelp() {
        return help;
    }

    private void validateCommandLineArguments() {
        //System.out.println ("Validate Command Line Called");
        if (doHelp()) {
            return;
        }

        if (getEntityId() == null && getWspUrl() == null) {
            errorAndExit("No WSP was specified");
        }

        if (getEntityId() != null && getWspUrl() != null) {
            errorAndExit("Can only specify a single WSP to query.");
        }

        //System.out.println ( "EntityId = " + getEntityId() + ", WspURL = " + getWspUrl() );
        //System.out.flush ();

    }

    /**
     * Print command line help instructions.
     * 
     * @param out location where to print the output
     */
    public void printHelp(PrintStream out) {
        out.println("GFIPM Status Web Service Consumer Usage Information");
        out.println("Provides a command line interface for querying for GFIPM Web Service Status.");
        out.println();
        out.println("==== Command Line Options ====");
        out.println();
        out.println(String.format("  --%-20s %s", HELP_ARG.longForm(), "Prints this help information"));
        out.println();
        out.println("WSP Specification Options - '" + IN_URL_ARG.longForm() + "' and '" + IN_ID_ARG.longForm()
                + "' are mutually exclusive.  At least one option is required.");
        out.println(String.format("  --%-20s %s", IN_ID_ARG.longForm(),  "Entity ID of the GFIPM WSP to query for status."));
        out.println(String.format("  --%-20s %s", IN_URL_ARG.longForm(), "URL of the GFIPM WSP to query for status."));

        out.println();
        out.println();
        out.println("Keystore Certificate/Key Options - TBD."
                + " Options '"
                + KEYSTORE_ARG.longForm()
                + "' and '"
                + KEYSTORE_PW_ARG.longForm() + "' are required.");
        out.println(String.format("  --%-20s %s", KEYSTORE_ARG.longForm(), "Specifies the keystore file."));
        out.println(String.format("  --%-20s %s", KEYSTORE_PW_ARG.longForm(),
                "Specifies the password for the keystore. If not provided then the key password is used."));
        out.println(String.format("  --%-20s %s", TRUST_ARG.longForm(), "Specifies the truststore file."));
        out.println(String.format("  --%-20s %s", TRUST_PW_ARG.longForm(),
                "Specifies the password for the truststore. If not provided then the key password is used."));
        out.println();
    }

    /**
     * Prints the error message to STDERR and then exits.
     * 
     * @param error the error message
     */
    private void errorAndExit(String error) {
        System.out.println("-------");
        System.out.println("Error Message: " + error);
        System.out.println("-------");
        System.out.flush();
        System.out.println();
        printHelp(System.out);
        System.out.flush();
        System.exit(-1);
    }
}
