package com.orientechnologies.orient.core.intent;

import com.orientechnologies.orient.core.db.ODatabaseComplex;
import com.orientechnologies.orient.core.db.object.ODatabaseObject;
import com.orientechnologies.orient.core.db.raw.ODatabaseRaw;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;

public class OIntentMassiveInsert implements OIntent {
	private boolean	previousLevel1CacheEnabled;
	private boolean	previousLevel2CacheEnabled;
	private boolean	previousRetainRecords;
	private boolean	previousRetainObjects;

	public void begin(final ODatabaseRaw iDatabase, final Object... iArgs) {
		previousLevel1CacheEnabled = iDatabase.getDatabaseOwner().getLevel1Cache().isEnabled();
		iDatabase.getDatabaseOwner().getLevel1Cache().setEnable(false);
		previousLevel2CacheEnabled = iDatabase.getDatabaseOwner().getLevel2Cache().isEnabled();
		iDatabase.getDatabaseOwner().getLevel2Cache().setEnable(false);

		ODatabaseComplex<?> ownerDb = iDatabase.getDatabaseOwner();

		if (ownerDb instanceof ODatabaseRecord) {
			previousRetainRecords = ((ODatabaseRecord) ownerDb).isRetainRecords();
			((ODatabaseRecord) ownerDb).setRetainRecords(false);
		}

		while (ownerDb.getDatabaseOwner() != ownerDb)
			ownerDb = ownerDb.getDatabaseOwner();

		if (ownerDb instanceof ODatabaseObject) {
			previousRetainObjects = ((ODatabaseObject) ownerDb).isRetainObjects();
			((ODatabaseObject) ownerDb).setRetainObjects(false);
		}
	}

	public void end(final ODatabaseRaw iDatabase) {
		iDatabase.getDatabaseOwner().getLevel1Cache().setEnable(previousLevel1CacheEnabled);
		iDatabase.getDatabaseOwner().getLevel2Cache().setEnable(previousLevel2CacheEnabled);

		ODatabaseComplex<?> ownerDb = iDatabase.getDatabaseOwner();

		if (ownerDb instanceof ODatabaseRecord)
			((ODatabaseRecord) ownerDb).setRetainRecords(previousRetainRecords);

		while (ownerDb.getDatabaseOwner() != ownerDb)
			ownerDb = ownerDb.getDatabaseOwner();

		if (ownerDb instanceof ODatabaseObject)
			((ODatabaseObject) ownerDb).setRetainObjects(previousRetainObjects);
	}
}
