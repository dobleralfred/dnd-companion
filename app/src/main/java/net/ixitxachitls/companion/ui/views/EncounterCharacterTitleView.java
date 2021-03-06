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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

import java.util.List;
import java.util.Optional;

/**
 * A view displaying the character information in an encounter.
 */
public class EncounterCharacterTitleView extends EncounterTitleView<Character> {

  public EncounterCharacterTitleView(Context context) {
    this(context, null);
  }

  public EncounterCharacterTitleView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes, R.color.characterLight, R.color.character, R.color.characterLight,
        R.drawable.noun_viking_30736);
  }

  @Override
  public void update(Character character) {
    super.update(character);

    setAction(() -> {
      CompanionFragments.get().showCharacter((Character) character, Optional.of(this));
    });

    updateIcons();
    updateMessages();
  }

  public void update(Messages messages) {
    updateMessages();
  }

  @Override
  protected List<Integer> iconDrawableResources() {
    List<Integer> resources = super.iconDrawableResources();

    if (creature.isPresent()) {
      if (creature.get().amPlayer()) {
        resources.add(R.drawable.noun_puppet_52120);
      }
    }

    return resources;
  }

  @Override
  protected List<Message> messageIcons() {
    List<Message> messages = super.messageIcons();
    messages.addAll(CompanionApplication.get().messages().getMessages(creature.get().getId()));

    return messages;
  }

  @Override
  protected MessageView createMessageIcon(Message message) {
    return new CharacterMessageView(getContext(), creature.get(), message);
  }
}
