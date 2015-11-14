// This file is part of MongoFX.
//
// MongoFX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
// Foobar is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

//
// Copyright (c) Andrey Dubravin, 2015
//
package mongofx.ui.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class EditorFileController {
  private static final Logger log = LoggerFactory.getLogger(EditorFileController.class);

  private final UIBuilder uiBuilder;
  private final CodeArea codeArea;

  private File selectedFile;

  public EditorFileController(UIBuilder uiBuilder, CodeArea codeArea) {
    this.uiBuilder = uiBuilder;
    this.codeArea = codeArea;
  }

  public void saveCurrentBufferAs(String intialFileName) {
    File userFile = choosFileToSave(intialFileName);
    if (userFile != null) {
      this.selectedFile = userFile;
      saveToFile(userFile);
    }
  }

  private File choosFileToSave(String intialFileName) {
    FileChooser chooser = new FileChooser();
    chooser.setSelectedExtensionFilter(new ExtensionFilter("Java Script", "*.js"));
    chooser.setInitialFileName(intialFileName);
    File userFile = chooser.showSaveDialog(uiBuilder.getPrimaryStage());
    return userFile;
  }

  public void saveCurrentBuffer(String intialFileName) {
    if (this.selectedFile == null) {
      File userFile = choosFileToSave(intialFileName);
      if (userFile != null) {
        this.selectedFile = userFile;
        saveToFile(userFile);
      }
    } else {
      saveToFile(this.selectedFile);
    }
  }

  private void saveToFile(File selectedFile) {
    try(OutputStream out = Files.newOutputStream(selectedFile.toPath())) {
      out.write(codeArea.getText().getBytes());
    }
    catch (IOException e) {
      log.error("IOException:",e);
    }
  }

  public void loadToBuffer() {
    FileChooser chooser = new FileChooser();
    chooser.setSelectedExtensionFilter(new ExtensionFilter("Java Script", "*.js"));
    File userFile = chooser.showOpenDialog(uiBuilder.getPrimaryStage());
    if (userFile != null) {
      this.selectedFile = userFile;
      try(InputStream in = Files.newInputStream(userFile.toPath())) {
        byte[] text = new byte[in.available()];
        in.read(text);
        codeArea.replaceText(new String(text));
      }
      catch (IOException e) {
        log.error("IOException:",e);
      }
    }
  }

}
