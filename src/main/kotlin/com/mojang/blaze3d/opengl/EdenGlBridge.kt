package com.mojang.blaze3d.opengl

import com.mojang.blaze3d.systems.GpuDevice
import dev.eden.mixin.GpuDeviceAccessor

object EdenGlBridge {
	fun directStateAccess(device: GpuDevice): DirectStateAccess? {
		val backend = (device as GpuDeviceAccessor).edenBackend()
		return if (backend is GlDevice) backend.directStateAccess() else null
	}
}
