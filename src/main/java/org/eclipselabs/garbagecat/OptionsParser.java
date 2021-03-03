package org.eclipselabs.garbagecat;

import java.util.ResourceBundle;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipselabs.garbagecat.util.Constants;
import org.json.JSONObject;

public class OptionsParser {
	
    private static Options options;

    static {
        // Declare command line options
        options = new Options();
        options.addOption(Constants.OPTION_HELP_SHORT, Constants.OPTION_HELP_LONG, false, "help");
        options.addOption(Constants.OPTION_VERSION_SHORT, Constants.OPTION_VERSION_LONG, false, "version");
        options.addOption(Constants.OPTION_LATEST_VERSION_SHORT, Constants.OPTION_LATEST_VERSION_LONG, false,
                "latest version");
        options.addOption(Constants.OPTION_JVMOPTIONS_SHORT, Constants.OPTION_JVMOPTIONS_LONG, true,
                "JVM options used during JVM run");
        options.addOption(Constants.OPTION_PREPROCESS_SHORT, Constants.OPTION_PREPROCESS_LONG, false,
                "do preprocessing");
        options.addOption(Constants.OPTION_STARTDATETIME_SHORT, Constants.OPTION_STARTDATETIME_LONG, true,
                "JVM start datetime (yyyy-MM-dd HH:mm:ss,SSS) required for handling datestamp-only logging");
        options.addOption(Constants.OPTION_THRESHOLD_SHORT, Constants.OPTION_THRESHOLD_LONG, true,
                "threshold (0-100) for throughput bottleneck reporting");
        options.addOption(Constants.OPTION_REORDER_SHORT, Constants.OPTION_REORDER_LONG, false,
                "reorder logging by timestamp");
        options.addOption(Constants.OPTION_OUTPUT_SHORT, Constants.OPTION_OUTPUT_LONG, true,
                "output file name (default " + Constants.OUTPUT_FILE_NAME + ")");
    }

	/**
	 * Parse command line options.
	 * 
	 * @return
	 */
	public static final CommandLine parseOptions(String[] args) throws ParseException {
	    CommandLineParser parser = new BasicParser();
	    CommandLine cmd = null;
	    // Allow user to just specify help or version.
	    if (args.length == 1 && (args[0].equals("-" + Constants.OPTION_HELP_SHORT)
	            || args[0].equals("--" + Constants.OPTION_HELP_LONG))) {
	        usage();
	    } else if (args.length == 1 && (args[0].equals("-" + Constants.OPTION_VERSION_SHORT)
	            || args[0].equals("--" + Constants.OPTION_VERSION_LONG))) {
	        System.out.println("Running garbagecat version: " + getVersion());
	    } else if (args.length == 1 && (args[0].equals("-" + Constants.OPTION_LATEST_VERSION_SHORT)
	            || args[0].equals("--" + Constants.OPTION_LATEST_VERSION_LONG))) {
	        System.out.println("Latest garbagecat version/tag: " + getLatestVersion());
	    } else if (args.length == 2 && (((args[0].equals("-" + Constants.OPTION_VERSION_SHORT)
	            || args[0].equals("--" + Constants.OPTION_VERSION_LONG))
	            && (args[1].equals("-" + Constants.OPTION_LATEST_VERSION_SHORT)
	                    || args[1].equals("--" + Constants.OPTION_LATEST_VERSION_LONG)))
	            || ((args[1].equals("-" + Constants.OPTION_VERSION_SHORT)
	                    || args[1].equals("--" + Constants.OPTION_VERSION_LONG))
	                    && (args[0].equals("-" + Constants.OPTION_LATEST_VERSION_SHORT)
	                            || args[0].equals("--" + Constants.OPTION_LATEST_VERSION_LONG))))) {
	        System.out.println("Running garbagecat version: " + getVersion());
	        System.out.println("Latest garbagecat version/tag: " + getLatestVersion());
	    } else {
	        cmd = parser.parse(options, args);
	        Main.validateOptions(cmd);
	    }
	    return cmd;
	}
	
	/**
     * Output usage help.
     * 
     * @param options
     */
    static void usage() {
        // Use the built in formatter class
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("garbagecat [OPTION]... [FILE]", options);
    }
	
    /**
     * @return version string.
     */
    static String getVersion() {
        ResourceBundle rb = ResourceBundle.getBundle("META-INF/maven/garbagecat/garbagecat/pom");
        return rb.getString("version");
    }

    /**
     * @return version string.
     */
    static String getLatestVersion() {
        String url = "https://github.com/mgm3746/garbagecat/releases/latest";
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                    .build();
            HttpGet request = new HttpGet(url);
            request.addHeader("Accept", "application/json");
            request.addHeader("content-type", "application/json");
            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            return new JSONObject(json).getString("tag_name");
        }

        catch (Exception ex) {
            ex.printStackTrace();
            return "Unable to retrieve";
        }
    }


}
