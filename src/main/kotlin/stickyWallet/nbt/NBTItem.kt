package stickyWallet.nbt

import org.bukkit.inventory.ItemStack

class NBTItem(bukkitItem: ItemStack) {

    var bukkitItem = bukkitItem.clone()
        private set

    fun setString(key: String, value: String) {
        val newEntry = NBTReflections.setString(this.bukkitItem, key, value)
        this.bukkitItem = newEntry ?: throw Error("Failed to set NBT Tag $key to \"$value\"")
    }

    fun getString(key: String) = NBTReflections.getString(this.bukkitItem, key)

    fun setInt(key: String, value: Int) {
        val newEntry = NBTReflections.setInt(this.bukkitItem, key, value)
        this.bukkitItem = newEntry ?: throw Error("Failed to set NBT Tag $key to \"$value\"")
    }

    fun getInt(key: String) = NBTReflections.getInt(this.bukkitItem, key)

    fun setDouble(key: String, value: Double) {
        val newEntry = NBTReflections.setDouble(this.bukkitItem, key, value)
        this.bukkitItem = newEntry ?: throw Error("Failed to set NBT Tag $key to \"$value\"")
    }

    fun getDouble(key: String) = NBTReflections.getDouble(this.bukkitItem, key)

    fun setBoolean(key: String, value: Boolean) {
        val newEntry = NBTReflections.setBoolean(this.bukkitItem, key, value)
        this.bukkitItem = newEntry ?: throw Error("Failed to set NBT Tag $key to \"$value\"")
    }

    fun getBoolean(key: String) = NBTReflections.getBoolean(this.bukkitItem, key)

    fun hasKey(key: String) = NBTReflections.hasKey(this.bukkitItem, key)
}
