package fr.lleotraas.blackjack_french.ui.fragment

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.github.aachartmodel.aainfographics.aachartcreator.*
import dagger.hilt.android.AndroidEntryPoint
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.databinding.FragmentProfileBinding
import fr.lleotraas.blackjack_french.model.User
import fr.lleotraas.blackjack_french.ui.activity.ProfileActivityViewModel
import fr.lleotraas.blackjack_french.utils.ImageUtils
import fr.lleotraas.blackjack_french.utils.Utils
import fr.lleotraas.blackjack_french.utils.Utils.Companion.CURRENT_USER_ID

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var mBinding: FragmentProfileBinding
    private val mViewModel: ProfileActivityViewModel by viewModels()
    private lateinit var currentUser: User
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private var currentImage = false
    private var rotation = 90f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentProfileBinding.inflate(inflater, container, false)
        val currentUserId = requireActivity().intent.extras?.get(CURRENT_USER_ID) as String
        updateUi(currentUserId)
        getUserStatsTab(currentUserId)
        permissionsLauncher = registerForPermissions()
        configureListeners(registerForPickMedia())
        updateOrRequestPermission()
        configureSupportNavigateUp()
        return mBinding.root
    }

    private fun registerForPickMedia() = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Glide.with(mBinding.root)
                    .load(uri)
                    .circleCrop()
                    .into(mBinding.fragmentProfileImg)
                createTag(uri.toString())
                currentImage = true
            } else {
                Log.e(TAG, "configureListeners: no media selected")
            }
    }

    private fun registerForPermissions() = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
        writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted
    }

    private fun updateUi(currentUserId: String) {
        mViewModel.getOnlineUser(currentUserId).observe(viewLifecycleOwner) { user ->
            currentUser = user as User
            rotation = user.pictureRotation!!
            mBinding.apply {
                if (user.isDefaultProfileImage == false) {
                    mViewModel.getAllImage().observe(viewLifecycleOwner) { allImage ->
                        Glide.with(root)
                            .load(allImage[user.id])
                            .circleCrop()
                            .into(fragmentProfileImg)
                    }
                } else {
                    Glide.with(root)
                        .load(user.userPicture)
                        .circleCrop()
                        .into(fragmentProfileImg)
                }
                fragmentProfileUsernameInput.setText(user.pseudo)
                fragmentProfileImg.rotation = user.pictureRotation!!
            }
        }
    }

    private fun createGraphView(arrayList: ArrayList<Any>, arrayList2: ArrayList<String>) {
        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Line)
            .zoomType(AAChartZoomType.XY)
            .animationType(AAChartAnimationType.Elastic)
            .title("User Wallet")
            .dataLabelsEnabled(false)
            .categories(arrayList2.toTypedArray())
            .yAxisTitle("")
            .series(arrayOf(
                AASeriesElement()
                    .name("User Economy")
                    .data(arrayList.toTypedArray())
                    .color("#BB86FC")
            ))
        mBinding.fragmentProfilePlayerInfoGraph.aa_drawChartWithChartModel(aaChartModel)
    }

    private fun getUserStatsTab(searchedUserId: String) {
        mViewModel.getUserStats(searchedUserId).observe(viewLifecycleOwner) { listOfGamePlayed ->
            val arrayOfWalletVariation = ArrayList<Any>()
            val arrayOfDate = ArrayList<String>()
            for (gamePlayed in listOfGamePlayed) {
                arrayOfWalletVariation.add(gamePlayed.walletStateWhenGameEnding ?: 0.0)
                arrayOfDate.add(Utils.formatDate(gamePlayed.date.toString()))
            }
            createGraphView(arrayOfWalletVariation, arrayOfDate)
        }
    }

    private fun saveAndExit() {
        currentUser.pseudo = mBinding.fragmentProfileUsernameInput.text.toString()
        currentUser.pictureRotation = rotation
        Log.e("ProfileFragment", "saveAndExit: rotation: $rotation userRotation: ${currentUser.pictureRotation}")
        mViewModel.updateUser(currentUser)
        requireActivity().finish()
    }

    private fun configureListeners(pickMedia: ActivityResultLauncher<PickVisualMediaRequest>) {
        mBinding.apply {
            fragmentProfileEditPicture.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            fragmentProfileRotateImage.setOnClickListener {
                rotation += 90f
                fragmentProfileImg.rotation = rotation
                if (rotation > 270f) {
                    rotation = 0f
                }
                Log.e("ProfileFragment", "configureListeners: rotation: $rotation")
            }
        }
    }

    private fun configureSupportNavigateUp() {
        requireActivity().addMenuProvider(object : MenuProvider{

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.profile_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.menu_save_profile) {
                    saveProfileAndExit()
                } else {
                    requireActivity().finish()
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveProfileAndExit() {
        mBinding.apply {
            if (currentImage) {
                currentUser.isDefaultProfileImage = false
                val imageResized = ImageUtils().reduceImageSize(fragmentProfileImg.tag.toString().toUri(), requireActivity().contentResolver)
                if (imageResized != null) {
                    mViewModel.deleteImage(
                        currentUser.id.toString(),
                        currentUser.userPicture!!.toUri()
                    ).also {
                        mViewModel.uploadImage(
                            currentUser.id.toString(),
                            fragmentProfileImg.tag.toString().toUri(),
                            imageResized
                        )
                    }
                    saveAndExit()
                } else {
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.fragment_profile_image_over_size), Toast.LENGTH_SHORT).show()
                }
            } else {
                saveAndExit()
            }
        }
    }

    private fun createTag(uri: String) {
        val imageFilePath = Uri.parse(uri)
        mBinding.fragmentProfileImg.tag = imageFilePath
    }

    private fun updateOrRequestPermission() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}