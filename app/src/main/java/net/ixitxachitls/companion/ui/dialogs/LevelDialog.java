/*
 * Copyright (c) 2017-2019 Peter Balsiger
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
import android.view.View;
import android.widget.Button;

import com.google.common.collect.Lists;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Feat;
import net.ixitxachitls.companion.data.documents.Level;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.templates.FeatTemplate;
import net.ixitxachitls.companion.data.templates.LevelTemplate;
import net.ixitxachitls.companion.rules.Levels;
import net.ixitxachitls.companion.rules.Spells;
import net.ixitxachitls.companion.ui.fragments.ListSelectDialog;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * A dialog to setup a single level.
 */
public class LevelDialog extends Dialog<LevelDialog, Level> {

  private static final String ARG_ID = "id";
  private static final String ARG_LEVEL = "level";

  private Character character;
  private int characterLevel = 0;
  private int classLevel = 0;
  private Level level;
  private Optional<Feat> feat = Optional.empty();
  private Optional<Feat> racialFeat = Optional.empty();
  private Optional<Feat> classFeat = Optional.empty();

  // UI.
  private LabelledTextView className;
  private LabelledEditTextView hp;
  private LabelledTextView abilityIncrease;
  private LabelledTextView featView;
  private LabelledTextView racialFeatView;
  private LabelledTextView classFeatView;

  @Override
  protected Level getValue() {
    if (className == null || hp == null || abilityIncrease == null) {
      return new Level();
    }

    return new Level(Templates.get().getOrCreateLevel(className.getText()),
        Integer.parseInt(hp.getText()),
        abilityIncrease.getText().isEmpty()
            ? Optional.empty() : Optional.of(Ability.fromName(abilityIncrease.getText())),
        feat, racialFeat, classFeat);
  }

  @Override
  protected void createContent(View view) {
    if (!characters().get(getArguments().getString(ARG_ID)).isPresent()) {
      Status.error("Cannot find character to show level");
      super.save();
    }

    character = characters().get(getArguments().getString(ARG_ID)).get();
    characterLevel = getArguments().getInt(ARG_LEVEL);

    if (character.getLevel(characterLevel).isPresent()) {
      level = character.getLevel(characterLevel).get();
    } else {
      level = new Level();
    }

    Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);

    className = view.findViewById(R.id.class_name);
    className.onClick(this::selectClass);
    hp = view.findViewById(R.id.hp);
    hp.validate(new EditTextWrapper.RangeValidator(1, level.getMaxHp()));
    abilityIncrease = view.findViewById(R.id.ability_increase);
    if (Levels.allowsAbilityIncrease(characterLevel) || level.hasAbilityIncrease()) {
      abilityIncrease.onClick(this::selectAbility);
    } else {
      abilityIncrease.disabled();
    }
    featView = view.findViewById(R.id.feat);
    racialFeatView = view.findViewById(R.id.racial_feat);
    classFeatView = view.findViewById(R.id.class_feat);
    if (Levels.allowsFeat(characterLevel)) {
      featView.onClick(this::selectFeat);
    } else {
      featView.disabled();
    }
    if (character.getRace().isPresent()
        && character.getRace().get().hasBonusFeat(characterLevel)) {
      racialFeatView.onClick(this::selectRacialFeat);
    } else {
      racialFeatView.disabled();
    }

    feat = level.getFeat();
    classFeat = level.getClassFeat();
    racialFeat = level.getRacialFeat();
    className.text(level.getTemplate().getName());
    hp.text(String.valueOf(level.getHp()));
    abilityIncrease.text(level.getIncreasedAbility().isPresent()
        ? level.getIncreasedAbility().get().getName() : "");
    featView.text(feat.isPresent() ? feat.get().getTitle() : "");
    racialFeatView.text(racialFeat.isPresent() ? racialFeat.get().getTitle(): "");
    classFeatView.text(classFeat.isPresent() ? classFeat.get().getTitle() : "");

