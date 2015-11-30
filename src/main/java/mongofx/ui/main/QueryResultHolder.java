// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// MongoFX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with MongoFX.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.ui.main;

import com.mongodb.client.MongoCursor;
import mongofx.js.api.ObjectListPresentation;
import mongofx.js.api.TextPresentation;
import org.bson.Document;

import java.util.List;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class QueryResultHolder {
  private final ObjectListPresentation objectListResult;
  private final TextPresentation textResult;

  // cache
  private int cacheSkip = 0;
  private int cacheLimit = 0;
  private List<Document> cacheDocuments;

  public QueryResultHolder() {
    objectListResult = null;
    textResult = null;
  }

  public QueryResultHolder(TextPresentation textResult) {
    objectListResult = null;
    this.textResult = textResult;
  }

  public QueryResultHolder(ObjectListPresentation objectListResult) {
    textResult = null;
    this.objectListResult = objectListResult;
  }

  public boolean isTextOnlyPresentation() {
    return textResult != null;
  }

  public boolean isEmpty() {
    return textResult == null && objectListResult == null;
  }

  private void assertObjectList() {
    if (isTextOnlyPresentation()) {
      throw new IllegalStateException("Can't call object list method on text presentation");
    }
  }

  public Optional<Integer> getSkip() {
    assertObjectList();
    return objectListResult.getSkip();
  }

  public Optional<Integer> getLimit() {
    assertObjectList();
    return objectListResult.getLimit();
  }

  public List<Document> getDocuments(int skip, int limit) {
    assertObjectList();
    if (cacheDocuments == null || cacheSkip != skip || cacheLimit != limit) {
      try (MongoCursor<Document> iterator = objectListResult.iterator(skip, limit)) {
        cacheDocuments = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false).collect(Collectors.toList());
        cacheSkip = skip;
        cacheLimit = limit;
      }
    }

    return cacheDocuments;
  }

  public String getListPresentationString(int skip, int limit) {
    return getDocuments(skip, limit).stream().map(DocumentUtils::formatJson).collect(Collectors.joining(",\n", "[", "]"));
  }

  public String getTextPresentationString() {
    return String.valueOf(textResult);
  }

  public String getCollectionName() {
    return objectListResult.getCollectionName();
  }
}
