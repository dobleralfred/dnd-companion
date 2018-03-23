/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

package net.ixitxachitls.companion.data.dynamics;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Information and storage for all creatures. Creatures that are also characters are not
 * stored here and should be obtained from Characters.
 */
public class Creatures {
  private static CreaturesData local;

  // Live data storages.
  private static final Map<String, MutableLiveData<ImmutableList<String>>>
      creatureIdsByCampaignId = new ConcurrentHashMap<>();

  public static Optional<? extends BaseCreature> getCreatureOrCharacter(String creatureId) {
    Optional<? extends DynamicEntry> entry = StoredEntries.getTyped(creatureId);
    if (entry.isPresent() && entry.get() instanceof BaseCreature) {
      return Optional.of((BaseCreature) entry.get());
    }

    return Optional.empty();
  }

  // Data accessors.

  public static LiveData<Optional<Creature>> getCreature(String creatureId) {
    return local.getCreature(creatureId);
  }

  public static boolean has(Creature creature){
    return has(creature.getCreatureId());
  }

  public static boolean has(String creatureId) {
    return local.has(creatureId);
  }

  public static LiveData<ImmutableList<String>> getCampaignCreatureIds(String campaignId) {
    if (creatureIdsByCampaignId.containsKey(campaignId)) {
      return creatureIdsByCampaignId.get(campaignId);
    }

    MutableLiveData<ImmutableList<String>> ids = new MutableLiveData<>();
    LiveDataUtils.setValueIfChanged(ids, ImmutableList.copyOf(creatureIds(campaignId)));
    creatureIdsByCampaignId.put(campaignId, ids);

    return ids;
  }

  public static List<Creature> getCampaignCreatures(String campaignId) {
    return local.getCreatures(campaignId);
  }

  // Data mutations.

  public static void update(Creature creature) {
    Status.log("updating creature " + creature);

    // We cannot move a creature to a different campaign, so id lists cannot change.
    local.update(creature);
  }

  public static void add(Creature creature) {
    Status.log("adding creature " + creature);

    local.add(creature);

    if (creatureIdsByCampaignId.containsKey(creature.getCampaignId())) {
      LiveDataUtils.setValueIfChanged(creatureIdsByCampaignId.get(creature.getCampaignId()),
          ImmutableList.copyOf(creatureIds(creature.getCampaignId())));
    }
  }

  public static void remove(String creatureId) {
    Optional<Creature> creature = local.getCreature(creatureId).getValue();
    if (creature.isPresent()) {
      remove(creature.get());
    } else {
      Status.error("Cannot remove unknown creature " + creatureId);
    }
  }

  public static void remove(Creature creature) {
    Status.log("removing creature " + creature);
    local.remove(creature);

    // Update live data.
    if (creatureIdsByCampaignId.containsKey(creature.getCampaignId())) {
      LiveDataUtils.setValueIfChanged(creatureIdsByCampaignId.get(creature.getCampaignId()),
            ImmutableList.copyOf(local.ids(creature.getCampaignId())));
    }

    Images.get(creature.isLocal()).remove(Creature.TABLE, creature.getCreatureId());
  }

  // Private methods.

  // While this method is public, it should only be called in the main application.
  public static void load(CompanionApplication application) {
    loadLocal(application);
  }

  private static void loadLocal(CompanionApplication application) {
    if (local != null) {
      Status.log("local creature already loaded");
      return;
    }

    Status.log("loading local creature");
    local = new CreaturesData(application);
  }

  private static List<String> orphaned() {
    return local.orphaned().stream()
        .map(Creature::getCreatureId)
        .collect(Collectors.toList());
  }

  private static List<String> creatureIds(String campaignId) {
    if (campaignId.equals(Campaigns.defaultCampaign.getCampaignId())) {
      return orphaned();
    }

    return local.ids(campaignId);
  }
}
