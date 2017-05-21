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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Information and storage for all characters.
 */
public class Characters extends StoredEntries {
  private static final String TAG = "Characters";

  private static Characters local;
  private static Characters remote;

  private final Uri table;
  private final Map<String, Character> characterByCharacterId = new HashMap<>();
  private final Multimap<String, Character> charactersByCamppaignId = HashMultimap.create();

  private Characters(Context context, Uri table) {
    super(context);
    this.table = table;

    Cursor cursor = context.getContentResolver().query(table, DataBase.COLUMNS, null, null, null);
    while (cursor.moveToNext()) {
      try {
        Character character =
            Character.fromProto(cursor.getLong(cursor.getColumnIndex("_id")),
                Data.CharacterProto.getDefaultInstance().getParserForType()
                    .parseFrom(cursor.getBlob(cursor.getColumnIndex(DataBase.COLUMN_PROTO))));
        add(character);
      } catch (InvalidProtocolBufferException e) {
        Log.e("Campaigns", "Cannot parse proto for campaign: " + e);
        Toast.makeText(context, "Cannot parse proto for campaign: " + e, Toast.LENGTH_LONG);
      }
    }
  }

  public static Characters getLocal() {
    Preconditions.checkNotNull(local, "local characters have to be loaded!");
    return local;
  }

  public static Characters getRemote() {
    Preconditions.checkNotNull(local, "remote characters have to be loaded!");
    return remote;
  }

  public static Characters loadLocal(Context context) {
    if (local != null) {
      Log.d("Characters", "local characters already loaded");
      return local;
    }

    Log.d("Characters", "loading local characters");
    local = new Characters(context, DataBaseContentProvider.CHARACTERS_LOCAL);

    return local;
  }

  public static Characters loadRemote(Context context) {
    if (remote != null) {
      Log.d("Characters", "remote characters already loaded");
      return remote;
    }

    Log.d("Characters", "loading remote characters");
    remote = new Characters(context, DataBaseContentProvider.CHARACTERS_REMOTE);

    return remote;
  }

  public Character getCharacter(String characterId, String campaignId) {
    if (characterId.isEmpty()) {
      return Character.createNew(campaignId);
    }

    Preconditions.checkArgument(characterByCharacterId.containsKey(characterId));
    return characterByCharacterId.get(characterId);
  }

  private void add(Character character) {
    if (characterByCharacterId.containsKey(character.getCharacterId())) {
      throw new IllegalArgumentException("Character '" + character.getName()
          + "' and '" + characterByCharacterId.get(character.getCharacterId()).getName()
          + "' share the same id '" + character.getCharacterId() + "'");
    }
    characterByCharacterId.put(character.getCharacterId(), character);
    charactersByCamppaignId.put(character.getCampaignId(), character);
  }

  public void addOrUpdate(Character character) {
    if (characterByCharacterId.containsKey(character.getCharacterId())) {
      Character existingCharacter = characterByCharacterId.get(character.getCharacterId());
      character.mergeFrom(existingCharacter);
      characterByCharacterId.remove(existingCharacter.getCharacterId());
      charactersByCamppaignId.remove(existingCharacter.getCampaignId(),
          existingCharacter);
    }

    add(character);
    character.store();
  }

  public void remove(Character character) {
    characterByCharacterId.remove(character.getCharacterId());
    charactersByCamppaignId.remove(character.getCampaignId(), character);

    context.getContentResolver().delete(table, "id = " + character.getId(), null);
  }

  public List<Character> getCharacters() {
    List<Character> characters = new ArrayList<>(characterByCharacterId.values());
    Collections.sort(characters, new CharacterComparator());
    return characters;
  }

  public List<Character> getCharacters(String campaignId) {
    if (Campaigns.get().getCampaign(campaignId).isDefault()) {
      return getOrphanedCharacters();
    }

    List<Character> characters = new ArrayList<>(charactersByCamppaignId.get(campaignId));
    Collections.sort(characters, new CharacterComparator());
    return characters;
  }

  public List<Character> getOrphanedCharacters() {
    List<Character> characters = new ArrayList<>();

    for (String campaignId : charactersByCamppaignId.keySet()) {
      if (!Campaigns.get().hasCampaign(campaignId)
          || Campaigns.get().getCampaign(campaignId).isDefault()) {
        characters.addAll(charactersByCamppaignId.get(campaignId));
      }
    }

    return characters;
  }

  public void publish() {
    Log.d(TAG, "publishing all characters");
    for (Character character : getCharacters()) {
      character.publish();
      Images.get().publish(character.getCampaignId(), Character.TABLE, character.getCharacterId());
    }
  }

  public void publish(String campaignId) {
    Log.d(TAG, "publishing characters of campaign " + campaignId);
    for (Character character : getCharacters(campaignId)) {
      character.publish();
    }
  }

  private class CharacterComparator implements Comparator<Character> {
    @Override
    public int compare(Character first, Character second) {
      if (first.getId() == second.getId())
        return 0;

      int compare = first.getName().compareTo(second.getName());
      if (compare != 0) {
        return compare;
      }

      return Long.compare(first.getId(), second.getId());
    }

    @Override
    public boolean equals(Object obj) {
      return false;
    }
  }
}
