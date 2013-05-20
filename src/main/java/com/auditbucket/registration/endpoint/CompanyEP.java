package com.auditbucket.registration.endpoint;

import com.auditbucket.helper.SecurityHelper;
import com.auditbucket.registration.model.ICompany;
import com.auditbucket.registration.model.IFortress;
import com.auditbucket.registration.model.ISystemUser;
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
 * User: mike
 * Date: 1/05/13
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
    public List<IFortress> register(@PathVariable("companyName") String companyName) throws Exception {
        // curl -u mike:123 -X GET  http://localhost:8080/ab/company/Monowai/fortresses
        List<IFortress> results = null;
        ICompany company = companyService.findByName(companyName);
        if (company == null)
            return results;

        return fortressService.findFortresses(companyName);
    }

    @RequestMapping(value = "/{companyName}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ICompany> getCompany(@PathVariable("companyName") String companyName) throws Exception {
        // curl -u mike:123 -X GET http://localhost:8080/ab/company/Monowai
        ICompany company = companyService.findByName(companyName);
        if (company == null)
            return new ResponseEntity<ICompany>(company, HttpStatus.NOT_FOUND);

        ISystemUser sysUser = securityHelper.getSysUser(true);
        if (!sysUser.getCompany().getId().equals(company.getId())) {
            // Not Authorised
            return new ResponseEntity<ICompany>(company, HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<ICompany>(company, HttpStatus.OK);
        }
    }

}