package com.orientechnologies.orient.core.type.tree;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.orientechnologies.common.collection.OMVRBTreeNonCompositeTest;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.serialization.serializer.binary.impl.index.OSimpleKeySerializer;
import com.orientechnologies.orient.core.serialization.serializer.stream.OStreamSerializerLiteral;
import com.orientechnologies.orient.core.storage.OStorage;

@Test
public class OMVRBTreeDatabaseLazySaveNonCompositeTest extends OMVRBTreeNonCompositeTest {
  private ODatabaseDocumentTx database;
  private int                 oldPageSize;
  private int                 oldEntryPoints;

  @BeforeClass
  public void beforeClass() {
    database = new ODatabaseDocumentTx("memory:mvrbtreeindextest").create();
    database.addCluster("indextestclsuter", OStorage.CLUSTER_TYPE.MEMORY);
  }

  @BeforeMethod
  @Override
  public void beforeMethod() throws Exception {
    oldPageSize = OGlobalConfiguration.MVRBTREE_NODE_PAGE_SIZE.getValueAsInteger();
    OGlobalConfiguration.MVRBTREE_NODE_PAGE_SIZE.setValue(4);

    oldEntryPoints = OGlobalConfiguration.MVRBTREE_ENTRYPOINTS.getValueAsInteger();
    OGlobalConfiguration.MVRBTREE_ENTRYPOINTS.setValue(1);

    tree = new OMVRBTreeDatabaseLazySave<Double, Double>("indextestclsuter", new OSimpleKeySerializer<Double>(OType.DOUBLE),
        OStreamSerializerLiteral.INSTANCE, 1, 5000);

    for (double i = 1; i < 10; i++) {
      tree.put(i, i);
    }

    ((OMVRBTreeDatabaseLazySave<Double, Double>) tree).save();
    ((OMVRBTreeDatabaseLazySave<Double, Double>) tree).optimize(true);
  }

  @AfterClass
  public void afterClass() {
    database.drop();

    OGlobalConfiguration.MVRBTREE_NODE_PAGE_SIZE.setValue(oldPageSize);
    OGlobalConfiguration.MVRBTREE_ENTRYPOINTS.setValue(oldEntryPoints);
  }

}
