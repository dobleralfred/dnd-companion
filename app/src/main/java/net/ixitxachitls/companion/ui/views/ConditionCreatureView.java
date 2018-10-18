/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.values.Duration;
import net.ixitxachitls.companion.data.values.Encounter;
import net.ixitxachitls.companion.data.values.TargetedTimedCondition;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.util.List;

/**
 * View for the conditions of a single ceature.
 */
public class ConditionCreatureView extends LinearLayout {

  private final LinearLayout container;
  private final Encounter encounter;

  private boolean hasConditions = false;

  public ConditionCreatureView(Context context, String name, Encounter encounter) {
    super(context);

    this.encounter = encounter;

    View view =
        LayoutInflater.from(getContext()).inflate(R.layout.view_condition_creature, this, false);

    TextWrapper.wrap(view, R.id.creature_name).text(name);
    container = view.findViewById(R.id.creature_conditions);

    addView(view);
  }

  public void addConditions(Creature<?> creature, boolean isDM) {
    for (TargetedTimedCondition condition : creature.getInitiatedConditions()) {
      addCondition(creature.getName(), creature.getId(), condition.getTargetIds(),
          condition.getTimedCondition(), false, isDM);
    }

    for (TimedCondition condition : creature.getAffectedConditions()) {
      addCondition(CompanionApplication.get(getContext()).creatures()
              .nameFor(condition.getSourceId()), condition.getSourceId(),
          ImmutableList.<String>of(creature.getId()), condition, true, isDM);
    }
  }

  private void addCondition(String sourceName, String sourceId, List<String> targetIds,
                            TimedCondition condition, boolean affected, boolean isDM) {
    Duration remaining;
    if (condition.hasEndDate()) {
      remaining = encounter.getCampaign().getCalendar().dateDifference(encounter.getCampaign().getDate(),
          condition.getEndDate());
    } else if (condition.isPermanent()) {
      remaining = Duration.PERMANENT;
    } else {
      if (!condition.active(encounter)) {
        return;
      }

      remaining = Duration.rounds(condition.getEndRound() - encounter.getTurn());
    }

    hasConditions = true;
    if (affected) {
      container.addView(new AffectedConditionLineView(getContext(), condition,
          targetIds.size() == 1 && sourceId.equals(targetIds.get(0)) ? "" : sourceName,
          sourceId, targetIds, remaining, isDM));
    } else {
      container.addView(new InitiatedConditionLineView(getContext(), condition, sourceName,
          sourceId, targetIds, remaining, isDM));
    }
  }

  public boolean hasConditions() {
    return hasConditions;
  }

  public void update(Campaign campaign) {
    for (int i = 0; i < container.getChildCount(); i++) {
      View view = container.getChildAt(i);
      if (view instanceof ConditionLineView) {
        ((ConditionLineView) view).update(campaign);
      }
    }
  }
}
