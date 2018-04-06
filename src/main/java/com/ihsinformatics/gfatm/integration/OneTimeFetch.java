/* Copyright(C) 2018 Interactive Health Solutions, Pvt. Ltd.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 3 of the License (GPLv3), or any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Interactive Health Solutions, info@ihsinformatics.com
You can also access the license on the internet at the address: http://www.gnu.org/licenses/gpl-3.0.html

Interactive Health Solutions, hereby disclaims all copyright interest in this program written by the contributors.
*/

package com.ihsinformatics.gfatm.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ihsinformatics.gfatm.integration.model.GeneXpertResult;
import com.ihsinformatics.util.DatabaseUtil;
import com.ihsinformatics.util.FileUtil;

/**
 * @author owais.hussain@ihsinformatics.com
 *
 */
public class OneTimeFetch {

	private static final Logger log = Logger.getLogger(OneTimeFetch.class);
	private static final String PROP_FILE_NAME = "gfatm-gxalert-integration.properties";
	private static Properties prop;
	private static String baseUrl;
	private static String apiKey;
	private static String authentication;
	private static DatabaseUtil dbUtil;

	public static void main(String[] args) {
		try {
			readProperties();
			FileUtil fu = new FileUtil();
			String[] ids = fu.getLines("D:\\ids.csv");
			for (String id : ids) {
				StringBuilder param = new StringBuilder();
				param.append("patientId=" + id);
				JSONArray results = getGxAlertResults(param.toString());
				if (results == null) {
					continue;
				}
				for (int i = 0; i < results.length(); i++) {
					JSONObject result = results.getJSONObject(i);
					GeneXpertResult gxp = new GeneXpertResult();
					gxp.fromJson(result);
					log.info(result);
					// Write results to DB
					Connection connection = dbUtil.getConnection();
					PreparedStatement statement = connection
							.prepareStatement("INSERT INTO gxalert (id,data) VALUES (?,?)");
					statement.setInt(1, 0);
					statement.setString(2, result.toString());
					statement.execute();
					statement.close();
				}
				Thread.sleep(50);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		System.exit(0);
	}

	/**
	 * Read properties from file
	 * 
	 * @throws IOException
	 */
	private static void readProperties() throws IOException {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROP_FILE_NAME);
		prop = new Properties();
		prop.load(inputStream);
		// Initiate properties
		String dbUsername = prop.getProperty("connection.username");
		String dbPassword = prop.getProperty("connection.password");
		String url = prop.getProperty("connection.url");
		String dbName = prop.getProperty("connection.default_schema");
		String driverName = prop.getProperty("connection.driver_class");
		dbUtil = new DatabaseUtil(url, dbName, driverName, dbUsername, dbPassword);
		log.info("Trying to connect with database: " + (dbUtil.tryConnection() ? "SUCCESS!" : "FAILED!"));
		if (!dbUtil.tryConnection()) {
			log.fatal("Unable to connect with database using " + dbUtil.toString());
			System.exit(0);
		}
		baseUrl = prop.getProperty("gxalert.url");
		apiKey = prop.getProperty("gxalert.api_key");
		authentication = prop.getProperty("gxalert.authentication");
	}

	public static JSONArray getGxAlertResults(String queryString) throws MalformedURLException {
		URL url = new URL(baseUrl + "/test?" + queryString);
		HttpURLConnection conn = null;
		StringBuilder response = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", authentication + " " + apiKey);
			conn.setDoOutput(true);
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				log.error("Response code: " + responseCode);
			}
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader br = new BufferedReader(inputStreamReader);
			response = new StringBuilder();
			String str = null;
			while ((str = br.readLine()) != null) {
				response.append(str);
			}
			conn.disconnect();
		} catch (Exception e) {
		}
		if (response == null) {
			return null;
		}
		return new JSONArray(response.toString());
	}

}
