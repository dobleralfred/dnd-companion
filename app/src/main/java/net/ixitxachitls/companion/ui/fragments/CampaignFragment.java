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

package net.ixitxachitls.companion.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.design.widget.FloatingActionButton;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.DateDialog;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;
import net.ixitxachitls.companion.ui.dialogs.InviteDialog;
import net.ixitxachitls.companion.ui.dialogs.MonsterInitiativeDialog;
import net.ixitxachitls.companion.ui.dialogs.TimedConditionDialog;
import net.ixitxachitls.companion.ui.dialogs.XPDialog;
import net.ixitxachitls.companion.ui.views.CampaignTitleView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.io.IOException;
import java.util.Optional;

import static android.app.Activity.RESULT_OK;

/** A fragment displaying campaign information. */
public class CampaignFragment extends CompanionFragment {

  private final int PICK_IMAGE = 1;

  protected Campaign campaign;

  // UI elements.
  protected CampaignTitleView title;
  protected TextWrapper<TextView> date;
  protected PartyFragment party;
  protected EncounterFragment encounter;
  protected Wrapper<FloatingActionButton> delete;
  protected Wrapper<FloatingActionButton> edit;
  protected Wrapper<FloatingActionButton> calendar;
  protected Wrapper<FloatingActionButton> invite;
  protected Wrapper<ImageView> startEncounter;
  private LinearLayout encounterActions;
  protected Wrapper<ImageView> endEncounter;
  protected Wrapper<ImageView> nextInEncounter;
  protected Wrapper<ImageView> conditionInEncounter;
  protected Wrapper<ImageView> addMonsterInEncounter;
  protected Wrapper<ImageView> delayInEncounter;
  private Wrapper<ImageView> xp;

  public CampaignFragment() {
    super(Type.campaign);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign, container, false);

    Wrapper.<ImageView>wrap(view, R.id.back)
        .onClick(this::goBack)
        .description("Back", "Go back to the list of campaigns.");
    title = view.findViewById(R.id.title);
    title.setImageAction(this::editImage);
    images().observe(this, title::update);
    party = (PartyFragment) getChildFragmentManager().findFragmentById(R.id.party);
    encounter = (EncounterFragment) getChildFragmentManager().findFragmentById(R.id.encounter);
    encounter.hide();
    delete = Wrapper.<FloatingActionButton>wrap(view, R.id.delete).gone();
    delete.onClick(this::deleteCampaign).gone()
        .description("Delete", "Delete this campaign. This action cannot be undone and will send "
            + "a deletion request to players to delete this campaign on their devices too. "
            + "You cannot delete a campaign that is currently published or that has local "
            + "characters.");
    edit = Wrapper.<FloatingActionButton>wrap(view, R.id.edit).gone()
          .description("Edit", "Change the basic information of the campaign.");
    calendar = Wrapper.<FloatingActionButton>wrap(view, R.id.calendar).gone()
        .description("Calendar", "Open the calendar for the campaign to allow you to change the "
            + "current date and time of your campaign.");
    invite = Wrapper.<FloatingActionButton>wrap(view, R.id.invite).gone()
        .description("Invite", "Invite players to create characters in this campaign.");
    date = TextWrapper.wrap(view, R.id.date)
        .description("Calendar", "Open the calendar for the campaign to allow you to change the "
            + "current date and time of your campaign.");
    startEncounter = Wrapper.<ImageView>wrap(view, R.id.encounter_start)
        .onClick(this::startEncounter)
        .description("Start Encounter", "Start an encounter (combat).");
    encounterActions = view.findViewById(R.id.actions_encounter);
    endEncounter = Wrapper.<ImageView>wrap(view, R.id.encounter_end)
        .onClick(this::endEncounter)
        .description("End Encounter", "Stop the encounter")
        .invisible();
    nextInEncounter = Wrapper.<ImageView>wrap(view, R.id.encounter_next)
        .onClick(this::nextInEncounter)
        .description("Next Participant",
            "Finish the current participants round and go to the next participant")
        .invisible();
    conditionInEncounter = Wrapper.<ImageView>wrap(view, R.id.encounter_condition)
        .onClick(this::conditionInEncounter)
        .description("Set a Condition",
            "Set a special condition on one ore multiple characters.")
        .invisible();
    addMonsterInEncounter = Wrapper.<ImageView>wrap(view, R.id.encounter_add_monster)
        .onClick(this::addMonsterInEncounter)
        .description("Add Monster", "Add a monster to the running encounter.")
        .invisible();
    delayInEncounter = Wrapper.<ImageView>wrap(view, R.id.encounter_delay)
        .onClick(this::delayInEncounter)
        .description("Delay", "Delay the current creatures turn.")
        .invisible();
    xp = Wrapper.<ImageView>wrap(view, R.id.xp)
        .onClick(this::awardXP)
        .description("Award XP", "Award experience points to your characters.")
        .invisible();

