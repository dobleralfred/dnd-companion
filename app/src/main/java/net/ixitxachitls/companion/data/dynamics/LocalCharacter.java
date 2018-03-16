/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.data.values.Condition;
import net.ixitxachitls.companion.data.values.TargetedTimedCondition;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * A local instanace of a character.
 */
public class LocalCharacter extends Character {

  public LocalCharacter(long id, String name, String campaignId) {
    super(id, name, campaignId, true, DataBaseContentProvider.CHARACTERS_LOCAL);
  }

  public static LocalCharacter createNew(String campaignId) {
    return new LocalCharacter(0, "", campaignId);
  }

  @Override
  public void setCampaignId(String campaignId) {
    this.campaignId = campaignId;
  }

  @Override
  public void setRace(String name) {
    mRace = Entries.get().getMonsters().get(name);
  }

  @Override
  public void setGender(Gender gender) {
    this.gender = gender;
  }

  @Override
  public void setStrength(int strength) {
    if (this.strength != strength) {
      this.strength = strength;
    }
  }

  @Override
  public void setConstitution(int constitution) {
    if (this.constitution != constitution) {
      this.constitution = constitution;
    }
  }

  @Override
  public void setDexterity(int dexterity) {
    if (this.dexterity != dexterity) {
      this.dexterity = dexterity;
    }
  }

  @Override
  public void setIntelligence(int intelligence) {
    if (this.intelligence != intelligence) {
      this.intelligence = intelligence;
    }
  }

  @Override
  public void setWisdom(int wisdom) {
    if (this.wisdom != wisdom) {
      this.wisdom = wisdom;
    }
  }

  @Override
  public void setCharisma(int charisma) {
    if (this.charisma != charisma) {
      this.charisma = charisma;
    }
  }

  @Override
  public void setBattle(int initiative, int number) {
    this.initiative = initiative;
    this.initiativeRandom = RANDOM.nextInt(100_000);
    this.battleNumber = number;

    // TODO(merlin): If we want to support long running conditions outside of battle, this has to
    // change.
    this.initiatedConditions.clear();

    // Clear all conditions exception surprised, as we only just added it.
    for (Iterator<TimedCondition> i = affectedConditions.iterator(); i.hasNext(); ) {
      if (!i.next().getName().equals(Conditions.SURPRISED.getName())) {
        i.remove();
      }
    }
    store();
  }

  @Override
  public void clearInitiative() {
    this.initiative = NO_INITIATIVE;
    store();
  }

  @Override
  public void setXp(int xp) {
    this.xp = xp;
  }

  @Override
  public void addXp(int xp) {
    this.xp += xp;
    store();
  }

  @Override
  public void setLevel(int index, Level level) {
    if(levels.size() > index) {
      levels.set(index, level);
    } else {
      addLevel(level);
    }
  }

  @Override
  public void addLevel(Character.Level level) {
    levels.add(level);
  }

  // TODO: remove this once we properly support level objets.
  @Override
  public void setLevel(int level) {
    levels.clear();
    for (int i = 0; i < level; i++) {
      addLevel(new Character.Level("Barbarian"));
    }
  }

  @Override
  public void addInitiatedCondition(TargetedTimedCondition condition) {
    if (!condition.isPredefined()) {
      conditionsHistory.add(condition.getCondition());
      conditionsHistory =
          conditionsHistory.subList(0, Math.min(conditionsHistory.size(), MAX_HISTORY));
    }

    super.addInitiatedCondition(condition);
  }

  public void publish() {
    Status.log("publishing character " + this);
    CompanionMessenger.get().send(this);
  }

  @Override
  public boolean store() {
    if (playerName.isEmpty()) {
      playerName = Settings.get().getNickname();
    }

    if (super.store()) {
      CompanionMessenger.get().send(this);

      return true;
    }

    return false;
  }

  @Override
  public String toString() {
    return super.toString() + "/local";
  }

  public static LocalCharacter fromProto(long id, Data.CharacterProto proto) {
    LocalCharacter character = new LocalCharacter(id, proto.getCreature().getName(),
        proto.getCreature().getCampaignId());
    character.fromProto(proto.getCreature());
    character.playerName = proto.getPlayer();
    character.conditionsHistory = proto.getConditionHistoryList().stream()
        .map(Condition::fromProto)
        .collect(Collectors.toList());
    character.xp = proto.getXp();

    for (Data.CharacterProto.Level level : proto.getLevelList()) {
      character.levels.add(Character.fromProto(level));
    }

    if (character.playerName.isEmpty()) {
      character.playerName = Settings.get().getNickname();
    }

    return character;
  }
}