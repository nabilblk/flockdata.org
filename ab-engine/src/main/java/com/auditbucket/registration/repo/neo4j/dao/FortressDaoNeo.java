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

package com.auditbucket.registration.repo.neo4j.dao;

import com.auditbucket.registration.dao.FortressDao;
import com.auditbucket.registration.model.Fortress;
import com.auditbucket.registration.model.FortressUser;
import com.auditbucket.registration.repo.neo4j.FortressRepository;
import com.auditbucket.registration.repo.neo4j.FortressUserRepository;
import com.auditbucket.registration.repo.neo4j.model.FortressNode;
import com.auditbucket.registration.repo.neo4j.model.FortressUserNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User: Mike Holdsworth
 * Date: 20/04/13
 * Time: 10:29 PM
 */
@Repository
public class FortressDaoNeo implements FortressDao {
    @Autowired
    private FortressRepository fortressRepo;
    @Autowired
    private FortressUserRepository fortressUserRepo;

    @Override
    public Fortress save(Fortress fortress) {
        return fortressRepo.save((FortressNode) fortress);
    }

    @Override
    public Fortress findByPropertyValue(String name, Object value) {
        return fortressRepo.findByPropertyValue(name, value);
    }

    @Override
    public Fortress findOne(Long id) {
        return fortressRepo.findOne(id);
    }

    @Autowired
    Neo4jTemplate template;

    @Override
    public FortressUser getFortressUser(Long id, String name) {
        FortressUser fu = fortressRepo.getFortressUser(id, name);
        if (fu != null)
            template.fetch(fu.getFortress());
        return fu;
    }

    @Override
    public List<Fortress> findFortresses(Long companyID) {

//        TraversalDescription td = Traversal.description()
//                .breadthFirst()
//                .relationships( DynamicRelationshipType.withName("owns"), Direction.OUTGOING )
//                .evaluator( Evaluators.excludeStartPosition() );

        //return fortressRepo.findAllByTraversal(companyID, td );

        return fortressRepo.findCompanyFortresses(companyID);
    }

    @Override
    public FortressUser findOneUser(Long id) {
        return fortressUserRepo.findOne(id);
    }

    @Override
    public FortressUser save(FortressUser fortressUser) {
        return fortressUserRepo.save((FortressUserNode) fortressUser);
    }


}