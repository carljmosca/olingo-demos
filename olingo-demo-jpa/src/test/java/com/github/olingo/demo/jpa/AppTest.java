package com.github.olingo.demo.jpa;

import java.io.InputStream;
import java.sql.Connection;
import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for simple App.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/application-context.xml"})
public class AppTest extends TestCase {

    private static final String FLAT_XML_DATASET = "FlatXmlDataSet.xml";
    @Autowired
    BasicDataSource bds;

    @Before
    @Override
    public void setUp() throws Exception {
        DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
    }

    @Test
    public void testApp() {
        assertTrue(true);
    }

    private IDataSet getDataSet() throws Exception {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(FLAT_XML_DATASET);
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        IDataSet dataset = builder.build(inputStream);
        return dataset;
    }

    private IDatabaseConnection getConnection() throws Exception {

        Connection jdbcConnection = bds.getConnection();
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);
        return connection;
    }
}
