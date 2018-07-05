/* Copyright(C) 2018 Interactive Health Solutions, Pvt. Ltd.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 3 of the License (GPLv3), or any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Interactive Health Solutions, info@ihsinformatics.com
You can also access the license on the internet at the address: http://www.gnu.org/licenses/gpl-3.0.html

Interactive Health Solutions, hereby disclaims all copyright interest in this program written by the contributors.
 */
package com.ihsinformatics.gfatm.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.ihsinformatics.util.DateTimeUtil;

/**
 * 
 * @author owais.hussain@ihsinformatics.com
 *
 */
public class GxAlertMain implements Serializable {

	private static final long serialVersionUID = -4482413651235453707L;
	private static final Logger log = Logger.getLogger(GxAlertMain.class);
	private static final String PROP_FILE_NAME = "gfatm-gxalert-integration.properties";

	public static void main(String[] args) {
		try {
			boolean doImportAll = false;
			boolean doRestrictDate = false;
			// Check arguments first
			// -a to import all results day-by-day
			// -r to import results for a specific date
			// server
			// -h or -help or --help or -? to display parameters
			Date forDate = null;
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help") || args[i].equals("--help")
						|| args[i].equals("-?")) {
					StringBuilder usage = new StringBuilder();
					usage.append("Command usage:\r\n");
					usage.append("-a to import all results day-by-day\r\n");
					usage.append(
							"-r to import all results for a specific date (yyyy-MM-dd). E.g. gfatm-gxalert-integration-xxx.jar -r 2017-12-31\r\n");
					usage.append("-h or -help or --help or -? to display parameters on console\r\n");
					usage.append("NO parameters to auto-import from last encounter date to current date\r\n");
					log.info(usage.toString());
					return;
				} else if (args[i].equals("-a")) {
					doImportAll = true;
				} else if (args[i].equals("-r")) {
					doRestrictDate = true;
					forDate = DateTimeUtil.fromSqlDateString(args[i + 1]);
					if (forDate == null) {
						log.fatal(
								"Invalid date provided. Please specify date in SQL format without quotes, i.e. yyyy-MM-dd");
						System.exit(-1);
					}
				}
			}
			GxAlertImportService service = new GxAlertImportService(readProperties());
			// Import all results
			if (doImportAll) {
				service.importAll();
			} else if (doRestrictDate) {
				service.importForDate(new DateTime(forDate.getTime()));
			} else {
				service.importAuto();
			}
			log.info("Import process complete.");
		} catch (Exception e) {
			log.error(e.getMessage());
			System.exit(-1);
		}
		System.exit(0);
	}

	/**
	 * Read properties from file
	 * 
	 * @throws IOException
	 */
	public static Properties readProperties() throws IOException {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROP_FILE_NAME);
		Properties properties = new Properties();
		properties.load(inputStream);
		return properties;
	}

}
