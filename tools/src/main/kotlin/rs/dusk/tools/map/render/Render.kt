package rs.dusk.tools.map.render

import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.cache.config.decoder.OverlayDecoder
import rs.dusk.cache.config.decoder.UnderlayDecoder
import rs.dusk.cache.definition.decoder.TextureDecoder
import rs.dusk.engine.client.cacheConfigModule
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectMapDecoder
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.obj.objectMapDecoderModule
import rs.dusk.engine.map.region.obj.xteaModule
import rs.dusk.engine.map.region.tile.TileData
import rs.dusk.engine.map.region.tile.TileDecoder
import rs.dusk.tools.map.render.raster.QuarterRasterImage
import rs.dusk.tools.map.render.raster.Raster
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.sin

object Render {

    val TILE_TYPE_HEIGHT_OVERRIDE = arrayOf(
        booleanArrayOf(true, true, true, true, true, true, true, true, true, true, true, true, true),
        booleanArrayOf(true, true, true, false, false, false, true, true, false, false, false, false, true),
        booleanArrayOf(true, false, false, false, false, true, true, true, false, false, false, false, false),
        booleanArrayOf(false, false, true, true, true, true, false, false, false, false, false, false, false),
        booleanArrayOf(true, true, true, true, true, true, false, false, false, false, false, false, false),
        booleanArrayOf(true, true, true, false, false, true, true, true, false, false, false, false, false),
        booleanArrayOf(true, true, false, false, false, true, true, true, false, false, false, false, true),
        booleanArrayOf(true, true, false, false, false, false, false, true, false, false, false, false, false),
        booleanArrayOf(false, true, true, true, true, true, true, true, false, false, false, false, false),
        booleanArrayOf(true, false, false, false, true, true, true, true, true, true, false, false, false),
        booleanArrayOf(true, true, true, true, true, false, false, false, true, true, false, false, false),
        booleanArrayOf(true, true, true, false, false, false, false, false, false, false, true, true, false),
        BooleanArray(13),
        booleanArrayOf(true, true, true, true, true, true, true, true, true, true, true, true, true),
        BooleanArray(13)
    )

    fun hslToPaletteIndex(luminance: Int, saturation: Int, hue: Int): Int {
        var saturation = saturation
        if (luminance <= 243) {
            if (luminance <= 217) {
                if (luminance > 192) {
                    saturation = saturation shr 2
                } else if (luminance > 179) {
                    saturation = saturation shr 1
                }
            } else {
                saturation = saturation shr 3
            }
        } else {
            saturation = saturation shr 4
        }
        return (luminance shr 1) + (saturation shr 5 shl 7) + (0xff and hue shr 2 shl 10)
    }

    var COSINE = IntArray(16384)
    var SINE = IntArray(16384)

    init {
        val d = 3.834951969714103E-4
        for (i in 0..16383) {
            SINE[i] = (16384.0 * sin(i.toDouble() * d)).toInt()
            COSINE[i] = (16384.0 * cos(d * i.toDouble())).toInt()
        }
    }

    fun smoothNoise(y: Int, x: Int): Int {
        val corners = perlin(-1 + y, x + -1) - (-perlin(y - 1, 1 + x) + (-perlin(1 + y, -1 + x) - perlin(y + 1, 1 + x)))
        val sides = perlin(y, x - 1) + (perlin(y, x + 1) + perlin(-1 + y, x)) + perlin(y + 1, x)
        val center = perlin(y, x)
        return sides / 8 + (corners / 16 + center / 4)
    }

    fun perlin(y: Int, x: Int): Int {
        var n = 57 * y + x
        n = n xor (n shl 13)
        val i_3_ = 0x7fffffff and 1376312589 + n * (15731 * (n * n) + 789221)
        return i_3_ shr 19 and 0xff
    }

    val size = 4

    val tileWater = false
    val tileLighting = false
    val sceneryShadows = -1
    var groundBlending = -1
    val aBoolean8715 = true
    val aBoolean10563 = false
    var waterMovement = false//Reapply water stuff (Requires method2810)

    val tileYOffsets = intArrayOf(0, 0, 0, 256, 512, 512, 512, 256, 256, 384, 128, 128, 256)
    val tileXOffsets = intArrayOf(0, 256, 512, 512, 512, 256, 0, 0, 128, 256, 128, 384, 256)

