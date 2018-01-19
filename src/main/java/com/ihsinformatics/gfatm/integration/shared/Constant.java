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

	public static final String patientIdRegex = "^[A-Za-z0-9]{5}\\-[0-9]$";
	public static final int gxpEncounterType = 172; // 23;
	public static final int gxpResultConceptId = 162202;
	public static final int invalidConceptId = 163611;
	public static final int noResultConceptId = 164312;
	public static final int detectedConceptId = 1301;
	public static final int notDetectedConceptId = 1302;
	public static final int errorConceptId = 165361;
	public static final int rifResultConceptId = 164347;
	public static final int rifDetectedConceptId = 1301;
	public static final int rifNotDetectedConceptId = 1302;
	public static final int rifIndeterminateConceptId = 1138;
	public static final int mtbBurdenConceptId = 164345;
	public static final int highConceptId = 1408;
	public static final int mediumConceptId = 164342;
	public static final int lowConceptId = 1407;
	public static final int veryLowConceptId = 164343;
	public static final int cartridgeConceptId = 164306;
	public static final int errorCodeConceptId = 164348;
	public static final int errorNotesConceptId = 166068;
	public static final int notesConceptId = 166066;
	public static final int sampleConceptId = 159968;
	public static final int hostConceptId = 166069;
	public static final int reagentLotConceptId = 166070;
	public static final int moduleSerialConceptId = 166071;

}
