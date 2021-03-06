/*
 * Copyright (c) 2012-2015 "FlockData LLC"
 *
 * This file is part of FlockData.
 *
 * FlockData is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FlockData is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FlockData.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.flockdata.test.engine.functional;

import org.flockdata.kv.service.KvService;
import org.flockdata.registration.bean.FortressInputBean;
import org.flockdata.test.engine.Helper;
import org.flockdata.track.bean.ContentInputBean;
import org.flockdata.track.bean.EntityInputBean;
import org.flockdata.track.bean.EntitySummaryBean;
import org.flockdata.track.bean.TrackResultBean;
import org.flockdata.model.*;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Fortress
 * Created by mike on 22/03/15.
 */
public class TestVersioning extends EngineBase {
    @Test
    public void defaults_Fortress() throws Exception {
        // DAT-346
        SystemUser su = registerSystemUser();
        assertEquals(Boolean.TRUE, engineConfig.isStoreEnabled());
        // System default behaviour controlled by configuration.properties
        FortressInputBean fib = new FortressInputBean("vtest");
        Fortress fortress = fortressService.registerFortress(su.getCompany(), fib);
        assertTrue(fortress.isStoreEnabled());

        engineConfig.setStoreEnabled("false");
        assertEquals(Boolean.FALSE, engineConfig.isStoreEnabled());
        fib = new FortressInputBean("disabledTest");
        assertEquals(null, fib.getStoreActive());
        fortress = fortressService.registerFortress(su.getCompany(), fib);
        assertFalse("System default should have been returned", fortress.isStoreEnabled());

        engineConfig.setStoreEnabled("false");
        fib = new FortressInputBean("manualEnableTest");
        fortress = fortressService.registerFortress(su.getCompany(), fib);
        fortress.setStoreEnabled(true);
        assertTrue("Callers setting did not override System default", fortress.isStoreEnabled());

        engineConfig.setStoreEnabled("true");
        fib = new FortressInputBean("manualDisableTest");
        fortress = fortressService.registerFortress(su.getCompany(), fib);
        fortress.setStoreEnabled(false);
        assertFalse("Callers setting did not override System default", fortress.isStoreEnabled());

    }

    @After
    public void resetDefaults() {
        engineConfig.setStoreEnabled("true");
    }

    @Test
    public void kv_Ignored() throws Exception {
        SystemUser su = registerSystemUser("kv_Ignored", "kv_Ignored");
        engineConfig.setStoreEnabled("false");
        Fortress fortress = fortressService.registerFortress(su.getCompany(), new FortressInputBean("kv_Ignored", true));
        assertFalse(engineConfig.isStoreEnabled());
        EntityInputBean eib = new EntityInputBean(fortress, "kv_Ignored", "kv_Ignored", new DateTime());
        ContentInputBean cib = new ContentInputBean(Helper.getRandomMap());
        eib.setContent(cib);
        TrackResultBean trackResult = mediationFacade.trackEntity(su.getCompany(), eib);
        assertEquals(Boolean.FALSE, trackResult.getEntity().getFortress().isStoreEnabled());
        EntityLog entityLog= entityService.getLastEntityLog(trackResult.getEntity().getId());
        assertNotNull ( entityLog);

        assertNotNull ( entityLog.getLog());
        assertEquals(new Long(0), entityLog.getLog().getId());
        assertNotNull ( entityLog.getId());

        Entity entity = entityService.getEntity(su.getCompany(), trackResult.getEntity().getMetaKey());
        assertNotNull ( entity);

        Set<EntityLog> logs = entityService.getEntityLogs(su.getCompany(), trackResult.getEntity().getMetaKey());
        assertFalse(logs.isEmpty());
        assertEquals(1, logs.size());
        // Check various properties that we still want to return
        for (EntityLog log : logs) {
            assertEquals (entity.getFortressCreatedTz().getMillis(),log.getFortressWhen().longValue() );
            assertNotNull ( log.getLog().getEvent());
            assertEquals("Create", log.getLog().getEvent().getName());
            assertNotNull(log.getFortressWhen());
            assertNotNull(log.getLog().getMadeBy());
            assertTrue(log.isMocked());
            assertTrue(log.getLog().isMocked());
        }
        EntityLog mockLog = entityService.getLogForEntity(entity, 0l);
        assertNotNull (mockLog);
        assertNotNull(mockLog.getLog());
        assertTrue( mockLog.isMocked());
        Assert.assertEquals(KvService.KV_STORE.NONE.name(), mockLog.getLog().getStorage());

        EntitySummaryBean summaryBean = entityService.getEntitySummary(su.getCompany(), entity.getMetaKey());
        assertNotNull ( summaryBean);
        assertNotNull ( summaryBean.getChanges());
        assertEquals ( 1, summaryBean.getChanges().size());

        // See TestFdIntegration for a fully integrated version of this test
    }
    @Test
    public void log_ValidateValues() throws Exception{
        Map<String, Object> json = Helper.getSimpleMap("Athlete", "Katerina Neumannová");
        SystemUser su = registerSystemUser("store_Disabled");

        FortressInputBean fib= new FortressInputBean("store_Disabled", true);
        fib.setStoreActive(false);
        Fortress fortress = fortressService.registerFortress(su.getCompany(), fib);

        ContentInputBean log = new ContentInputBean("store_Disabled", new DateTime(), json);
        EntityInputBean input = new EntityInputBean(fortress, "mikeTest", "store_Disabled", new DateTime(), "store_Disabled");
        input.setContent(log);

        TrackResultBean result = mediationFacade.trackEntity(su.getCompany(), input);
        EntityLog entityLog = entityService.getLastEntityLog(result.getEntity().getId());
        assertNotNull(entityLog);
        Assert.assertEquals(KvService.KV_STORE.NONE.name(), entityLog.getLog().getStorage());

        engineConfig.setStoreEnabled("false");
        assertEquals("MemMap is OK", kvService.ping());
        engineConfig.setStoreEnabled("true");

    }

    @Test
    public void storage_CorrectMechanismSelected() throws Exception {
        // DAT-353
        engineConfig.setStoreEnabled("true");
        // The system default store is MEMORY
        ContentInputBean content  = new ContentInputBean(Helper.getRandomMap());
        // Fortress is not enabled but the overall configuration says the store is enabled
        Entity entity = Helper.getEntity("blah", "abc", "abc", "123");

        // set a default for the fortress
        entity.getFortress().setStoreEnabled(false);
        TrackResultBean trackResult = new TrackResultBean(entity, new DocumentType("abc"));
        trackResult.setContentInput(content);

        Log log = new Log(entity);

        log = kvService.prepareLog(trackResult, log);
        assertEquals("Store should be set to that of the fortress", KvService.KV_STORE.NONE.name(), log.getContent().getStorage() );

        entity.getFortress().setStoreEnabled(true);
        log = kvService.prepareLog(trackResult, log);
        // Falls back to the system default
        assertEquals("Store should be set to the system default", KvService.KV_STORE.MEMORY.name(), log.getContent().getStorage() );


    }

}
