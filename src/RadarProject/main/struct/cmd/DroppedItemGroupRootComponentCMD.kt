package main.struct.cmd

import main.deserializer.channel.ActorChannel.Companion.droppedItemCompToItem
import main.deserializer.channel.ActorChannel.Companion.droppedItemGroup
import main.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import main.deserializer.channel.ActorChannel.Companion.itemBag
import main.struct.*
import main.util.DynamicArray

object DroppedItemGroupRootComponentCMD {
    fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
        with(bunch) {
            when (waitingHandle) {
                4 -> {
                    val arraySize = readUInt16()
                    val oldSize: Int
                    val items: DynamicArray<NetworkGUID?>
                    val oldItems = droppedItemGroup[actor.netGUID]
                    if (oldItems == null) {
                        oldSize = 0
                        items = DynamicArray(arraySize)
                    } else {
                        oldSize = oldItems.size
                        items = oldItems.resize(arraySize)
                    }
                    var index = readIntPacked()
                    val toRemove = HashSet<NetworkGUID>()
                    val toAdd = HashSet<NetworkGUID>()
                    while (index != 0) {
                        val i = index - 1
                        val (netguid, obj) = readObject()
                        items[i]?.apply {
                            toRemove.add(this)
                            toAdd.add(netguid)
                        }
                        items[i] = netguid
                        index = readIntPacked()
                    }
                    for (i in oldSize - 1 downTo arraySize)
                        items.rawGet(i)?.apply { toRemove.add(this) }
                    toRemove.removeAll(toAdd)
                    droppedItemGroup[actor.netGUID] = items
                    for (removedComp in toRemove)
                        droppedItemLocation.remove(droppedItemCompToItem[removedComp] ?: continue)
                }
                else -> return false
            }
        }
        return true
    }
}

fun Bunch.updateItemBag(actor:Actor) {
    val arraySize=readUInt16()
    val oldSize:Int
    val items:DynamicArray<NetworkGUID?>
    val oldItems=itemBag[actor.netGUID]
    if (oldItems == null) {
        oldSize=0
        items=DynamicArray(arraySize)
    } else {
        oldSize=oldItems.size
        items=oldItems.resize(arraySize)
    }
    var index=readIntPacked()
    val toRemove=HashSet<NetworkGUID>()
    val toAdd=HashSet<NetworkGUID>()
    while (index != 0) {
        val i=index-1
        val (netguid,obj)=readObject()
        items[i]?.apply {
            toRemove.add(this)
            toAdd.add(netguid)
        }
        items[i]=netguid
        index=readIntPacked()
    }
    for (i in oldSize-1 downTo arraySize)
        items.rawGet(i)?.apply {toRemove.add(this)}
    toRemove.removeAll(toAdd)
    itemBag[actor.netGUID]=items
    for (removedComp in toRemove)
        droppedItemLocation.remove(droppedItemCompToItem[removedComp] ?: continue)
}