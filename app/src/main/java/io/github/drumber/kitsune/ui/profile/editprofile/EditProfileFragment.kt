package io.github.drumber.kitsune.ui.profile.editprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.algolia.instantsearch.android.searchbox.SearchBoxViewEditText
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.hits.connectHitsView
import com.algolia.instantsearch.searchbox.connectView
import com.algolia.search.helper.deserialize
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentEditProfileBinding
import io.github.drumber.kitsune.domain.mapper.toCharacter
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.search.CharacterSearchResult
import io.github.drumber.kitsune.ui.base.BaseDialogFragment
import io.github.drumber.kitsune.util.DataUtil
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import io.github.drumber.kitsune.util.parseDate
import io.github.drumber.kitsune.util.toDate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileFragment : BaseDialogFragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModel()

    private val connectionHandler = ConnectionHandler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.nestedScrollView.initMarginWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        if (!viewModel.hasUser()) {
            Toast.makeText(requireContext(), R.string.error_invalid_user, Toast.LENGTH_LONG).show()
            dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { dismiss() }

            fieldLocation.editText?.apply {
                setText(viewModel.profileState.location)
                doAfterTextChanged {
                    viewModel.acceptChanges(
                        viewModel.profileState.copy(
                            location = it?.trim().toString()
                        )
                    )
                }
            }

            fieldBirthday.editText?.setOnClickListener {
                val selectedDate = viewModel.profileState.birthday.parseDate()?.time
                    ?: MaterialDatePicker.todayInUtcMilliseconds()

                openDatePicker(selectedDate, getString(R.string.profile_data_birthday)) { date ->
                    val dateString = date.toDate().formatDate("yyyy-MM-dd")
                    viewModel.acceptChanges(
                        viewModel.profileState.copy(birthday = dateString)
                    )
                }
            }
            fieldBirthday.setEndIconOnClickListener {
                if (viewModel.profileState.birthday.isEmpty()) {
                    fieldBirthday.editText?.performClick()
                } else {
                    viewModel.acceptChanges(
                        viewModel.profileState.copy(birthday = "")
                    )
                }
            }

            (menuGender.editText as? MaterialAutoCompleteTextView)?.apply {
                val genderItems = arrayOf(
                    R.string.profile_data_private,
                    R.string.profile_gender_male,
                    R.string.profile_gender_female,
                    R.string.profile_gender_custom
                ).map { getString(it) }.toTypedArray()
                setSimpleItems(genderItems)
                setText(
                    DataUtil.getGenderString(viewModel.profileState.gender, requireContext()),
                    false
                )

                setOnItemClickListener { _, _, position, _ ->
                    val gender = when (position) {
                        0 -> "secret"
                        1 -> "male"
                        2 -> "female"
                        else -> "custom"
                    }
                    val customGender = if (gender != "custom") "" else viewModel.profileState.customGender
                    viewModel.acceptChanges(
                        viewModel.profileState.copy(
                            gender = gender,
                            customGender = customGender
                        )
                    )
                    binding.fieldCustomGender.editText?.setText(customGender)
                    if (gender == "custom") {
                        postDelayed(100) {
                            binding.fieldCustomGender.editText?.requestFocus()
                        }
                    }
                }
            }

            fieldCustomGender.editText?.setText(viewModel.profileState.customGender)
            fieldCustomGender.editText?.doAfterTextChanged {
                viewModel.acceptChanges(
                    viewModel.profileState.copy(customGender = it?.trim().toString())
                )
            }

            (menuWaifu.editText as? MaterialAutoCompleteTextView)?.apply {
                val waifuItems = arrayOf(
                    "",
                    getString(R.string.profile_data_waifu),
                    getString(R.string.profile_data_husbando)
                )
                setSimpleItems(waifuItems)
                setText(viewModel.profileState.waifuOrHusbando, false)

                setOnItemClickListener { _, _, position, _ ->
                    val waifuOrHusbando = waifuItems[position]
                    viewModel.acceptChanges(
                        viewModel.profileState.copy(waifuOrHusbando = waifuOrHusbando)
                    )
                }
            }

            fieldSearchWaifu.editText?.setOnClickListener {
                viewModel.initSearchClient()
                characterSearchView.clearText()
                characterSearchView.show()
            }
            fieldSearchWaifu.setEndIconOnClickListener {
                if (viewModel.profileState.character != null) {
                    viewModel.acceptChanges(
                        viewModel.profileState.copy(character = null)
                    )
                } else {
                    fieldSearchWaifu.editText?.performClick()
                }
            }

            fieldBio.editText?.apply {
                setText(viewModel.profileState.about)
                doAfterTextChanged {
                    viewModel.acceptChanges(
                        viewModel.profileState.copy(about = it?.trim().toString())
                    )
                }
            }

            btnUpdateProfile.setOnClickListener {
                viewModel.updateUserProfile()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileStateFlow.collectLatest { profileState ->
                binding.apply {
                    fieldBirthday.editText?.setText(
                        profileState.birthday.parseDate()?.formatDate()
                    )
                    fieldBirthday.setEndIconDrawable(
                        if (profileState.birthday.isEmpty()) {
                            R.drawable.ic_calendar_month_24
                        } else {
                            R.drawable.ic_close_24
                        }
                    )

                    fieldCustomGender.isVisible = profileState.gender == "custom"

                    fieldSearchWaifu.apply {
                        isVisible = profileState.waifuOrHusbando.isNotBlank()
                        editText?.setText(profileState.character?.name)
                        setEndIconDrawable(
                            if (profileState.character == null) {
                                R.drawable.ic_search_24
                            } else {
                                R.drawable.ic_heart_broken_24
                            }
                        )
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.canUpdateProfileFlow.collectLatest { canUpdate ->
                binding.btnUpdateProfile.isEnabled = canUpdate
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadingStateFlow.collectLatest { loadingState ->
                binding.layoutLoading.isVisible = loadingState is LoadingState.Loading

                if (loadingState is LoadingState.Error) {
                    Toast.makeText(
                        requireContext(),
                        R.string.error_user_update_failed,
                        Toast.LENGTH_LONG
                    ).show()
                } else if (loadingState is LoadingState.Success) {
                    dismiss()
                }
            }
        }

        initSearchView()
    }

    private fun initSearchView() {
        val adapter = CharacterSearchResultAdapter {
            viewModel.acceptChanges(viewModel.profileState.copy(character = it.toCharacter()))
            binding.characterSearchView.hide()
        }

        binding.rvCharacterResults.apply {
            initPaddingWindowInsetsListener(left = true, right = true, bottom = true, consume = false)
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchBoxConnectorFlow.collectLatest { searchBox ->
                connectionHandler.clear()
                val searchBoxView = SearchBoxViewEditText(binding.characterSearchView.editText)
                connectionHandler += searchBox.connectView(searchBoxView)
                connectionHandler += searchBox.searcher.connectHitsView(adapter) { response ->
                    response.hits.deserialize(CharacterSearchResult.serializer())
                }
            }
        }
    }

    private fun openDatePicker(selectedDate: Long, title: String, action: (Long) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(selectedDate)
            .build()

        datePicker.addOnPositiveButtonClickListener(action)
        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        connectionHandler.clear()
    }
}