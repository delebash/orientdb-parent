/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.config;

public class OStorageDataConfiguration extends OStorageSegmentConfiguration {
	private static final long							serialVersionUID	= 1L;

	public OStorageDataHoleConfiguration	holeFile;

	private static final String						START_SIZE				= "1Mb";
	private static final String						INCREMENT_SIZE		= "100%";

	public OStorageDataConfiguration(final OStorageConfiguration iRoot, final String iSegmentName, final int iId) {
		super(iRoot, iSegmentName, iId);
		fileStartSize = START_SIZE;
		fileIncrementSize = INCREMENT_SIZE;
	}

	public OStorageDataConfiguration(final OStorageConfiguration iRoot, final String iSegmentName, final int iId,
			final String iDirectory) {
		super(iRoot, iSegmentName, iId, iDirectory);
		fileStartSize = START_SIZE;
		fileIncrementSize = INCREMENT_SIZE;
	}

	@Override
	public void setRoot(final OStorageConfiguration root) {
		super.setRoot(root);
		holeFile.parent = this;
	}
}
