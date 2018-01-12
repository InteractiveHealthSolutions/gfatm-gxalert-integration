/* Copyright(C) 2017 Interactive Health Solutions, Pvt. Ltd.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 3 of the License (GPLv3), or any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Interactive Health Solutions, info@ihsinformatics.com
You can also access the license on the internet at the address: http://www.gnu.org/licenses/gpl-3.0.html

Interactive Health Solutions, hereby disclaims all copyright interest in this program written by the contributors.
 */

package com.ihsinformatics.gfatm.integration.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

/**
 * @author owais.hussain@ihsinformatics.com
 *
 */
public class GeneXpertResultTest {

	public static JSONArray jsonExamples;
	public static GeneXpertResult gxpExample;

	static {
		gxpExample = new GeneXpertResult("9988", "17-12-12-52356", 85223641L,
				"DETECTED", "MTB DETECTED MEDIUM|RIF NOT DETECTED", new Date(),
				new Date(), "owais.hussain");
		Map<String, Double> probeData = new HashMap<String, Double>();
		probeData.put("a", 0.0);
		probeData.put("b", null);
		probeData.put("c", 2.0);
		probeData.put("d", 9.0);
		probeData.put("e", null);
		probeData.put("aCt", -11.0);
		probeData.put("bCt", null);
		probeData.put("cCt", 35.25);
		probeData.put("dCt", 133.1);
		probeData.put("eCt", null);
		gxpExample.setProbeData(probeData);
		jsonExamples = new JSONArray();
		jsonExamples
				.put(new JSONObject(
						"{\"resultText\":\"MTB NOT DETECTED||\",\"notes\":\"LUH HYD\",\"patientId\":\"MUSHTAQUE\",\"testStartedOn\":\"2017-10-09T10:09:33\",\"instrumentSerial\":\"719504\",\"patientGender\":1,\"insertedOn\":\"2017-10-09T06:48:31\",\"errorCode\":null,\"assayVersion\":\"5\",\"errorNotes\":null,\"probeData\":{\"qc2Endpt\":0,\"a\":null,\"qc2\":null,\"b\":null,\"c\":null,\"eEndpt\":-2,\"d\":null,\"e\":null,\"spc\":null,\"bEndpt\":3,\"spcEndpt\":196,\"qc1Endpt\":0,\"bCt\":0,\"aCt\":0,\"cEndpt\":8,\"dCt\":0,\"cCt\":0,\"qc1Ct\":0,\"eCt\":0,\"qc2Ct\":0,\"aEndpt\":0,\"qc1\":null,\"spcCt\":25.3,\"dEndpt\":3},\"testEndedOn\":\"2017-10-09T11:51:09\",\"systemName\":null,\"computerName\":null,\"programName\":\"API User\",\"patientAge\":45,\"patientAgeMonths\":null,\"cartridgeSerial\":\"280042708\",\"patientAddress\":null,\"result2\":null,\"result1\":4,\"salt\":null,\"programCode\":null,\"sampleId\":\"S6-01-1995-17\",\"messageSentOn\":\"2017-10-10T04:41:03\",\"hostId\":\"LUH-HYD\",\"updatedOn\":\"2017-10-10T04:39:04\",\"patientPhone\":null,\"cartridgeExpirationDate\":\"2018-08-12T23:59:59\",\"patientAgeYears\":45,\"deviceSerial\":\"Cepheid-64261MT\",\"assay\":\"Xpert MTB-RIF Assay G4\",\"reagentLotId\":\"37606\",\"user\":\"LUH-HYD\",\"softwareVersion\":\"4.8\",\"moduleSerial\":\"685268\"}"));
		jsonExamples
				.put(new JSONObject(
						"{\"resultText\":\"MTB DETECTED LOW|Rif Resistance NOT DETECTED|\",\"notes\":\"TH TDM\",\"patientId\":\"MUSHTAQUE\",\"testStartedOn\":\"2017-10-16T10:23:07\",\"instrumentSerial\":\"808195\",\"patientGender\":1,\"insertedOn\":\"2017-10-16T07:04:14\",\"errorCode\":null,\"assayVersion\":\"5\",\"errorNotes\":null,\"probeData\":{\"qc2Endpt\":0,\"a\":null,\"qc2\":null,\"b\":null,\"c\":null,\"eEndpt\":125,\"d\":null,\"e\":null,\"spc\":null,\"bEndpt\":122,\"spcEndpt\":272,\"qc1Endpt\":0,\"bCt\":24.6,\"aCt\":23.6,\"cEndpt\":166,\"dCt\":24.8,\"cCt\":24.1,\"qc1Ct\":0,\"eCt\":25.3,\"qc2Ct\":0,\"aEndpt\":116,\"qc1\":null,\"spcCt\":25.3,\"dEndpt\":157},\"testEndedOn\":\"2017-10-16T12:05:06\",\"systemName\":null,\"computerName\":null,\"programName\":\"API User\",\"patientAge\":18,\"patientAgeMonths\":null,\"cartridgeSerial\":\"586024809\",\"patientAddress\":null,\"result2\":4,\"result1\":5,\"salt\":null,\"programCode\":null,\"sampleId\":\"S9-S15-0939-17\",\"messageSentOn\":\"2017-10-16T22:17:39\",\"hostId\":\"SIMS-Shahdadpur\",\"updatedOn\":\"2017-10-16T09:17:21\",\"patientPhone\":null,\"cartridgeExpirationDate\":\"2018-11-04T23:59:59\",\"patientAgeYears\":18,\"deviceSerial\":\"Cepheid-528264J\",\"assay\":\"Xpert MTB-RIF Assay G4\",\"reagentLotId\":\"22103\",\"user\":\"SIMs-Shahdadpur\",\"softwareVersion\":\"4.6a\",\"moduleSerial\":\"664853\"}"));
		jsonExamples
				.put(new JSONObject(
						"{\"resultText\":\"NO RESULT\",\"notes\":\"RBUT SHP\",\"patientId\":\"MUSHTAQUE\",\"testStartedOn\":\"2017-10-23T12:57:08\",\"instrumentSerial\":\"707414\",\"patientGender\":null,\"insertedOn\":\"2017-10-27T09:05:24\",\"errorCode\":null,\"assayVersion\":\"5\",\"errorNotes\":null,\"probeData\":{\"qc2Endpt\":0,\"a\":null,\"qc2\":null,\"b\":null,\"c\":null,\"eEndpt\":0,\"d\":null,\"e\":null,\"spc\":null,\"bEndpt\":0,\"spcEndpt\":0,\"qc1Endpt\":0,\"bCt\":0,\"aCt\":0,\"cEndpt\":0,\"dCt\":0,\"cCt\":0,\"qc1Ct\":0,\"eCt\":0,\"qc2Ct\":0,\"aEndpt\":0,\"qc1\":null,\"spcCt\":0,\"dEndpt\":0},\"testEndedOn\":\"2017-10-23T12:57:08\",\"systemName\":null,\"computerName\":null,\"programName\":\"API User\",\"patientAge\":null,\"patientAgeMonths\":null,\"cartridgeSerial\":\"578562868\",\"patientAddress\":null,\"result2\":null,\"result1\":7,\"salt\":null,\"programCode\":null,\"sampleId\":\"S17-01-00235-17\",\"messageSentOn\":\"2017-10-27T09:05:23\",\"hostId\":\"RBUT-Shikarpur\",\"updatedOn\":\"2017-10-27T09:05:24\",\"patientPhone\":null,\"cartridgeExpirationDate\":\"2018-10-21T23:59:59\",\"patientAgeYears\":null,\"deviceSerial\":\"tgm-06b653044b4\",\"assay\":\"Xpert MTB-RIF Assay G4\",\"reagentLotId\":\"21705\",\"user\":\"Zafarullah\",\"softwareVersion\":\"4.3\",\"moduleSerial\":\"607750\"}"));
		jsonExamples
				.put(new JSONObject(
						"{\"resultText\":\"ERROR\",\"notes\":\"RBUT SHP\",\"patientId\":\"MUSHTAQUE\",\"testStartedOn\":\"2017-10-23T14:33:11\",\"instrumentSerial\":\"707414\",\"patientGender\":null,\"insertedOn\":\"2017-10-27T09:05:36\",\"errorCode\":\"5007\",\"assayVersion\":\"5\",\"errorNotes\":\"Error 5007: [QC-2] probe check failed. Probe check value of 33.2 for reading number 2 was below the minimum of 34.0\",\"probeData\":{\"qc2Endpt\":0,\"a\":null,\"qc2\":null,\"b\":null,\"c\":null,\"eEndpt\":0,\"d\":null,\"e\":null,\"spc\":null,\"bEndpt\":0,\"spcEndpt\":0,\"qc1Endpt\":0,\"bCt\":0,\"aCt\":0,\"cEndpt\":0,\"dCt\":0,\"cCt\":0,\"qc1Ct\":0,\"eCt\":0,\"qc2Ct\":0,\"aEndpt\":0,\"qc1\":null,\"spcCt\":0,\"dEndpt\":0},\"testEndedOn\":\"2017-10-23T14:56:08\",\"systemName\":null,\"computerName\":null,\"programName\":\"API User\",\"patientAge\":null,\"patientAgeMonths\":null,\"cartridgeSerial\":\"578562850\",\"patientAddress\":null,\"result2\":null,\"result1\":3,\"salt\":null,\"programCode\":null,\"sampleId\":\"S17-01-02235-17\",\"messageSentOn\":\"2017-10-27T09:05:35\",\"hostId\":\"RBUT-Shikarpur\",\"updatedOn\":\"2017-10-27T09:05:36\",\"patientPhone\":null,\"cartridgeExpirationDate\":\"2018-10-21T23:59:59\",\"patientAgeYears\":null,\"deviceSerial\":\"tgm-06b653044b4\",\"assay\":\"Xpert MTB-RIF Assay G4\",\"reagentLotId\":\"21705\",\"user\":\"Zafarullah\",\"softwareVersion\":\"4.3\",\"moduleSerial\":\"605694\"}"));
		jsonExamples
				.put(new JSONObject(
						"{\"resultText\":\"MTB NOT DETECTED\",\"notes\":\"RBUT SHP\",\"patientId\":\"MUSHTAQUE\",\"testStartedOn\":\"2017-10-25T15:22:54\",\"instrumentSerial\":\"707414\",\"patientGender\":null,\"insertedOn\":\"2017-10-27T09:06:29\",\"errorCode\":null,\"assayVersion\":\"5\",\"errorNotes\":null,\"probeData\":{\"qc2Endpt\":0,\"a\":null,\"qc2\":null,\"b\":null,\"c\":null,\"eEndpt\":-4,\"d\":null,\"e\":null,\"spc\":null,\"bEndpt\":1,\"spcEndpt\":255,\"qc1Endpt\":0,\"bCt\":0,\"aCt\":0,\"cEndpt\":4,\"dCt\":0,\"cCt\":0,\"qc1Ct\":0,\"eCt\":0,\"qc2Ct\":0,\"aEndpt\":2,\"qc1\":null,\"spcCt\":26,\"dEndpt\":-1},\"testEndedOn\":\"2017-10-25T17:03:22\",\"systemName\":null,\"computerName\":null,\"programName\":\"API User\",\"patientAge\":null,\"patientAgeMonths\":null,\"cartridgeSerial\":\"280040188\",\"patientAddress\":null,\"result2\":null,\"result1\":4,\"salt\":null,\"programCode\":null,\"sampleId\":\"01-0244-17\",\"messageSentOn\":\"2017-10-27T09:06:28\",\"hostId\":\"RBUT-Shikarpur\",\"updatedOn\":\"2017-10-27T09:06:29\",\"patientPhone\":null,\"cartridgeExpirationDate\":\"2018-08-12T23:59:59\",\"patientAgeYears\":null,\"deviceSerial\":\"tgm-06b653044b4\",\"assay\":\"Xpert MTB-RIF Assay G4\",\"reagentLotId\":\"37606\",\"user\":\"Zafarullah\",\"softwareVersion\":\"4.3\",\"moduleSerial\":\"631478\"}"));
		jsonExamples
				.put(new JSONObject(
						"{\"resultText\":\"MTB DETECTED MEDIUM|Rif Resistance NOT DETECTED\",\"notes\":\"TBH KHAIRPUR\",\"patientId\":\"MUSHTAQUE\",\"testStartedOn\":\"2017-11-18T09:54:50\",\"instrumentSerial\":\"722049\",\"patientGender\":1,\"insertedOn\":\"2017-11-18T06:36:00\",\"errorCode\":null,\"assayVersion\":\"5\",\"errorNotes\":null,\"probeData\":{\"qc2Endpt\":0,\"a\":null,\"qc2\":null,\"b\":null,\"c\":null,\"eEndpt\":111,\"d\":null,\"e\":null,\"spc\":null,\"bEndpt\":99,\"spcEndpt\":236,\"qc1Endpt\":0,\"bCt\":21.8,\"aCt\":19.7,\"cEndpt\":196,\"dCt\":20.9,\"cCt\":20.4,\"qc1Ct\":0,\"eCt\":21.6,\"qc2Ct\":0,\"aEndpt\":133,\"qc1\":null,\"spcCt\":26.3,\"dEndpt\":212},\"testEndedOn\":\"2017-11-18T11:36:43\",\"systemName\":null,\"computerName\":null,\"programName\":\"API User\",\"patientAge\":40,\"patientAgeMonths\":-1,\"cartridgeSerial\":\"277697115\",\"patientAddress\":null,\"result2\":4,\"result1\":5,\"salt\":null,\"programCode\":null,\"sampleId\":\"S9-01-0959-17\",\"messageSentOn\":\"2017-11-18T07:54:41\",\"hostId\":\"TB Hospital-Khairpur\",\"updatedOn\":\"2017-11-18T07:54:40\",\"patientPhone\":null,\"cartridgeExpirationDate\":\"2018-08-19T23:59:59\",\"patientAgeYears\":40,\"deviceSerial\":\"CepheidAdmin-PC\",\"assay\":\"Xpert MTB-RIF Assay G4\",\"reagentLotId\":\"37504\",\"user\":\"TBH-KHAIRPUR\",\"softwareVersion\":\"4.8\",\"moduleSerial\":\"693876\"}"));
		jsonExamples
				.put(new JSONObject(
						"{\"resultText\":\"MTB DETECTED LOW|Rif Resistance NOT DETECTED\",\"notes\":\"ICD KOTRI\",\"patientId\":\"MUSHTAQUE\",\"testStartedOn\":\"2017-11-24T10:10:03\",\"instrumentSerial\":\"719355\",\"patientGender\":1,\"insertedOn\":\"2017-11-24T06:50:35\",\"errorCode\":null,\"assayVersion\":\"5\",\"errorNotes\":null,\"probeData\":{\"qc2Endpt\":0,\"a\":null,\"qc2\":null,\"b\":null,\"c\":null,\"eEndpt\":104,\"d\":null,\"e\":null,\"spc\":null,\"bEndpt\":108,\"spcEndpt\":230,\"qc1Endpt\":0,\"bCt\":23.5,\"aCt\":22.4,\"cEndpt\":173,\"dCt\":23.4,\"cCt\":22.8,\"qc1Ct\":0,\"eCt\":24,\"qc2Ct\":0,\"aEndpt\":110,\"qc1\":null,\"spcCt\":28.7,\"dEndpt\":160},\"testEndedOn\":\"2017-11-24T11:51:16\",\"systemName\":null,\"computerName\":null,\"programName\":\"API User\",\"patientAge\":60,\"patientAgeMonths\":-1,\"cartridgeSerial\":\"586026395\",\"patientAddress\":null,\"result2\":4,\"result1\":5,\"salt\":null,\"programCode\":null,\"sampleId\":\"S2-01-7044-17\",\"messageSentOn\":\"2017-11-25T05:18:51\",\"hostId\":\"ICD-Kotri\",\"updatedOn\":\"2017-11-25T05:18:50\",\"patientPhone\":null,\"cartridgeExpirationDate\":\"2018-11-04T23:59:59\",\"patientAgeYears\":60,\"deviceSerial\":\"cephied-PC\",\"assay\":\"Xpert MTB-RIF Assay G4\",\"reagentLotId\":\"22103\",\"user\":\"ICD-KOTRI\",\"softwareVersion\":\"4.7b\",\"moduleSerial\":\"685257\"}"));
	}

	/**
	 * Test method for
	 * {@link com.ihsinformatics.gfatmweb.shared.model.GeneXpertResult#fromJson(org.json.JSONObject)}
	 * .
	 */
	@Test
	public final void testFromJson() {
		GeneXpertResult obj = new GeneXpertResult();
		for (int i = 0; i < jsonExamples.length(); i++) {
			try {
				JSONObject json = jsonExamples.getJSONObject(i);
				obj.fromJson(json);
				assertNotNull("Should convert from JSON", obj);
				if (obj.getMtbResult().equals("ERROR")) {
					assertNotNull(
							"Error code should be shown in case of error.",
							obj.getErrorCode());
				}
			} catch (Exception e) {
				e.printStackTrace();
				fail("Unable to convert from JSON object");
			}
		}
	}

	/**
	 * Test method for
	 * {@link com.ihsinformatics.gfatmweb.shared.model.GeneXpertResult#toJson()}
	 * .
	 */
	@Test
	public final void testToJson() {
		JSONObject json = gxpExample.toJson();
		assertTrue(
				"Should contain all keys.",
				json.has("patientId") && json.has("sampleId")
						&& json.has("resultText") && json.has("user")
						&& json.has("probeData"));
	}
}
