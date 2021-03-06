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

import com.google.protobuf.Internal;

import java.util.ArrayList;

/**
 * Helper methods for enum values.
 */

public class Enums {

  public interface Named {
    String getName();
    String getShortName();
  }

  public interface Proto<P extends Internal.EnumLite> {
    P toProto();
  }

  private static <E extends Named> boolean contains(E [] values, E check) {
    for (E value : values) {
      if (value == check) {
        return true;
      }
    }

    return false;
  }

  public static <E extends Named> E fromName(String name, E []values) {
    for(E value : values)
      if (value.getName().equalsIgnoreCase(name))
        return value;

    throw new IllegalArgumentException("cannot convert " + values[0].getClass().getName()
        + ": " + name);
  }

  public static <P extends Internal.EnumLite, E extends Proto<P>>
  E fromProto(P proto, E []values) {
    for (E value : values)
      if (value.toProto() == proto)
        return value;

    throw new IllegalArgumentException("cannot convert " + proto.getClass().getName()
        + ": " + proto);
  }

  @SuppressWarnings("unchecked")
  public static <E extends Named> ArrayList<String> names(E []values, E ... without) {
    ArrayList<String> names = new ArrayList<>();
    for (E value : values) {
      if (contains(without, value)) {
        continue;
      }
      names.add(value.getName());
    }

    return names;
  }

  public static <E extends Named> ArrayList<String> names(E []values) {
    ArrayList<String> names = new ArrayList<>();
    for(E value : values)
      names.add(value.getName());

    return names;
  }
}
