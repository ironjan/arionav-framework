package de.ironjan.arionav_fw.ionav.services

import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData

data class IndoorDataState(val indoorData: IndoorData, val indoorDataLoadingState: IndoorDataLoadingState)