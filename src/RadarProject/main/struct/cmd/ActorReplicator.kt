package main.struct.cmd

import main.deserializer.ROLE_MAX
import main.deserializer.channel.ActorChannel.Companion.actors
import main.struct.*

object ActorReplicator {
  fun process(actor:Actor,bunch:Bunch,repObj:NetGuidCacheObject?,waitingHandle:Int,data:HashMap<String,Any?>):Boolean {
    with(bunch) {
      when (waitingHandle) {
      //AActor
        1 -> readBit()//bHidden
        2 -> readBit() // bReplicateMovement
        3 -> readBit() //bTearOff
        4 -> readInt(ROLE_MAX)
        5 -> {
          val (netGUID,_)=readObject()
          actor.owner=if (netGUID.isValid()) netGUID else null
        }
        6 -> repMovement(actor)
        7 -> {
          val (a,_)=readObject()
          val attachTo=if (a.isValid()) {
            actors[a]?.attachChildren?.add(actor.netGUID)
            a
          } else null
          if (actor.attachParent != null)
            actors[actor.attachParent!!]?.attachChildren?.remove(actor.netGUID)
          actor.attachParent=attachTo
        }
        8 -> propertyVector100()
        9 -> propertyVector100()
        10 -> readRotationShort()
        11 -> propertyName()
        12 -> readObject()
        13 -> readInt(ROLE_MAX)
        14 -> propertyBool()
        15 -> propertyObject()
        else -> return false
      }
      return true
    }
  }
}