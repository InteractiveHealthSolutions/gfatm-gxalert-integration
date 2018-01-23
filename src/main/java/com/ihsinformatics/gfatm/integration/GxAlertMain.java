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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ihsinformatics.gfatm.integration.model.GeneXpertResult;
import com.ihsinformatics.gfatm.integration.shared.Constant;
import com.ihsinformatics.util.CommandType;
import com.ihsinformatics.util.DatabaseUtil;
import com.ihsinformatics.util.DateTimeUtil;

/**
 * 
 * @author owais.hussain@ihsinformatics.com
 *
 */
public class GxAlertMain implements java.io.Serializable {

	private static final long serialVersionUID = -4482413651235453707L;
	private static final Logger log = Logger.getLogger(GxAlertMain.class);
	private static final String PROP_FILE_NAME = "gfatm-gxalert-integration.properties";
	private static Properties prop;
	private static String baseUrl;
	private static String apiKey;
	private static String authentication;
	private static int gxAlertUserId;
	private static int fetchDurationHours;
	private static DatabaseUtil dbUtil;

	public static void main(String[] args) {
		try {
			boolean doAuto = true;
			boolean doImportAll = false;
			boolean doRestrictDate = false;
			// Check arguments first
			// -a to import all results day-by-day
			// -r to import results for a specific date
			Date forDate = null;
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-a")) {
					doImportAll = true;
					doAuto = false;
				} else if (args[i].equals("-r")) {
					doRestrictDate = true;
					doAuto = false;
					forDate = DateTimeUtil.fromSqlDateString(args[i + 1]);
					if (forDate == null) {
						System.out
								.println("Invalid date provided. Please specify date in SQL format without quotes, i.e. yyyy-MM-dd");
					}
				}
			}
			readProperties();
			// Import all results
			if (doImportAll) {
				importAll();
			} else if (doRestrictDate) {
				importForDate(new DateTime(forDate.getTime()));
			} else if (doAuto) {
				importAuto();
			}
			System.out.println("Import process complete.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static void readProperties() throws IOException {
		InputStream inputStream = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(PROP_FILE_NAME);
		prop = new Properties();
		prop.load(inputStream);
		// Initiate properties
		String dbUsername = GxAlertMain.prop
				.getProperty("connection.username");
		String dbPassword = GxAlertMain.prop
				.getProperty("connection.password");
		String url = GxAlertMain.prop.getProperty("connection.url");
		String dbName = GxAlertMain.prop
				.getProperty("connection.default_schema");
		String driverName = GxAlertMain.prop
				.getProperty("connection.driver_class");
		dbUtil = new DatabaseUtil(url, dbName, driverName, dbUsername,
				dbPassword);
		String username = prop.getProperty("gxalert.openmrs.username");
		gxAlertUserId = Integer.parseInt(prop.getProperty(
				"gxalert.openmrs.user_id", "427"));
		baseUrl = prop.getProperty("gxalert.url");
		apiKey = prop.getProperty("gxalert.api_key");
		authentication = prop.getProperty("gxalert.authentication");
		fetchDurationHours = Integer.parseInt(prop.getProperty(
				"gxalert.fetch_duration_hours", "24"));
		// Get User ID against the user
		Object userId = dbUtil.runCommand(CommandType.SELECT,
				"select user_id from openmrs.users where username = '"
						+ username + "'");
		gxAlertUserId = Integer.parseInt(userId.toString());
	}

	/**
	 * Fetches last update date from Encounter table against
	 * {@code Constant.gxpEncounterType}. If no results are there, then first
	 * date from Encounter table is picked.
	 * 
	 * @throws MalformedURLException
	 * @throws JSONException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws ParseException
	 * @throws SQLException
	 */
	public static void importAuto() throws MalformedURLException,
			JSONException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, ParseException, SQLException {
		GxAlertMain gxAlert = new GxAlertMain();
		String dateStr = GxAlertMain.dbUtil
				.getValue("select ifnull(max(date_created), (select min(date_created) from encounter)) as max_date from encounter where encounter_type = "
						+ Constant.gxpEncounterType);
		DateTime start = new DateTime()
				.minusHours(GxAlertMain.fetchDurationHours);
		if (dateStr != null) {
			start = new DateTime(DateTimeUtil.fromSqlDateString(dateStr));
		}
		DateTime end = start.plusHours(GxAlertMain.fetchDurationHours);
		gxAlert.run(start, end);
	}

