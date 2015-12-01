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
package mongofx.service.suggest;

import org.fxmisc.richtext.CodeArea;

import javafx.scene.control.IndexRange;

public class SuggestContext {
	private String collectionName;
	private CodeArea codeArea;

	public SuggestContext(String collectionName, CodeArea codeArea) {
		super();
		this.collectionName = collectionName;
		this.codeArea = codeArea;
	}

	public String getCollectionName() {
		if (collectionName == null) {
			collectionName = "";
		}
		return collectionName;
	}

	public void insert(String text) {
		codeArea.replaceText(codeArea.getSelection(), text);
	}
	
	public void reaplace(int back, String text) {
		IndexRange selection = codeArea.getSelection();
		selection = new IndexRange(selection.getStart() - back, selection.getEnd());
		codeArea.replaceText(selection, text);
	}
}
