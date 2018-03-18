@file:Suppress("NAME_SHADOWING")
package main.struct.cmd

import main.struct.Archetype.Plane
import main.struct.Actor
import main.struct.Archetype
import main.struct.*
import main.struct.Archetype.*
import main.struct.Bunch
import main.struct.cmd.Replicator.processors
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


object Replicator {
    val processors = mapOf<String, cmdProcessor>(
            Team.name to TeamReplicator::process,
            GameState.name to GameStateCMD::process,
            Other.name to APawnCMD::process,
            DroppedItem.name to DroppedItemCMD::process,
            DroopedItemGroup.name to APawnCMD::process,
            Grenade.name to APawnCMD::process,
            TwoSeatBoat.name to VehicleCMD::process,
            SixSeatBoat.name to VehicleCMD::process,
            TwoSeatCar.name to VehicleCMD::process,
            ThreeSeatCar.name to VehicleCMD::process,
            TwoSeatBike.name to VehicleCMD::process,
            FourSeatP.name to VehicleCMD::process,
            FourSeatDU.name to VehicleCMD::process,
            SixSeatCar.name to VehicleCMD::process,
            Plane.name to VehicleCMD::process,
            Player.name to ActorCMD::process,
            Parachute.name to APawnCMD::process,
            AirDrop.name to AirDropComponentCMD::process,
            Archetype.PlayerState.name to PlayerStateCMD::process,
            "DroppedItemGroupRootComponent" to DroppedItemGroupRootComponentCMD::process,
            "DroppedItemInteractionComponent" to DroppedItemInteractionComponentCMD::process,
            WeaponProcessor.name to  WeaponProcessorReplicator::process
    )
}