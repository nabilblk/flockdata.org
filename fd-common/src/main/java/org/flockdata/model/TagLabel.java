/*
 * Copyright (c) 2012-2015 "FlockData LLC"
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

import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

/**
 *
 * Represents part of the tracked schema. This is a label that exists within the graph
 *
 * User: Mike Holdsworth
 * Date: 30/06/13
 * Time: 10:02 AM
 */
@NodeEntity
@TypeAlias("TagLabel")
public class TagLabel {

    @GraphId
    Long id;

    private String name;

    @Indexed(unique = true)
    private String companyKey;

    //@Relationship( type = "TAG_INDEX", direction = Relationship.OUTGOING)
    @RelatedTo( type = "TAG_INDEX", direction = Direction.OUTGOING)
    private Company company;

    protected TagLabel() {
    }

    public TagLabel(Company company, String name) {
        this();
        this.name = name;
        this.companyKey = parseTagLabel(company, name);

    }

    public static String parseTagLabel(Company company, String label) {
        return company.getId() + ".t." + label.toLowerCase().replaceAll("\\s", "");
    }


    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public Company getCompany() {
        return company;
    }

    @Override
    public String toString() {
        return "DocumentType{" +
                "id=" + id +
                ", company=" + company +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagLabel)) return false;

        TagLabel that = (TagLabel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (companyKey != null ? !companyKey.equals(that.companyKey) : that.companyKey != null) return false;
        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (companyKey != null ? companyKey.hashCode() : 0);
        return result;
    }
}
