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
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;

import net.ixitxachitls.companion.ui.views.wrappers.AbstractWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;

/**
 * A labelled text view allowing multiple selections.
 */
public class LabelledMultiAutocompleteTextView
    extends LabelledAutocompleteTextView<LabelledMultiAutocompleteTextView, MultiAutoCompleteTextView> {

  public LabelledMultiAutocompleteTextView(Context context, AttributeSet attributes) {
    super(context, attributes);
  }

  @Override
  protected MultiAutoCompleteTextView createTextView() {
    text = EditTextWrapper.wrap(new MultiAutoCompleteTextView(getContext()));
    text.get().setBackground(null);
    text.padding(AbstractWrapper.Padding.BOTTOM, 0);
    text.padding(AbstractWrapper.Padding.TOP, 0);
    text.get().setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

    return text.get();
  }
}
