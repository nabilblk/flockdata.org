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

package org.flockdata.engine.track.service;

import org.flockdata.engine.PlatformConfig;
import org.flockdata.helper.FlockException;
import org.flockdata.registration.model.Fortress;
import org.flockdata.registration.model.Tag;
import org.flockdata.track.bean.EntityInputBean;
import org.flockdata.track.bean.TrackResultBean;
import org.flockdata.track.service.EntityService;
import org.flockdata.track.service.LogService;
import org.flockdata.track.service.SchemaService;
import org.neo4j.graphdb.ConstraintViolationException;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.kernel.DeadlockDetectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * User: mike
 * Date: 20/09/14
 * Time: 3:38 PM
 */
@Service
public class EntityRetryService {

    @Autowired
    EntityService entityService;

    @Autowired
    LogService logService;

    @Autowired
    PlatformConfig engineConfig;

    @Autowired
    SchemaService schemaService;

    @Retryable(include = {NotFoundException.class, InvalidDataAccessResourceUsageException.class, DataIntegrityViolationException.class, ConcurrencyFailureException.class, DeadlockDetectedException.class, ConstraintViolationException.class},
            maxAttempts = 20,
            backoff = @Backoff(delay = 100, maxDelay = 500))
    public Iterable<TrackResultBean> track(Fortress fortress, List<EntityInputBean> entities, Collection<Tag> tags)
            throws InterruptedException, ExecutionException, FlockException, IOException {
        return doTrack(fortress, entities, tags);
    }

    @Transactional(timeout = 4000)
    Iterable<TrackResultBean> doTrack(Fortress fortress, Collection<EntityInputBean> entityInputs, Collection<Tag> tags) throws InterruptedException, FlockException, ExecutionException, IOException {

        Collection<TrackResultBean>
                resultBeans = entityService.trackEntities(fortress, entityInputs, tags);
        // ToDo: DAT-343 - write via a queue
        boolean processAsync;


        if (engineConfig.isTestMode())   // We always run sync in test mode
            processAsync = false;
        else if (resultBeans.size() == 1) { // When processing one result, defer to the isNew flag
            // Existing entities are processed sync, new ones async
            processAsync = resultBeans.iterator().next().getEntity().isNew();
        } else { // Could have a mix of new and existing entities, so we need to
            // Split the batch between new and existing entities
            Collection<TrackResultBean> newEntities = TrackBatchSplitter.splitEntityResults(resultBeans);

            if (!newEntities.isEmpty()) { // New can be processed async
                logService.processLogs(fortress, newEntities);
                if (resultBeans.isEmpty())
                    return newEntities;

            }
            // Process updates synchronously
            logService.processLogs(fortress, resultBeans).get();
            resultBeans.addAll(newEntities);
            return resultBeans;
        }

        if (processAsync) {
            // DAT-342 - we already know what the content log will be so we can end
            //           this transaction and get on with writing the search results
            // Occurs async
            logService.processLogs(fortress, resultBeans);
            return resultBeans;

        } else {
            return logService.processLogs(fortress, resultBeans).get();
        }

    }


}
