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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * A small chip
 */
public class ChipView extends LinearLayout {

  private static final int PADDING_SELECT = 5;

  private final String dataId;
  private final int chipColor;
  private final int backgroundColor;

  protected final Wrapper<RelativeLayout> container;
  protected final TextWrapper<TextView> name;
  protected final TextWrapper<TextView> subtitle;
  protected final RoundImageView image;
  private boolean disabled = false;

  public ChipView(Context context, String dataId, String name, String subtitle,
                  @ColorRes int chipColor, @ColorRes int backgroundColor) {
    super(context);
    this.dataId = dataId;
    this.chipColor = chipColor;
    this.backgroundColor = backgroundColor;

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_chip, this, false);

    this.container = Wrapper.wrap(view, R.id.container);
    container.backgroundColor(backgroundColor);
    this.name = TextWrapper.wrap(view, R.id.name);
    this.name.text(name).backgroundColor(chipColor).noWrap();

    this.subtitle = TextWrapper.wrap(view, R.id.subtitle);
    this.subtitle.noWrap().backgroundColor(chipColor).noWrap();
    setSubtitle(subtitle);

    this.image = (RoundImageView) view.findViewById(R.id.image);

    Drawable drawable = getResources().getDrawable(R.drawable.ic_person_black_48dp_inverted, null);
    drawable.setTint(getResources().getColor(chipColor, null));
    image.setImageDrawable(drawable);

    addView(view);
  }

  public void disabled() {
    disabled = true;
    name.textColor(R.color.disabled);
    subtitle.textColor(R.color.disabled);
  }

  public void setBackground(@ColorRes int color) {
    container.backgroundColor(color);
  }

  public void setSubtitle(String text) {
    subtitle.text(text);
    if (text.isEmpty()) {
      this.subtitle.gone();
    } else {
      this.subtitle.visible();
    }
  }

  public void select() {
    name.backgroundColor(backgroundColor);
    subtitle.backgroundColor(backgroundColor);
  }

  public void unselect() {
    name.backgroundColor(chipColor);
    subtitle.backgroundColor(chipColor);
  }

  public String getDataId() {
    return dataId;
  }
}