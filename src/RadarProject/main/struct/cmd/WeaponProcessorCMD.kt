@file:Suppress("NAME_SHADOWING")
package main.struct.cmd

import main.deserializer.channel.ActorChannel.Companion.actorHasWeapons
import main.struct.*
import main.util.DynamicArray

object WeaponProcessorReplicator {
    fun process(actor:Actor,bunch:Bunch,repObj:NetGuidCacheObject?,waitingHandle:Int,data:HashMap<String,Any?>):Boolean {
        with(bunch) {
            when (waitingHandle) {
            //AWeaponProcessor
                16 -> {//EquippedWeapons
                    val arraySize=readUInt16()
                    actorHasWeapons.compute(actor.owner!!) {_,equippedWeapons->
                        val equippedWeapons=equippedWeapons?.resize(arraySize) ?: DynamicArray(arraySize)
                        var index=readIntPacked()
                        while (index != 0) {
                            val i=index-1
                            val (netguid,_)=readObject()
                            equippedWeapons[i]=netguid
                            index=readIntPacked()
                        }
                        equippedWeapons
                    }
                }
                17 -> {//CurrentWeaponIndex
                    val currentWeaponIndex=propertyInt()
//          println("$actor carry $currentWeaponIndex")
                }
                else -> return ActorReplicator.process(actor,bunch,repObj,waitingHandle,data)
            }
            return true
        }
    }
}