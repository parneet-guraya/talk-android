/*
 * Nextcloud Talk application
 *
 * @author Andy Scherzinger
 * @author Parneet Singh
 * Copyright (C) 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * Copyright (C) 2024 Parneet Singh <gurayaparneet@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.nextcloud.talk.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import autodagger.AutoInjector
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.nextcloud.talk.R
import com.nextcloud.talk.api.NcApi
import com.nextcloud.talk.application.NextcloudTalkApplication
import com.nextcloud.talk.data.user.model.User
import com.nextcloud.talk.databinding.DialogSetPhoneNumberBinding
import com.nextcloud.talk.models.json.generic.GenericOverall
import com.nextcloud.talk.settings.SettingsActivity
import com.nextcloud.talk.ui.theme.ViewThemeUtils
import com.nextcloud.talk.utils.ApiUtils
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AutoInjector(NextcloudTalkApplication::class)
class SetPhoneNumberDialogFragment : DialogFragment() {

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    @Inject
    lateinit var ncApi: NcApi

    private lateinit var binding: DialogSetPhoneNumberBinding
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NextcloudTalkApplication.sharedApplication?.componentApplication?.inject(this)
        arguments?.let {
            currentUser = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(CURRENT_USER_ARG, User::class.java)
            } else {
                it.getParcelable(CURRENT_USER_ARG)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogSetPhoneNumberBinding.inflate(requireActivity().layoutInflater)

        val dialogBuilder = MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.nc_settings_phone_book_integration_phone_number_dialog_title)
            .setView(binding.root)
            .setMessage(R.string.nc_settings_phone_book_integration_phone_number_dialog_description)
            .setPositiveButton(requireContext().resources.getString(R.string.nc_common_set)) { dialog, _ ->
                setPhoneNumber(binding.phoneInputLayout, dialog)
            }
            .setNegativeButton(requireContext().resources.getString(R.string.nc_common_skip)) { dialog, _ -> dialog.dismiss() }

        viewThemeUtils.dialog.colorMaterialAlertDialogBackground(requireActivity(), dialogBuilder)

        binding.phoneInputLayout.setHelperTextColor(
            ColorStateList.valueOf(
                resources.getColor(
                    R.color.nc_darkRed,
                    null
                )
            )
        )

        binding.phoneEditTextField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.phoneInputLayout.helperText = ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })


        return dialogBuilder.create()
    }

    override fun onStart() {
        super.onStart()
        val alertDialog = dialog as AlertDialog?
        alertDialog?.let {
            viewThemeUtils.platform.colorTextButtons(it.getButton(AlertDialog.BUTTON_POSITIVE))
            viewThemeUtils.platform.colorTextButtons(it.getButton(AlertDialog.BUTTON_NEGATIVE))
        }
    }

    private fun setPhoneNumber(textInputLayout: TextInputLayout, dialog: DialogInterface) {
        val phoneNumber = textInputLayout.editText!!.text.toString()
        ncApi.setUserData(
            ApiUtils.getCredentials(currentUser!!.username, currentUser!!.token),
            ApiUtils.getUrlForUserData(currentUser!!.baseUrl!!, currentUser!!.userId!!),
            "phone",
            phoneNumber
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GenericOverall> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                override fun onNext(genericOverall: GenericOverall) {
                    when (val statusCode = genericOverall.ocs?.meta?.statusCode) {
                        SettingsActivity.HTTP_CODE_OK -> {
                            dialog.dismiss()
                            Snackbar.make(
                                binding.root,
                                resources.getString(
                                    R.string.nc_settings_phone_book_integration_phone_number_dialog_success
                                ),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        else -> {
                            textInputLayout.helperText = resources.getString(
                                R.string.nc_settings_phone_book_integration_phone_number_dialog_invalid
                            )
                            Log.d(TAG, "failed to set phoneNumber. statusCode=$statusCode")
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    textInputLayout.helperText = resources.getString(
                        R.string.nc_settings_phone_book_integration_phone_number_dialog_invalid
                    )
                    Log.e(TAG, "setPhoneNumber error", e)
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    companion object {
        const val TAG = "SetPhoneNumberDialogFragment"
        private const val CURRENT_USER_ARG = "CURRENT_USER_ARG"

        fun newInstance(currentUser: User): DialogFragment {
            val bundle = Bundle()
            bundle.putParcelable(CURRENT_USER_ARG, currentUser)
            val fragment = SetPhoneNumberDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