    refresh();
  }

  @Override
  public void save() {
    character.setLevel(characterLevel, getValue());

    super.save();
  }

  private List<FeatTemplate> bonusFeats() {
    Optional<LevelTemplate> level = Templates.get().getLevelTemplates().get(className.getText());
    if (level.isPresent()) {
      return level.get().collectBonusFeats(classLevel);
    } else {
      return Collections.emptyList();
    }
  }

  private void editAbility(List<String> ability) {
    abilityIncrease.text(ability.get(0));
  }

  private void editClass(List<String> className) {
    this.className.text(className.get(0));
    refresh(); // The class level could have changed.
  }

  private void editClassFeat(List<String> featName) {
    classFeat = Optional.of(new Feat(featName.get(0), "Level " + characterLevel));
    this.classFeatView.text(classFeat.get().getTitle());

    editQualifier(classFeat, this::editClassFeatQualifier);
  }

  private void editClassFeatQualifier(List<String> qualifiers) {
    classFeat = Optional.of(classFeat.get().withQualifiers(qualifiers));
    this.classFeatView.text(classFeat.get().getTitle());
  }

  private void editFeat(List<String> featName) {
    if (!feat.isPresent() || !feat.get().getName().equals(featName.get(0))) {
      feat = Optional.of(new Feat(featName.get(0), "Level " + characterLevel));
    }
    this.featView.text(feat.get().getTitle());

    editQualifier(feat, this::editFeatQualifier);
  }

  private void editFeatQualifier(List<String> qualifiers) {
    feat = Optional.of(feat.get().withQualifiers(qualifiers));
    this.featView.text(feat.get().getTitle());
  }

  private void editQualifier(Optional<Feat> feat, ListSelectDialog.SelectAction action) {
    if (!feat.isPresent()) {
      return;
    }

    switch (feat.get().getTemplate().getRequiredQualifier()) {
      case none:
        break;

      case weapon:
        ListSelectDialog.newStringInstance(R.string.feat_qualifier_weapon,
            feat.get().getQualifiers(), Templates.get().getItemTemplates().weapons(),
            R.color.character)
            .setSelectListener(action)
            .display();
        break;

      case school:
        ListSelectDialog.newStringInstance(R.string.feat_qualifier_school,
            new ArrayList<>(feat.get().getQualifiers()), Spells.schools(), R.color.character)
            .setSelectListener(action)
            .display();
        break;

      case skill:
        ListSelectDialog.newStringInstance(R.string.feat_qualifier_skill,
            new ArrayList<>(feat.get().getQualifiers()),
            Templates.get().getSkillTemplates().getNames(),
            R.color.character)
            .setSelectListener(action)
            .display();
        break;

      case spells:
        ListSelectDialog.newStringInstance(R.string.feat_qualifier_spells,
            new ArrayList<>(feat.get().getQualifiers()), 3,
            Templates.get().getSpellTemplates().getNames(),
            R.color.character)
            .setSelectListener(action)
            .display();
        break;
    }
  }

  private void editRacialFeat(List<String> featName) {
    racialFeat = Optional.of(new Feat(featName.get(0), "Level " + characterLevel));
    this.racialFeatView.text(racialFeat.get().getTitle());

    editQualifier(racialFeat, this::editRacialFeatQualifier);
  }

  private void editRacialFeatQualifier(List<String> qualifiers) {
    racialFeat = Optional.of(racialFeat.get().withQualifiers(qualifiers));
    this.racialFeatView.text(racialFeat.get().getTitle());
  }

  private void refresh() {
    classLevel = character.getClassLevel(className.getText(), characterLevel);
    // If we change the class in this level, then the class level is actually one level higher.
    if (!level.getTemplate().getName().equals(className.getText())) {
      classLevel++;
    }

    Optional<LevelTemplate> level = Templates.get().getLevelTemplates().get(className.getText());
    if (level.isPresent() && level.get().hasBonusFeat(classLevel)) {
      classFeatView.enabled();
      classFeatView.onClick(this::selectClassFeat);
    } else {
      classFeatView.disabled();
    }
  }

  private void selectAbility() {
    ListSelectDialog.newStringInstance(
        R.string.character_select_ability,
        Lists.newArrayList(abilityIncrease.getText()),
        Ability.names(), R.color.character)
        .setSelectListener(this::editAbility)
        .display();
  }

  private void selectClass() {
    ListSelectDialog.newStringInstance(
        R.string.character_select_class, Lists.newArrayList(className.getText()),
        Templates.get().getLevelTemplates().getValues().stream()
            .filter(LevelTemplate::isFromPHB)
            .map(LevelTemplate::getName)
            .collect(Collectors.toList()), R.color.character)
        .setSelectListener(this::editClass)
        .display();
  }

  private void selectClassFeat() {
    ListSelectDialog.newStringInstance(
        R.string.character_select_feat,
        classFeat.isPresent()
            ? Lists.newArrayList(classFeat.get().getName()) : Collections.emptyList(),
        bonusFeats().stream().map(FeatTemplate::getName).collect(Collectors.toList()),
        R.color.character)
        .setSelectListener(this::editClassFeat)
        .display();
  }

  private void selectFeat() {
    ListSelectDialog.newStringInstance(
        R.string.character_select_feat,
        feat.isPresent() ? Lists.newArrayList(feat.get().getName()) : Collections.emptyList(),
        Templates.get().getFeatTemplates().getValues().stream()
            .filter(FeatTemplate::isFromPHB)
            .map(FeatTemplate::getName)
            .collect(Collectors.toList()), R.color.character)
        .setSelectListener(this::editFeat)
        .display();
  }

  private void selectRacialFeat() {
    ListSelectDialog.newStringInstance(
        R.string.character_select_feat,
        racialFeat.isPresent()
            ? Lists.newArrayList(racialFeat.get().getName()) : Collections.emptyList(),
        Templates.get().getFeatTemplates().getValues().stream()
            .filter(FeatTemplate::isFromPHB)
            .map(FeatTemplate::getName)
            .collect(Collectors.toList()), R.color.character)
        .setSelectListener(this::editRacialFeat)
        .display();
  }

  protected static Bundle arguments(@LayoutRes int layoutId, String title,
                                    @ColorRes int colorId, String characterId, int level) {
    Bundle arguments = Dialog.arguments(layoutId, title, colorId);
    arguments.putString(ARG_ID, characterId);
    arguments.putInt(ARG_LEVEL, level);
    return arguments;
  }

  public static LevelDialog newInstance(String characterId, int level) {
    LevelDialog dialog = new LevelDialog();
    dialog.setArguments(arguments(R.layout.dialog_level, "Level " + level,
        R.color.character, characterId, level));
    return dialog;
  }
}
