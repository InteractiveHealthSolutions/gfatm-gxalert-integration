/* Copyright(C) 2018 Interactive Health Solutions, Pvt. Ltd.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 3 of the License (GPLv3), or any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Interactive Health Solutions, info@ihsinformatics.com
You can also access the license on the internet at the address: http://www.gnu.org/licenses/gpl-3.0.html

Interactive Health Solutions, hereby disclaims all copyright interest in this program written by the contributors.
*/

package com.ihsinformatics.gfatm.integration.gxalert;

import java.io.BufferedReader;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ihsinformatics.gfatm.integration.gxalert.model.GeneXpertResult;
import com.ihsinformatics.gfatm.integration.gxalert.shared.Constant;
import com.ihsinformatics.util.CommandType;
import com.ihsinformatics.util.DatabaseUtil;
import com.ihsinformatics.util.DateTimeUtil;

/**
 * @author owais.hussain@ihsinformatics.com
 *
 */
public class GxAlertImportService {

	private static final Logger log = Logger.getLogger(GxAlertImportService.class);
	private static boolean testMode = false;

	private Properties properties;
	private DatabaseUtil dbUtil;

	private int gxAlertUserId;
	private int fetchDurationHours;
	private int fetchDelay = 100;
	private Map<String, Integer> locations;

	public GxAlertImportService(Properties properties) {
		this.properties = properties;
		fetchDurationHours = Integer.parseInt(properties.getProperty("gxalert.fetch_duration_hours", "24"));
		fetchDelay = Integer.parseInt(properties.getProperty("gxalert.fetch_delay", "100"));
		initDatabase();
		getLocations();
		setGxAlertUser();
	}

	/**
	 * Initialize database
	 */
	public void initDatabase() {
		// Initiate properties
		String dbUsername = properties.getProperty("connection.username");
		String dbPassword = properties.getProperty("connection.password");
		String url = properties.getProperty("connection.url");
		String dbName = properties.getProperty("connection.default_schema");
		String driverName = properties.getProperty("connection.driver_class");
		dbUtil = new DatabaseUtil(url, dbName, driverName, dbUsername, dbPassword);
		if (!dbUtil.tryConnection()) {
			log.fatal("Unable to connect with database using " + dbUtil.toString());
			System.exit(0);
		}
	}

	/**
	 * Get GXAlert user from properties and set mapping ID by searching in OpenMRS
	 */
	public void setGxAlertUser() {
		String username = properties.getProperty("gxalert.openmrs.username");
		Object userId = dbUtil.runCommand(CommandType.SELECT,
				"select user_id from openmrs.users where username = '" + username + "'");
		if (userId != null) {
			gxAlertUserId = Integer.parseInt(userId.toString());
			return;
		}
		gxAlertUserId = Integer.parseInt(properties.getProperty("gxalert.openmrs.user_id"));
	}

