/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.olingo.demo.app;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.olingo.odata2.processor.api.jpa.ODataJPAContext;
import org.apache.olingo.odata2.processor.api.jpa.ODataJPAServiceFactory;

public class ScenarioServiceFactory extends ODataJPAServiceFactory {

    private static final String PUNIT_NAME = "sampleDb";

    @Override   
    public ODataJPAContext initializeODataJPAContext() {

        ODataJPAContext oDataJPAContext = null;
        try {
            oDataJPAContext = this.getODataJPAContext();
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(PUNIT_NAME);
            oDataJPAContext.setEntityManagerFactory(emf);
            oDataJPAContext.setPersistenceUnitName(PUNIT_NAME);
            //} catch (ODataJPARuntimeException ex) {
        } catch (Exception ex) {
            Logger.getLogger(ScenarioServiceFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        return oDataJPAContext;

    }
}
