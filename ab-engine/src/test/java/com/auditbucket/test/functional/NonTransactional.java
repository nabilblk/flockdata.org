package com.auditbucket.test.functional;

import com.auditbucket.fortress.endpoint.FortressEP;
import com.auditbucket.registration.bean.FortressInputBean;
import com.auditbucket.registration.bean.RegistrationBean;
import com.auditbucket.registration.model.CompanyUser;
import com.auditbucket.registration.model.Fortress;
import com.auditbucket.registration.model.FortressUser;
import com.auditbucket.registration.model.SystemUser;
import com.auditbucket.registration.service.FortressService;
import com.auditbucket.registration.service.RegistrationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * User: mike
 * Date: 2/05/14
 * Time: 8:26 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:root-context.xml")
public class NonTransactional {
    @Autowired
    FortressService fortressService;

    @Autowired
    RegistrationService registrationService;

    @Autowired
    FortressEP fortressEP;
    @Autowired
    private Neo4jTemplate template;

    private Logger logger = LoggerFactory.getLogger(NonTransactional.class);

    private Authentication authA = new UsernamePasswordAuthenticationToken("mike", "123");

    @Test
    public void multipleFortressUserRequestsThreaded() throws Exception {
        Neo4jHelper.cleanDb(template);
        Transaction t = template.getGraphDatabase().beginTx();
        logger.info("Starting multipleFortressUserRequestsThreaded");
        String uname = "mike";
        // Assume the user has now logged in.
        //org.neo4j.graphdb.Transaction t = graphDatabaseService.beginTx();
        String company = "MFURT";
        SystemUser su = registrationService.registerSystemUser(new RegistrationBean(company, uname).setIsUnique(false));
        SecurityContextHolder.getContext().setAuthentication(authA);
        CompanyUser nonAdmin = registrationService.addCompanyUser(uname, company);
        assertNotNull(nonAdmin);

        Fortress fortress = fortressEP.registerFortress(new FortressInputBean("multipleFortressUserRequestsThreaded", true), su.getApiKey(), null).getBody();
        // This is being done to create the schema index which otherwise errors when the threads kick off
        fortressService.getFortressUser(fortress, "don'tcare");
        fortress = fortressEP.registerFortress(new FortressInputBean("testThis", true), su.getApiKey(), null).getBody();
        assertNotNull(fortress);

        t.success();
        t.close();
        Thread.sleep(200);

        int count = 5;

        CountDownLatch latch = new CountDownLatch(count);
        // Run threaded tests
        ArrayList<FuAction> actions = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        int i = 0;
        while (i <= count) {
            FuAction action = new FuAction(fortress, Integer.toString(i), "mike", latch);
            actions.add(action);
            threads.add(new Thread(action));
            threads.get(i).start();
            i++;
        }

        boolean timedOut = !latch.await(60, TimeUnit.SECONDS);
        assertFalse(timedOut);

        assertNotNull(fortressService.findByName(fortress.getCompany(), fortress.getName()));
        i = 0;
        while (i < count) {
            assertFalse("Fu" + i + "Fail", actions.get(i).isFailed());
            i++;
        }
        // Check we only get one back
        // Not 100% sure this works
        FortressUser fu = fortressService.getFortressUser(fortress, uname);
        assertNotNull(fu);

    }

    class FuAction implements Runnable {
        Fortress fortress;
        String uname;
        CountDownLatch latch;
        boolean failed;
        boolean done = false;

        public FuAction(Fortress fortress, String id, String uname, CountDownLatch latch) {
            logger.info("Preparing FuAction {}, {}", id, latch.getCount());
            this.fortress = fortress;
            this.uname = uname;
            this.latch = latch;
        }

        public boolean isFailed() {
            return failed;
        }

        public void run() {
            logger.info("Running " + this);
            int runCount = 50;
            int i = 0;
            failed = false;
            int deadLockCount = 0;
            while (i < runCount) {
                boolean deadlocked = false;
                try {
                    FortressUser fu = fortressService.getFortressUser(this.fortress, uname);
                    assertNotNull(fu);
                } catch (Exception e) {
                    deadLockCount++;
                    Thread.yield();
                    if (deadLockCount == 100) {
                        failed = true;
                        logger.error("Exception count exceeded");
                        return;
                    }
                    deadlocked = true;
                }

                if (!deadlocked)
                    i++;
            } // End while
            logger.info("Finishing {}", failed);
            failed = false;
            latch.countDown();
        }
    }
}