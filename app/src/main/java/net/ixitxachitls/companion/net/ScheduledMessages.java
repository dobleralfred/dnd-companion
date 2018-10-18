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

package net.ixitxachitls.companion.net;

import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.data.dynamics.StoredEntries;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Storage for all schedule messages.
 */
public class ScheduledMessages extends StoredEntries<ScheduledMessage> {

  public ScheduledMessages(CompanionContext context) {
    super(context, DataBaseContentProvider.MESSAGES, true);
  }

  @Override
  protected Optional<ScheduledMessage> parseEntry(long id, byte[] blob) {
    try {
      return Optional.of(ScheduledMessage.fromProto(companionContext, id,
          Entry.ScheduledMessageProto.getDefaultInstance().getParserForType().parseFrom(blob)));
    } catch (InvalidProtocolBufferException e) {
      Status.toast("Cannot parse proto for message: " + e);
      return Optional.empty();
    }
  }

  public List<ScheduledMessage> getMessagesByReceiver(String receiverId) {
    List<ScheduledMessage> messages = new ArrayList<>();

    for (ScheduledMessage message : getAll()) {
      if (message.matches(companionContext.me().getId(), receiverId)) {
        messages.add(message);
      }
    }

    return messages;
  }

  public List<ScheduledMessage> getMessagesBySender(String senderId) {
    List<ScheduledMessage> messages = new ArrayList<>();

    for (ScheduledMessage message : getAll()) {
      if (message.getSenderId().equals(senderId)) {
        messages.add(message);
      }
    }

    return messages;
  }

  public void remove(ScheduledMessage message) {
    super.remove(message);
  }
}