	/**
	 * Get all locations and store in memory to save extra query executions
	 */
	public void getLocations() {
		Object[][] data = dbUtil.getTableData("openmrs.location", "location_id,name", "retired=0", true);
		locations = new HashMap<String, Integer>(200);
		for (Object[] row : data) {
			Integer id = 1;
			try {
				id = Integer.parseInt(row[0].toString());
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			String name = row[1].toString();
			locations.put(name, id);
		}
	}

	/**
	 * Fetches last update date from Encounter table against
	 * {@code Constant.GXP_ENCOUNTER_TYPE}. If no results are there, then first date
	 * from Encounter table is picked.
	 * 
	 * @throws MalformedURLException
	 * @throws JSONException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws ParseException
	 * @throws SQLException
	 */
	public void importAuto() throws MalformedURLException, JSONException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, ParseException, SQLException {
		StringBuilder query = new StringBuilder();
		query.append(
				"select ifnull(max(encounter_datetime), (select min(encounter_datetime) from encounter)) as max_date from encounter where encounter_type = ");
		query.append(Constant.GXP_ENCOUNTER_TYPE);
		// Also restrict to the results entered by GXAlert user
		query.append(" and ");
		query.append("creator = " + gxAlertUserId);
		String dateStr = dbUtil.getValue(query.toString());
		DateTime start = new DateTime().minusHours(fetchDurationHours);
		if (dateStr != null) {
			start = new DateTime(DateTimeUtil.fromSqlDateString(dateStr));
		}
		// Ehtiyatan, wait for 1 hour after the test is complete
		DateTime end = new DateTime().minusHours(1);
		run(start, end);
	}

	/**
	 * Fetches last update date from Encounter table against
	 * {@code Constant.GXP_ENCOUNTER_TYPE} and for every date, new results are
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
	public void importAll() throws MalformedURLException, JSONException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, ParseException, SQLException {
		String dateStr = dbUtil.getValue(
				"select ifnull(max(encounter_datetime), (select min(encounter_datetime) from encounter)) as max_date from encounter where encounter_type = "
						+ Constant.GXP_ENCOUNTER_TYPE);
		DateTime start = new DateTime(DateTimeUtil.fromSqlDateString(dateStr));
		DateTime end = start.plusDays(1);
		while (end.isBeforeNow()) {
			run(start, end);
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
	public void importForDate(DateTime start) throws MalformedURLException, JSONException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, ParseException, SQLException {
		start = start.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
		DateTime end = start.plusDays(1).minusSeconds(1);
		run(start, end);
	}

	/**
	 * This method fetches all GXP results entered between given range of dates and
	 * processes them
	 * 
	 * @param start
	 * @param end
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SQLException
	 */
	public void run(DateTime start, DateTime end) throws MalformedURLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		StringBuilder param = new StringBuilder();
		param.append("start=" + DateTimeUtil.toSqlDateString(start.toDate()));
		param.append("&end=" + DateTimeUtil.toSqlDateString(end.toDate()));
		String baseUrl = properties.getProperty("gxalert.url");
		String apiKey = properties.getProperty("gxalert.api_key");
		String authentication = properties.getProperty("gxalert.authentication");
		JSONArray results = getGxAlertResults(baseUrl, apiKey, authentication, param.toString());
		if (results.length() == 0) {
			log.warn("No result found between " + start + " and " + end);
			return;
		}
		for (int i = 0; i < results.length(); i++) {
			JSONObject result = results.getJSONObject(i);
			// Save GeneXpert result in OpenMRS
			GeneXpertResult geneXpertResult = new GeneXpertResult();
			try {
				geneXpertResult.fromJson(result);
				processResult(geneXpertResult);
				// Add a fetchDelay between two iterations
				try {
					Thread.sleep(fetchDelay);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			} catch (Exception e1) {
				log.error(e1.getMessage());
			}
		}
	}

	/**
	 * Takes a GeneXpertResult object and stores into database after several
	 * validation checks
	 * 
	 * @param geneXpertResult
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void processResult(GeneXpertResult geneXpertResult)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		// Skip if the result is incomplete
		if (geneXpertResult.getTestEndedOn() == null) {
			log.info("Test " + geneXpertResult.getCartridgeSerial() + " is not yet complete.");
			return;
		}
		// Skip if the Patient ID scheme does not match
		if (!geneXpertResult.getPatientId().matches(Constant.PATIENT_ID_REGEX)) {
			// Try if it matches with Patient ID2
			log.warn("Patient ID " + geneXpertResult.getPatientId() + " is invalid!");
			if (geneXpertResult.getPatientId2() == null) {
				return;
			} else {
				if (geneXpertResult.getPatientId2().matches(Constant.PATIENT_ID_REGEX)) {
					// Swap both IDs
					String temp = geneXpertResult.getPatientId();
					geneXpertResult.setPatientId(geneXpertResult.getPatientId2());
					geneXpertResult.setPatientId2(temp);
				} else {
					log.error("Patient ID2 " + geneXpertResult.getPatientId2() + " is invalid!");
					return;
				}
			}
		}
		// Fetch patient ID against the given identifier in GXP test
		StringBuilder query = new StringBuilder("select patient_id from openmrs.patient_identifier where identifier='"
				+ geneXpertResult.getPatientId() + "'");
		log.info("Searching for Patient...");
		if (dbUtil == null) {
			initDatabase();
		}
		Object str = dbUtil.runCommand(CommandType.SELECT, query.toString());
		if (testMode) {
			log.info("Since we are in test mode, therefore picking randomly.");
			str = (2 + new Random().nextInt(1000));
		}
		if (str == null) {
			log.info("Patient ID " + geneXpertResult.getPatientId() + " not found.");
			return;
		}
		Integer patientId = Integer.parseInt(str.toString());
		// Fetch Location ID from Deployment Short Name
		Integer encounterLocationId = locations.get(geneXpertResult.getDeploymentName());
		DateTime dateCreated = new DateTime(geneXpertResult.getTestEndedOn().getTime());
		// If an observation is found with same Cartridge ID, then continue
		StringBuilder filter = new StringBuilder();
		filter.append("where concept_id = " + Constant.CARTRIDGE_CONCEPT);
		filter.append(" and value_text = '" + geneXpertResult.getCartridgeSerial() + "'");
		// Search for all GX Test forms with missing results
		long rows = -1;
		try {
			rows = dbUtil.getTotalRows("openmrs.obs", filter.toString());
		} catch (SQLException e1) {
			// Do nothing
		}
		if (rows > 0) {
			log.warn("Record already exists against Cartridge ID: " + geneXpertResult.getCartridgeSerial());
			return;
		}
		boolean success = saveGeneXpertResult(patientId, encounterLocationId, gxAlertUserId, dateCreated,
				geneXpertResult);
		log.info("Imported result for " + geneXpertResult.getPatientId() + (success ? ": YES" : ": NO"));
	}

	/**
	 * Searches for latest result in a given array
	 * 
	 * @param results
	 * @return
	 * @throws ParseException
	 * @throws JSONException
	 */
	public JSONObject searchLatestResult(JSONArray results) throws ParseException, JSONException {
		JSONObject latest = null;
		Date latestDate = new DateTime().minusYears(99).toDate();
		for (int i = 0; i < results.length(); i++) {
			JSONObject currentObj = results.getJSONObject(i);
			// Look for testStartedOn variable
			String startedOnStr = currentObj.getString("testStartedOn");
			Date startedOn = null;
			try {
				startedOn = DateTimeUtil.fromString(startedOnStr, "yyyy-MM-dd'T'HH:mm:ss");
			} catch (Exception e) {
				startedOn = DateTimeUtil.fromString(startedOnStr, "yyyy-MM-dd'T'HH:mm:ss'Z'");
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
	public JSONArray getGxAlertResults(String baseUrl, String apiKey, String authentication, String queryString)
			throws MalformedURLException, JSONException {
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
			log.error(e.getMessage());
		}
		if (response == null) {
			return null;
		}
		return new JSONArray(response.toString());
	}

	/**
	 * This lengthy method converts GeneXpert results in JSON format into SQL
	 * queries and saves in DB. If you have any refactoring ideas, you're welcome to
	 * do so
	 * 
	 * @param encounterId
	 * @param patientId
	 * @param dateEncounterCreated
	 * @param encounterLocationId
	 * @param geneXpertResult
	 * @return
	 * @throws Exception
	 */
	public boolean saveGeneXpertResult(int patientId, int encounterLocationId, int gxAlertUserId,
			DateTime dateEncounterCreated, GeneXpertResult gxp)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		List<String> queries = new ArrayList<String>();
		Integer encounterId = saveGeneXpertEncounter(patientId, encounterLocationId, gxAlertUserId,
				dateEncounterCreated);
		StringBuilder query;
		Date obsDate = new Date();
		try {
			obsDate = gxp.getTestEndedOn();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String insertQueryPrefix = "INSERT INTO openmrs.obs (obs_id,person_id,concept_id,encounter_id,obs_datetime,location_id,interpretation,value_coded,value_datetime,value_numeric,value_text,comments,creator,date_created,voided,uuid) VALUES ";

		// Queries for error observations
		if (gxp.getErrorCode() != null) {
			query = getErrorCodeQuery(patientId, encounterLocationId, gxAlertUserId, gxp, encounterId, obsDate,
					insertQueryPrefix);
			queries.add(query.toString());
			query = getErrorNotesQuery(patientId, encounterLocationId, gxp, encounterId, obsDate, insertQueryPrefix);
			queries.add(query.toString());
		}

		// Query for GXP result observation
		query = getGxpResultQuery(patientId, encounterLocationId, gxAlertUserId, encounterId, obsDate,
				insertQueryPrefix, gxp.getMtbResult());
		queries.add(query.toString());

		// Query for MTB Burden observation
		if (gxp.getMtbResult().equals(Constant.DETECTED)) {
			query = getMtbBurdenQuery(patientId, encounterLocationId, gxAlertUserId, encounterId, obsDate,
					insertQueryPrefix, gxp.getMtbBurden());
			queries.add(query.toString());
		}

		// Query for RIF result observation
		if (gxp.getRifResult() != null) {
			boolean rifDetected = gxp.getRifResult().equals(Constant.DETECTED);
			boolean rifIndeterminate = gxp.getMtbResult().equals(Constant.INDETERMINATE);
			query = getRifResultQuery(patientId, encounterLocationId, gxAlertUserId, encounterId, obsDate,
					insertQueryPrefix, rifDetected, rifIndeterminate);
			queries.add(query.toString());
		}

		// Query for Notes observation
		if (gxp.getNotes() != null) {
			String notes = gxp.getNotes().replaceAll("['\"]", "_");
			query = getNotesQuery(patientId, encounterLocationId, gxAlertUserId, encounterId, obsDate,
					insertQueryPrefix, notes);
			queries.add(query.toString());
		}

		// Query for Cartridge serial no. observation
		if (gxp.getCartridgeSerial() != null) {
			long cartridgeNo = gxp.getCartridgeSerial();
			query = getCartridgeNoQuery(patientId, encounterLocationId, gxAlertUserId, encounterId, obsDate,
					insertQueryPrefix, cartridgeNo);
			queries.add(query.toString());
		}

		// Query for Sample ID observation
		if (gxp.getSampleId() != null) {
			String sampleId = gxp.getSampleId();
			query = getSampleIdQuery(patientId, encounterLocationId, gxAlertUserId, encounterId, obsDate,
					insertQueryPrefix, sampleId);
			queries.add(query.toString());
		}

		// Query for Host ID observation
		if (gxp.getHostId() != null) {
			String hostName = gxp.getHostId();
			query = getHostNameQuery(patientId, encounterLocationId, gxAlertUserId, encounterId, obsDate,
					insertQueryPrefix, hostName);
			queries.add(query.toString());
		}

		// Query for Reagent Lot ID observation
		if (gxp.getReagentLotId() != null) {
			long reagentLotNo = gxp.getReagentLotId();
			query = getReagentLotNoQuery(patientId, encounterLocationId, gxAlertUserId, encounterId, obsDate,
					insertQueryPrefix, reagentLotNo);
			queries.add(query.toString());
		}

		// Query for Module serial no. observation
		if (gxp.getModuleSerial() != null) {
			long moduleSerialNo = gxp.getModuleSerial();
			query = getModuleSerialNoQuery(patientId, encounterLocationId, gxAlertUserId, encounterId, obsDate,
					insertQueryPrefix, moduleSerialNo);
			queries.add(query.toString());
		}
		for (String q : queries) {
			try {
				if (testMode) {
					log.info("Skipping query due to test mode." + q);
					return true;
				}
				dbUtil.runCommandWithException(CommandType.INSERT, q);
			} catch (Exception e) {
				StringBuilder message = new StringBuilder();
				message.append("Query failed: ");
				message.append(query);
				message.append("\r\nException: ");
				message.append(e.getMessage());
				log.error(message.toString());
			}
		}
		return true;
	}

	public StringBuilder getModuleSerialNoQuery(int patientId, int encounterLocationId, int gxAlertUserId,
			Integer encounterId, Date obsDate, String insertQueryPrefix, long moduleSerialNo) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.MODULE_SERIAL_CONCEPT + "," + encounterId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + moduleSerialNo + "','Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	public StringBuilder getReagentLotNoQuery(int patientId, int encounterLocationId, int gxAlertUserId,
			Integer encounterId, Date obsDate, String insertQueryPrefix, long reagentLotNo) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.REAGENT_LOT_CONCEPT + "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,NULL,NULL,");
		query.append(reagentLotNo + ",NULL,'Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	public StringBuilder getHostNameQuery(int patientId, int encounterLocationId, int gxAlertUserId,
			Integer encounterId, Date obsDate, String insertQueryPrefix, String hostName) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.HOST_CONCEPT + "," + encounterId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + hostName + "','Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	public StringBuilder getSampleIdQuery(int patientId, int encounterLocationId, int gxAlertUserId,
			Integer encounterId, Date obsDate, String insertQueryPrefix, String sampleId) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.SAMPLE_CONCEPT + "," + encounterId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + sampleId + "','Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	public StringBuilder getCartridgeNoQuery(int patientId, int encounterLocationId, int gxAlertUserId,
			Integer encounterId, Date obsDate, String insertQueryPrefix, long cartridgeNo) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.CARTRIDGE_CONCEPT + "," + encounterId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + cartridgeNo + "','Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	public StringBuilder getNotesQuery(int patientId, int encounterLocationId, int gxAlertUserId, Integer encounterId,
			Date obsDate, String insertQueryPrefix, String notes) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.NOTES_CONCEPT + "," + encounterId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + notes + "','Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	public StringBuilder getRifResultQuery(int patientId, int encounterLocationId, int gxAlertUserId,
			Integer encounterId, Date obsDate, String insertQueryPrefix, boolean rifDetected,
			boolean rifIndeterminate) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.RIF_RESULT_CONCEPT + "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,");
		if (rifDetected) {
			query.append(Constant.RIF_DETECTED_CONCEPT + ",");
		} else if (rifIndeterminate) {
			query.append(Constant.RIF_INDETERMINATE_CONCEPT + ",");
		} else {
			query.append(Constant.RIF_NOT_DETECTED_CONCEPT + ",");
		}
		query.append("NULL,NULL,NULL,'Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	public StringBuilder getMtbBurdenQuery(int patientId, int encounterLocationId, int gxAlertUserId,
			Integer encounterId, Date obsDate, String insertQueryPrefix, String mtbBurden) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.MTB_BURDEN_CONCEPT + "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,");
		if (mtbBurden.equals(Constant.HIGH)) {
			query.append(Constant.HIGH_CONCEPT + ",");
		} else if (mtbBurden.equals(Constant.MEDIUM)) {
			query.append(Constant.MEDIUM_CONCEPT + ",");
		} else if (mtbBurden.equals(Constant.LOW)) {
			query.append(Constant.LOW_CONCEPT + ",");
		} else if (mtbBurden.equals(Constant.VERY_LOW)) {
			query.append(Constant.VERY_LOW_CONCEPT + ",");
		} else if (mtbBurden.equals(Constant.TRACE)) {
			query.append(Constant.TRACE_CONCEPT + ",");
		}
		query.append("NULL,NULL,NULL,'Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	public StringBuilder getGxpResultQuery(int patientId, int encounterLocationId, int gxAlertUserId,
			Integer encounterId, Date obsDate, String insertQueryPrefix, String mtbResult) {
		boolean mtbDetected = mtbResult.equals(Constant.DETECTED);
		boolean error = mtbResult.equals(Constant.ERROR);
		boolean noResult = mtbResult.equals(Constant.NO_RESULT);
		boolean invalid = mtbResult.equals(Constant.INVALID);
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.GXP_RESULT_CONCEPT + "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,");
		if (mtbDetected) {
			query.append(Constant.MTB_DETECTED_CONCEPT + ",");
		} else if (error) {
			query.append(Constant.ERROR_CONCEPT + ",");
		} else if (noResult) {
			query.append(Constant.NO_RESULT_CONCEPT + ",");
		} else if (invalid) {
			query.append(Constant.INVALID_CONCEPT + ",");
		} else {
			query.append(Constant.MTB_NOT_DETECTED_CONCEPT + ",");
		}
		query.append("NULL,NULL,NULL,'Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	public StringBuilder getErrorNotesQuery(int patientId, int encounterLocationId, GeneXpertResult gxp,
			Integer encounterId, Date obsDate, String insertQueryPrefix) {
		StringBuilder query;
		query = new StringBuilder(insertQueryPrefix);
		query.append("(0," + patientId + "," + Constant.ERROR_NOTES_CONCEPT + "," + encounterId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,NULL,NULL,NULL,");
		query.append("'" + gxp.getErrorNotes() + "','Auto-saved by GXAlert.',");
		query.append(
				",1,'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
		return query;
	}

	public StringBuilder getErrorCodeQuery(int patientId, int encounterLocationId, int gxAlertUserId,
			GeneXpertResult gxp, Integer encounterId, Date obsDate, String queryPrefix) {
		StringBuilder query = new StringBuilder(queryPrefix);
		query.append("(0," + patientId + "," + Constant.ERROR_CODE_CONCEPT + "," + encounterId + ",");
		query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "'," + encounterLocationId + ",NULL,NULL,NULL,");
		query.append(gxp.getErrorCode() + ",NULL,'Auto-saved by GXAlert.',");
		query.append(gxAlertUserId + ",");
		query.append(
				"'" + DateTimeUtil.toSqlDateTimeString(new Date()) + "',0,'" + UUID.randomUUID().toString() + "')");
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
	public Integer saveGeneXpertEncounter(int patientId, int encounterLocationId, int gxAlertUserId,
			DateTime dateEncounterCreated)
			throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		StringBuilder query = new StringBuilder(
				"INSERT INTO openmrs.encounter (encounter_id, encounter_type, patient_id, location_id, encounter_datetime, creator, date_created, uuid) VALUES ");
		query.append("(0," + Constant.GXP_ENCOUNTER_TYPE);
		query.append("," + patientId);
		query.append("," + encounterLocationId);
		query.append(",'" + DateTimeUtil.toSqlDateString(dateEncounterCreated.toDate()));
		query.append("'," + gxAlertUserId);
		query.append(",current_timestamp()");
		query.append(",'" + UUID.randomUUID().toString() + "')");
		Integer encounterId = -1;
		// Yes. I know this makes little sense, but if you have any better
		// ideas to fetch encounter ID of the record just inserted, then be
		// my guest. And please try executeUpdate Statement yourself before
		// making "that" suggestion
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
