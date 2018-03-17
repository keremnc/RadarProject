package main.struct.cmd

import main.deserializer.channel.ActorChannel.Companion.airDropLocation
import main.struct.*
import main.struct.cmd.*

object AirDropComponentCMD {
    fun process(actor:Actor,bunch:Bunch,repObj:NetGuidCacheObject?,waitingHandle:Int,data:HashMap<String,Any?>):Boolean {
        with(bunch) {
            when (waitingHandle) {
                6 -> {
                    repMovement(actor)
                    airDropLocation[actor.netGUID]=actor.location
                }
                16 -> updateItemBag(actor)
                else -> return ActorCMD.process(actor,bunch,repObj,waitingHandle,data)
            }
            return true
        }
    }
}