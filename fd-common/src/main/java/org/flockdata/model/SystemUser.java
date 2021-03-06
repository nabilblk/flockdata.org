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

import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.*;

@NodeEntity
@TypeAlias(value = "SystemUser")
public class SystemUser  {
    @GraphId
    Long id;

    private String name = null;

    //@Indexed(unique = true)
    private String login;

    @Indexed
    private String apiKey;

    //@Relationship( type = "ACCESSES", direction = Relationship.OUTGOING)
    @Fetch
    @RelatedTo( type = "ACCESSES", direction = Direction.OUTGOING)
    private Company company;

    protected SystemUser() {
    }

    public SystemUser(String name, String login, org.flockdata.model.Company company, boolean admin) {
        setName(name);
        if ( login == null )
            login = name;
        setLogin(login);

        if (admin)
            setAdministers(company);
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login.toLowerCase();
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String openUID) {
        this.apiKey = openUID;
    }

    public Company getCompany() {
        return company;
    }

    public Long getId() {
        return id;

    }

    void setAdministers(org.flockdata.model.Company company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "SystemUserNode{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", company=" + company +
                '}';
    }
}
