package main.struct

import com.badlogic.gdx.math.Vector3
import main.struct.Archetype.*
import main.struct.Archetype.Companion.fromArchetype
import main.struct.Archetype.PlayerState
import main.util.DynamicArray
import main.util.tuple2
import java.util.Collections.newSetFromMap
import java.util.concurrent.ConcurrentHashMap

enum class Archetype { //order matters, it affects the order of drawing
    Other,
    GameState,
    Plane,
    Parachute,
    Player,
    DroopedItemGroup,
    Grenade,
    TwoSeatBoat,
    FourSeatDU,
    FourSeatP,
    SixSeatBoat,
    TwoSeatBike,
    TwoSeatCar,
    ThreeSeatCar,
    SixSeatCar,
    AirDrop,
    PlayerState,
    Team,
    DeathDropItemPackage,
    DroppedItem,
    WeaponProcessor,
    Weapon;

    companion object {
        fun fromArchetype(archetype:String)=when {
            archetype.contains("Default__TSLGameState") -> GameState
            archetype.contains("Aircraft") -> Plane
            archetype.contains("Parachute") -> Parachute
            archetype.contains("Default__Player") -> Player
            archetype.contains("DroppedItemGroup") -> DroopedItemGroup
            archetype.contains("bike", true) -> TwoSeatBike
            archetype.contains("Sidecart", true) -> ThreeSeatCar
            archetype.contains("buggy", true) -> TwoSeatCar
            archetype.contains("dacia", true) -> FourSeatDU
            archetype.contains("uaz", true) -> FourSeatDU
            archetype.contains("pickup", true) -> FourSeatP
            archetype.contains("bus", true) -> SixSeatCar
            archetype.contains("van", true) -> SixSeatCar
            archetype.contains("AquaRail", true) -> TwoSeatBoat
            archetype.contains("boat", true) -> SixSeatBoat
            archetype.contains("Carapackage", true) -> AirDrop
            archetype.contains(Regex("(SmokeBomb|Molotov|Grenade|FlashBang|BigBomb)", RegexOption.IGNORE_CASE)) -> Grenade
            archetype.contains("Default__TslPlayerState") -> PlayerState
            archetype.contains("Default__Team",true) -> Team
            archetype.contains("DeathDropItemPackage", true) -> DeathDropItemPackage
            archetype.contains("DroppedItem") -> DroppedItem
            archetype.contains("Default__WeaponProcessor") -> WeaponProcessor
            archetype.contains("Weap") -> Weapon
            else -> Other
        }
    }
}


    fun makeActor(netGUID:NetworkGUID,archetype:NetGuidCacheObject):Actor {
        val type = fromArchetype(archetype.pathName)
        return when (type) {
            Player -> Character(netGUID,type,archetype.pathName)
            PlayerState -> PlayerState(netGUID,type,archetype.pathName)
            TwoSeatBoat,FourSeatDU,FourSeatP,SixSeatBoat,
            TwoSeatBike,TwoSeatCar,
            ThreeSeatCar,SixSeatCar,Plane -> Vehicle(netGUID,type,archetype.pathName)
            else-> Actor(netGUID,type,archetype.pathName)
        }
    }


open class Actor(val netGUID:NetworkGUID,val type:Archetype,val typeName:String) {

    var location=Vector3.Zero
    var rotation=Vector3.Zero
    var velocity=Vector3.Zero

    var owner:NetworkGUID?=null
    var attachParent:NetworkGUID?=null
    var attachChildren=newSetFromMap(ConcurrentHashMap<NetworkGUID,Boolean>())
    var isStatic=false

    override fun toString()="[${netGUID.value}]($typeName)"
    
    val isAPawn = when (type) {
        Parachute,
        TwoSeatBoat,
        SixSeatBoat,
        TwoSeatBike,
        TwoSeatCar,
        ThreeSeatCar,
        FourSeatDU,
        FourSeatP,
        SixSeatCar,
        Plane,
        Player -> true
        else -> false
    }
    val isACharacter = type == Player
    val isVehicle = type.ordinal >= TwoSeatBoat.ordinal && type.ordinal <= SixSeatCar.ordinal
}

class Character(netGUID:NetworkGUID,type:Archetype,typeName:String): Actor(netGUID,type,typeName) {
    var health=100f
    var groggyHealth=100f
    var boostGauge=0f
    var isReviving=false
    var isGroggying=false
}

class PlayerState(netGUID:NetworkGUID,type:Archetype,typeName:String): Actor(netGUID,type,typeName) {
    var name:String=""
    var teamNumber=0
    var numKills=0
    val equipableItems=DynamicArray<tuple2<String,Float>?>(3,0)
    val castableItems=DynamicArray<tuple2<String,Int>?>(8,0)
}

class Vehicle(netGUID:NetworkGUID,type:Archetype,typeName:String): Actor(netGUID,type,typeName) {
    var driverPlayerState=NetworkGUID(0)
}
    