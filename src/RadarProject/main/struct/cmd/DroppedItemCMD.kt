package pubgradar.struct.cmd

import main.deserializer.channel.ActorChannel.Companion.droppedItemToItem
import main.struct.Actor
import main.struct.Bunch
import main.struct.NetGuidCacheObject
import main.struct.cmd.ActorReplicator
import pubgradar.struct.*

object DroppedItemCMD {

    fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle:Int, data:HashMap<String,Any?>):Boolean {
        with(bunch) {
            when (waitingHandle) {
                16 -> {
                    val (itemguid,item)=readObject()
                    droppedItemToItem[actor.netGUID]=itemguid
                }
                else -> ActorReplicator.process(actor,bunch,repObj,waitingHandle,data)
            }
            return true
        }
    }
}