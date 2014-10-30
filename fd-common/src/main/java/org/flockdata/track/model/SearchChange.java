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

package org.flockdata.track.model;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.Map;

/**
 * User: Mike Holdsworth
 * Date: 21/04/13
 * Time: 7:44 PM
 */
public interface SearchChange {
    /**
     * @return search keys unique document identifier
     */
    public String getSearchKey();

    public void setSearchKey(String parent);

    /**
     * primary key of the Entity record that this document belongs to
     *
     * @return GUID
     */
    String getMetaKey();

    public void setWhat(Map<String, Object> what);

    public Map<String, Object> getWhat();

    public java.util.HashMap<String, Map<String, Object>> getTagValues();

    public void setTags(Iterable<EntityTag> tagSet);

    /**
     * @return who made this change
     */
    public String getWho();

    public Long getSysWhen();

    public Date getCreatedDate();

    public String getFortressName();

    public String getCompanyName();

    public String getIndexName();

    /**
     * @return date this was created in the owning fortress
     */
    public java.util.Date getWhen();

    /**
     * when this log was created in the Fortress
     *
     * @param date date
     */
    void setWhen(DateTime date);

    void setWho(String name);

    String getDocumentType();

    String getCallerRef();

    String getEvent();

    public void setSysWhen(Long sysWhen);

    void setLogId(Long id);

    Long getLogId();

    Long getEntityId();

    void setDescription(String description);

    String getDescription();

    /**
     * Hint to determine if a reply from the search service is expected
     * by the caller
     * <p/>
     * default to true
     */
    public void setReplyRequired(boolean required);

    boolean isReplyRequired();

    /**
     * Forces the search engine to ignore date checks and force an update of the document.
     * Usually in response to a cancellation in fd-engine
     *
     */
    public boolean isForceReindex();

    /**
     *
     * @return if the searchKey should be removed
     */
    public boolean isDelete();

    void setName(String name);

    void setAttachment(String attachment);

    boolean hasAttachment();

    String getAttachment();

    String getFileName();

    String getContentType();
}