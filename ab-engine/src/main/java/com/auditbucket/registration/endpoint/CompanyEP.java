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

package com.auditbucket.registration.endpoint;

import com.auditbucket.helper.SecurityHelper;
import com.auditbucket.registration.model.Company;
import com.auditbucket.registration.model.Fortress;
import com.auditbucket.registration.model.SystemUser;
import com.auditbucket.registration.service.CompanyService;
import com.auditbucket.registration.service.FortressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * User: Mike Holdsworth
 * Date: 4/05/13
 * Time: 8:23 PM
 */
@Controller
@RequestMapping("/company/")
public class CompanyEP {

    @Autowired
    FortressService fortressService;

    @Autowired
    CompanyService companyService;

    @Autowired
    SecurityHelper securityHelper;


    @RequestMapping(value = "/{companyName}/fortresses", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @ResponseBody
    public List<Fortress> register(@PathVariable("companyName") String companyName) throws Exception {
        // curl -u mike:123 -X GET  http://localhost:8080/ab/company/Monowai/fortresses
        List<Fortress> results = null;
        Company company = companyService.findByName(companyName);
        if (company == null)
            return results;

        return fortressService.findFortresses(companyName);
    }

    @RequestMapping(value = "/{companyName}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Company> getCompany(@PathVariable("companyName") String companyName) throws Exception {
        // curl -u mike:123 -X GET http://localhost:8080/ab/company/Monowai
        Company company = companyService.findByName(companyName);
        if (company == null)
            return new ResponseEntity<Company>(company, HttpStatus.NOT_FOUND);

        SystemUser sysUser = securityHelper.getSysUser(true);
        if (!sysUser.getCompany().getId().equals(company.getId())) {
            // Not Authorised
            return new ResponseEntity<Company>(company, HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<Company>(company, HttpStatus.OK);
        }
    }

}