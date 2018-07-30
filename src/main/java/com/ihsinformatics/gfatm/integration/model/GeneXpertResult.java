/* Copyright(C) 2017 Interactive Health Solutions, Pvt. Ltd.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 3 of the License (GPLv3), or any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Interactive Health Solutions, info@ihsinformatics.com
You can also access the license on the internet at the address: http://www.gnu.org/licenses/gpl-3.0.html

Interactive Health Solutions, hereby disclaims all copyright interest in this program written by the contributors.
 */

package com.ihsinformatics.gfatm.integration.model;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.ihsinformatics.util.DateTimeUtil;

/**
 * @author owais.hussain@ihsinformatics.com
 *
 */
public class GeneXpertResult implements Serializable {

	private static final long serialVersionUID = 1652520571460665819L;
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	private String patientId;
	private String patientId2;
	private Integer patientAge;
	private Character patientGender;
	private String sampleId;
	private Long cartridgeSerial;
	private String mtbResult;
	private String mtbBurden;
	private String rifResult;
	private String resultText;
	private Integer result1;
	private Integer result2;
	private String hostId;
	private String deploymentName;
	private Date testStartedOn;
	private Date testEndedOn;
	private Date updatedOn;
	private Integer errorCode;
	private String errorNotes;
	private String notes;
	private String user;
	private Long reagentLotId;
	private Long moduleSerial;
	private Long instrumentSerial;
	private Date cartridgeExpirationDate;
	private String computerName;
	private String deviceSerial;
	private String assay;
	private String softwareVersion;
	private Map<String, Double> probeData;

	public GeneXpertResult() {
	}

	/**
	 * @param patientId
	 * @param sampleId
	 * @param cartridgeSerial
	 * @param mtbResult
	 * @param resultText
	 * @param testStartedOn
	 * @param testEndedOn
	 * @param user
	 */
	public GeneXpertResult(String patientId, String sampleId, Long cartridgeSerial, String mtbResult, String resultText,
			Date testStartedOn, Date testEndedOn, String user) {
		super();
		this.patientId = patientId;
		this.sampleId = sampleId;
		this.cartridgeSerial = cartridgeSerial;
		this.mtbResult = mtbResult;
		this.resultText = resultText;
		this.testStartedOn = testStartedOn;
		this.testEndedOn = testEndedOn;
		this.user = user;
	}

	/**
	 * @param patientId
	 * @param patientId2
	 * @param patientAge
	 * @param patientGender
	 * @param sampleId
	 * @param cartridgeSerial
	 * @param mtbResult
	 * @param mtbBurden
	 * @param rifResult
	 * @param resultText
	 * @param hostId
	 * @param deploymentName
	 * @param testStartedOn
	 * @param testEndedOn
	 * @param updatedOn
	 * @param errorCode
	 * @param errorNotes
	 * @param notes
	 * @param user
	 * @param reagentLotId
	 * @param moduleSerial
	 * @param instrumentSerial
	 * @param cartridgeExpirationDate
	 * @param computerName
	 * @param deviceSerial
	 * @param assay
	 * @param softwareVersion
	 * @param probeData
	 */
	public GeneXpertResult(String patientId, String patientId2, Integer patientAge, Character patientGender,
			String sampleId, Long cartridgeSerial, String mtbResult, String mtbBurden, String rifResult,
			String resultText, Integer result1, Integer result2, String hostId, String deploymentName,
			Date testStartedOn, Date testEndedOn, Date updatedOn, Integer errorCode, String errorNotes, String notes,
			String user, Long reagentLotId, Long moduleSerial, Long instrumentSerial, Date cartridgeExpirationDate,
			String computerName, String deviceSerial, String assay, String softwareVersion,
			Map<String, Double> probeData) {
		super();
		this.patientId = patientId;
		this.patientId2 = patientId2;
		this.patientAge = patientAge;
		this.patientGender = patientGender;
		this.sampleId = sampleId;
		this.cartridgeSerial = cartridgeSerial;
		this.mtbResult = mtbResult;
		this.mtbBurden = mtbBurden;
		this.rifResult = rifResult;
		this.resultText = resultText;
		this.setResult1(result1);
		this.setResult2(result2);
		this.deploymentName = deploymentName;
		this.hostId = hostId;
		this.testStartedOn = testStartedOn;
		this.testEndedOn = testEndedOn;
		this.updatedOn = updatedOn;
		this.errorCode = errorCode;
		this.errorNotes = errorNotes;
		this.notes = notes;
		this.user = user;
		this.reagentLotId = reagentLotId;
		this.moduleSerial = moduleSerial;
		this.instrumentSerial = instrumentSerial;
		this.cartridgeExpirationDate = cartridgeExpirationDate;
		this.computerName = computerName;
		this.deviceSerial = deviceSerial;
		this.assay = assay;
		this.softwareVersion = softwareVersion;
		this.probeData = probeData;
	}

