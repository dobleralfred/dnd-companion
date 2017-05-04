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
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.ui.Setup;

import java.util.Random;

/**
 * A view to select a dice result or roll randomly.
 */
public class DiceView extends LinearLayout {

  private static final Random RANDOM = new Random();

  private int modifier;
  private int dice;
  private SelectAction action;
  private final DiceAdapter adapter = new DiceAdapter();

  // UI elements.
  private TextView modifierView;
  private GridView grid;
  private Button random;

  public DiceView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  private void init(@Nullable AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.DiceView );

    View view = LayoutInflater.from(getContext())
        .inflate(R.layout.view_dice, null, false);
    Setup.textView(view, R.id.label).setText(array.getString(R.styleable.DiceView_modifier_label));
    modifierView = (TextView) Setup.textView(view, R.id.modifier);
    random = (Button) Setup.button(view, R.id.random, this::selectRandom);

    grid = (GridView) view.findViewById(R.id.numbers);
    grid.setAdapter(adapter);
    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        select(position + 1);
      }
    });

    addView(view);
  }

  private void selectRandom() {
    select(RANDOM.nextInt(dice) + 1);
  }

  private void select(int number) {
    if (action != null) {
      action.select(number + modifier);
    }
  }

  public void setSelectAction(SelectAction action) {
    this.action = action;
  }

  public void setModifier(int modifier) {
    this.modifier = modifier;
    this.modifierView.setText(modifier >= 0 ? "+" + modifier : String.valueOf(modifier));
  }

  public void setDice(int dice) {
    this.dice = dice;

    grid.setAdapter(adapter);
  }

  private class DiceAdapter extends BaseAdapter {
    @Override
    public int getCount() {
      return dice;
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView text;
      if (convertView == null) {
        text = new TextView(parent.getContext());
        text.setTextSize(24);
        text.setPadding(20, 40, 20, 40);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setBackgroundColor(getResources().getColor(R.color.cell, null));
      } else {
        text = (TextView) convertView;
      }

      text.setText(String.valueOf(position + 1));

      return text;
    }
  }

  @FunctionalInterface
  public interface SelectAction {
    public void select(int selection);
  }
}