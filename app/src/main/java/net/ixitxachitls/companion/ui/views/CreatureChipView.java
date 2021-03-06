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
import android.graphics.Bitmap;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.Monster;

import java.util.Optional;

/**
 * Chip view for a creature (monster).
 */
public class CreatureChipView extends ChipView {

  private final ConditionIconsView conditions;

  private int perLine;

  public CreatureChipView(Context context, Monster monster, int perLine) {
    this(context, monster, R.color.monsterDark, R.color.monsterLight,
        R.drawable.ic_person_black_48dp_inverted, perLine);
  }

  protected CreatureChipView(Context context, Creature creature, @ColorRes int chipColor,
                             @ColorRes int highlightColor, @DrawableRes int drawable, int perLine) {
    super(context, creature.getId(), creature.getName(), "init " + creature.getEncounterInitiative(),
        chipColor, highlightColor, drawable);

    this.conditions = new ConditionIconsView(context, VERTICAL, highlightColor, chipColor);
    this.perLine = perLine;

    icons.addView(conditions);
    CompanionApplication.get().conditions().readConditions(creature.getId());

    update(creature);
  }

  public String getCreatureId() {
    return getDataId();
  }

  @Override
  public void onMeasure(int width, int height) {
    super.onMeasure(width, height);

    if (perLine > 0) {
      int windowWidth = CompanionApplication.get().getCurrentActivity().getWindowManager()
          .getDefaultDisplay().getWidth();
      int chrome = getMeasuredWidth() - image.get().getMeasuredWidth();
      int newSize = windowWidth / perLine - chrome;
      if (image.get().getMeasuredWidth() != newSize) {
        image.width(newSize);
        image.height(newSize);

        super.onMeasure(width, height);
      }
    }
  }

  public void update(Creature<?> creature) {
    Optional<Bitmap> bitmap = CompanionApplication.get().images().get(creature.getId(), 1);
    if (bitmap.isPresent()) {
      image.get().setImageBitmap(bitmap.get());
    }
    conditions.update(creature);
  }
}