	/**
	 * @return the patientId
	 */
	public String getPatientId() {
		return patientId;
	}

	/**
	 * @param patientId
	 *            the patientId to set
	 */
	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	/**
	 * @return the patientId2
	 */
	public String getPatientId2() {
		return patientId2;
	}

	/**
	 * @param patientId2
	 *            the patientId2 to set
	 */
	public void setPatientId2(String patientId2) {
		this.patientId2 = patientId2;
	}

	/**
	 * @return the patientAge
	 */
	public Integer getPatientAge() {
		return patientAge;
	}

	/**
	 * @param patientAge
	 *            the patientAge to set
	 */
	public void setPatientAge(Integer patientAge) {
		this.patientAge = patientAge;
	}

	/**
	 * @return the patientGender
	 */
	public Character getPatientGender() {
		return patientGender;
	}

	/**
	 * @param patientGender
	 *            the patientGender to set
	 */
	public void setPatientGender(Character patientGender) {
		this.patientGender = patientGender;
	}

	/**
	 * @return the sampleId
	 */
	public String getSampleId() {
		return sampleId;
	}

	/**
	 * @param sampleId
	 *            the sampleId to set
	 */
	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}

	/**
	 * @return the cartridgeSerial
	 */
	public Long getCartridgeSerial() {
		return cartridgeSerial;
	}

	/**
	 * @param cartridgeSerial
	 *            the cartridgeSerial to set
	 */
	public void setCartridgeSerial(Long cartridgeSerial) {
		this.cartridgeSerial = cartridgeSerial;
	}

	/**
	 * @return the mtbResult
	 */
	public String getMtbResult() {
		return mtbResult;
	}

	/**
	 * @param mtbResult
	 *            the mtbResult to set
	 */
	public void setMtbResult(String mtbResult) {
		this.mtbResult = mtbResult;
	}

	/**
	 * @return the mtbBurden
	 */
	public String getMtbBurden() {
		return mtbBurden;
	}

	/**
	 * @param mtbBurden
	 *            the mtbBurden to set
	 */
	public void setMtbBurden(String mtbBurden) {
		this.mtbBurden = mtbBurden;
	}

	/**
	 * @return the rifResult
	 */
	public String getRifResult() {
		return rifResult;
	}

	/**
	 * @param rifResult
	 *            the rifResult to set
	 */
	public void setRifResult(String rifResult) {
		this.rifResult = rifResult;
	}

	/**
	 * @return the resultText
	 */
	public String getResultText() {
		return resultText;
	}

	/**
	 * @param resultText
	 *            the resultText to set
	 */
	public void setResultText(String resultText) {
		this.resultText = resultText;
	}

	/**
	 * @return the result1
	 */
	public Integer getResult1() {
		return result1;
	}

	/**
	 * @param result1
	 *            the result1 to set
	 */
	public void setResult1(Integer result1) {
		this.result1 = result1;
	}

	/**
	 * @return the result2
	 */
	public Integer getResult2() {
		return result2;
	}

	/**
	 * @param result2
	 *            the result2 to set
	 */
	public void setResult2(Integer result2) {
		this.result2 = result2;
	}

	/**
	 * @return the hostId
	 */
	public String getHostId() {
		return hostId;
	}

	/**
	 * @param hostId
	 *            the hostId to set
	 */
	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	/**
	 * @return the deploymentName
	 */
	public String getDeploymentName() {
		return deploymentName;
	}

	/**
	 * @param deploymentName
	 *            the deploymentName to set
	 */
	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}

	/**
	 * @return the testStartedOn
	 */
	public Date getTestStartedOn() {
		return testStartedOn;
	}

	/**
	 * @param testStartedOn
	 *            the testStartedOn to set
	 */
	public void setTestStartedOn(Date dateStarted) {
		this.testStartedOn = dateStarted;
	}

	/**
	 * @return the testEndedOn
	 */
	public Date getTestEndedOn() {
		return testEndedOn;
	}

	/**
	 * @param testEndedOn
	 *            the testEndedOn to set
	 */
	public void setTestEndedOn(Date dateEnded) {
		this.testEndedOn = dateEnded;
	}

	/**
	 * @return the updatedOn
	 */
	public Date getUpdatedOn() {
		return updatedOn;
	}

	/**
	 * @param updatedOn
	 *            the updatedOn to set
	 */
	public void setUpdatedOn(Date dateUpdated) {
		this.updatedOn = dateUpdated;
	}

	/**
	 * @return the errorCode
	 */
	public Integer getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode
	 *            the errorCode to set
	 */
	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorNotes
	 */
	public String getErrorNotes() {
		return errorNotes;
	}

	/**
	 * @param errorNotes
	 *            the errorNotes to set
	 */
	public void setErrorNotes(String errorNotes) {
		this.errorNotes = errorNotes;
	}

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes
	 *            the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the reagentLotId
	 */
	public Long getReagentLotId() {
		return reagentLotId;
	}

	/**
	 * @param reagentLotId
	 *            the reagentLotId to set
	 */
	public void setReagentLotId(Long reagentLotId) {
		this.reagentLotId = reagentLotId;
	}

	/**
	 * @return the moduleSerial
	 */
	public Long getModuleSerial() {
		return moduleSerial;
	}

	/**
	 * @param moduleSerial
	 *            the moduleSerial to set
	 */
	public void setModuleSerial(Long moduleSerial) {
		this.moduleSerial = moduleSerial;
	}

	/**
	 * @return the instrumentSerial
	 */
	public Long getInstrumentSerial() {
		return instrumentSerial;
	}

	/**
	 * @param instrumentSerial
	 *            the instrumentSerial to set
	 */
	public void setInstrumentSerial(Long instrumentSerial) {
		this.instrumentSerial = instrumentSerial;
	}

	/**
	 * @return the cartridgeExpirationDate
	 */
	public Date getCartridgeExpiryDate() {
		return cartridgeExpirationDate;
	}

	/**
	 * @param cartridgeExpirationDate
	 *            the cartridgeExpirationDate to set
	 */
	public void setCartridgeExpiryDate(Date cartridgeExpiry) {
		this.cartridgeExpirationDate = cartridgeExpiry;
	}

	/**
	 * @return the computerName
	 */
	public String getComputerName() {
		return computerName;
	}

	/**
	 * @param computerName
	 *            the computerName to set
	 */
	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}

	/**
	 * @return the deviceSerial
	 */
	public String getDeviceSerial() {
		return deviceSerial;
	}

	/**
	 * @param deviceSerial
	 *            the deviceSerial to set
	 */
	public void setDeviceSerial(String deviceSerial) {
		this.deviceSerial = deviceSerial;
	}

	/**
	 * @return the assay
	 */
	public String getAssay() {
		return assay;
	}

	/**
	 * @param assay
	 *            the assay to set
	 */
	public void setAssay(String assay) {
		this.assay = assay;
	}

	/**
	 * @return the softwareVersion
	 */
	public String getSoftwareVersion() {
		return softwareVersion;
	}

	/**
	 * @param softwareVersion
	 *            the softwareVersion to set
	 */
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	/**
	 * @return the probeData
	 */
	public Map<String, Double> getProbeData() {
		return probeData;
	}

	/**
	 * @return
	 */
	public JSONObject getProbeDataJson() {
		if (probeData == null) {
			return null;
		}
		JSONObject json = new JSONObject();
		Set<String> keySet = probeData.keySet();
		for (String key : keySet) {
			json.put(key, probeData.get(key));
		}
		return json;
	}

	/**
	 * @param probeData
	 *            the probeData to set
	 */
	public void setProbeData(Map<String, Double> probeData) {
		this.probeData = probeData;
	}

	/**
	 * @param probeDataJson
	 *            the probeDataJson to set
	 */
	public void setProbeData(JSONObject probeDataJson) {
		probeData = new HashMap<String, Double>();
		Set<String> keySet = probeDataJson.keySet();
		for (String key : keySet) {
			try {
				probeData.put(key, probeDataJson.getDouble(key));
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Read object from JSON and set current
	 * 
	 * @param json
	 * @throws ParseException
	 * @throws Exception
	 */
	public void fromJson(JSONObject json) throws ParseException {
		if (json.has("patientId")) {
			patientId = String.valueOf(json.get("patientId"));
		}
		if (json.has("patientId2")) {
			patientId2 = String.valueOf(json.get("patientId2"));
		}
		if (json.has("patientAge")) {
			if (!json.isNull("patientAge")) {
				patientAge = json.getInt("patientAge");
			}
		}
		if (json.has("patientGender")) {
			if (!json.isNull("patientGender")) {
				patientGender = json.getInt("patientGender") == 1 ? 'M' : 'F';
			}
		}
		if (json.has("sampleId")) {
			sampleId = String.valueOf(json.get("sampleId"));
		}
		if (json.has("cartridgeSerial")) {
			cartridgeSerial = json.getLong("cartridgeSerial");
		}
		if (json.has("sampleId")) {
			sampleId = String.valueOf(json.get("sampleId"));
		}
		if (json.has("hostId")) {
			hostId = String.valueOf(json.get("hostId"));
		}
		if (json.has("deploymentShortName")) {
			deploymentName = String.valueOf(json.get("deploymentShortName"));
		}
		if (json.has("testStartedOn")) {
			String dateStr = String.valueOf(json.get("testStartedOn"));
			testStartedOn = DateTimeUtil.fromString(dateStr, DEFAULT_DATE_FORMAT);
		}
		if (json.has("testEndedOn")) {
			String dateStr = String.valueOf(json.get("testEndedOn"));
			testEndedOn = DateTimeUtil.fromString(dateStr, DEFAULT_DATE_FORMAT);
		}
		if (json.has("updatedOn")) {
			String dateStr = String.valueOf(json.get("updatedOn"));
			updatedOn = DateTimeUtil.fromString(dateStr, DEFAULT_DATE_FORMAT);
		}
		if (json.has("notes")) {
			notes = String.valueOf(json.get("notes"));
		}
		if (json.has("user")) {
			user = String.valueOf(json.get("user"));
		}
		if (json.has("reagentLotId")) {
			reagentLotId = json.getLong("reagentLotId");
		}
		if (json.has("moduleSerial")) {
			moduleSerial = json.getLong("moduleSerial");
		}
		if (json.has("instrumentSerial")) {
			instrumentSerial = json.getLong("instrumentSerial");
		}
		if (json.has("cartridgeExpirationDate")) {
			String dateStr = String.valueOf(json.get("cartridgeExpirationDate"));
			cartridgeExpirationDate = DateTimeUtil.fromString(dateStr, DEFAULT_DATE_FORMAT);
		}
		if (json.has("computerName")) {
			computerName = String.valueOf(json.get("computerName"));
		}
		if (json.has("deviceSerial")) {
			deviceSerial = String.valueOf(json.get("deviceSerial"));
		}
		if (json.has("assay")) {
			assay = String.valueOf(json.get("assay"));
		}
		if (json.has("softwareVersion")) {
			softwareVersion = String.valueOf(json.get("softwareVersion"));
		}
		if (json.has("resultText")) {
			parseResults(json);
		}
	}

	/**
	 * Parse the results and assigns values to respective attributes
	 * 
	 * @param json
	 */
	public void parseResults(JSONObject json) {
		resultText = json.getString("resultText").toUpperCase();
		if (resultText.equals("ERROR") || resultText.equals("INVALID") || resultText.equals("NO RESULT")) {
			mtbResult = resultText;
			if (json.has("errorCode")) {
				if (!json.isNull("errorCode")) {
					errorCode = json.getInt("errorCode");
				}
			}
			if (json.has("errorNotes")) {
				errorNotes = String.valueOf(json.get("errorNotes"));
			}
		} else {
			mtbResult = (resultText.startsWith("MTB DETECTED") || resultText.contains("TRACE"))
					? "DETECTED"
					: "NOT DETECTED";
			if (mtbResult.equals("DETECTED")) {
				if (resultText.contains("MTB DETECTED HIGH")) {
					mtbBurden = "HIGH";
				} else if (resultText.contains("MTB DETECTED MEDIUM")) {
					mtbBurden = "MEDIUM";
				} else if (resultText.contains("MTB DETECTED LOW")) {
					mtbBurden = "LOW";
				} else if (resultText.contains("MTB DETECTED VERY LOW")) {
					mtbBurden = "VERY LOW";
				} else {
					mtbBurden = "TRACE";
				}
			}
			if (resultText.contains("RIF RESISTANCE DETECTED")) {
				rifResult = "DETECTED";
			} else if (resultText.contains("RIF RESISTANCE INDETERMINATE")) {
				rifResult = "INDETERMINATE";
			} else {
				rifResult = "NOT DETECTED";
			}
			if (json.has("probeData")) {
				setProbeData(json.getJSONObject("probeData"));
			}
		}
		if (json.has("result1")) {
			if (!json.isNull("result1")) {
				result1 = json.getInt("result1");
			}
		}
		if (json.has("result2")) {
			if (!json.isNull("result2")) {
				result2 = json.getInt("result2");
			}
		}
	}

	/**
	 * Convert current object into JSON
	 * 
	 * @return
	 */
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("patientId", patientId);
		json.put("patientId2", patientId2);
		json.put("patientAge", patientAge);
		json.put("patientGender", patientGender);
		json.put("sampleId", sampleId);
		json.put("cartridgeSerial", cartridgeSerial);
		json.put("mtbResult", mtbResult);
		json.put("mtbBurden", mtbBurden);
		json.put("rifResult", rifResult);
		json.put("resultText", resultText);
		json.put("hostId", hostId);
		json.put("deploymentName", deploymentName);
		json.put("testStartedOn", testStartedOn == null ? null : DateTimeUtil.toSqlDateString(testStartedOn));
		json.put("testEndedOn", testEndedOn == null ? null : DateTimeUtil.toSqlDateString(testEndedOn));
		json.put("updatedOn", updatedOn == null ? null : DateTimeUtil.toSqlDateString(updatedOn));
		json.put("errorCode", errorCode);
		json.put("errorNotes", errorNotes);
		json.put("notes", notes);
		json.put("user", user);
		json.put("reagentLotId", reagentLotId);
		json.put("moduleSerial", moduleSerial);
		json.put("instrumentSerial", instrumentSerial);
		json.put("cartridgeExpirationDate",
				cartridgeExpirationDate == null ? null : DateTimeUtil.toSqlDateString(cartridgeExpirationDate));
		json.put("computerName", computerName);
		json.put("deviceSerial", deviceSerial);
		json.put("assay", assay);
		json.put("softwareVersion", softwareVersion);
		json.put("probeData", getProbeDataJson());
		return json;
	}

	@Override
	public String toString() {
		return patientId + ", " + patientId2 + ", " + patientAge + ", " + patientGender + ", " + sampleId + ", "
				+ cartridgeSerial + ", " + mtbResult + ", " + mtbBurden + ", " + rifResult + ", " + resultText + ", "
				+ hostId + ", " + deploymentName + ", " + testStartedOn + ", " + testEndedOn + ", " + updatedOn + ", "
				+ errorCode + ", " + errorNotes + ", " + notes + ", " + user + ", " + reagentLotId + ", " + moduleSerial
				+ ", " + instrumentSerial + ", " + cartridgeExpirationDate + ", " + computerName + ", " + deviceSerial
				+ ", " + assay + ", " + softwareVersion + ", " + "probeData={" + probeData.toString() + "}";
	}
}
