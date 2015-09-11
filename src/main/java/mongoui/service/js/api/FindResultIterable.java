package mongoui.service.js.api;

import java.util.Iterator;

import org.bson.Document;

import com.mongodb.client.FindIterable;

public class FindResultIterable implements ObjectListPresentationIterables {

  private FindIterable<Document> findIterable;

  public FindResultIterable(FindIterable<Document> findIterable) {
    this.findIterable = findIterable;
  }

  @Override
  public Iterator<Document> iterator() {
    return findIterable.iterator();
  }

}