	/**
	 * Fetches last update date from Encounter table against
	 * {@code Constant.gxpEncounterType} and for every date, new results are
	 * fetched and stored
	 * 
	 * @throws MalformedURLException
	 * @throws JSONException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws ParseException
	 * @throws SQLException
	 */
	public static void importAll() throws MalformedURLException, JSONException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, ParseException, SQLException {
		GxAlertMain gxAlert = new GxAlertMain();
		String dateStr = GxAlertMain.dbUtil
				.getValue("select ifnull(max(encounter_datetime), (select min(encounter_datetime) from encounter)) as max_date from encounter where encounter_type = "
						+ Constant.gxpEncounterType);
		DateTime start = new DateTime(DateTimeUtil.fromSqlDateString(dateStr));
		DateTime end = start.plusDays(1);
		while (end.isBeforeNow()) {
			gxAlert.run(start, end);
			start = start.plusDays(1);
			end = end.plusDays(1);
		}
	}

	/**
	 * Fetch results of a specific date and import
	 * 
	 * @param start
	 * @throws MalformedURLException
	 * @throws JSONException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws ParseException
	 * @throws SQLException
	 */
	public static void importForDate(DateTime start)
			throws MalformedURLException, JSONException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, ParseException, SQLException {
		GxAlertMain gxAlert = new GxAlertMain();
		start = start.withHourOfDay(0).withMinuteOfHour(0)
				.withSecondOfMinute(0);
		DateTime end = start.plusDays(1).minusSeconds(1);
		gxAlert.run(start, end);
	}

	/**
	 * This method fetches all GXP results entered between given range of dates
	 * 
	 * @param start
	 * @param end
	 * @throws ParseException
	 * @throws JSONException
	 * @throws MalformedURLException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void run(DateTime start, DateTime end) throws ParseException,
			MalformedURLException, JSONException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		StringBuilder param = new StringBuilder();
		param.append("start=" + DateTimeUtil.toSqlDateString(start.toDate()));
		param.append("&end=" + DateTimeUtil.toSqlDateString(end.toDate()));
		JSONArray results = getGxAlertResults(param.toString());
		if (results.length() == 0) {
			System.out.println("No result found between " + start + " and "
					+ end);
			return;
		}
		for (int i = 0; i < results.length(); i++) {
			JSONObject result = results.getJSONObject(i);
			// Save GeneXpert result in OpenMRS
			GeneXpertResult geneXpertResult = new GeneXpertResult();
			geneXpertResult.fromJson(result);

			if (geneXpertResult.getMtbResult().equals("DETECTED")) {
				if (geneXpertResult.getMtbBurden() == null) {
					geneXpertResult.fromJson(result);
				}
			}

			// Skip if the Patient ID scheme does not match
			if (!geneXpertResult.getPatientId()
					.matches(Constant.patientIdRegex)) {
				System.out.println("Patient ID "
						+ geneXpertResult.getPatientId() + " is invalid!");
				continue;
			}
			// Fetch patient ID against the given identifier in GXP test
			StringBuilder query = new StringBuilder(
					"select patient_id from openmrs.patient_identifier where identifier='"
							+ geneXpertResult.getPatientId() + "'");
			String str = dbUtil.getValue(query.toString());
			if (str == null) {
				System.out.println("Patient ID "
						+ geneXpertResult.getPatientId() + " not found.");
				continue;
			}
			Integer patientId = Integer.parseInt(str);
			// Fetch Location ID from Host ID
			query = new StringBuilder(
					"select location_id from openmrs.location where name = '"
							+ geneXpertResult.getHostId() + "'");
			str = dbUtil.getValue(query.toString());
			Integer encounterLocationId = 1;
			if (str != null) {
				encounterLocationId = Integer.parseInt(str);
			}
			DateTime dateCreated = new DateTime(geneXpertResult
					.getTestEndedOn().getTime());
			// If an observation is found with same Cartridge ID, then continue
			StringBuilder filter = new StringBuilder();
			filter.append("where concept_id = " + Constant.cartridgeConceptId);
			filter.append(" and value_text = '"
					+ geneXpertResult.getCartridgeSerial() + "'");
			// Search for all GX Test forms with missing results
			long rows = dbUtil.getTotalRows("openmrs.obs", filter.toString());
			if (rows > 0) {
				System.out
						.println("Record already exists against Cartridge ID: "
								+ geneXpertResult.getCartridgeSerial());
				continue;
			}
			boolean success = saveGeneXpertResult(patientId,
					encounterLocationId, gxAlertUserId, dateCreated,
					geneXpertResult);
			System.out.println("Imported result for "
					+ geneXpertResult.getPatientId()
					+ (success ? ": YES" : ": NO"));
		}
	}

	/**
	 * Searches for latest result in a given array
	 * 
	 * @param results
	 * @return
	 * @throws ParseException
	 * @throws JSONException
	 */
	public JSONObject searchLatestResult(JSONArray results)
			throws ParseException, JSONException {
		JSONObject latest = null;
		Date latestDate = new DateTime().minusYears(99).toDate();
		for (int i = 0; i < results.length(); i++) {
			JSONObject currentObj = results.getJSONObject(i);
			// Look for testStartedOn variable
			String startedOnStr = currentObj.getString("testStartedOn");
			Date startedOn = null;
			try {
				startedOn = DateTimeUtil.fromString(startedOnStr,
						"yyyy-MM-dd'T'HH:mm:ss");
			} catch (Exception e) {
				startedOn = DateTimeUtil.fromString(startedOnStr,
						"yyyy-MM-dd'T'HH:mm:ss'Z'");
			}
			if (startedOn.after(latestDate)) {
				latestDate = startedOn;
				latest = currentObj;
			}
		}
		return latest;
	}

