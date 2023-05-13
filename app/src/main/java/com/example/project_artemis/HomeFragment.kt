package com.example.project_artemis

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import com.example.project_artemis.databinding.FragmentHomeBinding

@Suppress("SENSELESS_COMPARISON")
class HomeFragment : Fragment() {

    private var buildingObject: String? = null
    private lateinit var spinnerBuilding: Spinner
    private lateinit var spinnerTime: Spinner
    private lateinit var itemsBuilding: List<String>
    private lateinit var itemsTime: List<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerBuilding = view.findViewById(R.id.buildingSpinner)
        spinnerTime = view.findViewById(R.id.timeSpinner)

        itemsTime = listOf("24hr", "7d", "1m", "1y", "2y", "3y", "4y", "5y", "All Time")
        itemsBuilding = listOf(
            "CEAFA Building",
            "CIT Building",
            "CICS Building",
            "COE Building",
            "Gymnasium",
            "STEER Hub",
            "Student Services Center"
        )

        val adapterBuilding: ArrayAdapter<*> = ArrayAdapter<Any?>(
            requireContext().applicationContext,
            R.layout.spinner_selected_item,
            itemsBuilding
        )

        adapterBuilding.setDropDownViewResource(R.layout.style_spinner)
        spinnerBuilding.adapter = adapterBuilding

        val selectedBuilding = spinnerBuilding.selectedItem.toString()

        this@HomeFragment.buildingObject = when (selectedBuilding) {
            "CEAFA Building" -> "CEAFA"
            "CIT Building" -> "CIT"
            "CICS Building" -> "CICS"
            "COE Building" -> "COE"
            "Gymnasium" -> "GYM"
            "STEER Hub" -> "STEER Hub"
            "Student Services Center" -> "SSC"
            else -> ""
        }
        val adapterTime: ArrayAdapter<*> = ArrayAdapter<Any?>(
            requireContext().applicationContext,
            R.layout.spinner_selected_item,
            itemsTime
        )

        adapterTime.setDropDownViewResource(R.layout.style_spinner)
        spinnerTime.adapter = adapterTime

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://us-central1-artemis-b18ae.cloudfunctions.net/server/waste/latest")
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle network errors here
            }
        
            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string()
                val jsonArray = JSONArray(responseString)
                val jsonObject = jsonArray.getJSONObject(0)
                val buildingObject: JSONObject? = try {
                    jsonObject.getJSONObject("$buildingObject")
                } catch (e: JSONException) {
                    null
                }
                if (buildingObject != null) {
                    val weightObject = buildingObject.getJSONObject("weight")
                    val residualWasteWeight = weightObject.getInt("residual")
                    val foodWasteWeight = weightObject.getInt("food_waste")
                    val recyclableWasteWeight = weightObject.getInt("recyclable")

                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            binding.displayfood.text = foodWasteWeight.toString()
                            binding.displayres.text = residualWasteWeight.toString()
                            binding.displayrec.text = recyclableWasteWeight.toString()
                        }
                    }
                }
            }
        })
        
        val name = arguments?.getString("name")

        if (name != null) {
            val words = name.split("\\s+".toRegex())
            if (words.size > 1) {
                binding.name.text = words[0]
            } else {
                binding.name.text = name
            }
        }
        
        binding.dashboardTitle.text = getString(R.string.Dashboard)
        binding.thisyourdashboard.text = getString(R.string.this_is_you_dashboard)
        binding.Hello.text = getString(R.string.HELLO)
        binding.mostGatheredWaste.text = getString(R.string.Mostgatheredwaste)

        return binding.root
    }

}