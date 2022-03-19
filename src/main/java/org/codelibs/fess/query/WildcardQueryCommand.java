/*
 * Copyright 2012-2022 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.fess.query;

import java.util.Locale;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.codelibs.fess.Constants;
import org.codelibs.fess.entity.QueryContext;
import org.codelibs.fess.exception.InvalidQueryException;
import org.lastaflute.core.message.UserMessages;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;

public class WildcardQueryCommand extends QueryCommand {
    protected boolean lowercaseWildcard = true;

    @Override
    protected String getQueryClassName() {
        return WildcardQuery.class.getSimpleName();
    }

    @Override
    public QueryBuilder execute(final QueryContext context, final Query query, final float boost) {
        if (query instanceof final WildcardQuery wildcardQuery) {
            return convertWildcardQuery(context, wildcardQuery, boost);
        }
        throw new InvalidQueryException(messages -> messages.addErrorsInvalidQueryUnknown(UserMessages.GLOBAL_PROPERTY_KEY),
                "Unknown q: " + query.getClass() + " => " + query);
    }

    protected QueryBuilder convertWildcardQuery(final QueryContext context, final WildcardQuery wildcardQuery, final float boost) {
        final String field = getSearchField(context, wildcardQuery.getField());
        if (Constants.DEFAULT_FIELD.equals(field)) {
            context.addFieldLog(field, wildcardQuery.getTerm().text());
            return buildDefaultQueryBuilder(
                    (f, b) -> QueryBuilders.wildcardQuery(f, toLowercaseWildcard(wildcardQuery.getTerm().text())).boost(b * boost));
        }
        if (isSearchField(field)) {
            context.addFieldLog(field, wildcardQuery.getTerm().text());
            return QueryBuilders.wildcardQuery(field, toLowercaseWildcard(wildcardQuery.getTerm().text())).boost(boost);
        }
        final String query = wildcardQuery.getTerm().toString();
        final String origQuery = toLowercaseWildcard(query);
        context.addFieldLog(Constants.DEFAULT_FIELD, query);
        context.addHighlightedQuery(origQuery);
        return buildDefaultQueryBuilder((f, b) -> QueryBuilders.wildcardQuery(f, origQuery).boost(b * boost));
    }

    protected String toLowercaseWildcard(final String value) {
        if (lowercaseWildcard) {
            return value.toLowerCase(Locale.ROOT);
        }
        return value;
    }

    public void setLowercaseWildcard(boolean lowercaseWildcard) {
        this.lowercaseWildcard = lowercaseWildcard;
    }
}
