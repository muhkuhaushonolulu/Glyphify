package com.frank.glyphify.ui.dialogs

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.frank.glyphify.PermissionHandling
import com.frank.glyphify.R

object Dialog {
    fun showDialog(
        context: Context,
        layoutId: Int,
        buttonActions: Map<Int, (View) -> Unit>,
        isCancelable: Boolean = true,
        delayEnableButtonId: Int? = null,
        delayMillis: Long = 0,
        onDismiss: (() -> Unit)? = null
    ) {
        val dialogView = LayoutInflater.from(context).inflate(layoutId, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(isCancelable)
            .create()

        for ((buttonId, action) in buttonActions) {
            val button = dialogView.findViewById<Button>(buttonId)
            button.setOnClickListener {
                action(dialogView)
                dialog.dismiss()
            }
            button.backgroundTintList = null
        }

        val editText = dialogView.findViewById<EditText>(R.id.editText)
        if(editText != null) {
            dialogView.findViewById<Button>(R.id.positiveButton).isEnabled = false
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    dialogView.findViewById<Button>(R.id.positiveButton).isEnabled = !s.isNullOrEmpty()
                }
            })
        }


        delayEnableButtonId?.let {
            dialogView.findViewById<Button>(it).isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                dialogView.findViewById<Button>(it).isEnabled = true
            }, delayMillis)
        }

        dialog.setOnDismissListener {
            onDismiss?.invoke()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun supportMe(context: Context, permHandler: PermissionHandling) {
        showDialog(
            context,
            R.layout.first_boot,
            mapOf(
                R.id.paypalBtn to {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.paypal.com/donate/?hosted_button_id=HJU8Y7F34Z6TL"))
                    )
                },
                R.id.negativeButton to {
                    val permissions = mutableListOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    permHandler.askRequiredPermissions(permissions, R.layout.dialog_perm_notifications)
                }
            ),
            isCancelable = false,
            delayEnableButtonId = R.id.negativeButton,
            delayMillis = 10000,
            onDismiss = {}
        )
    }
}
