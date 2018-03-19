@file:Suppress("NAME_SHADOWING")

package main.ui


import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D
import com.badlogic.gdx.graphics.Texture.TextureFilter.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Buttons.LEFT
import com.badlogic.gdx.Input.Buttons.MIDDLE
import com.badlogic.gdx.Input.Buttons.RIGHT
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.DEFAULT_CHARS
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import main.*
import main.deserializer.channel.ActorChannel.Companion.actorHasWeapons
import main.deserializer.channel.ActorChannel.Companion.actors
import main.deserializer.channel.ActorChannel.Companion.airDropLocation
import main.deserializer.channel.ActorChannel.Companion.corpseLocation
import main.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import main.deserializer.channel.ActorChannel.Companion.visualActors
import main.deserializer.channel.ActorChannel.Companion.weapons
import main.struct.Actor
import main.struct.Archetype
import main.struct.Archetype.*
import main.struct.Character
import main.struct.NetworkGUID
import main.struct.cmd.ActorCMD.actorHealth
import main.struct.cmd.ActorCMD.actorWithPlayerState
import main.struct.cmd.ActorCMD.playerStateToActor
import main.struct.cmd.GameStateCMD.ElapsedWarningDuration
import main.struct.cmd.GameStateCMD.IsTeamMatch
import main.struct.cmd.GameStateCMD.NumAlivePlayers
import main.struct.cmd.GameStateCMD.NumAliveTeams
import main.struct.cmd.GameStateCMD.PoisonGasWarningPosition
import main.struct.cmd.GameStateCMD.PoisonGasWarningRadius
import main.struct.cmd.GameStateCMD.RedZonePosition
import main.struct.cmd.GameStateCMD.RedZoneRadius
import main.struct.cmd.GameStateCMD.SafetyZonePosition
import main.struct.cmd.GameStateCMD.SafetyZoneRadius
import main.struct.cmd.GameStateCMD.TotalWarningDuration
import main.struct.cmd.PlayerStateCMD.attacks
import main.struct.cmd.PlayerStateCMD.selfID
import main.struct.cmd.PlayerStateCMD.selfStateID
import main.struct.PlayerState
import main.struct.cmd.TeamReplicator.team
import main.struct.cmd.selfAttachTo
import main.struct.cmd.selfHeight
import main.struct.cmd.selfCoords
import main.struct.cmd.selfDirection
import main.util.tuple5
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

typealias renderInfo = tuple5<Actor, Float, Float, Float, Float>

class GLMap : InputAdapter(), ApplicationListener, GameListener {
    companion object {
        operator fun Vector3.component1(): Float = x
        operator fun Vector3.component2(): Float = y
        operator fun Vector3.component3(): Float = z
        operator fun Vector2.component1(): Float = x
        operator fun Vector2.component2(): Float = y
    }

    init {
        register(this)
    }


    override fun onGameStart() {
        //preSelfCoords.set(if (isErangel) spawnErangel else spawnDesert)
        // selfCoords.set(preSelfCoords)
        //preDirection.setZero()
        selfCoords.setZero()
        selfAttachTo = null
    }

    override fun onGameOver() {
        camera.zoom = 2 / 4f

        aimStartTime.clear()
        attackLineStartTime.clear()
        pinLocation.setZero()
    }

    fun show() {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("")
        config.useOpenGL3(false, 2, 1)
        config.setWindowedMode(800, 800)
        config.setResizable(true)
        config.useVsync(false)
        config.setIdleFPS(120)
        config.setBackBufferConfig(4, 4, 4, 4, 16, 4, 8)
        Lwjgl3Application(this, config)

    }


    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    lateinit var mapErangel: Texture
    lateinit var mapMiramar: Texture
    private lateinit var DaMap: Texture
    private lateinit var iconImages: Icons
    private lateinit var corpseboximage: Texture
    private lateinit var AirDropAllTheColors: Texture
    private lateinit var bgcompass: Texture
    private lateinit var menu: Texture
    private lateinit var largeFont: BitmapFont
    private lateinit var littleFont: BitmapFont
    private lateinit var nameFont: BitmapFont
    private lateinit var itemFont: BitmapFont
    private lateinit var hporange: BitmapFont
    private lateinit var hpred: BitmapFont
    private lateinit var hpgreen: BitmapFont
    private lateinit var menuFont: BitmapFont
    private lateinit var menuFontOn: BitmapFont
    private lateinit var menuFontOFF: BitmapFont
    private lateinit var fontCamera: OrthographicCamera
    private lateinit var itemCamera: OrthographicCamera
    private lateinit var camera: OrthographicCamera
    private lateinit var alarmSound: Sound
    private lateinit var hubpanel: Texture
    private lateinit var hubpanelblank: Texture
    private lateinit var vehicle: Texture
    private lateinit var boato: Texture
    private lateinit var teamarrow: Texture

    private lateinit var vano: Texture
    private lateinit var vehicleo: Texture
    private lateinit var jetskio: Texture
    private lateinit var plane: Texture
    private lateinit var boat: Texture
    private lateinit var BikeBLUE: Texture
    private lateinit var BikeRED: Texture
    private lateinit var Bike3BLUE: Texture
    private lateinit var Bike3RED: Texture
    private lateinit var pickupBLUE: Texture
    private lateinit var pickupRED: Texture
    private lateinit var BuggyBLUE: Texture
    private lateinit var BuggyRED: Texture
    private lateinit var van: Texture
    private lateinit var arrow: Texture
    private lateinit var arrowsight: Texture
    private lateinit var jetski: Texture
    private lateinit var player: Texture
    private lateinit var playersight: Texture
    private lateinit var teamsight: Texture
    private lateinit var parachute: Texture
    private lateinit var grenade: Texture
    private lateinit var hubFont: BitmapFont
    private lateinit var hubFontShadow: BitmapFont
    private lateinit var espFont: BitmapFont
    private lateinit var espFontShadow: BitmapFont
    private lateinit var compaseFont: BitmapFont
    private lateinit var compaseFontShadow: BitmapFont
    private lateinit var littleFontShadow: BitmapFont

