package dev.eden.client.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.renderpearl.backend.opengl.EdenGlBridge
import com.mojang.renderpearl.backend.opengl.GlConst
import com.mojang.renderpearl.backend.opengl.GlStateManager
import dev.eden.mixin.GuiGraphicsExtractorAccessor
import dev.eden.mixin.PictureInPictureRendererAccessor
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState
import org.lwjgl.opengl.GL33C

class EdenNvgPipRenderer : PictureInPictureRenderer<EdenNvgPipRenderer.NvgRenderState>() {
	@Suppress("CAST_NEVER_SUCCEEDS")
	override fun renderToTexture(state: NvgRenderState, poseStack: PoseStack, submitNodeCollector: SubmitNodeCollector) {
		val accessor = this as PictureInPictureRendererAccessor
		val colorTexture = accessor.edenColorTextureView() ?: return
		val depthTexture = accessor.edenDepthTextureView() ?: return
		val framebuffer = EdenGlBridge.framebuffer(RenderSystem.getDevice(), colorTexture, depthTexture) ?: return
		val width = colorTexture.getWidth(0)
		val height = colorTexture.getHeight(0)
		val previousState = OpenGlState.capture()

		try {
			GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, framebuffer)
			GlStateManager._viewport(0, 0, width, height)

			GL33C.glActiveTexture(GL33C.GL_TEXTURE0)
			GL33C.glBindSampler(0, 0)
			EdenNvgRenderer.beginFrame(width.toFloat(), height.toFloat())
			try {
				state.renderContent()
			} finally {
				EdenNvgRenderer.endFrame()
			}
		} finally {
			/*
			 * NanoVG uses OpenGL directly, outside RenderPearl's state tracking. In
			 * 26.3, leaving its font texture, sampler, shader, or VAO bound can make
			 * the first subsequently-rendered item sample an empty texture until the
			 * next resource upload repairs the state.
			 */
			previousState.restore()
		}

		GlStateManager._disableDepthTest()
		GlStateManager._disableCull()
		// Force RenderPearl's blend-state cache back in sync after NanoVG's raw GL calls.
		GlStateManager._disableBlend(0)
		GlStateManager._enableBlend(0)
		GlStateManager._blendFuncSeparate(770, 771, 1, 0)
	}

	override fun getTranslateY(height: Int, windowScaleFactor: Int): Float = height / 2.0f

	override fun getRenderStateClass(): Class<NvgRenderState> = NvgRenderState::class.java

	override fun getTextureLabel(): String = "eden_nvg_renderer"

	private data class OpenGlState(
		val framebuffer: Int,
		val viewport: IntArray,
		val activeTexture: Int,
		val texture2d: Int,
		val sampler: Int,
		val program: Int,
		val vertexArray: Int,
		val arrayBuffer: Int,
		val uniformBuffer: Int,
	) {
		fun restore() {
			// This binding was changed through GlStateManager, so restore it through
			// the same path to keep RenderPearl's framebuffer cache synchronized.
			GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, framebuffer)
			GL33C.glViewport(viewport[0], viewport[1], viewport[2], viewport[3])
			GL33C.glUseProgram(program)
			GL33C.glBindVertexArray(vertexArray)
			GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, arrayBuffer)
			GL33C.glBindBuffer(GL33C.GL_UNIFORM_BUFFER, uniformBuffer)
			GL33C.glActiveTexture(GL33C.GL_TEXTURE0)
			GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, texture2d)
			GL33C.glBindSampler(0, sampler)
			GL33C.glActiveTexture(activeTexture)
		}

		companion object {
			fun capture(): OpenGlState {
				val viewport = IntArray(4)
				GL33C.glGetIntegerv(GL33C.GL_VIEWPORT, viewport)
				val activeTexture = GL33C.glGetInteger(GL33C.GL_ACTIVE_TEXTURE)
				GL33C.glActiveTexture(GL33C.GL_TEXTURE0)
				val texture2d = GL33C.glGetInteger(GL33C.GL_TEXTURE_BINDING_2D)
				val sampler = GL33C.glGetIntegeri(GL33C.GL_SAMPLER_BINDING, 0)
				GL33C.glActiveTexture(activeTexture)
				return OpenGlState(
					GL33C.glGetInteger(GL33C.GL_DRAW_FRAMEBUFFER_BINDING),
					viewport,
					activeTexture,
					texture2d,
					sampler,
					GL33C.glGetInteger(GL33C.GL_CURRENT_PROGRAM),
					GL33C.glGetInteger(GL33C.GL_VERTEX_ARRAY_BINDING),
					GL33C.glGetInteger(GL33C.GL_ARRAY_BUFFER_BINDING),
					GL33C.glGetInteger(GL33C.GL_UNIFORM_BUFFER_BINDING),
				)
			}
		}
	}

	data class NvgRenderState(
		private val x: Int,
		private val y: Int,
		private val width: Int,
		private val height: Int,
		private val scissor: ScreenRectangle?,
		private val bounds: ScreenRectangle,
		val renderContent: () -> Unit,
	) : PictureInPictureRenderState {
		override fun scale(): Float = 1.0f
		override fun x0(): Int = x
		override fun y0(): Int = y
		override fun x1(): Int = x + width
		override fun y1(): Int = y + height
		override fun scissorArea(): ScreenRectangle? = scissor
		override fun bounds(): ScreenRectangle = bounds
	}

	companion object {
		fun draw(
			graphics: GuiGraphicsExtractor,
			x: Int,
			y: Int,
			width: Int,
			height: Int,
			renderContent: () -> Unit,
		) {
			val bounds = ScreenRectangle(x, y, width, height)
			val state = NvgRenderState(x, y, width, height, null, bounds, renderContent)
			(graphics as GuiGraphicsExtractorAccessor).edenGuiRenderState().addPicturesInPictureState(state)
		}
	}
}
