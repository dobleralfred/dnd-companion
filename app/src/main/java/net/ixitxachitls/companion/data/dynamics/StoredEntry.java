/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Player Companion.
 *
 * The Player Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Player Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.dynamics;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.util.Ids;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An entry that is stored in the database.
 */
public abstract class StoredEntry<P extends MessageLite> extends DynamicEntry<P> {
  private static final String TAG = "StoredEntry";
  private static final Map<String, MessageLite> PROTO_CACHE = new ConcurrentHashMap<>();

  private long id;
  protected String entryId;
  private final Uri dbUrl;
  private final boolean local;

  protected StoredEntry(long id, String entryId, String name, boolean local, Uri dbUrl) {
    super(name);

    this.id = id;
    this.entryId = entryId;
    this.local = local;
    this.dbUrl = dbUrl;
  }

  private static ContentValues toValues(MessageLite proto) {
    ContentValues values = new ContentValues();
    values.put(DataBase.COLUMN_PROTO, proto.toByteArray());

    return values;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Override
  public void setName(String name) {
    super.setName(name);
  }

  protected long getId() {
    return id;
  }

  public boolean isLocal() {
    return local;
  }

  public String getEntryId() {
    return entryId;
  }

  public String getServerId() {
    return Ids.extractServerId(entryId);
  }

  public boolean store() {
    // TODO: move the new proto to a map per id to make it global for all entry objects.
    P proto = toProto();

    String key = protoCacheKey();
    if (proto.equals(PROTO_CACHE.get(key))) {
      Log.d(TAG, "no changes for " + getClass().getSimpleName() + "/" + getName());
      return false;
    }

    if (id == 0) {
      Uri row = Entries.getContext().getContentResolver().insert(dbUrl, toValues(proto));
      id = ContentUris.parseId(row);
      if (isLocal()) {
        entryId = Settings.get().getAppId() + "-" + id;
      }
      proto = toProto();
    }

    // Store it again if id made us change the entry id above.
    Entries.getContext().getContentResolver().update(dbUrl, toValues(proto),
        "id = " + id, null);

    PROTO_CACHE.put(key, proto);
    Log.d(TAG, "stored changes for " + getClass().getSimpleName() + "/" + getName());
    return true;
  }

  private String protoCacheKey() {
    return entryId + "-" + isLocal() + "-" + getClass().getSimpleName();
  }
}
