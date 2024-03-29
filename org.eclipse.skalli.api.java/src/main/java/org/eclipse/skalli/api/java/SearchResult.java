/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.api.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.model.ext.EntityBase;

public class SearchResult<T extends EntityBase> {

    private String queryString;
    private long duration;
    private List<SearchHit<T>> result;
    private int resultCount;
    private PagingInfo pagingInfo;

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<SearchHit<T>> getResult() {
        return result;
    }

    public void setResult(List<SearchHit<T>> result) {
        this.result = result;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }

    public PagingInfo getPagingInfo() {
        return pagingInfo;
    }

    public void setPagingInfo(PagingInfo pagingInfo) {
        this.pagingInfo = pagingInfo;
    }

    public List<T> getEntities() {
        List<T> ret = new ArrayList<T>(result.size());
        for (SearchHit<T> hit : getResult()) {
            ret.add(hit.getEntity());
        }
        return ret;
    }

}
