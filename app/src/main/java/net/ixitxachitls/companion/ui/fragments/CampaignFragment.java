/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.views.CampaignTitleView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/** A fragment displaying campaign information. */
public class CampaignFragment extends CompanionFragment {

  protected Campaign campaign = Campaigns.defaultCampaign;

  // UI elements.
  protected CampaignTitleView title;
  protected TextWrapper<TextView> date;
  protected Wrapper<FloatingActionButton> delete;
  protected Wrapper<FloatingActionButton> publish;
  protected Wrapper<FloatingActionButton> unpublish;
  protected Wrapper<FloatingActionButton> edit;
  protected Wrapper<FloatingActionButton> calendar;

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
    delete = Wrapper.<FloatingActionButton>wrap(view, R.id.delete).gone();
    publish = Wrapper.<FloatingActionButton>wrap(view, R.id.publish).gone();
    unpublish = Wrapper.<FloatingActionButton>wrap(view, R.id.unpublish).gone();
    edit = Wrapper.<FloatingActionButton>wrap(view, R.id.edit).gone();
    calendar = Wrapper.<FloatingActionButton>wrap(view, R.id.calendar).gone();
    date = TextWrapper.wrap(view, R.id.date);

    Campaigns.getCurrentCampaignId().observe(this, this::showCampaign);

    return view;
  }

  public void showCampaign(String campaignId) {
    Campaigns.getCampaign(campaign.getCampaignId()).removeObserver(this::update);
    Campaigns.getCampaign(campaignId).observe(this, this::update);
  }

  @CallSuper
  protected void update(Optional<Campaign> campaign) {
    if (campaign.isPresent()) {
      this.campaign = campaign.get();
      title.setCampaign(campaign.get());
      if (campaign.get().isDefault()) {
        date.text("");
      } else {
        date.text(campaign.get().getCalendar().format(campaign.get().getDate()));
      }
    }
  }

  @Override
  public boolean goBack() {
    CompanionFragments.get().show(Type.campaigns, Optional.of(title));
    return true;
  }
}
