package de.ironjan.arionav.framework

import com.graphhopper.PathWrapper
import com.google.gson.Gson



object PathWrapperJsonConverter {
    fun toSimplifiedRouteJson(pathWrapper: PathWrapper): String {
        val gson = Gson()
        val simplifiedRoute = simplify(pathWrapper)
        val toJson = gson.toJson(simplifiedRoute)
        return toJson
    }

    fun fromJson(json: String): SimplifiedRoute {
        val gson = Gson()
        return gson.fromJson(json, SimplifiedRoute::class.java)
    }

    private fun simplify(pathWrapper: PathWrapper): SimplifiedRoute {
        val instructionList = pathWrapper.instructions.toList()
        val waypoints = pathWrapper.points
        return SimplifiedRoute(waypoints, instructionList)
    }
}