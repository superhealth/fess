/*
 * Copyright 2012-2023 CodeLibs Project and the Others.
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
package org.codelibs.fess.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codelibs.fess.es.client.SearchEngineClient;
import org.lastaflute.di.exception.IORuntimeException;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.common.xcontent.XContentHelper;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.search.SearchHit;

public final class SearchEngineUtil {

    private static final Logger logger = LogManager.getLogger(SearchEngineUtil.class);

    private SearchEngineUtil() {
    }

    public static OutputStream getXContentBuilderOutputStream(final XContentBuilderCallback func, final XContentType xContentType) {
        try (final XContentBuilder builder = func.apply(XContentFactory.contentBuilder(xContentType), ToXContent.EMPTY_PARAMS)) {
            builder.flush();
            return builder.getOutputStream();
        } catch (final IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to print the output.", e);
            }
            return new ByteArrayOutputStream();
        }
    }

    public static OutputStream getXContentOutputStream(final ToXContent xContent, final XContentType xContentType) {
        return getXContentBuilderOutputStream((builder, params) -> xContent.toXContent(builder, params), xContentType);
    }

    public static long scroll(final String index, final Function<SearchHit, Boolean> callback) {
        final SearchEngineClient client = ComponentUtil.getSearchEngineClient();
        return client.<SearchHit> scrollSearch(index, searchRequestBuilder -> true, (searchResponse, hit) -> hit,
                hit -> callback.apply(hit));
    }

    public static String getXContentString(final ToXContent xContent, final XContentType xContentType) {
        try {
            return XContentHelper.toXContent(xContent, xContentType, ToXContent.EMPTY_PARAMS, false).utf8ToString();
        } catch (final IOException e) {
            throw new IORuntimeException(e);
        }
    }

    public interface XContentBuilderCallback {
        XContentBuilder apply(XContentBuilder builder, ToXContent.Params params) throws IOException;
    }

}
