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
import java.io.PrintWriter;
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
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class GxAlertMain extends HttpServlet {

	private static final long serialVersionUID = -4482413651235453707L;
	private static final Logger log = Logger.getLogger(GxAlertMain.class);
	private static final String PROP_FILE_NAME = "gfatm-gxalert-integration.properties";
	private static Properties prop;
	private String baseUrl;
	private String apiKey;
	private String authentication;
	private int gxAlertUserId;
	private int fetchDurationDays;
	private DatabaseUtil dbUtil;

	public static void main(String[] args) {
		try {
			InputStream inputStream = Thread.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(PROP_FILE_NAME);
			prop = new Properties();
			prop.load(inputStream);
			GxAlertMain gxAlert = new GxAlertMain();
			gxAlert.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public GxAlertMain() {
		String username = GxAlertMain.prop.getProperty("connection.username");
		String password = GxAlertMain.prop.getProperty("connection.password");
		String url = GxAlertMain.prop.getProperty("connection.url");
		String dbName = GxAlertMain.prop
				.getProperty("connection.default_schema");
		String driverName = GxAlertMain.prop
				.getProperty("connection.driver_class");
		dbUtil = new DatabaseUtil(url, dbName, driverName, username, password);
		gxAlertUserId = Integer.parseInt(prop.getProperty(
				"gxalert.openmrs.user_id", "427"));
		baseUrl = prop.getProperty("gxalert.url");
		apiKey = prop.getProperty("gxalert.api_key");
		authentication = prop.getProperty("gxalert.authentication");
		fetchDurationDays = Integer.parseInt(prop.getProperty(
				"gxalert.fetch_duration_days", "30"));
	}

	/**
	 * Method to handle HTTP requests
	 * 
	 * @param request
	 * @param resp
	 * @throws IOException
	 * @throws JSONException
	 */
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse resp) throws IOException, JSONException {
		PrintWriter out = resp.getWriter();
		StringBuffer requestParams = new StringBuffer();
		Enumeration<?> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String value = request.getParameter(name);
			requestParams.append(name + "=");
			requestParams.append(value + "&");
		}
		JSONArray results = getGxAlertResults(requestParams.toString());
		if (results == null) {
			out.write("ERROR! Could not retrieve GXAlert results.");
		} else {
			out.write(results.toString());
		}
		out.close();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			handleRequest(req, resp);
			super.doGet(req, resp);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			handleRequest(req, resp);
			super.doPost(req, resp);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method fetches all encounters, which are missing GXP results and
	 * searches for respective results via GXAlert API. If a result is found, it
	 * is saved as a set of observations
	 */
	public void run() {
		// TODO: Remove the minusMonths() part on production
		DateTime from = new DateTime().minusMonths(3);
		from = from.minusDays(fetchDurationDays);
		DateTime to = new DateTime();
		StringBuilder query = new StringBuilder();
		query.append("select e.encounter_id, e.patient_id, pi.identifier, e.location_id, e.date_created from openmrs.encounter as e ");
		query.append("inner join openmrs.patient_identifier as pi on pi.identifier_type = 3 and pi.patient_id = e.patient_id and pi.preferred = 1 and pi.voided = 0 ");
		query.append("where e.encounter_type = " + Constant.gxpEncounterType
				+ " ");
		query.append("and e.date_created between date('"
				+ DateTimeUtil.toSqlDateTimeString(from.toDate())
				+ "') and date('"
				+ DateTimeUtil.toSqlDateTimeString(to.toDate()) + "') ");
		query.append("and not exists (select * from openmrs.obs where encounter_id = e.encounter_id and concept_id = "
				+ Constant.gxpResultConceptId + ")");
		// Search for all GX Test forms with missing results
		Object[][] list = dbUtil.getTableData(query.toString());
		// Get GXAlert user ID
		for (Object[] record : list) {
			try {
				Integer.parseInt(record[0].toString());
				int patientId = Integer.parseInt(record[1].toString());
				String identifier = record[2].toString();
				int encounterLocationId = Integer
						.parseInt(record[3].toString());
				Date date = DateTimeUtil.fromSqlDateTimeString(record[4]
						.toString());
				DateTime dateEncounterCreated = new DateTime(date.getTime());
				// Make GXAlert call and search for results for given unique
				// identifier
				JSONArray results = getGxAlertResults("patientId=" + identifier);
				if (results.length() == 0) {
					System.out.println("No result found for " + identifier);
					continue;
				}
				// For multiple results, search for the latest one
				JSONObject result = searchLatestResult(results);
				// Save GeneXpert result in OpenMRS
				GeneXpertResult geneXpertResult = new GeneXpertResult();
				geneXpertResult.fromJson(result);
				boolean success = saveGeneXpertResult(patientId,
						encounterLocationId, gxAlertUserId,
						dateEncounterCreated, geneXpertResult);
				System.out.println(success);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		StringBuilder query;
		Integer encounterId = saveGeneXpertEncounter(patientId,
				encounterLocationId, gxAlertUserId, dateEncounterCreated);
		query = new StringBuilder();
		Date obsDate = new Date();
		try {
			obsDate = gxp.getTestEndedOn();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String queryPrefix = "INSERT INTO openmrs.obs (obs_id,person_id,concept_id,encounter_id,obs_datetime,location_id,value_boolean,value_coded,value_datetime,value_numeric,value_text,comments,creator,date_created,voided,uuid) VALUES ";
		boolean mtbDetected = gxp.getMtbResult().equals("DETECTED");
		boolean error = gxp.getMtbResult().equals("ERROR");
		boolean noResult = gxp.getMtbResult().equals("NO RESULT");
		boolean invalid = gxp.getMtbResult().equals("INVALID");
		boolean rifDetected = gxp.getRifResult().equals("DETECTED");
		boolean rifIndeterminate = gxp.getMtbResult().equals("INDETERMINATE");
		String mtbBurden = "";

		// Queries for error observations
		if (gxp.getErrorCode() != null) {
			query = new StringBuilder(queryPrefix);
			query.append("(0," + patientId + "," + Constant.errorCodeConceptId
					+ "," + encounterId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
					+ encounterLocationId + ",NULL,NULL,NULL,");
			query.append(gxp.getErrorCode() + ",NULL,'Auto-saved by GXAlert.',");
			query.append(gxAlertUserId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
					+ "',0,'" + UUID.randomUUID().toString() + "')");
			queries.add(query.toString());
			query = new StringBuilder(queryPrefix);
			query.append("(0," + patientId + "," + Constant.errorNotesConceptId
					+ "," + encounterId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
					+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
			query.append("'" + gxp.getErrorNotes()
					+ "','Auto-saved by GXAlert.',");
			query.append(",1,'" + DateTimeUtil.toSqlDateTimeString(new Date())
					+ "',0,'" + UUID.randomUUID().toString() + "')");
			queries.add(query.toString());
		}

		// Query for GXP result observation
		query = new StringBuilder(queryPrefix);
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
		queries.add(query.toString());

		// Query for MTB Burden observation
		if (mtbDetected) {
			query = new StringBuilder(queryPrefix);
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
			queries.add(query.toString());
		}

		// Query for RIF result observation
		query = new StringBuilder(queryPrefix);
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
		queries.add(query.toString());

		// Query for Notes observation
		if (gxp.getNotes() != null) {
			String notes = gxp.getNotes();
			query = new StringBuilder(queryPrefix);
			query.append("(0," + patientId + "," + Constant.notesConceptId
					+ "," + encounterId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
					+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
			query.append("'" + notes + "','Auto-saved by GXAlert.',");
			query.append(gxAlertUserId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
					+ "',0,'" + UUID.randomUUID().toString() + "')");
			queries.add(query.toString());
		}

		// Query for Cartridge serial no. observation
		if (gxp.getCartridgeSerial() != null) {
			long cartridgeNo = gxp.getCartridgeSerial();
			query = new StringBuilder(queryPrefix);
			query.append("(0," + patientId + "," + Constant.cartridgeConceptId
					+ "," + encounterId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
					+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
			query.append("'" + cartridgeNo + "','Auto-saved by GXAlert.',");
			query.append(gxAlertUserId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
					+ "',0,'" + UUID.randomUUID().toString() + "')");
			queries.add(query.toString());
		}

		// Query for Sample ID observation
		if (gxp.getSampleId() != null) {
			final int sampleConceptId = 159968;
			String sampleId = gxp.getSampleId();
			query = new StringBuilder(queryPrefix);
			query.append("(0," + patientId + "," + sampleConceptId + ","
					+ encounterId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
					+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
			query.append("'" + sampleId + "','Auto-saved by GXAlert.',");
			query.append(gxAlertUserId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
					+ "',0,'" + UUID.randomUUID().toString() + "')");
			queries.add(query.toString());
		}

		// Query for Host ID observation
		if (gxp.getHostId() != null) {
			final int hostConceptId = 166069;
			String hostName = gxp.getHostId();
			query = new StringBuilder(queryPrefix);
			query.append("(0," + patientId + "," + hostConceptId + ","
					+ encounterId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
					+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
			query.append("'" + hostName + "','Auto-saved by GXAlert.',");
			query.append(gxAlertUserId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
					+ "',0,'" + UUID.randomUUID().toString() + "')");
			queries.add(query.toString());
		}

		// Query for Reagent Lot ID observation
		if (gxp.getReagentLotId() != null) {
			final int reagentLotConceptId = 166070;
			long reagentLotNo = gxp.getReagentLotId();
			query = new StringBuilder(queryPrefix);
			query.append("(0," + patientId + "," + reagentLotConceptId + ","
					+ encounterId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
					+ encounterLocationId + ",NULL,NULL,NULL,");
			query.append(reagentLotNo + ",NULL,'Auto-saved by GXAlert.',");
			query.append(gxAlertUserId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
					+ "',0,'" + UUID.randomUUID().toString() + "')");
			queries.add(query.toString());
		}

		// Query for Module serial no. observation
		if (gxp.getModuleSerial() != null) {
			final int moduleSerialConceptId = 166071;
			long moduleSerialNo = gxp.getModuleSerial();
			query = new StringBuilder(queryPrefix);
			query.append("(0," + patientId + "," + moduleSerialConceptId + ","
					+ encounterId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(obsDate) + "',"
					+ encounterLocationId + ",NULL,NULL,NULL,NULL,");
			query.append("'" + moduleSerialNo + "','Auto-saved by GXAlert.',");
			query.append(gxAlertUserId + ",");
			query.append("'" + DateTimeUtil.toSqlDateTimeString(new Date())
					+ "',0,'" + UUID.randomUUID().toString() + "')");
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
