package com.auditbucket.engine.service;

import com.auditbucket.audit.model.AuditChange;
import com.auditbucket.audit.model.AuditHeader;
import com.auditbucket.audit.model.AuditWhat;
import com.auditbucket.dao.AuditDao;
import com.auditbucket.engine.repo.KvRepo;
import com.auditbucket.engine.repo.redis.RedisRepo;
import com.auditbucket.engine.repo.riak.RiakRepo;
import com.auditbucket.helper.CompressionHelper;
import com.auditbucket.helper.CompressionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * User: Mike Holdsworth
 * Since: 4/09/13
 */
@Service
@Transactional
public class WhatService {

    public enum KV_STORE {REDIS, RIAK}
    private static final ObjectMapper om = new ObjectMapper();
    @Autowired(required = false)
    AuditDao auditDao = null;
    @Autowired
    RedisRepo redisRepo;
    @Autowired
    RiakRepo riakRepo;
    @Autowired
    EngineConfig engineAdmin;

    private Logger logger = LoggerFactory.getLogger(WhatService.class);

    public String logWhat(AuditHeader auditHeader, AuditChange change, String jsonText, int version) {
        // Compress the Value of JSONText
        CompressionResult result = CompressionHelper.compress(jsonText);
        Boolean compressed = result.getMethod() == CompressionResult.Method.GZIP;

        change.setWhatStore(String.valueOf(engineAdmin.getKvStore()));
        // Store First all information In Neo4j
        change = auditDao.save(change, compressed, version);

        // Store the what information Compressed in KV Store Depending on
        KvRepo kvRepo = getKvRepo(change);

        try {
            // ToDo: deal with this via spring integration??
            kvRepo.add(auditHeader, change.getId(), result.getAsBytes());
        } catch (IOException e) {
            logger.error("KV storage issue", e);
        }
        return change.getWhat().getId();
    }

    private KvRepo getKvRepo(AuditChange change) {
        String kvStore = change.getWhatStore();
        if (kvStore.equalsIgnoreCase(String.valueOf(KV_STORE.REDIS))) {
            return redisRepo;
        } else if (kvStore.equalsIgnoreCase(String.valueOf(KV_STORE.RIAK))) {
            return riakRepo ;
        } else {
            throw new IllegalStateException("The only supported KV Stores supported are redis & riak");
        }
    }

    public AuditWhat getWhat(AuditHeader auditHeader, AuditChange change) {
        if (change == null || change.getWhat() == null)
            return null;
        KvRepo kvRepo = getKvRepo(change);
        byte[] whatInformation = kvRepo.getValue(auditHeader, change.getId());
        AuditWhat auditWhat = auditDao.getWhat(Long.parseLong(change.getWhat().getId()));
        auditWhat.setWhatBytes(whatInformation);
        return auditWhat;
    }

    public void delete(AuditHeader auditHeader, AuditChange change) {

        getKvRepo(change).delete(auditHeader, change.getId());
    }



    /**
     * Locate and compare the two JSON What documents to determine if they have changed
     *
     *
     * @param auditHeader
     * @param compareFrom existing change to compare from
     * @param compareWith new Change to compare with - JSON format
     * @return false if different, true if same
     */
    public boolean isSame(AuditHeader auditHeader, AuditChange compareFrom, String compareWith) {
        if (compareFrom == null)
            return false;
        AuditWhat what = getWhat(auditHeader, compareFrom);

        if (what == null)
            return false;

        String jsonThis = what.getWhat();
        if (jsonThis == null || compareWith == null)
            return false;

        if (jsonThis.length() != compareWith.length())
            return false;

        // Compare values
        JsonNode compareTo = null;
        JsonNode other = null;
        try {
            compareTo = om.readTree(jsonThis);
            other = om.readTree(compareWith);
        } catch (IOException e) {
            logger.error("Comparing JSON docs");
        }
        return !(compareTo == null || other == null) && compareTo.equals(other);

    }

    public String getDelta(Long sourceId, Long otherId) {
        // ToDo: obtain the delta
        return null;
    }
}
