package com.mojang.renderpearl.backend.opengl

import com.mojang.renderpearl.api.device.GpuDevice
import com.mojang.renderpearl.api.textures.GpuTextureView
import dev.eden.mixin.GpuDeviceAccessor

object EdenGlBridge {
	fun framebuffer(device: GpuDevice, color: GpuTextureView, depth: GpuTextureView): Int? {
		val backend = (device as GpuDeviceAccessor).edenBackend()
		if (backend !is GlDevice || color !is FrameBufferAttachment || depth !is FrameBufferAttachment) {
			return null
		}
		return backend.frameBufferCache().getFbo(backend.directStateAccess(), listOf(color), depth)
	}
}
