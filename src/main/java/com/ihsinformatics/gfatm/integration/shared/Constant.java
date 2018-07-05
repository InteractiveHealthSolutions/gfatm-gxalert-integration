/* Copyright(C) 2018 Interactive Health Solutions, Pvt. Ltd.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 3 of the License (GPLv3), or any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Interactive Health Solutions, info@ihsinformatics.com
You can also access the license on the internet at the address: http://www.gnu.org/licenses/gpl-3.0.html

Interactive Health Solutions, hereby disclaims all copyright interest in this program written by the contributors.
 */

package com.ihsinformatics.gfatm.integration.shared;

/**
 * @author owais.hussain@ihsinformatics.com
 *
 */
public final class Constant {

	private Constant() {
		// Hide the implicit public interface
	}

	public static final String PATIENT_ID_REGEX = "^[A-Za-z0-9]{5}\\-[0-9]$";
	public static final int GXP_ENCOUNTER_TYPE = 172;
	public static final int GXP_RESULT_CONCEPT = 162202;
	public static final int INVALID_CONCEPT = 163611;
	public static final int NO_RESULT_CONCEPT = 164312;
	public static final int MTB_DETECTED_CONCEPT = 1301;
	public static final int MTB_NOT_DETECTED_CONCEPT = 1302;
	public static final int ERROR_CONCEPT = 165361;
	public static final int RIF_RESULT_CONCEPT = 164347;
	public static final int RIF_DETECTED_CONCEPT = 1301;
	public static final int RIF_NOT_DETECTED_CONCEPT = 1302;
	public static final int RIF_INDETERMINATE_CONCEPT = 1138;
	public static final int MTB_BURDEN_CONCEPT = 164345;
	public static final int HIGH_CONCEPT = 1408;
	public static final int MEDIUM_CONCEPT = 164342;
	public static final int LOW_CONCEPT = 1407;
	public static final int VERY_LOW_CONCEPT = 164343;
	public static final int TRACE_CONCEPT = 1874;
	public static final int CARTRIDGE_CONCEPT = 164306;
	public static final int ERROR_CODE_CONCEPT = 164348;
	public static final int ERROR_NOTES_CONCEPT = 166068;
	public static final int NOTES_CONCEPT = 166066;
	public static final int SAMPLE_CONCEPT = 159968;
	public static final int HOST_CONCEPT = 166069;
	public static final int REAGENT_LOT_CONCEPT = 166070;
	public static final int MODULE_SERIAL_CONCEPT = 166071;
}
