@file:Suppress("NAME_SHADOWING")
package main.struct.cmd

import com.badlogic.gdx.math.*
import main.struct.Archetype.Plane
import main.struct.Actor
import main.struct.Archetype
import main.struct.*
import main.struct.Archetype.*
import main.struct.Bunch
import main.struct.cmd.TeamCMD
import java.util.*

typealias cmdProcessor = (Actor, Bunch, NetGuidCacheObject?, Int, HashMap<String, Any?>) -> Boolean

fun receiveProperties(bunch: Bunch, repObj: NetGuidCacheObject?, actor: Actor): Boolean {
    val cmdProcessor = processors[repObj?.pathName ?: return false] ?: return false
    val data = HashMap<String, Any?>()
    bunch.readBit()
    var waitingHandle = 0
    do {
        waitingHandle = bunch.readIntPacked()
    } while (waitingHandle > 0 && cmdProcessor(actor, bunch, repObj, waitingHandle, data) && bunch.notEnd())
    return waitingHandle == 0
}

    val processors = mapOf<String, cmdProcessor>(
            GameState.name to GameStateCMD::process,
            Other.name to APawnCMD::process,
            DroppedItem.name to DroppedItemCMD::process,
            DroopedItemGroup.name to APawnCMD::process,
            Grenade.name to APawnCMD::process,
            TwoSeatBoat.name to APawnCMD::process,
            SixSeatBoat.name to APawnCMD::process,
            TwoSeatCar.name to APawnCMD::process,
            ThreeSeatCar.name to APawnCMD::process,
            TwoSeatBike.name to APawnCMD::process,
            FourSeatP.name to APawnCMD::process,
            FourSeatDU.name to APawnCMD::process,
            SixSeatCar.name to APawnCMD::process,
            Plane.name to APawnCMD::process,
            Player.name to ActorCMD::process,
            Parachute.name to APawnCMD::process,
            AirDrop.name to AirDropComponentCMD::process,
            PlayerState.name to PlayerStateCMD::process,
            Team.name to TeamCMD::process,
            "DroppedItemGroupRootComponent" to DroppedItemGroupRootComponentCMD::process,
            "DroppedItemInteractionComponent" to DroppedItemInteractionComponentCMD::process,
            WeaponProcessor.name to WeaponProcessorCMD::process
    )