    val firstTileTypeVertices = arrayOf(
        intArrayOf(0, 2),
        intArrayOf(0, 2),
        intArrayOf(0, 0, 2),
        intArrayOf(2, 0, 0),
        intArrayOf(0, 2, 0),
        intArrayOf(0, 0, 2),
        intArrayOf(0, 5, 1, 4),
        intArrayOf(0, 4, 4, 4),
        intArrayOf(4, 4, 4, 0),
        intArrayOf(6, 6, 6, 2, 2, 2),
        intArrayOf(2, 2, 2, 6, 6, 6),
        intArrayOf(0, 11, 6, 6, 6, 4),
        intArrayOf(0, 2),
        intArrayOf(0, 4, 4, 4),
        intArrayOf(0, 4, 4, 4)
    )
    var thirdTileTypeVertices = arrayOf(
        intArrayOf(6, 6),
        intArrayOf(6, 6),
        intArrayOf(6, 5, 5),
        intArrayOf(5, 6, 5),
        intArrayOf(5, 5, 6),
        intArrayOf(6, 5, 5),
        intArrayOf(5, 0, 4, 1),
        intArrayOf(7, 7, 1, 2),
        intArrayOf(7, 1, 2, 7),
        intArrayOf(8, 9, 4, 0, 8, 9),
        intArrayOf(0, 8, 9, 8, 9, 4),
        intArrayOf(11, 0, 10, 11, 4, 2),
        intArrayOf(6, 6),
        intArrayOf(7, 7, 1, 2),
        intArrayOf(7, 7, 1, 2)
    )
    var secondTileTypeVertices = arrayOf(
        intArrayOf(2, 4),
        intArrayOf(2, 4),
        intArrayOf(5, 2, 4),
        intArrayOf(4, 5, 2),
        intArrayOf(2, 4, 5),
        intArrayOf(5, 2, 4),
        intArrayOf(1, 6, 2, 5),
        intArrayOf(1, 6, 7, 1),
        intArrayOf(6, 7, 1, 1),
        intArrayOf(0, 8, 9, 8, 9, 4),
        intArrayOf(8, 9, 4, 0, 8, 9),
        intArrayOf(2, 10, 0, 10, 11, 11),
        intArrayOf(2, 4),
        intArrayOf(1, 6, 7, 1),
        intArrayOf(1, 6, 7, 1)
    )
    val underlaySizes = intArrayOf(0, 1, 2, 2, 1, 1, 2, 3, 1, 3, 3, 4, 2, 0, 4)
    val overlaySizes = intArrayOf(2, 1, 1, 1, 2, 2, 2, 1, 3, 3, 3, 2, 0, 4, 0)

    // TODO duplicate of
    class RegionLoader(private val cache: Cache, private val objectDecoder: GameObjectMapDecoder, private val tileDecoder: TileDecoder, private val xteas: Xteas, val width: Int, val height: Int) {
        val tiles = Array(4) { Array(64 * width + 1) { Array<TileData?>(64 * height + 1) { null } } }

        fun loadAll() {
            for(regionX in 0 until width) {
                for(regionY in 0 until height) {
                    load(Region.getId(regionX, regionY))
                }
            }
        }

        fun load(
            regionId: Int,
            regionX: Int = regionId shr 8,
            regionY: Int = regionId and 0xff
        ) {
            val mapData = cache.getFile(5, "m${regionX}_${regionY}") ?: return
//            val xtea = xteas[regionId]
//            val locationData = cache.getFile(5, "l${regionX}_${regionY}", xtea)
//
//            if (locationData == null) {
//                println("Missing xteas for region $regionId [${xtea?.toList()}].")
//                return
//            }

            tileDecoder.read(mapData, regionX, regionY, tiles)
//            val objects = objectDecoder.read(locationData, tiles)

        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, cacheConfigModule, xteaModule, objectMapDecoderModule)
        }.koin

        val cache: Cache = koin.get()
        val xteas: Xteas = koin.get()
        val tileDecoder = TileDecoder()
        val objDecoder: GameObjectMapDecoder = koin.get()
        val overlayDefinitions: OverlayDecoder = koin.get()
        val underlayDefinitions: UnderlayDecoder = koin.get()
        val textureDefinitions: TextureDecoder = koin.get()
        val horizontalRegions = 255
        val verticalRegions = 255
        val regionLoader = RegionLoader(cache, objDecoder, tileDecoder, xteas, horizontalRegions, verticalRegions)

        var start = System.currentTimeMillis()
        regionLoader.loadAll()
        println("Loading all regions took ${System.currentTimeMillis() - start}ms")
        start = System.currentTimeMillis()
        val blend = 0//5 = true 0 = false
        val width = horizontalRegions * 64 + blend
        val height = verticalRegions * 64 + blend
        val settings = MapTileSettings(width, height, 2, underlayDefinitions, overlayDefinitions, textureDefinitions, tiles = regionLoader.tiles)

//        settings.loadTiles(Region(regionId).x, Region(regionId).y, regionLoader, 0, -blend)
        val currentPlane = 0
        val planes = settings.loadSettings()
        println("Loading all settings took ${System.currentTimeMillis() - start}ms")
        start = System.currentTimeMillis()
        settings.loadUnderlays(null, planes)
        println("Loading underlays took ${System.currentTimeMillis() - start}ms")
        start = System.currentTimeMillis()
        val img1 = BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_ARGB)
        val index = 2
        val quad = QuarterRasterImage(img1, index)
        val raster = Raster(quad)
        val useUnderlay = Array(width) { BooleanArray(height) }
        for (plane in currentPlane until planes.size) {
            for (dx in 0 until width) {
                for (dy in 0 until height) {
                    useUnderlay[dx][dy] = settings.useUnderlay(dy, currentPlane, dx, plane)
                }
            }
            planes[plane].drawTiles(0, 0, width, height, useUnderlay, raster, IntArray(width), IntArray(height))//Tiles
        }
        println("Generated in ${System.currentTimeMillis() - start}ms")
        start = System.currentTimeMillis()
        try {
            ImageIO.write(img1, "png", File("./quad${index + 1}.png"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        println("Written in ${System.currentTimeMillis() - start}ms")
    }
}
