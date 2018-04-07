/*
 * Copyright (c) 2017-2018 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Fake asset accessor for tests.
 */
public class FakeAssetAccessor implements AssetAccessor {

  private final Map<String, String[]> paths = new HashMap<>();
  private final Map<String, byte[]> files = new HashMap<>();

  public FakeAssetAccessor() {
    paths.put("entities", new String[]{
        "Generic", "WTC 17755",
    });
    paths.put("entities/Generic", new String[]{
        "world",
    });
    paths.put("entities/WTC 17755", new String[]{
    });
    paths.put("entities/Generic/world", new String[]{
        "Generic.pb", "Forgotten Realms.pb",
    });
  }

  @Override
  public String[] list(String path) throws IOException {
    if (paths.containsKey(path)) {
      return paths.get(path);
    }

    throw new IllegalArgumentException("Cannot handle path " + path);
  }

  @Override
  public InputStream open(String file) throws IOException {
    return new FileInputStream("../app/src/main/assets/" + file);
  }

  @Override
  public File getExternalFilesDir(String name) {
    return new File("../app/src/test/files/" + name);
  }
}
