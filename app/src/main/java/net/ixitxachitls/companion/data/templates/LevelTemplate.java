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

package net.ixitxachitls.companion.data.templates;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.rules.Products;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * All the base information about a level.
 */
public class LevelTemplate extends StoredTemplate<Template.LevelTemplateProto> {
  public static final String TYPE = "level";

  private final net.ixitxachitls.companion.proto.Template.LevelTemplateProto proto;
  private final int maxHp;

  public LevelTemplate() {
    this(defaultProto(), "", 0);
  }

  public LevelTemplate(Template.LevelTemplateProto proto, String name, int maxHp) {
    super(name);
    this.proto = proto;
    this.maxHp = maxHp;
  }

  public Collection<? extends FeatTemplate> getAutomaticFeats() {
    List<FeatTemplate> feats = new ArrayList<>();

    for (String name : proto.getAutomaticFeatList()) {
      Optional<FeatTemplate> feat = Templates.get().getFeatTemplates().get(name);
      if (feat.isPresent()) {
        feats.add(feat.get());
      } else {
        Status.error("Cannot get feat for '" + name + "'!");
      }
    }

    return feats;
  }

  public int getMaxHp() {
    return maxHp;
  }

  public boolean isFromPHB() {
    return Products.isFromPHB(proto.getTemplate());
  }

  public List<FeatTemplate> collectBonusFeats(int level) {
    for (Template.LeveledTemplateProto bonus : proto.getBonusFeatList()) {
      if (bonus.getLevel() == level) {
        return Templates.get().getFeatTemplates().getValues().stream().filter(f -> {
          // Match on the name.
          if (bonus.getTemplate().getParameters().getFeatNameList().contains(f.getName())) {
            return true;
          }

          // Match on the type.
          if (bonus.getTemplate().getParameters().getFeatTypeList().contains(f.getType())) {
            return true;
          }

          return false;
        }).collect(Collectors.toList());
      }
    }

    return Collections.emptyList();
  }

  public boolean hasBonusFeat(int level) {
    for (Template.LeveledTemplateProto bonus : proto.getBonusFeatList()) {
      if (bonus.getLevel() == level) {
        return true;
      }
    }

    return false;
  }

  public static Template.LevelTemplateProto defaultProto() {
    return Template.LevelTemplateProto.getDefaultInstance();
  }

  public static LevelTemplate fromProto(Template.LevelTemplateProto proto) {
    LevelTemplate level = new LevelTemplate(proto, proto.getTemplate().getName(),
        proto.getHitDice().getDice());
    return level;
  }
}
