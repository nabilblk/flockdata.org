/*
 * Copyright (c) 2012-2014 "Monowai Developments Limited"
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

import com.auditbucket.engine.repo.neo4j.model.EntityNode;
import com.auditbucket.engine.repo.neo4j.model.TxRefNode;
import com.auditbucket.track.model.Entity;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import java.util.Collection;
import java.util.Set;

/**
 * User: Mike Holdsworth
 * Date: 14/04/13
 * Time: 8:00 PM
 */
public interface EntityRepo extends GraphRepository<EntityNode> {

    @Query(value = "start company=node({1}) " +
            "   MATCH company-[:TX]->txTag " +
            "   where txTag.name = {0} " +
            "return txTag")
    TxRefNode findTxTag(String userTag, Long company);

    @Query(elementClass = EntityNode.class, value = "start tx=node({0}) " +
            "   MATCH tx-[:AFFECTED]->change<-[:LOGGED]-auditHeader " +
            "return auditHeader")
    Set<Entity> findEntitiesByTxRef(Long txRef);

    @Query(elementClass = EntityNode.class, value =
                    " match (fortress:Fortress)-[:TRACKS]->(track:Entity) " +
                    " where id(fortress)={0} " +
                    " return track ORDER BY track.dateCreated ASC" +
                    " skip {1} limit 100 ")
    Set<Entity> findEntities(Long fortressId, Long skip);

    @Query(elementClass = EntityNode.class, value =
                    " match (fortress:Fortress)-[:TRACKS]->(entity:Entity) where id(fortress)={0} " +
                    " and entity.callerRef ={1}" +
                    " return entity ")
    Collection<Entity> findByCallerRef(Long fortressId, String callerRef);

    @Query (elementClass = EntityNode.class, value = "match (company:ABCompany), (entities:Entity) " +
            " where id(company)={0} " +
            "   and entities.metaKey in {1}  " +
            "  with company, entities match (company)-[*..2]-(entities) " +
            "return entities ")
    Collection<Entity> findEntities(Long id, Collection<String> toFind);

    @Query (value = "match (f:_Fortress)-[track:TRACKS]->(meta:_Entity)-[other]-(:FortressUser) where id(f)={0} delete other")
    public void purgePeopleRelationships(Long fortressId);

    @Query (value = "match (f:_Fortress)-[track:TRACKS]->(meta:_Entity)-[otherRlx]-(:_Entity) where id(f)={0} delete otherRlx")
    public void purgeCrossReferences(Long fortressId);

    @Query (value="match (f:_Fortress)-[track:TRACKS]->(meta:_Entity) where id(f)={0} delete track, meta ")
    public void purgeEntities(Long fortressId);


}