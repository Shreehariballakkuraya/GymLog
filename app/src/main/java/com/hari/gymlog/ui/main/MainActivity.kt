package com.hari.gymlog.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.hari.gymlog.R
import com.hari.gymlog.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // The 5 root destinations that should SHOW the bottom nav
    private val rootDestinations = setOf(
        R.id.homeFragment,
        R.id.workoutFragment,
        R.id.foodFragment,
        R.id.activityFragment,
        R.id.progressFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // Hide bottom nav on non-root screens (e.g. Reminder Settings, Weight, History)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in rootDestinations) {
                binding.bottomNav.visibility = View.VISIBLE
            } else {
                binding.bottomNav.visibility = View.GONE
            }
        }
    }
}
