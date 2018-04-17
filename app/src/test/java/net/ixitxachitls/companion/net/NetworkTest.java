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

package net.ixitxachitls.companion.net;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.support.multidex.MultiDexApplication;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.CompanionTest;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.FakeCompanionContext;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.data.values.TargetedTimedCondition;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.net.nsd.FakeNsdAccessor;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.storage.FakeAssetAccessor;
import net.ixitxachitls.companion.storage.FakeClient1DataBaseAccessor;
import net.ixitxachitls.companion.storage.FakeClient2DataBaseAccessor;
import net.ixitxachitls.companion.storage.FakeDataBaseAccessor;
import net.ixitxachitls.companion.storage.FakeServerDataBaseAccessor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;

/**
 * End 2 end test for the whole network stack.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = MultiDexApplication.class)
public class NetworkTest extends CompanionTest {

  // Make .setValue calls synchronous and allow them to be called in tests.
  @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  private final FakeNsdAccessor nsdAccessor = new FakeNsdAccessor();
  private final FakeAssetAccessor assetAccessor = new FakeAssetAccessor();

  private final FakeDataBaseAccessor serverDataBase = new FakeServerDataBaseAccessor();
  private FakeCompanionContext serverContext;

  private final FakeDataBaseAccessor client1DataBase = new FakeClient1DataBaseAccessor();
  private FakeCompanionContext client1Context;

  private final FakeDataBaseAccessor client2DataBase = new FakeClient2DataBaseAccessor();
  private FakeCompanionContext client2Context;

  private CompanionMessenger server;
  private CompanionMessenger client1;
  private CompanionMessenger client2;

  @Before
  public void setUp() {
    super.setUp();

    Entries.init(assetAccessor);
    serverContext = new FakeCompanionContext(serverDataBase, nsdAccessor);
    client1Context = new FakeCompanionContext(client1DataBase, nsdAccessor);
    client2Context = new FakeCompanionContext(client2DataBase, nsdAccessor);
    Images.load(serverContext, assetAccessor);

    server = serverContext.messenger();
    client1 = client1Context.messenger();
    client2 = client2Context.messenger();
  }

  @Test
  public void welcome() throws InterruptedException {

    server.start();
    // No welcome expected, but server should be started.
    assertTrue(server.getServer().isOnline());
    assertTrue(server.getServer().getSchedulersByRecpientId().values().isEmpty());

    client1.start();
    client2.start();

    processAllMessages(server, client1, client2);

    assertThat(server.getServer().connectedClientIds(),
        containsInAnyOrder("client1", "client2", "client3"));
    assertThat(server.getServer().getNsdServer().connectedIds(),
        containsInAnyOrder("client1", "client2"));
    assertEquals("Client 1", server.getServer().getNsdServer().getNameForId("client1"));
    assertEquals("Client 2", server.getServer().getNsdServer().getNameForId("client2"));
    assertThat(server.getClients().getSchedulersByServerId().keySet(),
        containsInAnyOrder("client1"));
    assertThat(server.getClients().getSchedulersByServerId().keySet(),
        containsInAnyOrder("client1"));

    assertServerSent(server, "server", Entry.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN,
        "campaign-server-1", "client1", "client2");
    assertServerSent(server, "server", Entry.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN,
        "campaign-server-3", "client1", "client2");
    assertServerSent(server, "server", Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-server-1", "client1", "client2");
    assertServerSent(server, "server", Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-client1-3", "client2");
    assertServerSent(server, "server", Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-client2-5", "client1");
    assertServerSent(server, "server", Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-client3-2", "client1", "client2");
    assertServerSent(server, "server", Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-1", "client1", "client2");
    assertServerSent(server, "server", Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-2", "client1");

    assertThat(client1.getClients().getClientsByServerId().keySet(),
        containsInAnyOrder("server"));
    assertThat(client1.getClients().getClientsByServerName().keySet(),
        containsInAnyOrder("Server"));
    assertClientSent(client1, "client1",  Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-client1-3", "server");

    assertThat(client2.getClients().getClientsByServerId().keySet(),
        containsInAnyOrder("server"));
    assertThat(client2.getClients().getClientsByServerName().keySet(),
        containsInAnyOrder("Server"));
    assertClientSent(client2, "client2",  Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-client2-5", "server");

    assertFinished(server, client1, client2);
  }

  @Test
  public void condition() throws InterruptedException {
    server.start();
    client1.start();
    client2.start();

    assertTrue(character(client1Context, "character-client1-3")
        .affectedConditions().isEmpty());
    assertTrue(character(serverContext, "character-server-1")
        .affectedConditions().isEmpty());
    assertTrue(character(client2Context, "character-client2-5")
        .affectedConditions().isEmpty());

    character(client1Context, "character-client1-3").addInitiatedCondition(
        new TargetedTimedCondition(new TimedCondition(Conditions.FLAT_FOOTED, "character-client1-3", 5),
            ImmutableList.of("character-client2-5", "character-server-1")));

    // Process messages and deliver to all clients.
    processAllMessages(server, client1, client2);

    assertThat(character(client1Context, "character-client1-3").affectedConditions(),
        containsInAnyOrder());
    assertThat(character(serverContext, "character-server-1").affectedConditions(),
        containsInAnyOrder(Conditions.FLAT_FOOTED.getName()));
    assertThat(character(client2Context, "character-client2-5").affectedConditions(),
        containsInAnyOrder(Conditions.FLAT_FOOTED.getName()));

    assertThat(character(serverContext, "character-client1-3").affectedConditions(),
        containsInAnyOrder());
    assertThat(character(client2Context, "character-client1-3").affectedConditions(),
        containsInAnyOrder());

    assertThat(character(client1Context, "character-server-1").affectedConditions(),
        containsInAnyOrder(Conditions.FLAT_FOOTED.getName()));
    assertThat(character(client2Context, "character-server-1").affectedConditions(),
        containsInAnyOrder(Conditions.FLAT_FOOTED.getName()));

    assertThat(character(serverContext, "character-client2-5").affectedConditions(),
        containsInAnyOrder(Conditions.FLAT_FOOTED.getName()));
    assertThat(character(client1Context, "character-client2-5").affectedConditions(),
        containsInAnyOrder(Conditions.FLAT_FOOTED.getName()));

    // Remove the condition again.
    character(client1Context, "character-client1-3")
        .removeInitiatedCondition(Conditions.FLAT_FOOTED.getName());

    assertThat(character(serverContext, "character-server-1").affectedConditions(),
        containsInAnyOrder(Conditions.FLAT_FOOTED.getName()));

    // Process messages and deliver to all clients.
    processAllMessages(server, client1, client2);

    assertTrue(character(client1Context, "character-client1-3").affectedConditions().isEmpty());
    assertTrue(character(serverContext, "character-server-1").affectedConditions().isEmpty());
    assertTrue(character(client2Context, "character-client2-5").affectedConditions().isEmpty());

    assertTrue(character(serverContext, "character-client1-3").affectedConditions().isEmpty());
    assertTrue(character(client2Context, "character-client1-3").affectedConditions().isEmpty());

    assertTrue(character(client1Context, "character-server-1").affectedConditions().isEmpty());
    assertTrue(character(client2Context, "character-server-1").affectedConditions().isEmpty());

    assertTrue(character(serverContext, "character-client2-5").affectedConditions().isEmpty());
    assertTrue(character(client1Context, "character-client2-5").affectedConditions().isEmpty());

    assertFinished(server, client1, client2);
  }
}
