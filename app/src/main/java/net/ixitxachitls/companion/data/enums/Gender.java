/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
 *
 * The Roleplay Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Roleplay Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.enums;

import net.ixitxachitls.companion.proto.Value;

import java.util.ArrayList;

/**
 * Representation of a gender of an entity (character, npc, monster, ...).
 */
public enum Gender implements Enums.Named, Enums.Proto<Value.Gender> {
  UNKNOWN("Unknown", "U", Value.Gender.UNKNOWN_GENDER),
  FEMALE("Female", "F", Value.Gender.FEMALE),
  MALE("Male", "M", Value.Gender.MALE),
  OTHER("Other", "O", Value.Gender.NONE_GENDER);

  private final String name;
  private final String shortName;
  private final Value.Gender proto;

  Gender(String name, String shortName, Value.Gender proto) {
    this.name = name;
    this.shortName = shortName;
    this.proto = proto;
  }

  public String getName() {
    return name;
  }

  public String getShortName() {
    return shortName;
  }

  public Value.Gender toProto() {
    return proto;
  }

  public static Gender fromProto(Value.Gender proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values(), UNKNOWN);
  }

  public static Gender fromName(String name) {
    return Enums.fromName(name, values());
  }
}
