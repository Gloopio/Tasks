package io.gloop.tasks.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.gloop.Gloop;
import io.gloop.tasks.R;
import io.gloop.tasks.model.TaskAccessRequest;
import io.gloop.tasks.model.Task;
import io.gloop.permissions.GloopGroup;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */

public class AcceptBoardAccessDialog extends Dialog {

    public AcceptBoardAccessDialog(@NonNull final Context context, final TaskAccessRequest request) {
        super(context, R.style.AppTheme_PopupTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_acceped_board_access);

        TextView textView = (TextView) findViewById(R.id.dialog_accept_text);
        textView.setText(context.getString(R.string.access_request, request.getUserId(), request.getBoardName()));

        //grant access
        Button grantButton = (Button) findViewById(R.id.dialog_accept_btn_grant);
        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GloopGroup group = Gloop
                        .all(GloopGroup.class)
                        .where()
                        .equalsTo("objectId", request.getBoardGroupId())
                        .first();
                group.addMember(request.getUserId());
                group.save();

                Task task = Gloop.all(Task.class)
                        .where()
                        .equalsTo("name", request.getBoardName())
                        .first();

                if (task != null) {
                    task.getTaskGroup().addMember(request.getUserId(), request.getUserImageUri());
                    task.save();
                }

                if (request.delete())
                    dismiss();
                else
                    Toast.makeText(context, "Could not complete the request.", Toast.LENGTH_LONG).show();

            }
        });
        // deny access
        Button denyButton = (Button) findViewById(R.id.dialog_accept_btn_deny);
        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (request.delete())
                    dismiss();
                else
                    Toast.makeText(context, "Could not complete the request.", Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
    }

}
