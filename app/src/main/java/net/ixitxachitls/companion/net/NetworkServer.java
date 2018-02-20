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

package net.ixitxachitls.companion.net;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.net.raw.Transmitter;
import net.ixitxachitls.companion.proto.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.content.ContentValues.TAG;

/**
 * Server endpoint for network communication.
 */
public class NetworkServer implements Runnable {

  private @Nullable ServerSocket socket;
  private Thread thread;
  private Map<String, Transmitter> transmittersById = new ConcurrentHashMap<>();
  private Map<String, String> namesById = new ConcurrentHashMap<>();

  public NetworkServer() {
    this.thread = new Thread(this);
  }

  public boolean start() {
    try {
      socket = new ServerSocket(0);
      Status.log("starting companion server");
      thread.start();
    } catch (IOException e) {
      Log.e(TAG, "Error creating ServerSocket: ", e);
      e.printStackTrace();
      return false;
    }

    return true;
  }

  public void stop() {
    Status.log("stopping server...");
    for (Transmitter transmitter : transmittersById.values()) {
      transmitter.stop();
    }

    thread.interrupt();
    try {
      socket.close();
    } catch (IOException ioe) {
      Log.e("Server", "Error when closing server socket.");
    }
  }

  public int getPort() {
    return socket.getLocalPort();
  }

  public InetAddress getAddress() {
    return socket.getInetAddress();
  }

  public Collection<String> connectedIds() {
    return namesById.keySet();
  }

  public Collection<String> connectedNames() {
    return namesById.values();
  }

  @Override
  public void run() {
    Status.log("starting server thread");
    Log.d("Server", "ServerSocket Created, awaiting connection on "
        + socket.getInetAddress() + ":" + socket.getLocalPort());

    while (!Thread.currentThread().isInterrupted()) {
      try {
        Log.d("socket", "socket" + socket);
        Socket transmissionSocket = socket.accept();
        Transmitter transmitter = new Transmitter("server", transmissionSocket);
        // Temporarily store the transmitter without id until we get the welcome message.
        transmittersById.put("startup-" + transmittersById.keySet().size(), transmitter);
        transmitter.start();
        Status.log("client connected, setting up transmitter");

        // Send a welcome message to the client.
        Status.log("sending welcome message to client");
        transmitter.send(Data.CompanionMessageProto.newBuilder()
            .setHeader(Data.CompanionMessageProto.Header.newBuilder()
                .setSender(Data.CompanionMessageProto.Header.Id.newBuilder()
                    .setId(Settings.get().getAppId())
                    .setName(Settings.get().getNickname())
                    .build())
                // Don't know anything about the client yet.
                .build())
            .setData(Data.CompanionMessageProto.Payload.newBuilder()
                .setWelcome(Data.CompanionMessageProto.Payload.Welcome.newBuilder()
                    .setId(Settings.get().getAppId())
                    .setName(Settings.get().getNickname())
                    .build()))
            .build());

        Log.d("Server", "Connected.");
      } catch (IOException e) {
        Log.d("Server", "io exception when waiting for connections: " + e);
        e.printStackTrace();
      }
    }

    for (Transmitter transmitter : transmittersById.values()) {
      transmitter.stop();
    }

    transmittersById.clear();
  }

  public List<CompanionMessage> receive() {
    List<CompanionMessage> messages = new ArrayList<>();

    for (Map.Entry<String, Transmitter> transmitter : transmittersById.entrySet()) {
      for (Optional<Data.CompanionMessageProto> message = transmitter.getValue().receive();
           message.isPresent(); message = transmitter.getValue().receive()) {
        if (message.isPresent()) {
          // Handle welcome message.
          if (message.get().getData().getPayloadCase()
              == Data.CompanionMessageProto.Payload.PayloadCase.WELCOME) {
            String id = message.get().getData().getWelcome().getId();
            String name = message.get().getData().getWelcome().getName();

            transmittersById.remove(transmitter.getKey());
            transmittersById.put(id, transmitter.getValue());
            namesById.put(id, name);
            Status.log("Received welcome message from client " + name);
          }

          messages.add(CompanionMessage.fromProto(message.get()));
        }
      }
    }

    return messages;
  }

  public boolean send(String recieverId, long messageId, CompanionMessageData message) {
    Transmitter transmitter = transmittersById.get(recieverId);
    if (transmitter == null) {
      Log.e("Server", "No transmitter found for '" + recieverId + "', message not sent.");
      return false;
    }

    Log.d("Server", "sending message: " + message.toString());
    transmitter.send(Data.CompanionMessageProto.newBuilder()
        .setHeader(Data.CompanionMessageProto.Header.newBuilder()
            .setId(messageId)
            .setSender(Data.CompanionMessageProto.Header.Id.newBuilder()
                .setId(Settings.get().getAppId())
                .setName(Settings.get().getNickname())
                .build())
            .setReceiver(Data.CompanionMessageProto.Header.Id.newBuilder()
                .setId(recieverId)
                .setName(namesById.get(recieverId))
                .build())
            .build())
        .setData(message.toProto())
        .build());

    return true;
  }

  public String getNameForId(String id) {
    if (namesById.containsKey(id)) {
      return namesById.get(id);
    }

    return id;
  }
}