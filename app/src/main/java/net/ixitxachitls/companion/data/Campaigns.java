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

package net.ixitxachitls.companion.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Preconditions;
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
 * Access to and utilities for camapigns.
 */
public class Campaigns {

  private static Campaigns singleton;

  private Context context;
  private Map<Long, Campaign> campaignsByStorageId = new HashMap<>();
  private Map<String, Campaign> campaignsByCampaignId = new HashMap<>();
  private List<Campaign> campaignsSorted = new ArrayList<>();

  public Campaigns(Context context) {
    this.context = context;
  }

  public static Campaigns get() {
    Preconditions.checkNotNull(singleton, "Campaigns have to be loaded!");
    return singleton;
  }

  public static Campaigns load(Context context) {
    if (singleton != null) {
      Log.d("Campaigns", "campaigns already loaded");
      return singleton;
    }

    Log.d("Campaigns", "loading campaigns");
    singleton = new Campaigns(context);

    Cursor cursor = context.getContentResolver().query(
        DataBaseContentProvider.CAMPAIGNS, DataBase.COLUMNS, null, null, null);
    cursor.moveToFirst();
    do {
      try {
        Campaign campaign =
            Campaign.fromProto(cursor.getLong(cursor.getColumnIndex("_id")),
                Data.CampaignProto.getDefaultInstance().getParserForType()
                    .parseFrom(cursor.getBlob(cursor.getColumnIndex(DataBase.COLUMN_PROTO))));
        singleton.add(campaign);
      } catch (InvalidProtocolBufferException e) {
        Log.e("Campaigns", "Cannot parse proto for campaign: " + e);
        Toast.makeText(context, "Cannot parse proto for campaign: " + e, Toast.LENGTH_LONG);
      }
    } while(cursor.moveToNext());

    return singleton;
  }

  public Campaign getCampaign(String id) {
    if (id.isEmpty()) {
      return Campaign.createNew();
    }

    Preconditions.checkArgument(campaignsByCampaignId.containsKey(id));
    return campaignsByCampaignId.get(id);
  }

  private void add(Campaign campaign) {
    campaignsSorted.add(campaign);
    campaignsByStorageId.put(campaign.getId(), campaign);
    campaignsByCampaignId.put(campaign.getCampaignId(), campaign);
  }

  public void ensureAdded(Campaign campaign) {
    if (!campaignsByStorageId.containsKey(campaign.getId())) {
      add(campaign);
    }
  }

  public void remove(Campaign campaign) {
    campaignsSorted.remove(campaign);
    campaignsByStorageId.remove(campaign.getId());
    campaignsByCampaignId.remove(campaign.getCampaignId());

    context.getContentResolver().delete(DataBaseContentProvider.CAMPAIGNS,
        "id = " + campaign.getId(), null);
  }

  public List<Campaign> getCampaigns() {
    Collections.sort(campaignsSorted, new CampaignComparator());
    return campaignsSorted;
  }

  private class CampaignComparator implements Comparator<Campaign> {
    @Override
    public int compare(Campaign first, Campaign second) {
      if (first.getId() == second.getId())
        return 0;

      if (first.isDefault() && !second.isDefault()) {
        return -1;
      }
      if (!first.isDefault() && second.isDefault()) {
        return +1;
      }

      if (first.isLocal() && !second.isLocal()) {
        return -1;
      }
      if (!first.isLocal() && second.isLocal()) {
        return +1;
      }

      return first.getName().compareTo(second.getName());
    }

    @Override
    public boolean equals(Object obj) {
      return false;
    }
  }
}