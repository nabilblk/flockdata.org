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

package org.flockdata.model;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

/**
 * User: mike
 * Date: 3/10/14
 * Time: 4:56 PM
 */
@NodeEntity(useShortNames = true)
@TypeAlias("Profile")
public class Profile {
    @GraphId
    private Long id;

    @Indexed(unique = true)
    private String profileKey;

    //@Relationship(type = "FORTRESS_PROFILE")
    @RelatedTo(type = "FORTRESS_PROFILE")
    private Fortress fortress;

    //@Relationship( type = "DOCUMENT_PROFILE")
    @RelatedTo( type = "DOCUMENT_PROFILE")
    private DocumentType document;

    private String content;

    Profile() {}

    public Profile(Fortress fortress, DocumentType documentType) {
        this();
        this.fortress = fortress;
        this.document = documentType;
        this.profileKey = parseKey(fortress, documentType);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public static String parseKey(Fortress fortress, DocumentType documentType) {
        return fortress.getId() +"-"+documentType.getId();
    }

    public Long getId() {
        return id;
    }

}
