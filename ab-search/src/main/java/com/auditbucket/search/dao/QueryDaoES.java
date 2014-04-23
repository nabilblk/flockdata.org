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

package com.auditbucket.search.dao;

import com.auditbucket.dao.QueryDao;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * User: Mike Holdsworth
 * Date: 28/04/13
 * Time: 2:23 PM
 */
@Repository
public class QueryDaoES implements QueryDao {

    @Autowired
    private Client client;

    private Logger logger = LoggerFactory.getLogger(QueryDaoES.class);

    @Override
    public long getHitCount(String index) {
        SearchResponse response = client.prepareSearch(index)
                .execute()
                .actionGet();

        if (logger.isDebugEnabled())
            logger.debug("Searching index [" + index + "] for hit counts");

        return response.getHits().getTotalHits();

    }

    @Override
    public String doSearch(String index, String queryString) {
        SearchResponse result = client.prepareSearch(index)
                .setSource(getSimpleQuery(queryString))
                .execute()
                .actionGet();

        //logger.debug("looking for {} in index {}", queryString, index);
        return result.toString();

    }

    @Override
    public Collection<String> doMetaKeySearch(String index, String queryString) {
        ListenableActionFuture<SearchResponse> future = client.prepareSearch(index)
                .setSource(getSimpleQuery(queryString))
                .execute();
        Collection<String> results = new ArrayList<>();
        SearchResponse response = null;
        try {
            response = future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error ("",e);
        }
        // return the meta keys??
        for (SearchHit searchHitFields : response.getHits().getHits()) {
            results.add(searchHitFields.getSource().get("@metaKey").toString());
        }

        return results;

    }


    private String getSimpleQuery(String queryString) {
        logger.info("getSimpleQuery {}", queryString);

        return "{ query: { " +
                "          query_string : { " +
                "              \"query\" : \"" + queryString + "\" }" +
                "      }}";
    }


}