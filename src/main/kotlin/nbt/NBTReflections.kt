package nbt

import StickyWallet
import org.bukkit.inventory.ItemStack
import java.lang.Exception

object NBTReflections {

    private val nmsVersion: String
        get() = StickyWallet.instance.nmsManager.getVersionString()

    private fun getCraftItemStack(): Class<*>? {
        return try {
            Class.forName("org.bukkit.craftbukkit.${nmsVersion}.inventory.CraftItemStack")
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private fun getNewNBTTag(): Any? {
        return try {
            val c = Class.forName("net.minecraft.server.${nmsVersion}.NBTTagCompound")
            c.getDeclaredConstructor().newInstance()
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private fun setNBTTag(nbtTag: Any, nmsItem: Any): Any? {
        return try {
            val method = nmsItem.javaClass.getMethod("setTag", nbtTag.javaClass)
            method.invoke(nmsItem, nbtTag)
            nmsItem
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private fun getNMSItemStack(item: ItemStack): Any? {
        val cis = getCraftItemStack()!!
        return try {
            val method = cis.getMethod("asNMSCopy", ItemStack::class.java)
            return method.invoke(cis, item)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private fun getBukkitItemStack(item: Any): ItemStack? {
        val cis = getCraftItemStack()!!
        return try {
            val method = cis.getMethod("asBukkitCopy", item.javaClass)
            return method.invoke(cis, item) as ItemStack
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private fun getNBTTagCompound(nmsItem: Any): Any? {
        val c = nmsItem.javaClass
        return try {
            val method = c.getMethod("getTag")
            return method.invoke(nmsItem)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun setString(item: ItemStack, key: String, value: String): ItemStack? {
        var nmsItem = this.getNMSItemStack(item) ?: return null
        val nbtTag = this.getNBTTagCompound(nmsItem) ?: this.getNewNBTTag()!!

        return try {
            val method = nbtTag.javaClass.getMethod("setString", String::class.java, String::class.java)
            method.invoke(nbtTag, key, value)
            nmsItem = setNBTTag(nbtTag, nmsItem)!!
            return getBukkitItemStack(nmsItem)!!
        } catch (ex: Exception) {
            ex.printStackTrace()
            item
        }
    }

    fun getString(item: ItemStack, key: String): String? {
        val nmsItem = this.getNMSItemStack(item) ?: return null
        val nbtTag = this.getNBTTagCompound(nmsItem) ?: this.getNewNBTTag()!!

        return try {
            val method = nbtTag.javaClass.getMethod("getString", String::class.java)
            return method.invoke(nbtTag, key) as String
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun setInt(item: ItemStack, key: String, value: Int): ItemStack? {
        var nmsItem = this.getNMSItemStack(item) ?: return null
        val nbtTag = this.getNBTTagCompound(nmsItem) ?: this.getNewNBTTag()!!

        return try {
            val method = nbtTag.javaClass.getMethod("setInt", String::class.java, Int::class.java)
            method.invoke(nbtTag, key, value)
            nmsItem = setNBTTag(nbtTag, nmsItem)!!
            return getBukkitItemStack(nmsItem)!!
        } catch (ex: Exception) {
            ex.printStackTrace()
            item
        }
    }

    fun getInt(item: ItemStack, key: String): Int? {
        val nmsItem = this.getNMSItemStack(item) ?: return null
        val nbtTag = this.getNBTTagCompound(nmsItem) ?: this.getNewNBTTag()!!

        return try {
            val method = nbtTag.javaClass.getMethod("getInt", String::class.java)
            return method.invoke(nbtTag, key) as Int
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun setDouble(item: ItemStack, key: String, value: Double): ItemStack? {
        var nmsItem = this.getNMSItemStack(item) ?: return null
        val nbtTag = this.getNBTTagCompound(nmsItem) ?: this.getNewNBTTag()!!

        return try {
            val method = nbtTag.javaClass.getMethod("setDouble", String::class.java, Double::class.java)
            method.invoke(nbtTag, key, value)
            nmsItem = setNBTTag(nbtTag, nmsItem)!!
            return getBukkitItemStack(nmsItem)!!
        } catch (ex: Exception) {
            ex.printStackTrace()
            item
        }
    }

    fun getDouble(item: ItemStack, key: String): Double? {
        val nmsItem = this.getNMSItemStack(item) ?: return null
        val nbtTag = this.getNBTTagCompound(nmsItem) ?: this.getNewNBTTag()!!

        return try {
            val method = nbtTag.javaClass.getMethod("getDouble", String::class.java)
            return method.invoke(nbtTag, key) as Double
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun setBoolean(item: ItemStack, key: String, value: Boolean): ItemStack? {
        var nmsItem = this.getNMSItemStack(item) ?: return null
        val nbtTag = this.getNBTTagCompound(nmsItem) ?: this.getNewNBTTag()!!

        return try {
            val method = nbtTag.javaClass.getMethod("setBoolean", String::class.java, Boolean::class.java)
            method.invoke(nbtTag, key, value)
            nmsItem = setNBTTag(nbtTag, nmsItem)!!
            return getBukkitItemStack(nmsItem)!!
        } catch (ex: Exception) {
            ex.printStackTrace()
            item
        }
    }

    fun getBoolean(item: ItemStack, key: String): Boolean? {
        val nmsItem = this.getNMSItemStack(item) ?: return null
        val nbtTag = this.getNBTTagCompound(nmsItem) ?: this.getNewNBTTag()!!

        return try {
            val method = nbtTag.javaClass.getMethod("getBoolean", String::class.java)
            return method.invoke(nbtTag, key) as Boolean
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun hasKey(item: ItemStack, key: String): Boolean? {
        val nmsItem = this.getNMSItemStack(item) ?: return null
        val nbtTag = this.getNBTTagCompound(nmsItem) ?: this.getNewNBTTag()!!

        return try {
            val method = nbtTag.javaClass.getMethod("hasKey", String::class.java)
            return method.invoke(nbtTag, key) as Boolean
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

}