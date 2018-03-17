package main.struct.cmd

import main.struct.*

object VehicleCMD {
    fun process(actor:Actor,bunch:Bunch,repObj:NetGuidCacheObject?,waitingHandle:Int,data:HashMap<String,Any?>):Boolean {
        actor as Vehicle
        with(bunch) {
            when (waitingHandle) {
                16 -> {
                    val (netguid)=propertyObject()
                    actor.driverPlayerState=netguid
                }
                else -> return APawnCMD.process(actor,bunch,repObj,waitingHandle,data)
            }
            return true
        }
    }
}