    private val layout = GlyphLayout()
    private var windowWidth = initialWindowWidth
    private var windowHeight = initialWindowWidth

    private val aimStartTime = HashMap<NetworkGUID, Long>()
    private val attackLineStartTime = LinkedList<Triple<NetworkGUID, NetworkGUID, Long>>()
    private val pinLocation = Vector2()
    // Menu Settings
    //////////////////////////////
    private var filterWeapon = -1
    private var filterAttach = -1
    private var filterArmorBag = 1
    private var filterLvl2 = -1
    private var filterLvl3 = 1
    private var filterScope = -1
    private var filterHeals = -1
    private var filterAmmo = 1
    private var filterThrow = 1
    private var mapRotation = 1
    private var drawcompass = -1
    private var drawmenu = 1
    private var toggleView = -1
   // private var toggleVehicles = -1
  //  private var toggleVNames = -1
    private var drawgrid = -1
    private var nameToggles = 4
    private var VehicleInfoToggles = 1
    private var ZoomToggles = 1
    ///////////////////////////
    private var scopesToFilter = arrayListOf("")
    private var weaponsToFilter = arrayListOf("")
    private var attachToFilter = arrayListOf("")
    private var level2Filter = arrayListOf("")
    private var level3Filter = arrayListOf("")
    private var healsToFilter = arrayListOf("")
    private var ammoToFilter = arrayListOf("")
    private var throwToFilter = arrayListOf("")
    private var dragging = false
    private var prevScreenX = -1f
    private var prevScreenY = -1f
    private var screenOffsetX = 0f
    private var screenOffsetY = 0f


