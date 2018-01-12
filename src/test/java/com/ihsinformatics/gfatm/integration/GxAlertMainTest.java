/* Copyright(C) 2016 Interactive Health Solutions, Pvt. Ltd.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 3 of the License (GPLv3), or any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Interactive Health Solutions, info@ihsinformatics.com
You can also access the license on the internet at the address: http://www.gnu.org/licenses/gpl-3.0.html

Interactive Health Solutions, hereby disclaims all copyright interest in this program written by the contributors.
 */

/**
 * 
 */
package com.ihsinformatics.gfatm.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author owais.hussain@ihsinformatics.com
 *
 */
public class GxAlertMainTest {

	private static final String URL = "http://127.0.0.1:8888/gxalert.jsp";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	public JSONArray httpRequest(URL url) throws IOException {
		HttpURLConnection httpConnection = null;
		int responseCode = 0;
		httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setRequestMethod("GET");
		httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
		httpConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		httpConnection.setDoOutput(true);
		httpConnection.connect();
		responseCode = httpConnection.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		if (responseCode == HttpURLConnection.HTTP_OK) {
			InputStream inputStream = httpConnection.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader in = new BufferedReader(inputStreamReader);
			String inputLine;
			StringBuffer responseBuffer = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				responseBuffer.append(inputLine);
			}
			in.close();
			httpConnection.disconnect();
			return new JSONArray(responseBuffer.toString());
		}
		return null;
	}

	/**
	 * Test method to fetch GeneXpert results by Patient ID
	 */
	@Test
	public final void testGetGxAlertResultByPatientID() {
		String queryString = "?patientId=VFG2K-4";
		URL url;
		try {
			url = new URL(URL + queryString);
			JSONArray jsonArray = httpRequest(url);
			Assert.assertTrue("Should return at least one record.",
					jsonArray.length() > 0);
		} catch (Exception e) {
			Assert.fail("Exception: " + e.getMessage());
		}
	}

	/**
	 * Test method to fetch GeneXpert results by Cartridge ID
	 */
	@Test
	public final void testGetGxAlertResultByCartridgeSerial() {
		String queryString = "?cartridgeId=587024931";
		URL url;
		try {
			url = new URL(URL + queryString);
			JSONArray jsonArray = httpRequest(url);
			Assert.assertTrue("Should return at least one record.",
					jsonArray.length() > 0);
		} catch (Exception e) {
			Assert.fail("Exception: " + e.getMessage());
		}
	}

	/**
	 * Test method to fetch GeneXpert results by Cartridge ID
	 */
	@Test
	@Ignore
	public final void testGetGxAlertResultByDateFinished() {
		new DateTime().withDate(2017, 10, 1);
		String queryString = "???";
		URL url;
		try {
			url = new URL(URL + queryString);
			JSONArray jsonArray = httpRequest(url);
			Assert.assertTrue("Should return at least one record.",
					jsonArray.length() > 0);
		} catch (Exception e) {
			Assert.fail("Exception: " + e.getMessage());
		}
	}

	@Test
	public final void testSearchLatestGeneXpertResult() {
	}

	@Test
	public final void testSaveGeneXpertResult() {
	}

	@Test
	public final void testSaveGeneXpertEncounter() {
	}
}