    return view;
  }

  public void showCampaign(Campaign campaign) {
    if (this.campaign != null) {
      this.campaign.unobserve(this);
    }
    this.campaign = campaign;
    this.campaign.observe(this, this::update);
    characters().addPlayers(campaign);
    monsters().addCampaign(campaign.getId());
    party.show(campaign);
    encounter.show(campaign);
    startEncounter.visible(campaign.amDM());

    if (campaign.amDM()) {
      title.setAction(this::edit);
      edit.onClick(this::edit).visible();
      calendar.onClick(this::editDate).visible();
      invite.onClick(this::invite).visible();
      date.onClick(this::editDate);
    } else {
      title.removeAction();
      edit.removeClick().gone();
      calendar.removeClick().gone();
      invite.removeClick().gone();
      date.removeClick();
    }

    xp.visible(campaign.amDM());

    update(campaign);
  }

  public boolean shows(String campaignId) {
    return this.campaign.getId().equals(campaignId);
  }

  @CallSuper
  protected void update(Campaign campaign) {
    title.update(campaign);
    title.update(images());
    date.text(campaign.getCalendar().format(campaign.getDate()));
    delete.visible(canDeleteCampaign());

    TransitionManager.beginDelayedTransition((ViewGroup) getView());
    if (campaign.getEncounter().inBattle()) {
      party.hide();
      encounter.show();
      encounter.update(campaigns());
      startEncounter.disabled().tint(R.color.actionDisabled);
      setLayoutWidth(encounterActions, LinearLayout.LayoutParams.WRAP_CONTENT);
      endEncounter.visible(campaign.amDM());
      nextInEncounter.visible(campaign.amDM());
      conditionInEncounter.visible(campaign.amDM() || campaign.getEncounter().amCurrentPlayer());
      addMonsterInEncounter.visible(campaign.amDM());
      delayInEncounter.visible(campaign.amDM() || campaign.getEncounter().amCurrentPlayer());
    } else {
      party.show();
      encounter.hide();
      startEncounter.enabled().tint(R.color.action);
      setLayoutWidth(encounterActions, 0);
    }
  }

  private void setLayoutWidth(LinearLayout layout, int width) {
    ViewGroup.LayoutParams params = layout.getLayoutParams();
    params.width = width;
    layout.setLayoutParams(params);
  }

  protected void update(Characters characters) {
  }

  protected void deleteCampaign() {
    ConfirmationPrompt.create(getContext())
        .title(getResources().getString(R.string.campaign_delete_title))
        .message(getResources().getString(R.string.campaign_delete_message_remote))
        .yes(this::deleteCampaignOk)
        .show();
  }

  protected void deleteCampaignOk() {
    campaigns().delete(campaign);
    Toast.makeText(getActivity(), getString(R.string.campaign_deleted), Toast.LENGTH_SHORT).show();
    show(Type.campaigns);
  }

  protected boolean canDeleteCampaign() {
    return campaign.amDM();
  }

  private void edit() {
    EditCampaignDialog.newInstance(campaign.getId()).display();
  }

  private void editDate() {
    DateDialog.newInstance(campaign.getId()).display();
  }

  private void invite() {
    InviteDialog.newInstance(campaign.getId()).display();
  }

  @Override
  public boolean goBack() {
    CompanionFragments.get().show(Type.campaigns, Optional.of(title));
    return true;
  }

  private void editImage() {
    Intent intent = new Intent();
    // Show only images, no videos or anything else
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    // Always show the chooser (if there are multiple options available)
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
  }

  private void startEncounter() {
    if (!campaign.amDM() || characters().getCampaignCharacters(campaign.getId()).isEmpty()) {
      Status.error("You have to be DM of a campaign with characters to start an startEncounter.");
      return;
    }

    campaign.getEncounter().setup();
  }

  private void endEncounter() {
    campaign.getEncounter().end();
  }

  private void nextInEncounter() {
    campaign.getEncounter().creatureDone();
  }

  private void conditionInEncounter() {
    if (campaign.getEncounter().amCurrentPlayer()) {
      TimedConditionDialog.newInstance(campaign.getEncounter().getCurrentCreatureId(),
          campaign.getEncounter().getTurn()).display();
    } else if (campaign.amDM()) {
      TimedConditionDialog.newInstance(campaign.getId(),
          campaign.getEncounter().getTurn()).display();
    }
  }

  private void addMonsterInEncounter() {
    if (campaign.amDM()) {
      MonsterInitiativeDialog.newInstance(campaign.getId(), -1).display();
    }
  }

  private void delayInEncounter() {
    campaign.getEncounter().creatureWait();
  }

  private void awardXP() {
    XPDialog.newInstance(campaign.getId()).display();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
        data != null && data.getData() != null) {
      try {
        Uri uri = data.getData();
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        images().set(campaign.getId(), bitmap);
      } catch (IOException e) {
        Status.toast("Cannot load image bitmap: " + e);
      }
    }
  }
}