    private fun windowToMap(x: Float, y: Float) =
            Vector2(selfCoords.x + (x - windowWidth / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetX,
                    selfCoords.y + (y - windowHeight / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetY)

    private fun mapToWindow(x: Float, y: Float) =
            Vector2((x - selfCoords.x - screenOffsetX) / (camera.zoom * windowToMapUnit) + windowWidth / 2.0f,
                    (y - selfCoords.y - screenOffsetY) / (camera.zoom * windowToMapUnit) + windowHeight / 2.0f)

    fun Vector2.mapToWindow() = mapToWindow(x, y)
    fun Vector2.windowToMap() = windowToMap(x, y)


    private fun resetDragged() {
        screenOffsetX = 0f
        screenOffsetY = 0f
    }

    override fun scrolled(amount: Int): Boolean {

        if (camera.zoom >= 0.01f && camera.zoom <= 1f) {
            camera.zoom *= 1.05f.pow(amount)
        } else {
            if (camera.zoom < 0.01f) {
                camera.zoom = 0.01f
            }
            if (camera.zoom > 1f) {
                camera.zoom = 1f
            }
        }

        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when (button) {
            RIGHT -> {
                pinLocation.set(rotatePos(Vector2(screenX.toFloat(), screenY.toFloat()).windowToMap(), -rotateDirRespectToSelf(0f)))
                camera.update()
                println(pinLocation)
                return true
            }
            LEFT -> {
                dragging = true
                prevScreenX = screenX.toFloat()
                prevScreenY = screenY.toFloat()
                return true
            }
            MIDDLE -> {
                resetDragged()
            }
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {

        when (keycode) {

            // reset drag
            SPACE -> {
                resetDragged()
            }

        // Change Player Info
            F1 -> {
                if (nameToggles < 5) {nameToggles += 1}
                if (nameToggles == 5) {nameToggles = 0}
            }

            F5 -> {
                if (VehicleInfoToggles <= 4) {VehicleInfoToggles += 1}
                if (VehicleInfoToggles == 4) {VehicleInfoToggles = 1}
            }

        // Rotation of map
            F6 -> mapRotation = mapRotation * -1


        // Zoom (Loot, Combat, Scout)
            NUMPAD_8 -> {
                if (ZoomToggles <= 4) { ZoomToggles += 1}
                if (ZoomToggles == 4) { ZoomToggles = 1}
                // then
                if (ZoomToggles == 1) {camera.zoom = 1 / 8f}
                // or
                if (ZoomToggles == 2) {camera.zoom = 1 / 12f}
                // or
                if (ZoomToggles == 3) {camera.zoom = 1 / 24f}
            }
        // Other Filter Keybinds
            F2 -> drawcompass = drawcompass * -1
            F3 -> drawgrid = drawgrid * -1

        // Toggle View Line
            F4 -> toggleView = toggleView * -1

        // Toggle Menu5
            F12 -> drawmenu = drawmenu * -1

        // Icon Filter Keybinds
            NUMPAD_1 -> filterWeapon = filterWeapon * -1
            NUMPAD_2 -> filterLvl2 = filterLvl2 * -1
            NUMPAD_3 -> filterHeals = filterHeals * -1
            NUMPAD_4 -> filterThrow = filterThrow * -1
            NUMPAD_5 -> filterAttach = filterAttach * -1
            NUMPAD_6 -> filterScope = filterScope * -1
            NUMPAD_0 -> filterAmmo = filterAmmo * -1

        // Level 2 & 3 Toggle

             /*   F6 -> {
                    if (filterArmorBag <= 4) {
                        filterArmorBag += 1
                    }
                    if (filterArmorBag == 4) {
                        filterArmorBag = 1
                    }
                    // then
                    if (filterArmorBag == 1) {
                        filterLvl3 = 1
                    }
                    // or
                    if (filterArmorBag == 2) {
                        filterLvl2 = 1
                    }
                    // or
                    if (filterArmorBag == 3) {
                        //both?
                        filterLvl2 = 1
                    if (filterArmorBag == 3) {
                        filterLvl3 = 1
                    }
                    }
                }
*/
        // Zoom In/Out || Overrides Max/Min Zoom
            MINUS -> camera.zoom = camera.zoom + 0.00525f
            PLUS -> camera.zoom = camera.zoom - 0.00525f
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!dragging) return false
        with(camera) {
            screenOffsetX += (prevScreenX - screenX.toFloat()) * camera.zoom * 500
            screenOffsetY += (prevScreenY - screenY.toFloat()) * camera.zoom * 500
            prevScreenX = screenX.toFloat()
            prevScreenY = screenY.toFloat()
        }
        return true
    }


    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == LEFT) {
            dragging = false
            return true
        }
        return false
    }

    override fun create() {
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        Gdx.input.inputProcessor = this
        camera = OrthographicCamera(windowWidth, windowHeight)
        with(camera) {
            setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
            zoom = 1 / 4f
            update()
            position.set(mapWidth / 2, mapWidth / 2, 0f)
            update()
        }

        itemCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        fontCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        alarmSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Alarm.wav"))
        hubpanel = Texture(Gdx.files.internal("images/hub_panel.png"))
        bgcompass = Texture(Gdx.files.internal("images/bg_compass.png"))
        menu = Texture(Gdx.files.internal("images/menu.png"))
        hubpanelblank = Texture(Gdx.files.internal("images/hub_panel_blank_long.png"))
        corpseboximage = Texture(Gdx.files.internal("icons/box.png"))
        AirDropAllTheColors = Texture(Gdx.files.internal("icons/AirDropAllTheColors.png"))
        vehicle = Texture(Gdx.files.internal("images/vehicle.png"))
        vehicleo = Texture(Gdx.files.internal("images/vehicleo.png"))
        arrow = Texture(Gdx.files.internal("images/arrow.png"))
        plane = Texture(Gdx.files.internal("images/plane.png"))
        player = Texture(Gdx.files.internal("images/player.png"))
        playersight = Texture(Gdx.files.internal("images/green_view_line.png"))
        teamsight = Texture(Gdx.files.internal("images/teamsight.png"))
        arrowsight = Texture(Gdx.files.internal("images/red_view_line.png"))
        teamarrow = Texture(Gdx.files.internal("images/team.png"))
        parachute = Texture(Gdx.files.internal("images/parachute.png"))
        boat = Texture(Gdx.files.internal("images/boat.png"))
        boato = Texture(Gdx.files.internal("images/boato.png"))
        BikeBLUE = Texture(Gdx.files.internal("images/BikeBLUE.png"))
        BikeRED = Texture(Gdx.files.internal("images/BikeRED.png"))
        Bike3BLUE = Texture(Gdx.files.internal("images/Bike3BLUE.png"))
        Bike3RED = Texture(Gdx.files.internal("images/Bike3RED.png"))
        pickupBLUE = Texture(Gdx.files.internal("images/pickupBLUE.png"))
        pickupRED = Texture(Gdx.files.internal("images/pickupRED.png"))
        BuggyBLUE = Texture(Gdx.files.internal("images/BuggyBLUE.png"))
        BuggyRED = Texture(Gdx.files.internal("images/BuggyRED.png"))

        jetski = Texture(Gdx.files.internal("images/jetski.png"))
        jetskio = Texture(Gdx.files.internal("images/jetskio.png"))
        van = Texture(Gdx.files.internal("images/van.png"))
        vano = Texture(Gdx.files.internal("images/vano.png"))

        grenade = Texture(Gdx.files.internal("images/grenade.png"))
        iconImages = Icons(Texture(Gdx.files.internal("images/item-sprites.png")), 64)
        var cur = 0


        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, floatArrayOf(bgColor.r, bgColor.g, bgColor.b, bgColor.a))
        mapErangel = Texture(Gdx.files.internal("maps/Erangel.png"), null, true).apply {
            setFilter(MipMap, Linear)
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
        }
        mapMiramar = Texture(Gdx.files.internal("maps/Miramar.png"), null, true).apply {
            setFilter(MipMap, Linear)
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
        }

        val generatorHub = FreeTypeFontGenerator(Gdx.files.internal("font/AGENCYFB.TTF"))
        val paramHub = FreeTypeFontParameter()
        paramHub.characters = DEFAULT_CHARS
        paramHub.size = 30
        paramHub.color = WHITE
        hubFont = generatorHub.generateFont(paramHub)
        paramHub.color = Color(1f, 1f, 1f, 0.4f)
        hubFontShadow = generatorHub.generateFont(paramHub)
        paramHub.size = 16
        paramHub.color = WHITE
        espFont = generatorHub.generateFont(paramHub)
        paramHub.color = Color(1f, 1f, 1f, 0.2f)
        espFontShadow = generatorHub.generateFont(paramHub)
        val generatorNumber = FreeTypeFontGenerator(Gdx.files.internal("font/NUMBER.TTF"))
        val paramNumber = FreeTypeFontParameter()
        paramNumber.characters = DEFAULT_CHARS
        paramNumber.size = 24
        paramNumber.color = WHITE
        largeFont = generatorNumber.generateFont(paramNumber)
        val generator = FreeTypeFontGenerator(Gdx.files.internal("font/GOTHICB.TTF"))
        val param = FreeTypeFontParameter()
        param.characters = DEFAULT_CHARS
        param.size = 38
        param.color = WHITE
        largeFont = generator.generateFont(param)
        param.size = 15
        param.color = WHITE
        littleFont = generator.generateFont(param)
        param.color = BLACK
        param.size = 12
        nameFont = generator.generateFont(param)
        param.color = WHITE
        param.size = 6
        itemFont = generator.generateFont(param)
        val compaseColor = Color(0f, 0.95f, 1f, 1f)  //Turquoise1
        param.color = compaseColor
        param.size = 10
        compaseFont = generator.generateFont(param)
        param.color = Color(0f, 0f, 0f, 0.5f)
        compaseFontShadow = generator.generateFont(param)
        param.characters = DEFAULT_CHARS
        param.size = 20
        param.color = WHITE
        littleFont = generator.generateFont(param)
        param.color = Color(0f, 0f, 0f, 0.5f)
        littleFontShadow = generator.generateFont(param)
        param.color = WHITE
        param.size = 12
        menuFont = generator.generateFont(param)
        param.color = GREEN
        param.size = 12
        menuFontOn = generator.generateFont(param)
        param.color = RED
        param.size = 12
        menuFontOFF = generator.generateFont(param)
        param.color = ORANGE
        param.size = 12
        hporange = generator.generateFont(param)
        param.color = GREEN
        param.size = 12
        hpgreen = generator.generateFont(param)
        param.color = RED
        param.size = 12
        hpred = generator.generateFont(param)


        generatorHub.dispose()
        generatorNumber.dispose()
        generator.dispose()
    }

    override fun render() {
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
        Gdx.gl.glClearColor(0.417f, 0.417f, 0.417f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (gameStarted)
            DaMap = if (isErangel) mapErangel else mapMiramar
        else return
        val currentTime = System.currentTimeMillis()
        // Maybe not needed, could be draw error

        actors[selfID]?.apply {
            actors[attachParent ?: return@apply]?.apply {
                selfCoords.set(location.x, location.y)
                selfHeight = location.z
                selfDirection = rotation.y
            }
        }

        val (selfX, selfY) = selfCoords

        //val selfDir = Vector2(selfX, selfY).sub(preSelfCoords)
        //if (selfDir.len() < 1e-8)
        //  selfDir.set(preDirection)

        //move camera
        camera.position.set(selfX + screenOffsetX, selfY + screenOffsetY, 0f)
        camera.update()

        paint(camera.combined) {

            draw(DaMap, 0f, 0f, selfX, selfY,
                    mapWidth, mapWidth,1f, 1f,
                    rotateDirRespectToSelf(0f),
                    0, 0, DaMap.width, DaMap.height,
                    false, true)
        }

        shapeRenderer.projectionMatrix = camera.combined
        Gdx.gl.glEnable(GL20.GL_BLEND)

        drawCircles()

        val typeLocation = EnumMap<Archetype, MutableList<renderInfo>>(Archetype::class.java)
        for ((_, actor) in visualActors)
            typeLocation.compute(actor.type) { _, v ->
                val list = v ?: ArrayList()
                val (centerX, centerY) = actor.location
                val direction = actor.rotation.y
                list.add(tuple5(actor, centerX, centerY, actor.location.z, direction))
                list
            }


       // val zero = numKills.toString()
        paint(fontCamera.combined) {

            // NUMBER PANEL
            val numText = "$NumAlivePlayers"
            layout.setText(hubFont, numText)
            spriteBatch.draw(hubpanel, windowWidth - 130f, windowHeight - 60f)
            hubFontShadow.draw(spriteBatch, "ALIVE", windowWidth - 85f, windowHeight - 29f)
            hubFont.draw(spriteBatch, "$NumAlivePlayers", windowWidth - 110f - layout.width / 2, windowHeight - 29f)
            val teamText = "$NumAliveTeams"


            if (IsTeamMatch) {
                layout.setText(hubFont, teamText)
                spriteBatch.draw(hubpanel, windowWidth - 260f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "TEAM", windowWidth - 215f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$NumAliveTeams", windowWidth - 240f - layout.width / 2, windowHeight - 29f)
            }

            /*
            if (IsTeamMatch) {

                layout.setText(hubFont, zero)
                spriteBatch.draw(hubpanel, windowWidth - 390f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "KILLS", windowWidth - 345f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$zero", windowWidth - 370f - layout.width / 2, windowHeight - 29f)
            } else {
                spriteBatch.draw(hubpanel, windowWidth - 390f + 130f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "KILLS", windowWidth - 345f + 128f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$zero", windowWidth - 370f + 128f - layout.width / 2, windowHeight - 29f)

            }
            */

            // ITEM ESP FILTER PANEL
            spriteBatch.draw(hubpanelblank, 30f, windowHeight - 107f)

            // This is what you were trying to do
            if (filterWeapon != 1)
                espFont.draw(spriteBatch, "WEAPON", 40f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "WEAPON", 39f, windowHeight - 25f)

            if (filterAttach != 1)
                espFont.draw(spriteBatch, "ATTACH", 40f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "ATTACH", 40f, windowHeight - 42f)

            if (filterLvl2 != 1)
                espFont.draw(spriteBatch, "EQUIP", 100f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "EQUIP", 100f, windowHeight - 25f)

            if (filterScope != 1)
                espFont.draw(spriteBatch, "SCOPE", 98f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "SCOPE", 98f, windowHeight - 42f)

            if (filterHeals != 1)
                espFont.draw(spriteBatch, "MEDS", 150f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "MEDS", 150f, windowHeight - 25f)

            if (filterAmmo != 1)
                espFont.draw(spriteBatch, "AMMO", 150f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "AMMO", 150f, windowHeight - 42f)
            if (drawcompass == 1)
                espFont.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
            if (filterThrow != 1)
                espFont.draw(spriteBatch, "THROW", 200f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "THROW", 200f, windowHeight - 25f)


            val pinDistance = (pinLocation.cpy().sub(selfX, selfY).len() / 100).toInt()
            val (x, y) = rotatePosRespectToSelf(pinLocation).mapToWindow()

            safeZoneHint()
            drawPlayerNames(typeLocation[Player])
            //drawMyself(tuple4(null, selfX, selfY, selfDir.angle()))


            val camnum = camera.zoom

            if (drawmenu == 1) {
                spriteBatch.draw(menu, 20f, windowHeight / 2 - 200f)

                // Filters
                if (filterWeapon != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 103f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 103f)

                if (filterLvl2 != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 85f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 85f)

                if (filterHeals != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 67f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 67f)

                if (filterThrow != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 49f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 49f)

                if (filterAttach != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 31f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 31f)

                if (filterScope != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 13f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 13f)

                if (filterAmmo != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -5f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -5f)

                val camvalue = camera.zoom
                when {
                    camvalue <= 0.0100f -> menuFontOFF.draw(spriteBatch, "Max Zoom", 187f, windowHeight / 2 + -27f)
                    camvalue >= 1f -> menuFontOFF.draw(spriteBatch, "Min Zoom", 187f, windowHeight / 2 + -27f)
                    camvalue == 0.2500f -> menuFont.draw(spriteBatch, "Default", 187f, windowHeight / 2 + -27f)
                    camvalue == 0.1250f -> menuFont.draw(spriteBatch, "Scouting", 187f, windowHeight / 2 + -27f)
                    camvalue >= 0.0833f -> menuFont.draw(spriteBatch, "Combat", 187f, windowHeight / 2 + -27f)
                    camvalue <= 0.0417f -> menuFont.draw(spriteBatch, "Looting", 187f, windowHeight / 2 + -27f)

                    else -> menuFont.draw(spriteBatch, ("%.4f").format(camnum), 187f, windowHeight / 2 + -27f)
                }

                // Name Toggles
                val togs = nameToggles
                if (nameToggles != 0)

                    menuFontOn.draw(spriteBatch, "Enabled: $togs", 187f, windowHeight / 2 + -89f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -89f)


                // Compass
                if (drawcompass != 1)

                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -107f)
                else
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -107f)

                // Grid
                if (drawgrid == 1)

                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -125f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -125f)

                if (toggleView == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -143f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -143f)

                if (VehicleInfoToggles < 3)
                    menuFontOn.draw(spriteBatch, "Enabled: $VehicleInfoToggles", 187f, windowHeight / 2 + -161f)
                if (VehicleInfoToggles == 3)
                    menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -161f)

                // DrawMenu == 1 already
                menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -179f)
            }
            // DrawMenu == 0 (Disabled)


