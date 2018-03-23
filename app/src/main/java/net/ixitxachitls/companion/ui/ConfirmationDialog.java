/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Simple dialog to show a confirmation and waiting for a users yes or not.
 */
public class ConfirmationDialog {

  private final AlertDialog.Builder dialog;

  @Deprecated
  public interface Callback {
    void yes();
    void no();
  }

  public ConfirmationDialog(Context context) {
    this.dialog = new AlertDialog.Builder(context);
    this.dialog
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, null);
  }

  public ConfirmationDialog title(String title) {
    dialog.setTitle(title);
    return this;
  }

  public ConfirmationDialog message(String message) {
    dialog.setMessage(message);
    return this;
  }

  public ConfirmationDialog yes(Action action) {
    dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
        action.execute();
      }
    });
    return this;
  }

  public ConfirmationDialog no(Action action) {
    dialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
        action.execute();
      }
    });
    return this;
  }

  public ConfirmationDialog noNo() {
    dialog.setNegativeButton(null, null);
    return this;
  }

  public void show() {
    this.dialog.show();
  }


  @FunctionalInterface
  public interface Action {
    public void execute();
  }

  @FunctionalInterface
  @Deprecated
  public interface YesAction {
    public void yes();
  }

  @FunctionalInterface
  @Deprecated
  public interface NoAction {
    public void no();
  }

  public static ConfirmationDialog create(Context context) {
    return new ConfirmationDialog(context);
  }
}
