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
package com.orientechnologies.orient.core.tx;

import com.orientechnologies.orient.core.db.record.ORecordOperation;


/**
 * Listener interface to catch events during transactions phases.
 * 
 * @author Luca Garulli
 * 
 */
public interface OTxListener {
	public enum EVENT {
		BEFORE_COMMIT, AFTER_COMMIT, BEFORE_ROLLBACK, AFTER_ROLLBACK
	}

	public void onEvent(ORecordOperation iTxEntry, EVENT iEvent);
}
