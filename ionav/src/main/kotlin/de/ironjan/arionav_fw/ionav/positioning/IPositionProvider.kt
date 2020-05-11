package de.ironjan.arionav_fw.ionav.positioning

/**
 * Base interface for position providers.
 */
interface IPositionProvider : IPositionObservable {
    /** The provider's name */
    val name: String

    /** <code>true</code> if the provider is currently tracking positions */
    val enabled: Boolean

    /** <code>true</code> if the provider is currently stopped.  */
    val disabled: Boolean
        get() = !enabled

    /** Starts the provider */
    fun start()

    /** Stops the provider */
    fun stop()

}
