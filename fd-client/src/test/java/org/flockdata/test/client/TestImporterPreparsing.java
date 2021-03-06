/*
 * Copyright (c) 2012-2014 "FlockData LLC"
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

package org.flockdata.test.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.flockdata.helper.FlockException;
import org.flockdata.profile.ContentProfileImpl;
import org.flockdata.registration.bean.TagInputBean;
import org.flockdata.track.bean.EntityInputBean;
import org.flockdata.transform.ClientConfiguration;
import org.flockdata.transform.FileProcessor;
import org.flockdata.transform.ProfileReader;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by mike on 28/01/15.
 */
public class TestImporterPreparsing extends AbstractImport {
    @Test
    public void string_PreParseRow() throws Exception {
        FileProcessor fileProcessor = new FileProcessor();
        ClientConfiguration configuration = getClientConfiguration("/pre-parse.json");
        configuration.setDefaultUser("test");

        ContentProfileImpl params = ProfileReader.getImportProfile("/pre-parse.json");
        assertEquals(',', params.getDelimiter());
        assertEquals(false, params.hasHeader());
        long rows = fileProcessor.processFile(params, "/properties-rlx.txt", getFdWriter(), null, configuration);
        assertEquals(4, rows);

        List<EntityInputBean> entityBatch = getFdWriter().getEntities();

        for (EntityInputBean entityInputBean : entityBatch) {
            assertFalse("Expression not parsed for callerRef",entityInputBean.getCode().contains("|"));
            assertTrue("Tag not set", entityInputBean.getTags().size() == 3);
            TagInputBean politician= null;
            for (TagInputBean tagInputBean : entityInputBean.getTags()) {
                assertFalse("Expression not parsed for code", tagInputBean.getCode().contains("|"));
                if ( tagInputBean.getLabel().equals("Politician"))
                    politician= tagInputBean;
                if ( tagInputBean.getLabel().equals("InterestGroup")){
                    assertEquals("direct", tagInputBean.getEntityLinks().keySet().iterator().next());
                    TestCase.assertEquals(2, tagInputBean.getProperties().size());
                    TestCase.assertNotNull(tagInputBean.getProperties().get("amount"));
                    TestCase.assertEquals("ABC123", tagInputBean.getProperties().get("calculatedColumn"));
                }
            }
            assertNotNull(politician);
            HashMap link = (HashMap) politician.getEntityLinks().get("receives");
            assertNotNull(link);
            assertNotNull(link.get("amount"));
            assertTrue("Amount not calculated as a value", Integer.parseInt(link.get("amount").toString()) >0);

        }
        ObjectMapper om = new ObjectMapper();
        try {
            om.writeValueAsString(entityBatch);
        } catch (Exception e) {
            throw new FlockException("Failed to serialize");
        }

    }

}
