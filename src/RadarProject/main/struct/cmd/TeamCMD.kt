@file:Suppress("NAME_SHADOWING")

package main.struct.cmd

import main.GameListener
import main.bugln
import main.register
import main.struct.Actor
import main.struct.Bunch
import main.struct.*
import main.struct.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


object TeamCMD : GameListener {
    private val counter= AtomicInteger(0)
    val team=ConcurrentHashMap<String,Int>()

    init {
        register(this)
    }

    override fun onGameOver() {
        team.clear()
        counter.set(0)
    }

    fun process(actor:Actor,bunch:Bunch,repObj:NetGuidCacheObject?,waitingHandle:Int,data:HashMap<String,Any?>):Boolean {
        with(bunch) {
            //      println("${actor.netGUID} $waitingHandle")
            when (waitingHandle) {
                16 -> {
                    val playerLocation=propertyVector100()
                }
                17 -> {
                    val playerRotation=readRotationShort()
                }
                18 -> {
                    val playerName=propertyString()
                    team[playerName]=counter.incrementAndGet()
                }
                else -> return ActorCMD.process(actor,bunch,repObj,waitingHandle,data)
            }
            return true
        }
    }
}