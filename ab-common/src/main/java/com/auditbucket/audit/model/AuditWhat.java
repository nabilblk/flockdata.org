package com.auditbucket.audit.model;

import java.util.Map;

/**
 * User: Mike Holdsworth
 * Since: 4/09/13
 */
public interface AuditWhat {

    /**
     * @return uncompressed Json String
     */
    public String getWhat();

    /**
     * @return map converted to map via Json ObjectMapper
     */
    public Map<String, Object> getWhatMap();

    public boolean isCompressed();


}
