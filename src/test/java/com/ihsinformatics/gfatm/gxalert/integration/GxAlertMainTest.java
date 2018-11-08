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
package com.ihsinformatics.gfatm.gxalert.integration;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import com.ihsinformatics.gfatm.integration.gxalert.GxAlertImportService;
import com.ihsinformatics.gfatm.integration.gxalert.GxAlertMain;
import com.ihsinformatics.util.ClassLoaderUtil;
import com.ihsinformatics.util.DatabaseUtil;
import com.ihsinformatics.util.FileUtil;

/**
 * @author owais.hussain@ihsinformatics.com
 *
 */
public class GxAlertMainTest {

	DatabaseUtil dbUtil;

	GxAlertImportService gxAlert;

	private static final String TEST_DATA_FILE = "test_data.json";
	private JSONArray data;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setup() throws Exception {
		data = new JSONArray();
		String filePath = ClassLoaderUtil.getResource(TEST_DATA_FILE, GxAlertMain.class).getPath();
		FileUtil fu = new FileUtil();
		data = new JSONArray(fu.getText(filePath));
	}

	/**
	 * Test method for
	 * {@link com.ihsinformatics.gfatm.integration.gxalert.GxAlertMain#run(org.joda.time.DateTime, org.joda.time.DateTime)}.
	 */
	@Test
	public final void testRun() {
		DateTime start = new DateTime().minusDays(7);
		DateTime end = new DateTime();
		try {
			gxAlert.run(start, end);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
