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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Item;
import net.ixitxachitls.companion.data.statics.ItemTemplate;
import net.ixitxachitls.companion.data.values.Duration;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.ui.views.LabelledAutocompleteTextView;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledMultiAutocompleteTextView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Misc;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A dialog to add a new baseTemplate.
 */
public class EditItemDialog extends Dialog {

  private static final String ARG_CREATURE_ID = "creature_id";
  private static final String ARG_ITEM_ID = "item_id";

  private Optional<? extends BaseCreature> creature = Optional.empty();
  private Optional<Item> item;
  private Optional<ItemTemplate> baseTemplate = Optional.empty();
  private List<ItemTemplate> templates = Collections.emptyList();

  private LabelledAutocompleteTextView itemSelection;
  private LabelledMultiAutocompleteTextView templatesSelection;
  private LabelledEditTextView name;
  private LabelledEditTextView value;
  private LabelledEditTextView weight;
  private LabelledEditTextView hp;
  private LabelledEditTextView appearance;
  private LabelledEditTextView multiple;
  private LabelledEditTextView multiuse;
  private Wrapper<Button> add;
  private Wrapper<Button> save;

  public EditItemDialog() {
    this.item = Optional.empty();
  }

  public static EditItemDialog newInstance(String creatureId) {
    EditItemDialog dialog = new EditItemDialog();
    dialog.setArguments(arguments(creatureId, "", R.layout.dialog_edit_item,
        R.string.character_add_item, R.color.item));
    return dialog;
  }

  public static EditItemDialog newInstance(String creatureId, String itemId) {
    EditItemDialog dialog = new EditItemDialog();
    dialog.setArguments(arguments(creatureId, itemId, R.layout.dialog_edit_item, R.string
            .character_edit_item,
        R.color.item));

    return dialog;
  }

  protected static Bundle arguments(String creatureId, String itemId, @LayoutRes int layoutId,
                                    @StringRes int titleId, @ColorRes int colorId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_CREATURE_ID, creatureId);
    arguments.putString(ARG_ITEM_ID, itemId);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);

    if (getArguments() != null) {
      String creatureId = getArguments().getString(ARG_CREATURE_ID);
      creature =
          CompanionApplication.get(getContext()).creatures().getCreatureOrCharacter(creatureId);
      String itemId = getArguments().getString(ARG_ITEM_ID);
      if (creature.isPresent() && !itemId.isEmpty()) {
        item = creature.get().getItem(itemId);
      }
    }
  }

  @Override
  protected void createContent(View view) {
    itemSelection = view.findViewById(R.id.item);
    itemSelection.setAdapter(new ArrayAdapter<String>(getContext(),
        R.layout.list_item_select,
        Entries.get().getItems().realItems()));
    itemSelection.onChange(this::selectItem);

    templatesSelection = view.findViewById(R.id.templates);
    templatesSelection.setAdapter(new ArrayAdapter<String>(getContext(),
        R.layout.list_item_select,
        Entries.get().getItems().templates()));
    templatesSelection.onChange(this::selectItem);

    TextWrapper.wrap(view, R.id.id).text(item.isPresent() ? item.get().getId() : "")
        .visible(Misc.onEmulator());
    name = view.findViewById(R.id.name);
    value = view.findViewById(R.id.value);
    value.validate(Money::validate);
    weight = view.findViewById(R.id.weight);
    weight.disabled();
    hp = view.findViewById(R.id.hp);
    appearance = view.findViewById(R.id.appearance);
    multiple = view.findViewById(R.id.multiple);
    multiuse = view.findViewById(R.id.multiuse);
    add = Wrapper.<Button>wrap(view, R.id.add).onClick(this::add).disabled();
    save = Wrapper.<Button>wrap(view, R.id.save).onClick(this::store);
    if (item.isPresent()) {
      add.gone();
      update(item.get());
    } else {
      save.gone();
      name.gone();
    }
  }

  private void update(Item item) {
    baseTemplate = item.getBaseTemplate();
    if (baseTemplate.isPresent()) {
      templates = item.getTemplates();
      update(templates);
      itemSelection.text(baseTemplate.get().getName());
      templatesSelection.text(Strings.COMMA_JOINER.join(templates.stream().skip(1)
          .map(ItemTemplate::getName)
          .collect(Collectors.toList())));
    }
  }

  private void update(List<ItemTemplate> templates) {
    name.text(Item.name(templates));
    value.text(Item.value(templates).toString());
    weight.text(Item.weight(templates).toString());
    hp.text(String.valueOf(Item.hp(templates)));
    appearance.text(Item.appearance(templates));
  }

  private void selectItem() {
    baseTemplate = Entries.get().getItems().get(itemSelection.getText());

    if (baseTemplate.isPresent()) {
      templates = new ArrayList<>();
      templates.add(baseTemplate.get());
      for (String name : templatesSelection.getText().split("\\s*,\\s*")) {
        Optional<ItemTemplate> template = Entries.get().getItems().get(name);
        if (template.isPresent()) {
          templates.add(template.get());
        }
      }
      update(templates);
      add.enabled();
    } else {
      name.text("");
      value.text("");
      weight.text("");
      hp.text("");
      appearance.text("");
      add.disabled();
    }

    multiple.text("");
    multiuse.text("");
  }

  private void add() {
    if (!baseTemplate.isPresent()) {
      return;
    }

    // Create the corresponding item.
    Optional<Money> itemValue = Money.parse(value.getText());
    if (!itemValue.isPresent()) {
      Status.error("Cannot parse item value!");
    } else if (!creature.isPresent()) {
      Status.error("No creature to add item to!");
    } else {
      Item item = new Item(
          Item.generateId(((CompanionApplication) getContext().getApplicationContext()).context()),
          baseTemplate.get().getName(),
          templates, parseHp(),
          itemValue.get(), appearance.getText(), "", "", "", parseMultiple(), parseMultiuse(),
          Duration.ZERO, false, Collections.emptyList());
      creature.get().add(item);
      save();
    }
  }

  private int parseHp() {
    if (hp.isEmpty()) {
      return 0;
    }

    return Integer.parseInt(hp.getText());
  }

  private int parseMultiple() {
    if (multiple.isEmpty()) {
      return 1;
    }

    return Integer.parseInt(multiple.getText());
  }

  private int parseMultiuse() {
    if (multiuse.isEmpty()) {
      return 1;
    }

    return Integer.parseInt(multiuse.getText());
  }

  private void store() {
    if (!baseTemplate.isPresent() || !item.isPresent()) {
      return;
    }

    Optional<Money> itemValue = Money.parse(value.getText());
    if (!itemValue.isPresent()) {
      Status.error("Cannot parse item value!");
    } else if (!creature.isPresent()) {
      Status.error("No creature to edit item in!");
    } else {
      item.get().setValue(itemValue.get());
      item.get().setName(baseTemplate.get().getName());
      item.get().setTemplates(templates);
      item.get().setHp(Integer.parseInt(hp.getText()));
      item.get().setAppearance(appearance.getText());
      item.get().setMultiple(parseMultiple());
      item.get().setMultiuse(parseMultiuse());
      creature.get().updated(item.get());
      save();
    }
  }
}