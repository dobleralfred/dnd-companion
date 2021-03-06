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

package net.ixitxachitls.companion.data.documents;

import net.ixitxachitls.companion.data.CompanionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Collection allowing access to user data.
 */
public class Users extends Documents<Users> {
  private Optional<User> me = Optional.empty();
  private Map<String, User> usersById = new HashMap<>();

  public Users(CompanionContext context) {
    super(context);
  }

  public User getMe() {
    if (me.isPresent()) {
      return me.get();
    }

    throw new IllegalStateException("Tried to get logged in user before user logged in!");
  }

  public static boolean isUserId(String id) {
    return id.startsWith(User.PATH + "/");
  }

  public User fromPath(String path) {
    String email = path.replaceAll(User.PATH + "/(.*?)/.*", "$1");
    return get(email);
  }

  public User get(String id) {
    User user = usersById.get(id);
    if (user == null) {
      user = User.getOrCreate(context, id);
      usersById.put(id, user);
    }

    return user;
  }

  public void login(String id, String photoUrl) {
    me = Optional.of(get(id));
    me.get().whenCompleted(() -> {
      me.get().setPhotoUrl(photoUrl);
      me.get().store();

      context.loggedIn(me.get());
    });

    usersById.put(me.get().getId(), me.get());
  }
}
