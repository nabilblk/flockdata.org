/*
 * Copyright (c) 2012-2013 "Monowai Developments Limited"
 *
 * This file is part of AuditBucket.
 *
 * AuditBucket is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuditBucket is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuditBucket.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.auditbucket.engine.repo.neo4j;

import com.auditbucket.audit.model.AuditEvent;
import com.auditbucket.engine.repo.neo4j.model.AuditEventNode;
import com.auditbucket.engine.repo.neo4j.model.AuditTagRelationship;
import com.auditbucket.engine.repo.neo4j.model.DocumentTypeNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import java.util.Set;

/**
 * User: Mike Holdsworth
 * Date: 28/06/13
 * Time: 10:56 PM
 */
public interface AuditEventRepo extends GraphRepository<AuditTagRelationship> {

    @Query(elementClass = AuditEventNode.class, value = "start company=node({0})" +
            "   match company-[:COMPANY_EVENT]->event " +
            "   where event.code = {1}" +
            "  return event")
    AuditEventNode findCompanyEvent(Long companyId, String eventName);

    @Query(elementClass = AuditEventNode.class, value = "start company=node({0})" +
            "   match company-[:COMPANY_EVENT]->events " +
            "  return events")
    Set<AuditEvent> findCompanyEvents(Long id);
}