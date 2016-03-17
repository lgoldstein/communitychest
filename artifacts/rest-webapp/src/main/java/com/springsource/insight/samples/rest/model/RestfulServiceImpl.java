/**
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.samples.rest.model;

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

/**
 * @author lgoldstein
 */
@Service
public class RestfulServiceImpl implements RestfulService {
    private final RestfulRepository    _repo;

    @Inject
    public RestfulServiceImpl(final RestfulRepository repo) {
        _repo = repo;
    }
    /*
     * @see com.springsource.insight.samples.rest.RestfulService#findAll()
     */
    @Override
    public RestfulDataList findAll() {
        final Collection<? extends RestfulData>    values=_repo.findAll();
        if ((values == null) || values.isEmpty()) {
            return new RestfulDataList();
        }

        final RestfulDataList    list=new RestfulDataList(values);
        for (int    i=0; i < list.size(); i++) {
            final RestfulData    val=list.get(i);
            list.set(i, val.clone());
        }

        return list;
    }
    /*
     * @see com.springsource.insight.samples.rest.RestfulService#getData(long)
     */
    @Override
    public RestfulData getData(long id) {
        final RestfulData    value=_repo.getData(id);
        if (value != null) {
            return value.clone();
        }

        return null;
    }
    /*
     * @see com.springsource.insight.samples.rest.RestfulService#create(int)
     */
    @Override
    public RestfulData create(int balance) {
        final RestfulData    value=_repo.create(balance);
        if (value != null) {
            return value.clone();
        }

        return null;
    }
    /*
     * @see com.springsource.insight.samples.rest.RestfulService#setBalance(long, int)
     */
    @Override
    public RestfulData setBalance(long id, int balance) {
        final RestfulData    value=_repo.setBalance(id, balance);
        if (value != null) {
            return value.clone();
        }

        return null;
    }
    /*
     * @see com.springsource.insight.samples.rest.RestfulService#removeData(long)
     */
    @Override
    public RestfulData removeData(long id) {
        return _repo.removeData(id);
    }

}