            if (drawcompass == 1) {

                spriteBatch.draw(bgcompass, windowWidth / 2 - 168f, windowHeight / 2 - 168f)

                layout.setText(compaseFont, "0")
                compaseFont.draw(spriteBatch, "0", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height + 150)                  // N
                layout.setText(compaseFont, "45")
                compaseFont.draw(spriteBatch, "45", windowWidth / 2 - layout.width / 2 + 104, windowHeight / 2 + layout.height / 2 + 104)          // NE
                layout.setText(compaseFont, "90")
                compaseFont.draw(spriteBatch, "90", windowWidth / 2 - layout.width / 2 + 147, windowHeight / 2 + layout.height / 2)                // E
                layout.setText(compaseFont, "135")
                compaseFont.draw(spriteBatch, "135", windowWidth / 2 - layout.width / 2 + 106, windowHeight / 2 + layout.height / 2 - 106)          // SE
                layout.setText(compaseFont, "180")
                compaseFont.draw(spriteBatch, "180", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height / 2 - 151)                // S
                layout.setText(compaseFont, "225")
                compaseFont.draw(spriteBatch, "225", windowWidth / 2 - layout.width / 2 - 109, windowHeight / 2 + layout.height / 2 - 109)          // SW
                layout.setText(compaseFont, "270")
                compaseFont.draw(spriteBatch, "270", windowWidth / 2 - layout.width / 2 - 153, windowHeight / 2 + layout.height / 2)                // W
                layout.setText(compaseFont, "315")
                compaseFont.draw(spriteBatch, "315", windowWidth / 2 - layout.width / 2 - 106, windowHeight / 2 + layout.height / 2 + 106)          // NW
            }
            littleFont.draw(spriteBatch, "$pinDistance", x, windowHeight - y)


        }

        if (drawgrid == 1) {
            drawGrid()

        }


        // This makes the array empty if the filter is off for performance with an inverted function since arrays are expensive
        scopesToFilter = if (filterScope != 1) {
            arrayListOf("")
        } else {
            arrayListOf("DotSight", "Aimpoint", "Holosight", "CQBSS", "ACOG")
        }


        attachToFilter = if (filterAttach != 1) {
            arrayListOf("")
        } else {
            arrayListOf("AR.Stock", "S.Loops", "CheekPad", "A.Grip", "V.Grip", "U.Ext", "AR.Ext", "S.Ext", "U.ExtQ", "AR.ExtQ", "S.ExtQ", "Choke", "AR.Comp", "FH", "U.Supp", "AR.Supp", "S.Supp")
        }

        weaponsToFilter = if (filterWeapon != 1) {
            arrayListOf("")
        } else {
            arrayListOf("M16A4", "HK416", "Kar98k", "SCAR-L", "AUG", "M249", "AWM", "Groza", "M24", "MK14", "Mini14", "Pan")
        }

        healsToFilter = if (filterHeals != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Bandage", "FirstAid", "MedKit", "Drink", "Pain", "Syringe")
        }

        ammoToFilter = if (filterAmmo != 1) {
            arrayListOf("")
        } else {
            arrayListOf("9mm", "45mm", "556mm", "762mm", "300mm")
        }

        throwToFilter = if (filterThrow != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Grenade", "FlashBang", "SmokeBomb", "Molotov")
        }

        level2Filter = if (filterLvl2 != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Bag2", "Armor2", "Helmet2","Bag3", "Armor3", "Helmet3")
        }
        level3Filter = if (filterLvl3 != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Bag3", "Armor3", "Helmet3")
        }
        paint(itemCamera.combined) {
            //Draw Corpse Icon
            corpseLocation.values.forEach {
                val (x, y) = it
                val (sx, sy) = rotatePosRespectToSelf(Vector2(x + 16, y - 16)).mapToWindow()
                val syFix = windowHeight - sy
                val iconScale = 2f / camera.zoom
                spriteBatch.draw(corpseboximage, sx - iconScale / 2, syFix + iconScale / 2, iconScale, -iconScale,
                        0, 0, 128, 128,
                        false, true)
            }
            //Draw Airdrop Icon
            airDropLocation.values.forEach {

                val (x, y) = it
                val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                val syFix = windowHeight - sy
                val iconScale = 4f / camera.zoom
                spriteBatch.draw(AirDropAllTheColors, sx - iconScale / 2, syFix + iconScale / 2, iconScale, -iconScale,
                        0, 0, 64, 64,
                        false, true)
            }

            val iconScale = 1 / camera.zoom

            droppedItemLocation.values
                    .forEach {
                        val (x, y) = it._1
                        val items = it._2
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val syFix = windowHeight - sy

                        items.forEach {
                            if ((items !in weaponsToFilter && items !in scopesToFilter && items !in attachToFilter && items !in level2Filter
                                    && items !in level3Filter && items !in ammoToFilter && items !in healsToFilter) && items !in throwToFilter
                                    && camera.zoom < 0.0833f && sx > 0 && sx < windowWidth && syFix > 0 && syFix < windowHeight) {
                                iconImages.setIcon(items)

                                draw(iconImages.icon,
                                        sx - iconScale / 2, syFix - iconScale / 2,
                                        iconScale, iconScale)
                            }
                        }
                    }

            drawMyself(tuple5(null, selfX, selfY, selfHeight, selfDirection))
            drawPawns(typeLocation)

        }

        Gdx.gl.glEnable(GL20.GL_BLEND)
        draw(Line) {
            airDropLocation.values.forEach {
                val (x, y) = it
                val airdropcoords = rotatePosRespectToSelf(Vector2(x, y))
                color = YELLOW
                line(selfCoords, airdropcoords)
            }
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }


        val zoom = camera.zoom
        Gdx.gl.glEnable(GL20.GL_BLEND)
        draw(Filled) {
            color = redZoneColor
            circle(rotatePosRespectToSelf(RedZonePosition), RedZoneRadius, 100)

            color = visionColor
            circle(selfCoords, visionRadius, 100)

            color = pinColor
            circle(rotatePosRespectToSelf(pinLocation), pinRadius * zoom, 10)

        }
        drawAttackLine(currentTime)
        Gdx.gl.glDisable(GL20.GL_BLEND)

    }

    private fun rotatePosRespectToSelf(absolute: Vector2): Vector2 {
        if (mapRotation != 1) return absolute

        return rotatePos(absolute, rotateDirRespectToSelf(0f))
    }


    private fun rotatePos(absolute: Vector2, dir: Float): Vector2 {
        val (x, y) = absolute.cpy().sub(selfCoords)

        val dirRad = Math.toRadians(dir.toDouble()).toFloat()

        val diff = Vector2((x * cos(dirRad)) - (y * sin(dirRad)), ((x * sin(dirRad)) + (y * cos(dirRad))))
        // pre-multiplication by rotation matrix in 2D space about selfCoords

        return selfCoords.cpy().add(diff)
    }


    private fun rotateDirRespectToSelf(dir: Float): Float {
        if (mapRotation != 1) return dir

        return (360 - ((selfDirection - dir) + 90))
    }

    private fun drawMyself(actorInfo: renderInfo) {
        val (actor, x, y, z, dir) = actorInfo
        if (actor?.netGUID == selfID) return

        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
        val sDir = rotateDirRespectToSelf(dir)

        if (toggleView == 1) {
            // Just draw them both at the same time to avoid player not drawing ¯\_(ツ)_/¯
            spriteBatch.draw(
                    playersight,
                    sx + 1, windowHeight - sy - 2,
                    2.toFloat() / 2,
                    2.toFloat() / 2,
                    12.toFloat(), 2.toFloat(),
                    10f, 10f,
                    sDir * -1, 0, 0, 512, 64, true, false)
        }
            spriteBatch.draw(
                    player,
                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                    sDir * -1, 0, 0, 64, 64, true, false)
    }


    private fun drawAttackLine(currentTime: Long) {
        while (attacks.isNotEmpty()) {
            val (A, B) = attacks.poll()
            attackLineStartTime.add(Triple(A, B, currentTime))
        }
        if (attackLineStartTime.isEmpty()) return
        draw(Line) {
            val iter = attackLineStartTime.iterator()
            while (iter.hasNext()) {
                val (A, B, st) = iter.next()
                if (A == selfStateID || B == selfStateID) {
                    if (A != B) {
                        val otherGUID = playerStateToActor[if (A == selfStateID) B else A]
                        if (otherGUID == null) {
                            iter.remove()
                            continue
                        }
                        val other = actors[otherGUID]
                        if (other == null || currentTime - st > attackLineDuration) {
                            iter.remove()
                            continue
                        }
                        color = attackLineColor
                        val (xA, yA) = other.location
                        val (xB, yB) = selfCoords
                        line(xA, yA, xB, yB)
                    }
                } else {
                    val actorAID = playerStateToActor[A]
                    val actorBID = playerStateToActor[B]
                    if (actorAID == null || actorBID == null) {
                        iter.remove()
                        continue
                    }
                    val actorA = actors[actorAID]
                    val actorB = actors[actorBID]
                    if (actorA == null || actorB == null || currentTime - st > attackLineDuration) {
                        iter.remove()
                        continue
                    }
                    color = attackLineColor
                    val (xA, yA) = actorA.location
                    val (xB, yB) = actorB.location
                    line(xA, yA, xB, yB)
                }
            }
        }
    }

    private fun drawCircles() {
        Gdx.gl.glLineWidth(2f)
        draw(Line) {
            //vision circle

            val adjustedGasPos = rotatePosRespectToSelf(PoisonGasWarningPosition)
            val adjustedSafePos = rotatePosRespectToSelf(SafetyZonePosition)

            color = safeZoneColor
            circle(adjustedGasPos, PoisonGasWarningRadius, 100)

            color = BLUE
            circle(adjustedSafePos, SafetyZoneRadius, 100)

            if (PoisonGasWarningPosition.len() > 0) {
                color = safeDirectionColor
                line(selfCoords, adjustedGasPos)
            }

        }

        Gdx.gl.glLineWidth(1f)
    }


    private fun drawPawns(typeLocation: EnumMap<Archetype, MutableList<renderInfo>>) {
        val iconScale = 2f / camera.zoom
        for ((type, actorInfos) in typeLocation) {

            when (type) {
                TwoSeatBoat -> actorInfos?.forEach {

                    if (VehicleInfoToggles < 3) {

                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "JSKI", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                jetski,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                nDir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    jetskio,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                    nDir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                SixSeatBoat -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "BOAT", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                boat,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                nDir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    boato,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                    nDir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                TwoSeatBike -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)

                        if (VehicleInfoToggles == 2)compaseFont.draw(spriteBatch, "BIKE", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                BikeBLUE,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 3, iconScale / 3,
                                nDir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    BikeRED,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 3, iconScale / 3,
                                    nDir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                TwoSeatCar -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "BUGGY", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                BuggyBLUE,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                nDir * -1, 0, 0, 64, 64, false, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    BuggyRED,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    nDir * -1, 0, 0, 64, 64, false, false
                            )
                        }
                    }
                }
                ThreeSeatCar -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "BIKE", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                Bike3BLUE,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2, 4.toFloat() / 2,
                                4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                nDir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    Bike3RED,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2, 4.toFloat() / 2,
                                    4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                    nDir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }

                }
                FourSeatDU -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "CAR", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                vehicle,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                nDir * -1, 0, 0, 128, 128, false, false
                        )
                        // Draw over top whenever some is in car
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    vehicleo,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    nDir * -1, 0, 0, 128, 128, false, false
                            )
                        }
                    }

                }
                FourSeatP -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)

                        if (VehicleInfoToggles == 2)compaseFont.draw(spriteBatch, "PICKUP", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                pickupBLUE,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                nDir * -1, 0, 0, 64, 64, true, false
                        )

                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    pickupRED,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    nDir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                SixSeatCar -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "VAN", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                van,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                nDir * -1, 0, 0, 64, 64, false, false
                        )

                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    vano,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    nDir * -1, 0, 0, 64, 64, false, false
                            )
                        }
                    }
                }
                Player -> actorInfos?.forEach {

                    for ((_, _) in typeLocation) {
                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)
                        val playerStateGUID = actorWithPlayerState[actor!!.netGUID] ?: return@forEach
                        val PlayerState= actors[playerStateGUID] as? PlayerState ?: return@forEach
                        val teamNumber = PlayerState.teamNumber
                        val attach=actor.attachChildren.firstOrNull()
                        val teamId=isTeamMate(actor)

                        if (teamId > 0 ) {

                            // Can't wait for the "Omg Players don't draw issues
                            if (toggleView == 1) {
                                spriteBatch.draw(
                                        teamsight,
                                        sx + 1, windowHeight - sy - 2,
                                        2.toFloat() / 2,
                                        2.toFloat() / 2,
                                        12.toFloat(), 2.toFloat(),
                                        10f, 10f,
                                        nDir * -1, 0, 0, 512, 64, true, false)
                            }

                            spriteBatch.draw(
                                    teamarrow,
                                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                                    nDir * -1, 0, 0, 64, 64, true, false)


                        } else {

                            if (toggleView == 1) {
                                spriteBatch.draw(
                                        arrowsight,
                                        sx + 1, windowHeight - sy - 2,
                                        2.toFloat() / 2,
                                        2.toFloat() / 2,
                                        12.toFloat(), 2.toFloat(),
                                        10f, 10f,
                                        nDir * -1, 0, 0, 512, 64, true, false)
                            }

                            spriteBatch.draw(
                                    arrow,
                                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                                    nDir * -1, 0, 0, 64, 64, true, false)

                        }
                    }

                }
                Parachute -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {

                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)

                        spriteBatch.draw(
                                parachute,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 8f, 8f,
                                nDir * -1, 0, 0, 128, 128, true, false)

                    }
                }
                Plane -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {

                        val (actor, x, y, z, dir) = it
                        val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                        val nDir = rotateDirRespectToSelf(dir)

                        spriteBatch.draw(
                                plane,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 5.toFloat(), 5.toFloat(), 10f, 10f,
                                nDir * -1, 0, 0, 64, 64, true, false)

                    }
                }
                Grenade -> actorInfos?.forEach {
                    val (actor, x, y, z, dir) = it
                    val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                    val nDir = rotateDirRespectToSelf(dir)

                    spriteBatch.draw(
                            grenade,
                            sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                            4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                            nDir * -1, 0, 0, 16, 16, true, false)
                }

                else -> {
                    //nothing
                }
            }

        }
    }

    private fun drawPlayerNames(players: MutableList<renderInfo>?) {

        if (nameToggles > 0) {

            players?.forEach {

                val zoom = camera.zoom
                val (actor, x, y, z, _) = it
                if (actor != null && actor.isACharacter) {
                    // actor!!

                    val (sx, sy) = rotatePosRespectToSelf(Vector2(x, y)).mapToWindow()
                    val dir = Vector2(x - selfCoords.x, y - selfCoords.y)
                    val distance = (dir.len() / 100).toInt()
                    val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
                    val PlayerState = actors[playerStateGUID] as? PlayerState ?: return@forEach
                    val name = PlayerState.name
                    val teamNumber = PlayerState.teamNumber
                    val numKills = PlayerState.numKills
                    val angle = ((dir.angle() + 90) % 360).toInt()
                    val health = actorHealth[actor.netGUID] ?: 100f
                    val equippedWeapons = actorHasWeapons[actor.netGUID]
                    val df = DecimalFormat("###.#")


                    val deltaHeight = (if (z > selfHeight) "+" else "-") + df.format(abs(z - selfHeight) / 100f)
                    var weapon: String? = ""

                        if (equippedWeapons != null) {
                            for (w in equippedWeapons) {
                                val a = weapons[w ?: continue] ?: continue
                            val result = a.typeName.split("_")
                            weapon += " - " + result[2].substring(4) + "\n"
                            }
                        }
                    var items = ""
                        for (element in PlayerState.equipableItems) {
                            if (element == null || element._1.isBlank()) continue
                        items += "${element._1}->${element._2.toInt()}\n"
                        }
                        for (element in PlayerState.castableItems) {
                            if (element == null || element._1.isBlank()) continue
                        items += "${element._1}->${element._2}\n"
                        }

                    val drawName = nameToggles >= 1
                    val drawPos = nameToggles >= 2
                    val drawHealth = nameToggles >= 3
                    val drawPVP = nameToggles >= 4


                    if (actor is Character) {
                        when {
                            actor.isGroggying -> {
                                hpred.draw(spriteBatch, "DOWNED", sx + 20, windowHeight - sy)
                            }
                            actor.isReviving -> {
                                hporange.draw(spriteBatch, "GETTING REVIVED", sx + 20, windowHeight - sy)
                            }
                            else -> {
                                if (drawHealth) {
                                    val healthText = health
                                    when {
                                        healthText > 66f -> hpgreen.draw(spriteBatch, "\n${df.format(health)}%", sx + 20, windowHeight - sy)
                                        healthText > 33f -> hporange.draw(spriteBatch, "\n${df.format(health)}%", sx + 20, windowHeight - sy)
                                        else -> hpred.draw(spriteBatch, "\n${df.format(health)}%", sx + 20, windowHeight - sy)
                                    }
                                }
                                }
                            }
                        }
                    nameFont.draw(spriteBatch,
                            (if (drawName) "$name\n" else "") +
                                    (if (drawPos) "$angle°\n${distance}m | ${deltaHeight}m\n" else "") +
                                    (if (drawHealth) "\n" else "") +
                                    (if (drawPVP) "$numKills Kills\n$weapon" else ""),
                            sx + 20, windowHeight - sy + 30)

                    }
                }
            }
        }



    private fun drawGrid() {
        draw(Filled) {
            val unit = gridWidth / 8
            val unit2 = unit / 10
            color = BLACK
            //thin grid
            for (i in 0..7)
                for (j in 0..9) {
                    rectLine(0f, i * unit + j * unit2, gridWidth, i * unit + j * unit2, 100f)
                    rectLine(i * unit + j * unit2, 0f, i * unit + j * unit2, gridWidth, 100f)
                }
            color = GRAY
            //thick grid
            for (i in 0..7) {
                rectLine(0f, i * unit, gridWidth, i * unit, 500f)
                rectLine(i * unit, 0f, i * unit, gridWidth, 500f)
            }
        }
    }


    private var lastPlayTime = System.currentTimeMillis()
    private fun safeZoneHint() {
        if (PoisonGasWarningPosition.len() > 0) {
            val dir = PoisonGasWarningPosition.cpy().sub(selfCoords)
            val road = dir.len() - PoisonGasWarningRadius
            if (road > 0) {
                val runningTime = (road / runSpeed).toInt()
                val (x, y) = dir.nor().scl(road).add(selfCoords).mapToWindow()
                littleFont.draw(spriteBatch, "$runningTime", x, windowHeight - y)
                val remainingTime = (TotalWarningDuration - ElapsedWarningDuration).toInt()
                if (remainingTime == 60 && runningTime > remainingTime) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastPlayTime > 10000) {
                        lastPlayTime = currentTime
                        alarmSound.play()
                    }
                }
            }
        }
    }

    private inline fun draw(type: ShapeType, draw: ShapeRenderer.() -> Unit) {
        shapeRenderer.apply {
            begin(type)
            draw()
            end()
        }
    }

    private inline fun paint(matrix: Matrix4, paint: SpriteBatch.() -> Unit) {
        spriteBatch.apply {
            projectionMatrix = matrix
            begin()
            paint()
            end()
        }
    }

    private fun ShapeRenderer.circle(loc: Vector2, radius: Float, segments: Int) {
        circle(loc.x, loc.y, radius, segments)
    }


    private fun isTeamMate(actor:Actor?):Int {
        val playerStateGUID=actorWithPlayerState[actor?.netGUID ?: return 0] ?: return 0
        val playerState=actors[playerStateGUID] as? PlayerState ?: return 0
        return team[playerState.name] ?: 0
    }

    override fun resize(width: Int, height: Int) {
        windowWidth = width.toFloat()
        windowHeight = height.toFloat()
        camera.setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
        itemCamera.setToOrtho(false, windowWidth, windowHeight)
        fontCamera.setToOrtho(false, windowWidth, windowHeight)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
        deregister(this)
        alarmSound.dispose()
        nameFont.dispose()
        largeFont.dispose()
        littleFont.dispose()
        menuFont.dispose()
        menuFontOn.dispose()
        menuFontOFF.dispose()
        hporange.dispose()
        hpgreen.dispose()
        hpred.dispose()
        corpseboximage.dispose()
        AirDropAllTheColors.dispose()
        vehicle.dispose()
        iconImages.iconSheet.dispose()
        compaseFont.dispose()
        compaseFontShadow.dispose()

        var cur = 0
        spriteBatch.dispose()
        shapeRenderer.dispose()
    }
}
