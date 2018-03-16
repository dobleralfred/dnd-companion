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

package net.ixitxachitls.companion.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.CharacterDialog;

import java.io.IOException;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment for a local character.
 */
public class LocalCharacterFragment extends CharacterFragment {

  private final int PICK_IMAGE = 1;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);

    image.setAction(this::editImage);
    title.setAction(this::editBase);
    edit.visible()
        .onClick(this::editBase)
        .description("Edit Character", "Edit the basic character traits");
    delete.visible()
        .onClick(this::delete)
        .description("Delete Character", "Delete this character. This will irrevocably delete "
            + "the character and will send a deletion request to the DM and all other players.");
    move.visible()
        .onClick(this::move)
        .description("Move Character", "This button moves the character to an other campaign.");
    strength.setAction(this::editAbilities);
    dexterity.setAction(this::editAbilities);
    constitution.setAction(this::editAbilities);
    intelligence.setAction(this::editAbilities);
    wisdom.setAction(this::editAbilities);
    charisma.setAction(this::editAbilities);
    xp.onChange(this::changeXp);
    level.onChange(this::changeLevel);

    return view;
  }

  private void editImage() {
    if (character.isPresent()) {
      Intent intent = new Intent();
      // Show only images, no videos or anything else
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      // Always show the chooser (if there are multiple options available)
      startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
  }

  private void editBase() {
    if (!canEdit() || !character.isPresent()) {
      return;
    }

    CharacterDialog.newInstance(character.get().getCharacterId(),
        character.get().getCampaignId()).display();
  }

  private void delete() {
    ConfirmationDialog.create(getContext())
        .title(getResources().getString(R.string.character_delete_title))
        .message(getResources().getString(R.string.character_delete_message))
        .yes(this::deleteCharacterOk)
        .show();
  }

  private void deleteCharacterOk() {
    if (character.isPresent()) {
      Characters.remove(character.get());
      Toast.makeText(getActivity(), getString(R.string.character_deleted),
          Toast.LENGTH_SHORT).show();

      storeOnPause = false;
      if (campaign.isPresent()) {
        show(campaign.get().isLocal() ? Type.localCampaign : Type.campaign);
      }
    }
  }

  private void move() {
    ListSelectDialog fragment = ListSelectDialog.newInstance(
        R.string.character_select_campaign, "",
        Campaigns.getAllCampaigns().stream()
            .map(m -> new ListSelectDialog.Entry(m.getName(), m.getCampaignId()))
            .collect(Collectors.toList()),
        R.color.campaign);
    fragment.setSelectListener(this::move);
    fragment.display();
  }

  private void move(String campaignId) {
    if (character.isPresent()) {
      character.get().setCampaignId(campaignId);
    }

    Optional<Campaign> campaign = Campaigns.getCampaign(campaignId).getValue();
    if (campaign.isPresent()) {
      CompanionFragments.get().showCampaign(campaign.get(), Optional.absent());
    }
  }

  private void editAbilities() {
    if (!canEdit() || !character.isPresent()) {
      return;
    }

    AbilitiesDialog.newInstance(character.get().getCharacterId(),
        character.get().getCampaignId()).display();
  }

  private void changeXp() {
    if (character.isPresent() && !xp.getText().isEmpty()) {
      character.get().setXp(Integer.parseInt(xp.getText()));
    }
  }

  private void changeLevel() {
    if (character.isPresent() && !level.getText().isEmpty()) {
      character.get().setLevel(Integer.parseInt(level.getText()));
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
        data != null && data.getData() != null && character.isPresent()) {
      try {
        Uri uri = data.getData();
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        Image characterImage = new Image(Character.TABLE, character.get().getCharacterId(), bitmap);
        characterImage.save(character.get().isLocal());
        characterImage.publish();
        image.setImageBitmap(characterImage.getBitmap());
      } catch (IOException e) {
        Status.toast("Cannot load image bitmap: " + e);
      }
    }
  }

  public boolean canEdit() {
    return campaign.isPresent() && character.isPresent();
  }
}