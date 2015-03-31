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

package org.flockdata.test.importer;

import org.flockdata.transform.FileProcessor;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * Validate File Processor routines - Please add to this as you can
 * Created by mike on 24/03/15.
 */
public class TestFileProcessor {
    @Test
    public void validate_RowsToProcess () throws Exception {
        FileProcessor fp = new FileProcessor();
        assertFalse("No check should be made",fp.stopProcessing(1));

        fp = new FileProcessor(50,50);
        assertFalse("Less than skip count failed", fp.stopProcessing(49));
        assertFalse("Processing should begin", fp.stopProcessing(50));
        assertFalse("Processing should continue", fp.stopProcessing(51));
        assertTrue("Processing should stop", fp.stopProcessing(100));
        assertTrue("Processing should really have stopped", fp.stopProcessing(101));

        fp = new FileProcessor(50000,10);
        assertFalse("Processing should continue with no log message", fp.stopProcessing(1));

        fp = new FileProcessor(50000,1);
        assertFalse("Processing should not continue", fp.stopProcessing(50000));

    }
}