/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentValues;
import android.content.Context;
import android.database.CursorIndexOutOfBoundsException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.data.dynamics.StoredEntry;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * All the settings value of the user.
 */
@Singleton
public class Settings extends StoredEntry<Data.SettingsProto> {
  public static final String TABLE = "settings";
  public static final int ID = 1;

  private static Settings settings = null;

  private String appId;
  private long lastMessageId = 1;
  private MutableLiveData<Boolean> showStatus = new MutableLiveData<>();
  private boolean remoteCampaigns = false;
  private boolean remoteCharacters = false;
  private List<String> features = new ArrayList<>();

  private Settings(String name) {
    super(ID, TABLE, TABLE + "-" + ID, name, true, DataBaseContentProvider.SETTINGS);

    showStatus.setValue(false);
  }

  public List<String> getFeatures() {
    return Collections.unmodifiableList(features);
  }

  public void setFeatures(List<String> features) {
    this.features.clear();
    this.features.addAll(features);
  }

  public boolean isEnabled(String feature) {
    return features.contains(feature);
  }

  public static ContentValues defaultSettings() {
    ContentValues values = new ContentValues();
    values.put(DataBase.COLUMN_ID, ID);
    values.put(DataBase.COLUMN_PROTO,
        Data.SettingsProto.newBuilder().build().toByteArray());

    return values;
  }

  public static Settings init(Context context) {
    settings = load(context).orElse(new Settings(""));
    settings.ensureAppId();
    return settings;
  }

  private void ensureAppId() {
    if (Strings.isNullOrEmpty(appId)) {
      appId = UUID.randomUUID().toString();
      store();
    }
  }

  @Override
  public Data.SettingsProto toProto() {
    return Data.SettingsProto.newBuilder()
        .setNickname(name)
        .setAppId(appId)
        .setLastMessageId(lastMessageId)
        .setRemoteCampaigns(remoteCampaigns)
        .setRemoteCharacters(remoteCharacters)
        .addAllFeatures(features)
        .build();
  }

  public static Settings get() {
    Preconditions.checkNotNull(settings);
    return settings;
  }

  private static Settings fromProto(Data.SettingsProto proto) {
    Settings settings = new Settings(proto.getNickname());
    settings.appId = proto.getAppId();
    settings.lastMessageId = proto.getLastMessageId();
    settings.remoteCampaigns = proto.getRemoteCampaigns();
    settings.remoteCharacters = proto.getRemoteCharacters();
    settings.features.addAll(proto.getFeaturesList());

    // Don't use 0 as it could be confused with an unset message id.
    if (settings.lastMessageId == 0) {
      settings.lastMessageId = 1;
    }

    return settings;
  }

  private static Optional<Settings> load(Context context) {
    try {
      return Optional.of(fromProto(Data.SettingsProto.getDefaultInstance().getParserForType()
          .parseFrom(loadBytes(context, ID, DataBaseContentProvider.SETTINGS))));
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
      return Optional.empty();
    } catch (CursorIndexOutOfBoundsException e) {
      // For some reason, the default settings are not there anymore. Let's restore them.
      Entries.getContext().getContentResolver().insert(DataBaseContentProvider.SETTINGS,
          Settings.defaultSettings());

      return Optional.empty();
    }
  }

  public boolean isDefined() {
    return !name.isEmpty();
  }

  public String getNickname() {
    if (name == null || name.isEmpty()) {
      return getAppId();
    }

    return name;
  }

  public boolean useRemoteCampaigns() {
    return Misc.onEmulator() && remoteCampaigns;
  }

  public boolean useRemoteCharacters() {
    return Misc.onEmulator() && remoteCharacters;
  }

  public String getAppId() {
    return appId;
  }

  public void setNickname(String name) {
    setName(name);
  }

  public void useRemote(boolean remoteCampaigns, boolean remoteCharacters) {
    this.remoteCampaigns = remoteCampaigns;
    this.remoteCharacters = remoteCharacters;
  }

  public long getNextMessageId() {
    lastMessageId++;
    store();

    return lastMessageId;
  }

  public void setDebugStatus(boolean showStatus) {
    this.showStatus.setValue(showStatus);
  }

  public LiveData<Boolean> shouldShowStatus() {
    return showStatus;
  }
}