	/**
	 * Fetch results from GxAlert using API
	 * 
	 * @param queryString
	 * @return
	 * @throws MalformedURLException
	 * @throws JSONException
	 */
	public JSONArray getGxAlertResults(String queryString)
			throws MalformedURLException, JSONException {
		URL url = new URL(baseUrl + "/test?" + queryString);
		HttpURLConnection conn = null;
		StringBuilder response = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", authentication + " "
					+ apiKey);
			conn.setDoOutput(true);
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				log.error("Response code: " + responseCode);
			}
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader br = new BufferedReader(inputStreamReader);
			response = new StringBuilder();
			String str = null;
			while ((str = br.readLine()) != null) {
				response.append(str);
			}
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (response == null) {
			return null;
		}
		return new JSONArray(response.toString());
	}

	/**
	 * This lengthy method converts GeneXpert results in JSON format into SQL
	 * queries and saves in DB. If you have any refactoring ideas, you're
	 * welcome to do so
	 * 
	 * @param encounterId
	 * @param patientId
	 * @param dateEncounterCreated
	 * @param encounterLocationId
	 * @param geneXpertResult
	 * @return
	 * @throws Exception
	 */
	public boolean saveGeneXpertResult(int patientId, int encounterLocationId,
			int gxAlertUserId, DateTime dateEncounterCreated,
			GeneXpertResult gxp) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		List<String> queries = new ArrayList<String>();
		Integer encounterId = saveGeneXpertEncounter(patientId,
				encounterLocationId, gxAlertUserId, dateEncounterCreated);
		StringBuilder query;
		Date obsDate = new Date();
		try {
			obsDate = gxp.getTestEndedOn();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String insertQueryPrefix = "INSERT INTO openmrs.obs (obs_id,person_id,concept_id,encounter_id,obs_datetime,location_id,value_boolean,value_coded,value_datetime,value_numeric,value_text,comments,creator,date_created,voided,uuid) VALUES ";

		// Queries for error observations
		if (gxp.getErrorCode() != null) {
			query = getErrorCodeQuery(patientId, encounterLocationId,
					gxAlertUserId, gxp, encounterId, obsDate, insertQueryPrefix);
			queries.add(query.toString());
			query = getErrorNotesQuery(patientId, encounterLocationId, gxp,
					encounterId, obsDate, insertQueryPrefix);
			queries.add(query.toString());
		}

		// Query for GXP result observation
		query = getGxpResultQuery(patientId, encounterLocationId,
				gxAlertUserId, encounterId, obsDate, insertQueryPrefix,
				gxp.getMtbResult());
		queries.add(query.toString());

		// Query for MTB Burden observation
		if (gxp.getMtbResult().equals("DETECTED")) {
			query = getMtbBurdenQuery(patientId, encounterLocationId,
					gxAlertUserId, encounterId, obsDate, insertQueryPrefix,
					gxp.getMtbBurden());
			queries.add(query.toString());
		}

		// Query for RIF result observation
		if (gxp.getRifResult() != null) {
			boolean rifDetected = gxp.getRifResult().equals("DETECTED");
			boolean rifIndeterminate = gxp.getMtbResult().equals(
					"INDETERMINATE");
			query = getRifResultQuery(patientId, encounterLocationId,
					gxAlertUserId, encounterId, obsDate, insertQueryPrefix,
					rifDetected, rifIndeterminate);
			queries.add(query.toString());
		}

		// Query for Notes observation
		if (gxp.getNotes() != null) {
			String notes = gxp.getNotes().replaceAll("['\"]", "_");
			query = getNotesQuery(patientId, encounterLocationId,
					gxAlertUserId, encounterId, obsDate, insertQueryPrefix,
					notes);
			queries.add(query.toString());
		}

		// Query for Cartridge serial no. observation
		if (gxp.getCartridgeSerial() != null) {
			long cartridgeNo = gxp.getCartridgeSerial();
			query = getCartridgeNoQuery(patientId, encounterLocationId,
					gxAlertUserId, encounterId, obsDate, insertQueryPrefix,
					cartridgeNo);
			queries.add(query.toString());
		}

		// Query for Sample ID observation
		if (gxp.getSampleId() != null) {
			String sampleId = gxp.getSampleId();
			query = getSampleIdQuery(patientId, encounterLocationId,
					gxAlertUserId, encounterId, obsDate, insertQueryPrefix,
					sampleId);
			queries.add(query.toString());
		}

		// Query for Host ID observation
		if (gxp.getHostId() != null) {
			String hostName = gxp.getHostId();
			query = getHostNameQuery(patientId, encounterLocationId,
					gxAlertUserId, encounterId, obsDate, insertQueryPrefix,
					hostName);
			queries.add(query.toString());
		}

		// Query for Reagent Lot ID observation
		if (gxp.getReagentLotId() != null) {
			long reagentLotNo = gxp.getReagentLotId();
			query = getReagentLotNoQuery(patientId, encounterLocationId,
					gxAlertUserId, encounterId, obsDate, insertQueryPrefix,
					reagentLotNo);
			queries.add(query.toString());
		}

		// Query for Module serial no. observation
		if (gxp.getModuleSerial() != null) {
			long moduleSerialNo = gxp.getModuleSerial();
			query = getModuleSerialNoQuery(patientId, encounterLocationId,
					gxAlertUserId, encounterId, obsDate, insertQueryPrefix,
					moduleSerialNo);
			queries.add(query.toString());
		}
		for (String string : queries) {
			try {
				dbUtil.runCommandWithException(CommandType.INSERT, string);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private StringBuilder getModuleSerialNoQuery(int patientId,
			int encounterLocationId, int gxAlertUserId, Integer encounterId,
			Date obsDate, String insertQueryPrefix, long moduleSerialNo) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.moduleSerialConceptId
				+ "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + moduleSerialNo + "','Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	private StringBuilder getReagentLotNoQuery(int patientId,
			int encounterLocationId, int gxAlertUserId, Integer encounterId,
			Date obsDate, String insertQueryPrefix, long reagentLotNo) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.reagentLotConceptId
				+ "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,NULL,NULL,");
		query.append(reagentLotNo + ",NULL,'Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	private StringBuilder getHostNameQuery(int patientId,
			int encounterLocationId, int gxAlertUserId, Integer encounterId,
			Date obsDate, String insertQueryPrefix, String hostName) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.hostConceptId + ","
				+ encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + hostName + "','Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	private StringBuilder getSampleIdQuery(int patientId,
			int encounterLocationId, int gxAlertUserId, Integer encounterId,
			Date obsDate, String insertQueryPrefix, String sampleId) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.sampleConceptId + ","
				+ encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + sampleId + "','Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	private StringBuilder getCartridgeNoQuery(int patientId,
			int encounterLocationId, int gxAlertUserId, Integer encounterId,
			Date obsDate, String insertQueryPrefix, long cartridgeNo) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.cartridgeConceptId
				+ "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + cartridgeNo + "','Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	private StringBuilder getNotesQuery(int patientId, int encounterLocationId,
			int gxAlertUserId, Integer encounterId, Date obsDate,
			String insertQueryPrefix, String notes) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.notesConceptId + ","
				+ encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + notes + "','Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	private StringBuilder getRifResultQuery(int patientId,
			int encounterLocationId, int gxAlertUserId, Integer encounterId,
			Date obsDate, String insertQueryPrefix, boolean rifDetected,
			boolean rifIndeterminate) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.rifResultConceptId
				+ "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,");
		if (rifDetected) {
			query.append(Constant.rifDetectedConceptId + ",");
		} else if (rifIndeterminate) {
			query.append(Constant.rifIndeterminateConceptId + ",");
		} else {
			query.append(Constant.rifNotDetectedConceptId + ",");
		}
		query.append("NULL,NULL,NULL,'Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	private StringBuilder getMtbBurdenQuery(int patientId,
			int encounterLocationId, int gxAlertUserId, Integer encounterId,
			Date obsDate, String insertQueryPrefix, String mtbBurden) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.mtbBurdenConceptId
				+ "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,");
		if (mtbBurden.equals("HIGH")) {
			query.append(Constant.highConceptId + ",");
		} else if (mtbBurden.equals("MEDIUM")) {
			query.append(Constant.mediumConceptId + ",");
		} else if (mtbBurden.equals("LOW")) {
			query.append(Constant.lowConceptId + ",");
		} else {
			query.append(Constant.veryLowConceptId + ",");
		}
		query.append("NULL,NULL,NULL,'Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	private StringBuilder getGxpResultQuery(int patientId,
			int encounterLocationId, int gxAlertUserId, Integer encounterId,
			Date obsDate, String insertQueryPrefix, String mtbResult) {
		boolean mtbDetected = mtbResult.equals("DETECTED");
		boolean error = mtbResult.equals("ERROR");
		boolean noResult = mtbResult.equals("NO RESULT");
		boolean invalid = mtbResult.equals("INVALID");
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.gxpResultConceptId
				+ "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,");
		if (mtbDetected) {
			query.append(Constant.detectedConceptId + ",");
		} else if (error) {
			query.append(Constant.errorConceptId + ",");
		} else if (noResult) {
			query.append(Constant.noResultConceptId + ",");
		} else if (invalid) {
			query.append(Constant.invalidConceptId + ",");
		} else {
			query.append(Constant.notDetectedConceptId + ",");
		}
		query.append("NULL,NULL,NULL,'Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	private StringBuilder getErrorNotesQuery(int patientId,
			int encounterLocationId, GeneXpertResult gxp, Integer encounterId,
			Date obsDate, String insertQueryPrefix) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.errorNotesConceptId
				+ "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + gxp.getErrorNotes() + "','Auto-saved by GXAlert.',");
		query.append(",1,'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	private StringBuilder getErrorCodeQuery(int patientId,
			int encounterLocationId, int gxAlertUserId, GeneXpertResult gxp,
			Integer encounterId, Date obsDate, String queryPrefix) {
		StringBuilder query = new StringBuilder(queryPrefix);
		query.append("(0," + patientId + "," + Constant.errorCodeConceptId
				+ "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
				+ encounterLocationId + ",NULL,NULL,NULL,");
		query.append(gxp.getErrorCode() + ",NULL,'Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
				+ "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	/**
	 * This method saves the Encounter part of GeneXpertResult
	 * 
	 * @param patientId
	 * @param encounterLocationId
	 * @param gxAlertUserId
	 * @param dateEncounterCreated
	 * @return
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private Integer saveGeneXpertEncounter(int patientId,
			int encounterLocationId, int gxAlertUserId,
			DateTime dateEncounterCreated) throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		StringBuilder query = new StringBuilder(
				"INSERT INTO openmrs.encounter (encounter_id, encounter_type, patient_id, location_id, encounter_datetime, creator, date_created, uuid) VALUES ");
		query.append("(0," + Constant.gxpEncounterType);
		query.append("," + patientId);
		query.append("," + encounterLocationId);
		query.append(",'"
				+ DateTimeUtil.toSqlDateString(dateEncounterCreated.toDate()));
		query.append("'," + gxAlertUserId);
		query.append(",current_timestamp()");
		query.append(",'" + UUID.randomUUID().toString() + "')");
		Integer encounterId = -1;
		// Yes. I know this makes little sense, but if you have any better
		// ideas to fetch encounter ID of the record just inserted, then be
		// my guest. And please try executeUpdate Statement yourself before
		// making that suggestion
		Connection con = dbUtil.getConnection();
		PreparedStatement ps = con.prepareStatement(query.toString());
		ps.executeUpdate(query.toString(), Statement.RETURN_GENERATED_KEYS);
		ResultSet rs = ps.getGeneratedKeys();
		if (rs.next()) {
			encounterId = rs.getInt(1);
		}
		rs.close();
		ps.close();
		return encounterId;
	}

}
