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
package mongofx.driver;

import static com.mongodb.assertions.Assertions.notNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.mongodb.ExplainVerbosity;
import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.FindOptions;
import com.mongodb.operation.FindOperation;
import com.mongodb.operation.OperationExecutor;

public class FindIterable implements Iterable<Document>{

  private final MongoNamespace namespace;
  private final ReadPreference readPreference;
  private final CodecRegistry codecRegistry;
  private final OperationExecutor executor;
  private final FindOptions findOptions;
  private final Bson filter;

  public FindIterable(final MongoNamespace namespace,
      final CodecRegistry codecRegistry,
      final ReadPreference readPreference, final OperationExecutor executor,
      final Bson filter, final FindOptions findOptions) {
    this.namespace = notNull("namespace", namespace);
    this.codecRegistry = notNull("codecRegistry", codecRegistry);
    this.readPreference = notNull("readPreference", readPreference);
    this.executor = notNull("executor", executor);
    this.filter = notNull("filter", filter);
    this.findOptions = notNull("findOptions", findOptions);
  }

  @Override
  public MongoCursor<Document> iterator() {
    return new MongoBatchCursorAdapter<>(executor.execute(createQueryOperation(), readPreference));
  }

  public BsonDocument explainIterator(ExplainVerbosity explainVerbosity) {
    return executor.execute(createQueryOperation().asExplainableOperation(explainVerbosity), readPreference);
  }

  private FindOperation<Document> createQueryOperation() {
    return new FindOperation<>(namespace, codecRegistry.get(Document.class))
        .filter(filter.toBsonDocument(Document.class, codecRegistry))
        .batchSize(findOptions.getBatchSize())
        .skip(findOptions.getSkip())
        .limit(findOptions.getLimit())
        .maxTime(findOptions.getMaxTime(MILLISECONDS), MILLISECONDS)
        .modifiers(toBsonDocument(findOptions.getModifiers()))
        .projection(toBsonDocument(findOptions.getProjection()))
        .sort(toBsonDocument(findOptions.getSort()))
        .cursorType(findOptions.getCursorType())
        .noCursorTimeout(findOptions.isNoCursorTimeout())
        .oplogReplay(findOptions.isOplogReplay())
        .partial(findOptions.isPartial())
        .slaveOk(readPreference.isSlaveOk());
  }

  private BsonDocument toBsonDocument(final Bson document) {
    return document == null ? null : document.toBsonDocument(Document.class, codecRegistry);
  }
}
