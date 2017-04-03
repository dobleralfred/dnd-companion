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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Campaigns;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.ui.Setup;

/**
 * Fragment for editing a campaign
 */
public class EditCampaignFragment extends EditFragment {

  private static final String ARG_ID = "id";

  @FunctionalInterface
  public interface SaveAction {
    public void save(Campaign campaign);
  }

  private Optional<SaveAction> saveAction = Optional.absent();

  // The following values are only valid after onCreate().
  private Campaign campaign;
  private EditText name;
  private TextView world;
  private Button save;

  public EditCampaignFragment() {}

  public static EditCampaignFragment newInstance() {
    return newInstance("");
  }

  public static EditCampaignFragment newInstance(String id) {
    EditCampaignFragment fragment = new EditCampaignFragment();
    fragment.setArguments(arguments(R.layout.fragment_edit_campaign,
        id.isEmpty() ? R.string.campaign_title_add : R.string.campaign_title_edit,
        R.color.campaign, id));
    return fragment;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId) {
    Bundle arguments = EditFragment.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, campaignId);
    return arguments;
  }

  public void setSaveListener(@Nullable SaveAction save) {
    this.saveAction = Optional.of(save);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      String id = getArguments().getString(ARG_ID);
      campaign = Campaigns.get().getCampaign(id);
    } else {
      campaign = Campaign.createNew();
    }
  }

  @Override
  protected void createContent(View view) {
    name = Setup.editText(view, R.id.edit_name, campaign.getName(), R.string.campaign_edit_name,
        R.color.campaign, null, this::update);
    world = Setup.textView(view, R.id.world, this::selectWorld);
    if(!campaign.getWorld().isEmpty()) {
      world.setText(campaign.getWorld());
    }
    save = Setup.button(view, R.id.save, this::save);

    if (campaign.isDefined()) {
      view.findViewById(R.id.campaign_edit_intro).setVisibility(View.GONE);
    }

    update();
  }

  private void selectWorld() {
    ListSelectFragment fragment = ListSelectFragment.newInstance(
        R.string.campaign_select_world, campaign.getWorld(),
        Entries.get().getWorlds().getNames(), R.color.campaign);
    fragment.setSelectListener(this::editWorld);
    fragment.display(getFragmentManager());
  }

  private void editWorld(String value, int position) {
    campaign.setWorld(value);
    update();
  }

  protected void update() {
    if (name.getText().length() == 0 || campaign.getWorld().isEmpty()) {
      save.setVisibility(View.INVISIBLE);
    } else {
      save.setVisibility(View.VISIBLE);
    }

    if (!campaign.getWorld().isEmpty()) {
      world.setText(campaign.getWorld());
    }
  }

  protected void save() {
    campaign.setName(name.getText().toString());

    if (saveAction.isPresent()) {
      saveAction.get().save(campaign);
    }

    super.save();
  }
}
