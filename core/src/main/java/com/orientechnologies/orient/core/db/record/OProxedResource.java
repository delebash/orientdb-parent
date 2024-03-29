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
package com.orientechnologies.orient.core.db.record;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;

/**
 * Generic proxy abstratc class.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public abstract class OProxedResource<T> {
	protected T								delegate;
	protected ODatabaseRecord	database;

	public OProxedResource(final T iDelegate, final ODatabaseRecord iDatabase) {
		this.delegate = iDelegate;
		this.database = iDatabase;
	}

	protected void setCurrentDatabaseInThreadLocal() {
		ODatabaseRecordThreadLocal.INSTANCE.set(database);
	}
}
