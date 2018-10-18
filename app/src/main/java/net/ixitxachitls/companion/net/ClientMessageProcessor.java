/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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

package net.ixitxachitls.companion.net;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.Item;
import net.ixitxachitls.companion.data.dynamics.XpAward;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

import java.util.Optional;

/**
 * Processor specifically for client side message handling.
 */
public class ClientMessageProcessor extends MessageProcessor {

  private final CompanionApplication application;

  public ClientMessageProcessor(CompanionContext companionContext, CompanionApplication application, CompanionMessenger
      messenger) {
    super(companionContext, messenger);
    this.application = application;
  }

  public void process(String senderId, String senderName, long messageId,
                      CompanionMessageData message) {
    process(senderId, senderName, context.me().getId(), messageId, message);
  }

  @Override
  protected void handleImage(String senderId, Image image) {
    Status.log("received image for " + image.getType() + " " + image.getId());
    image.save();
  }

  @Override
  protected void handleItemAdd(String senderId, long messageId, String characterId, Item item) {
    /*
    Optional<Character> character = context.characters().getCharacter(characterId).getValue();
    if (character.isPresent()) {
      Status.log("received item " + item + " for " + character.get());
      character.get().add(item);
    } else {
      Status.error("Cannot add item to unknown character " + characterId);
    }
    messenger.sendAckToServer(senderId, messageId);
    */
  }

  protected void handleCampaign(String senderId, String senderName, long id, Campaign campaign) {
    Optional<Campaign> oldCampaign =
        context.campaigns().get(campaign.getId());
    if (oldCampaign.isPresent()
        && context.encounters().get(oldCampaign.get().getId()).isEnded()
        //&& !campaign.getBattle().isEnded()
        && CompanionFragments.get().showsCampaign(campaign.getId())) {
      // Show a message about the newly started battle.
      ConfirmationPrompt.create(application.getCurrentActivity())
          .title("Battle started")
          .message("A battle has started in " + campaign.getName()
              + ". Do you want to go to the battle screen?")
          .yes(() -> gotoBattle(campaign));
    }

    // Storing will also add the campaign if it's changed.
    campaign.store();
    Status.log("received campaign " + campaign.getName());
    status("received campaign " + campaign.getName());
  }

  private void gotoBattle(Campaign campaign) {
    //CompanionFragments.get().showCampaign(campaign, Optional.empty());
  }

  @Override
  protected void handleCampaignDeletion(String senderId, long messageId, String campaignId) {
    //context.campaigns().getCampaign(campaignId).ifPresent(Campaign::delete);
    messenger.sendAckToServer(senderId, messageId);
  }

  @Override
  protected void handleWelcome(String remoteId, String remoteName) {
    status("received welcome from server " + remoteName);
    super.handleWelcome(remoteId, remoteName);
    Status.addServerConnection(remoteId, remoteName);
  }

  @Override
  protected void handleXpAward(String receiverId, String senderId, long messageId, XpAward award) {
    /*
    Optional<Character> character =
        context.characters().getCharacter(award.getCharacterId()).getValue();
    if (character.isPresent()) {
      new ConfirmationPrompt(application.getCurrentActivity())
          .title("XP Award")
          .message("Congratulation!\n"
              + "Your DM has granted " + character.get().getName() + " " + award.getXp() + " XP!")
          .yes(() -> addXpAward(senderId, messageId, character.get().getCharacterId(),
              award.getXp()))
          .noNo()
          .show();
    }
    */
  }

  @Override
  protected void handleAck(String senderId, long messageId) {
    messenger.ackClient(senderId, messageId);
  }

  private void addXpAward(String senderId, long messageId, String characterId, int xp) {
    /*
    Optional<LocalCharacter> character =
        Character.asLocal(context.characters().getCharacter(characterId).getValue());
    if (character.isPresent()) {
      character.get().addXp(xp);
      messenger.sendAckToServer(senderId, messageId);
    }
     */
  }
}
