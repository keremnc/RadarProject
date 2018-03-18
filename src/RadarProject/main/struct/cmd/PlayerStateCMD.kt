@file:Suppress("NAME_SHADOWING")
package main.struct.cmd

import main.GameListener
import main.deserializer.ROLE_MAX
import main.register
import main.struct.Actor
import main.struct.Bunch
import main.struct.NetGuidCacheObject
import main.struct.NetworkGUID
import main.struct.*
import main.util.tuple2
import java.util.concurrent.ConcurrentHashMap
import main.struct.Item.Companion.simplify
import java.util.concurrent.ConcurrentLinkedQueue

object PlayerStateCMD : GameListener {
    init {
        register(this)
    }

    override fun onGameOver() {
        uniqueIds.clear()
        attacks.clear()
        selfID = NetworkGUID(0)
        selfStateID = NetworkGUID(0)
    }

    val uniqueIds = ConcurrentHashMap<String, NetworkGUID>()
    val attacks = ConcurrentLinkedQueue<Pair<NetworkGUID, NetworkGUID>>()//A -> B
    var selfID = NetworkGUID(0)
    var selfStateID = NetworkGUID(0)

    fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
        actor as PlayerState
        with(bunch) {
            //      println(waitingHandle)
            when (waitingHandle) {
                1 -> {
                    val bHidden = readBit()
                }
                2 -> {
                    val bReplicateMovement = readBit()
                }
                3 -> {
                    val bTearOff = readBit()
                }
                4 -> {
                    val role = readInt(ROLE_MAX)
                    val b = role
                }
                5 -> {
                    val (ownerGUID, owner) = propertyObject()
                }
                7 -> {
                    val (a, obj) = readObject()
                }
                13 -> {
                    readInt(ROLE_MAX)
                }
                16 -> {
                    val score = propertyFloat()
//          println("score=$score")
                }
                17 -> {
                    val ping = propertyByte()
                }
                18 -> {
                    val name = propertyString()
                    actor.name=name
//          println("${actor.netGUID} playerID=$name")
                }
                19 -> {
                    val playerID = propertyInt()
//          println("${actor.netGUID} playerID=$playerID")
                }
                20 -> {
                    val bFromPreviousLevel = propertyBool()
//          println("${actor.netGUID} bFromPreviousLevel=$bFromPreviousLevel")
                }
                21 -> {
                    val isABot = propertyBool()
//          println("${actor.netGUID} isABot=$isABot")
                }
                22 -> {
                    val bIsInactive = propertyBool()
//          println("${actor.netGUID} bIsInactive=$bIsInactive")
                }
                23 -> {
                    val bIsSpectator = propertyBool()
//          println("${actor.netGUID} bIsSpectator=$bIsSpectator")
                }
                24 -> {
                    val bOnlySpectator = propertyBool()
//          println("${actor.netGUID} bOnlySpectator=$bOnlySpectator")
                }
                25 -> {
                    val StartTime = propertyInt()
//          println("${actor.netGUID} StartTime=$StartTime")
                }
                26 -> {
                    val uniqueId = propertyNetId()
                    uniqueIds[uniqueId] = actor.netGUID
//          println("${playerNames[actor.netGUID]}${actor.netGUID} uniqueId=$uniqueId")
                }
                27 -> {//indicate player's death
                    val Ranking = propertyInt()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} Ranking=$Ranking")
                }
                28 -> {
                    val AccountId = propertyString()
//          println("${actor.netGUID} AccountId=$AccountId")
                }
                29 -> {
                    val ReportToken = propertyString()
                }
                30 -> {//ReplicatedCastableItems
                val arraySize=readUInt16()
                actor.castableItems.resize(arraySize)
                var index=readIntPacked()
                while (index != 0) {
                    val idx=index-1
                    val arrayIdx=idx/3
                    val structIdx=idx%3
                    val element=actor.castableItems[arrayIdx] ?: tuple2("",0)
                    when (structIdx) {
                        0 -> {
                            val (guid,castableItemClass)=readObject()
                            if (castableItemClass != null)
                                element._1=simplify(castableItemClass.pathName)
                        }
                        1 -> {
                            val ItemType=readInt(8)
                            val a=ItemType
                        }
                        2 -> {
                            val itemCount=readInt32()
                            element._2=itemCount
                        }
                    }
                    actor.castableItems[arrayIdx]=element
                    index=readIntPacked()
                }
                return true
            }
                31 -> {
                    val ObserverAuthorityType = readInt(4)
                }
                32 -> {
                    val teamNumber = readInt(100)
                    actor.teamNumber=teamNumber
                }
                33 -> {
                    val bIsZombie = propertyBool()
                }
                34 -> {
                    val scoreByDamage = propertyFloat()
                }
                35 -> {
                    val ScoreByKill = propertyFloat()
                }
                36 -> {
                    val ScoreByRanking = propertyFloat()
                }
                37 -> {
                    val ScoreFactor = propertyFloat()
                }
                38 -> {
                    val NumKills = propertyInt()
                    actor.numKills=NumKills
                }
                39 -> {
                    val TotalMovedDistanceMeter = propertyFloat()
                    selfStateID = actor.netGUID//only self will get this update
                }
                40 -> {
                    val TotalGivenDamages = propertyFloat()
                }
                41 -> {
                    val LongestDistanceKill = propertyFloat()
                }
                42 -> {
                    val HeadShots = propertyInt()
                }
                43 -> {//ReplicatedEquipableItems
                    val arraySize=readUInt16()
                    actor.equipableItems.resize(arraySize)
                    var index=readIntPacked()
                    while (index != 0) {
                        val idx=index-1
                        val arrayIdx=idx/2
                        val structIdx=idx%2
                        val element=actor.equipableItems[arrayIdx] ?: tuple2("",0f)
                        when (structIdx) {
                            0 -> {
                                val (guid,equipableItemClass)=readObject()
                                if (equipableItemClass != null)
                                    element._1=simplify(equipableItemClass.pathName)
                                val a=guid
                            }
                            1 -> {
                                val durability=readFloat()
                                element._2=durability
                                val a=durability
                            }
                        }
                        actor.equipableItems[arrayIdx]=element
                        index=readIntPacked()
                    }
                    return true
                }
                44 -> {
                    val bIsInAircraft = propertyBool()
                }
                45 -> {//LastHitTime
                    val lastHitTime = propertyFloat()
                }
                46 -> {
                    val currentAttackerPlayerNetId = propertyString()
                    attacks.add(Pair(uniqueIds[currentAttackerPlayerNetId]!!, actor.netGUID))
                }
                else -> return false
            }
        }
        return true
    }
}