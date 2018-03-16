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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * A text view with a label and description.
 */
public class LabelledTextView extends AbstractLabelledView {
  // UI elements.
  private TextWrapper<TextView> text;

  public LabelledTextView(Context context) {
    super(context, R.layout.view_labelled_text);
  }

  public LabelledTextView(Context context, AttributeSet attributes) {
    super(context, attributes, R.layout.view_labelled_text);
  }

  @Override
  protected void setup(View view, TypedArray array, TypedArray baseArray) {
    super.setup(view, array, baseArray);

    text = TextWrapper.wrap(view, R.id.text);
    text.text(array.getString(R.styleable.LabelledEditTextView_defaultText));
    text.textColorValue(array.getColor(R.styleable.LabelledEditTextView_textColor,
        getContext().getResources().getColor(R.color.colorPrimary, null)));
    View line = view.findViewById(R.id.line);
    line.setBackgroundColor(array.getColor(R.styleable.LabelledEditTextView_lineColor,
        getContext().getResources().getColor(R.color.colorPrimary, null)));
  }

  public String getText() {
    return text.getText();
  }

  public LabelledTextView text(String text) {
    this.text.text(text);

    return this;
  }

  public LabelledTextView onClick(Wrapper.Action action) {
    text.onClick(action);

    return this;
  }

  public LabelledTextView enabled(boolean enabled) {
    text.enabled(enabled);

    return this;
  }
}